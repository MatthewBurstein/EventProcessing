package eventprocessing.responseservices;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import eventprocessing.models.Message;
import eventprocessing.models.SqsResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class SqsResponseService {

    private final Logger logger = LogManager.getLogger("SqsResponseService");

    public SqsResponse parseResponse(String jsonString) {
        Gson gson = new Gson();
        SqsResponse sqsResponseObject = null;
        try {
            sqsResponseObject = gson.fromJson(jsonString, SqsResponse.class);
            String messageString = sqsResponseObject.getMessageString();
            Message messageObject = gson.fromJson(messageString, Message.class);
            sqsResponseObject.setMessage(messageObject);
        } catch (IllegalStateException | JsonSyntaxException e) {
            logger.warn("Invalid JSON string received" + e.getMessage());
            logger.warn("Received json string: " + jsonString);
        }
        return sqsResponseObject;
    }

}
