package fsu.jportal.resources;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jdom.Element;
import org.jdom.Namespace;
import org.mycore.common.MCRPersistenceException;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import fsu.jportal.metadata.RubricLabel;
import fsu.jportal.metadata.XMLMetaElement;

@Path("classifications")
public class ClassificationResource {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response newClassification(String json) {
        Gson gson = new Gson();
        Type rubricType = new TypeToken<XMLMetaElement<RubricLabel>>() {
        }.getType();
        XMLMetaElement<RubricLabel> metaElement = gson.fromJson(json, rubricType);

        MCRObjectID objId = MCRObjectID.getNextFreeId("jportal_jpclassi");
        
        MCRObject mcrObject = new MCRObject();
        mcrObject.setId(objId);
        mcrObject.setLabel(objId.toString());
        mcrObject.setSchema("datamodel-jpclassi.xsd");
        mcrObject.getMetadata().setMetadataElement(metaElement.toMCRMetaElement());
        
        try {
            MCRMetadataManager.create(mcrObject);
        } catch (MCRPersistenceException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (MCRActiveLinkException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return Response.created(URI.create(objId.toString())).build();
    }
    
    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getClassification(@PathParam("id") String id){
        MCRObject mcrObject = MCRMetadataManager.retrieveMCRObject(MCRObjectID.getInstance(id));
        
        List<Element> rubricList = mcrObject.getMetadata().createXML().getChildren("rubric");
        XMLMetaElement<RubricLabel> xmlMetaElement = new XMLMetaElement<RubricLabel>("rubric");
        for (Element rubric : rubricList) {
            List<Element> labelList = rubric.getChildren("label");
            for (Element label : labelList) {
                String lang = label.getAttributeValue("lang", Namespace.XML_NAMESPACE);
                String text = label.getChildText("text");
                String description = label.getChildText("description");
                RubricLabel rubricLabel = new RubricLabel(lang, text, description);
                xmlMetaElement.addMetaElemEntry(rubricLabel);
            }
        }
        Gson gson = new Gson();
        Type rubricType = new TypeToken<XMLMetaElement<RubricLabel>>() {
        }.getType();
        
        return gson.toJson(xmlMetaElement, rubricType);
    }
}
