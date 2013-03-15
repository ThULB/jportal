package fsu.jportal.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.mycore.access.MCRAccessManager;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.transformer.MCRContentTransformer;
import org.mycore.common.xml.MCRLayoutTransformerFactory;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;


import fsu.jportal.util.JerseyUtil;

@Path("render")
public class RenderResource {

    static Logger LOGGER = Logger.getLogger(RenderResource.class);

    @GET
    @Path("object/{id}")
    @Produces(MediaType.TEXT_HTML)
    public Response object(@PathParam("id") String id) {
        MCRObjectID mcrId = JerseyUtil.getID(id);
        JerseyUtil.checkPermission(mcrId, MCRAccessManager.PERMISSION_READ);
        try {
            MCRContent xmlContent = MCRXMLMetadataManager.instance().retrieveContent(mcrId);
            MCRContentTransformer transformer = MCRLayoutTransformerFactory.getTransformer("jp-layout-object");
            MCRContent htmlContent = transformer.transform(xmlContent);
            return Response.ok(htmlContent.asString(), MediaType.TEXT_HTML).build();
        } catch(Exception exc) {
            LOGGER.error("while creating content", exc);
            throw new WebApplicationException(exc, Status.INTERNAL_SERVER_ERROR);
        }
    }

}
