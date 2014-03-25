package fsu.jportal.gson;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.mycore.common.MCRJSONTypeAdapter;
import org.mycore.datamodel.ifs.MCRDirectory;
import org.mycore.datamodel.ifs.MCRFilesystemNode;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

public class MCRDirectoryTypeAdapter extends MCRJSONTypeAdapter<MCRDirectory>{
    private static final String dateFormat = "dd.MM.yyyy HH:mm:ss";

    private static final DateFormat dateFormatter = new SimpleDateFormat(dateFormat);

    @Override
    public JsonElement serialize(MCRDirectory dir, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject dirJSON = context.serialize(dir, MCRFilesystemNode.class).getAsJsonObject();
        MCRFilesystemNode[] children = dir.getChildren();
        JsonArray childrenJSON = new JsonArray();
        for (MCRFilesystemNode childNode : children) {
            JsonElement childNodeJSON = context.serialize(childNode, MCRFilesystemNode.class);
            childrenJSON.add(childNodeJSON);
        }
        
        dirJSON.add("children", childrenJSON);
        
        return dirJSON;
    }

    @Override
    public MCRDirectory deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        // TODO Auto-generated method stub
        return null;
    }
    
}