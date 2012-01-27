/**
 * 
 */
package fsu.thulb.jaxb;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Node;

public class JaxbTools {
    private static <T> JAXBContext newContext(Class<T> clazz) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
        return jaxbContext;
    }

    public static <T> T unmarschall(InputStream inputStream, Class<T> clazz) throws JAXBException {
        T unmarschalledObj = (T) newContext(clazz).createUnmarshaller().unmarshal(inputStream);
        return unmarschalledObj;
    }

    public static <T> T unmarschall(DOMSource source, Class<T> clazz) throws JAXBException {
        return unmarschall(source.getNode(), clazz);
    }

    public static <T> T unmarschall(Node node, Class<T> clazz) throws JAXBException {
        T unmarschalledObj = (T) newContext(clazz).createUnmarshaller().unmarshal(node);
        return unmarschalledObj;
    }
    
    public static void marschall(Object obj, OutputStream os) throws JAXBException {
        createMarshaller(obj).marshal(obj, os);
    }

    public static Marshaller createMarshaller(Object obj) throws JAXBException, PropertyException {
        Marshaller marshaller = newContext(obj.getClass()).createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        return marshaller;
    }

    public static void marschall(Object obj, File file) throws PropertyException, JAXBException {
        createMarshaller(obj).marshal(obj, file);
    }

    public static <T> T unmarschall(URL url, Class<T> clazz) throws JAXBException {
        T unmarschalledObj = (T) newContext(clazz).createUnmarshaller().unmarshal(url);
        return unmarschalledObj;
    }

}