package fsu.jportal.solr.index.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.transform.JDOMResult;
import org.jdom.xpath.XPath;
import org.junit.Test;

import fsu.jportal.test.framework.xsl.XSLTransformTest;

public class SolrDocGenerationTest extends XSLTransformTest{

    @Test
    public void test() throws JDOMException, IOException, TransformerConfigurationException, TransformerFactoryConfigurationError, TransformerException {
        String testFilePath = "/" + getClass().getSimpleName() + "/xml/oneObjResult.xml";
        InputStream testXMLAsStream = getClass().getResourceAsStream(testFilePath);
//        xmlOutput(testXMLAsStream);
        
        JDOMResult jdomResult = xslTransformation(testXMLAsStream);
        
//        xmlOutput(jdomResult);
        
        assertSolrFiled(jdomResult, "journalID", 1);
        assertSolrFiled(jdomResult, "maintitle", 1);
        assertSolrFiled(jdomResult, "journalTitle", 1);
    }
    
    private JDOMResult xslTransformation(InputStream testXMLAsStream) throws TransformerConfigurationException, TransformerFactoryConfigurationError, TransformerException {
        return xslTransformation(testXMLAsStream,"/config/jportal_mcr/solr/conf/xslt/jportal2fields.xsl");
    }

    @Test
    public void person() throws JDOMException, IOException, TransformerConfigurationException, TransformerFactoryConfigurationError, TransformerException {
        String testFilePath = "/" + getClass().getSimpleName() + "/xml/personObjResult.xml";
        InputStream testXMLAsStream = getClass().getResourceAsStream(testFilePath);
//        xmlOutput(testXMLAsStream);
        
        JDOMResult jdomResult = xslTransformation(testXMLAsStream);
        
//        xmlOutput(jdomResult);
        
        assertSolrFiled(jdomResult, "heading", 1);
    }
    
    @Test
    public void derivate() throws JDOMException, IOException, TransformerConfigurationException, TransformerFactoryConfigurationError, TransformerException {
        String testFilePath = "/" + getClass().getSimpleName() + "/xml/derivObjResult.xml";
        InputStream testXMLAsStream = getClass().getResourceAsStream(testFilePath);
//        xmlOutput(testXMLAsStream);
        
        JDOMResult jdomResult = xslTransformation(testXMLAsStream);
        
        xmlOutput(jdomResult);
        
//        assertSolrFiled(resultXML, "heading", 1);
//        assertSolrFiled(resultXML, "alternative.name", 3);
    }
    
    @Test
    public void jportal2fieldstest() throws JDOMException, IOException, TransformerConfigurationException, TransformerFactoryConfigurationError, TransformerException {
        String testFilePath = "/" + getClass().getSimpleName() + "/xml/isisObjResult.xml";
        InputStream testXMLAsStream = getClass().getResourceAsStream(testFilePath);
//        xmlOutput(testXMLAsStream);
        
        JDOMResult jdomResult = xslTransformation(testXMLAsStream);
        
//        xmlOutput(jdomResult);
        
        assertSolrFiled(jdomResult, "journalID", 1);
        assertSolrFiled(jdomResult, "maintitle", 1);
        assertSolrFiled(jdomResult, "journalTitle", 1);
        assertSolrFiled(jdomResult, "allMeta", 16);
//        assertSolrFiled(jdomResult, "date", 2);
        assertSolrFiled(jdomResult, "date.published_from", 1);
        assertSolrFiled(jdomResult, "date.published_until", 1);
//        assertSolrFiled(jdomResult, "rubric", 1);
        assertSolrFiled(jdomResult, "publisher", 1);
//        assertSolrFiled(jdomResult, "participant", 1);
    }
    
    private void assertSolrFiled(JDOMResult resultXML, String fieldName, int count) throws JDOMException {
        assertSolrFiled(resultXML.getDocument(), fieldName, count);
    }
    
    private void assertSolrFiled(Document resultXML, String fieldName, int count) throws JDOMException {
        List mycoreojectTags = XPath.selectNodes(resultXML, "/add/doc/field[@name='" + fieldName + "']");
        assertEquals("Wrong count for " + fieldName + ", ", count, mycoreojectTags.size());
    }

}
