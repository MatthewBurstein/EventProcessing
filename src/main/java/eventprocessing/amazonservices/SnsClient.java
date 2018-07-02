package eventprocessing.amazonservices;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

class SnsClient {
    private AmazonSNS sns;
    private final static Logger logger = LogManager.getLogger("SnSClient");

    AmazonSNS buildSNSClient() {
        try {
            this.sns = AmazonSNSClient
                    .builder()
                    .withRegion(System.getenv(S3Details.awsRegion))
                    .withCredentials(new EnvironmentVariableCredentialsProvider())
                    .build();
            logger.info("SnsClient created");
        } catch (SdkClientException e) {
            logger.error("Unable to create AmazonSNSClient");
            logger.error(e.getMessage());
        }
        return sns;
    }

    AmazonSNS getSns() {
        return sns;
    }
}
