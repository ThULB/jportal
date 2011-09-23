package fsu.jportal.gson;

import static fsu.jportal.gson.CategJsonPropName.CHILDREN;
import static fsu.jportal.gson.CategJsonPropName.ID;
import static fsu.jportal.gson.CategJsonPropName.LABELS;
import static fsu.jportal.gson.CategJsonPropName.PARENTID;
import static fsu.jportal.gson.CategJsonPropName.POSITION;
import static fsu.jportal.gson.CategJsonPropName.URISTR;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mycore.common.MCRConfiguration;
import org.mycore.common.MCRConfigurationException;
import org.mycore.datamodel.classifications2.MCRCategLinkService;
import org.mycore.datamodel.classifications2.MCRCategLinkServiceFactory;
import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.classifications2.MCRLabel;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

import fsu.jportal.wrapper.MCRCategoryListWrapper;
import fsu.jportal.wrapper.MCRLabelSetWrapper;

public class MCRCategoryTypeAdapter extends GsonTypeAdapter<MCRCategory> {
    private MCRCategLinkService linkService;

    @Override
    public MCRCategory deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject categJsonObject = json.getAsJsonObject();
        Category deserializedCateg = new Category();

        JsonElement idJsonElement = categJsonObject.get(ID);
        if (idJsonElement != null) {
            MCRCategoryID id = context.deserialize(idJsonElement, MCRCategoryID.class);
            deserializedCateg.setId(id);
        }

        JsonElement parentIdJsonElement = categJsonObject.get(PARENTID);
        if (parentIdJsonElement != null) {
            MCRCategoryID parentId = context.deserialize(parentIdJsonElement, MCRCategoryID.class);
            deserializedCateg.setParentID(parentId);
        }
        
        JsonElement positionJsonElem = categJsonObject.get(POSITION);
        if(positionJsonElem != null){
            deserializedCateg.setPositionInParent(positionJsonElem.getAsInt());
        }

        JsonElement labelSetWrapperElem = categJsonObject.get(LABELS);
        if (labelSetWrapperElem != null) {
            MCRLabelSetWrapper labelSetWrapper = context.deserialize(labelSetWrapperElem, MCRLabelSetWrapper.class);
            deserializedCateg.setLabels(labelSetWrapper.getSet());
        }

        JsonElement uriJsonElement = categJsonObject.get(URISTR);
        if (uriJsonElement != null) {
            String uriStr = uriJsonElement.getAsString();
            deserializedCateg.setURI(URI.create(uriStr));
        }

        return deserializedCateg;
    }

    @Override
    public JsonElement serialize(MCRCategory category, Type arg1, JsonSerializationContext contextSerialization) {
        JsonObject rubricJsonObject = new JsonObject();
        MCRCategoryID id = category.getId();
        if (id != null) {
            rubricJsonObject.add(ID, contextSerialization.serialize(id));
        }

        Set<MCRLabel> labels = category.getLabels();
        rubricJsonObject.add(LABELS, contextSerialization.serialize(new MCRLabelSetWrapper(labels)));
        URI uri = category.getURI();
        if (uri != null) {
            rubricJsonObject.addProperty(URISTR, uri.toString());
        }

        if (category.hasChildren()) {
            List<MCRCategory> children = category.getChildren();
            Map<MCRCategoryID, Boolean> linkMap = getLinkService().hasLinks(category);
            rubricJsonObject.add(CHILDREN, contextSerialization.serialize(new MCRCategoryListWrapper(children, linkMap)));
        }

        return rubricJsonObject;
    }
    
    private MCRCategLinkService getLinkService() {
        if (linkService == null) {
            try {
                linkService = (MCRCategLinkService) MCRConfiguration.instance().getInstanceOf("Category.Link.Service");
            } catch (MCRConfigurationException e) {
                linkService = MCRCategLinkServiceFactory.getInstance();
            }
        }

        return linkService;
    }
}
