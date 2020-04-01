package fsu.thulb.xml;

import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 * Created by chi on 2020-02-24
 *
 * @author Huu Chi Vu
 */
public class JAXBTools {

    private JAXBContext context;

    private JAXBTools(JAXBContext context) {
        this.context = context;
    }

    public static JAXBTools newInstance(Class<?>... classes) throws JAXBException {
        return new JAXBTools(JAXBContext.newInstance(classes));
    }

    public Marshaller getMarshaller() throws JAXBException {
        Marshaller marshaller = getContext().createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        return marshaller;
    }

    public Unmarshaller getUnMarshaller() throws JAXBException {
        return getContext().createUnmarshaller();
    }

    public void marshall(Object jaxbElement, Writer writer) throws JAXBException {
        getMarshaller().marshal(jaxbElement, writer);
    }

    public void marshall(Object jaxbElement, OutputStream os) throws JAXBException {
        getMarshaller().marshal(jaxbElement, os);
    }

    public String asString(Object jaxbElement) throws JAXBException {
        StringWriter stringWriter = new StringWriter();
        marshall(jaxbElement, stringWriter);
        return stringWriter.toString();
    }

    private JAXBContext getContext() {
        return context;
    }
}
