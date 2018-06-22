package eventprocessing.models;

public class Sensor {

    private final String id;
    private final double x;
    private final double y;

    public Sensor(String id, double x, double y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }


    public double getY() {
        return y;
    }

    public double getX() {
        return x;
    }

    public String getId() {
        return id;
    }
}
