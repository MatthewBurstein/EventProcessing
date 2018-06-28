package eventprocessing.models;

import com.google.gson.annotations.SerializedName;

public class Response {
    @SerializedName("MessageId")
    public String messageId;
    public Message message;
    @SerializedName("Message")
    public String messageString;

    @Override
    public String toString() {
        if (message != null) {
            return "messageId: " + messageId + "\nMessage: " + String.valueOf(message.getLocationId());
        } else {
            return "messageId: " + messageId + "\nMessage: NULL";
        }
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getMessageString() {
        return messageString;
    }

    public double getValue() {
        return message.getValue();
    }

    public long getTimestamp() {
        return message.getTimestamp();
    }

}
