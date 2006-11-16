/**
 * 
 */
package org.mycore.common.xml;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.mycore.common.MCRCache;
import org.mycore.common.MCRConfiguration;
import org.mycore.common.MCRException;
import org.mycore.common.MCRSession;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.xml.MCRURIResolver;
import org.mycore.common.xml.MCRURIResolver.MCRResolver;
import org.mycore.common.xml.MCRURIResolver.MCRResolverProvider;
import org.mycore.datamodel.classifications.query.Classification;
import org.mycore.datamodel.classifications.query.ClassificationTransformer;
import org.mycore.datamodel.classifications.query.MCRClassificationQuery;
import org.mycore.frontend.servlets.MCRServlet;

/**
 * @author mcrclient
 *
 */
public class MCRJportalClassificationResolver implements MCRResolverProvider {

	public Map getResolverMapping() {
		Map map=new HashMap(1);
		map.put("jportalClassification",new MCRJPortalClassification());
		return map;
	}
	
    private static class MCRJPortalClassification implements MCRResolver {
    	private static final Logger LOGGER = Logger.getLogger(MCRURIResolver.class);

        //private static final Pattern EDITORFORMAT_PATTERN = Pattern.compile("(\\[)([^\\]]*)(\\])");

        private static final MCRConfiguration CONFIG = MCRConfiguration.instance();

        private static final String CONFIG_PREFIX = "MCR.UriResolver.";
        
        //private static final String FORMAT_CONFIG_PREFIX = CONFIG_PREFIX+"classification.format.";
        
        private static MCRCache CLASS_CACHE;
        
        private static long CACHE_INIT_TIME;

        private static String URI_PREFIX = "jportalClassification";
        
        public MCRJPortalClassification(){
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
         * <code>jportalClassification:alias
         * 
         * @param uri
         *            URI in the syntax above
         *            
         * @return the root element of the XML document
         */
        public Element resolveElement(String uri) {
            LOGGER.debug("start resolving "+uri);
            
            if (!wellURI(uri)) 
            	throw new IllegalArgumentException("Invalid format of uri given to resolve jportalClassification:"+uri);
            String classID = resolveAlias(uri);
            if (classID==null)
            	throw new MCRException("Could not resolve given alias "+uri+" into MCRClassificationID");
            String classiURI = "classification:editor:-1:children:"+classID;
            Element returns;
            if (CONFIG.getSystemLastModified() > CACHE_INIT_TIME){
                initCache();
                returns = MCRURIResolver.instance().resolve(classiURI);
                CLASS_CACHE.put(classiURI,returns);
            } else {
                returns=(Element)CLASS_CACHE.get(uri);
                if (returns==null){
                    returns = MCRURIResolver.instance().resolve(classiURI);
                    CLASS_CACHE.put(classiURI,returns);
                }
            }
            return returns;
        }

		private String resolveAlias(String uri) {
			String[] parameters = uri.split(":");
            MCRSession session = MCRSessionMgr.getCurrentSession();
            String alias = "XSL."+URI_PREFIX+"."+parameters[1];
            String classID = (String) session.get(alias);
            return classID;
		}

		private boolean wellURI(String uri) {
			String[] parameters = uri.split(":");
            if (parameters.length==2 && parameters[0].equals(URI_PREFIX) && !parameters[1].equals("")){
                return true;
            }
            return false;
		}
    }
}
