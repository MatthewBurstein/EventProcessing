package EventProcessing;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

public class S3Client {
    public AmazonS3 clientBuilder() {
        final AmazonS3 s3 = AmazonS3ClientBuilder
                .standard()
                .withRegion(System.getenv("AWS_REGION"))
                .build();

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
