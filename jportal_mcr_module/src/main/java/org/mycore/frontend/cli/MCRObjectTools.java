package org.mycore.frontend.cli;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.xpath.XPath;
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
import org.mycore.frontend.util.DerivateLinkUtil;
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
        addCommand(new MCRCommand("cp {0} {1} {2} {3}", "org.mycore.frontend.cli.MCRObjectTools.cp String int String String",
                "cp [source ID] [n times] [layoutTemplate] [dataModelCoverage]"));

        addCommand(new MCRCommand("cp {0} {1} {2}", "org.mycore.frontend.cli.MCRObjectTools.cp String String String",
                "cp [sourceID] [layoutTemplate] [dataModelCoverage]."));

        addCommand(new MCRCommand("export import object {0}", "org.mycore.frontend.cli.MCRObjectTools.exportImport String",
                "export import [objectID]."));

//      addCommand(new MCRCommand("repair-cp {0} to {1}", "org.mycore.frontend.cli.MCRObjectTools.repairCopy String String",
//                "repair-cp [sourceObjectID] to [destinationObjectID]."));

        addCommand(new MCRCommand("update context of journal {0}", "org.mycore.frontend.cli.MCRObjectTools.updateJournalContext String",
                "update context of journal [journalID]."));

        addCommand(new MCRCommand("move file {0} to {1}", "org.mycore.frontend.cli.MCRObjectTools.moveFile String String",
                "move file abs. path to abs. path"));

