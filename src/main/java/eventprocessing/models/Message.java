package eventprocessing.models;

import java.math.BigInteger;

public class Message {
    private String locationId;
    private String eventId;
    private double value;
    private BigInteger timestamp;

    public String getLocationId() {
        return locationId;
    }

    public double getValue() {
        return value;
    }

    public BigInteger getTimestamp() {
        return timestamp;
    }
}
