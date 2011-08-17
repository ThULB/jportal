package fsu.jportal.resources;

import java.io.File;
import java.util.List;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.mycore.common.MCRConfiguration;
import org.mycore.common.MCRSession;
import org.mycore.common.MCRSessionMgr;
import org.mycore.datamodel.ifs.MCRDirectory;
import org.mycore.datamodel.ifs.MCRFilesystemNode;
import org.mycore.datamodel.metadata.MCRMetaLinkID;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.imagetiler.MCRImage;
import org.mycore.iview2.services.MCRIView2Tools;

@Path("cmd")
public class CMDCenterResource {
    private MCRSession currentSession = null;

    private boolean useSession = MCRConfiguration.instance().getBoolean(
            "ClassificationResouce.useSession", true);
	@Context UriInfo uriinfo;
	
	@POST
	@Path("mergeDerivIn/{objID}")
	public Response mergeDerivates(@PathParam("objID") String objID){
		MCRObject mcrObject = MCRMetadataManager.retrieveMCRObject(MCRObjectID.getInstance(objID));
		List<MCRMetaLinkID> derivates = mcrObject.getStructure().getDerivates();
		
		if(derivates.size() > 1){
		    MCRMetaLinkID destDerivLink = derivates.remove(0);
		    String destDerivID = destDerivLink.getXLinkHref();
		    openSession();
            MCRDirectory destDeriv = (MCRDirectory) MCRFilesystemNode.getRootNode(destDerivID);
            File dirOfDestDerivTiles = MCRImage.getTiledFile(MCRIView2Tools.getTileDir(), destDerivID, null);
		    
		    for (MCRMetaLinkID mcrMetaLinkID : derivates) {
                String derivId = mcrMetaLinkID.getXLinkHref();
                MCRDirectory derivate = (MCRDirectory) MCRFilesystemNode.getRootNode(derivId);
                for (MCRFilesystemNode child : derivate.getChildren()) {
                    child.move(destDeriv);
                }
                File dirOfDerivTiles = MCRImage.getTiledFile(MCRIView2Tools.getTileDir(), derivId, null);
                moveTiles(dirOfDerivTiles, dirOfDestDerivTiles);
                MCRMetadataManager.deleteMCRDerivate(MCRObjectID.getInstance(derivId));
            }
		    closeSession();
		    
		}
		return Response.ok().build();
	}

    private void moveTiles(File fromDir, File toDir) {
        File[] iviewFiles = fromDir.listFiles();
        for (File iviewFile : iviewFiles) {
            iviewFile.renameTo(new File(toDir, iviewFile.getName()));
        }
    }
	
	private void openSession() {
        if (useSession) {
            currentSession = MCRSessionMgr.getCurrentSession();
            currentSession.beginTransaction();
        }
    }

    private void closeSession() {
        if (useSession) {
            currentSession.commitTransaction();
            currentSession = null;
        }
    }
}
