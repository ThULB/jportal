package fsu.jportal.resources;

import java.io.IOException;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.mycore.parsers.bool.MCRCondition;
import org.mycore.services.fieldquery.MCRFieldDef;
import org.mycore.services.fieldquery.MCRQuery;
import org.mycore.services.fieldquery.MCRQueryManager;
import org.mycore.services.fieldquery.MCRQueryParser;
import org.mycore.services.fieldquery.MCRResults;
import org.mycore.services.fieldquery.MCRSortBy;

import fsu.jportal.gson.GsonManager;
import fsu.jportal.gson.MCRHitTypeAdapter;
import fsu.jportal.gson.MCRResultsTypeAdapter;
import fsu.jportal.gson.MCRResultsWrapper;

@Path("search")
public class SearchResource {
    static Logger LOGGER = Logger.getLogger(SearchResource.class);
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public String search(String query, @QueryParam("sortBy") String sortBy) throws IOException{
        MCRQueryParser mcrQueryParser = new MCRQueryParser();
        MCRCondition mcrCondition = mcrQueryParser.parse(query);
        MCRQuery mcrQuery = new MCRQuery(mcrCondition);
        
        if(sortBy != null){
            MCRSortBy mcrSortBy = new MCRSortBy(MCRFieldDef.getDef(sortBy), MCRSortBy.ASCENDING);
            mcrQuery.setSortBy(mcrSortBy);
        }
        
        MCRResults mcrResults = MCRQueryManager.search(mcrQuery);
        
        GsonManager gsonManager = GsonManager.instance();
        gsonManager.registerAdapter(new MCRResultsTypeAdapter());
        gsonManager.registerAdapter(new MCRHitTypeAdapter());
        String json = gsonManager.createGson().toJson(new MCRResultsWrapper(mcrResults));
        return json;
    }
}
