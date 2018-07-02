package eventprocessing.fileservices;

import eventprocessing.models.Sensor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
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
    private final Logger logger = LogManager.getLogger("JSONParser");

    public JSONParser(String filePath) {
        this.filePath = filePath;
    }

    public SensorList createSensorList() {
        return new SensorList(parseJSON());
    }

    private List<Sensor> parseJSON() {
        FileReader fileReader = null;
        Type collectionType = new TypeToken<ArrayList<Sensor>>(){}
                .getType();
        try {
            fileReader = new FileReader(filePath);

        } catch (FileNotFoundException e) {
            logger.error("Error reading file: " + e.getMessage());
        }
        JsonReader jsonReader = new JsonReader(fileReader);
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        return  gson.fromJson(jsonReader, collectionType);
    }
}
