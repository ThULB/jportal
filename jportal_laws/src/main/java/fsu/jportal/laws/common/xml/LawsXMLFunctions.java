package fsu.jportal.laws.common.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.mycore.datamodel.ifs.MCRDirectory;
import org.mycore.datamodel.ifs.MCRFile;
import org.mycore.datamodel.ifs.MCRFileMetadataManager;
import org.mycore.datamodel.ifs.MCRFilesystemNode;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetaIFS;
import org.mycore.datamodel.metadata.MCRMetaLinkID;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public abstract class LawsXMLFunctions {

    private static final Logger LOGGER = Logger.getLogger(LawsXMLFunctions.class);

    private static final ThreadLocal<DocumentBuilder> BUILDER_LOCAL = new ThreadLocal<DocumentBuilder>() {
        @Override
        protected DocumentBuilder initialValue() {
            try {
                return DocumentBuilderFactory.newInstance().newDocumentBuilder();
            } catch(ParserConfigurationException pce) {
                LOGGER.error("Unable to create document builder", pce);
                return null;
            }
        }
    };

    public static Document getRegister(String objectId) {
        if(objectId == null) {
            return null;
        }
        try {
            MCRObject mcrObj = MCRMetadataManager.retrieveMCRObject(MCRObjectID.getInstance(objectId));
            List<MCRMetaLinkID> metaLinkList = mcrObj.getStructure().getDerivates();
            for(MCRMetaLinkID derLink : metaLinkList) {
                String derId = derLink.getXLinkHref();
                MCRDerivate derivate = MCRMetadataManager.retrieveMCRDerivate(MCRObjectID.getInstance(derId));
                MCRMetaIFS metaIFS = derivate.getDerivate().getInternals();
                String mainDoc = metaIFS.getMainDoc();
                // assume that a xml file is always the register xml
                if(mainDoc.toLowerCase().endsWith(".xml")) {
                    MCRFilesystemNode xmlFile = MCRFileMetadataManager.instance().retrieveChild(metaIFS.getIFSID(), mainDoc);
                    if(xmlFile instanceof MCRFile) {
                        InputStream is = ((MCRFile)xmlFile).getContentAsInputStream();
                        return BUILDER_LOCAL.get().parse(is);
                    }
                }
            }
        } catch(Exception exc) {
            LOGGER.error("while retrieving register", exc);
        }
        return null;
    }

    public static String getImageDerivate(String objectId) {
        if(objectId == null) {
            return null;
        }
        MCRObject mcrObj = MCRMetadataManager.retrieveMCRObject(MCRObjectID.getInstance(objectId));
        List<MCRMetaLinkID> metaLinkList = mcrObj.getStructure().getDerivates();
        for(MCRMetaLinkID derLink : metaLinkList) {
            String derId = derLink.getXLinkHref();
            MCRDerivate derivate = MCRMetadataManager.retrieveMCRDerivate(MCRObjectID.getInstance(derId));
            MCRMetaIFS metaIFS = derivate.getDerivate().getInternals();
            String mainDoc = metaIFS.getMainDoc();
            // assume that a tif file is always
            if(mainDoc.toLowerCase().endsWith(".tif") || mainDoc.toLowerCase().endsWith(".tiff")) {
                return derId;
            }
        }
        return null;
    }


    public static String getImageByLaw(String numberOfLaw, String derivateId) {
        // check null and empty
        if(numberOfLaw == null || numberOfLaw.equals("")) {
            LOGGER.warn("Lawnumber is null or empty");
            return null;
        }
        if(derivateId == null || derivateId.equals("")) {
            LOGGER.warn("Derivate id is null or empty");
            return null;
        }
        // get law number as integer
        int number;
        try {
            number = Integer.parseInt(numberOfLaw);
        } catch(NumberFormatException nfe) {
            LOGGER.warn("while parsing law number " + numberOfLaw, nfe);
            return null;
        }
        // get files
        MCRDirectory dir = MCRDirectory.getRootDirectory(derivateId);
        if(dir == null) {
            LOGGER.warn("Unable to get diretory of derivate " + derivateId);
            return null;
        }
        return getImageByNumber(dir, number);
    }

    /**
     * Internal method to get a image by number. Number is always the first
     * part of the image name e.g. <b>004</b>_HZA_1821_T_001.tif.
     * 
     * @param parent parent directory
     * @param number number to find
     * @return name of the image
     */
    private static String getImageByNumber(MCRDirectory parent, int number) {
        MCRFilesystemNode[] children = parent.getChildren();
        for(MCRFilesystemNode node : children) {
            if (node instanceof MCRDirectory) {
                String fileName = getImageByNumber((MCRDirectory)node, number);
                if(fileName != null)
                    return fileName;
            } else {
                try {
                    String fileName = node.getName();
                    String numberPart = fileName.split("_")[3];
                    numberPart = numberPart.substring(3);
                    int compareNumber = Integer.parseInt(numberPart);
                    if(number == compareNumber)
                        return fileName;
                } catch(Exception exc) {
                    continue;
                }
            }
        }
        return null;
    }

}
