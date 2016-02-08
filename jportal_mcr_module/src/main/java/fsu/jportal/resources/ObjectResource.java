package fsu.jportal.resources;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.mycore.access.MCRAccessException;
import org.mycore.common.MCRJSONManager;
import org.mycore.common.content.MCRContent;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.jersey.MCRJerseyUtil;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.StringReader;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.mycore.access.MCRAccessManager.PERMISSION_DELETE;
import static org.mycore.access.MCRAccessManager.PERMISSION_READ;

@Path("object")
public class ObjectResource {
    static Logger LOGGER = LogManager.getLogger(ObjectResource.class);

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_XML)
    public Response get(@PathParam("id") String id) {
        MCRObjectID mcrId = MCRJerseyUtil.getID(id);
        MCRJerseyUtil.checkPermission(mcrId, PERMISSION_READ);
        try {
            MCRContent content = MCRXMLMetadataManager.instance().retrieveContent(mcrId);
            return Response.ok(content.asString(), MediaType.APPLICATION_XML).build();
        } catch (Exception exc) {
            LOGGER.error("while creating xml for object " + id, exc);
            throw new WebApplicationException(exc, Status.INTERNAL_SERVER_ERROR);
        }
    }

    @DELETE
    @Path("{id}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response delete(@PathParam("id") String id) {
        MCRObjectID mcrId = MCRJerseyUtil.getID(id);
        MCRJerseyUtil.checkPermission(mcrId, PERMISSION_DELETE);

        try {
            if (mcrId.getTypeId().equals("derivate")) {
                MCRDerivate mcrDer = MCRMetadataManager.retrieveMCRDerivate(mcrId);
                MCRMetadataManager.delete(mcrDer);
            } else {
                MCRObject mcrObj = MCRMetadataManager.retrieveMCRObject(mcrId);
                MCRMetadataManager.delete(mcrObj);
            }
        } catch (MCRAccessException mcrActExc) {
            return Response.status(Status.FORBIDDEN).entity(mcrActExc.getMessage()).build();
        } catch (MCRActiveLinkException e) {
            JsonObject o = new JsonObject();
            o.addProperty("type", "error");
            o.addProperty("msg", e.getMessage());
            List<String> sources = e.getActiveLinks().values().stream().flatMap(Collection::stream)
                .collect(Collectors.toList());
            JsonArray activeLinks = new JsonArray();
            sources.forEach(source -> activeLinks.add(source));
            o.add("activeLinks", activeLinks);
            Gson gson = MCRJSONManager.instance().createGson();
            return Response.status(Status.BAD_REQUEST).entity(gson.toJson(o)).build();
        }
        return Response.ok().build();
    }

    @POST
    @Path("import")
    @Consumes(MediaType.TEXT_XML)
    @Produces(MediaType.TEXT_PLAIN)
    public Response importObject(String xml) {
        SAXBuilder builder = new SAXBuilder();
        MCRObject obj;
        try {
            Document doc = builder.build(new StringReader(xml));
            obj = new MCRObject(doc);
        } catch (Exception exc) {
            LOGGER.error("unable to build xml or mcr object: " + xml);
            return Response.status(Status.BAD_REQUEST).entity("invalid xml data").build();
        }
        String objectType = obj.getId().getTypeId();
        MCRJerseyUtil.checkPermission("create-" + objectType);
        obj.setId(MCRObjectID.getNextFreeId("jportal", objectType));
        try {
            MCRMetadataManager.create(obj);
        } catch (MCRAccessException e) {
            LOGGER.error("unable to create mcr object: " + xml);
            e.printStackTrace();
        }
        return Response.ok(obj.getId().toString()).build();
    }

}
