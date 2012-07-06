package fsu.jportal.gson;

import java.lang.reflect.Type;

import org.mycore.common.MCRJSONTypeAdapter;
import org.mycore.services.fieldquery.MCRHit;
import org.mycore.services.fieldquery.MCRResults;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

public class MCRResultsTypeAdapter extends MCRJSONTypeAdapter<MCRResultsWrapper>{

    @Override
    public JsonElement serialize(MCRResultsWrapper resultsWrapper, Type typeOfSrc, JsonSerializationContext context) {
        MCRResults results = resultsWrapper.getResults();
        JsonObject jsonResuls = new JsonObject();
        jsonResuls.addProperty("id", results.getID());
        jsonResuls.addProperty("sorted", results.isSorted());
        jsonResuls.addProperty("numHits", results.getNumHits());
        
        JsonArray jsonHits = new JsonArray();
        for (MCRHit hit : results) {
            jsonHits.add(context.serialize(hit));
        }
        
        jsonResuls.add("hits", jsonHits);
        return jsonResuls;
    }

    @Override
    public MCRResultsWrapper deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        // TODO Auto-generated method stub
        return null;
    }

}
