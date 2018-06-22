package eventprocessing;

import com.amazonaws.services.s3.model.S3ObjectInputStream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class DataReader {
    public void saveToFile(S3ObjectInputStream amazonS3Stream) throws IOException {
        FileOutputStream fos = new FileOutputStream(new File("locations.json"));
        byte[] read_buf = new byte[1024];
        int read_len;
        while ((read_len = amazonS3Stream.read(read_buf)) > 0) {
            fos.write(read_buf, 0, read_len);
        }
        amazonS3Stream.close();
        fos.close();
    }

}
