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

        private static final Pattern EDITORFORMAT_PATTERN = Pattern.compile("(\\[)([^\\]]*)(\\])");

        private static final MCRConfiguration CONFIG = MCRConfiguration.instance();

        private static final String CONFIG_PREFIX = "MCR.UriResolver.";
        
        private static final String FORMAT_CONFIG_PREFIX = CONFIG_PREFIX+"classification.format.";
        
        private static MCRCache CLASS_CACHE;
        
        private static long CACHE_INIT_TIME;

        
        public MCRJPortalClassification(){
            initCache();
        }

        private void initCache() {
            int cacheSize = MCRConfiguration.instance().getInt(CONFIG_PREFIX + "classification.CacheSize", 1000);
            CLASS_CACHE = new MCRCache(cacheSize);
            CACHE_INIT_TIME=System.currentTimeMillis();
        }

        /**
         * returns a classification in a specific format.
         * 
         * Syntax:
         * <code>classification:{editor['['formatAlias']']|metadata}:{Levels}:{parents|children}:{ClassID}[:CategID]
         * 
         * formatAlias: MCRConfiguration property MCR.UriResolver.classification.format.formatAlias
         * 
         * @param uri
         *            URI in the syntax above
         *            
         * @return the root element of the XML document
         * @see ClassificationTransformer#getEditorDocument(Classification, String)
         */
        public Element resolveElement(String uri) {
            LOGGER.debug("start resolving "+uri);
            Element returns;
            if (CONFIG.getSystemLastModified() > CACHE_INIT_TIME){
                initCache();
                returns = getClassElement(uri);
                CLASS_CACHE.put(uri,returns);
            } else {
                returns=(Element)CLASS_CACHE.get(uri);
                if (returns==null){
                    returns = getClassElement(uri);
                    CLASS_CACHE.put(uri,returns);
                }
            }
            return returns;
        }

        private Element getClassElement(String uri) {
        	
        	//HttpSession session = 
            String[] parameters = uri.split(":");
            if (parameters.length!=2){
                //sanity check
                throw new IllegalArgumentException("Invalid format of uri for retrieval of jportalClassification: "+uri);
            }
            
            //MCRSessionMgr.getCurrentSession().
            //String type = parameters[1];
            
            // get classification ID from session using $type --> classID
            String classID = "jportal_class_00000001";
            String axis = "children"; 
            String categ = "";
            int depth = -1;
            String format = "editor";
            
            Classification cl=null;
            String labelFormat = getLabelFormat("editor");
            boolean withCounter=false;
            if ((labelFormat!=null) && (labelFormat.indexOf("{count}")!=-1)){
                withCounter=true;
            }
            LOGGER.debug("start ClassificationQuery");
            if (axis.equals("children")) {
                if (categ.length() > 0) {
                    cl = MCRClassificationQuery.getClassification(classID, categ, depth, withCounter);
                } else {
                    cl = MCRClassificationQuery.getClassification(classID, depth, withCounter);
                }
            } 
            
            Element returns;
            LOGGER.debug("start transformation of ClassificationQuery");
            if (format.startsWith("editor")) {
                if (labelFormat == null) {
                    returns = ClassificationTransformer.getEditorDocument(cl).getRootElement();
                } else {
                    returns = ClassificationTransformer.getEditorDocument(cl, labelFormat).getRootElement();
                }
            } else if (format.equals("metadata")) {
                returns = ClassificationTransformer.getMetaDataDocument(cl).getRootElement();
            } else {
                LOGGER.error("Unknown target format given. URI: "+uri);
                throw new IllegalArgumentException("Invalid target format ("+format+ ") in uri for retrieval of classification: "+uri);
            }
            LOGGER.debug("end resolving "+uri);
            return returns;
        }

        private static String getLabelFormat(String editorString) {
            Matcher m = EDITORFORMAT_PATTERN.matcher(editorString);
            if ((m.find()) && (m.groupCount() == 3)) {
                String formatDef = m.group(2);
                return CONFIG.getString(FORMAT_CONFIG_PREFIX + formatDef);
            }
            return null;
        }

    }
	

}
