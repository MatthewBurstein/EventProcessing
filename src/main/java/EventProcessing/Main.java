package EventProcessing;

import com.amazonaws.services.s3.model.S3ObjectInputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class Main {
    static Logger logger = LogManager.getLogger(Main.class);
    public static void main(String[] args) throws IOException {
        logger.debug("App launched");

        S3ObjectInputStream s3is = new S3Client().generateS3InputStream();
        DataReader reader = new DataReader();
        reader.saveToFile(s3is);
    }

}
