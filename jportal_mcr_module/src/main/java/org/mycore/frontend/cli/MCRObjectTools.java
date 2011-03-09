package org.mycore.frontend.cli;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.xpath.XPath;
import org.mycore.access.MCRAccessInterface;
import org.mycore.access.MCRAccessManager;
import org.mycore.common.MCRException;
import org.mycore.common.MCRPersistenceException;
import org.mycore.common.xml.MCRURIResolver;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.ifs.MCRDirectory;
import org.mycore.datamodel.ifs.MCRFilesystemNode;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetaLinkID;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.MCRJPortalJournalContextForWebpages;
import org.mycore.iview2.frontend.MCRIView2Commands;
import org.xml.sax.SAXParseException;

import fsu.thulb.jaxb.JaxbTools;
import fsu.thulb.jp.searchpojo.AtomLink;
import fsu.thulb.jp.searchpojo.ContentEntry;
import fsu.thulb.jp.searchpojo.StorageContentList;

public class MCRObjectTools extends MCRAbstractCommands {
    private static Logger LOGGER = Logger.getLogger(MCRObjectTools.class.getName());

    public MCRObjectTools() {
        super();
        MCRCommand com = null;

        com = new MCRCommand("cp {0} {1} {2} {3}", "org.mycore.frontend.cli.MCRObjectTools.cp String int String String",
                "cp [source ID] [n times] [layoutTemplate] [dataModelCoverage]");
        command.add(com);

        com = new MCRCommand("cp {0} {1} {2}", "org.mycore.frontend.cli.MCRObjectTools.cp String String String",
                "cp [sourceID] [layoutTemplate] [dataModelCoverage].");
        command.add(com);

        com = new MCRCommand("export import object {0}", "org.mycore.frontend.cli.MCRObjectTools.exportImport String",
                "export import [objectID].");
        command.add(com);

        com = new MCRCommand("repair-cp {0} to {1}", "org.mycore.frontend.cli.MCRObjectTools.repairCopy String String",
                "repair-cp [sourceObjectID] to [destinationObjectID].");

        com = new MCRCommand("update context of journal {0}", "org.mycore.frontend.cli.MCRObjectTools.updateJournalContext String",
                "update context of journal [journalID].");
        command.add(com);

        com = new MCRCommand("move file {0} to {1}", "org.mycore.frontend.cli.MCRObjectTools.moveFile String String",
                "move file abs. path to abs. path");
        command.add(com);

        com = new MCRCommand("convert volumes {0} to articles", "org.mycore.frontend.cli.MCRObjectTools.convertVolumesToArticles String",
                "converts a volume to an article");

        com = new MCRCommand("vd17Import {0}", "org.mycore.frontend.cli.MCRObjectTools.vd17Import String", "vd17Import url");

        command.add(com);

        com = new MCRCommand("add derivates {0} to object {1}",
                "org.mycore.frontend.cli.MCRObjectTools.addDerivatesToObject String String", "adds one ore more derivates to an object ");
        command.add(com);
    }

    public static void vd17Import(String url) throws IOException, JAXBException, URISyntaxException, MCRActiveLinkException, MCRException,
            SAXParseException {
        StorageContentList storageContentList = JaxbTools.unmarschall(new URL(url), StorageContentList.class);

        ContentEntry participantEntries = storageContentList.getContentFor("participant");
        importObjects(participantEntries);
        ContentEntry jpVolumeEntries = storageContentList.getContentFor("jpvolume");
        importObjects(jpVolumeEntries);
    }

    private static void importObjects(ContentEntry contentEntries) throws URISyntaxException, MCRActiveLinkException, MCRException,
            SAXParseException {
        for (AtomLink participantLink : contentEntries.getLink()) {
            MCRObject mcrObject = new MCRObject(new URI(participantLink.getHref()));
            MCRMetadataManager.update(mcrObject);
        }
    }

    public static void updateJournalContext(String journalID) {
        MCRObject mcrObj = MCRMetadataManager.retrieveMCRObject(MCRObjectID.getInstance(journalID));

        String objType = mcrObj.getId().getTypeId();

        if (objType.equals("jpjournal")) {
            MCRJPortalJournalContextForWebpages.updateContext(mcrObj);
            LOGGER.info("Updated context for \"" + journalID + "\".");
        } else {
            LOGGER.info(journalID + " in no journal!");
        }
    }

