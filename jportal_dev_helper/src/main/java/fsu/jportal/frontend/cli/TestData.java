package fsu.jportal.frontend.cli;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.ProviderNotFoundException;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.persistence.EntityManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filter;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.access.MCRAccessException;
import org.mycore.backend.jpa.MCREntityManagerProvider;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.cli.annotation.MCRCommand;
import org.mycore.frontend.cli.annotation.MCRCommandGroup;
import org.xml.sax.SAXParseException;

/**
 * Created by chi on 04.12.17.
 *
 * @author Huu Chi Vu
 */
@MCRCommandGroup(name = "JPortal Test Data")
public class TestData {
    static Logger LOGGER = LogManager.getLogger();

    static Document journalTMPDoc;

    static {
        InputStream journalIS = TestData.class.getClassLoader().getResourceAsStream("journal_tmp.xml");
        SAXBuilder saxBuilder = new SAXBuilder();
        try {
            journalTMPDoc = saxBuilder.build(journalIS);
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @MCRCommand(syntax = "create test data", help = "create test data")
    public static void createTestData() {
        LOGGER.info("Create Test Data!");

        removeURN();

        Stream.of("derivate", "jparticle", "jpvolume", "jpjournal", "person", "jpinst")
              .map(MCRXMLMetadataManager.instance()::listIDsOfType)
              .flatMap(Collection::stream)
              .map(MCRObjectID::getInstance)
              .forEach(TestData::delete);

        InputStream personTmp = TestData.class.getClassLoader().getResourceAsStream("person_tmp.xml");

        try {
            int read = personTmp.read();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while (read != -1) {
                baos.write(read);
                read = personTmp.read();
            }
            personTmp.close();

            MCRObject mcrObject = new MCRObject(baos.toByteArray(), false);
            MCRMetadataManager.update(mcrObject);
            Enumeration<URL> templates = TestData.class.getClassLoader()
                                                       .getResources("META-INF/resources/jp_templates");

            Collections.list(templates)
                       .stream()
                       .map(uri -> uriToPath(uri))
                       .map(wrapper(path -> Files.newDirectoryStream(path)))
                       .flatMap(ds -> StreamSupport.stream(ds.spliterator(), false))
                       .map(Path::getFileName)
                       .map(Path::toString)
                       .map(t -> t.replaceAll("/", ""))
                       .forEach(TestData::createJournal);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (MCRAccessException e) {
            e.printStackTrace();
        } catch (SAXParseException e) {
            e.printStackTrace();
        }
    }

    private static void removeURN() {
        EntityManager em = MCREntityManagerProvider.getCurrentEntityManager();
        int i = em.createQuery("delete from MCRPI pi where pi.service = 'DNBURNGranular'")
                  .executeUpdate();

        LOGGER.info("Deleted URNs: " + i);
    }

    private static void createJournal(String templateName) {
        String journalID = MCRObjectID.getNextFreeId("jportal_jpjournal").toString();
        String maintitle = templateName.substring(9);
        Document currentJournal = journalTMPDoc.clone();
        Filter<Element> elementFilter = Filters.element();
        Filter<Attribute> attributeFilter = Filters.attribute();
        LOGGER.info("Creating journal with template " + templateName + ", journalID: " + journalID +  " .....");

        getXpath("/mycoreobject/@ID", attributeFilter, currentJournal)
                .ifPresent(m -> m.setValue(journalID));

        getXpath("/mycoreobject/@label", attributeFilter, currentJournal)
                .ifPresent(m -> m.setValue(journalID));

        getXpath("/mycoreobject/metadata/hidden_jpjournalsID/hidden_jpjournalID", elementFilter, currentJournal)
                .ifPresent(m -> m.setText(journalID));

        getXpath("/mycoreobject/metadata/maintitles/maintitle", elementFilter, currentJournal)
                .ifPresent(m -> m.setText(maintitle));

        getXpath("/mycoreobject/metadata/hidden_templates/hidden_template", elementFilter, currentJournal)
                .ifPresent(m -> m.setText(templateName));

        MCRObject mcrObject = new MCRObject(currentJournal);
        try {
            MCRMetadataManager.update(mcrObject);
        } catch (MCRAccessException e) {
            e.printStackTrace();
        }

        LOGGER.info("Done.");
    }

    private static <T> Optional<T> getXpath(String xpathStr, Filter<T> filter,
                                            Document doc) {
        XPathExpression<T> maintitle = XPathFactory.instance()
                                                   .compile(xpathStr, filter);
        return Optional.ofNullable(maintitle.evaluateFirst(doc));
    }

    private static void delete(MCRObjectID id) {
        try {
            MCRMetadataManager.deleteMCRObject(id);
        } catch (MCRActiveLinkException | MCRAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static Path uriToPath(URL url) {
        if (url.getProtocol().equals("file")) {
            try {
                return Paths.get(url.toURI());
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }

        String[] splittedURL = url.toString().split("!");
        String jarFile = splittedURL[0];
        String jarFolder = splittedURL[1];
        Map<String, String> env = new HashMap<>();
        URI fsURL = URI.create(jarFile);
        try {
            FileSystem fs = FileSystems.getFileSystem(fsURL);
            if (!fs.isOpen()) {
                return FileSystems.newFileSystem(fsURL, env).getPath(jarFolder);
            } else {
                return fs.getPath(jarFolder);
            }

        } catch (IOException | ProviderNotFoundException | FileSystemNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @FunctionalInterface
    public interface FunctionWithException<T, R, E extends Exception> {
        R apply(T t) throws E;
    }

    private static <T, R, E extends Exception>
    Function<T, R> wrapper(FunctionWithException<T, R, E> fe) {
        return arg -> {
            try {
                return fe.apply(arg);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }
}
