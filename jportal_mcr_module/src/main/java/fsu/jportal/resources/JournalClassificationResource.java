package fsu.jportal.resources;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.classifications2.MCRLabel;

import com.google.gson.Gson;

import fsu.jportal.gson.Category;
import fsu.jportal.gson.GsonManager;
import fsu.jportal.xml.MCRObjConnector;

@Path("classifications/jp/{id}")
public class JournalClassificationResource extends ClassificationResource {
    
    @PathParam("id")
    String journalID;

    @GET
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    public String list() {

        MCRObjConnector objConnector = new MCRObjConnector(journalID);
        openSession();
        String rubricID = objConnector.getRubric(journalID);
        if(rubricID == null){
            Category newRubricClassi = new Category();
            MCRCategoryID newRubricID = newRootID();
            newRubricClassi.setId(newRubricID);
            Set<MCRLabel> labels = new HashSet<MCRLabel>();
            MCRLabel label = new MCRLabel("de", "Rubrik für " + journalID, null);
            labels.add(label);
            newRubricClassi.setLabels(labels);
            getCategoryDAO().addCategory(newRubricClassi.getParentID(), newRubricClassi.asMCRImpl());
            objConnector.addRubric(newRubricID);
            Gson gson = GsonManager.instance().createGson();
            return gson.toJson(newRubricClassi);
        }
        closeSession();
        return get(rubricID);
    }
}
