package eventprocessing.models;

import com.google.gson.annotations.SerializedName;
import org.joda.time.DateTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SqsResponse {
    @SerializedName("MessageId")
    private String messageId;
    @SerializedName("Message")
    private Message message;
    @SerializedName("Timestamp")
    private String responseTimestamp;

    private String category = "Unassigned";

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public String toString() {
        if (message != null) {
            return "messageId: " + messageId + "\nMessage: " + String.valueOf(message.getLocationId());
        } else {
            return "messageId: " + messageId + "\nMessage: NULL";
        }
    }

    public Message getMessage() {
        return message;
    }

    public String getMessageId() {
        return messageId;
    }

    public double getValue() {
        return message.getValue();
    }

    public long getMessageTimestamp() {
        return message.getTimestamp();
    }

    public long getResponseTimestamp() {
        long responseTimestampAsLong;
        responseTimestamp = responseTimestamp.substring(0, responseTimestamp.length() - 1) + "-0000";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            Date dt = sdf.parse(responseTimestamp);
            responseTimestampAsLong = dt.getTime();
        } catch(ParseException | NumberFormatException e) {
            responseTimestampAsLong = 0;
        }

        return responseTimestampAsLong;
    }

}
