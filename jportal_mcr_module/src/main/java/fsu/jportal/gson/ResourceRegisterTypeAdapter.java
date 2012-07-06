package fsu.jportal.gson;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.mycore.common.MCRJSONTypeAdapter;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;

public class ResourceRegisterTypeAdapter extends MCRJSONTypeAdapter<ResourceRegister> {

    @Override
    public JsonElement serialize(ResourceRegister src, Type typeOfSrc, JsonSerializationContext context) {
        Map<String, List<String>> registerMap = src.getMap();
        JsonArray jsonRegisterMap = new JsonArray();
        for (Entry<String, List<String>> registerEntry : registerMap.entrySet()) {
            JsonArray methods = new JsonArray();
            for (String method : registerEntry.getValue()) {
                methods.add(new JsonPrimitive(method));
            }
            JsonObject jsonEntry = new JsonObject();
            jsonEntry.addProperty("rsc", registerEntry.getKey());
            jsonEntry.add("methods", methods);
            jsonRegisterMap.add(jsonEntry);
        }
        return jsonRegisterMap;
    }

    @Override
    public ResourceRegister deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        // TODO Auto-generated method stub
        return null;
    }

//    @Override
//    public Type bindTo() {
//        return ResourceRegister.class;
//    }

}
