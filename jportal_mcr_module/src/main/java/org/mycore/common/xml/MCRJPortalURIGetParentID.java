package org.mycore.common.xml;

import static org.mycore.common.MCRConstants.XLINK_NAMESPACE;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.apache.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;
import org.jdom2.transform.JDOMSource;

public class MCRJPortalURIGetParentID implements URIResolver {

    private static final Logger LOGGER = Logger.getLogger(MCRJPortalURIGetParentID.class);

    public Element resolveElement(String URI) throws Exception {
        String parentID = URI.substring(URI.indexOf(":") + 1);
        LOGGER.debug("ParentID used to get editor template: " + parentID);
        Element mycoreobject = new Element("mycoreobject");
        Element parent = new Element("parent").setAttribute("href", parentID, XLINK_NAMESPACE);
        mycoreobject.addContent(new Element("structure").addContent(new Element("parents").addContent(parent)));
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(new XMLOutputter().outputString(mycoreobject));
        }
        return mycoreobject;
    }

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        try {
            return new JDOMSource(resolveElement(href));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return null;
    }
    
}
