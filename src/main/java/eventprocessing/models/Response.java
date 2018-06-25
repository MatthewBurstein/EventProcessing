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

    public String getMessageString() {
        return messageString;
    }

    public void setMessage(Message message) {
        this.message = message;
    }
}
