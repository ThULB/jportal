package spike;

import java.io.File;
import java.io.FileOutputStream;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;
import org.junit.Ignore;

@Ignore
public class XMLOutputterTest extends TestCase {
    private static final String FOO_XML = "resources/testoutput/foo.xml";
    private File outFolder;
    @Override
    protected void tearDown() throws Exception {
        File output = new File(FOO_XML);
        output.delete();
        FileUtils.deleteDirectory(new File("resources"));
        super.tearDown();
    }
    
    public void testOutputFolderDoesNotExist() throws Exception {
        XMLOutputter outputter = new XMLOutputter();
        Document doc = new Document(new Element("root"));
        
        System.out.println("path: " + new File("./").getCanonicalPath());
        outFolder = new File("resources/testoutput");
        System.out.println("path out: " + outFolder.getCanonicalPath());
        if(!outFolder.exists())
            outFolder.mkdirs();
        
        outputter.output(doc, new FileOutputStream(FOO_XML));
        
        
        File output = new File(FOO_XML);
        assertTrue(output.exists());
    }
}
