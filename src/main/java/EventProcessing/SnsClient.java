package EventProcessing;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SnsClient {
    private AmazonSNS sns;
    private final static Logger logger = LogManager.getLogger("SnSClient");

    public AmazonSNS buildSNSClient() {
        try {
            this.sns = AmazonSNSClient
                    .builder()
                    .withRegion(System.getenv("AWS_REGION"))
                    .withCredentials(new EnvironmentVariableCredentialsProvider())
                    .build();
        } catch (SdkClientException e) {
            logger.error("Unable to create AmazonSNSClient");
            logger.error(e.getMessage());
        }
        return sns;
    }

    public AmazonSNS getSns() {
        return sns;
    }
}
