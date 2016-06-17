package fsu.jportal.resources;


import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.mycore.common.MCRJSONManager;
import org.mycore.datamodel.classifications2.MCRCategoryDAO;
import org.mycore.datamodel.classifications2.MCRCategoryDAOFactory;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.classifications2.MCRLabel;
import org.mycore.frontend.classeditor.json.MCRJSONCategory;
import org.mycore.frontend.classeditor.resources.MCRClassificationEditorResource;

import com.google.gson.Gson;

import fsu.jportal.xml.MCRObjConnector;

@Path("classifications/jp/{id}")
public class JournalClassificationResource extends MCRClassificationEditorResource {

    private static final MCRCategoryDAO CATEGORY_DAO = MCRCategoryDAOFactory.getInstance();

    @PathParam("id")
    String journalID;

    @GET
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    public String list() {

        MCRObjConnector objConnector = new MCRObjConnector(journalID);
        List<String> rubrics = objConnector.getRubrics();
        if(rubrics.size() <= 0){
            MCRJSONCategory newRubricClassi = new MCRJSONCategory();
            MCRCategoryID newRubricID = newRootID();
            newRubricClassi.setId(newRubricID);
            Set<MCRLabel> labels = new HashSet<MCRLabel>();
            MCRLabel label = new MCRLabel("de", "Rubrik für " + journalID, null);
            labels.add(label);
            newRubricClassi.setLabels(labels);
            CATEGORY_DAO.addCategory(newRubricClassi.getParentID(), newRubricClassi.asMCRImpl());
            try {
                objConnector.addRubric(newRubricID);
            } catch(Exception e) {
                e.printStackTrace();
            }
            Gson gson = MCRJSONManager.instance().createGson();
            return gson.toJson(newRubricClassi);
        } else {
            StringBuffer rubricJsonArray = new StringBuffer();
            for (String rubricID : rubrics) {
                String rubricJson = get(rubricID);
                rubricJsonArray.append(rubricJson + ",");
            }
            return "[" + rubricJsonArray.toString().substring(0, rubricJsonArray.length()-1) + "]";
        }
    }
}
