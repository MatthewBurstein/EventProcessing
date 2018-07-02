package eventprocessing.amazonservices;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.services.sqs.model.DeleteQueueRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class SqsClient {
    private AmazonSQS sqs;
    private String queueUrl;
    private static Logger logger = LogManager.getLogger("SqsClient");

    AmazonSQS buildSQSClient() {
        try {
            this.sqs = AmazonSQSClient
                    .builder()
                    .withRegion(System.getenv(S3Details.awsRegion))
                    .withCredentials(new EnvironmentVariableCredentialsProvider())
                    .build();
        } catch (SdkClientException e) {
            logger.error("Unable to create AmazonSQSClient");
            logger.error(e.getMessage());
        }
        return sqs;
    }

    String getQueueUrl(){
        logger.info("Creating queue: " + S3Details.queueName);
        CreateQueueRequest createQueueRequest = new CreateQueueRequest(S3Details.queueName);
        CreateQueueResult queue = sqs.createQueue(createQueueRequest);
        this.queueUrl = queue.getQueueUrl();
        logger.info("Queue " + S3Details.queueName + " created at " + queueUrl);
        return queueUrl;
    }

    public AmazonSQS getSqs() {
        return sqs;
    }

    public void destroyQueue() {
        logger.info("Destroying queue " + S3Details.queueName + " at " + queueUrl);
        sqs.deleteQueue(new DeleteQueueRequest(queueUrl));
    }
}
