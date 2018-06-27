package eventprocessing;

import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import eventprocessing.amazonservices.*;
import eventprocessing.analysis.Analyser;
import eventprocessing.fileservices.JSONParser;
import eventprocessing.models.*;
import eventprocessing.responseservices.ResponseProcessor;
import eventprocessing.responseservices.ResponseService;
import eventprocessing.storage.MessageLog;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Scanner;

public class Main {
    static Logger logger = LogManager.getLogger(Main.class);

    private static ResponseService responseService;
    private static ResponseProcessor responseProcessor;
    private static MessageLog messageLog;
    private static Scanner scanner;
    private static AmazonController amazonController;
    private static InitialResponseList initialResponseList;
    private static BucketManager bucketManager;
    private static Analyser analyser;
    private static StopWatch stopWatch;

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
        int counter = 0;

        System.out.println("For how long do you want to run the event processor(seconds)?");
        int duration = scanner.nextInt();

        while (stopWatch.getTime() < 300000) {
            ReceiveMessageResult messageResult = sqsClient.getSqs().receiveMessage(receiveMessageRequest);
            for (Message msg : messageResult.getMessages()) {
                Response response = responseService.parseResponse(msg.getBody());
                if (responseProcessor.isValidMessage(response, sensorList, messageLog)) {
//                    System.out.println("working sensor with id: " + response.getMessage().getLocationId());
//                    System.out.println(msg.getBody());
                    initialResponseList.addResponse(response);
                    messageLog.addMessageHistory(response.messageId);
                    messageLog.truncateIfExceedsMaxSize();
                }
            }
        }

                System.out.println("EXITED FIRST WHILE LOOP");

                while (true) {
                    ReceiveMessageResult messageResult = sqsClient.getSqs().receiveMessage(receiveMessageRequest);
                    for (Message msg : messageResult.getMessages()) {
                        Response response = responseService.parseResponse(msg.getBody());
                        if (responseProcessor.isValidMessage(response, sensorList, messageLog)) {
                            System.out.println("working sensor with id: " + response.getMessage().getLocationId());
                            System.out.println(msg.getBody());
//                    responseList.getResponses().add(response);
                            messageLog.getMessageHistory().add(response.messageId);
                            messageLog.truncateIfExceedsMaxSize();
                        } else {
                            System.out.println("sensor not working with id: " + response.getMessage().getLocationId());
                        }
                    }
                    System.out.println("=================================================");
                    Thread.sleep(1000);

//            double averageValue = analyser.getAverageValue(responseList);
//            System.out.println("Cumulative average of sensor values: " + averageValue);

                    counter++;
                    if (counter > duration) {
                        sqsClient.destroyQueue();
                        break;
                    }
                }
            }

            private static void createObjects () throws IOException {
                int MAX_SIZE = 300;
                responseService = new ResponseService();
                responseProcessor = new ResponseProcessor();
                messageLog = new MessageLog(MAX_SIZE);
                scanner = new Scanner(System.in);
                amazonController = new AmazonController();
                initialResponseList = new InitialResponseList();
                analyser = new Analyser();
                stopWatch = new StopWatch();
            }
        }
