package eventprocessing;

import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import eventprocessing.amazonservices.AmazonController;
import eventprocessing.amazonservices.SqsClient;
import eventprocessing.customerrors.InvalidSqsResponseException;
import eventprocessing.fileservices.CSVFileService;
import eventprocessing.fileservices.JSONParser;
import eventprocessing.models.*;
import eventprocessing.responseservices.SqsResponseService;
import eventprocessing.threads.SqsClientThread;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.time.Clock;
import java.util.Scanner;

public class Main {
    static Logger logger = LogManager.getLogger(Main.class);

    private static GlobalConstants gc;
    private static SqsResponseService sqsResponseService;
    private static Scanner scanner;
    private static AmazonController amazonController;
    private static StopWatch stopWatch;
    private static CSVFileService csvFileService;
    private static ReadingAggregator readingAggregator;

    public static void main(String[] args) throws IOException {
        SqsClientThread thread = new SqsClientThread("some name");
        thread.start();
        logger.info("App launched");
        createObjects();

        JSONParser jsonParser = new JSONParser("locations.json");
        SensorList sensorList = jsonParser.createSensorList();

        SqsClient sqsClient = amazonController.getSqsClient();
        String queueUrl = amazonController.getQueueUrl();

        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl);
        System.out.println("How many minutes to run for?\nMUST be at least 1 minute longer than " + gc.MAX_MESSAGE_DELAY_MINS + " mins");
        int duration = scanner.nextInt();
        int notWorkingSensorCount = 0;
        stopWatch.start();

        int messageCounter = 0;


//        long tensOfSeconds = 0;

        while (stopWatch.getTime() < (duration*60000)) {

            //KEEP FOR LOGGING QUEUE SIZE
//            if (tensOfSeconds == stopWatch.getTime() / 10000) {
//                GetQueueAttributesRequest attr = new GetQueueAttributesRequest(queueUrl);
//                attr.setAttributeNames(Lists.newArrayList("ApproximateNumberOfMessages"));
//                Map<String, String> attributesMap = sqsClient.getSqs().getQueueAttributes(attr).getAttributes();
//                logger.info("queue size = " + attributesMap.get("ApproximateNumberOfMessages"));
//                tensOfSeconds++;
//            }


            ReceiveMessageResult messageResult = sqsClient.getSqs().receiveMessage(receiveMessageRequest);

            messageCounter += messageResult.getMessages().size();
            for (Message msg : messageResult.getMessages()) {
                try {
                    SqsResponse sqsResponse = sqsResponseService.parseResponse(msg.getBody());
                    Reading reading = new Reading(sqsResponse);
                    if(sensorList.isWorkingSensor(reading)) {
                        readingAggregator.process(reading);
                    } else {
                        notWorkingSensorCount++;
                    }
                } catch (InvalidSqsResponseException e) {
                    logger.error("Invalid JSON string received" + e.getMessage());
                    logger.error("Received json string: " + msg.getBody());
                }

            final String messageReceiptHandle = messageResult.getMessages().get(0).getReceiptHandle();
            sqsClient.getSqs().deleteMessage(new DeleteMessageRequest(queueUrl, messageReceiptHandle));
            }


        }
        readingAggregator.processAllBuckets();
        logger.info("Total messages received: " + messageCounter);
        logger.info("Total duplicates in this run: " + readingAggregator.getDuplicateCounter());
        logger.info("Total faulty sensors in this run: " + notWorkingSensorCount);

    }

    private static void createObjects() throws IOException {
        gc = new GlobalConstants(60, 5);
        sqsResponseService = new SqsResponseService();
        scanner = new Scanner(System.in);
        amazonController = new AmazonController();
        stopWatch = new StopWatch();
        csvFileService = new CSVFileService("ResponseData" + System.currentTimeMillis() + ".csv");
        readingAggregator = new ReadingAggregator(csvFileService, Clock.systemUTC(), gc);
    }
}
