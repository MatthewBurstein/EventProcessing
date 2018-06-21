package EventProcessing;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class Main {

    public static void main(String[] args) {
        final AmazonS3 s3 = AmazonS3ClientBuilder
                .standard()
                .withRegion(System.getenv("AWS_REGION"))
                .build();
        try {
            S3Object o = s3.getObject("eventprocessing-suzannejune2018-locationss3bucket-1jlchplwcxr2n", "locations.json");
            S3ObjectInputStream s3is = o.getObjectContent();
//            FileOutputStream fos = new FileOutputStream(new File("locations.json"));
            byte[] read_buf = new byte[1024];
            int read_len = 0;
            while ((read_len = s3is.read(read_buf)) > 0) {
                System.out.println(read_buf);
                System.out.println(read_len);
//                fos.write(read_buf, 0, read_len);
            }
            s3is.close();
//            fos.close();

        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
            System.exit(1);
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

}
