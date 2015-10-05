package fsu.jportal.frontend.cli;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.access.MCRAccessManager;
import org.mycore.common.MCRPersistenceException;
import org.mycore.common.xml.MCRURIResolver;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.ifs.MCRDirectory;
import org.mycore.datamodel.ifs.MCRFilesystemNode;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetaElement;
import org.mycore.datamodel.metadata.MCRMetaLangText;
import org.mycore.datamodel.metadata.MCRMetaLinkID;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.cli.MCRObjectCommands;
import org.mycore.frontend.cli.annotation.MCRCommand;
import org.mycore.frontend.cli.annotation.MCRCommandGroup;
import org.mycore.iview2.frontend.MCRIView2Commands;

import fsu.jportal.backend.io.ImportSink;
import fsu.jportal.backend.io.ImportSource;
import fsu.jportal.backend.io.RecursiveImporter;
import fsu.jportal.frontend.RecursiveObjectExporter.ExporterSink;
import fsu.jportal.frontend.RecursiveObjectExporter.ExporterSource;
import fsu.jportal.frontend.cli.io.LocalExportSink;
import fsu.jportal.frontend.cli.io.LocalExportSource;
import fsu.jportal.frontend.util.DerivateLinkUtil;

@MCRCommandGroup(name = "JP Object Commands")
public class ObjectTools {
    private static Logger LOGGER = LogManager.getLogger(ObjectTools.class.getName());

    @MCRCommand(help = "export import [objectID].", syntax = "export import object {0}")
    public static void exportImport(String objectID) throws MCRPersistenceException, MCRActiveLinkException {
        MCRObject mcrObject = MCRMetadataManager.retrieveMCRObject(MCRObjectID.getInstance(objectID));
        MCRMetadataManager.update(mcrObject);
    }

    @MCRCommand(help = "export object and children and their derivate from [objectID] to [destination].", syntax = "export object recursiv from {0} to {1}")
    public static void exportObject(String objectID, String dest) {
        ImportSource importSource = new ExporterSource(objectID);
        ImportSink importSink = new ExporterSink(Paths.get(dest));
        RecursiveImporter recursiveImporter = new RecursiveImporter(importSource, importSink);
        recursiveImporter.start();
    }

    @MCRCommand(help = "export recursive derivate from [objectID] to [destination].", syntax = "exportDerivates from {0} to {1}")
    public static void exportPics(String objectID, String dest) {
        ImportSource importSource = new LocalExportSource(objectID);
        ImportSink importSink = new LocalExportSink(Paths.get(dest));
        RecursiveImporter recursiveImporter = new RecursiveImporter(importSource, importSink);
        recursiveImporter.start();
    }

    // dataModelCoverage: browse, fully
    @MCRCommand(help = "cp(n) [source ID] [n times]", syntax = "cp(n) {0} {1}")
    public static List<String> cp(String sourceID, int times) {
        List<String> cmd = new ArrayList<String>();
        for (int i = 0; i < times; i++) {
            cmd.add("cp " + sourceID);
        }
        return cmd;
    }

