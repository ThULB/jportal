package org.mycore.common.xml;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.mycore.common.MCRException;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.metadata.MCRXMLTableManager;

public class MCRJPortalURIGetClassID implements MCRURIResolver.MCRResolver {
 	
	private static final Logger LOGGER = Logger.getLogger(MCRJPortalURIGetClassID.class);
    private static String URI = "jportal_getClassID";
    
    /**
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
        Element classID_root = journalXML.getRootElement().getChild("metadata").getChild(tag1);
        String classID=null;
        if (classID_root!=null && classID_root.getChild(tag2)!=null) 
            classID=classID_root.getChild(tag2).getTextTrim();
        return classID;
    }
    
	private boolean wellURI(String uri) {
		String[] parameters = uri.split(":");
        if ( parameters[0].equals(URI) && parameters.length==3 && !parameters[1].equals("") && !parameters[2].equals(""))
            return true;
        return false;
	}
}












