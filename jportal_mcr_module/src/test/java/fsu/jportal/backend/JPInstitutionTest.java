package fsu.jportal.backend;

import java.io.IOException;
import java.io.InputStream;

import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.mycore.common.MCRStoreTestCase;
import org.mycore.datamodel.metadata.MCRObject;

public class JPInstitutionTest extends MCRStoreTestCase {

    @Test
    public void getTitle() throws Exception {
        MCRObject mcrObject = loadResource("/marc/jportal_jpinst_00000001.xml");
        JPInstitution inst = new JPInstitution(mcrObject);
        Assert.assertEquals("Thüringer Universitäts- und Landesbibliothek Jena", inst.getTitle());
    }

    public MCRObject loadResource(String path) throws IOException, JDOMException {
        SAXBuilder builder = new SAXBuilder();
        InputStream is = getClass().getResourceAsStream(path);
        return new MCRObject(builder.build(is));
    }

}
