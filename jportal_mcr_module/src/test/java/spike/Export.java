package spike;

import static org.junit.Assert.*;

import java.io.InputStream;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.Test;

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
