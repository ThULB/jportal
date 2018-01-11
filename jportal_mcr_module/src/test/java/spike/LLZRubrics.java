package spike;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Created by chi on 01.06.17.
 *
 * @author Huu Chi Vu
 */

/**
 * Created by chi on 10.02.16.
 *
 * @author Huu Chi Vu
 */
public class LLZRubrics {
    // LLZ classification bf1522f2973b41c7bda3f25e0c5d5eb0
    @Test
    public void testName() throws Exception {
        URL rubricsText = getClass().getResource("/rubrics.txt");
        System.out.println("Path: " + rubricsText.getPath());
        Files.lines(Paths.get(rubricsText.getPath()))
             .filter(s -> s.trim() != "" && s.trim() != "\n" && s.trim().length() > 1).forEach(this::createXML);

        Assert.assertNotNull(rubricsText);
    }

    public void createXML(String name) {
        String id = UUID.randomUUID().toString();
        String xml = "<category ID=\"" + id + "\">\n"
                + "  <label xml:lang=\"de\" text=\"" + name + "\" />\n"
                + "</category>";
        System.out.println(xml);
    }
}
