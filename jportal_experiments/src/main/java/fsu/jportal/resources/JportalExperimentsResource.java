package fsu.jportal.resources;

import java.io.InputStream;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.jdom2.Document;
import org.jdom2.Element;
import org.mycore.common.MCRPersistenceException;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;

@Path("exp")
public class JportalExperimentsResource {
    @GET
    @Path("{filename:.*}")
    public InputStream getResources(@PathParam("filename") String filename) {
        return this.getClass().getResourceAsStream("/html/" + filename);
    }

    @POST
    @Path("cp/{id}")
    public Response copy(@PathParam("id") String id, @QueryParam("numCopy") int numCopy) throws Exception {
        MCRObjectID mcrId = MCRObjectID.getInstance(id);
        String base = mcrId.getBase();
        Document origObjXML = MCRXMLMetadataManager.instance().retrieveXML(mcrId);
        Element origObjXMLRootTag = origObjXML.getRootElement();

        for (int i = 0; i < numCopy; i++) {
            MCRObjectID nextFreeId = MCRObjectID.getNextFreeId(base);
            origObjXMLRootTag.setAttribute("ID", nextFreeId.toString());
            origObjXMLRootTag.setAttribute("label", nextFreeId.toString());

            try {
                MCRMetadataManager.create(new MCRObject(origObjXML));
                //                MCRXMLMetadataManager.instance().create(nextFreeId, origObjXML, new Date());
            } catch (MCRPersistenceException e) {
                e.printStackTrace();
                return Response.serverError().build();
            }
        }

        return Response.ok().build();
    }
}
