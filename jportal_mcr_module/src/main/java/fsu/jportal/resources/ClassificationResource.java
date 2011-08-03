package fsu.jportal.resources;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.poi.poifs.property.Parent;
import org.mycore.common.MCRConfiguration;
import org.mycore.common.MCRConfigurationException;
import org.mycore.common.MCRPersistenceException;
import org.mycore.common.MCRSession;
import org.mycore.common.MCRSessionMgr;
import org.mycore.datamodel.classifications2.MCRCategLinkService;
import org.mycore.datamodel.classifications2.MCRCategLinkServiceFactory;
import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryDAO;
import org.mycore.datamodel.classifications2.MCRCategoryDAOFactory;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.classifications2.MCRLabel;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonStreamParser;

import fsu.jportal.gson.CategJsonPropName;
import fsu.jportal.gson.Category;
import fsu.jportal.gson.GsonManager;
import fsu.jportal.wrapper.MCRCategoryListWrapper;

/**
 * This class is responsible for CRUD-operations of MCRCategories. It accepts
 * JSON objects of the form <code>
 * [{    "ID":{"rootID":"abcd","categID":"1234"}
 *      "label":[
 *          {"lang":"de","text":"Rubriken Test 2 fuer MyCoRe","descriptions":"test de"},
 *          {"lang":"en","text":"Rubric test 2 for MyCoRe","descriptions":"test en"}
 *      ],
 *      "parentID":{"rootID":"abcd","categID":"parent"}
 *      "children:"URL"
 * 
 * }
 * ...
 * ]
 * </code>
 * 
 * @author chi
 * 
 */
@Path("classifications")
public class ClassificationResource {
    private MCRSession currentSession = null;

    private boolean useSession = MCRConfiguration.instance().getBoolean("ClassificationResouce.useSession", true);

    private MCRCategoryDAO categoryDAO = null;

    @Context
    UriInfo uriInfo;

    private MCRCategLinkService linkService;

    @POST
    @Path("new")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response newClassification(String json) {
        return newCategoryFromJson(json, null, null);
    }

    @POST
    @Path("{rootIdStr}/new")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response newCategory(@PathParam("rootIdStr") String rootIdStr, String json) {
        return newCategoryFromJson(json, rootIdStr, null);
    }

    @POST
    @Path("{rootIdStr}/{parentIdStr}/new")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response newCategory(@PathParam("rootIdStr") String rootIdStr, @PathParam("parentIdStr") String parentIdStr, String json) {
        return newCategoryFromJson(json, rootIdStr, parentIdStr);
    }

    private Response newCategoryFromJson(String json, String rootIdStr, String parentIdStr) {
        Category category = parseJson(json);
        category.setParentID(createID(rootIdStr, parentIdStr));

        if (category.getId() == null) {
            assignId(rootIdStr, category);
        }

        return updateCateg(category);
    }

    private void assignId(String rootIdStr, Category category) {
        MCRCategoryID categoryID = newRandomUUID(rootIdStr);
        category.setId(categoryID);
    }

    private MCRCategoryID createID(String rootIdStr, String parentIdStr) {
        if (rootIdStr == null) {
            return null;
        }

        if (parentIdStr == null || "".equals(parentIdStr)) {
            return MCRCategoryID.rootID(rootIdStr);
        } else {
            return new MCRCategoryID(rootIdStr, parentIdStr);
        }
    }

    private Category parseJson(String json) {
        Gson gson = GsonManager.instance().createGson();
        Category category = gson.fromJson(json, Category.class);
        return category;
    }

