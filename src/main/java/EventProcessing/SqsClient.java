package EventProcessing;

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.services.sqs.model.ListQueuesResult;

public class SqsClient {
    private final AmazonSQS sqs;

    public SqsClient() {
        this.sqs = new AmazonSQSClient(new EnvironmentVariableCredentialsProvider());
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

    public void listQueues() {
        ListQueuesResult result = sqs.listQueues();
        for(String url : result.getQueueUrls()) {
            System.out.println(url);
        }
    }


}
