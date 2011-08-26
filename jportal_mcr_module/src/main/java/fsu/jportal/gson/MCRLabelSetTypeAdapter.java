package fsu.jportal.gson;

import static fsu.jportal.gson.CategJsonPropName.DESCRIPTION;
import static fsu.jportal.gson.CategJsonPropName.LANG;
import static fsu.jportal.gson.CategJsonPropName.TEXT;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import org.mycore.datamodel.classifications2.MCRLabel;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import fsu.jportal.wrapper.MCRLabelSetWrapper;

public class MCRLabelSetTypeAdapter extends GsonTypeAdapter<MCRLabelSetWrapper>{
    @Override
    public JsonElement serialize(MCRLabelSetWrapper labelSetWrapper, Type typeOfSrc, JsonSerializationContext context) {
        return labelsToJsonArray(labelSetWrapper.getSet());
    }

    private JsonArray labelsToJsonArray(Set<MCRLabel> labels) {
        JsonArray labelJsonArray = new JsonArray();
        for (MCRLabel label : labels) {
            JsonObject labelJsonObj = labelToJsonObj(label);
            labelJsonArray.add(labelJsonObj);
        }
        return labelJsonArray;
    }
    
    private JsonObject labelToJsonObj(MCRLabel label) {
        JsonObject labelJsonObj = new JsonObject();
        labelJsonObj.addProperty(LANG, label.getLang());
        labelJsonObj.addProperty(TEXT, label.getText());
        
        String description = label.getDescription();
        if(description != null && !"".equals(description)) {
            labelJsonObj.addProperty(DESCRIPTION, description);
        }
        
        return labelJsonObj;
    }
    
    @Override
    public MCRLabelSetWrapper deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Set<MCRLabel> labels = new HashSet<MCRLabel>();
        for (JsonElement jsonElement : json.getAsJsonArray()) {
            JsonObject labelJsonObject = jsonElement.getAsJsonObject();
            labels.add(jsonLabelToMCRLabel(labelJsonObject));
        }
        
        return new MCRLabelSetWrapper(labels);
    }

    private MCRLabel jsonLabelToMCRLabel(JsonObject labelJsonObject) {
        String lang = labelJsonObject.get(LANG).getAsString();
        String text = labelJsonObject.get(TEXT).getAsString();
        JsonElement jsonElement = labelJsonObject.get(DESCRIPTION);
        String description = null;
        if(jsonElement != null){
            description = jsonElement.getAsString();
        }
        return new MCRLabel(lang, text, description);
    }
    
    @Override
    public Type bindTo() {
        return MCRLabelSetWrapper.class;
    }
}
