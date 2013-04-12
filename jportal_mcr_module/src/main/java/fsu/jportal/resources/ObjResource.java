package fsu.jportal.resources;

import java.io.IOException;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.xml.sax.SAXException;

@Path("obj")
public class ObjResource {
    
    @POST
    @Path("{id}/mergeDeriv")
    public Response mergeDerivates(@PathParam("id") String objID){
        MCRObjectID mcrObjectID = MCRObjectID.getInstance(objID);
        try {
            Document objXML = MCRXMLMetadataManager.instance().retrieveXML(mcrObjectID);
            
            if(objXML == null){
                return Response.status(Status.NOT_FOUND).build();
            }
            
            
        } catch (IOException | JDOMException | SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return null;
    }
}
