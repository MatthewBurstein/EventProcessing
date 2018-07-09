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

    public boolean isWorkingSensor(Reading reading) {
        long count = sensors
                .stream()
                .filter(sensor -> sensor.getLocationId().equals(reading.getLocationId()))
                .count();
        return count > 0;
    }

    public void storeSensorData(Reading reading) {
        String readingLocationId = reading.getLocationId();
        sensors.forEach(sensor -> {
            if(sensor.getLocationId() == readingLocationId) {
                sensor.addReading(reading);
            }
        });
    }
}
