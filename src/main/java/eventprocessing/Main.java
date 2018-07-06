package eventprocessing;

import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.google.common.collect.Lists;
import eventprocessing.amazonservices.AmazonController;
import eventprocessing.amazonservices.SqsClient;
import eventprocessing.fileservices.CSVFileService;
import eventprocessing.fileservices.JSONParser;
import eventprocessing.models.ReadingAggregator;
import eventprocessing.models.SensorList;
import eventprocessing.models.TemporarySqsResponseStorage;
import eventprocessing.responseservices.SqsResponseService;
import eventprocessing.threads.ResponseProcessorThread;
import eventprocessing.threads.SqsClientThread;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.time.Clock;
import java.util.Map;
import java.util.Scanner;

public class Main {
    static Logger logger = LogManager.getLogger(Main.class);

    private static GlobalConstants gc;
    private static SqsResponseService sqsResponseService;
    private static Scanner scanner;
    private static AmazonController amazonController;
    private static CSVFileService csvFileService;
    private static ReadingAggregator readingAggregator;
    private static TemporarySqsResponseStorage temporarySqsResponseStorage;

    public static void main(String[] args) throws IOException, InterruptedException {
        logger.info("App launched");
        createObjects();

        JSONParser jsonParser = new JSONParser("locations.json");
        SensorList sensorList = jsonParser.createSensorList();

        SqsClient sqsClient = amazonController.getSqsClient();
        String queueUrl = amazonController.getQueueUrl();

        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl);
        System.out.println("How many minutes to run for?\nMUST be at least 1 minute longer than " + gc.MAX_MESSAGE_DELAY_MINS + " mins");
        int duration = scanner.nextInt();


        SqsClientThread sqsClientThread1 = createAndStartSqsClientThread(sqsClient, receiveMessageRequest, queueUrl);
        SqsClientThread sqsClientThread2 = createAndStartSqsClientThread(sqsClient, receiveMessageRequest, queueUrl);
        SqsClientThread sqsClientThread3 = createAndStartSqsClientThread(sqsClient, receiveMessageRequest, queueUrl);
        ResponseProcessorThread responseProcessorThread = createAndStartResponseProcessorThread(sensorList);

        runForMinutes(duration, sqsClient, queueUrl);

        sqsClientThread1.terminate();
        sqsClientThread2.terminate();
        sqsClientThread3.terminate();
        responseProcessorThread.terminate();
    }

    private static ResponseProcessorThread createAndStartResponseProcessorThread(SensorList sensorList) {
        ResponseProcessorThread responseProcessorThread = new ResponseProcessorThread(temporarySqsResponseStorage, sensorList, readingAggregator);
        responseProcessorThread.start();
        return responseProcessorThread;
    }

    private static void runForMinutes(int duration, SqsClient sqsClient, String queueUrl) {
        long tensOfSeconds = System.currentTimeMillis() / 10000;
        long endTime = System.currentTimeMillis() + duration * 60000;
        while (System.currentTimeMillis() < endTime) {
//      Uncomment to log queue size
            if (tensOfSeconds == System.currentTimeMillis() / 10000) {
                GetQueueAttributesRequest attr = new GetQueueAttributesRequest(queueUrl);
                attr.setAttributeNames(Lists.newArrayList("ApproximateNumberOfMessages"));
                Map<String, String> attributesMap = sqsClient.getSqs().getQueueAttributes(attr).getAttributes();
                logger.info("queue size = " + attributesMap.get("ApproximateNumberOfMessages"));
                tensOfSeconds++;
            }
        }
    }

    private static SqsClientThread createAndStartSqsClientThread(SqsClient sqsClient, ReceiveMessageRequest receiveMessageRequest, String queueUrl) {
        SqsClientThread sqsClientThread = new SqsClientThread(temporarySqsResponseStorage, sqsClient, receiveMessageRequest, queueUrl);
        sqsClientThread.start();
        return sqsClientThread;
    }

    private static void createObjects() throws IOException {
        gc = new GlobalConstants(60, 5);
        sqsResponseService = new SqsResponseService();
        scanner = new Scanner(System.in);
        amazonController = new AmazonController();
        csvFileService = new CSVFileService("ResponseData" + System.currentTimeMillis() + ".csv");
        readingAggregator = new ReadingAggregator(csvFileService, Clock.systemUTC(), gc);
        temporarySqsResponseStorage = new TemporarySqsResponseStorage();
    }
}
