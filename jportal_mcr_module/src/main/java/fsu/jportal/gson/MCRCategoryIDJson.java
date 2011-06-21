package fsu.jportal.gson;

import java.lang.reflect.Type;

import org.mycore.datamodel.classifications2.MCRCategoryID;
import static fsu.jportal.gson.CategJsonPropName.*;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class MCRCategoryIDJson {
    public static class Serializer implements JsonSerializer<MCRCategoryID>{

        @Override
        public JsonElement serialize(MCRCategoryID id, Type typeOfSrc, JsonSerializationContext context) {
            String rootID = id.getRootID();
            String categID = id.getID();
            
            JsonObject idJsonObj = new JsonObject();
            idJsonObj.addProperty(ROOTID, rootID);
            if(categID != null && !"".equals(categID)) {
                idJsonObj.addProperty(CATEGID, categID);
            }
            
            return idJsonObj;
        }
        
    }
    
    public static class Deserializer implements JsonDeserializer<MCRCategoryID>{

        @Override
        public MCRCategoryID deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject idJsonObj = json.getAsJsonObject();
            JsonElement rootIDObj = idJsonObj.get(ROOTID);
            JsonElement categIDObj = idJsonObj.get(CATEGID);
            
            String rootID = rootIDObj.getAsString();
            String categID = categIDObj == null? "" : categIDObj.getAsString();
            return new MCRCategoryID(rootID, categID);
        }
        
    }
    
    public static String serialize(MCRCategoryID id) {
        String rootID = id.getRootID();
        String categID = id.getID();

        return rootID + "." + (id == null ? "" : categID);
    }
    
    public static MCRCategoryID deserialize(String idStr) {
        String[] splittedID = idStr.split("\\.");
        
        String rootID = splittedID[0];
        String categID = splittedID.length > 1 ? splittedID[1] : "";
        
        return new MCRCategoryID(rootID, categID);
    }

    public static MCRCategoryID deserialize(JsonObject idJsonObject) {
        return deserialize(idJsonObject.getAsString());
    }
}
