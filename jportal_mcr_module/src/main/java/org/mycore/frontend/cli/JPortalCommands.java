package org.mycore.frontend.cli;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.metadata.MCRBase;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.cli.annotation.MCRCommand;
import org.mycore.frontend.cli.annotation.MCRCommandGroup;
import org.mycore.frontend.util.DerivateLinkUtil;
import org.xml.sax.SAXException;

import fsu.jportal.backend.DerivateTools;

@MCRCommandGroup(name = "JP Commands")
public class JPortalCommands {

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

    @MCRCommand(help = "Rename file in derivate: rename derivID_1:/path/to/fileName newFileName", syntax = "rename {0} {1}")
    public static void renameFileInIFS(String file, String name) throws Exception {
        DerivateTools.rename(file, name);
    }

    @MCRCommand(help = "Copy file in derivate: copy derivID_1:/path/to/source derivID_2:/path/to/target", syntax = "copy file {0} {1}")
    public static void copyFile(String oldFile, String newFile) {
        DerivateTools.cp(oldFile, newFile);
    }

    @MCRCommand(help = "Copy object", syntax = "copy object {0}")
    public static void copyObject(String id) throws Exception {
        MCRObjectID mcrId = MCRObjectID.getInstance(id);
        MCRBase object = MCRMetadataManager.retrieve(mcrId);
        object.setId(MCRObjectID.getNextFreeId(mcrId.getBase()));
        if (object instanceof MCRObject) {
            MCRMetadataManager.create((MCRObject) object);
        } else {
            MCRMetadataManager.create((MCRDerivate) object);
        }
    }

    @MCRCommand(help = "Move file in derivate: move derivID_1:/path/to/source derivID_2:/path/to/target", syntax = "move {0} {1}")
    public static void move(String oldFile, String newFile) {
        DerivateTools.mv(oldFile, newFile);
    }

    @MCRCommand(help = "Create directories in derivate: mkir derivID:/path/to/newDir", syntax = "mkdir {0}")
    public static void mkdir(String newDir) {
        DerivateTools.mkdir(newDir);
    }
}
