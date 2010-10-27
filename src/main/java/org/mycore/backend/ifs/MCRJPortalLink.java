/**
 * 
 */
package org.mycore.backend.ifs;

import java.io.IOException;
import java.util.Date;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.xpath.XPath;
import org.mycore.common.MCRPersistenceException;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;

/**
 * @author Andreas Trappe
 * 
 */
public class MCRJPortalLink {

    private static Logger LOGGER = Logger.getLogger(MCRJPortalLink.class);

    private MCRObjectID from;

    private String to;

    private static final String XML_LOCATION_OF_LINKS = "/mycoreobject/metadata/ifsLinks";

    private static final String XML_TAG_FOR_LINKS = "ifsLink";

    private static final String XPATH_TO_LINKS_COMPLETE = XML_LOCATION_OF_LINKS + "/" + XML_TAG_FOR_LINKS;

    /**
     * @param from,
     *            ID of Mycore-Object in which the link should be added.
     * @param to,
     *            Absolute path of a MCRFile, where the link should point to.
     */
    public MCRJPortalLink(MCRObjectID from, String to) {
        this.from = from;
        this.to = to;
    }

    /**
     * Removes a link from a Mycore-Object
     * 
     * @throws JDOMException
     * @throws MCRActiveLinkException
     * @throws IOException
     */
    public void remove() throws JDOMException, MCRActiveLinkException, IOException {

        // get link
        Document sourceObject = getFromObject();
        String xpathOfLinks = XPATH_TO_LINKS_COMPLETE + "[text()='" + to + "']";
        XPath xpath = XPath.newInstance(xpathOfLinks);
        Element link = (Element) xpath.selectSingleNode(sourceObject);

        // remove if exist
        if (null != link) {
            boolean lastLink = (XPath.newInstance("count(" + XPATH_TO_LINKS_COMPLETE + ")").numberValueOf(sourceObject).intValue() == 1);
            if (lastLink) {
                ((Element) XPath.newInstance(XML_LOCATION_OF_LINKS).selectSingleNode(sourceObject)).detach();
                LOGGER.debug("link " + to + " removed from object " + from + ", no more links left.");
            } else {
                link.detach();
                LOGGER.debug("link " + to + " removed from object " + from);
            }
            // save object
            saveObject(sourceObject);
        }
    }

    /**
     * Creates a link in a Mycore-Object
     * 
     * @throws IOException
     * @throws MCRPersistenceException
     * @throws MCRActiveLinkException
     */
    public void set() throws IOException, MCRPersistenceException, MCRActiveLinkException {

        // create xml containing link
        Element link = new Element("ifsLink");
        link.setAttribute("lang", "de", Namespace.XML_NAMESPACE);
        link.setText(to);

        // update object xml
        Document objectXML = getFromObject();
        boolean alreadyHasLink = false;
        if (null != objectXML.getRootElement().getChild("metadata").getChild("ifsLinks"))
            alreadyHasLink = true;
        if (alreadyHasLink) {
            objectXML.getRootElement().getChild("metadata").getChild("ifsLinks").addContent(link);
        } else {
            Element linkWrappingTag = new Element("ifsLinks");
            linkWrappingTag.setAttribute("class", "MCRMetaLangText");
            linkWrappingTag.addContent(link);
            objectXML.getRootElement().getChild("metadata").addContent(linkWrappingTag);
        }
        
        // save object
        saveObject(objectXML);

        LOGGER.debug("link in object " + from + " set to " + to);
    }

    /**
     * Saves a Mycore-Object in form of a JDOM-Document in Mycore.
     * 
     * @param objectXML,
     *            Mycore-Object, to be saved
     * @throws MCRActiveLinkException
     */
    private void saveObject(Document objectXML) throws MCRActiveLinkException {
        MCRXMLMetadataManager.instance().update(from, objectXML, new Date());
    }

    /**
     * @return
     */
    private Document getFromObject() {
        Document objectXML = MCRXMLMetadataManager.instance().retrieveXML(from);
        return objectXML;
    }
}
