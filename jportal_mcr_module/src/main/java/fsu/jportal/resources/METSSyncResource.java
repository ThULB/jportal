package fsu.jportal.resources;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
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

import org.jdom2.Document;
import org.mycore.common.MCRStreamUtils;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.frontend.jersey.MCRJerseyUtil;
import org.mycore.mets.model.Mets;
import org.mycore.mets.model.struct.LogicalDiv;
import org.mycore.mets.model.struct.LogicalStructMap;

import com.google.gson.JsonObject;

import fsu.jportal.backend.JPPeriodicalComponent;
import fsu.jportal.mets.LLZMetsUtils;
import fsu.jportal.util.JPComponentUtil;

/**
 * The mets sync resource tries to synchronize the mets.xml of a derivate with the
 * jportal object structure. When the ID's of the logical div's are valid mycore
 * object identifiers, then this resource tries to update the LABEL attribute's of
 * those divs. The maintitle of the objects will be in sync with the LABEL's of the
 * mets.xml.
 * 
 * @author Matthias Eichner
 */
@Path("mets/sync")
public class METSSyncResource {

    /**
     * Syncs the mets.xml with the object structure.
     * Returns a json object containg the number of changed logical div's.
     * 
     * @param derivateId the derivate
     * @return json object
     */
    @Path("{id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response sync(@PathParam("id") String derivateId) {
        // check write permission on derivate
        MCRJerseyUtil.checkPermission(MCRObjectID.getInstance(derivateId), "write");
        // get mets
        Mets mets = getMets(derivateId);
        // get all logical divs
        List<LogicalDiv> logicalDivList = getLogicalDivs(mets);

        // update mets
        List<LogicalDiv> updatedList = updateMaintitlesOfLogicalDivs(logicalDivList);

        // write mets
        if (!updatedList.isEmpty()) {
            write(mets.asDocument(), derivateId);
        }
        // return json object
        JsonObject json = new JsonObject();
        json.addProperty("updated", updatedList.size());
        return Response.ok().entity(json.toString()).build();
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
            Document metsXML = LLZMetsUtils.getMetsXMLasDocument(derivateId);
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
        return MCRStreamUtils.flatten(divContainer, LogicalDiv::getChildren, true).collect(Collectors.toList());
    }

    /**
     * Runs through all logical divs and tries to update them. If their ID attribute
     * is an mycore object id, then the corresponding mycore object is retrieved. The title
     * of this mycore object will be written into the LABEL attribute of the logical div.
     * 
     * @param logicalDivList a list of logical divs
     * @return a list of all logical divs which have an updated maintitle
     */
    private List<LogicalDiv> updateMaintitlesOfLogicalDivs(List<LogicalDiv> logicalDivList) {
        List<LogicalDiv> updateList = new ArrayList<>();
        for (LogicalDiv div : logicalDivList) {
            try {
                MCRObjectID mcrId = MCRObjectID.getInstance(div.getId());
                Optional<String> maintitle = JPComponentUtil.getPeriodical(mcrId).map(JPPeriodicalComponent::getTitle);
                if (maintitle.isPresent() && !Objects.equals(maintitle.get(), div.getLabel())) {
                    div.setLabel(maintitle.get());
                    updateList.add(div);
                }
            } catch (Exception exc) {
                continue;
            }
        }
        return updateList;
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
