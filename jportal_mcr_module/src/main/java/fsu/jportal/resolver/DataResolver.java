package fsu.jportal.resolver;

import fsu.jportal.frontend.GlobalMessageFile;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;

/**
 * get the file from .mycore/jportal/data/config/jp-globalmessage.xml
 */
@URIResolverSchema(schema = "getData")
public class DataResolver implements URIResolver {

    @Override
    public Source resolve(String href, String base) throws TransformerException {
    	try {
    		return new StreamSource(GlobalMessageFile.getInputStream());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	return null;
    }
}
