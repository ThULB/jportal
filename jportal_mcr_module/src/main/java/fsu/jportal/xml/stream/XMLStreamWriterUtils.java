package fsu.jportal.xml.stream;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Created by chi on 05.10.16.
 */
public class XMLStreamWriterUtils {
    public static Consumer<XMLStreamWriter> document(Consumer<XMLStreamWriter>... xml) {
        return writer -> {
            try {
                writer.writeStartDocument();
                fragment(xml).accept(writer);
                writer.writeEndDocument();
            } catch (XMLStreamException e) {
                e.printStackTrace();
            }
        };
    }

    public static Consumer<XMLStreamWriter> namespace(String prefix, String uri) {
        return writer -> {
            try {
                writer.setPrefix(prefix, uri);
            } catch (XMLStreamException e) {
                e.printStackTrace();
            }
        };
    }

    public static Consumer<XMLStreamWriter> element(String name) {
        return element(name, noMoreElement -> {});
    }

    public static Consumer<XMLStreamWriter> element(String prefix, String name, Consumer<XMLStreamWriter>... xml) {

        return writer -> {
            try {
                String uri = writer.getNamespaceContext().getNamespaceURI(prefix);
                writer.writeStartElement(uri, name);
                getAttributePrefixes(xml)
                        .map(nameSpaceWriter)
                        .forEach(ns -> ns.accept(writer));
                writer.writeNamespace(prefix, uri);
                fragment(xml).accept(writer);
                writer.writeEndElement();
            } catch (XMLStreamException e) {
                e.printStackTrace();
            }
        };
    }

    public static Consumer<XMLStreamWriter> element(String name, Consumer<XMLStreamWriter>... xml) {
        return writer -> {
            try {
                writer.writeStartElement(name);
                getAttributePrefixes(xml)
                        .map(nameSpaceWriter)
                        .forEach(ns -> ns.accept(writer));
                fragment(xml).accept(writer);
                writer.writeEndElement();
            } catch (XMLStreamException e) {
                e.printStackTrace();
            }
        };
    }

    public static Function<String, Consumer<XMLStreamWriter>> nameSpaceWriter = prefix -> writer -> {
        String uri = writer.getNamespaceContext().getNamespaceURI(prefix);
        try {
            writer.writeNamespace(prefix, uri);
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
    };

    private static Stream<String> getAttributePrefixes(Consumer<XMLStreamWriter>... xml) {

        return Arrays.stream(xml)
                     .filter(Attribute.class::isInstance)
                     .map(Attribute.class::cast)
                     .map(Attribute::getPrefix)
                     .distinct();
    }

    private static class Attribute implements Consumer<XMLStreamWriter> {
        private final String prefix;
        private final Consumer<XMLStreamWriter> attributeConsumer;

        public Attribute(String prefix, Consumer<XMLStreamWriter> attributeConsumer) {
            this.attributeConsumer = attributeConsumer;
            this.prefix = prefix;
        }

        @Override
        public void accept(XMLStreamWriter xmlStreamWriter) {
            this.attributeConsumer.accept(xmlStreamWriter);
        }

        public String getPrefix() {
            return prefix;
        }
    }

    public static Consumer<XMLStreamWriter> attr(String prefix, String name, String value) {
        return new Attribute(prefix,
                writer -> {
                    try {
                        String uri = writer.getNamespaceContext().getNamespaceURI(prefix);
                        writer.writeAttribute(uri, name, value);
                    } catch (XMLStreamException e) {
                        e.printStackTrace();
                    }
                }
        );
    }

    public static Consumer<XMLStreamWriter> attr(String name, String value) {
        return writer -> {
            try {
                writer.writeAttribute(name, value);
            } catch (XMLStreamException e) {
                e.printStackTrace();
            }
        };
    }

    public static Consumer<XMLStreamWriter> text(String text) {
        return writer -> {
            try {
                writer.writeCharacters(text);
            } catch (XMLStreamException e) {
                e.printStackTrace();
            }
        };
    }

    public static Consumer<XMLStreamWriter> fragment(Consumer<XMLStreamWriter>... xml) {
        return Stream.of(xml)
                     .reduce(Consumer::andThen)
                     .orElse(noElement -> {});
    }
}
