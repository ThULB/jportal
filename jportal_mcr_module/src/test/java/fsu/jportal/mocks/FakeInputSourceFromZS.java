package fsu.jportal.mocks;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import fsu.jportal.xml.stream.XMLStreamReaderUtils;

/**
 * Created by chi on 29.09.16.
 *
 * @author Huu Chi Vu
 */
public class FakeInputSourceFromZS {
    public static Optional<InputStream> inputStreamFromThULBPath(String path) {
        try {
            URL url = new URL("http://zs.thulb.uni-jena.de" + path + "?XSL.Style=xml");
            return Optional.of(url.openConnection().getInputStream());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public static Optional<InputStream> cachedInputStreamFromServer(InputStream serverIS, String cachePath) {
        try {
            Path cacheFolder = Paths.get(cachePath);

            Supplier<String> filePathSup = () -> cachePath.startsWith("/") ?
                    cachePath.replaceFirst("/", "") :
                    cachePath;
            Path objXMLPath = cacheFolder.resolve(filePathSup.get() + ".xml");

            if (!Files.exists(objXMLPath)) {
                try (InputStream urlIS = serverIS) {
                    if (!Files.exists(cacheFolder)) {
                        Files.createDirectory(cacheFolder);
                    }

                    Path parentPath = objXMLPath.getParent();
                    if (!Files.exists(parentPath)) {
                        Files.createDirectories(parentPath);
                    }
                    Files.copy(urlIS, objXMLPath);
                }
            }

            return Optional.of(Files.newInputStream(objXMLPath));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public static Optional<XMLStreamReader> xmlStreamReaderFromIS(InputStream is) {
        XMLInputFactory factory = XMLInputFactory.newFactory();
        try {
            return Optional.of(factory.createXMLStreamReader(is));
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    public static Function<String, Optional<XMLStreamReader>> getReaderFor() {
        return id -> getReaderFor(id);
    }

    public static Optional<XMLStreamReader> getReaderFor(String id) {
        String path = id.startsWith("jportal_derivate_") ? "/servlets/MCRFileNodeServlet/" : "/receive/";
        return getXmlStreamReader(id, path);
    }

    public static Stream<DerivateFileConsumer> getDerivateWith(String id) {
        return getXmlStreamReader(id, "/servlets/MCRFileNodeServlet/")
                .map(XMLStreamReaderUtils::toStream)
                .orElse(Stream.empty())
                .map(DerivateFileCollector::toList)
                .flatMap(List::stream);
    }

    private static Optional<XMLStreamReader> getXmlStreamReader(String id, String path) {
        return inputStreamFromThULBPath(path + id)
                .map(FakeInputSourceFromZS::xmlStreamReaderFromIS)
                .orElseGet(Optional::empty);
    }
}
