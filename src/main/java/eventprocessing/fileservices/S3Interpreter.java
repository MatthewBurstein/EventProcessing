package eventprocessing.fileservices;

import com.amazonaws.services.s3.model.S3ObjectInputStream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class S3Interpreter {
    public void saveToFile(S3ObjectInputStream amazonS3Stream, String fileSaveLocation) throws IOException {
        FileOutputStream fos = new FileOutputStream(new File(fileSaveLocation));
        byte[] read_buf = new byte[1024];
        int read_len;
        while ((read_len = amazonS3Stream.read(read_buf)) > 0) {
            fos.write(read_buf, 0, read_len);
        }
        amazonS3Stream.close();
        fos.close();
    }

}
