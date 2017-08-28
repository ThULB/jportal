package fsu.jportal.gson;

import com.google.gson.*;
import org.mycore.common.MCRJSONTypeAdapter;
import org.mycore.datamodel.ifs.MCRFile;
import org.mycore.datamodel.ifs.MCRFilesystemNode;
import org.mycore.urn.services.MCRURNManager;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class MCRFilesystemNodeTypeAdapter extends MCRJSONTypeAdapter<MCRFilesystemNode>{
    private static final String dateFormat = "dd.MM.yyyy HH:mm:ss";

    private static final DateFormat dateFormatter = new SimpleDateFormat(dateFormat);

    @Override
    public JsonElement serialize(MCRFilesystemNode fileNode, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject fileNodeJSON = new JsonObject();
        fileNodeJSON.addProperty("name", fileNode.getName());
        fileNodeJSON.addProperty("absPath", fileNode.getAbsolutePath());
        fileNodeJSON.addProperty("size", fileNode.getSize());
        fileNodeJSON.addProperty("lastmodified", dateFormatter.format(fileNode.getLastModified().getTime()));
        
        if(fileNode instanceof MCRFile) {
            fileNodeJSON.addProperty("type", "file");
            MCRFile mcrFile = (MCRFile)fileNode;
            fileNodeJSON.addProperty("contentType", mcrFile.getContentTypeID());
            fileNodeJSON.addProperty("md5", mcrFile.getMD5());
        } else{
            fileNodeJSON.addProperty("type", "directory");
        }
        
        String derivID = fileNode.getOwnerID();
        String absolutePath = fileNode.getAbsolutePath();
        String urnForFile = MCRURNManager.getURNForFile(derivID, absolutePath);
        if(urnForFile != null && !"".equals(urnForFile)){
            fileNodeJSON.addProperty("urn", urnForFile);
        }
        
        return fileNodeJSON;
    }

    @Override
    public MCRFilesystemNode deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        // TODO Auto-generated method stub
        return null;
    }
    
}