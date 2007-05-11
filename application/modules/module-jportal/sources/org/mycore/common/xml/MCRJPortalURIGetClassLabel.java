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
    /*private static final MCRConfiguration CONFIG = MCRConfiguration.instance();
    private static final String CONFIG_PREFIX = "MCR.UriResolver.";
    private static MCRCache CLASS_CACHE;
    private static long CACHE_INIT_TIME;
    */
    private static String URI = "jportal_getClassLabel";
    private static String I18NDEFAULTLABELPREFIX = "editormask.labels.";
    
    public MCRJPortalURIGetClassLabel(){
        //initCache();
    }

    /*
    private void initCache() {
        int cacheSize = MCRConfiguration.instance().getInt(CONFIG_PREFIX + "classification.CacheSize", 1000);
        CLASS_CACHE = new MCRCache(cacheSize);
        CACHE_INIT_TIME=System.currentTimeMillis();
    }
    */

    /**
     * Returns a jportal classification in format for editors from a given alias.
     * The alias is resolved by getting classification ID as value from MCRSession for key=alias 
     * 
     * Syntax:
     * <code>jportal_getClassLabel:XPathWhereToFindClassIDInJournalXML
     * 
     * @return 
     * <dummyRoot>
     *    <label>label text...</label>
     * </dummyRoot>
     */
    public Element resolveElement(String uri) {
        LOGGER.debug("start resolving "+uri);
        
        if (!wellURI(uri)) 
        	throw new IllegalArgumentException("Invalid format of uri given to resolve "+URI+"="+uri);

        // get journal id
        String journalID = MCRJPortalURIGetJournalID.getID();
        
        // get label
        String label = "";
        String[] params = uri.split(":");
        	// get from class
        if (journalID!=null && !journalID.equals("")) {
            String classID = MCRJPortalURIGetClassID.getClassID(journalID, params[1]);
            if (classID==null)
            	throw new MCRException("Could not resolve given alias "+uri+" into MCRClassificationID");
            label = getClassLabel(classID);	
		}	
        	// use default i18n one's 
        else {
        	label = MCRTranslation.translate(I18NDEFAULTLABELPREFIX+params[1]);
		}
        
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
		String[] parameters = uri.split(":");
        if ( parameters.length==2 && parameters[0].equals(URI) && !parameters[1].equals("") )
            return true;
        return false;
	}	
}
