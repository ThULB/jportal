package org.mycore.common.xml;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.mycore.common.MCRException;
import org.mycore.datamodel.classifications.MCRClassification;
import org.mycore.datamodel.classifications.MCRClassificationObject;
import org.mycore.datamodel.classifications.query.MCRClassificationQuery;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.metadata.MCRXMLTableManager;
import org.mycore.frontend.servlets.MCRClassificationBrowser;

public class MCRJPortalURIGetAllClassIDs implements MCRURIResolver.MCRResolver {

    private static final Logger LOGGER = Logger.getLogger(MCRJPortalURIGetAllClassIDs.class);

    private static String URI = "jportal_getALLClassIDs:";

    /**
     * 
     * Syntax: jportal_getAllClassIDs
     * 
     * @return <dummyRoot> <list type="dropdown"> <item value="{classification
     *         id}"> <label xml:lang="de">{classification description}</label>
     *         </item> </list> </dummyRoot>
     * 
     */
    public Element resolveElement(String uri) {
        LOGGER.debug("start resolving " + uri);

        if (!wellURI(uri))
            throw new IllegalArgumentException("Invalid format of uri given to resolve " + URI + "=" + uri);

        Element returnXML = new Element("dummyRoot");
        MCRXMLTableManager objectDB = MCRXMLTableManager.instance();
        ArrayList<String> ci = objectDB.retrieveAllIDs("class");
        Iterator<String> ciIt = ci.iterator();
        while (ciIt.hasNext()) {
            String classID = (String) ciIt.next();
            String descr = objectDB.readDocument(new MCRObjectID(classID)).getRootElement().getChild("label").getAttributeValue("description");
            Element item = new Element("item").setAttribute("value", classID);
            Element label = new Element("label").setAttribute("lang", "de", Namespace.XML_NAMESPACE);
            label.setText(classID + " (" + descr + ")");
            item.addContent(label);
            returnXML.addContent(item);
        }

        return returnXML;
    }

    private boolean wellURI(String uri) {
        if (uri.equals(URI))
            return true;
        else
            return false;
    }

}
