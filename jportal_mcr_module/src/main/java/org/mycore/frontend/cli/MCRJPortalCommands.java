package org.mycore.frontend.cli;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.util.DerivateLinkUtil;

public class MCRJPortalCommands extends MCRAbstractCommands {

    private static Logger LOGGER = Logger.getLogger(MCRJPortalCommands.class.getName());

    public MCRJPortalCommands() {
        super();
        addCommand(new MCRCommand("export dbblob of object {0} to {1}", "org.mycore.frontend.cli.MCRJPortalCommands.exportBlob String String", ""));

        addCommand(new MCRCommand("import dbblob from {0}", "org.mycore.frontend.cli.MCRJPortalCommands.importBlob String", ""));

        addCommand(new MCRCommand("convert datamodel1 to datamodel2 from file {0}", "org.mycore.frontend.cli.command.MCRDatamodelToDatamodel2Command.convert String",
        "converts a datamodel 1 file to a new datamodel 2 one"));

        addCommand(new MCRCommand("convert ifs linking to derivate linking", "org.mycore.frontend.cli.command.MCRLinkConvertCommand.convert",
        "converts the old ifs linking of articles and journals to the new derivate linking"));

        addCommand(new MCRCommand("internal replace ifs link {0}", "org.mycore.frontend.cli.command.MCRLinkConvertCommand.replaceLink String", ""));

        addCommand(new MCRCommand("start gbv sru pica import", "org.mycore.dataimport.pica.MCRGbvSruImportCommand.gbcSruImport", ""));

        addCommand(new MCRCommand("import journals with mapping file {0}", "org.mycore.frontend.cli.MCRImportJournalCommands.importJournals String", ""));

        addCommand(new MCRCommand("internal create default context {0}", "org.mycore.frontend.cli.MCRImportJournalCommands.createContext String", ""));

        addCommand(new MCRCommand("delete xml:lang", "org.mycore.frontend.cli.DeleteLangCommand.delete",
        "deletes all xml:lang attributes in all journals, volumes and articles"));

        addCommand(new MCRCommand("internal delete xml:lang {0}", "org.mycore.frontend.cli.DeleteLangCommand.deleteXmlLang String", ""));
        
        addCommand(new MCRCommand("add derivate link {0} to {1}", "org.mycore.frontend.cli.MCRJPortalCommands.addDerivateLink String String", ""));
    }

    public static void exportBlob(String objectID, String file) {
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

    public static void addDerivateLink(String link, String id) throws MCRActiveLinkException {
        MCRObjectID mcrId = MCRObjectID.getInstance(id);
        DerivateLinkUtil.setLink(mcrId, link);
    }
}
