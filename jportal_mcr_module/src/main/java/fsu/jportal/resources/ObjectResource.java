package fsu.jportal.resources;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import org.mycore.access.MCRAccessException;
import org.mycore.common.MCRJSONManager;
import org.mycore.common.MCRPersistenceException;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.metadata.*;
import org.mycore.frontend.jersey.MCRJerseyUtil;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.StringReader;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import fsu.jportal.backend.MetadataManager;
import static org.mycore.access.MCRAccessManager.*;

@Path("object")
public class ObjectResource {
    static Logger LOGGER = LogManager.getLogger(ObjectResource.class);

    @GET
    @Path("{id}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response get(@PathParam("id") String id, @Context HttpHeaders httpHeaders) {
        MCRObjectID mcrId = MCRJerseyUtil.getID(id);
        MCRJerseyUtil.checkPermission(mcrId, PERMISSION_READ);
        MCRObject object = MetadataManager.retrieveMCRObject(mcrId);
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
                MCRObject mcrObj = MetadataManager.retrieveMCRObject(mcrId);
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
            sources.forEach(activeLinks::add);
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
            LOGGER.error("unable to build xml or mcr object: {}", xml, exc);
            return Response.status(Status.BAD_REQUEST).entity("invalid xml data").build();
        }
        String objectType = obj.getId().getTypeId();
        MCRJerseyUtil.checkPermission("create-" + objectType);
        obj.setId(MCRObjectID.getNextFreeId("jportal", objectType));
        try {
            MCRMetadataManager.create(obj);
        } catch (MCRAccessException e) {
            LOGGER.error("unable to create mcr object: {}", xml, e);
        }
        return Response.ok(obj.getId().toString()).build();
    }

    @GET
    @Path("restore/{id}/{rev}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response restore(@PathParam("id") String id, @PathParam("rev") Long revision) {
        MCRObjectID mcrId = MCRObjectID.getInstance(id);
        MCRJerseyUtil.checkPermission(mcrId, PERMISSION_WRITE);
        try {
            MCRObjectUtils.restore(mcrId, revision);
        } catch (MCRPersistenceException pExc) {
            MCRJerseyUtil.throwException(Status.NOT_FOUND, "There is no mycore object with that revision.");
        } catch (Exception exc) {
            throw new InternalServerErrorException("Unable to revert mycore object to revision " + revision + ".", exc);
        }
        return Response.ok().build();
    }

}
