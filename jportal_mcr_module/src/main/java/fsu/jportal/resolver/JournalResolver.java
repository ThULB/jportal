package fsu.jportal.resolver;

import org.jdom2.transform.JDOMSource;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.metadata.MCRObjectUtils;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

/**
 * Gets the journal by any children id. For creating new objects the 'parent'
 * part id is used, for editing objects the 'id' part is used as a reference object. 
 * 
 * <p>
 * <b>getJournal:{id}:{parent}</b>
 * </p>
 */
@URIResolverSchema(schema = "xJournal")
public class JournalResolver implements URIResolver {

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        String[] uriParts = href.split(":");
        String objectId = uriParts.length >= 2 && !uriParts[1].equals("") ? uriParts[1] : null;
        String parentId = uriParts.length >= 3 && !uriParts[2].equals("") ? uriParts[2] : null;
        if (objectId == null && parentId == null) {
            throw new IllegalArgumentException("Cannot get journal id without any reference: " + href);
        }
        MCRObject refObject = MCRMetadataManager.retrieveMCRObject(MCRObjectID.getInstance(objectId != null ? objectId
            : parentId));
        MCRObject journal = MCRObjectUtils.getRoot(refObject);
        if (journal == null) {
            if (!refObject.getId().getTypeId().equals("jpjournal")) {
                throw new IllegalArgumentException("Cannot receive journal id when root is null: " + href);
            }
            journal = refObject;
        }
        return new JDOMSource(journal.createXML());
    }
}
