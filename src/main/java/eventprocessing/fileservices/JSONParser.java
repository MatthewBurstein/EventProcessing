package eventprocessing.fileservices;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import eventprocessing.models.Sensor;
import eventprocessing.models.SensorList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class JSONParser {

    private String filePath;
    private static final Logger logger = LogManager.getLogger("S3Client");

    public JSONParser(String filePath) {
        this.filePath = filePath;
    }

    public SensorList createSensorList() throws FileNotFoundException {
        return new SensorList(parseJSON());
    }

    private List<Sensor> parseJSON() throws FileNotFoundException {
        Type collectionType = new TypeToken<ArrayList<Sensor>>(){}
                .getType();
        try {
            FileReader fileReader = new FileReader(filePath);
            JsonReader jsonReader = new JsonReader(fileReader);
            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.create();
            return  gson.fromJson(jsonReader, collectionType);
        } catch (FileNotFoundException e) {
            logger.error("Error reading file: " + e.getMessage() + " - file is used to get working sensors");
            throw new FileNotFoundException(e.getMessage());
        }
    }
}
