package eventprocessing.models;

public class Message {
    private String locationId;
    private String eventId;
    private double value;
    private String timestamp;

    public String getLocationId() {
        return locationId;
    }

    public double getValue() {
        return value;
    }
}
