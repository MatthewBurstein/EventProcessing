package EventProcessing;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class S3Client {
    Logger logger = LoggerFactory.getLogger("S3Client");

    public AmazonS3 clientBuilder() {

        final AmazonS3 s3 = AmazonS3ClientBuilder
                .standard()
                .withRegion(System.getenv("AWS_REGION"))
                .build();

//        logger.debug("AWS S3 connection established");

        return s3;
    }

    public S3ObjectInputStream generateS3InputStream() {
        AmazonS3 s3 = clientBuilder();
        S3ObjectInputStream s3is = null;
        try {
            S3Object o = s3.getObject("eventprocessing-suzannejune2018-locationss3bucket-1jlchplwcxr2n", "locations.json");
            s3is = o.getObjectContent();
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
            System.exit(1);
        }
        return s3is;
    }

}
