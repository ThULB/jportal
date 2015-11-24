package fsu.jportal.resources;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.transform.TransformerException;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.xml.MCRLayoutService;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.editor.MCREditorServlet;
import org.mycore.services.i18n.MCRTranslation;
import org.xml.sax.SAXException;

import fsu.jportal.resolver.EditorPreProc;
import fsu.jportal.resolver.EditorPreProc.ParamsXML;
import fsu.jportal.util.JPComponentUtil;
import fsu.jportal.xml.LayoutTools;

@Path("editor")
public class EditorResource {
    @Context
    HttpServletRequest request;

    @Context
    HttpServletResponse response;

    @QueryParam("cancelUrl")
    String cancelURL;

    @GET
    @Path("create/{type}")
    @Produces(MediaType.TEXT_HTML)
    public Response create(@PathParam("type") String type) throws Exception {
        return create(null, type);
    }

    @GET
    @Path("{parentID}/create/{type}")
    @Produces(MediaType.TEXT_HTML)
    public Response create(@PathParam("parentID") String parentID, @PathParam("type") String type) throws Exception {
        ParamsXML paramsXML = new ParamsXML();

        if (parentID != null) {
            paramsXML.put("sourceUri", "xslStyle:asParent:mcrobject:" + parentID);
            Optional<String> journalID = JPComponentUtil.getJournalID(parentID);
            if(journalID.isPresent()) {
                paramsXML.put("journalID", journalID.get());
            }
        }
        paramsXML.put("mcrid", "jportal_" + type + "_00000000");
        paramsXML.put("editServlet", "CreateObjectServlet");
        paramsXML.put("cancelUrl", cancelURL != null ? cancelURL : encodeURL(request.getHeader("referer")));
        MCRContent content = transform("create", type, paramsXML);
        return Response.ok(content.getInputStream()).build();
    }
    
    private String encodeURL(String url){
        try {
            URI relativeURI;
            relativeURI = new URI(null, null, url, null, null);
            return relativeURI.getScheme() + ":" + relativeURI.getRawSchemeSpecificPart();
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return null;
    }

    @GET
    @Path("update/{id}")
    @Produces(MediaType.TEXT_HTML)
    public Response update(/*@PathParam("type") String type,*/@PathParam("id") String id) throws Exception {
        String type = MCRObjectID.getIDParts(id)[1];
        ParamsXML paramsXML = new ParamsXML();
        paramsXML.put("mcrid", id);
        paramsXML.put("editServlet", "UpdateObjectServlet");
        paramsXML.put("sourceUri", "xslStyle:mycoreobject-editor:mcrobject:" + id);
        paramsXML.put("cancelUrl", "receive/" + id);
        String journalID = "jpjournal".equals(type) ? id : JPComponentUtil.getJournalID(id).orElse("");
        paramsXML.put("journalID", journalID);
        MCRContent content = transform("update", type, paramsXML);
        return Response.ok(content.getInputStream()).build();
    }

    private MCRContent transform(String method, String type, ParamsXML paramsXML) throws IOException, SAXException,
            TransformerException {
        paramsXML.put("type", type);
        String title = MCRTranslation.translateWithBaseName(method + "." + type + ".title", "editor.i18n.labels");
        paramsXML.put("title", title);
        String validationMsg = MCRTranslation.translateWithBaseName("validationMsg", "editor.i18n.labels");
        paramsXML.put("validationMsg", validationMsg);

        String uri = "editorPreProc:start-editor" + paramsXML.getParamStr();

        EditorPreProc proc = new EditorPreProc();
        Document editorJDOM = proc.exec("start-editor", paramsXML);
        MCREditorServlet.replaceEditorElements(request, uri, editorJDOM);
        MCRJDOMContent editorReplaced = new MCRJDOMContent(editorJDOM);
        return MCRLayoutService.instance().getTransformedContent(request, response, editorReplaced);
    }
}
