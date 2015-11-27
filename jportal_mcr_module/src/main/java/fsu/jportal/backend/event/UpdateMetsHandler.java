package fsu.jportal.backend.event;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.events.MCREvent;
import org.mycore.common.events.MCREventHandlerBase;
import org.mycore.datamodel.metadata.MCRMetaLinkID;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.metadata.MCRObjectUtils;
import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.mets.model.Mets;
import org.mycore.mets.model.struct.LogicalDiv;
import org.mycore.mets.model.struct.LogicalStructMap;

import fsu.jportal.mets.LLZMetsUtils;
import fsu.jportal.util.DerivateLinkUtil;
import fsu.jportal.util.JPComponentUtil;

/**
 * Updates the label of the corresponding logical div of a mets file when
 * the title of an article, volume or journal is changed.
 * 
 * @author Matthias Eichner
 */
public class UpdateMetsHandler extends MCREventHandlerBase {

    private static Logger LOGGER = LogManager.getLogger(UpdateMetsHandler.class);

    @Override
    protected void handleObjectUpdated(MCREvent evt, MCRObject obj) {
        MCRObjectID mcrId = obj.getId();
        String type = mcrId.getTypeId();
        // apply only for journal, volume and article
        if (!(type.equals("jpjournal") || type.equals("jparticle") || type.equals("jpvolume"))) {
            return;
        }

        // get title
        Optional<String> maintitle = JPComponentUtil.getMaintitle(mcrId.toString());
        if (!maintitle.isPresent()) {
            LOGGER.error("Unable to get maintitle of object " + mcrId);
            return;
        }

        // fetch all derivates
        List<String> derivateLinks = getDerivateLinks(obj);

        // run through derivates
        for (String derivateID : derivateLinks) {
            try {
                Document metsXML = LLZMetsUtils.getMetsXMLasDocument(derivateID);
                Optional<Document> newMetsXML = updateMets(metsXML, mcrId.toString(), maintitle.get());
                if (newMetsXML.isPresent()) {
                    write(newMetsXML.get(), derivateID);
                }
            } catch (FileNotFoundException fnfe) {
                continue;
            } catch (Exception exc) {
                LOGGER.error("while parsing or writing mets.xml of derivate " + derivateID, exc);
            }
        }
    }

    /**
     * Returns a list of derivates which are in relation to the given object. This includes
     * all derivates of the structure part and all derivate links.
     * 
     * @param obj mycore object
     * @return list of derivate id's
     */
    private List<String> getDerivateLinks(MCRObject obj) {
        // first derivate links
        List<String> links = DerivateLinkUtil.getLinks(obj);
        if (!links.isEmpty()) {
            return links;
        }
        // run through the derivates of ancestors and self
        List<MCRObject> ancestorsAndSelf = MCRObjectUtils.getAncestorsAndSelf(obj);
        // return list of all derivate Links
        return StreamSupport.stream(ancestorsAndSelf.spliterator(), false)
            .flatMap(o -> o.getStructure().getDerivates().stream()).map(MCRMetaLinkID::getXLinkHref).distinct()
            .collect(Collectors.toList());
    }

    /**
     * Look up for a logical div with the given <code>mcrId</code>. If a div is present
     * the label is changed to <code>newTitle</code>. Returns true if something is
     * changed, otherwise false.
     * 
     * @param metsXML mets xml to change
     * @param mcrId lookup id in logical div's
     * @param newTitle the new title
     * @return the new document
     */
    private Optional<Document> updateMets(Document metsXML, String mcrId, String newTitle) {
        Mets mets = new Mets(metsXML);
        LogicalStructMap logicalStructMap = (LogicalStructMap) mets.getStructMap(LogicalStructMap.TYPE);
        if (logicalStructMap == null) {
            return Optional.empty();
        }
        LogicalDiv rootDiv = logicalStructMap.getDivContainer();
        if (rootDiv == null) {
            return Optional.empty();
        }
        LogicalDiv div = rootDiv.getLogicalSubDiv(mcrId);
        if(div == null) {
            return Optional.empty();
        }
        if (newTitle.equals(div.getLabel())) {
            return Optional.empty();
        }
        div.setLabel(newTitle);
        return Optional.of(mets.asDocument());
    }

    /**
     * Writes the document as /mets.xml to the derivate.
     * 
     * @param doc the mets.xml to store
     * @param derivateId the derivate
     * @throws IOException something went wrong while writing
     */
    private void write(Document doc, String derivateId) throws IOException {
        byte[] bytes = new MCRJDOMContent(doc).asByteArray();
        MCRPath path = MCRPath.getPath(derivateId, "/mets.xml");
        Files.write(path, bytes);
    }

}
