package eventprocessing.models;

public class Message {
    private String locationId;
    private String eventId;
    private double value;
    private long timestamp;

    String getLocationId() {
        return locationId;
    }

    String getEventId() {
        return eventId;
    }

    public double getValue() {
        return value;
    }

    long getTimestamp() {
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

    public static class Builder {
        private String locationId;
        private String eventId;
        private double value;
        private long timestamp;

        public Builder withLocationId(String locationId) {
            this.locationId = locationId;

            return this;
        }

        public Builder withEventId(String eventId) {
            this.eventId = eventId;

            return this;
        }

        public Builder withValue(double value) {
            this.value = value;

            return this;
        }

        public Builder withTimestamp(long timestamp) {
            this.timestamp = timestamp;

            return this;
        }

        public Message build() {
            Message message = new Message();
            message.locationId = this.locationId;
            message.eventId = this.eventId;
            message.value = this.value;
            message.timestamp = this.timestamp;

            return message;
        }
    }
}
