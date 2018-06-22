package EventProcessing;

import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.sns.util.Topics;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;

public class Main {
    static Logger logger = LogManager.getLogger(Main.class);
    public static void main(String[] args) throws IOException, InterruptedException {
        logger.debug("App launched");

        S3ObjectInputStream s3is = new S3Client().generateS3InputStream();
        DataReader reader = new DataReader();
        reader.saveToFile(s3is);

        SqsClient sqsClient= new SqsClient();
        sqsClient.buildSQSClient();
        String myQueueUrl = sqsClient.getQueueUrl();

        SnsClient snsClient = new SnsClient();
        snsClient.buildSNSClient();

        Topics.subscribeQueue(snsClient.getSns(), sqsClient.getSqs(), S3Details.arnTopic, myQueueUrl);

        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(myQueueUrl);
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
