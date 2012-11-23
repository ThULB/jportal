package org.mycore.frontend.pagegeneration.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.jdom.JDOMException;
import org.jdom.transform.JDOMResult;
import org.junit.Test;

import fsu.jportal.test.framework.xsl.XSLTransformTest;

public class SearchResultsListTest extends XSLTransformTest{

    @Test
    public void test() throws TransformerConfigurationException, TransformerFactoryConfigurationError, TransformerException, IOException, JDOMException {
        String testFilePath = "/" + getClass().getSimpleName() + "/xml/searchPersonResults.xml";
        InputStream testXMLAsStream = getClass().getResourceAsStream(testFilePath);
//        xmlOutput(testXMLAsStream);
        JDOMResult jdomResult = xslTransformation(testXMLAsStream, "/" + getClass().getSimpleName() + "/xsl/start.xsl");
        xmlOutput(jdomResult);
//        fail("Not yet implemented");
    }

}
