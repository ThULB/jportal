package fsu.jportal.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("classifications/{id}")
public class JournalClassificationResource extends ClassificationResource {
    @GET
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    public String getClassification(@PathParam("id") String journalID){
        System.out.println("JournalID: " + journalID);
        return getClassification();
    }
    
}
