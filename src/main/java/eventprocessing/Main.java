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
    private static InitialResponseList initialResponseList;
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

        System.out.println("For how long do you want to run the event processor(seconds)?");
        int duration = scanner.nextInt();

        System.out.println("Please wait 5 minutes while the initial set of responses is compiled...");
        int messageCounter = 0;
        //initial while loop stores five minutes of data with no buckets
        while (stopWatch.getTime() < 300000) {
            ReceiveMessageResult messageResult = sqsClient.getSqs().receiveMessage(receiveMessageRequest);
            for (Message msg : messageResult.getMessages()) {
                Response response = responseService.parseResponse(msg.getBody());
                if (responseProcessor.isValidMessage(response, sensorList, initialResponseList)) {
//                    System.out.println("working sensor with id: " + response.getMessage().getLocationId());
//                    System.out.println(msg.getBody());
                    initialResponseList.addResponse(response);
                }
            }
            if (initialResponseList.getResponses().size()/10 > messageCounter) {
                System.out.println(initialResponseList.getResponses().size() + " messages stored");
            }
            messageCounter = initialResponseList.getResponses().size()/10;
        }
        logger.info("Finding earliest timestamp...");
        long earliestTimestamp = initialResponseList.getEarliestTimestamp();

        //Initial responses are bucketed
        logger.info("Creating bucket...");
        bucketManager = new BucketManager(earliestTimestamp, stopWatch);
        bucketManager.addMultipleResponsesToBucket(initialResponseList);

        ResponseList removedBucket = bucketManager.removeExpiredBucket();

        if (removedBucket != null) {
            csvFileService.writeToFile(removedBucket);
        }

        //Responses from here bucketed as they come in
//        while (true) {
//            ResponseList removedBucket = bucketManager.removeExpiredBucket();
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
        int MAX_SIZE = 300;
        responseService = new ResponseService();
        responseProcessor = new ResponseProcessor();
        messageLog = new MessageLog(MAX_SIZE);
        scanner = new Scanner(System.in);
        amazonController = new AmazonController();
        initialResponseList = new InitialResponseList();
        analyser = new Analyser();
        stopWatch = new StopWatch();
        csvFileService = new CSVFileService();
    }
}
