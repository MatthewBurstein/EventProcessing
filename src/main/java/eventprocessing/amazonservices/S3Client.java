package eventprocessing.amazonservices;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class S3Client {
    Logger logger = LogManager.getLogger("S3Client");

    public AmazonS3 clientBuilder() {

        final AmazonS3 s3 = AmazonS3ClientBuilder
                .standard()
                .withRegion(System.getenv("AWS_REGION"))
                .build();

        logger.debug("AWS S3 connection established");

        return s3;
    }

    public S3ObjectInputStream generateS3InputStream() {
        AmazonS3 s3 = clientBuilder();
        try {
            S3Object o = s3.getObject(S3Details.s3BucketLocation, S3Details.s3Key);
            S3ObjectInputStream s3is = o.getObjectContent();
            return s3is;
        } catch (AmazonServiceException e) {
            logger.fatal("Fatal error retrieving locations.json from S3 Bucket - " + e.getErrorMessage());
            throw new AmazonServiceException(e.getMessage());
        }

    }

}
