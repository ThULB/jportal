package fsu.jportal.gson;

import static fsu.jportal.gson.CategJsonPropName.HASCHILDREN;
import static fsu.jportal.gson.CategJsonPropName.HASLINK;
import static fsu.jportal.gson.CategJsonPropName.LABELS;
import static fsu.jportal.gson.CategJsonPropName.ID;
import static fsu.jportal.gson.CategJsonPropName.URISTR;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryID;
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
            Map<MCRCategoryID, Boolean> linkMap = categListWrapper.getLinkMap();
            
            if(linkMap == null){
                throw new RuntimeException("For serializing link map must not be null.");
            }
            
            return categListToJsonArray(categListWrapper.getList(), linkMap);
        }
        
        private JsonElement categListToJsonArray(List<MCRCategory> categList, Map<MCRCategoryID, Boolean> linkMap) {
            JsonArray categJsonArray = new JsonArray();
            for (MCRCategory categ : categList) {
                Boolean hasLink = linkMap.get(categ.getId());
                JsonElement element = createCategRefJSONObj(categ, hasLink);
                categJsonArray.add(element);
            }
            
            return categJsonArray;
        }
        
        private JsonElement createCategRefJSONObj(MCRCategory categ, Boolean hasLink) {
            JsonObject categRefJsonObject = new JsonObject();
            categRefJsonObject.add(ID, contextSerializer.serialize(categ.getId()));
            Set<MCRLabel> labels = categ.getLabels();
            categRefJsonObject.add(LABELS, contextSerializer.serialize(new MCRLabelSetWrapper(labels)));
            URI uri = categ.getURI();
            if(uri != null) {
                categRefJsonObject.addProperty(URISTR, uri.toString());
            }
            
            categRefJsonObject.addProperty(HASCHILDREN, categ.hasChildren());
            categRefJsonObject.addProperty(HASLINK, hasLink);
            
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
//                MCRCategoryImpl categ = new MCRCategoryImpl();
//                
//                MCRCategoryID id = null;
//                JsonElement idJsonElement = categRefJsonObject.get(ID);
//                if (idJsonElement != null) {
//                    id = context.deserialize(idJsonElement, MCRCategoryID.class);
//                    categ.setId(id);
//                }
//                
//                MCRLabelSetWrapper labelsWrapper = context.deserialize(categRefJsonObject.get(LABELS), MCRLabelSetWrapper.class);
//                categ.setLabels(labelsWrapper.getSet());
//                
//                JsonElement uriJsonElement = categRefJsonObject.get(URISTR);
//                if(uriJsonElement != null){
//                    String uriStr = uriJsonElement.getAsString();
//                    categ.setURI(URI.create(uriStr));
//                }
                MCRCategory categ = context.deserialize(categRefJsonObject, MCRCategoryImpl.class);
                categList.add(categ);
            }
            
            return new MCRCategoryListWrapper(categList);
        }
    }
}
