package fsu.jportal.resources;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.metadata.MCRMetaLinkID;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.metadata.MCRObjectStructure;
import org.mycore.frontend.jersey.MCRJerseyUtil;
import org.mycore.solr.index.MCRSolrIndexer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import fsu.jportal.backend.JPContainer;
import fsu.jportal.backend.sort.JPLevelSorting;
import fsu.jportal.backend.sort.JPSorter;
import fsu.jportal.backend.sort.JPSorter.Order;
import fsu.jportal.util.JPComponentUtil;
import fsu.jportal.util.JPLevelSortingUtil;

/**
 * Resource to handle jportal sorting. 
 * 
 * The following endpoints are available:
 * <ul>
 * <li>the auto or manual sorting of objects
 *  <ul>
 *   <li>POST sortby/{id} -> sets the sortBy for the given object</li>
 *   <li>DELETE sortby/{id} -> removes the sortBy for the given object </li>
 *   <li>POST resort/{id} -> manual resorting with json data</li>
 *  </ul>
 * </li>
 * <li>the level sorting of journals
 *  <ul>
 *   <li>GET level/{journal id}</li>
 *  </ul>
 * </li>
 * </ul>
 * 
 * @author Matthias Eichner
 */
@Path("sort")
public class SortResource {

    /**
     * Sets a new sort by for the given object.
     * 
     * @param id the object identifier
     * @param sorterClass the new sorter class 
     * @param orderString the order ascending|descending
     */
    @POST
    @Path("sortby/{id}")
    public void sortByUpdate(@PathParam("id") String id, @QueryParam("sorter") String sorterClass,
        @QueryParam("order") String orderString) {
        JPContainer jpContainer = get(id);
        try {
            Class<? extends JPSorter> sorter = Class.forName(sorterClass).asSubclass(JPSorter.class);
            Order order = Order.valueOf(orderString.toUpperCase());
            jpContainer.setSortBy(sorter, order);
            jpContainer.store();
        } catch (Exception exc) {
            throw new WebApplicationException(exc,
                Response.status(Status.INTERNAL_SERVER_ERROR)
                        .entity("Unable to set sorter " + sorterClass + " for " + id)
                        .build());
        }
    }

    /**
     * Removes the sort by for the given object
     * 
     * @param id the object identifier
     */
    @DELETE
    @Path("sortby/{id}")
    public void sortByDelete(@PathParam("id") String id) {
        JPContainer jpContainer = get(id);
        try {
            jpContainer.setSortBy(null, null);
            jpContainer.store();
        } catch (Exception exc) {
            throw new WebApplicationException(exc,
                Response.status(Status.INTERNAL_SERVER_ERROR).entity("Unable to remove sorter for " + id).build());
        }
    }

    /**
     * Does a resorting of the given object
     * 
     * @param id the object identifier
     * @param data the json array containing all children on a specific position
     * <pre>
     * {@code
     *   
     * }
     * </pre>
     */
    @POST
    @Path("resort/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void sort(@PathParam("id") String id, String data) {
        JPContainer container = get(id);
        List<MCRObjectID> mcrChildren = container.getChildren();
        JsonArray jsonArray = new JsonParser().parse(data).getAsJsonArray();

        // check size
        if (jsonArray.size() != mcrChildren.size()) {
            throwUnprocessableEntity("The json array contains " + jsonArray.size() + " children but the object has "
                + mcrChildren.size() + ".");
        }
        // check each object
        List<MCRObjectID> newChildren = new ArrayList<>();
        List<MCRObjectID> changedPosition = new ArrayList<>();
        for (int jsonIndex = 0; jsonIndex < jsonArray.size(); jsonIndex++) {
            JsonElement e = jsonArray.get(jsonIndex);
            String errorMsg = "The json array contains an invalid object " + e + ".";
            if (!e.isJsonObject()) {
                throwUnprocessableEntity(errorMsg);
            }
            JsonObject jsonObject = e.getAsJsonObject();
            if (!jsonObject.has("id")) {
                throwUnprocessableEntity(errorMsg + ". The id is missing.");
            }
            String childId = jsonObject.get("id").getAsString();
            MCRObjectID mcrChildID = MCRObjectID.getInstance(childId);
            if (!mcrChildren.contains(mcrChildID)) {
                throwUnprocessableEntity(errorMsg + ". The object is no child of " + id + ".");
            }
            newChildren.add(mcrChildID);
            if (!mcrChildren.get(jsonIndex).equals(mcrChildID)) {
                changedPosition.add(mcrChildID);
            }
        }

        // set new children
        MCRObjectStructure structure = container.getObject().getStructure();
        structure.clearChildren();
        newChildren.stream().map(childId -> {
            return new MCRMetaLinkID("child", childId, null, null);
        }).forEachOrdered(structure::addChild);

        try {
            // TODO: cannot use MCRMetadataManager because it does not respect the children order
            // container.store();

            // store
            MCRObject obj = container.getObject();
            MCRXMLMetadataManager.instance().update(obj.getId(), obj.createXML(), new Date());
            MCRMetadataManager.fireUpdateEvent(obj);
            // solr events for position changed 
            MCRSolrIndexer.rebuildMetadataIndex(
                changedPosition.stream().map(MCRObjectID::toString).collect(Collectors.toList()), true);
        } catch (Exception exc) {
            throwInternalServerError(exc, "Unable to store object " + id + ".");
        }
    }

