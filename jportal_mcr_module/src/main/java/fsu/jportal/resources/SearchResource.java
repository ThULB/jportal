package fsu.jportal.resources;

import java.io.IOException;
import java.io.StringWriter;
import java.text.MessageFormat;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.mycore.common.MCRJSONManager;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.xml.MCRLayoutService;
import org.mycore.parsers.bool.MCRCondition;
import org.mycore.services.fieldquery.MCRFieldDef;
import org.mycore.services.fieldquery.MCRQuery;
import org.mycore.services.fieldquery.MCRQueryManager;
import org.mycore.services.fieldquery.MCRQueryParser;
import org.mycore.services.fieldquery.MCRResults;
import org.mycore.services.fieldquery.MCRSortBy;
import org.mycore.solr.SolrServerFactory;
import org.mycore.solr.search.SolrURL;

import fsu.jportal.gson.MCRHitTypeAdapter;
import fsu.jportal.gson.MCRResultsTypeAdapter;
import fsu.jportal.gson.MCRResultsWrapper;

@Path("search")
public class SearchResource {
    static Logger LOGGER = Logger.getLogger(SearchResource.class);

    @Context
    HttpServletResponse httpResponse;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String search(@QueryParam("q") String query, @QueryParam("s") String sortBy, @QueryParam("m") Integer maxResults,
            @QueryParam("o") String sortOrder) throws IOException {
        SolrURL solrURL = new SolrURL(SolrServerFactory.getSolrServer());
        solrURL.setQueryParamter(query);
        if(maxResults != null) {
            solrURL.setRows(maxResults);
        }
        if(sortBy != null) {
            solrURL.addSortOption(sortBy, sortOrder);
        }
        solrURL.setWriterType("json");
        StringWriter writer = new StringWriter();
        IOUtils.copy(solrURL.openStream(), writer, "UTF-8");
        return writer.toString();
    }

    private MCRResults mcrQuery(String query, String sortBy, int maxResults, String sortOrder) {
        MCRQuery mcrQuery = parseQuery(query);
        boolean boolSortOrder = ("descending".equals(sortOrder)) ? MCRSortBy.DESCENDING : MCRSortBy.ASCENDING;

        if (sortBy != null) {
            MCRSortBy mcrSortBy = new MCRSortBy(MCRFieldDef.getDef(sortBy), boolSortOrder);
            mcrQuery.setSortBy(mcrSortBy);
        }

        mcrQuery.setMaxResults(maxResults);

        MCRResults mcrResults = MCRQueryManager.search(mcrQuery);
        return mcrResults;
    }

    @POST
    @Path("all")
    public void searchAllForm(@FormParam("q") String query, @FormParam("s") String sortBy, @FormParam("m") int maxResults,
            @FormParam("o") String sortOrder) throws IOException {
        MCRResults mcrResults = mcrQuery(query, sortBy, maxResults, sortOrder);
        MCRLayoutService.instance().doLayout(null, httpResponse, new MCRJDOMContent(mcrResults.buildXML()));
    }

    @GET
    @Path("all")
    public String searchAll(@QueryParam("q") String query, @QueryParam("s") String sortBy, @QueryParam("m") int maxResults,
            @QueryParam("o") String sortOrder) throws IOException {
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
