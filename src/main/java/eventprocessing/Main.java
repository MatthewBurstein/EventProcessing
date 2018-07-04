package eventprocessing;

import com.amazonaws.services.sqs.model.*;
import com.amazonaws.services.sqs.model.Message;
import eventprocessing.amazonservices.*;
import eventprocessing.customerrors.InvalidSqsResponseException;
import eventprocessing.fileservices.CSVFileService;
import eventprocessing.fileservices.JSONParser;
import eventprocessing.models.*;
import eventprocessing.responseservices.SqsResponseService;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class Main {
    static Logger logger = LogManager.getLogger(Main.class);

    private static SqsResponseService sqsResponseService;
    private static Scanner scanner;
    private static AmazonController amazonController;
    private static InitialBucket initialBucket;
    private static BucketManager bucketManager;
    private static StopWatch stopWatch;
    private static CSVFileService csvFileService;

    public static void main(String[] args) throws IOException, InterruptedException {
        logger.debug("App launched");
        createObjects();

        csvFileService.deleteOutputFile();

        JSONParser jsonParser = new JSONParser("locations.json");
        SensorList sensorList = jsonParser.createSensorList();

        SqsClient sqsClient = amazonController.getSqsClient();
        String queueUrl = amazonController.getQueueUrl();

        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl);

        System.out.println("How many minutes to run for?\nMUST be at least 1 minute longer than " + GlobalConstants.MAX_MESSAGE_DELAY_MINS + " mins");
        int duration = scanner.nextInt();

        //initial while loop stores five minutes of data with no buckets
        stopWatch.start();
        long tensOfSeconds = 0;
        while (stopWatch.getTime() < GlobalConstants.FIRST_LOOP_DURATION) {
//            if (tensOfSeconds == stopWatch.getTime() / 10000) {
//                GetQueueAttributesRequest attr = new GetQueueAttributesRequest(queueUrl);
//                attr.setAttributeNames(Lists.newArrayList("ApproximateNumberOfMessages"));
//                Map<String, String> attributesMap = sqsClient.getSqs().getQueueAttributes(attr).getAttributes();
//                logger.info("queue size = " + attributesMap.get("ApproximateNumberOfMessages"));
//                tensOfSeconds++;
//            }
            ReceiveMessageResult messageResult = sqsClient.getSqs().receiveMessage(receiveMessageRequest);
            for (Message msg : messageResult.getMessages()) {
                try {
                    SqsResponse sqsResponse = sqsResponseService.parseResponse(msg.getBody());
                    if (!initialBucket.isDuplicateMessage(sqsResponse) && sensorList.isWorkingSensor(sqsResponse)) {
                        initialBucket.addResponse(sqsResponse);
                    }
                } catch (InvalidSqsResponseException e) {
                    logger.warn("Invalid JSON string received" + e.getMessage());
                    logger.warn("Received json string: " + msg.getBody());
                }
            }
        }

        logger.info("Finding earliest timestamp...");
        long earliestTimestamp = initialBucket.getEarliestTimestamp();
        long expiryTime = earliestTimestamp
                + (GlobalConstants.BUCKET_UPPER_BOUND * (duration - GlobalConstants.MAX_MESSAGE_DELAY_MINS + 1))
                + GlobalConstants.MAX_MESSAGE_DELAY_MINS * GlobalConstants.BUCKET_UPPER_BOUND;

        //Initial responses are bucketed
        logger.info("Creating bucket...");

        bucketManager = new BucketManager(earliestTimestamp);

        bucketManager.addMultipleResponsesToBucket(initialBucket);

        List<Bucket> removedBuckets = bucketManager.removeMultipleExpiredBuckets(expiryTime);

        logger.info("Removed buckets: " + removedBuckets);

        csvFileService.writeMultipleBucketDataToFile(removedBuckets);

        bucketManager.createNextBucket();

        //Responses from here bucketed as they come in
        while (stopWatch.getTime() < (duration*60000 - GlobalConstants.FIRST_LOOP_DURATION)) {
            ReceiveMessageResult messageResult = sqsClient.getSqs().receiveMessage(receiveMessageRequest);

            for (Message msg : messageResult.getMessages()) {
                try {
                    SqsResponse sqsResponse = sqsResponseService.parseResponse(msg.getBody());
                    if(!bucketManager.isDuplicateMessage(sqsResponse) && sensorList.isWorkingSensor(sqsResponse)) {
                            bucketManager.addResponseToBucket(sqsResponse);
                    }

                } catch (InvalidSqsResponseException e) {
                    logger.warn("Invalid JSON string received" + e.getMessage());
                    logger.warn("Received json string: " + msg.getBody());
                }
            }
            Bucket removedBucket = bucketManager.removeExpiredBucket(System.currentTimeMillis());
            if (removedBucket != null) {
                csvFileService.write(removedBucket);
                bucketManager.createNextBucket();
            }
        }

    }

    private static void createObjects() throws IOException {
        sqsResponseService = new SqsResponseService();
        scanner = new Scanner(System.in);
        amazonController = new AmazonController();
        initialBucket = new InitialBucket();
        stopWatch = new StopWatch();
        csvFileService = new CSVFileService("ResponseData.csv");
    }
}