    private Response updateCateg(Category categ) {
        openSession();
        MCRCategoryID newParentID = categ.getParentID();
        if (newParentID != null && !getCategoryDAO().exist(newParentID)) {
            throw new WebApplicationException(Status.NOT_FOUND);
        }

        Response response = null;
        if (getCategoryDAO().exist(categ.getId())) {
            Set<MCRLabel> labels = categ.getLabels();
            for (MCRLabel mcrLabel : labels) {
                getCategoryDAO().setLabel(categ.getId(), mcrLabel);
            }
            
            if(newParentID != null) {
                getCategoryDAO().moveCategory(categ.getId(), newParentID, categ.getPositionInParent());
            }
            response = Response.status(Status.OK).build();
        } else {
            getCategoryDAO().addCategory(newParentID, categ.asMCRImpl());
            URI uri = buildGetURI(categ.getId());
            response = Response.created(uri).build();
        }

        closeSession();
        return response;
    }

    private MCRCategoryID getParentID(Category categ) {
        List<MCRCategory> parents = getCategoryDAO().getParents(categ.getId());
        if (parents.size() > 0) {
            return parents.get(0).getId();
        }

        return null;
    }

    @GET
    @Path("newID/{rootID}")
    @Produces(MediaType.APPLICATION_JSON)
    public String newIDJson(@PathParam("rootID") String rootID) {
        Gson gson = GsonManager.instance().createGson();
        return gson.toJson(newRandomUUID(rootID));
    }

    private MCRCategoryID newRandomUUID(String rootID) {
        if (rootID == null) {
            rootID = UUID.randomUUID().toString();
        }

        return new MCRCategoryID(rootID, UUID.randomUUID().toString());
    }

    @GET
    @Path("newID")
    @Produces(MediaType.APPLICATION_JSON)
    public String newRootIDJson() {
        Gson gson = GsonManager.instance().createGson();
        return gson.toJson(newRootID());
    }

    private MCRCategoryID newRootID() {
        return MCRCategoryID.rootID(UUID.randomUUID().toString());
    }

    private URI buildGetURI(MCRCategoryID categoryID) {
        UriBuilder uriBuilder = UriBuilder.fromUri(uriInfo.getBaseUri());
        uriBuilder.path(this.getClass());
        uriBuilder.path(categoryID.getRootID());
        String categID = categoryID.getID();
        if (categID != null && !"".equals(categID)) {
            uriBuilder.path(categID);
        }

        return uriBuilder.build();
    }

    protected void openSession() {
        if (useSession) {
            currentSession = MCRSessionMgr.getCurrentSession();
            currentSession.beginTransaction();
        }
    }

    protected void closeSession() {
        if (useSession) {
            currentSession.commitTransaction();
            currentSession.close();
            currentSession = null;
        }
    }

    private MCRCategoryDAO getCategoryDAO() {
        if (categoryDAO == null) {
            categoryDAO = MCRCategoryDAOFactory.getInstance();
        }
        return categoryDAO;
    }

    /**
     * @param rootidStr
     *            rootID.categID
     * @return
     */
    @GET
    @Path("{rootidStr}")
    @Produces(MediaType.APPLICATION_JSON)
    public String get(@PathParam("rootidStr") String rootidStr) {
        if (rootidStr == null || "".equals(rootidStr)) {
            throw new WebApplicationException(Status.NOT_FOUND);
        }

        MCRCategoryID id = MCRCategoryID.rootID(rootidStr);
        return getCategory(id);
    }

    /**
     * @param rootidStr
     *            rootID.categID
     * @return
     */
    @GET
    @Path("{rootidStr}/{categidStr}")
    @Produces(MediaType.APPLICATION_JSON)
    public String get(@PathParam("rootidStr") String rootidStr, @PathParam("categidStr") String categidStr) {

        if (rootidStr == null || "".equals(rootidStr) || categidStr == null || "".equals(categidStr)) {
            throw new WebApplicationException(Status.NOT_FOUND);
        }

        MCRCategoryID id = new MCRCategoryID(rootidStr, categidStr);
        return getCategory(id);
    }

