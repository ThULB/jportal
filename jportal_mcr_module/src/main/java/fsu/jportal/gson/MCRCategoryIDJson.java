package fsu.jportal.gson;

import org.mycore.datamodel.classifications2.MCRCategoryID;

import com.google.gson.JsonObject;

public class MCRCategoryIDJson {
    public static String serialize(MCRCategoryID id) {
        String rootID = id.getRootID();
        String categID = id.getID();

        return rootID + "." + (id == null ? "" : categID);
    }
    
    public static MCRCategoryID deserialize(String idStr) {
        String[] splittedID = idStr.split("\\.");
        
        String rootID = splittedID[0];
        String categID = splittedID.length > 1 ? splittedID[1] : "";
        
        return new MCRCategoryID(rootID, categID);
    }

    public static MCRCategoryID deserialize(JsonObject idJsonObject) {
        return deserialize(idJsonObject.getAsString());
    }
}
