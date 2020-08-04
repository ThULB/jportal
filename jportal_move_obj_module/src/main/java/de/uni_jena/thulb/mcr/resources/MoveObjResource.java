package de.uni_jena.thulb.mcr.resources;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.logging.log4j.LogManager;
import org.mycore.access.MCRAccessException;
import org.mycore.common.MCRPersistenceException;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.frontend.cli.MCRObjectCommands;
import org.mycore.frontend.jersey.filter.access.MCRRestrictedAccess;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import de.uni_jena.thulb.mcr.acl.MoveObjectAccess;
import fsu.jportal.backend.MetaDataTools;
import fsu.jportal.backend.mcr.JPConfig;

@Path("moveObj")
@MCRRestrictedAccess(MoveObjectAccess.class)
public class MoveObjResource {

    @Context
    HttpServletRequest request;

    @Context
    HttpServletResponse response;

    @Context
    UriInfo uri;

    @GET
    @Path("start")
    public byte[] start() throws Exception {
        return MetaDataTools.transformMCRWebPage(request, "/webpages/jportal_move_obj/webpage.xml");
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
                } catch (MCRPersistenceException | MCRAccessException e) {
                    LogManager.getLogger().error(
                        String.format("Unable to replace parent of %s with %s", objID, newParentID), e);
                    return Response.status(Status.UNAUTHORIZED).build();
                }
            }

        }
        return Response.ok().build();
    }

    @GET
    @Path("confJS")
    public Response confJS() {
        JsonObject confObj = new JsonObject();
        String sort = JPConfig.getStringOrThrow("MCR.Module.Move.Obj.sort");
        String parentField = JPConfig.getStringOrThrow("MCR.Module.Move.Obj.parentField");
        List<String> parentTypes = JPConfig.getStrings("MCR.Module.Move.Obj.parentTypes");
        JsonArray parentTypeJson = new JsonArray();
        for (String parentType : parentTypes) {
            parentTypeJson.add(new JsonPrimitive(parentType));
        }

        String baseURL = MCRFrontendUtil.getBaseURL();
        confObj.addProperty("sort", sort);
        confObj.addProperty("parentField", parentField);
        confObj.add("parentTypes", parentTypeJson);
        String url = JPConfig.getString("MCR.Module.Move.Obj.Url", "");
        if (!url.equals("")) {
            confObj.addProperty("url", baseURL + url.substring(1));
        }

        confObj.addProperty("baseUrl", baseURL);
        String jsonConf = "var jpMoveObjConf = " + confObj.toString();
        return Response.ok(jsonConf).build();
    }

}
