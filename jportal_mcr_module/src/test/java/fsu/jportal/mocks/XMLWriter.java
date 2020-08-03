package fsu.jportal.mocks;

import java.util.Optional;
import java.util.function.Consumer;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * Created by chi on 09.09.16.
 * @author Huu Chi Vu
 */
public class XMLWriter {
    final XMLStreamWriter writer;

    private XMLWriter(XMLStreamWriter writer) {
        this.writer = writer;
    }

    public static XMLWriter use(XMLStreamWriter writer) {
        return new XMLWriter(writer);
    }

    public XMLWriter startDoc() {
        try {
            writer.writeStartDocument();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }

        return this;
    }

    public XMLWriter add(Consumer<XMLWriter> w) {
        w.accept(this);
        return this;
    }

    public XMLWriter endDoc() {
        try {
            writer.writeEndDocument();
            writer.flush();
            writer.close();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }

        return this;
    }

    public XMLWriter element(String name) {
        return element(name, doNothing());
    }

    public XMLWriter element(String name, Consumer<XMLWriter> w) {
        try {
            writer.writeStartElement(name);
            w.accept(this);
            writer.writeEndElement();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        return this;
    }

    public XMLWriter element(String prefix, String name) {
        return element(prefix, name, doNothing());
    }

    public XMLWriter element(String prefix, String name, Consumer<XMLWriter> w) {
        String uri = writer.getNamespaceContext().getNamespaceURI(prefix);
        try {
            writer.writeStartElement(uri, name);
            writer.writeNamespace(prefix, uri);
            w.accept(this);
            writer.writeEndElement();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        return this;
    }

    public XMLWriter attribute(String name, Optional<String> value) {
        return value.map(v -> attribute(name, v)).orElse(this);
    }

    public XMLWriter attribute(String prefix, String name, String value) {
        String uri = writer.getNamespaceContext().getNamespaceURI(prefix);
        try {
            writer.writeAttribute(uri, name, value);
            writer.writeNamespace(prefix, uri);
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        return this;
    }

    public XMLWriter attribute(String name, String value) {
        try {
            writer.writeAttribute(name, value);
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        return this;
    }

    public XMLWriter prefix(String prefix, String uri) {
        try {
            writer.setPrefix(prefix, uri);
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        return this;
    }

    public XMLWriter text(Optional<String> text) {
        return text.map(this::text).orElse(this);
    }

    public XMLWriter text(String text) {
        try {
            writer.writeCharacters(text);
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        return this;
    }

    private Consumer<XMLWriter> doNothing() {
        return nothing -> {
        };
    }
}
