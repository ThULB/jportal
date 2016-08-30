package fsu.jportal.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.mycore.common.MCRConstants;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRPathContent;
import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.mets.model.Mets;

/**
 * Util class for mets.xml handling.
 * 
 * @author Matthias Eichner
 */
public abstract class MetsUtil {

    public static final ArrayList<Namespace> METS_NS_LIST;

    static {
        METS_NS_LIST = new ArrayList<Namespace>();
        METS_NS_LIST.add(MCRConstants.METS_NAMESPACE);
        METS_NS_LIST.add(MCRConstants.MODS_NAMESPACE);
        METS_NS_LIST.add(MCRConstants.XLINK_NAMESPACE);
    }

    /**
     * Returns the mets.xml as java object.
     * 
     * @param derivateId the derivate where to get the mets.xml
     * @throws FileNotFoundException when there is no mets.xml
     * @throws IOException when the mets.xml couldn't be read by the io
     * @throws JDOMException when the file exists but couldn't be parsed with jdom
     * @return the mets.xml as java object
     */
    public static Mets getMets(String derivateId) throws IOException, JDOMException {
        return new Mets(getMetsXMLasDocument(derivateId));
    }

    /**
     * Returns the mets.xml as a jdom document object.
     *
     * @param derivateId the derivate where to get the mets.xml
     * @throws FileNotFoundException when there is no mets.xml
     * @throws IOException when the mets.xml couldn't be read by the io
     * @throws JDOMException when the file exists but couldn't be parsed with jdom
     * @return the mets.xml as jdom document
     */
    public static Document getMetsXMLasDocument(String derivateId) throws IOException, JDOMException {
        InputStream metsXMLStream = getMetsXMLasStream(derivateId);
        SAXBuilder builder = new SAXBuilder();
        try {
            return builder.build(metsXMLStream);
        } catch (JDOMException exc) {
            throw new JDOMException("Error while parsing mets.xml of " + derivateId, exc);
        }
    }

    /**
     * Returns the mets.xml as an input stream.
     * 
     * @throws FileNotFoundException when there is no mets.xml
     * @throws IOException when the mets.xml couldn't be read by the io
     * @param derivateId the derivate where to get the mets.xml
     * @return the mets.xml as {@link InputStream}
     */
    public static InputStream getMetsXMLasStream(String derivateId) throws IOException {
        MCRPath metsXML = MCRPath.getPath(derivateId, "/mets.xml");
        if (!Files.exists(metsXML)) {
            throw new FileNotFoundException("No mets.xml in this derivate " + derivateId);
        }
        try {
            MCRContent content = new MCRPathContent(metsXML);
            return content.getInputStream();
        } catch (Exception exc) {
            throw new IOException("Error while reading " + metsXML.toAbsolutePath().toString(), exc);
        }
    }

    /**
     * Checks the if the root element name is equals 'mets' and the
     * PROFILE is 'ENMAP'.
     * 
     * @param metsDocument the document to check
     */
    public static boolean isENMAP(Document metsDocument) {
        // check root element
        Element rootElement = metsDocument.getRootElement();
        if (!rootElement.getName().toLowerCase().equals("mets")) {
            return false;
        }
        // check profile
        String profile = rootElement.getAttributeValue("PROFILE");
        return profile != null && profile.equals("ENMAP");
    }

}