    public static void repairCopy(String sourceObjectID, String destinationObjectID) {
        // find if there the destination object allready has children
        // when yes, add them to the destination object
        String querystring = "parent = \"" + destinationObjectID + "\"";
        MCRObjectCommands.selectObjectsWithQuery(querystring);
        List<String> idList = MCRObjectCommands.getSelectedObjectIDs();

        // getting source XML
        // replace the children element
        Document sourceDoc = MCRXMLMetadataManager.instance().retrieveXML(MCRObjectID.getInstance(sourceObjectID));

        sourceDoc.getRootElement().getAttribute("ID").setValue(destinationObjectID);
        sourceDoc.getRootElement().getAttribute("label").setValue(destinationObjectID);

        Element maintitle = getElementWithXpath(sourceDoc, "/mycoreobject/metadata/maintitles/maintitle[@inherited='0']");
        if (maintitle != null)
            maintitle.setText(maintitle.getText() + "[Copy]");

        Element structElement = sourceDoc.getRootElement().getChild("structure");
        Element childElement = structElement.getChild("children");
        if (childElement != null)
            childElement.detach();

        structElement.addContent(generateChildrenHref(idList));

        MCRXMLMetadataManager.instance().create(MCRObjectID.getInstance(destinationObjectID), sourceDoc, new Date());
        MCRObjectCommands.repairMetadataSearchForID(destinationObjectID);
    }

    private static Element generateChildrenHref(List<String> idList) {
        Element childrenElement = new Element("children");
        childrenElement.setAttribute("class", "MCRMetaLinkID");
        Namespace xlink = Namespace.getNamespace("xlink", "http://www.w3.org/1999/xlink");
        for (String objID : idList) {
            Element childElement = new Element("child");
            childElement.setAttribute("inherited", "0");
            childElement.setAttribute("type", "locator", xlink);
            childElement.setAttribute("href", objID, xlink);
            childElement.setAttribute("title", objID, xlink);

            childrenElement.addContent(childElement);
        }
        return childrenElement;
    }

    public static void exportImport(String objectID) throws MCRPersistenceException, MCRActiveLinkException {
        MCRObject mcrObject = MCRMetadataManager.retrieveMCRObject(MCRObjectID.getInstance(objectID));
        MCRMetadataManager.update(mcrObject);
    }

    // dataModelCoverage: browse, fully
    public static List<String> cp(String sourceID, int times, String layoutTemp, String dataModelCoverage) {
        List<String> cmd = new ArrayList<String>();

        for (int i = 0; i < times; i++) {
            cmd.add("cp " + sourceID + " " + layoutTemp + " " + dataModelCoverage);
        }

        return cmd;

    }
    
    public static List<String> moveFile(String sourcePath, String destPath) {
        List<String> executeMoreCMDs = new ArrayList<String>();
        MCRFilesystemNode sourceNode = getFileSystemNode(sourcePath);
        

        MCRFilesystemNode destNode;
        if (destPath.startsWith("..")) {
            destNode = sourceNode.getParent().getChildByPath(destPath);
        } else {
            destNode = getFileSystemNode(destPath);
        }

        if (destNode instanceof MCRDirectory) {
            sourceNode.move((MCRDirectory)destNode);
            
            executeMoreCMDs.addAll(MCRIView2Commands.tileDerivate(sourceNode.getOwnerID()));
            executeMoreCMDs.addAll(MCRIView2Commands.tileDerivate(destNode.getOwnerID()));
        } else {
            LOGGER.info(destPath + " is not a directory");
        }
        
        return executeMoreCMDs;
    }

    public static void cp(String sourceMcrIdStr, String layoutTemp, String dataModelCoverage) {
        MCRObjectID sourceMcrId = MCRObjectID.getInstance(sourceMcrIdStr);
        Document mcrOrigObjXMLDoc = MCRXMLMetadataManager.instance().retrieveXML(sourceMcrId);

        // we don't want to adopt children
        mcrOrigObjXMLDoc.getRootElement().getChild("structure").getChild("children").detach();

        Element maintitleElem = null;
        String mainTitlePath = "/mycoreobject/metadata/maintitles/maintitle";
        maintitleElem = getElementWithXpath(mcrOrigObjXMLDoc, mainTitlePath);

        Element hidden_jpjournalIDElem = null;
        String hidden_jpjournalIDPath = "/mycoreobject/metadata/hidden_jpjournalsID/hidden_jpjournalID";
        hidden_jpjournalIDElem = getElementWithXpath(mcrOrigObjXMLDoc, hidden_jpjournalIDPath);

        if (!dataModelCoverage.equals("")) {
            Element dataModelCoverageElem = null;
            String dataModelCoverageLocation = "/mycoreobject/metadata/dataModelCoverages/dataModelCoverage";
            dataModelCoverageElem = getElementWithXpath(mcrOrigObjXMLDoc, dataModelCoverageLocation);
            dataModelCoverageElem.setAttribute("categid", dataModelCoverage);
        }

        Element hiddenWebContextsElem = null;
        String hiddenWebContextsPath = "/mycoreobject/metadata/hidden_websitecontexts/hidden_websitecontext";
        hiddenWebContextsElem = getElementWithXpath(mcrOrigObjXMLDoc, hiddenWebContextsPath);

        MCRObjectID newMcrID = MCRObjectID.getNextFreeId(sourceMcrId.getBase());
        maintitleElem.setText(maintitleElem.getText() + "[Copy] " + newMcrID.getNumberAsInteger());
        hidden_jpjournalIDElem.setText(newMcrID.toString());

        MCRXMLMetadataManager.instance().create(newMcrID, mcrOrigObjXMLDoc, new Date());

        if (newMcrID.getTypeId().equals("jpjournal")) {
            // creating website context
            String precHref = "/content/main/journalList/dummy.xml";
            if (hiddenWebContextsElem != null)
                precHref = hiddenWebContextsElem.getText();

            String[] splitPrecHref = precHref.split("/");
            String shortCut = splitPrecHref[splitPrecHref.length - 1].replaceAll(".xml", "") + "_" + newMcrID.getNumberAsInteger();
            MCRJPortalJournalContextForWebpages webContext = new MCRJPortalJournalContextForWebpages(newMcrID.toString(), precHref,
                    layoutTemp, shortCut);
            webContext.create();

            // creating ACL for copy
            // retrieve ACL from source Object
            Element servAcl = MCRURIResolver.instance().resolve("access:action=all&object=" + sourceMcrIdStr);
            List<Element> permissions = servAcl.getChildren("servacl");
            MCRAccessInterface AI = MCRAccessManager.getAccessImpl();

            for (Iterator<Element> iterator = permissions.iterator(); iterator.hasNext();) {
                Element perm = (Element) iterator.next();
                String permName = perm.getAttributeValue("permission");
                MCRAccessManager.addRule(newMcrID, permName, perm.getChild("condition"), permName + " permission for "
                        + newMcrID.toString());
            }
        }

    }

