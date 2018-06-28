package eventprocessing.responseservices;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.MalformedJsonException;
import eventprocessing.models.Message;
import eventprocessing.models.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class ResponseService {

    private final Logger logger = LogManager.getLogger("ResponseService");

    public Response parseResponse(String jsonString) {
        Gson gson = new Gson();
        Response responseObject = null;
        try {
            responseObject = gson.fromJson(jsonString, Response.class);
            String messageString = responseObject.getMessageString();
            Message messageObject = gson.fromJson(messageString, Message.class);
            responseObject.setMessage(messageObject);
        } catch (IllegalStateException | JsonSyntaxException e) {
            logger.warn("Invalid JSON string received" + e.getMessage());
            logger.warn("Received json string: " + jsonString);
        }
        return responseObject;
    }

}
