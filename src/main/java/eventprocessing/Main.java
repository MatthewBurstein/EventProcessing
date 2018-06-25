package eventprocessing;

import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import eventprocessing.amazonservices.*;
import eventprocessing.fileservices.JSONParser;
import eventprocessing.models.Response;
import eventprocessing.models.SensorList;
import eventprocessing.responseservices.ResponseProcessor;
import eventprocessing.responseservices.ResponseService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class Main {
    static Logger logger = LogManager.getLogger(Main.class);
    public static void main(String[] args) throws IOException, InterruptedException {
        logger.debug("App launched");

        JSONParser jsonParser = new JSONParser("locations.json");
        SensorList sensorList = jsonParser.createSensorList();
        sensorList.getSensors().forEach(sensor -> System.out.println(sensor.getId()));
        ResponseService responseService = new ResponseService();
        ResponseProcessor responseProcessor = new ResponseProcessor();

        AmazonController amazonController = new AmazonController();
        SqsClient sqsClient = amazonController.getSqsClient();
        String queueUrl = amazonController.getQueueUrl();

        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl);
        int counter = 0;

        while (true) {
            ReceiveMessageResult messageResult = sqsClient.getSqs().receiveMessage(receiveMessageRequest);
            for(Message msg : messageResult.getMessages()) {
                Response response = responseService.parseResponse(msg.getBody());
                if (responseProcessor.isWorkingSensor(response, sensorList)) {
                    System.out.println("working sensor with id: " + response.getMessage().getLocationId());
                    System.out.println(msg.getBody());
                } else {
                    System.out.println("sensor not working with id: " + response.getMessage().getLocationId());
                }
            }
            System.out.println("=================================================");
            Thread.sleep(1000);
            counter++;
            if (counter > 10) {
                break;
            }
        }

        sqsClient.destroyQueue();
    }

}
