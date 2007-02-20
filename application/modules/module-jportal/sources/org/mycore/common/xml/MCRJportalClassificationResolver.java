/**
 * 
 */
package org.mycore.common.xml;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;
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
        private static final MCRConfiguration CONFIG = MCRConfiguration.instance();
        private static final String CONFIG_PREFIX = "MCR.UriResolver.";
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
         * <code>jportalClassification:getClass:alias
         * or
         * <code>jportalClassification:getClassID:alias:xpathToBeFilled
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
            
            String[] parameters = uri.split(":");
            Element returnXML;
            if (parameters[1].equals("getClassID")) {
            	returnXML = new Element("hidden");
            	returnXML.setAttribute("var", parameters[3]);
            	returnXML.setAttribute("default",classID);
            	LOGGER.debug("1########################");
            	LOGGER.debug("get ClassID, return=");
            	XMLOutputter out = new XMLOutputter();
            	try {
					out.output(returnXML, System.out);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            	LOGGER.debug("2#############################################################################################");            	
            	
			} else {
				if (CONFIG.getSystemLastModified() > CACHE_INIT_TIME){
	                initCache();
	                returnXML = MCRURIResolver.instance().resolve(classiURI);
	                CLASS_CACHE.put(classiURI,returnXML);
	            } else {
	                returnXML=(Element)CLASS_CACHE.get(uri);
	                if (returnXML==null){
	                    returnXML = MCRURIResolver.instance().resolve(classiURI);
	                    CLASS_CACHE.put(classiURI,returnXML);
	                }
	            }
            	LOGGER.debug("1#############################################################################################");
            	LOGGER.debug("get Class");
            	LOGGER.debug("2#############################################################################################"); 
			}
            if (parameters[1].equals("getClassID")) {
            	LOGGER.debug("1########################");
            	LOGGER.debug("das kommt raus =");
            	XMLOutputter out = new XMLOutputter();
            	try {
    				out.output(returnXML, System.out);
    			} catch (IOException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
            	LOGGER.debug("2#############################################################################################");  
			}
 
            return returnXML;
        }

		private String resolveAlias(String uri) {
			String[] parameters = uri.split(":");
            MCRSession session = MCRSessionMgr.getCurrentSession();
            String alias = "XSL."+URI_PREFIX+"."+parameters[2];
            String classID = (String) session.get(alias);
            return classID;
		}

		private boolean wellURI(String uri) {
			String[] parameters = uri.split(":");
            if ( parameters[0].equals(URI_PREFIX) 
            		&& ((parameters[1].equals("getClass") && !parameters[2].equals("") && parameters.length==3) 
            			|| (parameters[1].equals("getClassID") && !parameters[2].equals("") && !parameters[3].equals("") && parameters.length==4))){
                return true;
            }
            return false;
		}
    }
}
