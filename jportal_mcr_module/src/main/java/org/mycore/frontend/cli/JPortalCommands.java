package org.mycore.frontend.cli;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.ifs.MCRDirectory;
import org.mycore.datamodel.ifs.MCRFileMetadataManager;
import org.mycore.datamodel.ifs.MCRFilesystemNode;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetaIFS;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.cli.annotation.MCRCommand;
import org.mycore.frontend.cli.annotation.MCRCommandGroup;
import org.mycore.frontend.util.DerivateLinkUtil;
import org.xml.sax.SAXException;

@MCRCommandGroup(name = "JP Commands")
public class JPortalCommands {

    public static class FileLocation {
        static final Pattern locationPattern = Pattern.compile("/(jportal_\\w*_[0-9]{1,8})((/.*)*/(.*)$)?");

        String ownerID;

        String path;
        
        String fileName;

        public FileLocation(String oldFile) {
            parseOwnerIDAndPath(oldFile);
        }

        private void parseOwnerIDAndPath(String oldFile) {
            Matcher locationMatcher = locationPattern.matcher(oldFile);
            while (locationMatcher.find()) {
                ownerID = locationMatcher.group(1);
                path = locationMatcher.group(2);
                fileName = locationMatcher.group(4);
            }
        }

        public String getOwnerID() {
            return ownerID;
        }

        public String getPath() {
            return path;
        }

        public String getFileName() {
            return fileName;
        }

    }

    private static Logger LOGGER = Logger.getLogger(JPortalCommands.class.getName());

    @MCRCommand(help = "Export object XML with id to file: export object {id} to file {name}.", syntax = "export object {0} to file {1}")
    public static void exportBlob(String objectID, String file) throws SAXException, JDOMException, IOException {
        Document objXML = MCRXMLMetadataManager.instance().retrieveXML(MCRObjectID.getInstance(objectID));
        XMLOutputter xo = new XMLOutputter(Format.getPrettyFormat());
        try {
            xo.output(objXML, new FileOutputStream(new File(file)));
            LOGGER.info("exported blob of object " + objectID + " to " + file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @MCRCommand(help = "Import object XML from file: import object from file {name}", syntax = "import object from file {0}")
    public static void importBlob(String file) {
        Document objXML = null;
        try {
            objXML = new SAXBuilder().build(new File(file));
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String id = objXML.getRootElement().getAttributeValue("ID");
        MCRXMLMetadataManager.instance().update(MCRObjectID.getInstance(id), objXML, new Date());
        LOGGER.info("imported object " + id + " to blob from " + file);
    }

    @MCRCommand(help = "Add derivate link to object: add derivate link {location} to object {id}", syntax = "add derivate link {0} to object {1}")
    public static void addDerivateLink(String link, String id) throws MCRActiveLinkException {
        MCRObjectID mcrId = MCRObjectID.getInstance(id);
        DerivateLinkUtil.setLink(mcrId, link);
    }

    @MCRCommand(help = "Rename file in derivate: mv {oldName} {newName}", syntax = "mv {0} {1}")
    public static void renameFileInIFS(String oldFile, String newFile) {
        // check if oldName, newName or derivId not null and not empty ""
        oldFile = oldFile.trim();
        newFile = newFile.trim();

        FileLocation oldFileLocation = new FileLocation(oldFile);
        FileLocation newFileLocation = new FileLocation(newFile);
        
        String oldFileName = oldFileLocation.getFileName();
        String newFileName = newFileLocation.getFileName();
        String oldDerivId = oldFileLocation.getOwnerID();
        String newDerivId = newFileLocation.getOwnerID();
        
        if(!oldDerivId.equals(newDerivId)){
            MCRDirectory oldRootNode = (MCRDirectory) MCRFilesystemNode.getRootNode(oldDerivId);
            MCRDirectory newRootNode = (MCRDirectory) MCRFilesystemNode.getRootNode(newDerivId);
            MCRFilesystemNode file = oldRootNode.getChildByPath(oldFileLocation.getPath());
            file.move(newRootNode);
            LOGGER.info("Moved file " + oldFile + " to " + newFile);
        } else if(oldDerivId.equals(newDerivId) && !newFileName.equals(oldFileName)){
            MCRDirectory rootNode = (MCRDirectory) MCRFilesystemNode.getRootNode(oldDerivId);
            MCRFilesystemNode file = rootNode.getChildByPath(oldFileLocation.getPath());

            file.setName(newFileName);
            MCRFileMetadataManager.instance().storeNode(file);
            LOGGER.info("Renamed file " + oldFile + " to " + newFile);

            MCRObjectID mcrid = MCRObjectID.getInstance(oldDerivId);
            MCRDerivate der = MCRMetadataManager.retrieveMCRDerivate(mcrid);
            MCRMetaIFS internals = der.getDerivate().getInternals();
            String mainDoc = internals.getMainDoc();
            if (oldFileName.equals(mainDoc)) {
                internals.setMainDoc(newFileName);
                MCRMetadataManager.updateMCRDerivateXML(der);
                LOGGER.info("Setted maindoc " + mainDoc + " to " + newFile);
            }   
        }else{
            LOGGER.info("New file name " + newFile + " equals old file name " + oldFileName + ", nothing to do.");
        }
    }
}
