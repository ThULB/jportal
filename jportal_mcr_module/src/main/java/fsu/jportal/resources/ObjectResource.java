package fsu.jportal.resources;

import static org.mycore.access.MCRAccessManager.PERMISSION_DELETE;
import static org.mycore.access.MCRAccessManager.PERMISSION_READ;

import java.io.StringReader;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import org.mycore.access.MCRAccessException;
import org.mycore.common.MCRJSONManager;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.jersey.MCRJerseyUtil;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@Path("object")
public class ObjectResource {
    static Logger LOGGER = LogManager.getLogger(ObjectResource.class);

    @GET
    @Path("{id}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response get(@PathParam("id") String id, @Context HttpHeaders httpHeaders) {
        MCRObjectID mcrId = MCRJerseyUtil.getID(id);
        MCRJerseyUtil.checkPermission(mcrId, PERMISSION_READ);
        MCRObject object = MCRMetadataManager.retrieveMCRObject(mcrId);
        for (MediaType mediaType : httpHeaders.getAcceptableMediaTypes()) {
            if (mediaType.equals(MediaType.APPLICATION_JSON_TYPE)) {
                return getJSON(object);
            } else if (mediaType.equals(MediaType.APPLICATION_XML_TYPE) || mediaType.equals(MediaType.TEXT_XML_TYPE)) {
                return getXML(object);
            }
        }
        return getXML(object);
    }

    private Response getJSON(MCRObject object) {
        JsonObject json = object.createJSON();
        return Response.status(Status.OK).entity(json.toString()).type(MediaType.APPLICATION_JSON).build();
    }

    private Response getXML(MCRObject object) {
        Document xml = object.createXML();
        String xmlAsString = new XMLOutputter().outputString(xml);
        return Response.status(Status.OK).entity(xmlAsString).type(MediaType.TEXT_XML).build();
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
            List<String> sources = e.getActiveLinks()
                                    .values()
                                    .stream()
                                    .flatMap(Collection::stream)
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
