package org.mycore.common.xml;

import static org.mycore.common.MCRConstants.XLINK_NAMESPACE;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

public class MCRJPortalURIGetParentID implements MCRURIResolver.MCRResolver {

    private static final Logger LOGGER = Logger.getLogger(MCRJPortalURIGetParentID.class);

    @Override
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
    
}
