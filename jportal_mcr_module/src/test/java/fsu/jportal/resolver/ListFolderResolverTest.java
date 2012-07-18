package fsu.jportal.resolver;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.text.MessageFormat;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.transform.JDOMSource;
import org.jdom.xpath.XPath;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mycore.common.MCRConfiguration;

public class ListFolderResolverTest {

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();
    
    enum TemplateNames{
        template_isis, template_master2, template_endocyto
    }
    
    @Before
    public void init(){
        for (TemplateNames templateName : TemplateNames.values()) {
            tmpFolder.newFolder(templateName.name());
        }
    }
    
    @Test
    public void testResolveFilePath() throws TransformerException, IOException, JDOMException {
        ListFolderResolver templateNameListResolver = new ListFolderResolver();
        Source resolve = templateNameListResolver.resolve(tmpFolder.getRoot().getAbsolutePath(), "");
        

        
        assertSource(resolve);
    }

    private void assertSource(Source resolve) throws JDOMException {
        Document resolvedDoc = ((JDOMSource) resolve).getDocument();
        
//      XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
//      xmlOutputter.output(resolvedDoc, System.out);
        
        assertEquals(TemplateNames.values().length, resolvedDoc.getRootElement().getChildren().size());
        for (TemplateNames templateName : TemplateNames.values()) {
            Object node = XPath.selectSingleNode(resolvedDoc, MessageFormat.format("/folderList/item/label[text()=''{0}'']", templateName.name()) );
            assertNotNull(templateName.name() + " should exist", node);
        }
    }
    
    @Test
    public void testResolveProperties() throws Exception {
        System.setProperty("MCR.Configuration.File", "config/test.properties");
        String propName = "test.folder";
        MCRConfiguration.instance().set(propName, tmpFolder.getRoot().getAbsolutePath());
        
        ListFolderResolver templateNameListResolver = new ListFolderResolver();
        Source resolve = templateNameListResolver.resolve("templates:prop:" + propName, "");
        assertSource(resolve);
    }
}
