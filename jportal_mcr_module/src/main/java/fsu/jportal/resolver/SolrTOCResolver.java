package fsu.jportal.resolver;

import fsu.jportal.annotation.URIResolverSchema;
import fsu.jportal.frontend.SolrToc;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

/**
 * Created by chi on 08.09.15.
 * @author Huu Chi Vu
 */
@URIResolverSchema(schema = "toc")
public class SolrTOCResolver implements URIResolver {
    /*
     * toc:parentID:objectType:rows:start
     */
    @Override
    public Source resolve(String href, String base) throws TransformerException {
        String[] uriParts = href.split(":");
        if (uriParts.length < 5) {
            throw new IllegalArgumentException("Invalid format of uri given to resolve: " + href);
        }

        String parentID = uriParts[1];
        String objectType = uriParts[2];
        int rows = Integer.valueOf(uriParts[3]);
        String startStr = uriParts[4];

        int start = 0;
        if(startStr.startsWith("ref=")){
            String refID = startStr.substring(4);
            if(refID.equals("")){
                throw new IllegalArgumentException("Invalid format of of referer: " + href);
            }
            start = SolrToc.getRefererStart(parentID, objectType, refID, rows);
        }else{
            start = Integer.valueOf(startStr);
        }
        return SolrToc.getToc(parentID, objectType, start, rows);
    }
}
