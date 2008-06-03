package org.mycore.common.xml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.Namespace;
import org.mycore.common.MCRSessionMgr;
import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryDAO;
import org.mycore.datamodel.classifications2.MCRCategoryDAOFactory;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.classifications2.impl.MCRCategoryDAOImpl;
import org.mycore.datamodel.common.MCRXMLTableManager;
import org.mycore.datamodel.metadata.MCRObjectID;

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
        String currentLang = MCRSessionMgr.getCurrentSession().getCurrentLanguage();
        MCRCategoryDAO categDoa = MCRCategoryDAOFactory.getInstance();
        Iterator<MCRCategoryID> ci = categDoa.getRootCategoryIDs().iterator();
        while (ci.hasNext()) {
            MCRCategoryID cid = (MCRCategoryID) ci.next();
            String classID = cid.getRootID();
            String descr = categDoa.getRootCategory(cid, 0).getLabels().get(currentLang).getText();
            Element item = new Element("item").setAttribute("value", classID);
            Element label = new Element("label").setAttribute("lang", "de", Namespace.XML_NAMESPACE);
            label.setText(descr + " (" + classID + ")");
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
