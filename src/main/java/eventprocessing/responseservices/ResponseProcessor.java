package eventprocessing.responseservices;

import eventprocessing.models.Response;
import eventprocessing.models.SensorList;


public class ResponseProcessor {

    public boolean isWorkingSensor(Response response, SensorList sensorList) {
        long count = sensorList.getSensors().stream()
                .filter(sensor -> sensor.getId().equals(response.getMessage().getLocationId()))
                .count();
        return count > 0;
    }

}
