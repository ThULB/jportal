/**
 * 
 */
package org.mycore.common.xml;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import org.jdom.transform.JDOMResult;
import org.jdom.transform.JDOMSource;
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
import org.mycore.services.fieldquery.MCRQuery;
import org.mycore.services.fieldquery.MCRQueryManager;
import org.mycore.services.fieldquery.MCRResults;

/**
 * @author mcrclient
 *
 */
public class MCRJportalClassificationResolver implements MCRResolverProvider {

	public Map getResolverMapping() {
		Map map=new HashMap(1);
		map.put("jportalClassification",new MCRJPortalClassification());
		map.put("jportalURI",new MCRJPortalURI());		
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
            	returnXML = new Element("dummyRoot");
            	returnXML.addContent(new Element("hidden").setAttribute("var", parameters[3]).setAttribute("default",classID));
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
    
private static class MCRJPortalURI implements MCRResolver {
    	
    	private static final Logger LOGGER = Logger.getLogger(MCRURIResolver.class);
        private static final MCRConfiguration CONFIG = MCRConfiguration.instance();
        private static String URI_PREFIX = "jportalURI";
        static javax.xml.transform.TransformerFactory factory = javax.xml.transform.TransformerFactory.newInstance();
        /**
         * Syntax:
         * <code>jportalURI:getClass:alias
         * or
         * <code>jportalURI:getJPJournalID:conditions/boolean/condition90/@value
         * 
         * @param uri
         *            URI in the syntax above
         *            
         * @return the root element of the XML document
         */
        public Element resolveElement(String uri) {
            LOGGER.debug("start resolving "+uri);
            
            if (!wellURI(uri)) 
            	throw new IllegalArgumentException("Invalid format of uri given to resolve jportalURI:"+uri);
            
            String[] uriParams = uri.split(":");
            Element returnXML = new Element("dummyRoot");
            
            if (uriParams[1].equals("getJPJournalID")) {
            	MCRSession session = MCRSessionMgr.getCurrentSession();
            	
            	// get website context 
            	Element webSiteContextElem = new Element("root");
            	String lastPage = "";
            	if (session.get("XSL.lastPage")!=null) {
            		lastPage=(String)session.get("XSL.lastPage");
            	}
            	LOGGER.debug("S-gefundene lastPage:#################################");
            	LOGGER.debug("gefundene lastPage= "+lastPage);
            	LOGGER.debug("E:#################################");
            	String baseDir = CONFIG.getString("MCR.basedir");
            	String xslPath = baseDir+"/build/webapps/WEB-INF/stylesheets/getWebsiteContext.xsl";
            	StreamSource xsl = new StreamSource(new File(xslPath));
        		JDOMSource source = new JDOMSource(webSiteContextElem);
        		JDOMResult result = new JDOMResult();            	
        		try {
					Transformer transformer = factory.newTransformer(xsl);
					transformer.setParameter("lastpage", lastPage);
					transformer.setParameter("basedir", baseDir);
					transformer.transform(source, result); 
					
				} catch (TransformerConfigurationException e) {
					e.printStackTrace();
				} catch (TransformerException e) {
					e.printStackTrace();
				}
				webSiteContextElem = result.getDocument().getRootElement();
				
				if (webSiteContextElem==null || webSiteContextElem.getTextTrim().equals(""))
					throw new IllegalStateException("Didn't find website context URL in navigation");
				XMLOutputter out = new XMLOutputter();
				try {
					LOGGER.debug("S-gefundener websitecontext:#################################");
					LOGGER.debug("--gefundener websitecontext=");out.output(webSiteContextElem,System.out);
					LOGGER.debug("E-gefundener websitecontext:#################################");
				} catch (IOException e) {
					e.printStackTrace();
				}
				String webSiteContext = webSiteContextElem.getTextTrim();
				
            	// search for all jpjournal ids containing this website context
				String query = "(objectType = \"jpjournal\") and (webcontext = \""+webSiteContext+"\")";
	            Document input = getQueryDocument(query, null, null);
	            	// Execute query
	            long start = System.currentTimeMillis();
	            MCRResults resultIDs = MCRQueryManager.search(MCRQuery.parseXML(input));
	            long qtime = System.currentTimeMillis() - start;
	            LOGGER.debug("MCRSearching total query time: " + qtime);
				try {
					LOGGER.debug("S-gefundener jids:#################################");
					LOGGER.debug("--gefundener jids=");out.output(resultIDs.buildXML(),System.out);
					LOGGER.debug("E-gefundener jids:#################################");
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (resultIDs.getNumHits()>1)
					throw new IllegalStateException("More than one journal found that contains current website context="+webSiteContext);
				
				String journalID = resultIDs.getHit(0).getID();
				LOGGER.debug("S-ID:#################################");
				LOGGER.debug("--ID="+journalID);
				LOGGER.debug("E-ID:#################################");
				
            	// put ids in returnXML
				returnXML.addContent(new Element("hidden").setAttribute("var", uriParams[2]).setAttribute("default",journalID));
				
            	// store in cache 
					
            }
            
            return returnXML;
        }

		private boolean wellURI(String uri) {
			String[] parameters = uri.split(":");
            if ( parameters.length==3
            		&& parameters[0].equals(URI_PREFIX)
            		&& parameters[1].equals("getJPJournalID")
            		&& (parameters[2]!=null && !parameters[2].equals("")) ){
                return true;
            }
            return false;
		}

        private static Document getQueryDocument(String query, String sortby, String order) {
            Element queryElement = new Element("query");
            queryElement.setAttribute("maxResults", "0");
            queryElement.setAttribute("numPerPage", "0");
            Document input = new Document(queryElement);

            Element conditions = new Element("conditions");
            queryElement.addContent(conditions);
            conditions.setAttribute("format", "text");
            conditions.addContent(query);
            org.jdom.Element root = input.getRootElement();
            if (sortby != null) {
                final Element fieldElement = new Element("field").setAttribute("name", sortby);
                if (order != null) {
                    fieldElement.setAttribute("order", order);
                }
                root.addContent(new Element("sortBy").addContent(fieldElement));
            }
            if (LOGGER.isDebugEnabled()) {
                XMLOutputter out = new XMLOutputter(org.jdom.output.Format.getPrettyFormat());
                LOGGER.debug(out.outputString(input));
            }
            return input;
        }
        
    }    
}









