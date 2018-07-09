package eventprocessing.amazonservices;

import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.sns.util.Topics;
import eventprocessing.fileservices.S3Interpreter;

import java.io.IOException;

public class AmazonController {

    private SqsClient sqsClient;

    public AmazonController(String s3InterpreterFileName) throws IOException {
        S3ObjectInputStream s3is = new S3Client().generateS3InputStream();
        S3Interpreter s3Interpreter = new S3Interpreter();
        s3Interpreter.saveToFile(s3is, s3InterpreterFileName);

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
