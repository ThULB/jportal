package fsu.jportal.resources;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
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

@Path("cmd")
public class CMDCenterResource {
    private MCRSession currentSession = null;

    private boolean useSession = MCRConfiguration.instance().getBoolean(
            "ClassificationResouce.useSession", true);
	@Context UriInfo uriinfo;
	
	@POST
	@Path("mergeDerivIn/{objID}")
	public Response mergeDerivates(@PathParam("objID") String objID){
	    openSession();
		MCRObject mcrObject = MCRMetadataManager.retrieveMCRObject(MCRObjectID.getInstance(objID));
		List<MCRMetaLinkID> derivates = mcrObject.getStructure().getDerivates();
		closeSession();
		
		if(derivates.size() > 1){
		    openSession();
		    MCRMetaLinkID destDerivLink = derivates.remove(0);
		    String destDerivID = destDerivLink.getXLinkHref();
            MCRDirectory destDeriv = (MCRDirectory) MCRFilesystemNode.getRootNode(destDerivID);
		    
		    for (MCRMetaLinkID mcrMetaLinkID : derivates) {
                mcrMetaLinkID.getXLinkHrefID();
                String id = mcrMetaLinkID.getXLinkHref();
                MCRDirectory derivate = (MCRDirectory) MCRFilesystemNode.getRootNode(id);
                for (MCRFilesystemNode child : derivate.getChildren()) {
                    child.move(destDeriv);
                }
                MCRMetadataManager.deleteMCRDerivate(MCRObjectID.getInstance(id));
            }
		    closeSession();
		    
		    ImageTiling imageTiling = new ImageTiling(destDerivID);
		    new Thread(imageTiling).start();
		}
		return Response.ok().build();
	}
	
	private class ImageTiling implements Runnable{
        private String derivID;
        
        @Override
        public void run() {
            httpGetWebCLI("request", "getKnownCommands");
            httpGetWebCLI("run", "tile images of derivate " + derivID);
        }

        private void httpGetWebCLI(String queryName, Object queryValue) {
            URI baseUri = uriinfo.getBaseUri();
            URI requestURI = UriBuilder.fromUri(baseUri).replacePath("servlets/MCRWebCLIServlet").queryParam(queryName, queryValue).build();
            try {
                URL requestURL = requestURI.toURL();
                HttpURLConnection connection = (HttpURLConnection) requestURL.openConnection();
                connection.setRequestMethod("GET");
                connection.setUseCaches(false);
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        public ImageTiling(String derivID) {
            this.derivID = derivID;
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
            currentSession.close();
            currentSession = null;
        }
    }
}
