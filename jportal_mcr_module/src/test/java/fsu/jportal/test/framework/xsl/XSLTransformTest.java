package fsu.jportal.test.framework.xsl;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.transform.JDOMResult;

import javax.validation.constraints.NotNull;
import javax.xml.transform.*;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

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

    public JDOMResult xslTransformation(InputStream testXMLAsStream, String styleSheet,
                                        @NotNull Map<String, String> params) throws TransformerConfigurationException,
            TransformerFactoryConfigurationError, TransformerException {

        InputStream stylesheetAsStream = getClass().getResourceAsStream(styleSheet);
        TransformerFactory transformerFactory = TransformerFactory
                .newInstance("org.apache.xalan.processor.TransformerFactoryImpl", null);
        if (transformerFactory.getFeature(SAXSource.FEATURE) && transformerFactory.getFeature(SAXResult.FEATURE)) {
            SAXTransformerFactory saxFactory = (SAXTransformerFactory) transformerFactory;
            URIResolver resolver = getResolver();
            saxFactory.setURIResolver(resolver);
            Transformer transformer = saxFactory.newTransformer(new StreamSource(stylesheetAsStream));

            params.forEach((param, value) -> {
                transformer.setParameter(param, params.get(param));
            });

            JDOMResult jdomResult = new JDOMResult();
            transformer.transform(new StreamSource(testXMLAsStream), jdomResult);
            return jdomResult;
        } else {
            throw new TransformerFactoryConfigurationError("No SAXTransformerFactory");
        }
    }

    public void xmlOutput(InputStream xmlStream) throws JDOMException, IOException {
        if (saxBuilder == null) {
            saxBuilder = new SAXBuilder();
        }

        Document xmlDoc = saxBuilder.build(xmlStream);
        xmlOutput(xmlDoc);
        xmlStream.reset();
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
