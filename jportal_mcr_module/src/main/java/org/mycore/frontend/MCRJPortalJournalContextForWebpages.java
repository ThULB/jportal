package org.mycore.frontend;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;
import org.mycore.common.MCRConfiguration;
import org.mycore.common.MCRException;
import org.mycore.common.xml.MCRXMLHelper;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.cli.MCRObjectTools;
import org.mycore.tools.MCRObjectFactory;
import org.xml.sax.SAXParseException;

public class MCRJPortalJournalContextForWebpages {

    private String preceedingItemHref;
    
    private String currentItemHref;

    private String layoutTemplate;

    private String shortCut;

    private String journalID;

    private String journalTitle;

    private String dataModelCoverage;

    private Element journalObjectXML;

    private static final MCRConfiguration PROPS = MCRConfiguration.instance();

    private static Logger LOGGER = Logger.getLogger(MCRJPortalJournalContextForWebpages.class);;

    private static String baseDir = PROPS.getString("MCR.basedir");

    private static String deployedDir = baseDir + "/build/webapps";
    
    private final static String SRC_DIR = deployedDir + "/create-journalContext/";

    public MCRJPortalJournalContextForWebpages(String journalId, String preceedingItemHref, String layoutTemplate, String shortCut) {
        this.journalID = journalId;
        this.journalObjectXML = MCRXMLMetadataManager.instance().retrieveXML(MCRObjectID.getInstance(this.journalID)).getRootElement();
        this.dataModelCoverage = journalObjectXML.getChild("metadata").getChild("dataModelCoverages").getChild("dataModelCoverage")
                .getAttributeValue("categid");
        this.journalTitle = journalObjectXML.getChild("metadata").getChild("maintitles").getChildText("maintitle");
        this.preceedingItemHref = preceedingItemHref;
        this.layoutTemplate = layoutTemplate;
        this.shortCut = shortCut;
    }

