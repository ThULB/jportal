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

import org.jdom2.Document;
import org.jdom2.transform.JDOMResult;

public class XMLTools {

    public Document transform(String xmlSource, String xslSource, Map<String, Object> params) throws TransformerFactoryConfigurationError, TransformerException{
        InputStream xmlIS = getClass().getResourceAsStream(xmlSource);
        InputStream xslIS = getClass().getResourceAsStream(xslSource);
        StreamSource xmlSourceStream = new StreamSource(xmlIS);
        StreamSource xslSourceStream = new StreamSource(xslIS);
        JDOMResult transformationResult = new JDOMResult();
        
        transform(xmlSourceStream, xslSourceStream, params, transformationResult);
        
        return transformationResult.getDocument();
    }
    
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