package EventProcessing;

import com.amazonaws.services.s3.model.S3ObjectInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Main {
    static Logger logger = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) throws IOException {

        S3ObjectInputStream s3is = new S3Client().generateS3InputStream();
        DataReader reader = new DataReader();
        logger.debug("AWS S3 connection established");
        reader.saveToFile(s3is);
    }

}
