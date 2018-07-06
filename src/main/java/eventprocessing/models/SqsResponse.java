package eventprocessing.models;

import com.google.gson.annotations.SerializedName;

public class SqsResponse {
    @SerializedName("MessageId")
    private String messageId;
    @SerializedName("Message")
    private Message message;
    @SerializedName("Timestamp")
    private String responseTimestamp;

    @Override
    public String toString() {
        if (message != null) {
            return "messageId: " + messageId + "\nMessage: " + String.valueOf(message.getLocationId());
        } else {
            return "messageId: " + messageId + "\nMessage: NULL";
        }
    }

    public String getMessageId() {
        return messageId;
    }

    public double getValue() {
        return message.getValue();
    }

    String getEventId() { return message.getEventId(); }

    String getLocationId() { return message.getLocationId(); }

    public long getMessageTimestamp() {
        return message.getTimestamp();
    }

}
