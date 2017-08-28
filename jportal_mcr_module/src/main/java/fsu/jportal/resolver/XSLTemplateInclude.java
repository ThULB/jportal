package fsu.jportal.resolver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.xml.MCRURIResolver;
import org.mycore.common.xml.MCRURIResolver.MCRXslIncludeHrefs;

import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * Includes xsl files which are located in a resource folder.
 * </p>
 * Example: xslInc:/xsl/conf?template_ --> xslInc:/path/to/folder?fileStartsWithFilter
 * 
 * @return A xsl file with the includes as href.
 */
public class XSLTemplateInclude implements MCRXslIncludeHrefs {
    static final Logger LOGGER = LogManager.getLogger(XSLTemplateInclude.class);

    @Override
    public List<String> getHrefs() {
        ServletContext servletContext = MCRURIResolver.getServletContext();
        String templatesPath = "/jp_templates/";
        Set<String> resourcePaths = servletContext.getResourcePaths(templatesPath);
        List<String> hrefList = new ArrayList<String>();
        for (String string : resourcePaths) {
            String xslPath = string + "XSL/";
            Set<String> xsl = servletContext.getResourcePaths(xslPath);
            
            if(xsl != null){
                for (String xslName : xsl) {
                    if (xslName.endsWith(".xsl")) {
                        hrefList.add("webapp:" + xslName);
                    }
                }
            }
        }
        
        return hrefList;
    }
}
