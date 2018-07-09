package eventprocessing.models;

import com.google.gson.annotations.SerializedName;

public class Sensor {

    @SerializedName("id")
    private final String locationId;
    private final double x;
    private final double y;
    private double totalValue = 0;
    private int numberOfReadings = 0;


    public Sensor(String locationId, double x, double y) {
        this.locationId = locationId;
        this.x = x;
        this.y = y;
    }

    public double getY() {
        return y;
    }

    public double getX() {
        return x;
    }

    public String getLocationId() {
        return locationId;
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
