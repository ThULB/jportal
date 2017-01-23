package fsu.jportal.resources;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.xml.transform.TransformerException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.xml.MCRLayoutService;
import org.mycore.frontend.cli.MCRCommandManager;
import org.mycore.frontend.jersey.MCRJerseyUtil;
import org.mycore.frontend.jersey.filter.access.MCRRestrictedAccess;
import org.mycore.webcli.cli.MCRWebCLICommandManager;
import org.xml.sax.SAXException;

import com.google.gson.JsonObject;

@Path("doublets")
@MCRRestrictedAccess(ResourceAccess.class)
public class DoubletsFinderResource {

    private static final Logger LOGGER = LogManager.getLogger();

    @Context
    HttpServletRequest request;

    @Context
    HttpServletResponse response;

    @GET
    public void start() throws IOException, JDOMException, SAXException, TransformerException {
        InputStream guiXML = getClass().getResourceAsStream("/jportal_doublet_finder_module/gui/xml/webpage.xml");
        SAXBuilder saxBuilder = new SAXBuilder();
        MCRLayoutService.instance().doLayout(request, response, new MCRJDOMContent(saxBuilder.build(guiXML)));
    }

    @DELETE
    @Path("{type}")
    public Response removeDuplicatesFor(@PathParam("type") String type) {
        MCRJerseyUtil.checkPermission("default", "delete-doublets");

        MCRCommandManager commandManager = new MCRWebCLICommandManager();
        JsonObject returnObject = new JsonObject();
        returnObject.addProperty("type", type);
        try {
            invoke(commandManager, "jp clean up " + type);
            returnObject.addProperty("status", "ok");
        } catch (Exception e) {
            LOGGER.error("Unable to clean up doublets", e);
            returnObject.addProperty("status", "error");
            returnObject.addProperty("errorMsg", e.getCause().getMessage());
        }
        return Response.ok().entity(returnObject.toString()).build();
    }

    protected void invoke(MCRCommandManager commandManager, String command) throws Exception {
        List<String> returnList = commandManager.invokeCommand(command);
        for (String subCommand : returnList) {
            invoke(commandManager, subCommand);
        }
    }

}