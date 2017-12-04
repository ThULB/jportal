package fsu.jportal.gson;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import fsu.jportal.urn.URNTools;
import org.apache.logging.log4j.LogManager;
import org.mycore.common.MCRJSONTypeAdapter;
import org.mycore.datamodel.ifs.MCRFilesystemNode;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;

public class DerivateTypeAdapter extends MCRJSONTypeAdapter<FileNodeWrapper> {

    private static final String DATE_FORMAT = "dd.MM.yyyy HH:mm:ss";

    private static final DateFormat DATE_FORMATTER = new SimpleDateFormat(DATE_FORMAT);

    @Override
    public JsonElement serialize(FileNodeWrapper deriv, Type typeOfSrc, JsonSerializationContext context) {
        String maindoc = deriv.getMaindoc();
        JsonObject nodeJSON = createJSON(context, maindoc, deriv.getNode());
        if (!deriv.isDir()) {
            return nodeJSON;
        }
        String ownerID = deriv.getNode().getOwnerID();
        boolean hasURNAssigned = URNTools.hasURNAssigned(ownerID);
        MCRFilesystemNodeTypeAdapter.setURNAssigned(ownerID, hasURNAssigned);

        nodeJSON.addProperty("maindocName", maindoc);
        MCRFilesystemNode[] children = deriv.getChildren();
        JsonArray childrenJSON = new JsonArray();
        long serializeChildren = System.currentTimeMillis();
        for (MCRFilesystemNode childNode : children) {
            JsonElement childNodeJSON = createJSON(context, maindoc, childNode);
            childrenJSON.add(childNodeJSON);
        }
        LogManager.getLogger()
                  .info("Serialize children took " + (System.currentTimeMillis() - serializeChildren) + "ms");

        nodeJSON.addProperty("hasURN", hasURNAssigned);
        if (hasURNAssigned) {
            MCRObjectID derivateID = MCRObjectID.getInstance(ownerID);
            MCRDerivate derivate = MCRMetadataManager.retrieveMCRDerivate(derivateID);
            nodeJSON.addProperty("urn", derivate.getDerivate().getURN());
        }
        if (!deriv.getNode().getAbsolutePath().equals("/")) {
            nodeJSON.addProperty("parentName", deriv.getNode().getRootDirectory().getName());
            nodeJSON.addProperty("parentSize", deriv.getNode().getRootDirectory().getSize());
            nodeJSON.addProperty("parentLastMod",
                DATE_FORMATTER.format(deriv.getNode().getRootDirectory().getLastModified().getTime()));
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
        return null;
    }

}
