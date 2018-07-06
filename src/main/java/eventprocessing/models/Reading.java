package eventprocessing.models;

public class Reading {
    private String id;
    private long timestamp;
    private String locationId;
    private double value;

    public Reading(SqsResponse sqsResponse) {
        this.id = sqsResponse.getEventId();
        this.timestamp = sqsResponse.getMessageTimestamp();
        this.locationId = sqsResponse.getLocationId();
        this.value = sqsResponse.getValue();
    }

    public String getLocationId() {
        return locationId;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public String getId() {
        return id;
    }

    public double getValue() {
        return value;
    }
}
