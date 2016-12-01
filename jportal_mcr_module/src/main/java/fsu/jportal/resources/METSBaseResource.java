package fsu.jportal.resources;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.mycore.access.MCRAccessManager;
import org.mycore.common.MCRStreamUtils;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.frontend.jersey.MCRJerseyUtil;
import org.mycore.mets.misc.StructLinkGenerator;
import org.mycore.mets.model.Mets;
import org.mycore.mets.model.struct.LogicalDiv;
import org.mycore.mets.model.struct.LogicalStructMap;
import org.mycore.mets.model.struct.StructLink;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import fsu.jportal.backend.JPPeriodicalComponent;
import fsu.jportal.mets.ALTOMETSHierarchyGenerator;
import fsu.jportal.mets.MetsVersionStore;
import fsu.jportal.util.JPComponentUtil;
import fsu.jportal.util.MetsUtil;

/**
 * <p>
 * <b>Generate</b><br />
 * Generates the mets.xml for the given derivate.
 * This will overwrite the existing mets.xml.
 * </p>
 * 
 * <p>
 * <b>Synchronize:</b><br />
 * Synchronize the mets.xml of a derivate with the jportal object structure.
 * 
 * <ul>
 *  <li>Syncs the label's of the logical divs with the corresponding mycore object titles.
 *  (this does only work when the identifier of an logical div is an mycore object id)</li>
 *  <li>Updates the structLink section if necessary.</li>
 * </ul>
 * </p>
 * @author Matthias Eichner
 */
@Path("mets/base")
public class METSBaseResource {

    private static final Logger LOGGER = LogManager.getLogger(METSBaseResource.class);

    /**
     * Generates the mets.xml for the given derivate. This will overwrite the existing mets.xml.
     * 
     * @param derivateId
     * @return
     */
    @Path("generate/{id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response generate(@PathParam("id") String derivateId) {
        // check write permission on derivate
        MCRJerseyUtil.checkPermission(MCRObjectID.getInstance(derivateId), MCRAccessManager.PERMISSION_WRITE);
        try {
            // get old mets
            Mets oldMets;
            try {
                oldMets = MetsUtil.getMets(derivateId);
            } catch(FileNotFoundException fnfe) {
                oldMets = null;
            }
            // generate
            Mets newMets = new ALTOMETSHierarchyGenerator(oldMets).getMETS(MCRPath.getPath(derivateId, "/"),
                new HashSet<MCRPath>());
            // as mcr content
            MCRJDOMContent newMetsContent = new MCRJDOMContent(newMets.asDocument());
            // store old mets
            storeOldMets(derivateId);
            // path to mets.xml
            MCRPath metsPath = MCRPath.getPath(derivateId, "mets.xml");
            // store in derivate
            Files.copy(newMetsContent.getInputStream(), metsPath, StandardCopyOption.REPLACE_EXISTING);
            // send response
            JsonObject response = new JsonObject();
            response.addProperty("status", "ok");
            return Response.ok().entity(response.toString()).build();
        } catch (WebApplicationException webExc) {
            throw webExc;
        } catch (Exception exc) {
            throw new WebApplicationException(exc, Response.serverError().build());
        }
    }

    /**
     * Synchronizes the mets.xml with the object structure.
     * Returns a json object containing the number of changed logical div's.
     * 
     * @param derivateId the derivate
     * @return json object
     */
    @Path("sync/{id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response sync(@PathParam("id") String derivateId) {
        // check write permission on derivate
        MCRJerseyUtil.checkPermission(MCRObjectID.getInstance(derivateId), MCRAccessManager.PERMISSION_WRITE);
        // get mets
        Mets mets = getMets(derivateId);

        // do the sync
        List<LogicalDiv> logicalDivList = getLogicalDivs(mets);
        List<LogicalDiv> updatedList = new ArrayList<>();
        Map<LogicalDiv, String> errorMap = new HashMap<>();
        updateMaintitlesOfLogicalDivs(logicalDivList, updatedList, errorMap);

        boolean structLinkSynced = syncStructLink(mets);

        if (structLinkSynced || !updatedList.isEmpty()) {
            // store old mets
            storeOldMets(derivateId);
            // replace mets
            write(mets.asDocument(), derivateId);
        }

        // return json object
        JsonObject json = new JsonObject();
        json.addProperty("labelsUpdated", updatedList.size());
        json.addProperty("structLinkSynced", structLinkSynced);
        if (!errorMap.isEmpty()) {
            JsonArray errors = new JsonArray();
            json.add("errors", errors);
            errorMap.forEach((div, reason) -> {
                JsonObject error = new JsonObject();
                error.addProperty("id", div.getId());
                error.addProperty("reason", reason);
                errors.add(error);
            });
        }
        return Response.ok().entity(json.toString()).build();
    }

