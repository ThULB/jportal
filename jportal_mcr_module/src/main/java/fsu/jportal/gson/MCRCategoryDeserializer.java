package fsu.jportal.gson;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryDAO;
import org.mycore.datamodel.classifications2.MCRCategoryDAOFactory;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.classifications2.MCRLabel;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import fsu.jportal.metadata.Rubric;
import fsu.jportal.utils.MCRCategUtils;

public class MCRCategoryDeserializer implements JsonDeserializer<MCRCategory> {

    @Override
    public MCRCategory deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        MCRCategoryDAO categoryDAO = MCRCategoryDAOFactory.getInstance();
        JsonObject rubricJsonObject = json.getAsJsonObject();
        JsonObject parentIDJsonObject = rubricJsonObject.getAsJsonObject(Rubric.PARENTID);
        JsonObject idJsonObject = rubricJsonObject.getAsJsonObject(Rubric.ID);

        JsonArray labelJsonArray = rubricJsonObject.getAsJsonArray(Rubric.LABEL);

        Set<MCRLabel> labels = new HashSet<MCRLabel>();
        for (JsonElement jsonElement : labelJsonArray) {
            JsonObject labelJsonObject = jsonElement.getAsJsonObject();
            labels.add(jsonLabelToMCRLabel(labelJsonObject));
        }

        if (parentIDJsonObject != null) {
            MCRCategoryID parentID = jsonCategIDToMCRCategID(parentIDJsonObject);
            MCRCategory parentCateg = categoryDAO.getCategory(parentID, 0);
            MCRCategoryID id = jsonCategIDToMCRCategID(idJsonObject);
            if (id == null) {
                id = new MCRCategoryID(parentID.getRootID(), UUID.randomUUID().toString());
            }

            return MCRCategUtils.newCategory(id, labels, parentCateg);
        } else {
            MCRCategoryID id = jsonCategIDToMCRCategID(idJsonObject);
            if (id == null) {
                id = MCRCategoryID.rootID(UUID.randomUUID().toString());
            }
            return MCRCategUtils.newCategory(id, labels, null);
        }
    }

    private MCRCategoryID jsonCategIDToMCRCategID(JsonObject idJsonObject) {
        String rootID = getID(idJsonObject.get(Rubric.ROOTID));
        String categID = getID(idJsonObject.get(Rubric.CATEGID));

        MCRCategoryID id = new MCRCategoryID(rootID, categID);
        return id;
    }

    private String getID(JsonElement idJsonObj) {
        if (idJsonObj != null) {
            return idJsonObj.getAsString();
        } else {
            return UUID.randomUUID().toString();
        }
    }

    private MCRLabel jsonLabelToMCRLabel(JsonObject labelJsonObject) {
        String lang = labelJsonObject.get(Rubric.LANG).getAsString();
        String text = labelJsonObject.get(Rubric.TEXT).getAsString();
        String description = labelJsonObject.get(Rubric.DESCRIPTION).getAsString();
        return new MCRLabel(lang, text, description);
    }

}
