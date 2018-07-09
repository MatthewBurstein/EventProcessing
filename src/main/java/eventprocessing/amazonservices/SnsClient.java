package eventprocessing.amazonservices;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

class SnsClient {
    private AmazonSNS sns;
    private static final Logger logger = LogManager.getLogger("S3Client");

    AmazonSNS buildSNSClient() {
        try {
            this.sns = AmazonSNSClient
                    .builder()
                    .withRegion(S3Details.awsRegion)
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
