package eventprocessing.responseservices;

import eventprocessing.models.Response;
import eventprocessing.models.ResponseList;
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

    public boolean isValidMessage(Response response, SensorList sensorList, ResponseList responseList) {
        return isWorkingSensor(response, sensorList) && !isDuplicateMessage(response, responseList);
    }

    public boolean isDuplicateMessage(Response response, ResponseList responseList) {
        boolean result = responseList.getMessageIds().contains(response.getMessageId());
        if (result == true) { logger.info("Removed duplicate message with ID: " + response.getMessageId()); }
        return result;
    }
}
