package EventProcessing;

import com.amazonaws.services.s3.model.S3ObjectInputStream;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        S3ObjectInputStream s3is = new S3Client().generateS3InputStream();
        DataReader reader = new DataReader();
        reader.saveToFile(s3is);
    }

}
