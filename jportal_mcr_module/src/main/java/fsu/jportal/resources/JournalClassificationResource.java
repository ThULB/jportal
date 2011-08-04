package fsu.jportal.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.jdom.Document;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;

import fsu.jportal.xml.ClassificationIDExtractor;

@Path("classifications/{id}")
public class JournalClassificationResource extends ClassificationResource {
   
    @GET
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    public String getClassification(@PathParam("id") String journalID) {
        Document journalXML = MCRXMLMetadataManager.instance().retrieveXML(MCRObjectID.getInstance(journalID));
        new ClassificationIDExtractor();
        System.out.println("JournalID: " + journalID);
        return getClassification();
    }
}
