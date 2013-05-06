package fsu.jportal.xml;

import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

public class XMLTools {

    public void transform(Source xmlSource, Source xslSource, Map<String, Object> params, Result outputTarget)
            throws TransformerFactoryConfigurationError, TransformerException {
        URIResolver resolver = new URIResolver() {
            
            @Override
            public Source resolve(String href, String base) throws TransformerException {
                InputStream resource = getClass().getResourceAsStream(href);
                return new StreamSource(resource);
            }
        };
        
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setURIResolver(resolver);
        Transformer xslt = transformerFactory.newTransformer(xslSource);
        
        for (Entry<String, Object> param : params.entrySet()) {
            xslt.setParameter(param.getKey(), param.getValue());
        }
        
        xslt.transform(xmlSource, outputTarget);
    }
}