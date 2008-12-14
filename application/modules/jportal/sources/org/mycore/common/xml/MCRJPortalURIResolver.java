/**
 * 
 */
package org.mycore.common.xml;

import java.util.HashMap;
import java.util.Map;

import org.mycore.common.xml.MCRURIResolver.MCRResolver;
import org.mycore.common.xml.MCRURIResolver.MCRResolverProvider;

/**
 * @author mcrclient
 */

public class MCRJPortalURIResolver implements MCRResolverProvider {

    public Map<String, MCRResolver> getResolverMapping() {
    	
        final Map<String, MCRResolver> map = new HashMap<String, MCRResolver>();
        
		map.put("jportal_getClass",new MCRJPortalURIGetClass());		
		map.put("jportal_getClassID",new MCRJPortalURIGetClassID());
		map.put("jportal_getClassLabel",new MCRJPortalURIGetClassLabel());
		map.put("jportal_getALLClassIDs",new MCRJPortalURIGetAllClassIDs());
		map.put("jportal_getJournalID",new MCRJPortalURIGetJournalID());
		map.put("jportal_includeEditorCode",new MCRJPortalURIIncludeEditorCode());
		map.put("jportal_getDerDirXML",new MCRJPortalGetDerDirXML());
		
		return map;
	}
	
}









