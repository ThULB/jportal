package fsu.jportal.resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryID;

import com.google.gson.Gson;

import fsu.jportal.gson.GsonManager;
import fsu.jportal.wrapper.MCRCategoryListWrapper;
import fsu.jportal.xml.ClassificationIDExtractor;

@Path("classifications/jp/{id}")
public class JournalClassificationResource extends ClassificationResource {
   
    @GET
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    public String getClassification(@PathParam("id") String journalID) {
        ClassificationIDExtractor classificationIDExtractor = new ClassificationIDExtractor();
        List<String> classIDs = classificationIDExtractor.getClassIDs(journalID);
        
        openSession();
        Gson gson = GsonManager.instance().createGson();
        ArrayList<MCRCategory> categList = new ArrayList<MCRCategory>();
        Map<MCRCategoryID, Boolean> linkMap = new HashMap<MCRCategoryID, Boolean>();
        for (String classId : classIDs) {
            MCRCategory classification = getCategoryDAO().getCategory(MCRCategoryID.rootID(classId), 0);
            categList.add(classification);
            Map<MCRCategoryID, Boolean> hasLinks = getLinkService().hasLinks(classification);
            linkMap.putAll(hasLinks);
        }
        
        String json = gson.toJson(new MCRCategoryListWrapper(categList, linkMap));
        closeSession();
        return json;
    }
}
