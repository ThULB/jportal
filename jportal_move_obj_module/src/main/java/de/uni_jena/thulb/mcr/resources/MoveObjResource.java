package de.uni_jena.thulb.mcr.resources;

import java.io.InputStream;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.mycore.common.MCRConstants;
import org.mycore.common.MCRPersistenceException;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.content.transformer.MCRContentTransformer;
import org.mycore.common.content.transformer.MCRParameterizedTransformer;
import org.mycore.common.xml.MCRLayoutService;
import org.mycore.common.xsl.MCRParameterCollector;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.frontend.cli.MCRObjectCommands;
import org.mycore.frontend.jersey.filter.access.MCRRestrictedAccess;
import org.mycore.frontend.servlets.MCRServlet;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import de.uni_jena.thulb.mcr.acl.MoveObjectAccess;

@Path("moveObj")
@MCRRestrictedAccess(MoveObjectAccess.class)
public class MoveObjResource {
    static Logger LOGGER = Logger.getLogger(MoveObjResource.class);

    @Context
    HttpServletRequest request;

    @Context
    HttpServletResponse response;
    
    @Context
    UriInfo uri;

    @GET
    @Path("start")
    public byte[] start() throws Exception {
        return transform("/webpages/jportal_move_obj/webpage.xml");
    }

    protected byte[] transform(String xmlFile) throws Exception {
        InputStream is = getClass().getResourceAsStream(xmlFile);
        if (is == null) {
            LOGGER.error("Unable to locate xmlFile of move object resource");
            throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).build());
        }
        SAXBuilder saxBuilder = new SAXBuilder();
        Document webPage = saxBuilder.build(is);
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
    public Response moveTo(String data) {
        JsonParser jsonParser = new JsonParser();
        JsonArray jsonArray = jsonParser.parse(data).getAsJsonArray();
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
            String objID = jsonObject.get("objId").getAsString();
            String newParentID = jsonObject.get("newParentId").getAsString();
            if (!objID.equals(newParentID)) {
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
    
    @GET
    @Path("confJS")
    public Response confJS() {
        JsonObject confObj = new JsonObject();
        String sort = MCRConfiguration.instance().getString("MCR.Module.Move.Obj.sort");
        String parentField = MCRConfiguration.instance().getString("MCR.Module.Move.Obj.parentField");
        List<String> parentTypes = MCRConfiguration.instance().getStrings("MCR.Module.Move.Obj.parentTypes");
        JsonArray parentTypeJson = new JsonArray();
        for (String parentType : parentTypes) {
            parentTypeJson.add(new JsonPrimitive(parentType));
        }
        
        String url = MCRConfiguration.instance().getString("MCR.Module.Move.Obj.Url");
        String baseURL = MCRServlet.getBaseURL();
        confObj.addProperty("sort", sort);
        confObj.addProperty("parentField", parentField);
        confObj.add("parentTypes", parentTypeJson);
        confObj.addProperty("url", uri.getBaseUriBuilder().replacePath(url).build().toString());
        confObj.addProperty("baseUrl", baseURL);
        String jsonConf = "var jpMoveObjConf = " + confObj.toString();
        return Response.ok(jsonConf).build();
    }
}