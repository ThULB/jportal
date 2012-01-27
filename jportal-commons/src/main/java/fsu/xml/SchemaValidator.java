package fsu.xml;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.util.JAXBSource;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.SAXException;

public class SchemaValidator {
    
    private StreamSource schemaSource;

    public SchemaValidator(String schemaLocation) {
        schemaSource = getSchemaSource(schemaLocation);
    }

    private StreamSource getSchemaSource(String schemaLocation) {
        InputStream datamodelPersonStream = getClass().getResourceAsStream(schemaLocation);
        StreamSource streamSource = new StreamSource(datamodelPersonStream);
        return streamSource;
    }

    private Schema createSchema(Source datamodellSource) throws SAXException {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        schemaFactory.setResourceResolver(new ClathPathResourceResolver());
        Schema schema = schemaFactory.newSchema(datamodellSource);
        return schema;
    }

    public void validate(Source source) throws SAXException, IOException, JAXBException {
            Schema schema = createSchema(schemaSource);
            Validator validator = schema.newValidator();
            validator.validate(source);
    }
}