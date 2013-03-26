package fsu.jportal.resources;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.xml.MCRLayoutService;

@Path("doublets")
public class DoubletsFinderResource {
    @Context HttpServletRequest request;
    @Context HttpServletResponse response;
    
    @GET
    public void start() throws IOException, JDOMException{
        InputStream guiXML = getClass().getResourceAsStream("/jportal_doublet_finder_module/gui/xml/webpage.xml");
        SAXBuilder saxBuilder = new SAXBuilder();
        MCRLayoutService.instance().doLayout(request, response, new MCRJDOMContent(saxBuilder.build(guiXML)));
    }
}
