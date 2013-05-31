package fsu.jportal.util;

import java.util.Collection;

import org.mycore.datamodel.common.MCRLinkTableManager;
import org.mycore.datamodel.metadata.MCRObjectID;

public abstract class ImprintUtil {

    public static final String IMPRINT_TYPE = "imprint";

    /**
     * Returns the imprint of the given object id or throws a 404 not
     * found web application exception.
     * 
     * @param objID mycore object id
     * @return id of imprint
     */
    public static String getImprintID(String objID) {
        MCRObjectID mcrObjID = MCRObjectID.getInstance(objID);
        Collection<String> c = MCRLinkTableManager.instance().getDestinationOf(mcrObjID, IMPRINT_TYPE);
        if (c.isEmpty()) {
            return null;
        }
        return c.iterator().next();
    }

    /**
     * Checks if the given object id is assigned to an imprint.
     * 
     * @param objID mycore object id to check
     * @return true if an imprint is assigned, otherwise false
     */
    public static boolean has(String objID) {
        return getImprintID(objID) != null;
    }

}
