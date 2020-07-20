package fsu.jportal.mets;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.jdom2.Document;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.niofs.MCRContentTypes;
import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.mets.model.MCRMETSGenerator;
import org.mycore.mets.model.Mets;

import fsu.jportal.backend.JPDerivateComponent;
import fsu.jportal.backend.JPJournal;
import fsu.jportal.backend.JPObjectType;
import fsu.jportal.backend.JPVolume;
import fsu.jportal.backend.impl.JPVolumeTypeDefaultDetector;
import fsu.jportal.util.JPComponentUtil;

/**
 * Created by chi on 20.07.20
 *
 * @author Huu Chi Vu
 */
public class ZVDDMetsGenerator {
    public Document generateMets(String id) throws Exception {
        if (!MCRObjectID.isValid(id)) {
            throw new Exception(id + " is not a valid mycore identifier.");
        }
        MCRObjectID mcrObjectID = MCRObjectID.getInstance(id);
        if (!(JPComponentUtil.is(mcrObjectID, JPObjectType.jpvolume)
                || JPComponentUtil.is(mcrObjectID, JPObjectType.jpjournal))) {
            throw new Exception("Invalid identifier " + id + ". Type has to be jpvolume or jpjournal.");
        }

        Document metsXML;
        if (JPComponentUtil.is(mcrObjectID, JPObjectType.jpjournal)) {
            metsXML = handleJournal(mcrObjectID);
        } else {
            JPVolume volume = new JPVolume(mcrObjectID);
            JPJournal journal = volume.getJournal();
            if (journal.isJournalType("jportal_class_00000200", "newspapers")) {
                metsXML = handleNewspaperVolume(volume);
            } else {
                metsXML = handleMagazineVolume(volume);
            }
        }
        return metsXML;
    }

    private Document getMetsXML(MCRMETSGenerator metsGenerator) {
        Mets mets = metsGenerator.generate();
        return mets.asDocument();
    }

    public Document handleJournal(MCRObjectID mcrObjectID) {
        JPJournal journal = new JPJournal(mcrObjectID);
        ZvddJournalMetsGenerator journalMetsGenerator = new ZvddJournalMetsGenerator(journal);
        return getMetsXML(journalMetsGenerator);
    }

    private Document handleMagazineVolume(JPVolume volume) throws Exception {
//        return findDerivate(volume)
//                .map(JPDerivateComponent::getId)
//                .map(MCRObjectID::toString)
//                .map(ZvddDerivateMetsGenerator::new)
//                .map(this::getResponse)
//                .orElseThrow(() ->
//                        new BadRequestException("Object " + volume.getId() + " does not contain a fitting derivate."));


        Optional<JPDerivateComponent> derivateOptional = findDerivate(volume);
        if (!derivateOptional.isPresent()) {
            throw new Exception("Object " + volume.getId() + " does not contain a fitting derivate.");
        }
        ZvddDerivateMetsGenerator metsGenerator = new ZvddDerivateMetsGenerator();
        metsGenerator.setup(derivateOptional.get().getId().toString());
        return getMetsXML(metsGenerator);
    }

    private Document handleNewspaperVolume(JPVolume volume) throws Exception {
        List<String> types = volume.getVolumeTypes();
        boolean isIssue = types.contains(JPVolumeTypeDefaultDetector.JPVolumeType.issue.name());
        if (isIssue) {
            return getMetsXML(new ZvddNewspaperIssueMetsGenerator(volume));
        }
        boolean isYear = types.contains(JPVolumeTypeDefaultDetector.JPVolumeType.year.name());
        if (isYear) {
            return getMetsXML(new ZvddNewspaperYearMetsGenerator(volume));
        }
        throw new Exception(
                "Requesting invalid object '" + volume.getId() + "'. The type of the requested newspaper volume is"
                        + "'" + types + "'. But only 'issue' and 'year' are supported.");
    }

    /**
     * Goes through all derivates in the given volume and finds the one where the main document is an image. This avoids
     * using a derivate which just have a pdf.
     *
     * @param volume the volume to check
     * @return an optional derivate
     */
    private Optional<JPDerivateComponent> findDerivate(JPVolume volume) {
        List<JPDerivateComponent> derivates = volume.getDerivates();
        for (JPDerivateComponent derivate : derivates) {
            MCRPath mainDocPath = MCRPath.toMCRPath(derivate.getPath().resolve(derivate.getMainDoc()));
            try {
                String contentType = MCRContentTypes.probeContentType(mainDocPath);
                if (contentType.startsWith("image/")) {
                    return Optional.of(derivate);
                }
            } catch (IOException ioException) {
                LogManager.getLogger().error("Unable to probe content type of " + mainDocPath, ioException);
            }
        }
        return Optional.empty();
    }
}
