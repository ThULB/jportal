package fsu.jportal.solr.index.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.transform.JDOMResult;
import org.jdom.xpath.XPath;
import org.junit.Test;

public class SolrDocGenerationTest {

    public class IncludeResolver implements URIResolver {

        @Override
        public Source resolve(String href, String base) throws TransformerException {
            String styleSheetPath = "/"+SolrDocGenerationTest.class.getSimpleName() + "/xsl/" + href;
            InputStream stylesheetAsStream = getClass().getResourceAsStream(styleSheetPath);
            return new StreamSource(stylesheetAsStream);
        }

    }

    @Test
    public void test() throws JDOMException, IOException, TransformerConfigurationException, TransformerFactoryConfigurationError, TransformerException {
        String testFilePath = "/" + getClass().getSimpleName() + "/xml/oneObjResult.xml";
        InputStream testXMLAsStream = getClass().getResourceAsStream(testFilePath);
        XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
//        SAXBuilder saxBuilder = new SAXBuilder();
//        Document doc = saxBuilder.build(testXMLAsStream);
//        xmlOutputter.output(doc, System.out);
        
        JDOMResult jdomResult = xslTransformation(testXMLAsStream);
        Document resultXML = jdomResult.getDocument();
        
        xmlOutputter.output(resultXML, System.out);
        
        assertSolrFiled(resultXML, "journalID", 1);
        assertSolrFiled(resultXML, "maintitle", 1);
        assertSolrFiled(resultXML, "journalTitle", 1);
    }
    
    @Test
    public void jportal2fieldstest() throws JDOMException, IOException, TransformerConfigurationException, TransformerFactoryConfigurationError, TransformerException {
        String testFilePath = "/" + getClass().getSimpleName() + "/xml/isisObjResult.xml";
        InputStream testXMLAsStream = getClass().getResourceAsStream(testFilePath);
        XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
//        SAXBuilder saxBuilder = new SAXBuilder();
//        Document doc = saxBuilder.build(testXMLAsStream);
//        xmlOutputter.output(doc, System.out);
        
        JDOMResult jdomResult = xslTransformation(testXMLAsStream);
        Document resultXML = jdomResult.getDocument();
        
        xmlOutputter.output(resultXML, System.out);
        
        assertSolrFiled(resultXML, "journalID", 1);
        assertSolrFiled(resultXML, "maintitle", 1);
        assertSolrFiled(resultXML, "journalTitle", 1);
        assertSolrFiled(resultXML, "allMeta", 16);
        assertSolrFiled(resultXML, "date", 2);
        assertSolrFiled(resultXML, "date.published_from", 1);
        assertSolrFiled(resultXML, "date.published_until", 1);
//        assertSolrFiled(resultXML, "rubric", 1);
        assertSolrFiled(resultXML, "publisher", 1);
//        assertSolrFiled(resultXML, "participant", 1);
    }
    
    private void assertSolrFiled(Document resultXML, String fieldName, int count) throws JDOMException {
        List mycoreojectTags = XPath.selectNodes(resultXML, "/add/doc/field[@name='" + fieldName + "']");
        assertEquals("Wrong count for " + fieldName + ", ", count, mycoreojectTags.size());
    }

    private JDOMResult xslTransformation(InputStream testXMLAsStream) throws TransformerConfigurationException,
            TransformerFactoryConfigurationError, TransformerException {
        String styleSheetPath = MessageFormat.format("/config/jportal_mcr/solr/conf/xslt/jportal2fields.xsl", File.separator);
        InputStream stylesheetAsStream = getClass().getResourceAsStream(styleSheetPath);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        URIResolver resolver = new IncludeResolver();
        transformerFactory.setURIResolver(resolver);
        Templates templates = transformerFactory.newTemplates(new StreamSource(stylesheetAsStream));
        Transformer transformer = templates.newTransformer();
        JDOMResult jdomResult = new JDOMResult();
        transformer.transform(new StreamSource(testXMLAsStream), jdomResult);
        return jdomResult;
    }

}
