package org.mycore.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.mycore.common.MCRSession;
import org.mycore.common.MCRSessionMgr;
import org.mycore.datamodel.metadata.MCRObjectID;

@Path("nextFreeID")
public class IdentifierResource {
    @GET
    @Path("{base_id}")
    public String nextFreeID(@PathParam("base_id") String base_id){
        MCRSession currentSession = MCRSessionMgr.getCurrentSession();
        currentSession.beginTransaction();
        MCRObjectID mcrObjectID = MCRObjectID.getNextFreeId(base_id);
        currentSession.commitTransaction();
        currentSession.close();
        return mcrObjectID.toString();
    }
}
