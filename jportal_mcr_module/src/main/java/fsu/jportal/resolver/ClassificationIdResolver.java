package fsu.jportal.resolver;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.jdom2.Element;
import org.jdom2.transform.JDOMSource;
import org.mycore.common.MCRException;

import fsu.jportal.annotation.URIResolverSchema;

@URIResolverSchema(schema = "xClassificationID")
public class ClassificationIdResolver implements URIResolver {

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        String[] uriParts = href.split(":");
        if (uriParts.length < 3) {
            throw new IllegalArgumentException("Invalid format of uri given to resolve: " + href);
        }
        String journalID = uriParts[1];
        String xpath = uriParts[2];
        String classID = ClassificationResolver.getClassificationID(journalID, xpath);
        if (classID == null) {
            throw new MCRException("Couldn't find classification id '" + xpath + "' in journal " + journalID);
        }
        return new JDOMSource(new Element("dummyRoot").addContent(new Element("input").setAttribute("type", "hidden")
            .setAttribute("value", classID)));
    }

}
