package fsu.jportal.resources;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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
import org.mycore.mets.misc.AreaStructLinkGenerator;
import org.mycore.mets.model.Mets;
import org.mycore.mets.model.struct.IDiv;
import org.mycore.mets.model.struct.LogicalDiv;
import org.mycore.mets.model.struct.LogicalStructMap;
import org.mycore.mets.model.struct.PhysicalStructMap;
import org.mycore.mets.model.struct.PhysicalSubDiv;
import org.mycore.mets.model.struct.SmLink;
import org.mycore.mets.model.struct.StructLink;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import fsu.jportal.backend.JPArticle;
import fsu.jportal.backend.JPPeriodicalComponent;
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
     * @param derivateId where to generate the mets.xml
     * @return the json response object {status = 'ok'}
     */
    @Path("generate/{id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response generate(@PathParam("id") String derivateId) {
        // check write permission on derivate
        MCRObjectID derId = MCRObjectID.getInstance(derivateId);
        MCRJerseyUtil.checkPermission(derId, MCRAccessManager.PERMISSION_WRITE);
        try {
            if (!MetsUtil.isGeneratable(derId)) {
                throw new WebApplicationException("Unable to generate mets.xml cause derivate or object does not exists"
                        + " or the derivate does not contain any image.");
            }
            MetsUtil.generateAndReplace(derId);
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
        MCRObjectID mcrDerivateId = MCRObjectID.getInstance(derivateId);
        MCRJerseyUtil.checkPermission(mcrDerivateId, MCRAccessManager.PERMISSION_WRITE);
        // get mets
        Mets mets = getMets(derivateId);

        // do the sync
        List<IDiv> updatedList = new ArrayList<>();
        Map<IDiv, String> errorMap = new HashMap<>();
        updateLogicalDivs(mets, updatedList, errorMap);
        updatePhysicalDivs(mets, updatedList, errorMap);

        boolean structLinkSynced = syncStructLink(mets);

        if (structLinkSynced || !updatedList.isEmpty()) {
            // store old mets
            storeOldMets(mcrDerivateId);
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
    private void storeOldMets(MCRObjectID derivateId) {
        try {
            MetsVersionStore.store(derivateId);
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
            StructLink newStructLink = new AreaStructLinkGenerator().generate(mets.getPhysicalStructMap(),
                mets.getLogicalStructMap());
            if (!oldStructLink.equals(newStructLink)) {
                mets.setStructLink(newStructLink);
                return true;
            }
        } catch (Exception exc) {
            LOGGER.warn("Unable to create or sync the struct link, leave the old one..., exc");
        }
        return false;
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
            return new Mets(metsXML);
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
     * @param mets the mets object
     * @param updateList an empty list where the updated divs are stored
     * @param errorMap an empty map where the errors are stored (div, reason)
     */
    private void updateLogicalDivs(Mets mets, List<IDiv> updateList, Map<IDiv, String> errorMap) {
        List<LogicalDiv> logicalDivList = getLogicalDivs(mets);

        for (LogicalDiv div : logicalDivList) {
            String divId = div.getId();
            try {
                if (!MCRObjectID.isValid(divId)) {
                    continue;
                }
                MCRObjectID mcrId = MCRObjectID.getInstance(divId);
                if (!MCRMetadataManager.exists(mcrId)) {
                    errorMap.put(div, "MyCoRe object does not exist " + divId);
                    LOGGER.warn("LogicalStructMap @ID '{}' does not exists on system.", mcrId);
                    continue;
                }
                if (updateMaintitle(div, mcrId)) {
                    updateList.add(div);
                }
            } catch (Exception exc) {
                errorMap.put(div, "Unknown error for " + divId);
                LOGGER.error("Unable to get maintitle of " + divId, exc);
            }
        }
    }

    private void updatePhysicalDivs(Mets mets, List<IDiv> updateList, Map<IDiv, String> errorMap) {
        PhysicalStructMap physicalStructMap = mets.getPhysicalStructMap();
        boolean sizeUpdated = false;
        for (PhysicalSubDiv physicalDiv : physicalStructMap.getDivContainer().getChildren()) {
            try {
                List<SmLink> links = mets.getStructLink().getSmLinkByTo(physicalDiv.getId());
                if (links.isEmpty()) {
                    continue;
                }
                SmLink smLink = links.get(0);
                String from = smLink.getFrom();
                if (!MCRObjectID.isValid(from)) {
                    continue;
                }
                MCRObjectID mcrId = MCRObjectID.getInstance(from);
                if (updateSize(physicalDiv, mcrId)) {
                    updateList.add(physicalDiv);
                    sizeUpdated = true;
                }
            } catch (Exception exc) {
                errorMap.put(physicalDiv, "Unknown error for " + physicalDiv.getId());
                LOGGER.error("Unable to get size of " + physicalDiv.getId(), exc);
            }
        }
        if (sizeUpdated) {
            MetsUtil.interpolateOrderLabels(mets);
        }
    }

    private boolean updateMaintitle(LogicalDiv logicalDiv, MCRObjectID mcrId) {
        Optional<String> maintitle = JPComponentUtil.getPeriodical(mcrId).map(JPPeriodicalComponent::getTitle);
        if (maintitle.isPresent() && !Objects.equals(maintitle.get(), logicalDiv.getLabel())) {
            logicalDiv.setLabel(maintitle.get());
            return true;
        }
        return false;
    }

    private boolean updateSize(PhysicalSubDiv physicalDiv, MCRObjectID mcrId) {
        if (!mcrId.getTypeId().equals("jparticle")) {
            return false;
        }
        Optional<String> size = new JPArticle(mcrId).getSize();
        return size.filter(s -> MetsUtil.setOrderLabel(physicalDiv, s, true)).isPresent();
    }

    /**
     * Writes the document as /mets.xml to the derivate.
     *
     * @param doc the mets.xml to store
     * @param derivateId the derivate
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
