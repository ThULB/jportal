package spike;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

/**
 * Reads a csv file from piwik and adds all journal title's.
 * 
 * @author Matthias Eichner
 */
public class PiwikCSVEnhancer {

    public void enhance(String filePath) throws IOException {
        String result = null;
        try (Stream<String> lines = Files.lines(Paths.get(filePath), Charset.defaultCharset())) {
            result = lines.map(line -> processLine(line)).collect(Collectors.joining("\n"));
        }
        write(filePath, result);
    }

    public void write(String filePath, String data) throws IOException {
        InputStream is = new ByteArrayInputStream(data.getBytes(Charset.defaultCharset()));
        Files.copy(is, Paths.get(filePath), StandardCopyOption.REPLACE_EXISTING);
    }

    private String processLine(String line) {
        try {
            if (!line.startsWith("jportal_jpjournal")) {
                return line;
            }
            String[] lineParts = line.split(",");
            String journalId = lineParts[0];

            URL url = new URL("http://zs.thulb.uni-jena.de/receive/" + journalId + "?XSL.Style=xml");
            URLConnection connection = url.openConnection();

            SAXBuilder b = new SAXBuilder();
            Document xml = b.build(connection.getInputStream());
            Element root = xml.getRootElement();
            String title = root.getChild("metadata").getChild("maintitles").getChild("maintitle").getText();
            return line + ",\"" + title + "\"";
        } catch (Exception exc) {
            exc.printStackTrace();
            return line;
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            out("missing argument [path to csv file]");
            return;
        }
        PiwikCSVEnhancer piwikCSVEnhancer = new PiwikCSVEnhancer();
        piwikCSVEnhancer.enhance(args[0]);
    }

    public static void out(String msg) {
        System.out.println(msg);
    }

}
