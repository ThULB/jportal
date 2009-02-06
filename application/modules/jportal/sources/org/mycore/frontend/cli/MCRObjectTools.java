package org.mycore.frontend.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;
import org.mycore.access.MCRAccessInterface;
import org.mycore.access.MCRAccessManager;
import org.mycore.common.MCRConfiguration;
import org.mycore.common.MCRPersistenceException;
import org.mycore.common.xml.MCRURIResolver;
import org.mycore.common.xml.MCRXMLHelper;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.MCRJPortalJournalContextForWebpages;

public class MCRObjectTools extends MCRAbstractCommands {
    private static Logger LOGGER = Logger.getLogger(MCRObjectTools.class.getName());

    public MCRObjectTools() {
        super();
        MCRCommand com = null;

        com = new MCRCommand("cp {0} {1} {2}", "org.mycore.frontend.cli.MCRObjectTools.cp String int String", "cp [source ID] [n times] [layoutTemplate]");
        command.add(com);

        com = new MCRCommand("cp {0} {1}", "org.mycore.frontend.cli.MCRObjectTools.cp String String", "cp [sourceID] [layoutTemplate].");
        command.add(com);
    }

    public static List<String> cp(String sourceID, int times, String layoutTemp) {
        List<String> cmd = new ArrayList<String>();

        for (int i = 0; i < times; i++) {
            cmd.add("cp " + sourceID + " " + layoutTemp);
        }

        return cmd;

    }

    public static void cp(String sourceMcrId, String layoutTemp) {
        MCRObject mcrObj = new MCRObject();
        mcrObj.receiveFromDatastore(sourceMcrId);

        Document mcrOrigObjXML = mcrObj.createXML();

        Element maintitleElem = null;
        try {
            String mainTitlePath = "/mycoreobject/metadata/maintitles/maintitle";
            XPath xpathOfmainTitle = XPath.newInstance(mainTitlePath);
            maintitleElem = (Element) xpathOfmainTitle.selectSingleNode(mcrOrigObjXML);
        } catch (JDOMException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        Element hidden_jpjournalIDElem = null;
        try {
            String hidden_jpjournalIDPath = "/mycoreobject/metadata/hidden_jpjournalsID/hidden_jpjournalID";
            XPath xpathOfhidden_jpjournalID = XPath.newInstance(hidden_jpjournalIDPath);
            hidden_jpjournalIDElem = (Element) xpathOfhidden_jpjournalID.selectSingleNode(mcrOrigObjXML);
        } catch (JDOMException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        Element hiddenWebContextsElem = null;
        try {
            String hiddenWebContextsPath = "/mycoreobject/metadata/hidden_websitecontexts/hidden_websitecontext";
            XPath xpathOfHiddenWebContexts = XPath.newInstance(hiddenWebContextsPath);
            hiddenWebContextsElem = (Element) xpathOfHiddenWebContexts.selectSingleNode(mcrOrigObjXML);
        } catch (JDOMException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        MCRObjectID newMcrID = mcrObj.getId();
        newMcrID.setNextFreeId();
        maintitleElem.setText(maintitleElem.getText() + "[Copy] " + newMcrID.getNumberAsInteger());
        hidden_jpjournalIDElem.setText(newMcrID.getId());
        mcrObj.setFromJDOM(mcrOrigObjXML);

        mcrObj.setId(newMcrID);
        try {
            mcrObj.createInDatastore();

            if (newMcrID.getTypeId().equals("jpjournal")) {
                // creating website context
                String precHref = "/content/main/journalList/dummy.xml";
                if (hiddenWebContextsElem != null)
                    precHref = hiddenWebContextsElem.getText();

                String[] splitPrecHref = precHref.split("/");
                String shortCut = splitPrecHref[splitPrecHref.length - 1].replaceAll(".xml", "") + "_" + newMcrID.getNumberAsInteger();
                MCRJPortalJournalContextForWebpages webContext = new MCRJPortalJournalContextForWebpages(newMcrID.getId(), precHref, layoutTemp, shortCut);
                webContext.create();

                String baseDir = MCRConfiguration.instance().getString("MCR.basedir");
                String deployedDir = baseDir + "/build/webapps";
                String naviFile = deployedDir + "/config/navigation.xml";
                XMLOutputter xmlOutputter = new XMLOutputter();
                xmlOutputter.setFormat(Format.getPrettyFormat());
                try {
                    Document naviDoc = MCRXMLHelper.parseXML(new FileInputStream(naviFile),false);
                    String keywordSearchPath = "//item[@href='/browse/keywords?XSL.dummy=" + sourceMcrId + "']";
                    XPath xpath = XPath.newInstance(keywordSearchPath);
                    Element keywordSearchElem = (Element) xpath.selectSingleNode(naviDoc);
                    if (keywordSearchElem != null) {
                        String keywordSearchElemAsStr = xmlOutputter.outputString(keywordSearchElem);
                        keywordSearchElemAsStr = keywordSearchElemAsStr.replaceAll(sourceMcrId, newMcrID.getId());

                        String searchPath = "//item[@href='/content/main/journalList/" + shortCut + "/search.xml']";
                        xpath = XPath.newInstance(searchPath);
                        Element searchElem = (Element) xpath.selectSingleNode(naviDoc);
                        searchElem.addContent(3,MCRXMLHelper.parseXML(keywordSearchElemAsStr,false).getRootElement().detach());
                    }
                    
                    String keywordEditPath = "//item/label[text()='Neues Schlagwort in Register aufnehmen']";
                    xpath = XPath.newInstance(keywordEditPath);
                    Element keywordEditElem = ((Element) xpath.selectSingleNode(naviDoc)).getParentElement();
                    if (keywordSearchElem != null) {
                        String keywordEditElemAsStr = xmlOutputter.outputString(keywordEditElem);
                        keywordEditElemAsStr = keywordEditElemAsStr.replaceAll("XSL.dummy=[a-zA-Z_0-9]*\"","XSL.dummy=" + newMcrID.getId()+"\"");
                        
                        String editPath = "//item[@href='/content/main/journalList/" + shortCut + "/internal.xml']";
                        xpath = XPath.newInstance(editPath);
                        Element editElem = (Element) xpath.selectSingleNode(naviDoc);
                        editElem.addContent(MCRXMLHelper.parseXML(keywordEditElemAsStr,false).getRootElement().detach());
                    }
                    
                    xmlOutputter.output(naviDoc, new FileOutputStream(new File(naviFile)));
                } catch (JDOMException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                // creating ACL for copy
                // retrieve ACL from source Object
                Element servAcl = MCRURIResolver.instance().resolve("access:action=all&object=" + sourceMcrId);
                List<Element> permissions = servAcl.getChildren("servacl");
                MCRAccessInterface AI = MCRAccessManager.getAccessImpl();

                for (Iterator<Element> iterator = permissions.iterator(); iterator.hasNext();) {
                    Element perm = (Element) iterator.next();
                    String permName = perm.getAttributeValue("permission");
                    MCRAccessManager.addRule(newMcrID, permName, perm.getChild("condition"), permName + " permission for " + newMcrID.toString());
                }
            }
        } catch (MCRPersistenceException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (MCRActiveLinkException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
