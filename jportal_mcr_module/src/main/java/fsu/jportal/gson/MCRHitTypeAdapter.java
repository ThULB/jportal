package fsu.jportal.gson;

import java.lang.reflect.Type;
import java.util.List;

import org.mycore.common.MCRJSONTypeAdapter;
import org.mycore.services.fieldquery.MCRFieldValue;
import org.mycore.services.fieldquery.MCRHit;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

public class MCRHitTypeAdapter extends MCRJSONTypeAdapter<MCRHit>{

    @Override
    public JsonElement serialize(MCRHit hit, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonHit = new JsonObject();
        jsonHit.addProperty("id", hit.getID());
        jsonHit.addProperty("host", hit.getHost());
        jsonHit.add("sortData", fieldValuesToJson(hit.getSortData()));
        jsonHit.add("metaData", fieldValuesToJson(hit.getMetaData()));
        
        return jsonHit;
    }

    protected JsonObject fieldValuesToJson(List<MCRFieldValue> fieldValues) {
        JsonObject jsonObj = new JsonObject();
        for (MCRFieldValue fieldValue : fieldValues) {
            jsonObj.addProperty(fieldValue.getField().getName(), fieldValue.getValue());
        }
        
        return jsonObj;
    }

    @Override
    public MCRHit deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        // TODO Auto-generated method stub
        return null;
    }

}
