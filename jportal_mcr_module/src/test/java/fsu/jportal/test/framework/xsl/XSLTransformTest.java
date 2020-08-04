package fsu.jportal.test.framework.xsl;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
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
            System.out.println("inc: " + href);
            InputStream stylesheetAsStream = getClass().getResourceAsStream("/xsl/" + href);

            if (stylesheetAsStream == null) {
                stylesheetAsStream = getClass().getResourceAsStream(styleSheetBasePath + href);
            }
            return new StreamSource(stylesheetAsStream);
        }

    }

    public JDOMResult xslTransformation(String testXMLPath, String styleSheetPath,
                                        Map<String, String> params) throws TransformerConfigurationException,
            TransformerFactoryConfigurationError, TransformerException {
        InputStream testXMLAsStream = getClass().getResourceAsStream(testXMLPath);
        InputStream stylesheetAsStream = getClass().getResourceAsStream(styleSheetPath);

        return xslTransformation(testXMLAsStream, stylesheetAsStream, params);
    }

    public JDOMResult xslTransformation(InputStream testXMLAsStream, InputStream stylesheetAsStream,
                                        Map<String, String> params) throws TransformerConfigurationException,
            TransformerFactoryConfigurationError, TransformerException {

        TransformerFactory transformerFactory = TransformerFactory
                .newInstance("org.apache.xalan.processor.TransformerFactoryImpl", null);
        if (transformerFactory.getFeature(SAXSource.FEATURE) && transformerFactory.getFeature(SAXResult.FEATURE)) {
            SAXTransformerFactory saxFactory = (SAXTransformerFactory) transformerFactory;
            URIResolver resolver = getResolver();
            saxFactory.setURIResolver(resolver);
            Transformer transformer = saxFactory.newTransformer(new StreamSource(stylesheetAsStream));

            Optional.ofNullable(params)
                    .orElseGet(() -> new HashMap<>())
                    .forEach((param, value) -> {
                        transformer.setParameter(param, params.get(param));
                    });

            JDOMResult jdomResult = new JDOMResult();
            transformer.transform(new StreamSource(testXMLAsStream), jdomResult);
            return jdomResult;
        } else {
            throw new TransformerFactoryConfigurationError("No SAXTransformerFactory");
        }
    }

    public void xmlOutput(String xmlPath) throws JDOMException, IOException {
        xmlOutput(getClass().getResourceAsStream(xmlPath));
    }
    public void xmlOutput(InputStream xmlStream) throws JDOMException, IOException {
        if (saxBuilder == null) {
            saxBuilder = new SAXBuilder();
        }

        Document xmlDoc = saxBuilder.build(xmlStream);
        xmlOutput(xmlDoc);
        xmlStream.close();
    }

    public void xmlOutput(JDOMResult jdromResult) throws IOException {
        xmlOutput(jdromResult.getDocument());
    }

    public void xmlOutput(Document xmlDoc) throws IOException {
        if (xmlOutputter == null) {
            xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
        }

        xmlOutputter.output(xmlDoc, System.out);
    }

    public URIResolver getResolver() {
        String styleSheetBasePath = "/" + getClass().getSimpleName() + "/xsl/";
        return new IncludeResolver(styleSheetBasePath);
    }

}
