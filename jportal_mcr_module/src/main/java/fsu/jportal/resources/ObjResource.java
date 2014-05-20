package fsu.jportal.resources;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.mycore.common.MCRPersistenceException;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.ifs.MCRFile;
import org.mycore.frontend.cli.MCRObjectCommands;
import org.mycore.iview2.services.MCRIView2Tools;
import org.mycore.iview2.services.MCRImageTiler;
import org.mycore.iview2.services.MCRTileJob;
import org.mycore.iview2.services.MCRTilingQueue;

import fsu.jportal.gson.ParentsListJSON;
import fsu.jportal.util.ContentTools;

@Path("obj/{id}")
public class ObjResource {

    private static final Logger LOGGER = Logger.getLogger(ObjResource.class);

    @PathParam("id")
    String objID;

    @GET
    @Path("parents")
    @Produces(MediaType.APPLICATION_JSON)
    public String parentsListJSON() {
        ContentTools contentTools = new ContentTools();
        return contentTools.getParents(objID, new ParentsListJSON());
    }

    @PUT
    @Path("moveTo/{newParentID}")
    public Response moveTo(@PathParam("newParentID") String newParentID) {
        try {
            MCRObjectCommands.replaceParent(objID, newParentID);
        } catch (MCRPersistenceException e) {
            e.printStackTrace();
            return Response.status(Status.UNAUTHORIZED).build();
        } catch (MCRActiveLinkException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return Response.ok().build();
    }

    public void tileImage(MCRFile file) {
        MCRTilingQueue TILE_QUEUE = MCRTilingQueue.getInstance();

        if (MCRIView2Tools.isFileSupported(file)) {
            MCRTileJob job = new MCRTileJob();
            job.setDerivate(file.getOwnerID());
            job.setPath(file.getAbsolutePath());
            TILE_QUEUE.offer(job);
            LOGGER.info("Added to TilingQueue: " + file.getID() + " " + file.getAbsolutePath());
            startMasterTilingThread();
        }
    }

    private void startMasterTilingThread() {
        if (!MCRImageTiler.isRunning()) {
            LOGGER.info("Starting Tiling thread.");
            final Thread tiling = new Thread(MCRImageTiler.getInstance());
            tiling.start();
        }
    }

}
