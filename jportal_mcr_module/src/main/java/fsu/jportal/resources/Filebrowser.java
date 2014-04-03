package fsu.jportal.resources;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.access.MCRAccessManager;
import org.mycore.common.MCRJSONManager;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.ifs.MCRDirectory;
import org.mycore.datamodel.ifs.MCRFilesystemNode;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.cli.JPortalCommands;
import org.mycore.frontend.jersey.filter.access.MCRResourceAccessChecker;
import org.xml.sax.SAXException;

import com.sun.jersey.spi.container.ContainerRequest;

import fsu.jportal.gson.DerivateTypeAdapter;

@Path("filebrowser")
public class Filebrowser {
    private MCRJSONManager gsonManager;

    public Filebrowser() {
        gsonManager = MCRJSONManager.instance();
        gsonManager.registerAdapter(new DerivateTypeAdapter());
        gsonManager.registerAdapter(new DerivateTypeAdapter());
    }

    @GET
    @Path("{id}{path:(/.*)*}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response browsePath(@PathParam("id") String id, @PathParam("path") String path) {
        MCRDirectory rootDirectory = MCRDirectory.getRootDirectory(id);
        if (rootDirectory == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        
        String maindoc = getMaindoc(id);

        if (path != null && !"".equals(path.trim())) {
            MCRFilesystemNode node = rootDirectory.getChildByPath(path);
            if (node == null) {
                return Response.status(Status.NOT_FOUND).build();
            }

            return createJSON(node);
        }
        
        return createJSON(rootDirectory);
    }

    private String getMaindoc(String id) {
        Document derivXML;
        try {
            derivXML = MCRXMLMetadataManager.instance().retrieveXML(MCRObjectID.getInstance(id));
            XPathFactory xPathFactory = XPathFactory.instance();
            XPathExpression<Attribute> attr = xPathFactory.compile("/mycorederivate/derivate/internals/internal/@maindoc", Filters.attribute());
            return attr.evaluateFirst(derivXML).getValue();
        } catch (IOException | JDOMException | SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return null;
    }

    private Response createJSON(MCRDirectory node) {
        
        String json = gsonManager.createGson().toJson(node);
        return Response.ok(json).build();
    }
    
    private Response createJSON(MCRFilesystemNode node) {

        if (node instanceof MCRDirectory) {
            return createJSON((MCRDirectory)node);
        }

        String json = gsonManager.createGson().toJson(node);
        return Response.ok(json).build();
    }

    @GET
    @Path("gui/{id}{path:(/.*)*}")
    @Produces(MediaType.TEXT_HTML)
    public Response gui(@PathParam("id") String id) {
        //        MCRFilesystemNode rootNode = MCRFilesystemNode.getRootNode(id);
        //        if(rootNode == null){
        //            return Response.status(Status.NOT_FOUND).build();
        //        }
        InputStream mainGui = getClass().getResourceAsStream("/gui/main.html");
        return Response.ok(mainGui).build();
    }

    @DELETE
    @Path("{id}{path:(/.*)*}")
    //    @MCRRestrictedAccess(ResourceAccess.class)
    public Response deleteFile(@PathParam("id") String id, @PathParam("path") String path) {
        if (MCRAccessManager.checkPermission(id, "delete")) {

        }
        MCRFilesystemNode rootNode = MCRFilesystemNode.getRootNode(id);
        if (rootNode == null) {
            return Response.status(Status.NOT_FOUND).build();
        }

        if (rootNode instanceof MCRDirectory && path != null && !"".equals(path.trim())) {
            MCRFilesystemNode node = ((MCRDirectory) rootNode).getChildByPath(path);
            if (node == null) {
                return Response.status(Status.NOT_FOUND).build();
            }

            node.delete();
            return Response.ok().build();
        }
        //        } else {
        //            response.sendError(HttpServletResponse.SC_FORBIDDEN,
        //                MessageFormat.format("User has not the \"" + PERMISSION_DELETE + "\" permission on object {0}.", derivateId));
        //        }

        return Response.serverError().build();
    }

    
    @POST
    @Path("rename")
    public Response rename(@QueryParam("newFile") String newFile, @QueryParam("oldFile") String oldFile){
        JPortalCommands.renameFileInIFS(oldFile, newFile);
        return Response.ok().build();
    }
    
    public static class ResourceAccess implements MCRResourceAccessChecker {
        private static Logger LOGGER = Logger.getLogger(ResourceAccess.class);

        @Override
        public boolean isPermitted(ContainerRequest request) {
            LOGGER.info("Method: " + request.getMethod());
            LOGGER.info("Path: " + request.getPath());
            LOGGER.info("AbsPath: " + request.getAbsolutePath());
            LOGGER.info("BaseUri: " + request.getBaseUri());
            return false;
        }

    }
}