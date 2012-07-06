package fsu.jportal.resources;

import java.io.IOException;
import java.text.MessageFormat;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.mycore.common.MCRConfiguration;
import org.mycore.common.MCRJSONManager;
import org.mycore.parsers.bool.MCRBooleanClauseParser;
import org.mycore.parsers.bool.MCRCondition;
import org.mycore.services.fieldquery.MCRFieldDef;
import org.mycore.services.fieldquery.MCRQuery;
import org.mycore.services.fieldquery.MCRQueryManager;
import org.mycore.services.fieldquery.MCRQueryParser;
import org.mycore.services.fieldquery.MCRResults;
import org.mycore.services.fieldquery.MCRSortBy;

import fsu.jportal.gson.MCRHitTypeAdapter;
import fsu.jportal.gson.MCRResultsTypeAdapter;
import fsu.jportal.gson.MCRResultsWrapper;

@Path("search")
public class SearchResource {
    static Logger LOGGER = Logger.getLogger(SearchResource.class);
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String search(@QueryParam("q") String query, @QueryParam("s") String sortBy, @QueryParam("m") int maxResults, @QueryParam("o") String sortOrder) throws IOException{
        MCRQuery mcrQuery = parseQuery(query);
        boolean boolSortOrder = ("descending".equals(sortOrder)) ? MCRSortBy.DESCENDING : MCRSortBy.ASCENDING;
        
        if(sortBy != null){
            MCRSortBy mcrSortBy = new MCRSortBy(MCRFieldDef.getDef(sortBy), boolSortOrder);
            mcrQuery.setSortBy(mcrSortBy);
        }
        
        mcrQuery.setMaxResults(maxResults);
        
        MCRResults mcrResults =  MCRQueryManager.search(mcrQuery);
        return toJson(mcrResults);
    }
    
    @GET
    @Path("all")
    public String searchAll(@QueryParam("q") String query, @QueryParam("s") String sortBy, @QueryParam("m") int maxResults, @QueryParam("o") String sortOrder)throws IOException{
        query = MessageFormat.format("(allMeta like \"{0}\") or (content like \"{0}\")", query.toLowerCase());
        return search(query, sortBy, maxResults, sortOrder);
    }

    protected MCRQuery parseQuery(String query) {
        MCRQueryParser mcrQueryParser = new MCRQueryParser();
        MCRCondition mcrCondition = mcrQueryParser.parse(query);
        MCRQuery mcrQuery = new MCRQuery(mcrCondition);
        return mcrQuery;
    }

    protected String toJson(MCRResults mcrResults) {
        MCRJSONManager gsonManager = MCRJSONManager.instance();
        gsonManager.registerAdapter(new MCRResultsTypeAdapter());
        gsonManager.registerAdapter(new MCRHitTypeAdapter());
        String json = gsonManager.createGson().toJson(new MCRResultsWrapper(mcrResults));
        return json;
    }
}
