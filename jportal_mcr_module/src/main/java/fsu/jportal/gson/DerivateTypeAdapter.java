package fsu.jportal.gson;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.mycore.common.MCRJSONTypeAdapter;
import org.mycore.datamodel.ifs.MCRFilesystemNode;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

public class DerivateTypeAdapter extends MCRJSONTypeAdapter<FileNodeWraper>{
    private static final String dateFormat = "dd.MM.yyyy HH:mm:ss";

    private static final DateFormat dateFormatter = new SimpleDateFormat(dateFormat);

    @Override
    public JsonElement serialize(FileNodeWraper deriv, Type typeOfSrc, JsonSerializationContext context) {
        Class<MCRFilesystemNode> type = MCRFilesystemNode.class;
        JsonObject dirJSON = context.serialize(deriv.getNode(), type).getAsJsonObject();
        MCRFilesystemNode[] children = deriv.getChildren();
        JsonArray childrenJSON = new JsonArray();
        String maindoc = deriv.getMaindoc();
        
        for (MCRFilesystemNode childNode : children) {
            JsonElement childNodeJSON = context.serialize(childNode, type);
            if(childNode.getName().equals(maindoc)){
                childNodeJSON.getAsJsonObject().addProperty("maindoc", true);
            }
            
            childrenJSON.add(childNodeJSON);
        }
        
        dirJSON.add("children", childrenJSON);
        
        return dirJSON;
    }

    @Override
    public FileNodeWraper deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        // TODO Auto-generated method stub
        return null;
    }
    
}