    // this private constructor is only for removing journal
    // it is used in removeContext(MCRObject)
    private MCRJPortalJournalContextForWebpages(MCRObject obj) {
        try {
            this.journalID = obj.getId().toString();
            this.journalObjectXML = obj.createXML().getRootElement();
            this.dataModelCoverage = journalObjectXML.getChild("metadata").getChild("dataModelCoverages").getChild("dataModelCoverage").getAttributeValue(
                    "categid");
            this.journalTitle = journalObjectXML.getChild("metadata").getChild("maintitles").getChildText("maintitle");

            String currentItemHrefXpathString = "//hidden_websitecontexts/hidden_websitecontext";
            XPath currentItemHrefXpath = XPath.newInstance(currentItemHrefXpathString);
            Element currentItemHrefElem = (Element) currentItemHrefXpath.selectSingleNode(journalObjectXML);

            // assigning current item href to preceeding item href
            // is not consistent in the naming schema (totally wrong), we'll do
            // it
            // anyway, so there is no need to define a new class field.
            // We'll use it to delete the files of the context
            this.currentItemHref = currentItemHrefElem.getText();
            
            String[] help = this.currentItemHref.split("/");
            
            this.preceedingItemHref = this.currentItemHref.replaceAll(help[help.length-1], "dummy.xml");
            
            String naviFile = deployedDir + "/config/navigation.xml";
            Document navi = MCRXMLHelper.getParser().parseXML(new FileInputStream(naviFile),false);
            String itemXPath = "//item[@href='" + this.currentItemHref + "']";
            LOGGER.debug("find item with xpath=" + itemXPath + " in " + naviFile);
            XPath xp = XPath.newInstance(itemXPath);
            Element currentItemElem = ((Element) xp.selectSingleNode(navi));
            this.layoutTemplate = currentItemElem.getAttributeValue("template");
            String[] tmp = this.currentItemHref.split("/");
            this.shortCut = tmp[tmp.length-1].replaceAll(".xml", "");
            
        } catch (JDOMException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (MCRException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void create() throws MCRException {
        copyWebpages();
        updateNavigation();
        updateJournalObject();
        // TODO: remove cached template id for journal id
        // ...
    }

    private void updateJournalObject() {
        LOGGER.debug("update journal object");
        XMLOutputter xo = new XMLOutputter();
        xo.setFormat(Format.getPrettyFormat());
        try {
            LOGGER.debug("journal object before updating");
            xo.output(journalObjectXML, System.out);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        // remove old values
        if (journalObjectXML.getChild("metadata").getChild("hidden_jpjournalsID") != null)
            journalObjectXML.getChild("metadata").removeChild("hidden_jpjournalsID");
        if (journalObjectXML.getChild("metadata").getChild("hidden_websitecontexts") != null)
            journalObjectXML.getChild("metadata").removeChild("hidden_websitecontexts");
        // set new values
        String xmlTemplate = SRC_DIR + "/partOfJournalXML.xml";
        SAXBuilder sb = new SAXBuilder();
        Element journalXMLCode = null;
        try {
            String xmlTemplateCode = xo.outputString(sb.build(xmlTemplate));
            String xmlCodePre = xmlTemplateCode.replaceAll("JOURNAL_PATHTOSHORTCUT", getDestDirRelative() + this.shortCut);
            String xmlCode = xmlCodePre.replaceAll("JOURNAL_ID", this.journalID);
            journalXMLCode = sb.build(new StringReader(xmlCode)).getRootElement();
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // update object
        List<Element> metadataTags = journalXMLCode.getChildren();
        for (int i = 0; i < metadataTags.size(); i++) {
            Element tag = (Element) metadataTags.get(i);
            journalObjectXML.getChild("metadata").addContent((Element) tag.clone());
        }
        try {
            LOGGER.debug("journal object after updating");
            xo.output(journalObjectXML, System.out);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        // save journal object in mycore
        Document newJournalXML = new Document().addContent(journalObjectXML.detach());
        MCRObject journal = new MCRObject(newJournalXML);
        try {
            MCRMetadataManager.update(journal);
        } catch(MCRActiveLinkException ale) {
            LOGGER.error("while update journal", ale);
        }
        LOGGER.info("updated journal object");
    }

    private void updateNavigation() {
        LOGGER.debug("update navigation");
        // init
        String naviSrcLoc = SRC_DIR + this.dataModelCoverage + "/navigation.xml";
        String naviFile = deployedDir + "/config/navigation.xml";
        Document naviXMLCode = null;

        // replace static values by real ones
        SAXBuilder sb = new SAXBuilder();
        XMLOutputter xo = new XMLOutputter();
        xo.setFormat(Format.getPrettyFormat());
        try {
            String codeOfNavi = xo.outputString(sb.build(naviSrcLoc));
            String codeOfNaviNew1 = codeOfNavi.replaceAll("JOURNAL_PATHTOSHORTCUT", getDestDirRelative() + this.shortCut);
            String codeOfNaviNew2 = codeOfNaviNew1.replaceAll("TEMPLATE_NAME", this.layoutTemplate);
            String codeOfNaviNew3 = codeOfNaviNew2.replaceAll("JOURNAL_NAME", this.journalTitle);
            String codeOfNaviNew4 = codeOfNaviNew3.replaceAll("JOURNAL_ID", this.journalID);
            naviXMLCode = sb.build(new StringReader(codeOfNaviNew4));
            LOGGER.debug("update navigation - replaced static values");
        } catch (JDOMException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        // update
        Document navi = null;
        try {
            navi = sb.build(naviFile);
            String itemXPath = "//item[@href='" + this.preceedingItemHref + "']";
            LOGGER.debug("find item with xpath=" + itemXPath + " in " + naviFile);
            XPath xp = XPath.newInstance(itemXPath);
            Element parentItem = ((Element) xp.selectSingleNode(navi)).getParentElement();
            LOGGER.debug("found parent item with @href=" + parentItem.getAttributeValue("href"));
            List<Element> items = parentItem.getChildren("item");
            LOGGER.debug("length if children under parent=" + items.size());
            for (int i = 0; i < items.size(); i++) {
                Element item = (Element) items.get(i);
                if (item.getAttributeValue("href").equals(this.preceedingItemHref))
                    items.add(i + 1, naviXMLCode.detachRootElement());
            }
            xo.output(navi, new FileOutputStream(new File(naviFile)));
            LOGGER.debug("update navigation - inserted new XML and safed");
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        LOGGER.info("updated navigation");
    }

    private void copyWebpages() throws MCRException {
        LOGGER.debug("copy webpages");
        // init
        String srcDirPrefix = SRC_DIR + this.dataModelCoverage + "/content";
        String srcDir = srcDirPrefix + "/JOURNAL_SHORTCUT";
        String destDir = deployedDir + getDestDirRelative() + shortCut;
        String srcFile = srcDirPrefix + "/JOURNAL_SHORTCUT.xml";
        String destFile = destDir + ".xml";

        // test if short cut is already in use
        if (new File(destFile).exists())
            throw new MCRException("This journal context shortcut=" + this.shortCut + " is already in use. Please, Choose another one");

        // copy
        try {
            FileUtils fu = new FileUtils();
            // // folders
            LOGGER.debug("copy folders of webpage content from " + srcDir + " to " + destDir);
            fu.copyDirectory(new File(srcDir), new File(destDir));
            // // home page
            LOGGER.debug("copy home page content from " + srcFile + " to " + destFile);
            fu.copyFile(new File(srcFile), new File(destFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        // replace JOURNAL_NAME by real values
        try {
            SAXBuilder sb = new SAXBuilder();
            Document homePage = sb.build(destFile);
            XMLOutputter xo = new XMLOutputter();
            xo.setFormat(Format.getPrettyFormat());
            String codeOfHomePage = xo.outputString(homePage);
            String pattern = "JOURNAL_NAME";
            String codeOfHomePageNew = codeOfHomePage.replaceAll(pattern, this.journalTitle);
            Document docNew = sb.build(new StringReader(codeOfHomePageNew));
            xo.output(docNew, new FileOutputStream(new File(destFile)));
            LOGGER.debug("replaced 'JOURNAL_NAME' in " + destFile + " by '" + this.journalTitle + "'");
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        LOGGER.info("copied webpages");
    }

    /**
     * @return
     */
    private String getDestDirRelative() {
        return this.preceedingItemHref.substring(0, this.preceedingItemHref.lastIndexOf("/") + 1);
    }

    public static void removeContext(MCRObject obj) {
        MCRJPortalJournalContextForWebpages journalContext = new MCRJPortalJournalContextForWebpages(obj);

        journalContext.removeEntryInNavigation();
        journalContext.removeWebpages();
    }

    private void removeWebpages() {
        LOGGER.info("Removing webpages for journal \"" + journalID + "\" ...");
        // remember! preceeding item href is current item href
        String locationOfWebpageXML = deployedDir + currentItemHref;
        try {
            File webPageXMl = new File(locationOfWebpageXML);
            if (webPageXMl.exists())
                FileUtils.forceDelete(webPageXMl);

            String webPageFolderLocation = locationOfWebpageXML.replaceAll(".xml$", "");
            File webPageFolder = new File(webPageFolderLocation);
            if (webPageFolder.exists())
                FileUtils.deleteDirectory(webPageFolder);

            LOGGER.info("Webpages for journal \"" + journalID + "\" removed successfully.");
        } catch (IOException e) {
            LOGGER.info("Removing webpages for journal \"" + journalID + "\" failed");
            e.printStackTrace();
        }
    }

    private void removeEntryInNavigation() {
        LOGGER.info("Removing navigation entry for journal \"" + journalID + "\" ...");
        String naviFileLocation = deployedDir + "/config/navigation.xml";
        try {
            Document naviFileDoc = MCRXMLHelper.getParser().parseXML(new FileInputStream(naviFileLocation), false);
            String entryInNaviFileDoc = "//item[@href='" + this.currentItemHref + "']";
            Element jdomElemOfLocation = (Element) XPath.selectSingleNode(naviFileDoc, entryInNaviFileDoc);

            if (jdomElemOfLocation != null) {
                jdomElemOfLocation.detach();
                XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
                xmlOutputter.output(naviFileDoc, new FileOutputStream(naviFileLocation));
                LOGGER.info("Navigation entry for journal \"" + journalID + "\" removed successfully.");
            } else {
                LOGGER.info("Journal \"" + journalID + "\" has no entry in " + naviFileLocation + " .");
            }
        } catch (MCRException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JDOMException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static void updateContext(MCRObject obj) {
        MCRJPortalJournalContextForWebpages context = new MCRJPortalJournalContextForWebpages(obj);
        context.removeEntryInNavigation();
        // you shouldn't delete everything especially the SHORTCUT.xml, which could has content
        // so we save it into a variable
        Document journalStartWebPage = context.getJournalStartWebpage();
        context.removeWebpages();
        context.copyWebpages();
        // now we write back the content of SHORTCUT.xml
        context.writeJournalStartWebpage(journalStartWebPage);
        context.updateNavigation();
    }

    private void writeJournalStartWebpage(Document journalStartWebPage) {
        String locationOfWebpageXML = deployedDir + currentItemHref;
        XMLOutputter xmlOutputter = new XMLOutputter();
        xmlOutputter.setFormat(Format.getPrettyFormat());
        
        try {
            xmlOutputter.output(journalStartWebPage, new FileOutputStream(locationOfWebpageXML));
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private Document getJournalStartWebpage() {
        String locationOfWebpageXML = deployedDir + currentItemHref;
        
        try {
            return MCRXMLHelper.getParser().parseXML(new FileInputStream(locationOfWebpageXML), false);
        } catch (MCRException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return null;
    }

}
