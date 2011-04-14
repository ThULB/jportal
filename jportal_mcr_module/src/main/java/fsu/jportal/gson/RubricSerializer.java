package fsu.jportal.gson;

import java.lang.reflect.Type;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import fsu.jportal.metadata.Rubric;
import fsu.jportal.metadata.Rubric.Label;

public class RubricSerializer implements JsonSerializer<Rubric> {

    @Override
    public JsonElement serialize(Rubric rubric, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject rubricJsonObject = new JsonObject();
        rubricJsonObject.addProperty(Rubric.PARENTID, rubric.getParentID());
        JsonArray labelJsonArray = mcrMetaElementToJsonArray(rubric);
        rubricJsonObject.add(Rubric.LABEL, labelJsonArray);
        return rubricJsonObject;
    }

    private JsonArray mcrMetaElementToJsonArray(Rubric rubric) {
        JsonArray labelJsonArray = new JsonArray();
        for (Label label : rubric) {
            JsonObject labelJsonObj = labelXMLToJsonObj(label);

            labelJsonArray.add(labelJsonObj);
        }
        return labelJsonArray;
    }

    private JsonObject labelXMLToJsonObj(Label label) {
        JsonObject labelJsonObj = new JsonObject();
        labelJsonObj.addProperty(Rubric.LANG, label.getLang());
        labelJsonObj.addProperty(Rubric.TEXT, label.getText());
        labelJsonObj.addProperty(Rubric.DESCRIPTION, label.getDescription());

        return labelJsonObj;
    }

}