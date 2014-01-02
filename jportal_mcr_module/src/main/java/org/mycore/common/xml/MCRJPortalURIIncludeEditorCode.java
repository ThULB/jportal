package org.mycore.common.xml;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import org.jdom2.transform.JDOMSource;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.common.config.MCRConfiguration;

public class MCRJPortalURIIncludeEditorCode implements URIResolver {

    private static final Logger LOGGER = Logger.getLogger(MCRJPortalURIIncludeEditorCode.class);

    private static final MCRConfiguration CONFIG = MCRConfiguration.instance();

    //    private static MCRCache CACHE;

    private final String JPJOURNAL = "jpjournal";

    private final String JPARTICLE = "jparticle";

    private static final String SEP = "#$#$#$#";

    private static String URI = "jportal_includeEditorCode";

    private static final String FS = System.getProperty("file.seperator", "/");

    private static final String CONFIG_PREFIX = "MCR.UriResolver.";

    //    public MCRJPortalURIIncludeEditorCode() {
    //        initCache();
    //    }

    //    private void initCache() {
    //        if (CACHE == null) {
    //            int cacheSize = MCRConfiguration.instance().getInt(CONFIG_PREFIX + "classification.CacheSize", 1000);
    //            CACHE = new MCRCache(cacheSize, "MCRJPortalURIIncludeEditorCode");
    //        }
    //    }

    /**
     * 
     * Syntax:
     * <code>jportal_includeEditorCode:jparticle:XPathWhereToFindClassIDInJournalXML:FileContainingEditorCode:IdOfPieceOfCode:cacheAble
     *  OR
     * <code>jportal_includeEditorCode:jparticle:XPathWhereToFindClassIDInJournalXML:FileContainingEditorCode:IdOfPieceOfCode
     *  OR
     * <code>jportal_includeEditorCode:jpjournal:FileContainingEditorCode:IdOfPieceOfCode
     * 
     *  The cacheAble attribute is optional, default is true
     *  FileContainingEditorCode: Relativ to $ServletContextPath of Webapplication
     *  IdOfPieceOfCode: <includeMyChildren id="include_rubric"> => $IdOfPieceOfCode="include_rubric"
     * 
     * @return <includeMyChildren id="$IdOfPieceOfCode">
     *         <codeContainedWithin-includeMyChildren /> </includeMyChildren>
     * @throws IOException
     * @throws JDOMException
     * 
     */
    public Element resolveElement(String uri) throws IOException, JDOMException {
        LOGGER.debug("start resolving " + uri);

        if (!wellURI(uri))
            throw new IllegalArgumentException("Invalid format of uri given to resolve " + URI + "=" + uri);

        // get params
        String[] pars = uri.split(":");
        String requestedResolver = pars[1];

        if (requestedResolver.equals(this.JPARTICLE))
            return resolve4JPArticle(uri, pars);
        else
            return resolve4JPJournal(uri, pars);

    }

    /**
     * <code>jportal_includeEditorCode:jpjournal:FileContainingEditorCode:
     * IdOfPieceOfCode
     * 
     * document = FileContainingEditorCode->IdOfPieceOfCode can contain and will
     * be replaced before delivery - #REPLACE_WITH_RUNNING_NUMBER# replacedBy->
     * an increasing number starting with 1 dependent on how much
     * classifications are in the list of property -
     * #REPLACE_WITH_CLASSIFICATION-ID# replacedBy-> classification id
     * 
     * @throws IOException
     * @throws JDOMException
     * 
     */
    private Element resolve4JPJournal(String uri, String[] pars) throws IOException, JDOMException {
        String fileContainingEditorCode = pars[2];
        String idOfPieceOfCode = pars[3];
        String propKey = "MCR.Module-JPortal.DynamicClassification.journal";

        // try to get code to be included from cache
        //        String cacheKey = getCacheKey("DUMMY", uri);
        //        if (!CACHE.isEmpty() && CACHE.keys().contains(cacheKey)) {
        //            LOGGER.debug("Editor code for jpjournal with URI=" + uri + " has been found in cache. So, just return it.");
        //            return ((Element) CACHE.get(cacheKey));
        //        }

        // is property set ?
        Map<String, String> propertiesMap = MCRConfiguration.instance().getPropertiesMap();
        if (!propertiesMap.containsKey(propKey) || propertiesMap.get(propKey).equals("")) {
            LOGGER.debug("BREAK: no property or property value found for " + propKey);
            return getEmptyAnswer();
        }

        // get piece of code
        Element pieceOfCode = getPieceOfCode(fileContainingEditorCode, idOfPieceOfCode);
        if (pieceOfCode == null) {
            LOGGER.debug("piece of code with xpath NOT found or exeption while executing XPATH occured");
            return getEmptyAnswer();
        }

        // replace dynamic values and dublicate pieceOfCode as described in head
        // // get classi-id's
        StringTokenizer classiList = new StringTokenizer(propertiesMap.get(propKey), ",");
        Document editor = new Document(new Element("root"));
        // // iterate over classiList
        for (int runningNumber = 0; classiList.hasMoreElements(); runningNumber++) {
            String classiID = (String) classiList.nextElement();
            // // get pieceOfCode to $poc
            XMLOutputter xout = new XMLOutputter();
            String poc = xout.outputString(pieceOfCode);
            // // replace
            String pattern1 = "#REPLACE_WITH_RUNNING_NUMBER#";
            String pattern2 = "#REPLACE_WITH_CLASSIFICATION-ID#";
            String pocNew = poc.replaceAll(pattern1, Integer.toString(runningNumber + 1));
            String pocNew2 = pocNew.replaceAll(pattern2, classiID);
            // // transform to jdom and add to $editor
            SAXBuilder sb = new SAXBuilder();
            Element pieceOfEditor = sb.build(new StringReader(pocNew2)).detachRootElement();
            editor.getRootElement().addContent(pieceOfEditor.cloneContent());
        }
        LOGGER.debug((new XMLOutputter()).outputString(editor));

        // cache
        //        CACHE.put(cacheKey, editor.getRootElement());
        //        LOGGER.debug("Editor code for jpjournal with URI=" + uri + " put to cache.");

        // return editor code
        return editor.getRootElement();
    }

