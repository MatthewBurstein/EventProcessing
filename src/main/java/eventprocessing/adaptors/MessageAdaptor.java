package eventprocessing.adaptors;

import com.google.gson.*;
import eventprocessing.models.Message;

import java.lang.reflect.Type;

public class MessageAdaptor implements JsonDeserializer<Message> {

    @Override
    public Message deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        try {
            final JsonObject jsonObject = jsonElement.getAsJsonObject();
            final String locationId = jsonObject.get("locationId").getAsString();
            final String eventId = jsonObject.get("eventId").getAsString();
            final double value = jsonObject.get("value").getAsDouble();
            final long timestamp = jsonObject.get("timestamp").getAsLong();

            final Message message = new Message.Builder()
                    .withLocationId(locationId)
                    .withEventId(eventId)
                    .withValue(value)
                    .withTimestamp(timestamp)
                    .build();

            return message;
        } catch (NumberFormatException e) {
            throw new JsonSyntaxException(e);
        }

    }


}
