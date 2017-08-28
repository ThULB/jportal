package spike;

import org.junit.Test;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;

public class Export {

    @Test
    public void export() throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        InputStream xsl = getClass().getResourceAsStream("/export/derivLink.xsl");
        InputStream xml = getClass().getResourceAsStream("/export/jportal_jpvolume_00141181.xml");
        Transformer transformer = transformerFactory.newTransformer(new StreamSource(xsl));
        transformer.transform(new StreamSource(xml), new StreamResult(System.out));
    }
}
