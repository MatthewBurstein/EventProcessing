package eventprocessing;

import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import eventprocessing.amazonservices.*;
import eventprocessing.analysis.Analyser;
import eventprocessing.fileservices.CSVFileService;
import eventprocessing.fileservices.JSONParser;
import eventprocessing.models.*;
import eventprocessing.responseservices.ResponseProcessor;
import eventprocessing.responseservices.ResponseService;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Scanner;

public class Main {
    static Logger logger = LogManager.getLogger(Main.class);

    private static ResponseService responseService;
    private static ResponseProcessor responseProcessor;
    private static Scanner scanner;
    private static AmazonController amazonController;
    private static InitialBucket initialBucket;
    private static BucketManager bucketManager;
    private static Analyser analyser;
    private static StopWatch stopWatch;
    private static CSVFileService csvFileService;

    public static void main(String[] args) throws IOException, InterruptedException {
        logger.debug("App launched");
        createObjects();
        stopWatch.start();


        JSONParser jsonParser = new JSONParser("locations.json");
        SensorList sensorList = jsonParser.createSensorList();
        sensorList.getSensors().forEach(sensor -> System.out.println(sensor.getId()));

        SqsClient sqsClient = amazonController.getSqsClient();
        String queueUrl = amazonController.getQueueUrl();

        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl);

        System.out.println("How many minutes to run for?\nMUST be at least 1 minute longer than " + GlobalConstants.MAX_MESSAGE_DELAY_MINS + " mins");
        int duration = scanner.nextInt();

//        System.out.println("Please wait " + GlobalConstants.MAX_MESSAGE_DELAY_MINS + " minutes while the initial set of responses is compiled...");
        int messageCounter = 0;

        //initial while loop stores five minutes of data with no buckets
        while (stopWatch.getTime() < (duration*60000 + 1000)) {
            ReceiveMessageResult messageResult = sqsClient.getSqs().receiveMessage(receiveMessageRequest);
            for (Message msg : messageResult.getMessages()) {
                Response response = responseService.parseResponse(msg.getBody());
                if (responseProcessor.isValidMessage(response, sensorList, initialBucket)) {
                    initialBucket.addResponse(response);
                }
            }
            if (initialBucket.getResponses().size() / GlobalConstants.MULTIPLES_OF_MESSAGES > messageCounter) {
                System.out.println(initialBucket.getResponses().size() + " messages stored");
            }
            messageCounter = initialBucket.getResponses().size() / GlobalConstants.MULTIPLES_OF_MESSAGES;
        }

        System.out.println("RESPONSE TIMESTAMPS");
        initialBucket.getResponses().forEach(response -> {
            System.out.println(response.getTimestamp());
//            System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        });
        System.out.println("++++++++++++++++++++++++++++++++++++++++");

        logger.info("Finding earliest timestamp...");
        long earliestTimestamp = initialBucket.getEarliestTimestamp();
        long expiryTime = earliestTimestamp + GlobalConstants.MAX_MESSAGE_DELAY_MINS * GlobalConstants.BUCKET_UPPER_BOUND;

        System.out.println("stopwatch.getStartTime = " + stopWatch.getStartTime());
        System.out.println("earliest Time stamp " + earliestTimestamp);

        //Initial responses are bucketed
        logger.info("Creating bucket...");

        bucketManager = new BucketManager(earliestTimestamp, stopWatch);

        bucketManager.addMultipleResponsesToBucket(initialBucket);

        bucketManager.getBuckets().forEach(bucket -> {
            System.out.println("BucketManager bucket isExpiredAtTime" + bucket.isExpiredAtTime(expiryTime));
            System.out.println("BucketManager bucket message IDs" + bucket.getMessageIds());
            System.out.println("BucketManager bucket timerange " + bucket.getTimeRange());
            System.out.println("BucketManager bucket number of responses " + bucket.getResponses().size());
            System.out.println("--------------------------------------------------------------------------");
        });


        Bucket removedBucket = bucketManager.removeExpiredBucket(expiryTime);
        System.out.println("Removed bucket " + removedBucket);

        if (removedBucket != null) {
            csvFileService.writeToFile(removedBucket);
        }

        //Responses from here bucketed as they come in
//        while (true) {
//            Bucket removedBucket = bucketManager.removeExpiredBucket();
//
//            csvFileService.writeToFile(removedBucket);
//
//            ReceiveMessageResult messageResult = sqsClient.getSqs().receiveMessage(receiveMessageRequest);
//            for (Message msg : messageResult.getMessages()) {
//                Response response = responseService.parseResponse(msg.getBody());
//                if (responseProcessor.isValidMessage(response, sensorList, messageLog)) {
//                    System.out.println("working sensor with id: " + response.getMessage().getLocationId());
//                    System.out.println(msg.getBody());
////                    responseList.getResponses().add(response);
//                    bucketManager.addResponseToBucket(response);
//                    messageLog.getMessageHistory().add(response.messageId);
//                    messageLog.truncateIfExceedsMaxSize();
//                } else {
//                    System.out.println("sensor not working with id: " + response.getMessage().getLocationId());
//                }
//            }
//            System.out.println("=================================================");
//            Thread.sleep(1000);
//
////            double averageValue = analyser.getAverageValue(responseList);
////            System.out.println("Cumulative average of sensor values: " + averageValue);
//
//            if (stopWatch.getTime() > duration) {
//                sqsClient.destroyQueue();
//                break;
//            }
//        }
    }

    private static void createObjects() throws IOException {
        responseService = new ResponseService();
        responseProcessor = new ResponseProcessor();
        scanner = new Scanner(System.in);
        amazonController = new AmazonController();
        initialBucket = new InitialBucket();
        analyser = new Analyser();
        stopWatch = new StopWatch();
        csvFileService = new CSVFileService();
    }
}
