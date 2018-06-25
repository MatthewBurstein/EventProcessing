package eventprocessing.responseservices;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import eventprocessing.models.Message;
import eventprocessing.models.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class ResponseService {

    private final Logger logger = LogManager.getLogger("ResponseService");

    public void parseResponse(String jsonString) {
        Gson gson = new Gson();
        Response responseObject;
        try {
            responseObject = gson.fromJson(jsonString, Response.class);
            logger.info("Try...");
            logger.info("Input variable: " + jsonString);
            logger.info("responseObject: " + responseObject.toString());

            String currMessage = responseObject.getMessageString();
            Message newMessage = gson.fromJson(currMessage, Message.class);
            responseObject.setMessage(newMessage);

            System.out.println(responseObject.toString());

        } catch (IllegalStateException | JsonSyntaxException e) {
            logger.info("Caught response..." + e.getMessage());
            logger.info("Input variable: " + jsonString);
//            logger.info("responseObject: " + responseObject);
        }


    }

}
