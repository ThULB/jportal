package fsu.jportal.gson;

import static fsu.jportal.gson.CategJsonPropName.HASCHILDREN;
import static fsu.jportal.gson.CategJsonPropName.ID;
import static fsu.jportal.gson.CategJsonPropName.LABELS;
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
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import fsu.jportal.utils.MCRCategUtils;
import fsu.jportal.wrapper.MCRCategoryListWrapper;
import fsu.jportal.wrapper.MCRLabelSetWrapper;

public class MCRCategoryJson {
    public static class Serializer implements JsonSerializer<MCRCategory> {
        private MCRCategLinkService linkService;

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
                rubricJsonObject.add(HASCHILDREN, contextSerialization.serialize(new MCRCategoryListWrapper(children, linkMap)));
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
    
    public static class Deserializer implements JsonDeserializer<MCRCategory> {
        @Override
        public MCRCategory deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject categJsonObject = json.getAsJsonObject();

            MCRCategoryID id = null;
            JsonElement idJsonElement = categJsonObject.get(ID);
            if (idJsonElement != null) {
                id = context.deserialize(idJsonElement, MCRCategoryID.class);
            }
            
            MCRLabelSetWrapper labelSetWrapper = context.deserialize(categJsonObject.get(LABELS), MCRLabelSetWrapper.class);
            MCRCategory deserializedCateg = MCRCategUtils.newCategory(id, labelSetWrapper.getSet(), null);
            
            JsonElement uriJsonElement = categJsonObject.get(URISTR);
            if(uriJsonElement != null){
                String uriStr = uriJsonElement.getAsString();
                deserializedCateg.setURI(URI.create(uriStr));
            }

            return deserializedCateg;
        }
    }
}
