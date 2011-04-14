package fsu.jportal.gson;

import java.lang.reflect.Type;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import fsu.jportal.metadata.Rubric;

public class RubricDeserializer implements JsonDeserializer<Rubric>{

    @Override
    public Rubric deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Rubric rubric = new Rubric();
        JsonObject rubricJsonObject = json.getAsJsonObject();
        JsonElement parentID = rubricJsonObject.get(Rubric.PARENTID);
        
        if (parentID != null) {
            rubric.setParentID(parentID.getAsString());
        }
        
        JsonArray labelJsonArray = rubricJsonObject.getAsJsonArray(Rubric.LABEL);
        
        for (JsonElement jsonElement : labelJsonArray) {
            JsonObject labelJsonObject = jsonElement.getAsJsonObject();
            
            addJsonLabelToRubric(labelJsonObject, rubric);
        }
        
        return rubric;
    }

    private void addJsonLabelToRubric(JsonObject labelJsonObject, Rubric rubric) {
        String lang = labelJsonObject.get(Rubric.LANG).getAsString();
        String text = labelJsonObject.get(Rubric.TEXT).getAsString();
        String description = labelJsonObject.get(Rubric.DESCRIPTION).getAsString();
        rubric.addLabel(lang, text, description);
    }
}