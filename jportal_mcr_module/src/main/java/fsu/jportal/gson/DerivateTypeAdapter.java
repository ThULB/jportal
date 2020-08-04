package fsu.jportal.gson;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.mycore.common.MCRJSONTypeAdapter;
import org.mycore.datamodel.ifs.MCRFilesystemNode;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.niofs.MCRPathUtils;
import org.mycore.pi.MCRPIRegistrationInfo;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

import fsu.jportal.urn.URNTools;

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

        nodeJSON.add("children", childrenJSON);
        MCRObjectID derivateID = MCRObjectID.getInstance(deriv.getNode().getOwnerID());
        String path = deriv.getNode().toPath().getOwnerRelativePath();
        String urn = MCRMetadataManager.retrieveMCRDerivate(derivateID).getDerivate().getURN();
        if (urn != null && !"".equals(urn)) {
            nodeJSON.addProperty("hasURN", true);
            addURNs(nodeJSON, derivateID, path);
        }
        else {
            nodeJSON.addProperty("hasURN", false);
        }
        if (!deriv.getNode().getAbsolutePath().equals("/")) {
            nodeJSON.addProperty("parentName", deriv.getNode().getRootDirectory().getName());
            try {
                nodeJSON.addProperty("parentSize", MCRPathUtils.getSize(deriv.getNode().getRootDirectory().toPath()));
            } catch (IOException e) {
                nodeJSON.addProperty("parentSize", 0);
            }
            nodeJSON.addProperty("parentLastMod",
                DATE_FORMATTER.format(deriv.getNode().getRootDirectory().getLastModified().getTime()));
        }

        return nodeJSON;
    }

    private void addURNs(JsonObject nodeJSON, MCRObjectID derivateID, String path) {
        Map<String, MCRPIRegistrationInfo> urnMap = URNTools.getURNsForDerivateAndPath(derivateID, path
        ).stream()
            .collect(Collectors.toMap(MCRPIRegistrationInfo::getAdditional, Function.identity()));
        addURNToJSONObject(urnMap, nodeJSON, "");
        nodeJSON.get("children").getAsJsonArray().forEach(file -> {
            JsonObject fileAsObject = file.getAsJsonObject();
            addURNToJSONObject(urnMap, fileAsObject, fileAsObject.get("absPath").getAsString());
        });
    }

    private void addURNToJSONObject(Map<String, MCRPIRegistrationInfo> urnMap, JsonObject fileAsObject, String path) {
        Optional.ofNullable(urnMap.getOrDefault(path, null))
            .map(MCRPIRegistrationInfo::getIdentifier)
            .ifPresent(i -> fileAsObject.addProperty("urn",i));
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
