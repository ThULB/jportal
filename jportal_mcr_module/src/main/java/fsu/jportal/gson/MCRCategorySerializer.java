package fsu.jportal.gson;

import java.lang.reflect.Type;

import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.classifications2.MCRLabel;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import fsu.jportal.metadata.Rubric;

public class MCRCategorySerializer implements JsonSerializer<MCRCategory> {

    @Override
    public JsonElement serialize(MCRCategory category, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject rubricJsonObject = new JsonObject();
        MCRCategory parentCateg = category.getParent();
        if (parentCateg != null) {
            rubricJsonObject.add(Rubric.PARENTID, idToJsonObj(parentCateg.getId()));
        }

        MCRCategoryID id = category.getId();
        if (id != null) {
            rubricJsonObject.add(Rubric.ID, idToJsonObj(id));
        }

        JsonArray labelJsonArray = labelsToJsonArray(category);
        rubricJsonObject.add(Rubric.LABEL, labelJsonArray);
        return rubricJsonObject;
    }

    private JsonObject idToJsonObj(MCRCategoryID id) {
        JsonObject idJsonObj = new JsonObject();
        idJsonObj.addProperty(Rubric.ROOTID, id.getRootID());
        String categID = id.getID();
        idJsonObj.addProperty(Rubric.CATEGID, categID);

        return idJsonObj;
    }

    private JsonArray labelsToJsonArray(MCRCategory category) {
        JsonArray labelJsonArray = new JsonArray();
        for (MCRLabel label : category.getLabels()) {
            JsonObject labelJsonObj = labelToJsonObj(label);
            labelJsonArray.add(labelJsonObj);
        }
        return labelJsonArray;
    }

    private JsonObject labelToJsonObj(MCRLabel label) {
        JsonObject labelJsonObj = new JsonObject();
        labelJsonObj.addProperty(Rubric.LANG, label.getLang());
        labelJsonObj.addProperty(Rubric.TEXT, label.getText());
        labelJsonObj.addProperty(Rubric.DESCRIPTION, label.getDescription());

        return labelJsonObj;
    }
}
