package eventprocessing.models;

import java.util.List;

public class SensorList {

    private List<Sensor> sensors;

    public SensorList(List<Sensor> sensors) {
        this.sensors = sensors;
    }

    public List<Sensor> getSensors() {
        return sensors;
    }

    public boolean isWorkingSensor(SqsResponse sqsResponse) {
        long count = sensors
                .stream()
                .filter(sensor -> sensor.getId().equals(sqsResponse.getMessage().getLocationId()))
                .count();
        return count > 0;
    }
}
