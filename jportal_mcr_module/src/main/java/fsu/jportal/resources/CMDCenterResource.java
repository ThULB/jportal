package fsu.jportal.resources;

import java.net.URI;
import java.util.List;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.mycore.datamodel.metadata.MCRMetaLinkID;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;

@Path("cmd")
public class CMDCenterResource {
	@Context UriInfo uriinfo;
	
	@POST
	@Path("mergeDerivIn/{objID}")
	public Response mergeDerivates(@PathParam("objID") String objID){
		MCRObject mcrObject = MCRMetadataManager.retrieveMCRObject(MCRObjectID.getInstance(objID));
		List<MCRMetaLinkID> derivates = mcrObject.getStructure().getDerivates();
		
		URI baseUri = uriinfo.getBaseUri();
		URI redirectURI = UriBuilder.fromUri(baseUri).replacePath("receive/" + objID).build();
		return Response.temporaryRedirect(redirectURI).build();
	}
}
