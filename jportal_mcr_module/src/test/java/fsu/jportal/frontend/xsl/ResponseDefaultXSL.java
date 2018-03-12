package fsu.jportal.frontend.xsl;

import fsu.jportal.test.framework.xsl.XSLTransformTest;
import org.jdom2.transform.JDOMResult;
import org.junit.Test;

import java.io.InputStream;
import java.util.HashMap;

/**
 * Created by chi on 06.03.18.
 *
 * @author Huu Chi Vu
 */
public class ResponseDefaultXSL extends XSLTransformTest {
    @Test
    public void renderResponse() throws Exception {
        String testFilePath = "/" + getClass().getSimpleName() + "/response.xml";
        InputStream testXMLAsStream = getClass().getResourceAsStream(testFilePath);

        JDOMResult jdomResult = xslTransformation(testXMLAsStream, "/xsl/jp-response-default_new.xsl", new HashMap<>());
        xmlOutput(jdomResult);
    }
}
