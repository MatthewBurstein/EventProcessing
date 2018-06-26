package eventprocessing;

import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import eventprocessing.amazonservices.*;
import eventprocessing.fileservices.JSONParser;
import eventprocessing.models.Response;
import eventprocessing.models.ResponseList;
import eventprocessing.models.SensorList;
import eventprocessing.responseservices.ResponseProcessor;
import eventprocessing.responseservices.ResponseService;
import eventprocessing.storage.MessageLog;
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
    private static ResponseList responseList;

    public static void main(String[] args) throws IOException, InterruptedException {
        logger.debug("App launched");
        createObjects();
        JSONParser jsonParser = new JSONParser("locations.json");
        SensorList sensorList = jsonParser.createSensorList();
        sensorList.getSensors().forEach(sensor -> System.out.println(sensor.getId()));

        SqsClient sqsClient = amazonController.getSqsClient();
        String queueUrl = amazonController.getQueueUrl();

        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl);
        int counter = 0;

        System.out.println("For how long do you want to run the event processor(seconds)?");
        int duration = scanner.nextInt();

        while (true) {
            ReceiveMessageResult messageResult = sqsClient.getSqs().receiveMessage(receiveMessageRequest);
            for(Message msg : messageResult.getMessages()) {
                Response response = responseService.parseResponse(msg.getBody());
                if (responseProcessor.isValidMessage(response, sensorList, messageLog)) {
                    System.out.println("working sensor with id: " + response.getMessage().getLocationId());
                    System.out.println(msg.getBody());
                    responseList.getResponses().add(response);
                    messageLog.getMessageHistory().add(response.messageId);
                    messageLog.truncateIfExceedsMaxSize();
                } else {
                    System.out.println("sensor not working with id: " + response.getMessage().getLocationId());
                }
            }
            System.out.println("=================================================");
            Thread.sleep(1000);
            counter++;
            if (counter > duration) {
                sqsClient.destroyQueue();
                break;
            }
        }
    }

    private static void createObjects() throws IOException {
        int MAX_SIZE = 300;
        responseService = new ResponseService();
        responseProcessor = new ResponseProcessor();
        messageLog = new MessageLog(MAX_SIZE);
        scanner = new Scanner(System.in);
        amazonController = new AmazonController();
        responseList = new ResponseList();
    }
}
