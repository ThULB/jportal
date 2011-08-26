package fsu.jportal.gson;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Set;

import javax.ws.rs.core.UriBuilder;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

public class RegResourceCollectionTypeAdapter extends GsonTypeAdapter<RegResourceCollection> {

    @Override
    public JsonElement serialize(RegResourceCollection src, Type typeOfSrc, JsonSerializationContext context) {
        URI resourceURI = src.getResourceURI();
        JsonObject regResourcesJson = new JsonObject();
        for (String regResourceClass : src.getRegResources()) {
            regResourcesJson.addProperty(regResourceClass, UriBuilder.fromUri(resourceURI).path(regResourceClass).build().toString());
        }
        
        return regResourcesJson;
    }

    @Override
    public RegResourceCollection deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        Set<Entry<String, JsonElement>> entrySet = jsonObject.entrySet();
        ArrayList<String> arrayList = new ArrayList<String>();
        URI uri = null;
        for (Entry<String, JsonElement> entry : entrySet) {
            String key = entry.getKey();
            arrayList.add(key);
            if(uri == null){
                String replaceAll = entry.getValue().getAsString().replaceAll(key, "");
                uri = URI.create(replaceAll);
            }
        }
        return new RegResourceCollection(arrayList, uri);
    }

    @Override
    public Type bindTo() {
        return RegResourceCollection.class;
    }

}
