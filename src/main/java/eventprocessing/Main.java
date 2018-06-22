package eventprocessing;

import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.sns.util.Topics;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import eventprocessing.amazonservices.*;
import eventprocessing.fileservices.JSONParser;
import eventprocessing.models.Sensor;
import eventprocessing.models.SensorList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;

public class Main {
    static Logger logger = LogManager.getLogger(Main.class);
    public static void main(String[] args) throws IOException, InterruptedException {
        logger.debug("App launched");

        JSONParser jsonParser = new JSONParser("locations.json");
        SensorList sensors = jsonParser.createSensorList();
        sensors.getSensors().forEach(sensor -> System.out.println(sensor.getId()));

        AmazonController amazonController = new AmazonController();
        SqsClient sqsClient = amazonController.getSqsClient();
        String queueUrl = amazonController.getQueueUrl();

        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl);
//        receiveMessageRequest.setWaitTimeSeconds(3);
        int counter = 0;

        while (true) {
            ReceiveMessageResult messageResult = sqsClient.getSqs().receiveMessage(receiveMessageRequest);
            for(Message msg : messageResult.getMessages()) {
                System.out.println(msg);
            }
            System.out.println("=================================================");
            Thread.sleep(4000);
            counter++;
            if (counter > 10) {
                break;
            }
        }

        sqsClient.destroyQueue();
    }

}
