package EventProcessing;

import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.util.Topics;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;

public class Main {
    static Logger logger = LogManager.getLogger(Main.class);
    public static void main(String[] args) throws IOException {
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

        List<Message> messages = sqsClient.getSqs().receiveMessage(new ReceiveMessageRequest(myQueueUrl)).getMessages();
        if (messages.size() > 0) {
            System.out.println(messages.get(0).getBody());
        }

        sqsClient.destroyQueue();
    }

}
