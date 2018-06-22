package EventProcessing;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.CreateTopicResult;

public class SnsClient {
    private final AmazonSNS sns;

    public SnsClient() {
        this.sns = AmazonSNSClient
                .builder()
                .withRegion(System.getenv("AWS_REGION"))
                .withCredentials(new EnvironmentVariableCredentialsProvider())
                .build();
    }

    public String getTopicArn() {
        CreateTopicRequest createTopicRequest = new CreateTopicRequest(S3Details.arnTopic);
        CreateTopicResult topic = sns.createTopic(createTopicRequest);
        String myTopicArn = topic.getTopicArn();
        return myTopicArn;
    }

    public AmazonSNS getSns() {
        return sns;
    }
}
