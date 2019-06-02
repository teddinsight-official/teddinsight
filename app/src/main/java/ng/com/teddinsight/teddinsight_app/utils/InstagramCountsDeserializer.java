package ng.com.teddinsight.teddinsight_app.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class InstagramCountsDeserializer<T> implements JsonDeserializer<T> {
    @Override
    public T deserialize(JsonElement je, Type type, JsonDeserializationContext jdc)
            throws JsonParseException {
        JsonElement countsRoot = je.getAsJsonObject().get("data");
        JsonElement counts = countsRoot.getAsJsonObject().get("counts");
        GsonBuilder builder = new GsonBuilder();
        return builder.create().fromJson(counts, type);
    }
}
