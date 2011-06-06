package fsu.jportal.gson;

import java.lang.reflect.Type;
import java.util.Set;

import org.mycore.datamodel.classifications2.MCRLabel;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import fsu.jportal.metadata.Rubric;
import fsu.jportal.wrapper.MCRLabelSetWrapper;

public class MCRLabelSetDoJoSerializer implements JsonSerializer<MCRLabelSetWrapper> {

    @Override
    public JsonElement serialize(MCRLabelSetWrapper labelSetWrapper, Type arg1, JsonSerializationContext arg2) {
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
        labelJsonObj.addProperty(Rubric.LANG, label.getLang());
        labelJsonObj.addProperty(Rubric.TEXT, label.getText());
        
        String description = label.getDescription();
        if(description != null && !"".equals(description)) {
            labelJsonObj.addProperty(Rubric.DESCRIPTION, description);
        }

        return labelJsonObj;
    }

}
