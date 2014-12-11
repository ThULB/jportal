package fsu.jportal.resources;

import static org.mycore.access.MCRAccessManager.PERMISSION_WRITE;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.mycore.access.MCRAccessManager;
import org.mycore.common.MCRJSONManager;
import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryDAO;
import org.mycore.datamodel.classifications2.MCRCategoryDAOFactory;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.classeditor.json.MCRJSONCategory;

import com.google.gson.Gson;

/**
 * Handles derivate context requests.
 * 
 * @author Matthias Eichner
 */
@Path("derivate/context")
public class DerivateContextResource {

    /**
     * Lists the derivateContext classification (first level). 
     * 
     * @return json
     */
    @GET
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    public String list() {
        MCRCategoryID id = MCRCategoryID.rootID("derivateContext");
        MCRCategoryDAO dao = MCRCategoryDAOFactory.getInstance();
        if (!dao.exist(id)) {
            throw new WebApplicationException(Response.status(Status.NOT_FOUND)
                .entity("There is no 'derivateContext' classification.").build());
        }
        MCRCategory category = dao.getCategory(id, 1);
        if (!(category instanceof MCRJSONCategory)) {
            category = new MCRJSONCategory(category);
        }
        Gson gson = MCRJSONManager.instance().createGson();
        return gson.toJson(category);
    }

    /**
     * Updates the context uri of the given derivate. This changes the xlink:role attribute of the linkmeta element.
     * If the contextURI parameter is empty or not set the context is removed.
     * 
     * @param derivateId derivate to update
     * @param contextURI the new context uri.
     * 
     * @throws Exception
     */
    @POST
    @Path("update/{derivateId}")
    public void update(@PathParam("derivateId") String derivateId, @QueryParam("context") String contextURI)
        throws Exception {
        if (contextURI == null || contextURI.equals("")) {
            contextURI = null;
        }
        MCRObjectID mcrDerivateId = MCRObjectID.getInstance(derivateId);
        if (!MCRAccessManager.checkPermission(mcrDerivateId, PERMISSION_WRITE)) {
            throw new WebApplicationException(Response.status(Status.UNAUTHORIZED).build());
        }
        MCRDerivate mcrDerivate = MCRMetadataManager.retrieveMCRDerivate(mcrDerivateId);
        mcrDerivate.getDerivate().getMetaLink().setXLinkRole(contextURI);
        MCRMetadataManager.update(mcrDerivate);
    }

}