    @MCRCommand(help = "merge several derivates", syntax = "merge derivates {0}")
    public static List<String> mergeDerivates(String derivateIDs) throws IOException {
        List<String> executeMoreCMDs = new ArrayList<String>();
        String[] derivateIdArray = derivateIDs.split(",");

        if (derivateIdArray.length > 1) {
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

    @MCRCommand(help = "move file abs. path to abs. path", syntax = "move file {0} to {1}")
    public static List<String> moveFile(String sourcePath, String destPath) throws IOException {
        List<String> executeMoreCMDs = new ArrayList<String>();
        MCRFilesystemNode sourceNode = getFileSystemNode(sourcePath);

        MCRFilesystemNode destNode;
        if (destPath.startsWith("..")) {
            destNode = sourceNode.getParent().getChildByPath(destPath);
        } else {
            destNode = getFileSystemNode(destPath);
        }

        if (destNode instanceof MCRDirectory) {
            sourceNode.move((MCRDirectory) destNode);

            executeMoreCMDs.addAll(MCRIView2Commands.tileDerivate(sourceNode.getOwnerID()));
            executeMoreCMDs.addAll(MCRIView2Commands.tileDerivate(destNode.getOwnerID()));
        } else {
            LOGGER.info(destPath + " is not a directory");
        }

        return executeMoreCMDs;
    }

    @MCRCommand(help = "cp [sourceID]", syntax = "cp {0}")
    public static void cp(String sourceMcrIdStr) throws Exception {
        MCRObjectID sourceMcrId = MCRObjectID.getInstance(sourceMcrIdStr);
        Document mcrOrigObjXMLDoc = MCRXMLMetadataManager.instance().retrieveXML(sourceMcrId);

        // we don't want to adopt children
        Element children = mcrOrigObjXMLDoc.getRootElement().getChild("structure").getChild("children");
        if (children != null) {
            children.detach();
        }

        MCRObjectID newMcrID = MCRObjectID.getNextFreeId(sourceMcrId.getBase());

        // set id
        mcrOrigObjXMLDoc.getRootElement().setAttribute("ID", newMcrID.toString());

        // set maintitle
        Element maintitleElem = null;
        String mainTitlePath = "/mycoreobject/metadata/maintitles/maintitle";
        maintitleElem = getElementWithXpath(mcrOrigObjXMLDoc, mainTitlePath);
        if (maintitleElem != null) {
            maintitleElem.setText(maintitleElem.getText() + "[Copy] " + newMcrID.getNumberAsInteger());
        }

        // set hidden journal id for journal's
        if (newMcrID.getTypeId().equals("jpjournal")) {
            Element hidden_jpjournalIDElem = null;
            String hidden_jpjournalIDPath = "/mycoreobject/metadata/hidden_jpjournalsID/hidden_jpjournalID";
            hidden_jpjournalIDElem = getElementWithXpath(mcrOrigObjXMLDoc, hidden_jpjournalIDPath);
            if (hidden_jpjournalIDElem != null) {
                hidden_jpjournalIDElem.setText(newMcrID.toString());
            }
        }

        // create
        MCRObject newObj = new MCRObject(mcrOrigObjXMLDoc);
        MCRMetadataManager.create(newObj);
    }

    private static Element getElementWithXpath(Document xmlDoc, String xpathExpression) {
        XPathExpression<Element> xpath = XPathFactory.instance().compile(xpathExpression, Filters.element());
        return xpath.evaluateFirst(xmlDoc);
    }

    private static MCRFilesystemNode getFileSystemNode(String sourcePath) {
        String ownerID = getOwnerID(sourcePath);
        MCRFilesystemNode root = MCRFilesystemNode.getRootNode(ownerID);
        int beginIndex = sourcePath.indexOf('/', 1) + 1;
        String filename = sourcePath.substring(beginIndex).trim();
        if (!"".equals(filename)) {
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

    @MCRCommand(help = "adds one ore more derivates to an object.", syntax = "add derivates {0} to object {1}")
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

    @MCRCommand(help = "merges all descendants derivates of the given object to the given layer. @see collapse command", syntax = "layer collapse {0} {1}")
    public static List<String> layerCollapse(String objectId, String layerAsString) {
        MCRObject mcrObj = MCRMetadataManager.retrieveMCRObject(MCRObjectID.getInstance(objectId));
        List<String> layerIds = getLayerIds(mcrObj, Integer.valueOf(layerAsString));

        List<String> cmdList = new ArrayList<String>();
        for (String id : layerIds) {
            cmdList.add("collapse " + id);
        }
        return cmdList;
    }

    private static List<String> getLayerIds(MCRObject mcrObj, int layer) {
        if (layer < 0) {
            throw new IllegalArgumentException("Layer cannot be lower than zero. " + layer);
        }
        List<String> ids = new ArrayList<String>();
        if (layer == 0) {
            ids.add(mcrObj.getId().toString());
            return ids;
        }
        List<MCRMetaLinkID> childrenLinks = mcrObj.getStructure().getChildren();
        for (MCRMetaLinkID metaLinkId : childrenLinks) {
            String childId = metaLinkId.getXLinkHref();
            if (layer == 1) {
                ids.add(childId);
            } else {
                MCRObject mcrChild = MCRMetadataManager.retrieveMCRObject(MCRObjectID.getInstance(childId));
                ids.addAll(getLayerIds(mcrChild, layer - 1));
            }
        }
        return ids;
    }

    @MCRCommand(help = "merges all descendants derivates to the given object (a new derivate is created)", syntax = "collapse {0}")
    public static List<String> collapse(String objectId) {
        MCRObjectID mcrObjId = MCRObjectID.getInstance(objectId);
        MCRObject mcrObj = MCRMetadataManager.retrieveMCRObject(mcrObjId);

        // get all derivates in descendants
        Map<MCRDerivate, MCRObject> derivateMap = getDescendants(mcrObj);

        List<String> cmdList = new ArrayList<String>();
        if (derivateMap.isEmpty()) {
            return cmdList;
        }

        // 1. merge with new derivate of objectId
        MCRDerivate mvDerivate = null;
        StringBuilder mergeDerivateCmd = new StringBuilder("merge derivates ");
        Iterator<MCRDerivate> it = derivateMap.keySet().iterator();
        while (it.hasNext()) {
            MCRDerivate mcrChildDer = it.next();
            mergeDerivateCmd.append(mcrChildDer.getId());
            if (it.hasNext()) {
                mergeDerivateCmd.append(",");
            }
            if (mvDerivate == null) {
                mvDerivate = mcrChildDer;
            }
        }
        cmdList.add(mergeDerivateCmd.toString());

        // 2. move derivate to object
        cmdList.add("link derivate " + mvDerivate.getId() + " to " + objectId);

        // 3. create derivate link
        for (Map.Entry<MCRDerivate, MCRObject> entry : derivateMap.entrySet()) {
            MCRDerivate mcrChildDer = entry.getKey();
            MCRObject mcrChildObj = entry.getValue();
            String mainDoc = mcrChildDer.getDerivate().getInternals().getMainDoc();
            cmdList.add("set derivate link to " + mcrChildObj.getId().toString() + " with path " + mvDerivate.getId()
                + "/" + mainDoc);
        }
        return cmdList;
    }

    @MCRCommand(help = "creates a new derivate link", syntax = "set derivate link to {0} with path {1}")
    public static void setDerivateLink(String objectId, String path) throws MCRActiveLinkException {
        DerivateLinkUtil.setLink(MCRObjectID.getInstance(objectId), path);
    }

    @MCRCommand(help = "removes all derivate links of derivate", syntax = "remove derivate links of object {0} of derivate {1}")
    public static void removeDerivateLinks(String objectId, String derivateId) throws Exception {
        DerivateLinkUtil.removeLinks(MCRObjectID.getInstance(objectId), MCRObjectID.getInstance(derivateId));
    }

    private static Map<MCRDerivate, MCRObject> getDescendants(MCRObject mcrObj) {
        Map<MCRDerivate, MCRObject> idMap = new HashMap<MCRDerivate, MCRObject>();
        List<MCRMetaLinkID> linkList = mcrObj.getStructure().getChildren();
        for (MCRMetaLinkID link : linkList) {
            MCRObject mcrChild = MCRMetadataManager.retrieveMCRObject(MCRObjectID.getInstance(link.getXLinkHref()));
            List<MCRMetaLinkID> derivateLinkList = mcrChild.getStructure().getDerivates();
            for (MCRMetaLinkID derivateLink : derivateLinkList) {
                MCRDerivate mcrDer = MCRMetadataManager
                    .retrieveMCRDerivate(MCRObjectID.getInstance(derivateLink.getXLinkHref()));
                idMap.put(mcrDer, mcrChild);
            }
            idMap.putAll(getDescendants(mcrChild));
        }
        return idMap;
    }

    @MCRCommand(help = "selects all mcr objects in the given file. every object should be in a new line", syntax = "select from file {0}")
    public static void selectFromFile(String filename) throws FileNotFoundException, IOException {
        List<String> selectedList = new ArrayList<String>();
        InputStream fis;
        BufferedReader br;
        String line;

        fis = new FileInputStream(filename);
        br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
        while ((line = br.readLine()) != null) {
            selectedList.add(line);
        }
        br.close();
        MCRObjectCommands.setSelectedObjectIDs(selectedList);
    }

    @MCRCommand(help = "goes through the hierarchy and rewrites the hidden journal id of every object", syntax = "fix hidden journal id for {0}")
    public static List<String> fixHiddenJournalId(String objectId) throws MCRActiveLinkException {
        MCRObjectID mcrId = MCRObjectID.getInstance(objectId);
        if (!MCRMetadataManager.exists(mcrId)) {
            LOGGER.error(objectId + " does not exist!");
            return null;
        }
        MCRObject mcrObj = MCRMetadataManager.retrieveMCRObject(mcrId);
        MCRObject journal = mcrObj;
        if (!journal.getId().getTypeId().equals("jpjournal")) {
            journal = getRoot(mcrObj);
            if (!journal.getId().getTypeId().equals("jpjournal")) {
                LOGGER.error("root object is not a journal " + journal.getId());
                return null;
            }
        }
        return fixHiddenJournalId(objectId, journal.getId().toString());
    }

    @MCRCommand(help = "goes through the hierarchy and rewrites the hidden journal id of every object", syntax = "internal fix hidden journal id for {0} {1}")
    public static List<String> fixHiddenJournalId(String objectId, String hiddenJournalID)
        throws MCRActiveLinkException {
        MCRObjectID mcrId = MCRObjectID.getInstance(objectId);
        if (!MCRMetadataManager.exists(mcrId)) {
            LOGGER.error(objectId + " does not exist!");
            return null;
        }
        MCRObject mcrObj = MCRMetadataManager.retrieveMCRObject(mcrId);
        setHiddenJournalID(mcrObj, hiddenJournalID);
        List<String> commandList = new ArrayList<String>();
        for (MCRMetaLinkID childID : mcrObj.getStructure().getChildren()) {
            commandList.add("internal fix hidden journal id for " + childID.getXLinkHref() + " " + hiddenJournalID);
        }
        return commandList;
    }

    private static void setHiddenJournalID(MCRObject obj, String hiddenJournalID) throws MCRActiveLinkException {
        MCRMetaElement journalIDElement = obj.getMetadata().getMetadataElement("hidden_jpjournalsID");
        if (journalIDElement == null) {
            MCRMetaElement me = new MCRMetaElement();
            me.setTag("hidden_jpjournalsID");
            me.setHeritable(true);
            me.setNotInherit(false);
            me.setClass(org.mycore.datamodel.metadata.MCRMetaLangText.class);
            me.addMetaObject(new MCRMetaLangText("hidden_jpjournalID", null, null, 0, "plain", hiddenJournalID));
            obj.getMetadata().setMetadataElement(me);
        } else {
            MCRMetaLangText mlt = (MCRMetaLangText) journalIDElement.getElementByName("hidden_jpjournalID");
            if (mlt.getText().equals(hiddenJournalID)) {
                return;
            }
            mlt.setText(hiddenJournalID);
        }
        MCRMetadataManager.update(obj);
    }

    public static List<MCRObject> getAncestors(MCRObject mcrObject) {
        List<MCRObject> ancestorList = new ArrayList<MCRObject>();
        while (mcrObject.hasParent()) {
            MCRObjectID parentID = mcrObject.getStructure().getParentID();
            MCRObject parent = MCRMetadataManager.retrieveMCRObject(parentID);
            ancestorList.add(parent);
            mcrObject = parent;
        }
        return ancestorList;
    }

    public static MCRObject getRoot(MCRObject mcrObject) {
        List<MCRObject> ancestorList = getAncestors(mcrObject);
        return ancestorList.isEmpty() ? null : ancestorList.get(ancestorList.size() - 1);
    }
}
