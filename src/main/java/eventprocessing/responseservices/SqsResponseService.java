package eventprocessing.responseservices;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import eventprocessing.customerrors.InvalidSqsResponseException;
import eventprocessing.models.Message;
import eventprocessing.models.SqsResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class SqsResponseService {

    private final Logger logger = LogManager.getLogger("SqsResponseService");

    public SqsResponse parseResponse(String jsonString) {
        Gson gson = new Gson();
        try {
            SqsResponse sqsResponseObject = gson.fromJson(jsonString, SqsResponse.class);
            String messageString = sqsResponseObject.getMessageString();
            Message messageObject = gson.fromJson(messageString, Message.class);
            sqsResponseObject.setMessage(messageObject);
            return sqsResponseObject;
        } catch (IllegalStateException | JsonSyntaxException e) {
            throw new InvalidSqsResponseException(e);
        }
    }
}
