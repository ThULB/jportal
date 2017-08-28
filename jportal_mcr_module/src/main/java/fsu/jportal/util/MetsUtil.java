package fsu.jportal.util;

import fsu.jportal.mets.ALTOMETSHierarchyGenerator;
import fsu.jportal.mets.MetsVersionStore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.mycore.common.MCRConstants;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.content.MCRPathContent;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.niofs.MCRContentTypes;
import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.mets.model.Mets;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Stream;

/**
 * Util class for mets.xml handling.
 * 
 * @author Matthias Eichner
 */
public abstract class MetsUtil {

    private static Logger LOGGER = LogManager.getLogger(MetsUtil.class);

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

    /**
     * Checks if this derivate can have a generated mets.xml. The following conditions
     * have to be true:
     * 
     * <ul>
     * <li>the derivate does exist</li>
     * <li>the owner object does exist</li>
     * <li>the owner object has at least one child</li>
     * <li>the derivate contains at least one image</li>
     * <li>the derivate doesn't have a mets.xml OR the mets.xml was generated too</li>
     * </ul>
     * 
     * @param id the mycore object id to check
     * @return true if a mets.xml can be generated for this derivate
     * @throws IOException the derivate files couldn't be read
     * @throws JDOMException when the mets.xml exists but couldn't be parsed with jdom
     */
    public static boolean isGeneratable(MCRObjectID id) throws IOException, JDOMException {
        // check derivate exists
        if (!MCRMetadataManager.exists(id)) {
            return false;
        }
        MCRDerivate derivate = MCRMetadataManager.retrieveMCRDerivate(id);
        MCRObjectID objId = derivate.getOwnerID();
        // check owner exists
        if (!MCRMetadataManager.exists(objId)) {
            return false;
        }
        MCRObject object = MCRMetadataManager.retrieveMCRObject(objId);
        // check children not empty
        if (object.getStructure().getChildren().isEmpty()) {
            return false;
        }
        // check contains at least one image
        try (Stream<Path> stream = Files.walk(MCRPath.getPath(id.toString(), "/"))) {
            if (!stream.anyMatch(path -> {
                try {
                    String probeContentType = MCRContentTypes.probeContentType(path);
                    return probeContentType.startsWith("image/");
                } catch (Exception exc) {
                    LOGGER.warn("Unable to probe content of " + path.toAbsolutePath().toString()
                        + " while checking for mets.xml generation.");
                    return false;
                }
            })) {
                return false;
            }
        }
        return true;
    }

    /**
     * Generates a new mets.xml for the given derivate. The mets.xml is just
     * generated and not stored!
     * 
     * @see ALTOMETSHierarchyGenerator
     * @param derivateId the derivate to generate the mets.xml for
     * @return the new mets.xml
     * @throws IOException mets.xml couldn't be generated due I/O error
     */
    public static Mets generate(MCRObjectID derivateId) throws IOException {
        // get old mets
        Mets oldMets;
        try {
            oldMets = MetsUtil.getMets(derivateId.toString());
        } catch (Exception fnfe) {
            oldMets = null;
        }
        // generate
        return new ALTOMETSHierarchyGenerator(oldMets).getMETS(MCRPath.getPath(derivateId.toString(), "/"),
            new HashSet<MCRPath>());
    }

    /**
     * Does the same as {@link #generate(MCRObjectID)} but stores the old mets.xml
     * in the {@link MetsVersionStore} and replace the old one with the new one.
     * 
     * @param derivateId the derivate to generate the mets.xml for
     * @throws IOException mets.xml couldn't be generated due I/O error
     */
    public static void generateAndReplace(MCRObjectID derivateId) throws IOException {
        // path to mets.xml
        MCRPath metsPath = MCRPath.getPath(derivateId.toString(), "mets.xml");

        // generate
        Mets mets = MetsUtil.generate(derivateId);
        MCRJDOMContent newMetsContent = new MCRJDOMContent(mets.asDocument());

        // store old mets
        if (Files.exists(metsPath)) {
            MetsVersionStore.store(derivateId);
        }

        // replace
        Files.copy(newMetsContent.getInputStream(), metsPath, StandardCopyOption.REPLACE_EXISTING);
    }

}
