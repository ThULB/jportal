package fsu.jportal.gson;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRLabel;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import fsu.jportal.utils.MCRCategUtils;
import fsu.jportal.wrapper.MCRCategoryListWrapper;
import fsu.jportal.wrapper.MCRLabelSetWrapper;

public class MCRCategoryListDoJoSerializer implements JsonSerializer<MCRCategoryListWrapper> {

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
        categRefJsonObject.addProperty("$ref", MCRCategUtils.maskCategID(categ.getId()));
        Set<MCRLabel> labels = categ.getLabels();
        categRefJsonObject.add("labels", contextSerializer.serialize(new MCRLabelSetWrapper(labels)));
        categRefJsonObject.addProperty("children", categ.hasChildren());

        return categRefJsonObject;
    }
}