    /**
     * @param uri
     * @param pars
     * @return
     */
    private Element resolve4JPArticle(String uri, String[] pars) {
        String xPathWhereToFindClassIDInJournalXML = pars[2];
        String fileContainingEditorCode = pars[3];
        String idOfPieceOfCode = pars[4];

        //        boolean cacheAble = true;

        //        if (pars.length == 6)
        //            cacheAble = Boolean.parseBoolean(pars[5]);

        // get journal ID
        String journalID = MCRJPortalURIGetJournalID.getID();
        if (journalID.equals("")) {
            LOGGER.debug("BREAK: journal NOT found");
            return getEmptyAnswer();
        }

        // try to get code to be included from cache
        //        String cacheKey = getCacheKey(journalID, uri);
        //        if (cacheAble) {
        //            if (!CACHE.isEmpty() && CACHE.keys().contains(cacheKey)) {
        //                LOGGER.debug("Editor code for journal=" + journalID + " and URI=" + uri + " has been found in cache. So, just return it.");
        //                return ((Element) CACHE.get(cacheKey));
        //            }
        //        }

        // get class id
        String classID = MCRJPortalURIGetClassID.getClassID(journalID, xPathWhereToFindClassIDInJournalXML);
        if (classID == null || classID.equals("")) {
            LOGGER.debug("BREAK: classid is null or emtpy");
            //            CACHE.put(cacheKey, getEmptyAnswer());
            return getEmptyAnswer();
        }

        // return empty answer if $classID is in black list
        String blackList = CONFIG.getString("MCR.Module-JPortal.SearchMask.ClassiBlackList", "");
        if (!blackList.equals("") && blackList.indexOf(classID) != -1) {
            LOGGER.debug("BREAK: classid=" + classID + " ON blacklist");
            //            CACHE.put(cacheKey, getEmptyAnswer());
            return getEmptyAnswer();
        }

        // get piece of code and return it
        Element pieceOfCode = getPieceOfCode(fileContainingEditorCode, idOfPieceOfCode /*, cacheKey*/);
        if (pieceOfCode != null)
            return pieceOfCode;
        else {
            LOGGER.debug("piece of code with xpath NOT found or exeption while executing XPATH occured");
            return getEmptyAnswer();
        }
    }

    /**
     * @param fileContainingEditorCode
     * @param idOfPieceOfCode
     */
    /*private Element getPieceOfCode(String fileContainingEditorCode, String idOfPieceOfCode) {
        return getPieceOfCode(fileContainingEditorCode, idOfPieceOfCode, "");
    }*/

    /**
     * @param fileContainingEditorCode
     * @param idOfPieceOfCode
     * @param cacheKey
     */
    private Element getPieceOfCode(String fileContainingEditorCode, String idOfPieceOfCode/*, String cacheKey*/) {
        // get piece of code and return it
        String sourceLoc = "webapp:" + fileContainingEditorCode;
        Element sourceCode = MCRURIResolver.instance().resolve(sourceLoc);
        String xpathEx = "includeMyChildren[@id='" + idOfPieceOfCode + "']";
        XPathExpression<Element> xpath = XPathFactory.instance().compile(xpathEx, Filters.element());
        Element answer = xpath.evaluateFirst(sourceCode);
        if (answer == null) {
            LOGGER.warn("piece of code with xpath=" + xpathEx + " NOT found");
            return null;
        }
        return (Element) answer.detach();
    }

    private static Element getEmptyAnswer() {
        return (new Element("root"));
    }

    /*
     * private boolean wellURI(String uri) { String[] pars = uri.split(":"); int
     * numOfArgs = pars.length; // number of given arguments correct ? if
     * (numOfArgs < 4 || numOfArgs > 5) return false; // right uri if
     * (!pars[0].equals(URI)) return false; // verify jpjournal uri if
     * (numOfArgs == 4) { // params are not empty ? if (pars[0].equals("") ||
     * pars[1].equals("") || pars[2].equals("") || pars[3].equals("")) return
     * false; // not called jpvolume resolver ? if
     * (!pars[1].equals(this.JPJOURNAL)) return false; } // verify jparticle uri
     * if (numOfArgs == 5) { // params are not empty ? if (pars[0].equals("") ||
     * pars[1].equals("") || pars[2].equals("") || pars[3].equals("") ||
     * pars[4].equals("")) return false; // not called jparticle resolver ? if
     * (!pars[1].equals(this.JPARTICLE)) return false; } // uri is well
     * LOGGER.debug("URI is ok"); return true; }
     */

    private boolean wellURI(String uri) {
        String[] pars = uri.split(":");
        int numOfArgs = pars.length;

        // number of given arguments correct ?
        if (!(numOfArgs >= 4) && !(numOfArgs <= 6))
            return false;
        // right uri
        if (!pars[0].equals(URI))
            return false;

        // there should be no empty String or blanks between two ":"
        for (int i = 0; i < numOfArgs; i++) {
            if (pars[i].trim().equals(""))
                return false;
        }

        return true;
    }

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        try {
            return new JDOMSource(resolveElement(href));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JDOMException e) {
            e.printStackTrace();
        }

        return null;
    }

}
