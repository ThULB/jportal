package fsu.jportal.resources;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.mycore.solr.MCRSolrServerFactory;
import org.mycore.solr.search.MCRSolrURL;

@Path("search")
public class SearchResource {
    static Logger LOGGER = Logger.getLogger(SearchResource.class);

    @Context
    HttpServletResponse httpResponse;

    /**
     * 
     * @param uriInfo
     * @return json result
     * 
     * @throws IOException
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String search(@Context UriInfo uriInfo) throws IOException {
        String encodedQuery = "";
        for(Entry<String, List<String>> e : uriInfo.getQueryParameters().entrySet()) {
            encodedQuery += "&" + e.getKey() + "=" + URLEncoder.encode(e.getValue().get(0), "UTF-8");
        }
        encodedQuery = encodedQuery.substring(1)+"&wt=json";
        MCRSolrURL solrURL = new MCRSolrURL(MCRSolrServerFactory.getSolrServer(), encodedQuery);
        return search(solrURL);
    }

    protected String search(MCRSolrURL solrURL) throws IOException  {
        StringWriter writer = new StringWriter();
        IOUtils.copy(solrURL.openStream(), writer, "UTF-8");
        return writer.toString();
    }

}
