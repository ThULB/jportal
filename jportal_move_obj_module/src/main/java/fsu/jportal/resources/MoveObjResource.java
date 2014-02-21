package fsu.jportal.resources;

import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.mycore.access.MCRAccessManager;
import org.mycore.common.MCRPersistenceException;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.content.transformer.MCRContentTransformer;
import org.mycore.common.content.transformer.MCRParameterizedTransformer;
import org.mycore.common.xml.MCRLayoutService;
import org.mycore.common.xsl.MCRParameterCollector;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.frontend.cli.MCRObjectCommands;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Path("moveObj")
public class MoveObjResource{
    static Logger LOGGER = Logger.getLogger(MoveObjResource.class);
    
    @Context
    HttpServletRequest request;

    @Context
    HttpServletResponse response;
    
    @GET
    @Path("start")
    public byte[] start() throws Exception {
        if (!MCRAccessManager.getAccessImpl().checkPermission("create-jpjournal")) {
            throw new WebApplicationException(Response.status(Status.UNAUTHORIZED).build());
        }
        InputStream guiXML = getClass().getResourceAsStream("/jportal_move_obj_module/gui/xml/webpage.xml");
        SAXBuilder saxBuilder = new SAXBuilder();
        Document webPage = saxBuilder.build(guiXML);
        MCRJDOMContent source = new MCRJDOMContent(webPage);
        
        MCRParameterCollector parameter = new MCRParameterCollector(request);
        MCRContentTransformer transformer = MCRLayoutService.getContentTransformer("MyCoReWebPage", parameter);
        MCRContent result;
        if (transformer instanceof MCRParameterizedTransformer) {
            result = ((MCRParameterizedTransformer) transformer).transform(source, parameter);
        } else {
            result = transformer.transform(source);
        }
        return result.asByteArray();
    }
    
    @PUT
    @Path("move")
    public Response moveTo(String data){
        JsonParser jsonParser = new JsonParser();
        JsonArray jsonArray = jsonParser.parse(data).getAsJsonArray();
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
            String objID = jsonObject.get("objId").getAsString();
            String newParentID = jsonObject.get("newParentId").getAsString();
            if (!objID.equals(newParentID)){
                try {
                    MCRObjectCommands.replaceParent(objID, newParentID);
                } catch (MCRPersistenceException e) {
                    e.printStackTrace();
                    return Response.status(Status.UNAUTHORIZED).build();
                } catch (MCRActiveLinkException e) {
                    e.printStackTrace();
                } 
            }

        }
        return Response.ok().build();
    }
}