    private static Element getElementWithXpath(Document xmlDoc, String xpathExpression) {

        try {
            XPath xpath = XPath.newInstance(xpathExpression);
            return (Element) xpath.selectSingleNode(xmlDoc);
        } catch (JDOMException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }

    private static MCRFilesystemNode getFileSystemNode(String sourcePath) {
        String ownerID = getOwnerID(sourcePath);
        MCRFilesystemNode root = MCRFilesystemNode.getRootNode(ownerID);
        int pos = ownerID.length() + 1;
        StringBuffer path = new StringBuffer(sourcePath.substring(pos));
        if (path.length() > 1 && (path.charAt(path.length() - 1) == '/')) {
            path.deleteCharAt(path.length() - 1);
        }
        MCRFilesystemNode node = ((MCRDirectory) root).getChildByPath(path.toString());
        return node;
    }

    private static String getOwnerID(String path) {
        StringBuffer ownerID = new StringBuffer(path.length());
        boolean running = true;
        for (int i = (path.charAt(0) == '/') ? 1 : 0; (i < path.length() && running); i++) {
            switch (path.charAt(i)) {
            case '/':
                running = false;
                break;
            default:
                ownerID.append(path.charAt(i));
                break;
            }
        }
        return ownerID.toString();
    }

    //    public static List<String> convertVolumesToArticles(String volumeIds) throws Exception {
    //        ArrayList<MCRImportRecord> recordList = new ArrayList<MCRImportRecord>();
    //        ArrayList<String> commandList = new ArrayList<String>();
    //        for(String volumeId : volumeIds.split(",")) {
    //            // get mcr volume object
    //            MCRObject volume = new MCRObject();
    //            volume.receiveFromDatastore(volumeId);
    //            Document volumeDoc = volume.createXML();
    //            // convert volume to mcrimportrecord
    //            MCRImportXMLConverter xmlConverter = new MCRImportXMLConverter("article");
    //            MCRImportRecord record = xmlConverter.convert(volumeDoc);
    //            recordList.add(record);
    //            // add delete command for volume
    //            StringBuffer deleteCommand = new StringBuffer("delete object ");
    //            commandList.add(deleteCommand.append(volumeId).toString());
    //        }
    //
    //        // start mapping
    //        StringBuffer fileBuf = new StringBuffer(MCRConfiguration.instance().getString("MCR.Modules.BaseDir"));
    //        fileBuf.append("/modules/jportal/config/import/volumeToArticle.xml");
    //        File mappingFile = new File(fileBuf.toString());
    //        MCRImportMappingManager.getInstance().init(mappingFile);
    //        MCRImportMappingManager.getInstance().startMapping(recordList);
    //        // import article to mycore
    //        MCRImportImporter importer = new MCRImportImporter(mappingFile);
    //        importer.generateMyCoReFiles();
    //        // return command list containing delete commands for volumes and
    //        // create commands for articles
    //        commandList.addAll(importer.getCommandList());
    //        return commandList;
    //    }

    public static void addDerivatesToObject(String derivateIds, String objectId) throws Exception {
        String[] derivateIdArray = derivateIds.split(",");
        for (String derId : derivateIdArray) {
            MCRDerivate der = MCRMetadataManager.retrieveMCRDerivate(MCRObjectID.getInstance(derId));
            // set link in derivate
            MCRMetaLinkID objLink = new MCRMetaLinkID();
            objLink.setSubTag("linkmeta");
            objLink.setReference(objectId, null, null);
            der.getDerivate().setLinkMeta(objLink);
            
            MCRMetadataManager.update(der);
        }
    }
}
