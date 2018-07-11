package eventprocessing;

import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.google.common.collect.Lists;
import eventprocessing.amazonservices.AmazonController;
import eventprocessing.amazonservices.SqsClient;
import eventprocessing.customerrors.InvalidSqsResponseException;
import eventprocessing.fileservices.CSVFileService;
import eventprocessing.fileservices.JSONParser;
import eventprocessing.models.*;
import eventprocessing.responseservices.SqsResponseService;
import eventprocessing.threads.SqsClientThread;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.time.Clock;
import java.util.Map;
import java.util.Scanner;

public class Main {
    private static Logger logger = LogManager.getLogger(Main.class);

    private static GlobalConstants gc;
    private static Scanner scanner;
    private static AmazonController amazonController;
    private static ReadingAggregator readingAggregator;
    private static TemporarySqsResponseStorage temporarySqsResponseStorage;
    private static String sensorsFileName = "locations.json";
    private static SensorList sensorList;
    private static SqsResponseService sqsResponseService;

    public static void main(String[] args) throws IOException {
        logger.info("App launched");

        JSONParser jsonParser = new JSONParser(sensorsFileName);
        sensorList = jsonParser.createSensorList();

        createObjects();

        SqsClient sqsClient = amazonController.getSqsClient();
        String queueUrl = amazonController.getQueueUrl();

        System.out.println("How many minutes to run for?\nMUST be at least 1 minute longer than " + gc.MAX_MESSAGE_DELAY_MINS + " mins");
        int duration = scanner.nextInt();

        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl);
        SqsClientThread sqsClientThread1 = createAndStartSqsClientThread(sqsClient, receiveMessageRequest, queueUrl);
        SqsClientThread sqsClientThread2 = createAndStartSqsClientThread(sqsClient, receiveMessageRequest, queueUrl);
        SqsClientThread sqsClientThread3 = createAndStartSqsClientThread(sqsClient, receiveMessageRequest, queueUrl);

        runForMinutes(duration, sqsClient, queueUrl);

        sqsClientThread1.terminate();
        sqsClientThread2.terminate();
        sqsClientThread3.terminate();

        sqsClient.destroyQueue();
    }

    private static SqsClientThread createAndStartSqsClientThread(SqsClient sqsClient, ReceiveMessageRequest receiveMessageRequest, String queueUrl) {
        SqsClientThread sqsClientThread = new SqsClientThread(temporarySqsResponseStorage, sqsClient, receiveMessageRequest, queueUrl);
        sqsClientThread.start();
        return sqsClientThread;
    }

    private static void runForMinutes(int duration, SqsClient sqsClient, String queueUrl) {
        long tensOfSeconds = System.currentTimeMillis() / 10000;
        long endTime = System.currentTimeMillis() + duration * 60000;

        while (System.currentTimeMillis() < endTime) {
            tensOfSeconds = logSqsQueueSizeEveryTenSeconds(sqsClient, queueUrl, tensOfSeconds);
            processResponses();
        }
        finaliseResponses();
    }

    private static void finaliseResponses() {
        readingAggregator.finalise();
        readingAggregator.getTotalMessageCount();
    }

    private static void processResponses() {
        ReceiveMessageResult messageResult = getMessageResultFromStorage();
        processMessageResult(messageResult);
    }

    private static ReceiveMessageResult getMessageResultFromStorage() {
        try {
            ReceiveMessageResult messageResult = temporarySqsResponseStorage.take();
            return messageResult;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void processMessageResult(ReceiveMessageResult messageResult) {
        for (Message msg : messageResult.getMessages()) {
            try {
                processMessage(msg);
            } catch (InvalidSqsResponseException e) {
                logger.error("Invalid JSON string received" + e.getMessage());
                logger.error("Received json string: " + msg.getBody());
            }
        }
    }

    private static void processMessage(Message msg) {
        SqsResponse sqsResponse = sqsResponseService.parseResponse(msg.getBody());
        Reading reading = new Reading(sqsResponse);
        readingAggregator.process(reading);
    }
    private static long logSqsQueueSizeEveryTenSeconds(SqsClient sqsClient, String queueUrl, long tensOfSeconds) {
        if (tensOfSeconds == System.currentTimeMillis() / 10000) {
            GetQueueAttributesRequest attr = new GetQueueAttributesRequest(queueUrl);
            attr.setAttributeNames(Lists.newArrayList("ApproximateNumberOfMessages"));
            Map<String, String> attributesMap = sqsClient.getSqs().getQueueAttributes(attr).getAttributes();
            logger.info("Queue size = " + attributesMap.get("ApproximateNumberOfMessages"));
            tensOfSeconds++;
        }
        return tensOfSeconds;
    }

    private static void createObjects() throws IOException {
        gc = new GlobalConstants(60, 5);
        scanner = new Scanner(System.in);
        amazonController = new AmazonController(sensorsFileName);
        long fileCreationTime =  System.currentTimeMillis();
        CSVFileService csvFileService = new CSVFileService(fileCreationTime + "ResponseData" + ".csv",
                fileCreationTime + "SensorData"  + ".csv");
        readingAggregator = new ReadingAggregator(csvFileService, Clock.systemUTC(), gc, sensorList);
        temporarySqsResponseStorage = new TemporarySqsResponseStorage();
        sqsResponseService = new SqsResponseService();
    }
}
