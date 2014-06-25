package fsu.jportal.util;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.mycore.access.MCRAccessManager;
import org.mycore.common.MCRException;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;

public class JerseyUtil {

    /**
     * Returns the mycore id. Throws a web application exception if the id is invalid or not found.
     * 
     * @param id id as string
     * @return mycore object id
     */
    public static MCRObjectID getID(String id) {
        MCRObjectID mcrId;
        try {
            mcrId = MCRObjectID.getInstance(id);
        } catch (MCRException mcrExc) {
            throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity("invalid mycore id").build());
        }
        if (!MCRMetadataManager.exists(mcrId)) {
            throw new WebApplicationException(Status.NOT_FOUND);
        }
        return mcrId;
    }

    /**
     * Checks if the mycore object has the given permission. Throws an unauthorized exception otherwise.
     * 
     * @param id mycore object id
     * @param permission permission to check
     */
    public static void checkPermission(MCRObjectID id, String permission) {
        if (!MCRAccessManager.checkPermission(id, permission)) {
            throw new WebApplicationException(Response.status(Status.UNAUTHORIZED).build());
        }
    }

    /**
     * Checks if the mycore object has the given permission. Throws an unauthorized exception otherwise.
     * 
     * @param id mycore object id
     * @param permission permission to check
     */
    public static void checkPermission(String id, String permission) {
        if (!MCRAccessManager.checkPermission(id, permission)) {
            throw new WebApplicationException(Response.status(Status.UNAUTHORIZED).build());
        }
    }

}
