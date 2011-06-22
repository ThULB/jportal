package fsu.jportal.access;

import org.mycore.common.MCRException;
import org.mycore.datamodel.metadata.MCRObjectID;

public class AccessTools {
    public static boolean isValidID(String id) {
        if (id.contains("_class_")) {
            return false;
        }

        // this is an anti pattern, but I use it any way
        try {
            MCRObjectID.getInstance(id);
            return true;
        } catch (MCRException e) {
            return false;
        }
    }
}
