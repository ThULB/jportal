package org.mycore.common.xml;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;
import org.mycore.common.MCRConfiguration;

public class MCRJPortalURIIncludeEditorCode implements MCRURIResolver.MCRResolver {

    private static final Logger LOGGER = Logger.getLogger(MCRJPortalURIIncludeEditorCode.class);

    private static final MCRConfiguration CONFIG = MCRConfiguration.instance();

    private static String URI = "jportal_includeEditorCode";

    private static HashMap CACHE = new HashMap();

    private static final String SEP = "#$#$#$#";

    /**
     * 
     * Syntax:
     * <code>jportal_includeEditorCode:XPathWhereToFindClassIDInJournalXML:FileContainingEditorCode:IdOfPieceOfCode
     * 
     *  FileContainingEditorCode: Relativ to $ServletContextPath of Webapplication
     *  IdOfPieceOfCode: <includeMyChildren id="include_rubric"> => $IdOfPieceOfCode="include_rubric"
     * 
     * @return 
     * <includeMyChildren id="$IdOfPieceOfCode">
     *       <codeContainedWithin-includeMyChildren />
     * </includeMyChildren>
     *
     */

    public Element resolveElement(String uri) {
        LOGGER.debug("start resolving " + uri);

        if (!wellURI(uri))
            throw new IllegalArgumentException("Invalid format of uri given to resolve " + URI + "=" + uri);

        // get params
        String[] params = uri.split(":");
        String xPathWhereToFindClassIDInJournalXML = params[1];
        String fileContainingEditorCode = params[2];
        String idOfPieceOfCode = params[3];

        // get journal ID
        String journalID = MCRJPortalURIGetJournalID.getID();
        if (journalID.equals("")) {
            LOGGER.debug("BREAK: journal NOT found");
            return getEmptyAnswer();
        }

        // try to get code to be included from cache
        String cacheKey = getCacheKey(journalID, uri);
        if (!CACHE.isEmpty() && CACHE.containsKey(cacheKey)) {
            LOGGER.debug("Editor code for journal=" + journalID + " and URI=" + uri + " has been found in cache. So, just return it.");
            return ((Element) CACHE.get(cacheKey));
        }

        // get class id
        String classID = MCRJPortalURIGetClassID.getClassID(journalID, xPathWhereToFindClassIDInJournalXML);
        if (classID == null || classID.equals("")) {
            LOGGER.debug("BREAK: classid is null or emtpy");
            CACHE.put(cacheKey, getEmptyAnswer());
            return getEmptyAnswer();
        }

        // return empty answer if $classID is in black list
        String blackList = CONFIG.getString("MCR.Module-JPortal.SearchMask.ClassiBlackList", "");
        if (!blackList.equals("") && blackList.indexOf(classID) != -1) {
            LOGGER.debug("BREAK: classid=" + classID + " ON blacklist");
            CACHE.put(cacheKey, getEmptyAnswer());
            return getEmptyAnswer();
        }

        // get piece of code and return it
        String sourceLoc = "webapp:" + fileContainingEditorCode;
        Element sourceCode = MCRURIResolver.instance().resolve(sourceLoc);
        Document doc = new Document(sourceCode);
        try {
            String xpathEx = "/*/includeMyChildren[@id='" + idOfPieceOfCode + "']";
            XPath xpath = XPath.newInstance(xpathEx);
            Element answer = (Element) xpath.selectSingleNode(doc);
            if (answer == null) {
                LOGGER.debug("piece of code with xpath=" + xpathEx + " NOT found");
                CACHE.put(cacheKey, getEmptyAnswer());
                return getEmptyAnswer();
            }
            CACHE.put(cacheKey, answer);
            return answer;
        } catch (JDOMException e) {
            e.printStackTrace();
        }

        LOGGER.debug("piece of code with xpath found BUT nothing given back ?????");
        return getEmptyAnswer();
    }

    private String getCacheKey(String journalID, String uri) {
        return journalID + SEP + uri;
    }

    private static Element getEmptyAnswer() {
        return (new Element("root"));
    }

    private boolean wellURI(String uri) {
        String[] parameters = uri.split(":");
        int numOfArgs = parameters.length;
        // number of given arguments correct ?
        if (numOfArgs != 4)
            return false;
        // right uri
        if (!parameters[0].equals(URI))
            return false;
        // params are not empty ?
        if (parameters[0].equals("") || parameters[1].equals("") || parameters[2].equals("") || parameters[3].equals(""))
            return false;
        LOGGER.debug("URI is ok");
        return true;
    }

}