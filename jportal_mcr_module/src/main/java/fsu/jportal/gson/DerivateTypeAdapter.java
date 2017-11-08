package fsu.jportal.gson;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import fsu.jportal.urn.URNTools;
import org.mycore.common.MCRJSONTypeAdapter;
import org.mycore.datamodel.ifs.MCRFilesystemNode;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class DerivateTypeAdapter extends MCRJSONTypeAdapter<FileNodeWrapper> {
    private static final String dateFormat = "dd.MM.yyyy HH:mm:ss";

    private static final DateFormat dateFormatter = new SimpleDateFormat(dateFormat);

    @Override
    public JsonElement serialize(FileNodeWrapper deriv, Type typeOfSrc, JsonSerializationContext context) {
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

        String ownerID = deriv.getNode().getOwnerID();

        if (URNTools.hasURNAssigned(ownerID)) {
            nodeJSON.addProperty("hasURN", true);
            MCRObjectID derivateID = MCRObjectID.getInstance(ownerID);
            MCRDerivate derivate = MCRMetadataManager.retrieveMCRDerivate(derivateID);
            nodeJSON.addProperty("urn", derivate.getDerivate().getURN());
        } else {
            nodeJSON.addProperty("hasURN", false);
        }
        if (!deriv.getNode().getAbsolutePath().equals("/")) {
            nodeJSON.addProperty("parentName", deriv.getNode().getRootDirectory().getName());
            nodeJSON.addProperty("parentSize", deriv.getNode().getRootDirectory().getSize());
            nodeJSON.addProperty("parentLastMod",
                dateFormatter.format(deriv.getNode().getRootDirectory().getLastModified().getTime()));
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
    public FileNodeWrapper deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
        // TODO Auto-generated method stub
        return null;
    }

}
