package eventprocessing.amazonservices;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

class S3Client {
    private final Logger logger = LogManager.getLogger("S3Client");

    private AmazonS3 clientBuilder() {

        final AmazonS3 s3 = AmazonS3ClientBuilder
                .standard()
                .withRegion(S3Details.awsRegion)
                .build();

        logger.debug("AWS S3 connection established");

        return s3;
    }

    S3ObjectInputStream generateS3InputStream() {
        AmazonS3 s3 = clientBuilder();
        try {
            S3Object s3Object = s3.getObject(S3Details.s3BucketLocation, S3Details.s3Key);
            return s3Object.getObjectContent();
        } catch (AmazonServiceException e) {
            logger.fatal("Fatal error retrieving locations.json from S3 Bucket - " + e.getErrorMessage());
            throw e;
        }

    }

}
