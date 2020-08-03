package fsu.jportal.mocks;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static fsu.jportal.xml.stream.XMLStreamReaderUtils.toStream;

/**
 * Created by chi on 29.09.16.
 * @author Huu Chi Vu
 */
public class DerivateFileCollector {
    String mimeType;

    String uri;

    XMLStreamReader reader;

    private DerivateFileCollector(XMLStreamReader reader) {
        this.reader = reader;
    }

    public static List<DerivateFileConsumer> toList(XMLStreamReader reader) {
        Predicate<XMLStreamReader> childFilter = r ->
                r.isStartElement()
                        && r.getLocalName().equals("child")
                        && r.getAttributeValue(null, "type").equals("file");

        return toStream(reader)
                                   .filter(childFilter)
                                   .map(DerivateFileCollector::new)
                                   .map(DerivateFileCollector::getMimeType)
                                   .map(DerivateFileCollector::getUri)
                                   .map(DerivateFileCollector::createConsumer)
                                   .collect(Collectors.toList());
    }

    private DerivateFileCollector getMimeType() {
        getElementText("contentType").ifPresent(t -> mimeType = t);
        return this;
    }

    private DerivateFileCollector getUri() {
        getElementText("uri").ifPresent(t -> uri = t);
        return this;
    }

    public DerivateFileConsumer createConsumer() {
        if (mimeType == null || uri == null) {
            return cons -> doNothing -> {/* nothing */};
        }

        return cons -> cons.apply(mimeType, uri);
    }

    private Optional<String> getElementText(String type) {
        Predicate<XMLStreamReader> contentTypeFilter = r ->
                r.isStartElement() && r.getLocalName().equals(type);

        return toStream(reader)
                                   .filter(contentTypeFilter)
                                   .findFirst()
                                   .map(DerivateFileCollector::getElementText);
    }

    private static String getElementText(XMLStreamReader r) {
        try {
            return r.getElementText();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }

        return null;
    }
}
