package fsu.jportal.gson;

import static fsu.jportal.gson.CategJsonPropName.HASCHILDREN;
import static fsu.jportal.gson.CategJsonPropName.LABELS;
import static fsu.jportal.gson.CategJsonPropName.ID;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRLabel;
import org.mycore.datamodel.classifications2.impl.MCRCategoryImpl;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import fsu.jportal.wrapper.MCRCategoryListWrapper;
import fsu.jportal.wrapper.MCRLabelSetWrapper;

public class MCRCategoryListJson {
    public static class Serializer implements JsonSerializer<MCRCategoryListWrapper>{
        private JsonSerializationContext contextSerializer;
        
        @Override
        public JsonElement serialize(MCRCategoryListWrapper categListWrapper, Type arg1, JsonSerializationContext arg2) {
            this.contextSerializer = arg2;
            return categListToJsonArray(categListWrapper.getList());
        }
        
        private JsonElement categListToJsonArray(List<MCRCategory> categList) {
            JsonArray categJsonArray = new JsonArray();
            for (MCRCategory categ : categList) {
                JsonElement element = createCategRefJSONObj(categ);
                categJsonArray.add(element);
            }
            
            return categJsonArray;
        }
        
        private JsonElement createCategRefJSONObj(MCRCategory categ) {
            JsonObject categRefJsonObject = new JsonObject();
            categRefJsonObject.addProperty(ID, MCRCategoryIDJson.serialize(categ.getId()));
            Set<MCRLabel> labels = categ.getLabels();
            categRefJsonObject.add(LABELS, contextSerializer.serialize(new MCRLabelSetWrapper(labels)));
            categRefJsonObject.addProperty(HASCHILDREN, categ.hasChildren());
            
            return categRefJsonObject;
        }
    }

    
    public static class Deserializer implements JsonDeserializer<MCRCategoryListWrapper>{
        @Override
        public MCRCategoryListWrapper deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            List<MCRCategory> categList = new ArrayList<MCRCategory>();
            
            for (JsonElement categRef : json.getAsJsonArray()) {
                JsonObject categRefJsonObject = categRef.getAsJsonObject();
                MCRCategoryImpl categ = new MCRCategoryImpl();
                categ.setId(MCRCategoryIDJson.deserialize(categRefJsonObject.get(ID).getAsString()));
                MCRLabelSetWrapper labelsWrapper = context.deserialize(categRefJsonObject.get(LABELS), MCRLabelSetWrapper.class);
                categ.setLabels(labelsWrapper.getSet());
                categList.add(categ);
            }
            
            return new MCRCategoryListWrapper(categList);
        }
    }
}
