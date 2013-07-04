package fsu.jportal.resolver;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.apache.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.transform.JDOMSource;
import org.mycore.common.MCRConstants;

import fsu.jportal.xml.XMLContentTools;

public class ParentsResolver implements URIResolver {

    private static final Logger LOGGER = Logger.getLogger(ParentsResolver.class);

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        String childID = href.replaceAll("parents:", "");
        try {
            Element parents = new XMLContentTools().getParents(childID);
            return new JDOMSource(parents);
        } catch (Exception exc) {
            LOGGER.error("unable to retrieve parents of child " + childID);
            Element parent = new Element("parent").setAttribute("error", "Error: unable to load parent").setAttribute("href", "unknown",
                    MCRConstants.XLINK_NAMESPACE);
            return new JDOMSource(new Element("parents").addContent(parent));
        }
    }

}
