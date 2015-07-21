package fsu.jportal.resources;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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

import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.xml.MCRLayoutService;
import org.mycore.frontend.cli.MCRCommandManager;
import org.mycore.frontend.jersey.filter.access.MCRRestrictedAccess;
import org.xml.sax.SAXException;

@Path("doublets")
@MCRRestrictedAccess(ResourceAccess.class)
public class DoubletsFinderResource {

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
        MCRCommandManager mcrKnownCommands = new MCRCommandManager();
        List<String> commandList = new ArrayList<>();
        commandList.add("jp clean up " + type);
        try {
            for (int i = 0; i < commandList.size(); i++){
                List<String> returnList = mcrKnownCommands.invokeCommand(commandList.get(i));
                commandList.addAll(returnList);
            }
            return Response.ok().build();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return Response.serverError().build();
        }
    }

}