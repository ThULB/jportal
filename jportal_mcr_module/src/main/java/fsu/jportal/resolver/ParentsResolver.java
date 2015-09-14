package fsu.jportal.resolver;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.transform.JDOMSource;
import org.mycore.common.MCRConstants;

import fsu.jportal.annotation.URIResolverSchema;
import fsu.jportal.util.ContentTools;
import fsu.jportal.xml.ParentsListXML;

@URIResolverSchema(schema = "parents")
public class ParentsResolver implements URIResolver {

    private static final Logger LOGGER = LogManager.getLogger(ParentsResolver.class);

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        String childID = href.replaceAll("parents:", "");
        try {
            Element parents = new ContentTools().getParents(childID, new ParentsListXML());
            return new JDOMSource(parents);
        } catch (Exception exc) {
            LOGGER.error("unable to retrieve parents of mcr object " + childID);
            Element parent = new Element("parent").setAttribute("error", "Error: unable to load parent").setAttribute("href", "unknown",
                    MCRConstants.XLINK_NAMESPACE);
            return new JDOMSource(new Element("parents").addContent(parent));
        }
    }

}