    /**
     * Stores the old mets.xml using the {@link MetsVersionStore}.
     * 
     * @param derivateId the derivate
     */
    private void storeOldMets(String derivateId) {
        try {
            MetsVersionStore.store(MCRObjectID.getInstance(derivateId));
        } catch (Exception exc) {
            JsonObject json = new JsonObject();
            json.addProperty("errorMsg", "unable to store old mets.xml: " + exc.getMessage());
            throw new WebApplicationException(exc, Response.ok().entity(json.toString()).build());
        }
    }

    /**
     * Synchronizes the struct link section of the given mets.
     * 
     * @param mets the mets to sync
     * @return true if the struct link section of the mets changed, otherwise false
     */
    private boolean syncStructLink(Mets mets) {
        try {
            StructLink oldStructLink = mets.getStructLink();
            StructLink newStructLink = new StructLinkGenerator().generate(mets);
            if (!oldStructLink.equals(newStructLink)) {
                mets.setStructLink(newStructLink);
                return true;
            }
            return false;
        } catch (Exception exc) {
            JsonObject json = new JsonObject();
            json.addProperty("errorMsg", "unable to generate struct link: " + exc.getMessage());
            throw new WebApplicationException(exc, Response.ok().entity(json.toString()).build());
        }
    }

    /**
     * Returns the mets.xml of the given derivate. If there is no mets.xml or an exception occur
     * while getting, a web application exception is thrown.
     * 
     * @param derivateId the derivate
     * @return the mets as java object
     */
    private Mets getMets(String derivateId) {
        try {
            Document metsXML = MetsUtil.getMetsXMLasDocument(derivateId);
            Mets mets = new Mets(metsXML);
            return mets;
        } catch (Exception exc) {
            JsonObject json = new JsonObject();
            json.addProperty("errorMsg", "mets.xml not found");
            throw new WebApplicationException(exc, Response.ok().entity(json.toString()).build());
        }
    }

    /**
     * Returns a list of all logical div's of the given mets.
     * 
     * @param mets the mets
     * @return a list of logical div's
     */
    private List<LogicalDiv> getLogicalDivs(Mets mets) {
        LogicalStructMap logicalStructMap = (LogicalStructMap) mets.getStructMap(LogicalStructMap.TYPE);
        if (logicalStructMap == null) {
            JsonObject json = new JsonObject();
            json.addProperty("errorMsg", "The mets.xml does not contain any logical struct map.");
            throw new WebApplicationException(Response.ok().entity(json.toString()).build());
        }
        LogicalDiv divContainer = logicalStructMap.getDivContainer();
        if (divContainer == null) {
            JsonObject json = new JsonObject();
            json.addProperty("errorMsg", "The logical struct map of the mets.xml does not contain any logical div.");
            throw new WebApplicationException(Response.ok().entity(json.toString()).build());
        }
        return MCRStreamUtils.flatten(divContainer, LogicalDiv::getChildren, Collection::parallelStream)
                             .collect(Collectors.toList());
    }

    /**
     * Runs through all logical divs and tries to update them. If their ID attribute
     * is an mycore object id, then the corresponding mycore object is retrieved. The title
     * of this mycore object will be written into the LABEL attribute of the logical div.
     * 
     * @param logicalDivList a list of logical divs
     * @param updateList an empty list where the updated divs are stored
     * @param errorMap an empty map where the errors are stored (div, reason)
     */
    private void updateMaintitlesOfLogicalDivs(List<LogicalDiv> logicalDivList, List<LogicalDiv> updateList,
        Map<LogicalDiv, String> errorMap) {
        for (LogicalDiv div : logicalDivList) {
            String divId = div.getId();
            try {
                if (!MCRObjectID.isValid(divId)) {
                    continue;
                }
                MCRObjectID mcrId = MCRObjectID.getInstance(divId);
                if (!MCRMetadataManager.exists(mcrId)) {
                    errorMap.put(div, "MyCoRe object does not exist " + divId);
                    LOGGER.warn("LogicalStructMap @ID '" + mcrId + "' does not exists on system.");
                    continue;
                }
                Optional<String> maintitle = JPComponentUtil.getPeriodical(mcrId).map(JPPeriodicalComponent::getTitle);
                if (maintitle.isPresent() && !Objects.equals(maintitle.get(), div.getLabel())) {
                    div.setLabel(maintitle.get());
                    updateList.add(div);
                }
            } catch (Exception exc) {
                errorMap.put(div, "Unknown error for " + divId);
                LOGGER.error("Unable to get maintitle of " + divId, exc);
            }
        }
    }

    /**
     * Writes the document as /mets.xml to the derivate.
     * 
     * @param doc the mets.xml to store
     * @param derivateId the derivate
     * @throws IOException something went wrong while writing
     */
    private void write(Document doc, String derivateId) {
        try {
            byte[] bytes = new MCRJDOMContent(doc).asByteArray();
            MCRPath path = MCRPath.getPath(derivateId, "/mets.xml");
            Files.write(path, bytes);
        } catch (Exception exc) {
            JsonObject json = new JsonObject();
            json.addProperty("errorMsg", "Unable to store mets.xml of derivate " + derivateId);
            throw new WebApplicationException(Response.ok().entity(json.toString()).build());
        }
    }

}
