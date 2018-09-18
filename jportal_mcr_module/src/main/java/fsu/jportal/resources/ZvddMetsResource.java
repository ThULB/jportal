package fsu.jportal.resources;

import java.util.Optional;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.mets.model.MCRMETSGenerator;
import org.mycore.mets.model.Mets;

import fsu.jportal.backend.JPDerivateComponent;
import fsu.jportal.backend.JPJournal;
import fsu.jportal.backend.JPObjectType;
import fsu.jportal.backend.JPVolume;
import fsu.jportal.backend.impl.JPVolumeTypeDefaultDetector;
import fsu.jportal.mets.ZvddDerivateMetsGenerator;
import fsu.jportal.mets.ZvddJournalMetsGenerator;
import fsu.jportal.mets.ZvddNewspaperIssueMetsGenerator;
import fsu.jportal.mets.ZvddNewspaperYearMetsGenerator;
import fsu.jportal.util.JPComponentUtil;

@Path("mets/zvdd")
public class ZvddMetsResource {

    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("{id}")
    public Response get(@PathParam("id") String id) {
        if (!MCRObjectID.isValid(id)) {
            throw new BadRequestException(id + " is not a valid mycore identifier.");
        }
        MCRObjectID mcrObjectID = MCRObjectID.getInstance(id);
        if (!(JPComponentUtil.is(mcrObjectID, JPObjectType.jpvolume)
            || JPComponentUtil.is(mcrObjectID, JPObjectType.jpjournal))) {
            throw new BadRequestException(
                "Invalid identifier " + id + ". Type has to be jpvolume or jpjournal.");
        }
        if (JPComponentUtil.is(mcrObjectID, JPObjectType.jpjournal)) {
            return handleJournal(mcrObjectID);
        }
        JPVolume volume = new JPVolume(mcrObjectID);
        JPJournal journal = volume.getJournal();
        if (journal.isJournalType("jportal_class_00000200", "newspapers")) {
            return handleNewspaperVolume(volume);
        } else {
            return handleMagazineVolume(volume);
        }
    }

    private Response handleJournal(MCRObjectID mcrObjectID) {
        JPJournal journal = new JPJournal(mcrObjectID);
        ZvddJournalMetsGenerator journalMetsGenerator = new ZvddJournalMetsGenerator(journal);
        return getResponse(journalMetsGenerator);
    }

    private Response handleNewspaperVolume(JPVolume volume) {
        String type = volume.getVolumeType();
        boolean isIssue = type.equals(JPVolumeTypeDefaultDetector.JPVolumeType.issue.name());
        if (isIssue) {
            return getResponse(new ZvddNewspaperIssueMetsGenerator(volume));
        }
        boolean isYear = type.equals(JPVolumeTypeDefaultDetector.JPVolumeType.year.name());
        if (isYear) {
            return getResponse(new ZvddNewspaperYearMetsGenerator(volume));
        }
        throw new BadRequestException(
            "Requesting invalid object '" + volume.getId() + "'. The type of the requested newspaper volume is"
                + "'" + type + "'. But only 'issue' and 'year' are supported.");
    }

    private Response handleMagazineVolume(JPVolume volume) {
        Optional<JPDerivateComponent> derivateOptional = volume.getFirstDerivate();
        if (!derivateOptional.isPresent()) {
            throw new BadRequestException("Object " + volume.getId() + " does not contain any derivates.");
        }
        ZvddDerivateMetsGenerator metsGenerator = new ZvddDerivateMetsGenerator();
        metsGenerator.setup(derivateOptional.get().getId().toString());
        return getResponse(metsGenerator);
    }

    private Response getResponse(MCRMETSGenerator metsGenerator) {
        Mets mets = metsGenerator.generate();
        Document xml = mets.asDocument();
        XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
        return Response.ok(out.outputString(xml), MediaType.APPLICATION_XML).build();
    }

}
