package org.mycore.common.xml;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.apache.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.transform.JDOMSource;
import org.mycore.common.MCRCache;
import org.mycore.common.MCRException;
import org.mycore.common.config.MCRConfiguration;

public class MCRJPortalURIGetClass implements URIResolver {

    private static final Logger LOGGER = Logger.getLogger(MCRJPortalURIGetClass.class);
    private static final String CONFIG_PREFIX = "MCR.UriResolver.";
    private static MCRCache CLASS_CACHE;
    private static long CACHE_INIT_TIME;

    private static String URI = "jportal_getClass";

    public MCRJPortalURIGetClass() {
        initCache();
    }

    private void initCache() {
        if (CLASS_CACHE == null) {
            int cacheSize = MCRConfiguration.instance().getInt(CONFIG_PREFIX + "classification.CacheSize", 1000);
            CLASS_CACHE = new MCRCache(cacheSize, "MCRJPortalURIGetClass");
            CACHE_INIT_TIME = System.currentTimeMillis();
        }
    }

    /**
     * Returns a jportal classification in format for editors from a given
     * alias. The alias is resolved by getting classification ID as value from
     * MCRSession for key=alias
     * 
     * Syntax: <code>jportal_getClass:XPathWhereToFindInJournalXML:journalID
     * 
     * @param uri
     *            URI in the syntax above
     * 
     * @return <?root?> <item value="ID"> <label xml:lang="de">label</label>
     *         </item> <item value="ID2"> <label xml:lang="de">label2</label>
     *         </item> ... </?root?>
     */
    public Element resolveElement(String uri) {
        LOGGER.debug("start resolving " + uri);

        if (!wellURI(uri))
            throw new IllegalArgumentException("Invalid format of uri given to resolve " + URI + "=" + uri);

        // get class id
        String journalID = MCRJPortalURIGetJournalID.getID();
        String[] params = uri.split(":");

        
        String classID = MCRJPortalURIGetClassID.getClassID(journalID, params[1]);

        // get class as xml
        if (classID == null)
        		throw new MCRException("Could not resolve given alias " + uri + " into MCRClassificationID");
        //classification:editor:-1:children: -> we get <items>
        //  String classiURI = "classification:editor:-1:children:" + classID;
        //xslStyle:items2options:classification:editor:-1:children: we get <option>
        	String classiURI = "xslStyle:items2options:classification:editor:-1:children:" + classID;
        
        Element returnXML;
        // not cached
        if (MCRConfiguration.instance().getSystemLastModified() > CACHE_INIT_TIME) {
            initCache();
            returnXML = MCRURIResolver.instance().resolve(classiURI);
            CLASS_CACHE.put(classiURI, returnXML);
        } // cached
        else {
            returnXML = (Element) CLASS_CACHE.get(uri);
            if (returnXML == null) {
                returnXML = MCRURIResolver.instance().resolve(classiURI);
                CLASS_CACHE.put(classiURI, returnXML);
            }
        }
        return returnXML;
    }

    private boolean wellURI(String uri) {
        String[] parameters = uri.split(":");
        if (parameters.length == 2 && parameters[0].equals(URI) && !parameters[1].equals(""))
            return true;
        return false;
    }

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        return new JDOMSource(resolveElement(href));
    }
}
