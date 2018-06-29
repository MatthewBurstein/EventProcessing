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
}
