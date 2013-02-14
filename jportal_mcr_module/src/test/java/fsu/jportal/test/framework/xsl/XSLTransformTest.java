package fsu.jportal.test.framework.xsl;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.transform.JDOMResult;

public class XSLTransformTest {
    XMLOutputter xmlOutputter = null;
    SAXBuilder saxBuilder = null;
    
    public class IncludeResolver implements URIResolver {

        private String styleSheetBasePath;

        public IncludeResolver(String styleSheetBasePath) {
            this.styleSheetBasePath = styleSheetBasePath;
        }

        @Override
        public Source resolve(String href, String base) throws TransformerException {
            InputStream stylesheetAsStream = getClass().getResourceAsStream("/xsl/" + href);
            
            if(stylesheetAsStream == null){
                stylesheetAsStream = getClass().getResourceAsStream(styleSheetBasePath + href);
            }
            return new StreamSource(stylesheetAsStream);
        }

    }
    
    public JDOMResult xslTransformation(InputStream testXMLAsStream, String styleSheet) throws TransformerConfigurationException,
            TransformerFactoryConfigurationError, TransformerException {
        InputStream stylesheetAsStream = getClass().getResourceAsStream(styleSheet);
    
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        URIResolver resolver = getResolver();
        transformerFactory.setURIResolver(resolver);
        Templates templates = transformerFactory.newTemplates(new StreamSource(stylesheetAsStream));
        Transformer transformer = templates.newTransformer();
        JDOMResult jdomResult = new JDOMResult();
        transformer.transform(new StreamSource(testXMLAsStream), jdomResult);
        return jdomResult;
    }
    
    public void xmlOutput(InputStream xmlStream) throws JDOMException, IOException{
        if(saxBuilder == null){
            saxBuilder = new SAXBuilder();
        }
        
        Document xmlDoc = saxBuilder.build(xmlStream);
        xmlOutput(xmlDoc);
        xmlStream.reset();
    }
    
    public void xmlOutput(JDOMResult jdromResult) throws IOException{
        xmlOutput(jdromResult.getDocument());
    }
    
    public void xmlOutput(Document xmlDoc) throws IOException{
        if(xmlOutputter == null){
            xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
        }
        
        xmlOutputter.output(xmlDoc, System.out);
    }

    public URIResolver getResolver() {
        String styleSheetBasePath = "/"+ getClass().getSimpleName() + "/xsl/";
        return new IncludeResolver(styleSheetBasePath);
    }
    
}
