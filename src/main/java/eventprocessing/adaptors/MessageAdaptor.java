package eventprocessing.adaptors;

import com.google.gson.*;
import eventprocessing.models.Message;

import java.lang.reflect.Type;

public class MessageAdaptor implements JsonDeserializer<Message> {

    @Override
    public Message deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        final JsonObject jsonObject = jsonElement.getAsJsonObject();
        final String locationId = jsonObject.get("locationId").getAsString();
        final String eventId = jsonObject.get("eventId").getAsString();
        final double value = jsonObject.get("value").getAsDouble();
        final long timestamp = jsonObject.get("timestamp").getAsLong();

        final Message message = new Message();
        message.setLocationId(locationId);
        message.setEventId(eventId);
        message.setValue(value);
        message.setTimestamp(timestamp);


        return message;
    }
}