    private String getCategory(MCRCategoryID id) {
        openSession();
        if (!getCategoryDAO().exist(id)) {
            throw new WebApplicationException(Status.NOT_FOUND);
        }

        MCRCategory category = getCategoryDAO().getCategory(id, 1);
        if (!(category instanceof Category)) {
            category = new Category(category);
        }
        Gson gson = GsonManager.instance().createGson();

        String json = gson.toJson(category);
        closeSession();
        return json;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getClassification() {
        openSession();
        Gson gson = GsonManager.instance().createGson();
        List<MCRCategory> rootCategories = getCategoryDAO().getRootCategories();
        Map<MCRCategoryID, Boolean> linkMap = getLinkService().hasLinks(null);
        String json = gson.toJson(new MCRCategoryListWrapper(rootCategories, linkMap));
        closeSession();
        return json;
    }

    private MCRCategLinkService getLinkService() {
        if (linkService == null) {
            try {
                linkService = (MCRCategLinkService) MCRConfiguration.instance().getInstanceOf("Category.Link.Service");
            } catch (MCRConfigurationException e) {
                linkService = MCRCategLinkServiceFactory.getInstance();
            }
        }

        return linkService;
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateClassification(String json) {
        Category category = parseJson(json);
        return updateCategory(category);

    }

    private Response updateCategory(Category newCategory) {
        openSession();
        if (!getCategoryDAO().exist(newCategory.getId())) {
            return Response.status(Status.NOT_FOUND).build();
        } else {
            MCRCategory oldCategory = getCategoryDAO().getCategory(newCategory.getId(), -1);
            newCategory.setChildren(oldCategory.getChildren());
            getCategoryDAO().replaceCategory(newCategory);

            closeSession();
            return Response.ok().build();
        }
    }

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteCateg(String json) {
        openSession();
        Category category = parseJson(json);

        try {
            if (getCategoryDAO().exist(category.getId())) {
                getCategoryDAO().deleteCategory(category.getId());
                closeSession();
                return Response.status(Status.GONE).build();
            } else {
                return Response.notModified().build();
            }
        } catch (MCRPersistenceException e) {
            e.printStackTrace();
            closeSession();
            return Response.status(Status.NOT_FOUND).build();
        }
    }

    @POST
    @Path("save")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response save(String json) {
        JsonStreamParser jsonStreamParser = new JsonStreamParser(json);
        if (jsonStreamParser.hasNext()) {
            JsonArray saveObjArray = jsonStreamParser.next().getAsJsonArray();

            for (JsonElement jsonElement : saveObjArray) {
                String status = getStatus(jsonElement);
                SaveElement categ = getCateg(jsonElement);
                Category parsedCateg = parseJson(categ.getJson());

                if ("update".equals(status)) {
                    updateCateg(parsedCateg);
                } else if ("delete".equals(status)) {
                    deleteCateg(categ.getJson());
                } else {
                    return Response.status(Status.BAD_REQUEST).build();
                }
            }
            return Response.status(Status.OK).build();
        } else {
            return Response.status(Status.BAD_REQUEST).build();
        }
    }

    private SaveElement getCateg(JsonElement jsonElement) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        JsonObject categ = jsonObject.get("item").getAsJsonObject();
        JsonElement parentID = jsonObject.get("parentId");
        JsonElement position = jsonObject.get("index");
        boolean hasParent = false;

        if (parentID != null && !parentID.toString().contains("_placeboid_") && position != null) {
            categ.add(CategJsonPropName.PARENTID, parentID);
            categ.add(CategJsonPropName.POSITION, position);
            hasParent = true;
        }

        return new SaveElement(categ.toString(), hasParent);
    }

    private class SaveElement {
        private String categJson;

        private boolean hasParent;

        public SaveElement(String categJson, boolean hasParent) {
            this.setCategJson(categJson);
            this.setHasParent(hasParent);
        }

        private void setHasParent(boolean hasParent) {
            this.hasParent = hasParent;
        }

        public boolean hasParent() {
            return hasParent;
        }

        private void setCategJson(String categJson) {
            this.categJson = categJson;
        }

        public String getJson() {
            return categJson;
        }
    }

    private String getStatus(JsonElement jsonElement) {
        return jsonElement.getAsJsonObject().get("state").getAsString();
    }
}
