package fsu.jportal.resolver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.transform.JDOMSource;
import org.mycore.common.xml.MCRURIResolver;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

@URIResolverSchema(schema = "xClassification")
public class ClassificationResolver implements URIResolver {

    private static final Logger LOGGER = LogManager.getLogger(ClassificationResolver.class);

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        String[] uriParts = href.split(":");
        if (uriParts.length < 3) {
            throw new IllegalArgumentException("Invalid format of uri given to resolve: " + href);
        }
        String journalID = uriParts[1];
        String xpath = uriParts[2];
        String classID = getClassificationID(journalID, xpath);
        if (classID == null) {
            // TODO: return empty list
            return new JDOMSource(new Document());
        }
        String classURI = "xslStyle:items2options:classification:editor:-1:children:" + classID;
        Element classElement = MCRURIResolver.instance().resolve(classURI);
        return new JDOMSource(classElement);
    }

    public static String getClassificationID(String journalID, String xpath) {
        try {
            Document journalXML = MCRXMLMetadataManager.instance().retrieveXML(MCRObjectID.getInstance(journalID));
            int sepPos = xpath.indexOf("/");
            String tag1 = xpath.substring(0, sepPos);
            String tag2 = xpath.substring(sepPos + 1, xpath.length());
            Element classID_root = journalXML.getRootElement().getChild("metadata").getChild(tag1);
            if (classID_root != null && classID_root.getChild(tag2) != null) {
                return classID_root.getChild(tag2).getTextTrim();
            }
        } catch (Exception exc) {
            LOGGER.error("Unable to get classification id by xpath " + xpath + " of journal " + journalID, exc);
        }
        return null;
    }

}
