package EventProcessing;

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.services.sqs.model.ListQueuesResult;

public class SqsClient {
    private final AmazonSQS sqs;

    public SqsClient() {
        this.sqs = AmazonSQSClient
                .builder()
                .withRegion(System.getenv("AWS_REGION"))
                .withCredentials(new EnvironmentVariableCredentialsProvider())
                .build();
    }

    public String getQueueUrl(){
        CreateQueueRequest createQueueRequest = new CreateQueueRequest(S3Details.queueName);
        CreateQueueResult queue = sqs.createQueue(createQueueRequest);
        String myQueueUrl = queue.getQueueUrl();
        return myQueueUrl;
    }

    public AmazonSQS getSqs() {
        return sqs;
    }

}
