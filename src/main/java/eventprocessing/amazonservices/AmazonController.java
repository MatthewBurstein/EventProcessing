package eventprocessing.amazonservices;

import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.sns.util.Topics;
import eventprocessing.fileservices.S3Interpreter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class AmazonController {

    private static final Logger logger = LogManager.getLogger("AmazonController");
    private SqsClient sqsClient;

    public AmazonController() throws IOException {
        S3ObjectInputStream s3is = new S3Client().generateS3InputStream();
        S3Interpreter reader = new S3Interpreter();
        reader.saveToFile(s3is);

        this.sqsClient = new SqsClient();
        sqsClient.buildSQSClient();
        String myQueueUrl = sqsClient.getQueueUrl();

        SnsClient snsClient = new SnsClient();
        snsClient.buildSNSClient();
        Topics.subscribeQueue(snsClient.getSns(), sqsClient.getSqs(), S3Details.arnTopic, myQueueUrl);

    }

    public SqsClient getSqsClient() {
        return sqsClient;
    }

    public String getQueueUrl() {
        return sqsClient.getQueueUrl();
    }
}
