package fsu.jportal.resources;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import org.jdom2.Document;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.xml.MCRLayoutService;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.editor.MCREditorServlet;
import org.mycore.services.i18n.MCRTranslation;

import fsu.jportal.resolver.EditorPreProc;
import fsu.jportal.resolver.EditorPreProc.ParamsXML;

@Path("editor")
public class EditorResource {
    @Context
    HttpServletRequest request;

    @Context
    HttpServletResponse response;
    
    @QueryParam("cancelUrl") String cancelURL;

    @GET
    @Path("create/{type}")
    public void create(@PathParam("type") String type) {
        ParamsXML paramsXML = new ParamsXML();
        paramsXML.put("type", type);
        paramsXML.put("mcrid", "jportal_"+type+"_00000000");
        paramsXML.put("editServlet", "CreateObjectServlet");
        if(cancelURL != null){
            paramsXML.put("cancelUrl", cancelURL);
        }
        String title = MCRTranslation.translateWithBaseName(type + ".title", "editor.i18n.labels");
        paramsXML.put("title", title);
        String validationMsg = MCRTranslation.translateWithBaseName("validationMsg", "editor.i18n.labels");
        paramsXML.put("validationMsg", validationMsg);
        
        initEditor(paramsXML);
    }
    
    @GET
    @Path("update/{id}")
    public void update(/*@PathParam("type") String type,*/ @PathParam("id") String id) {
        String type = MCRObjectID.getIDParts(id)[1];
        ParamsXML paramsXML = new ParamsXML();
        paramsXML.put("type", type);
        paramsXML.put("mcrid", id);
        paramsXML.put("editServlet", "UpdateObjectServlet");
        paramsXML.put("sourceUri", "mcrobject:" + id);
        paramsXML.put("cancelUrl", "receive/" + id);
        String title = MCRTranslation.translateWithBaseName(type + ".title", "editor.i18n.labels");
        paramsXML.put("title", title);
        String validationMsg = MCRTranslation.translateWithBaseName("validationMsg", "editor.i18n.labels");
        paramsXML.put("validationMsg", validationMsg);
        
        initEditor(paramsXML);
    }
    
    private void initEditor(ParamsXML paramsXML) {
        String uri = "editorPreProc:start-editor" + paramsXML.getParamStr();
        
        EditorPreProc proc = new EditorPreProc();
        Document editorJDOM = proc.exec("start-editor", paramsXML);

        try {
            MCREditorServlet.replaceEditorElements(request, uri, editorJDOM);
            MCRJDOMContent editorReplaced = new MCRJDOMContent(editorJDOM);

            MCRLayoutService.instance().doLayout(request, response, editorReplaced);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