//      addCommand(new MCRCommand("convert volumes {0} to articles", "org.mycore.frontend.cli.MCRObjectTools.convertVolumesToArticles String",
//                "converts a volume to an article"));

        addCommand(new MCRCommand("vd17Import {0}", "org.mycore.frontend.cli.MCRObjectTools.vd17Import String", "vd17Import url"));

        addCommand(new MCRCommand("add derivates {0} to object {1}",
                "org.mycore.frontend.cli.MCRObjectTools.addDerivatesToObject String String", "adds one ore more derivates to an object "));
        
        addCommand(new MCRCommand("merge derivates {0}",
                "org.mycore.frontend.cli.MCRObjectTools.mergeDerivates String", "merge several derivates"));

        addCommand(new MCRCommand("collapse {0}",
                "org.mycore.frontend.cli.MCRObjectTools.collapse String",
                "merges all descendants derivates to the given object (a new derivate is created)"));

        addCommand(new MCRCommand("layer collapse {0} {1}",
                "org.mycore.frontend.cli.MCRObjectTools.layerCollapse String String",
                "merges all descendants derivates of the given object to the given layer. @see collapse command"));

        addCommand(new MCRCommand("set derivate link to {0} with path {1}",
                "org.mycore.frontend.cli.MCRObjectTools.setDerivateLink String String",
                "creates a new derivate link"));
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
            SAXParseException, IOException {
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
    
    
    public static List<String> mergeDerivates(String derivateIDs) {
        List<String> executeMoreCMDs = new ArrayList<String>();
        String[] derivateIdArray = derivateIDs.split(",");
        
        if(derivateIdArray.length > 1){
            MCRDirectory destDeriv = (MCRDirectory) MCRFilesystemNode.getRootNode(derivateIdArray[0]);
            String[] subSetOfIds = Arrays.copyOfRange(derivateIdArray, 1, derivateIdArray.length);
            
            for (String derivID : subSetOfIds) {
                MCRDirectory derivate = (MCRDirectory) MCRFilesystemNode.getRootNode(derivID);
                for (MCRFilesystemNode child : derivate.getChildren()) {
                    child.move(destDeriv);
                }
                MCRMetadataManager.deleteMCRDerivate(MCRObjectID.getInstance(derivID));
            }
            executeMoreCMDs.addAll(MCRIView2Commands.tileDerivate(destDeriv.getOwnerID()));
        }
        return executeMoreCMDs;
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
        Element children = mcrOrigObjXMLDoc.getRootElement().getChild("structure").getChild("children");
        if(children != null) {
            children.detach();
        }

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
            if(dataModelCoverageElem != null) {
                dataModelCoverageElem.setAttribute("categid", dataModelCoverage);
            }
        }

        Element hiddenWebContextsElem = null;
        String hiddenWebContextsPath = "/mycoreobject/metadata/hidden_websitecontexts/hidden_websitecontext";
        hiddenWebContextsElem = getElementWithXpath(mcrOrigObjXMLDoc, hiddenWebContextsPath);

        MCRObjectID newMcrID = MCRObjectID.getNextFreeId(sourceMcrId.getBase());
        if(maintitleElem != null) {
            maintitleElem.setText(maintitleElem.getText() + "[Copy] " + newMcrID.getNumberAsInteger());
        }
        if(hidden_jpjournalIDElem != null) {
            hidden_jpjournalIDElem.setText(newMcrID.toString());
        }
        mcrOrigObjXMLDoc.getRootElement().setAttribute("ID", newMcrID.toString());
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
            @SuppressWarnings("unchecked")
            List<Element> permissions = servAcl.getChildren("servacl");

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
            LOGGER.error("while select node", e);
        }

        return null;
    }

    private static MCRFilesystemNode getFileSystemNode(String sourcePath) {
        String ownerID = getOwnerID(sourcePath);
        MCRFilesystemNode root = MCRFilesystemNode.getRootNode(ownerID);
        int beginIndex = sourcePath.indexOf('/', 1)+1;
        String filename = sourcePath.substring(beginIndex).trim();
        if(!"".equals(filename)) {
            return ((MCRDirectory) root).getChildByPath(filename);
        }
        
        return root;
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

    public static List<String> layerCollapse(String objectId, String layerAsString) {
        MCRObject mcrObj = MCRMetadataManager.retrieveMCRObject(MCRObjectID.getInstance(objectId));
        List<String> layerIds = getLayerIds(mcrObj, Integer.valueOf(layerAsString));
        
        List<String> cmdList = new ArrayList<String>();
        for(String id : layerIds) {
            cmdList.add("collapse " + id);
        }
        return cmdList;
    }

    private static List<String> getLayerIds(MCRObject mcrObj, int layer) {
        if(layer < 0) {
            throw new IllegalArgumentException("Layer cannot be lower than zero. " + layer);
        }
        List<String> ids = new ArrayList<String>();
        if(layer == 0) {
            ids.add(mcrObj.getId().toString());
            return ids;
        }
        List<MCRMetaLinkID> childrenLinks = mcrObj.getStructure().getChildren();
        for(MCRMetaLinkID metaLinkId : childrenLinks) {
            String childId = metaLinkId.getXLinkHref();
            if(layer == 1) {
                ids.add(childId);
            } else {
                MCRObject mcrChild = MCRMetadataManager.retrieveMCRObject(MCRObjectID.getInstance(childId));
                ids.addAll(getLayerIds(mcrChild, layer - 1));
            }
        }
        return ids;
    }

    public static List<String> collapse(String objectId) {
        MCRObjectID mcrObjId = MCRObjectID.getInstance(objectId);
        MCRObject mcrObj = MCRMetadataManager.retrieveMCRObject(mcrObjId);

        // get all derivates in descendants
        Map<MCRDerivate, MCRObject> derivateMap = getDescendants(mcrObj);

        List<String> cmdList = new ArrayList<String>();
        if(derivateMap.isEmpty()) {
            return cmdList;
        }

        // 1. merge with new derivate of objectId
        MCRDerivate mvDerivate = null;
        StringBuilder mergeDerivateCmd = new StringBuilder("merge derivates ");
        Iterator<MCRDerivate> it = derivateMap.keySet().iterator();
        while(it.hasNext()) {
            MCRDerivate mcrChildDer = it.next();
            mergeDerivateCmd.append(mcrChildDer.getId());
            if(it.hasNext()) {
                mergeDerivateCmd.append(",");
            }
            if(mvDerivate == null) {
                mvDerivate = mcrChildDer;
            }
        }
        cmdList.add(mergeDerivateCmd.toString());

        // 2. move derivate to object
        cmdList.add("link derivate " + mvDerivate.getId() + " to " + objectId);

        // 3. create derivate link
        for(Map.Entry<MCRDerivate, MCRObject> entry : derivateMap.entrySet()) {
            MCRDerivate mcrChildDer = entry.getKey();
            MCRObject mcrChildObj = entry.getValue();
            String mainDoc = mcrChildDer.getDerivate().getInternals().getMainDoc();
            cmdList.add("set derivate link to " + mcrChildObj.getId().toString() + " with path " + mvDerivate.getId() + "/" + mainDoc);
        }
        return cmdList;
    }

    public static void setDerivateLink(String objectId, String path) throws MCRActiveLinkException {
        DerivateLinkUtil.setLink(MCRObjectID.getInstance(objectId), path);
    }

    private static Map<MCRDerivate, MCRObject> getDescendants(MCRObject mcrObj) {
        Map<MCRDerivate, MCRObject> idMap = new HashMap<MCRDerivate, MCRObject>();
        List<MCRMetaLinkID> linkList = mcrObj.getStructure().getChildren();
        for(MCRMetaLinkID link : linkList) {
            MCRObject mcrChild = MCRMetadataManager.retrieveMCRObject(MCRObjectID.getInstance(link.getXLinkHref()));
            List<MCRMetaLinkID> derivateLinkList = mcrChild.getStructure().getDerivates();
            for(MCRMetaLinkID derivateLink : derivateLinkList) {
                MCRDerivate mcrDer = MCRMetadataManager.retrieveMCRDerivate(MCRObjectID.getInstance(derivateLink.getXLinkHref()));
                idMap.put(mcrDer, mcrChild);
            }
            idMap.putAll(getDescendants(mcrChild));
        }
        return idMap;
    }

}
