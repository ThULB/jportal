package fsu.jportal.resources;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jdom.Element;
import org.jdom.Namespace;
import org.mycore.common.MCRPersistenceException;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.metadata.MCRObjectMetadata;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import fsu.jportal.metadata.RubricLabel;
import fsu.jportal.metadata.XMLMetaElement;

@Path("classifications")
public class ClassificationResource {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response newClassification(String json) {
        XMLMetaElement<RubricLabel> metaElement = jsonToXMLMetaElement(json);

        MCRObjectID objId = MCRObjectID.getNextFreeId("jportal_jpclassi");

        MCRObject mcrObject = createMCRObject(objId);
        mcrObject.getMetadata().setMetadataElement(metaElement.toMCRMetaElement());

        try {
            MCRMetadataManager.create(mcrObject);
            return Response.created(URI.create(objId.toString())).build();
        } catch (MCRPersistenceException e) {
            e.printStackTrace();
            throw new WebApplicationException(Status.CONFLICT);
        } catch (MCRActiveLinkException e) {
            e.printStackTrace();
            throw new WebApplicationException(Status.CONFLICT);
        }
    }

    private MCRObject createMCRObject(MCRObjectID objId) {
        MCRObject mcrObject = new MCRObject();
        mcrObject.setId(objId);
        mcrObject.setLabel(objId.toString());
        mcrObject.setSchema("datamodel-jpclassi.xsd");
        return mcrObject;
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getClassification(@PathParam("id") String id) {
        try {
            MCRObject mcrObject = MCRMetadataManager.retrieveMCRObject(MCRObjectID.getInstance(id));
            XMLMetaElement<RubricLabel> xmlMetaElement = mcrMetadataToXMLMetaElement(mcrObject.getMetadata());
            
            Gson gson = new Gson();
            return gson.toJson(xmlMetaElement, rubricType());
        } catch (MCRPersistenceException e) {
            throw new WebApplicationException(Status.NOT_FOUND);
        }
    }

    private XMLMetaElement<RubricLabel> mcrMetadataToXMLMetaElement(MCRObjectMetadata mcrMetadata) {
        List<Element> rubricList = mcrMetadata.createXML().getChildren("rubric");
        XMLMetaElement<RubricLabel> xmlMetaElement = new XMLMetaElement<RubricLabel>("rubric");
        for (Element rubric : rubricList) {
            List<Element> labelList = rubric.getChildren("label");
            for (Element label : labelList) {
                RubricLabel rubricLabel = rubricLabelFromXML(label);
                xmlMetaElement.addMetaElemEntry(rubricLabel);
            }
        }
        return xmlMetaElement;
    }

    private RubricLabel rubricLabelFromXML(Element label) {
        String lang = label.getAttributeValue("lang", Namespace.XML_NAMESPACE);
        String text = label.getChildText("text");
        String description = label.getChildText("description");
        RubricLabel rubricLabel = new RubricLabel(lang, text, description);
        return rubricLabel;
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateClassification(@PathParam("id") String id, String json) {
        XMLMetaElement<RubricLabel> metaElement = jsonToXMLMetaElement(json);

        MCRObjectID objId = MCRObjectID.getInstance(id);

        MCRObject mcrObject = MCRMetadataManager.retrieveMCRObject(objId);
        mcrObject.getMetadata().setMetadataElement(metaElement.toMCRMetaElement());

        try {
            MCRMetadataManager.update(mcrObject);
            return Response.ok(json).build();
        } catch (MCRPersistenceException e) {
            e.printStackTrace();
            return Response.status(Status.NOT_FOUND).build();
        } catch (MCRActiveLinkException e) {
            e.printStackTrace();
            return Response.status(Status.CONFLICT).build();
        }

    }

    @DELETE
    @Path("{id}")
    public Response deleteClassification(@PathParam("id") String id) {
        MCRObjectID objId = MCRObjectID.getInstance(id);

        try {
            MCRMetadataManager.deleteMCRObject(objId);
            return Response.status(Status.GONE).build();
        } catch (MCRPersistenceException e) {
            e.printStackTrace();
            return Response.status(Status.NOT_FOUND).build();
        } catch (MCRActiveLinkException e) {
            e.printStackTrace();
            return Response.status(Status.CONFLICT).build();
        }
    }

    private XMLMetaElement<RubricLabel> jsonToXMLMetaElement(String json) {
        Gson gson = new Gson();
        return gson.<XMLMetaElement<RubricLabel>> fromJson(json, rubricType());
    }

    private Type rubricType() {
        Type rubricType = new TypeToken<XMLMetaElement<RubricLabel>>() {
        }.getType();
        return rubricType;
    }
}
