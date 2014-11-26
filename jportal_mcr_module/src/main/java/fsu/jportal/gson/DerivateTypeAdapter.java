package fsu.jportal.gson;

import java.lang.reflect.Type;

import org.mycore.common.MCRJSONTypeAdapter;
import org.mycore.datamodel.ifs.MCRFilesystemNode;
import org.mycore.urn.services.MCRURNManager;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

public class DerivateTypeAdapter extends MCRJSONTypeAdapter<FileNodeWraper> {

    @Override
    public JsonElement serialize(FileNodeWraper deriv, Type typeOfSrc, JsonSerializationContext context) {
        String maindoc = deriv.getMaindoc();
        JsonObject nodeJSON = createJSON(context, maindoc, deriv.getNode());
        if (!deriv.isDir()) {
            return nodeJSON;
        }
        
        nodeJSON.addProperty("maindocName", maindoc);

        MCRFilesystemNode[] children = deriv.getChildren();
        JsonArray childrenJSON = new JsonArray();

        for (MCRFilesystemNode childNode : children) {
            JsonElement childNodeJSON = createJSON(context, maindoc, childNode);

            childrenJSON.add(childNodeJSON);
        }
        MCRURNManager.hasURNAssigned(deriv.getNode().getOwnerID());
        if(MCRURNManager.hasURNAssigned(deriv.getNode().getOwnerID())){
            nodeJSON.addProperty("hasURN", true);
        }
        else{
            nodeJSON.addProperty("hasURN", false);
        }
        nodeJSON.add("children", childrenJSON);

        return nodeJSON;
    }

    private JsonObject createJSON(JsonSerializationContext context, String maindoc, MCRFilesystemNode childNode) {
        JsonElement nodeJSON = context.serialize(childNode, MCRFilesystemNode.class);
        if (childNode.getAbsolutePath().equals(maindoc)) {
            nodeJSON.getAsJsonObject().addProperty("maindoc", true);
        }
        return nodeJSON.getAsJsonObject();
    }

    @Override
    public FileNodeWraper deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        // TODO Auto-generated method stub
        return null;
    }

}