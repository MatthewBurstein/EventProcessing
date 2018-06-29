package eventprocessing.responseservices;

import eventprocessing.models.Response;
import eventprocessing.models.Bucket;
import eventprocessing.models.SensorList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ResponseProcessor {
    private final static Logger logger = LogManager.getLogger("ResponseProcessor");

    public boolean isWorkingSensor(Response response, SensorList sensorList) {
        long count = sensorList.getSensors()
                .stream()
                .filter(sensor -> sensor.getId().equals(response.getMessage().getLocationId()))
                .count();
        return count > 0;
    }

    public boolean isValidMessage(Response response, SensorList sensorList, Bucket bucket) {
        return isWorkingSensor(response, sensorList) && !isDuplicateMessage(response, bucket);
    }

    public boolean isDuplicateMessage(Response response, Bucket bucket) {
        boolean result = bucket.getMessageIds().contains(response.getMessageId());
        if (result) { logger.info("Removed duplicate message with ID: " + response.getMessageId()); }
        return result;
    }
}
