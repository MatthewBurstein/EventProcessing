package eventprocessing.models;

public class Message {
    private String locationId;
    private String eventId;
    private double value;
    private long timestamp;

    public String getLocationId() {
        return locationId;
    }

    public double getValue() {
        return value;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
