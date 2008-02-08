package org.mycore.common.xml;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.mycore.backend.hibernate.MCRTableGenerator;
import org.mycore.common.MCRCache;
import org.mycore.common.MCRConfiguration;
import org.mycore.common.MCRException;
import org.mycore.common.MCRSessionMgr;
import org.mycore.datamodel.classifications.MCRClassification;
import org.mycore.datamodel.classifications.MCRClassificationItem;
import org.mycore.datamodel.classifications.MCRClassificationObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.metadata.MCRXMLTableManager;
import org.mycore.services.i18n.MCRTranslation;

public class MCRJPortalURIGetClassLabel implements MCRURIResolver.MCRResolver {

    private static final Logger LOGGER = Logger.getLogger(MCRJPortalURIGetClassLabel.class);

    private static String URI = "jportal_getClassLabel";

    private static String I18NDEFAULTLABELPREFIX = "editormask.labels.";

    private final String GET_FROM_JOURNAL = "getFromJournal";

    private final String DIRECTELY_GIVEN = "getDirectely";    
    
    /**
     * Returns a label of a classification.
     * 
     * Syntax:
     * <code>jportal_getClassLabel:getFromJournal:XPathWhereToFindClassIDInJournalXML
     *  OR:
     * <code>jportal_getClassLabel:getDirectely:classiID
     * 
     * @return 
     * <dummyRoot>
     *    <label>label text...</label>
     * </dummyRoot>
     */
    public Element resolveElement(String uri) {
        LOGGER.debug("start resolving " + uri);

        if (!wellURI(uri))
            throw new IllegalArgumentException("Invalid format of uri given to resolve " + URI + "=" + uri);

        // get params
        String[] pars = uri.split(":");
        String requestedResolver = pars[1];

        if (requestedResolver.equals(this.DIRECTELY_GIVEN))
            return resolveDirectely(uri); 
        else
            return resolveFromJournal(uri);        
    }

    /**
     * @param uri
     * @return
     */
    private Element resolveFromJournal(String uri) {
        // get journal id
        String journalID = MCRJPortalURIGetJournalID.getID();

        // get label
        String label = "";
        String[] params = uri.split(":");
        String classID = null;
        // get from class
        if (journalID != null && !journalID.equals(""))
            classID = MCRJPortalURIGetClassID.getClassID(journalID, params[2]);
        if (classID != null)
            label = getClassLabel(classID);
        // use default i18n one's
        else
            label = MCRTranslation.translate(I18NDEFAULTLABELPREFIX + params[2]);

        // answer xml
        Element returnXML;
        returnXML = new Element("dummyRoot");
        returnXML.addContent(new Element("label").setText(label));
        return returnXML;
    }
    
    
    /**
     * @param uri
     * @return
     */
    private Element resolveDirectely(String uri) {
        String[] params = uri.split(":");
        String label = getClassLabel(params[2]);
        
        // answer xml
        Element returnXML;
        returnXML = new Element("dummyRoot");
        returnXML.addContent(new Element("label").setText(label));
        return returnXML;
    }

    private String getClassLabel(String classID) {
        // TODO: use cache
        String currentLang = MCRSessionMgr.getCurrentSession().getCurrentLanguage();
        String label = MCRClassificationItem.getClassificationItem(classID).getText(currentLang);
        return label;
    }

    private boolean wellURI(String uri) {
        String[] pars = uri.split(":");
        int numOfArgs = pars.length;
        // number of given arguments correct ?
        if (numOfArgs != 3)
            return false;
        // right uri ?
        if (!pars[0].equals(URI))
            return false;
        // params are not empty ?
        if (pars[0].equals("") || pars[1].equals("") || pars[2].equals(""))
            return false;
        // uri is well
        LOGGER.debug("URI is ok");
        return true;
    }
}


















