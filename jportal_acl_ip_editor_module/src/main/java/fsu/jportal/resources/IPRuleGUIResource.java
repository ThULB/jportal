package fsu.jportal.resources;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamSource;

import org.jdom2.transform.JDOMResult;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.xml.MCRLayoutService;

import fsu.jportal.xml.XMLTools;

@Path("ipAcl")
public class IPRuleGUIResource {
    @Context
    HttpServletRequest request;

    @Context
    HttpServletResponse response;

    @GET
    @Path("gui/{filename:.*}")
    public InputStream getResources(@PathParam("filename") String filename) {
        return getClass().getResourceAsStream("/jportal_acl_ip_editor_module/gui/" + filename);
    }

    @GET
    @Path("editor/{objID}")
    public Response start(@PathParam("objID") String objID) {
        InputStream guiXML = getClass().getResourceAsStream("/jportal_acl_ip_editor_module/gui/xml/webpage.xml");
        InputStream guiXSL = getClass().getResourceAsStream("/jportal_acl_ip_editor_module/gui/xsl/webpage.xsl");
        
        JDOMResult result = new JDOMResult();
        Map<String, Object> params = new HashMap<>();
        params.put("journalID", objID);
        params.put("url", "/rsc/IPRule/" + objID);
        
        try {
            new XMLTools().transform(new StreamSource(guiXML), new StreamSource(guiXSL), params, result);
            MCRLayoutService.instance().doLayout(request, response, new MCRJDOMContent(result.getDocument()));
            return Response.ok().build();
        } catch (TransformerFactoryConfigurationError | TransformerException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return Response.serverError().build();
        }
    }
}
