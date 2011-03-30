package org.mycore.common.xml;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.transform.JDOMSource;
import org.mycore.common.MCRException;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;

public class MCRJPortalURIGetClassID implements URIResolver {

    private static final Logger LOGGER = Logger.getLogger(MCRJPortalURIGetClassID.class);

    private static String URI = "jportal_getClassID";

    private static final String INCLUDE_TAG_RETURN_VALUE = "classification:editor[TextCounter]:2:children:";

    /**
     * 
     * Syntax:
     * <code>jportal_getClassID:WhereToFindClassIDInJournalXML:XpathToBeFilled:ReturnIncludeTag
     * 
     * ReturnIncludeTag: optional, if set => $ReturnIncludeTag="getInclude" 
     * 
     * @return 
     * if $ReturnIncludeTag="getInclude"
     *  <dummyRoot>
     *       <include cacheable="false" uri="classification:editor[TextCounter]:2:children:$classID" />
     *  </dummyRoot>
     * else 
     * <dummyRoot>
     *       <hidden var="XPath2BeFilled" default="classID" />
     *  </dummyRoot>
     *
     */
    public Element resolveElement(String uri) {
        LOGGER.debug("start resolving " + uri);

        if (!wellURI(uri))
            throw new IllegalArgumentException("Invalid format of uri given to resolve " + URI + "=" + uri);

        // get journal ID
        String journalID = MCRJPortalURIGetJournalID.getID();

        // get class id
        String[] params = uri.split(":");
        String classID = getClassID(journalID, params[1]);
        if (classID == null)
            throw new MCRException("Could not resolve given alias " + uri + " into MCRClassificationID");

        // answer xml
        Element returnXML = new Element("dummyRoot");
        if (returnIncludeTag(uri)) {
            String uriVal = INCLUDE_TAG_RETURN_VALUE + classID;
            returnXML.addContent(new Element("include").setAttribute("cacheable", "false").setAttribute("uri", uriVal));
        } else
            returnXML.addContent(new Element("hidden").setAttribute("var", params[2]).setAttribute("default", classID));

        return returnXML;
    }

    public static String getClassID(String journalID, String XPathWhereToFindClassIDInJournalXML) {

        // TODO: use cache
        LOGGER.debug("getClassID => journalID=" + journalID);

        Document journalXML = MCRXMLMetadataManager.instance().retrieveXML(MCRObjectID.getInstance(journalID));
        int sepPos = XPathWhereToFindClassIDInJournalXML.indexOf("/");
        String tag1 = XPathWhereToFindClassIDInJournalXML.substring(0, sepPos);
        String tag2 = XPathWhereToFindClassIDInJournalXML.substring(sepPos + 1, XPathWhereToFindClassIDInJournalXML.length());
        LOGGER.debug("MCRJPortalURIGetClassID: ermittelte tags -> tag1=" + tag1 + " tag2=" + tag2);
        Element classID_root = journalXML.getRootElement().getChild("metadata").getChild(tag1);
        String classID = null;
        if (classID_root != null && classID_root.getChild(tag2) != null)
            classID = classID_root.getChild(tag2).getTextTrim();
        return classID;
    }

    private boolean wellURI(String uri) {
        String[] parameters = uri.split(":");
        int numOfArgs = parameters.length;
        // number of given arguments correct ?
        if (numOfArgs < 3 || numOfArgs > 4)
            return false;
        // right uri
        if (!parameters[0].equals(URI))
            return false;
        // params are not empty ?
        if (numOfArgs == 3 && (parameters[1].equals("") || parameters[2].equals("")))
            return false;
        if (numOfArgs == 4 && (parameters[1].equals("") || parameters[2].equals("") || parameters[3].equals("")) && !returnIncludeTag(uri))
            return false;
        return true;
    }

    private boolean returnIncludeTag(String uri) {
        String[] parameters = uri.split(":");
        if (parameters.length == 4 && parameters[3].equals("getInclude"))
            return true;
        else
            return false;
    }

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        return new JDOMSource(resolveElement(href));
    }
}
