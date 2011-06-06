package fsu.jportal.gson;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.List;
import java.util.Set;

import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRLabel;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import fsu.jportal.utils.MCRCategUtils;
import fsu.jportal.wrapper.MCRCategoryListWrapper;
import fsu.jportal.wrapper.MCRLabelSetWrapper;

public class MCRCategoryDoJoSerializer implements JsonSerializer<MCRCategory> {

    @Override
    public JsonElement serialize(MCRCategory category, Type arg1, JsonSerializationContext contextSerialization) {
        JsonObject rubricJsonObject = new JsonObject();
        String maskedID = MCRCategUtils.maskCategID(category.getId());
        rubricJsonObject.addProperty("id", maskedID);
        Set<MCRLabel> labels = category.getLabels();
        rubricJsonObject.add("labels", contextSerialization.serialize(new MCRLabelSetWrapper(labels)));
        URI uri = category.getURI();
        if(uri != null) {
            rubricJsonObject.addProperty("URI", uri.toString());
        }
        
        if(category.hasChildren()) {
            List<MCRCategory> children = category.getChildren();
            rubricJsonObject.add("children", contextSerialization.serialize(new MCRCategoryListWrapper(children)));
        }
        
        return rubricJsonObject;
    }
}
