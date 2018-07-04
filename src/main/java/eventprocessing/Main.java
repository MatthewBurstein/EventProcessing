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
import java.time.Clock;
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
    private static ReadingAggregator readingAggregator;

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
        stopWatch.start();
        int messageCounter = 0;
        while (stopWatch.getTime() < (duration*60000)) {
            ReceiveMessageResult messageResult = sqsClient.getSqs().receiveMessage(receiveMessageRequest);
            messageCounter += messageResult.getMessages().size();
            for (Message msg : messageResult.getMessages()) {
                try {
                    SqsResponse sqsResponse = sqsResponseService.parseResponse(msg.getBody());
                    if(sensorList.isWorkingSensor(sqsResponse)) {
                        readingAggregator.process(sqsResponse);
//                            bucketManager.addResponseToBucket(sqsResponse);
                    }
                } catch (InvalidSqsResponseException e) {
                    logger.warn("Invalid JSON string received" + e.getMessage());
                    logger.warn("Received json string: " + msg.getBody());
                }
            }
        }
        logger.info("total messages received: " + messageCounter);
    }

    private static void createObjects() throws IOException {
        sqsResponseService = new SqsResponseService();
        scanner = new Scanner(System.in);
        amazonController = new AmazonController();
        initialBucket = new InitialBucket();
        stopWatch = new StopWatch();
        csvFileService = new CSVFileService("ResponseData.csv");
        readingAggregator = new ReadingAggregator(csvFileService, Clock.systemUTC());
    }
}
