package fsu.jportal.resources;

import fsu.jportal.util.ResolverUtil;
import org.apache.solr.common.params.ModifiableSolrParams;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by Huu Chi Vu on 15.06.17.
 */
@Path("facets")
public class FacetsResource {
    @GET
    @Path("label/{categID}")
    public String label(@PathParam("categID") String categID) {
        return ResolverUtil.getClassLabel(categID)
                           .orElse("undefined:" + categID);
    }

    @GET
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    public Response list() {
        //wt=json&sort=maintitle_sort asc&rows=9999&q='
        ModifiableSolrParams solrParams = new ModifiableSolrParams();

        return null;
    }
}
