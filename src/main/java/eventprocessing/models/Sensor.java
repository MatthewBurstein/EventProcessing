package eventprocessing.models;

public class Sensor {

    private final String id;
    private final double x;
    private final double y;
    private double totalValue = 0;
    private int numberOfReadings = 0;


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

    public double getNumberOfReadings() {
        return numberOfReadings;
    }

    public void addReading(Reading reading) {
        numberOfReadings++;
        totalValue += reading.getValue();
    }

    public double getTotalValue() {
        return totalValue;
    }
}
