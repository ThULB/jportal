package fsu.jportal.gson;

import java.lang.reflect.Type;
import java.util.Iterator;

import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.classifications2.impl.MCRCategoryImpl;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class CategoriesSaveListJson {
    public static class Deserializer implements JsonDeserializer<CategoriesSaveList>{

        @Override
        public CategoriesSaveList deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonArray categSaveArray = json.getAsJsonArray();
            for (JsonElement toSavedObj : categSaveArray) {
                JsonObject toSavedObjJson = toSavedObj.getAsJsonObject();
                JsonElement jsonElement = toSavedObjJson.get("item");
                MCRCategory categ = context.deserialize(jsonElement, MCRCategoryImpl.class);
                
                MCRCategoryID parentID = null;
                JsonElement parentIdJson = toSavedObjJson.get("parentId");
                if(parentIdJson != null){
                    parentID = context.deserialize(parentIdJson, MCRCategoryID.class);
                }
                
                JsonElement indexJson = toSavedObjJson.get("index");
                if(indexJson != null){
                    
                }
                
            }
            return null;
        }
        
    }
}
