package eventprocessing.responseservices;

import eventprocessing.models.Response;
import eventprocessing.models.SensorList;
import eventprocessing.storage.MessageLog;


public class ResponseProcessor {

    public boolean isWorkingSensor(Response response, SensorList sensorList) {
        long count = sensorList.getSensors().stream()
                .filter(sensor -> sensor.getId().equals(response.getMessage().getLocationId()))
                .count();
        return count > 0;
    }

    public boolean isDuplicateMessage(Response response, MessageLog messageLog) {
        return messageLog.getMessageHistory().contains(response.getMessageId());
    }
}
