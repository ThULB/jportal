package fsu.jportal.resolver;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.jdom2.Document;
import org.jdom2.transform.JDOMSource;
import org.mycore.common.MCRObjectMerger;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;

import fsu.archiv.mycore.sru.impex.pica.model.PicaRecord;
import fsu.jportal.annotation.URIResolverSchema;
import fsu.jportal.util.GndUtil;

@URIResolverSchema(schema = "sru")
public class SRUResolver implements URIResolver {

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        String[] uriParts = href.split(":");
        if (uriParts.length != 3) {
            throw new IllegalArgumentException("Invalid format of uri given to resolve: " + href);
        }
        String id = uriParts[1];
        String gnd = uriParts[2];

        try {
            MCRObjectID mcrId = MCRObjectID.getInstance(id);
            MCRObject mcrObject = MCRMetadataManager.retrieveMCRObject(mcrId);

            // record
            PicaRecord picaRecord = GndUtil.retrieveFromSRU(gnd);
            Document mcrObjectXML = GndUtil.toMCRObjectDocument(picaRecord);
            MCRObject sruObject = new MCRObject(mcrObjectXML);

            // merge
            MCRObjectMerger merger = new MCRObjectMerger(mcrObject);
            merger.mergeMetadata(sruObject, true);

            return new JDOMSource(merger.get().createXML());
        } catch (Exception exc) {
            throw new TransformerException(
                "While retriving/merging data from sru interface with id " + id + " and gnd " + gnd, exc);
        }
    }

}
