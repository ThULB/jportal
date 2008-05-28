package org.mycore.frontend;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

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
import org.mycore.common.MCRPersistenceException;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.common.MCRXMLTableManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;

public class MCRJPortalJournalContextForWebpages {

    private String preceedingItemHref;

    private String layoutTemplate;

    private String shortCut;

    private String journalID;

    private String journalTitle;

    private String dataModelCoverage;

    private Element journalObjectXML;

    private static final MCRConfiguration PROPS = MCRConfiguration.instance();

    private static Logger LOGGER = Logger.getLogger(MCRJPortalJournalContextForWebpages.class);;

    private static String homeDir = PROPS.getString("MCR.basedir");

    private final static String SRC_DIR = "/modules/jportal/webpages/create-journalContext/";

    private static String deployedDir = homeDir + "/build/webapps";

    public MCRJPortalJournalContextForWebpages(String journalId, String preceedingItemHref, String layoutTemplate, String shortCut) {
        this.journalID = journalId;
        this.journalObjectXML = MCRXMLTableManager.instance().readDocument(new MCRObjectID(this.journalID)).getRootElement();
        this.dataModelCoverage = journalObjectXML.getChild("metadata").getChild("dataModelCoverages").getChild("dataModelCoverage")
                        .getAttributeValue("categid");
        this.journalTitle = journalObjectXML.getChild("metadata").getChild("maintitles").getChildText("maintitle");
        this.preceedingItemHref = preceedingItemHref;
        this.layoutTemplate = layoutTemplate;
        this.shortCut = shortCut;
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
        String xmlTemplate = homeDir + SRC_DIR + "/partOfJournalXML.xml";
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
        MCRObject mo = new MCRObject();
        mo.setFromJDOM(new Document().addContent(journalObjectXML.detach()));
        try {
            mo.updateInDatastore();
        } catch (MCRPersistenceException e) {
            e.printStackTrace();
        } catch (MCRActiveLinkException e) {
            e.printStackTrace();
        }
        LOGGER.info("updated journal object");
    }

    private void updateNavigation() {
        LOGGER.debug("update navigation");
        // init
        String naviSrcLoc = homeDir + SRC_DIR + this.dataModelCoverage + "/navigation.xml";
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
        String srcDir = homeDir + srcDirPrefix + "/JOURNAL_SHORTCUT";
        String destDir = deployedDir + getDestDirRelative() + shortCut;
        String srcFile = homeDir + srcDirPrefix + "/JOURNAL_SHORTCUT.xml";
        String destFile = destDir + ".xml";

        // test if short cut is already in use
        if (new File(destFile).exists())
            throw new MCRException("This journal context shortcut=" + this.shortCut + " is already in use. Please, Choose another one");

        // copy
        try {
            org.apache.commons.io.FileUtils fu = new org.apache.commons.io.FileUtils();
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

}
