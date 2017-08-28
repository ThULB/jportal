package fsu.jportal.resolver;

import org.mycore.common.config.MCRConfiguration;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@URIResolverSchema(schema = "journalFile")
public class JournalFilesResolver implements URIResolver {

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        href = href.substring(href.indexOf(":") + 1);
        try {
            FileInputStream fis = stream(href);
            if (fis == null) {
                return null;
            }
            return fis != null ? new StreamSource(fis) : null;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static FileInputStream stream(String href) throws FileNotFoundException {
        Path path = getPath(href);
        if (path != null && Files.exists(path)) {
            return new FileInputStream(path.toFile());
        }
        return null;
    }

    public static Path getPath(String href) {
        String journalFileFolderPath = MCRConfiguration.instance().getString("JournalFileFolder");
        if (journalFileFolderPath != null) {
            return Paths.get(journalFileFolderPath, href);
        }
        return null;
    }
}
