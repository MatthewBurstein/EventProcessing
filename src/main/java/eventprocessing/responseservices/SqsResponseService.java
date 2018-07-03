package eventprocessing.responseservices;
import com.google.gson.*;
import eventprocessing.adaptors.MessageAdaptor;
import eventprocessing.customerrors.InvalidSqsResponseException;
import eventprocessing.models.Message;
import eventprocessing.models.SqsResponse;

public class SqsResponseService {

    public SqsResponse parseResponse(String jsonString) {
        jsonString = jsonString.replace("\"{", "{").replace("}\"", "}");
        Gson gson = new GsonBuilder().registerTypeAdapter(Message.class, new MessageAdaptor()).create();
        try {
            SqsResponse sqsResponseObject = gson.fromJson(jsonString, SqsResponse.class);
            return sqsResponseObject;

        } catch (IllegalStateException | JsonSyntaxException e) {
            throw new InvalidSqsResponseException(e);
        }
    }
}