    /**
     * Returns the level sorting structure for the given journal id.
     * The isNew attribute defines if the level sorting was loaded by
     * the configuration or if there was no configuration available.
     * <p>
     * When the isNew parameter is false, then the returning level
     * sorting structure is auto generated. The journal structure
     * was analyzed and a good sample starting point is returned.
     * </p>
     * 
     * <pre>{@code
     * {
     *   "isNew": true|false,
     *   "levels": [
     *     {index: 0, name: 'Zeitschrift', sorter: 'fsu.jportal.sort.JPMagicSorter'},
     *     ...
     *   ]
     * }
     * </pre>
     * 
     * @return the level sorting as json array
     */
    @GET
    @Path("level/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLevel(@PathParam("id") String id) {
        JPLevelSorting levelSorting = null;
        MCRObjectID journalId = MCRObjectID.getInstance(id);
        JsonObject returnObject = new JsonObject();
        try {
            levelSorting = JPLevelSortingUtil.load(journalId);
        } catch (IOException exc) {
            throwInternalServerError(exc, "Unable get level configuration for journal " + id + ".");
        }
        boolean isNew = levelSorting.isEmpty();
        levelSorting = isNew ? JPLevelSortingUtil.analyze(journalId) : levelSorting;
        returnObject.addProperty("isNew", isNew);
        returnObject.add("levels", levelSorting.toJSON());
        return Response.ok(returnObject.toString()).build();
    }

    @POST
    @Path("level/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void setLevel(@PathParam("id") String id, String data) {
        MCRObjectID journalId = MCRObjectID.getInstance(id);
        JsonArray array = new JsonParser().parse(data).getAsJsonArray();
        JPLevelSorting levelSorting;
        try {
            levelSorting = JPLevelSorting.fromJSON(array);
            JPLevelSortingUtil.store(journalId, levelSorting);
        } catch (ClassNotFoundException exc) {
            throwInternalServerError(exc, "Unable to store level sorting for " + id + ". One sorter is invalid.");
        } catch (IOException exc) {
            throwInternalServerError(exc,
                "Unable to store level sorting for " + id + ". Couldn't store on filesystem.");
        }
    }

    private void throwUnprocessableEntity(String msg) {
        throw new WebApplicationException(Response.status(422).entity(msg).build());
    }

    private void throwInternalServerError(Exception cause, String msg) {
        throw new WebApplicationException(cause, Response.status(Status.INTERNAL_SERVER_ERROR).entity(msg).build());
    }

    private JPContainer get(String id) {
        MCRObjectID mcrId = MCRObjectID.getInstance(id);
        MCRJerseyUtil.checkPermission(mcrId, MCRAccessManager.PERMISSION_WRITE);
        return JPComponentUtil.getContainer(mcrId).orElse(null);
    }

}
