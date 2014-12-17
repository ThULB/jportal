package fsu.jportal.resolver.xeditor;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.transform.JDOMSource;
import org.mycore.common.xml.MCRJPortalURIGetClassID;
import org.mycore.common.xml.MCRURIResolver;

public class ClassificationResolver implements URIResolver {

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        String[] uriParts = href.split(":");
        if (uriParts.length < 3) {
            throw new IllegalArgumentException("Invalid format of uri given to resolve: " + href);
        }
        String journalID = uriParts[1];
        String xpath = uriParts[2];
        String classID = MCRJPortalURIGetClassID.getClassID(journalID, xpath);
        if (classID == null) {
            // TODO: return empty list
            return new JDOMSource(new Document());
        }
        String classURI = "xslStyle:items2options:classification:editor:-1:children:" + classID;
        Element classElement = MCRURIResolver.instance().resolve(classURI);
        return new JDOMSource(classElement);
    }

}
