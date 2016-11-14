package fsu.jportal.mocks;

import fsu.jportal.xml.stream.DerivateFileInfo;
import spike.mets2.Transformer;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static fsu.jportal.xml.JPMCRObjXMLElementName.child;

/**
 * Created by chi on 14.11.16.
 * @author Huu Chi Vu
 */ // Mockups
public class DerivateXMLToParsedData implements Transformer<XMLStreamReader, DerivateFileInfo> {

    @Override
    public DerivateFileInfo apply(XMLStreamReader reader) {
        String mimeType = null;
        String fileName = null;
        String href = null;

        while (!(reader.isEndElement() && reader.getLocalName()
                                                .equals(child.toString()))) {
            try {
                reader.next();
                if (reader.isStartElement() && reader.getLocalName()
                                                     .equals("contentType")) {
                    mimeType = reader.getElementText();
                }

                if (reader.isStartElement() && reader.getLocalName()
                                                     .equals("name")) {
                    fileName = reader.getElementText();
                }

                if (reader.isStartElement() && reader.getLocalName()
                                                     .equals("uri")) {
                    href = reader.getElementText();
                }
            } catch (XMLStreamException e) {
                e.printStackTrace();
            }
        }
        return new DerivateFileInfo(mimeType, fileName, href);
    }

    @Override
    public boolean test(XMLStreamReader reader) {
        return reader.isStartElement()
                && reader.getLocalName()
                         .equals(child.toString())
                && reader.getAttributeValue(null, "type")
                         .equals("file");
    }

}
