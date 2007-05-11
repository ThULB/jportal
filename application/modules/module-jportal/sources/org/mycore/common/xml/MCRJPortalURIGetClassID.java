package org.mycore.common.xml;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.mycore.common.MCRCache;
import org.mycore.common.MCRConfiguration;
import org.mycore.common.MCRException;
import org.mycore.common.MCRSession;
import org.mycore.common.MCRSessionMgr;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.metadata.MCRObjectMetadata;
import org.mycore.datamodel.metadata.MCRXMLTableManager;

public class MCRJPortalURIGetClassID implements MCRURIResolver.MCRResolver {
 	
	private static final Logger LOGGER = Logger.getLogger(MCRJPortalURIGetClassID.class);
    private static final MCRConfiguration CONFIG = MCRConfiguration.instance();
    private static final String CONFIG_PREFIX = "MCR.UriResolver.";
    private static MCRCache CLASS_CACHE;
    private static long CACHE_INIT_TIME;
    
    private static String URI = "jportal_getClassID";
    
    public MCRJPortalURIGetClassID(){
        initCache();
    }

    private void initCache() {
        int cacheSize = MCRConfiguration.instance().getInt(CONFIG_PREFIX + "classification.CacheSize", 1000);
        CLASS_CACHE = new MCRCache(cacheSize);
        CACHE_INIT_TIME=System.currentTimeMillis();
    }

    /**
     * Returns a jportal classification in format for editors from a given alias.
     * The alias is resolved by getting classification ID as value from MCRSession for key=alias 
     * 
     * Syntax:
     * <code>jportal_getClassID:WhereToFindClassIDInJournalXML:XpathToBeFilled
     * 
     * @return 
     * <dummyRoot>
     * 	 <hidden var="XPath2BeFilled" default="classID" />
     * </dummyRoot>
     */
    public Element resolveElement(String uri) {
        LOGGER.debug("start resolving "+uri);
        
        if (!wellURI(uri)) 
        	throw new IllegalArgumentException("Invalid format of uri given to resolve "+URI+"="+uri);

        // get journal ID
        String journalID = MCRJPortalURIGetJournalID.getID();
        
        // get class id
        String[] params = uri.split(":");
        String classID = getClassID(journalID, params[1]);
        
        // answer xml 
        if (classID==null)
        	throw new MCRException("Could not resolve given alias "+uri+" into MCRClassificationID");
        Element returnXML;
        returnXML = new Element("dummyRoot");
        returnXML.addContent(new Element("hidden").setAttribute("var", params[2]).setAttribute("default",classID));
        return returnXML;
    }

    
    public static String getClassID(String journalID, String XPathWhereToFindClassIDInJournalXML) {
    	
        // TODO: use cache
    	LOGGER.debug("#############################################");
        LOGGER.debug("getClassID => journalID="+journalID);
        LOGGER.debug("#############################################");
        
        Document journalXML = MCRXMLTableManager.instance().readDocument(new MCRObjectID(journalID));
        int sepPos = XPathWhereToFindClassIDInJournalXML.indexOf("/");
        String tag1 = XPathWhereToFindClassIDInJournalXML.substring(0,sepPos);
        String tag2 = XPathWhereToFindClassIDInJournalXML.substring(sepPos+1,XPathWhereToFindClassIDInJournalXML.length());
        LOGGER.debug("#############################################");
        LOGGER.debug("MCRJPortalURIGetClassID: ermittelte tags -> tag1="+tag1+" tag2="+tag2);
        LOGGER.debug("#############################################");
        String classID = journalXML.getRootElement().getChild("metadata").getChild(tag1).getChild(tag2).getTextTrim();

        return classID;
    }
    
	private boolean wellURI(String uri) {
		String[] parameters = uri.split(":");
        if ( parameters[0].equals(URI) && parameters.length==3 && !parameters[1].equals("") && !parameters[2].equals(""))
            return true;
        return false;
	}
}












