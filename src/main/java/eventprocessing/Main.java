package eventprocessing;

import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import eventprocessing.amazonservices.*;
import eventprocessing.fileservices.JSONParser;
import eventprocessing.models.SensorList;
import eventprocessing.responseservices.ResponseService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class Main {
    static Logger logger = LogManager.getLogger(Main.class);
    public static void main(String[] args) throws IOException, InterruptedException {
        logger.debug("App launched");

        JSONParser jsonParser = new JSONParser("locations.json");
        SensorList sensors = jsonParser.createSensorList();
        sensors.getSensors().forEach(sensor -> System.out.println(sensor.getId()));
        ResponseService responseService = new ResponseService();

        AmazonController amazonController = new AmazonController();
        SqsClient sqsClient = amazonController.getSqsClient();
        String queueUrl = amazonController.getQueueUrl();

        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl);
//        receiveMessageRequest.setWaitTimeSeconds(3);
        int counter = 0;

        while (true) {
            ReceiveMessageResult messageResult = sqsClient.getSqs().receiveMessage(receiveMessageRequest);
            for(Message msg : messageResult.getMessages()) {
                responseService.parseResponse(msg.getBody());
//                System.out.println(msg.getBody());
            }
            System.out.println("=================================================");
            Thread.sleep(1000);
            counter++;
            if (counter > 5) {
                break;
            }
        }

        sqsClient.destroyQueue();
    }

}
