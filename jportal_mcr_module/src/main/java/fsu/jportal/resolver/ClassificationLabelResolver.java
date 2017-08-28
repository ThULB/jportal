package fsu.jportal.resolver;

import fsu.jportal.util.ResolverUtil;
import org.jdom2.Element;
import org.jdom2.transform.JDOMSource;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

@URIResolverSchema(schema = "xClassificationLabel")
public class ClassificationLabelResolver implements URIResolver {

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        String[] uriParts = href.split(":");
        if (uriParts.length < 3) {
            throw new IllegalArgumentException("Invalid format of uri given to resolve: " + href);
        }
        String journalID = uriParts[1];
        String xpath = uriParts[2];
        String classID = ClassificationResolver.getClassificationID(journalID, xpath);
        String label = ResolverUtil.getClassLabel(classID).orElse("undefined");
        return new JDOMSource(new Element("dummyRoot").addContent(new Element("label").setText(label)));
    }

}
