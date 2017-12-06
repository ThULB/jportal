package fsu.jportal.gson;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import org.mycore.common.MCRJSONTypeAdapter;
import org.mycore.datamodel.ifs.MCRFile;
import org.mycore.datamodel.ifs.MCRFilesystemNode;

public class MCRFilesystemNodeTypeAdapter extends MCRJSONTypeAdapter<MCRFilesystemNode> {

    private static final String DATE_FORMAT = "dd.MM.yyyy HH:mm:ss";

    private static final DateFormat DATE_FORMATTER = new SimpleDateFormat(DATE_FORMAT);

    @Override
    public JsonElement serialize(MCRFilesystemNode fileNode, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject fileNodeJSON = new JsonObject();
        fileNodeJSON.addProperty("name", fileNode.getName());
        fileNodeJSON.addProperty("absPath", fileNode.getAbsolutePath());
        fileNodeJSON.addProperty("size", fileNode.getSize());
        fileNodeJSON.addProperty("lastmodified", DATE_FORMATTER.format(fileNode.getLastModified().getTime()));

        if (fileNode instanceof MCRFile) {
            fileNodeJSON.addProperty("type", "file");
            MCRFile mcrFile = (MCRFile) fileNode;
            fileNodeJSON.addProperty("contentType", mcrFile.getContentTypeID());
            fileNodeJSON.addProperty("md5", mcrFile.getMD5());
        } else {
            fileNodeJSON.addProperty("type", "directory");
        }

        String derivID = fileNode.getOwnerID();
        String absolutePath = fileNode.getAbsolutePath();
        return fileNodeJSON;
    }

    @Override
    public MCRFilesystemNode deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        return null;
    }

}
