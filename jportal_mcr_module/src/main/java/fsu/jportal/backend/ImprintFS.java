package fsu.jportal.backend;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.transform.JDOMSource;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.common.content.MCRContent;

import com.google.common.base.Charsets;

public class ImprintFS {

    private static final Logger LOGGER = LogManager.getLogger(ImprintFS.class);

    private final Path IMPRINT_DIR;

    private final String fsType;

//    static {
//        String baseDir = MCRConfiguration.instance().getString("JP.imprint.baseDir", "/data/imprint");
//        IMPRINT_DIR = Paths.get(baseDir);
//        if(!Files.exists(IMPRINT_DIR)) {
//            try {
//                Files.createDirectories(IMPRINT_DIR);
//            } catch(Exception exc) {
//                LOGGER.error("while creating import directory " + IMPRINT_DIR, exc);
//            }
//        }
//    }
    
    public ImprintFS(String fsType) {
        this.fsType = fsType;
        String baseDir = MCRConfiguration.instance().getString("JP."+ fsType + ".baseDir", "/data/" + fsType);
        IMPRINT_DIR = Paths.get(baseDir);
        if(!Files.exists(IMPRINT_DIR)) {
            try {
                Files.createDirectories(IMPRINT_DIR);
            } catch(Exception exc) {
                LOGGER.error("while creating import directory " + IMPRINT_DIR, exc);
            }
        }
    }

    /**
     * Returns a list of all imprint in source folder.
     * 
     * @return
     */
    public List<String> list() throws IOException {
        final List<String> imprintList = new ArrayList<>();
        Files.walkFileTree(IMPRINT_DIR, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if(attrs.isRegularFile() && file.getFileName().toString().endsWith(".xml")) {
                    imprintList.add(getIDFromPath(file));
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return imprintList;
    }

    public void store(String imprintID, MCRContent content) throws IOException {
        Path storePath = getPath(imprintID);
        try (BufferedWriter writer = Files.newBufferedWriter(storePath, Charsets.UTF_8)) {
            writer.write(content.asString());
        }
    }

    public JDOMSource receive(String imprintID) throws IOException, JDOMException {
        SAXBuilder builder = new SAXBuilder();
        if (imprintID.equals("master") || imprintID.equals("")){
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("META-INF/resources/jp-imprint.xml");
            return new JDOMSource(builder.build(is));
        }
        Path receivePath = getPath(imprintID);
        try (BufferedReader reader = Files.newBufferedReader(receivePath, Charsets.UTF_8)) {
            StringBuffer buf = new StringBuffer();
            String line = null;
            while ((line = reader.readLine()) != null) {
                buf.append(line);
            }
            return new JDOMSource(builder.build(new StringReader(buf.toString())));
        }
    }

    public void delete(String imprintID) throws IOException {
        Path filePath = getPath(imprintID);
        Files.delete(filePath);
    }

    protected Path getPath(String imprintID) {
        return IMPRINT_DIR.resolve(imprintID + ".xml");
    }

    protected String getIDFromPath(Path path) {
        String filename = path.getFileName().toString();
        return filename.substring(0, filename.lastIndexOf(".xml"));
    }

}