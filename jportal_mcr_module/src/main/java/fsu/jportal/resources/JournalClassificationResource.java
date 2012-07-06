package fsu.jportal.resources;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.mycore.common.MCRJSONManager;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.classifications2.MCRLabel;
import org.mycore.frontend.classeditor.json.MCRJSONCategory;
import org.mycore.frontend.classeditor.resources.MCRClassificationEditorResource;

import com.google.gson.Gson;

import fsu.jportal.xml.MCRObjConnector;

@Path("classifications/jp/{id}")
public class JournalClassificationResource extends MCRClassificationEditorResource {
    
    @PathParam("id")
    String journalID;

    @GET
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    public String list() {

        MCRObjConnector objConnector = new MCRObjConnector(journalID);
        String rubricID = objConnector.getRubric(journalID);
        if(rubricID == null){
            MCRJSONCategory newRubricClassi = new MCRJSONCategory();
            MCRCategoryID newRubricID = newRootID();
            newRubricClassi.setId(newRubricID);
            Set<MCRLabel> labels = new HashSet<MCRLabel>();
            MCRLabel label = new MCRLabel("de", "Rubrik für " + journalID, null);
            labels.add(label);
            newRubricClassi.setLabels(labels);
            getCategoryDAO().addCategory(newRubricClassi.getParentID(), newRubricClassi.asMCRImpl());
            objConnector.addRubric(newRubricID);
            Gson gson = MCRJSONManager.instance().createGson();
            return gson.toJson(newRubricClassi);
        }
        return get(rubricID);
    }
}
