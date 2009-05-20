package org.mycore.frontend.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;
import org.mycore.common.MCRConfiguration;
import org.mycore.common.MCRException;
import org.mycore.common.xml.MCRURIResolver;
import org.mycore.common.xml.MCRXMLHelper;
import org.mycore.datamodel.common.MCRXMLTableManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;

public class MCRContentTools extends MCRAbstractCommands {
    private static Logger LOGGER = Logger.getLogger(MCRContentTools.class.getName());

    public MCRContentTools() {
        super();

        MCRCommand com = null;

        // sync the journal label in navigation XML with object XML
        com = new MCRCommand("fix journal label {0} in navi", "org.mycore.frontend.cli.MCRContentTools.fixlabel String",
                "fix fix journal label [pattern in label] in navi");
        command.add(com);

        // remove journal entry in navigation XML which no longer exists in System
        com = new MCRCommand("clean navi", "org.mycore.frontend.cli.MCRContentTools.cleanNavi", "clean navi");
        command.add(com);
    }

    public static void fixlabel(String pattern) throws JDOMException, IOException {
        String mcrBasedir = MCRConfiguration.instance().getString("MCR.basedir");
        String naviFileLocation = mcrBasedir + "/build/webapps/config/navigation.xml";
        Document naviJDOM = MCRXMLHelper.getParser().parseXML(new FileInputStream(naviFileLocation), false);

        List<Element> nodes = XPath.selectNodes(naviJDOM, "//item[contains(./label/text(),'" + pattern + "')]");
        XPath itemXpath = XPath.newInstance("./item[contains(@href,'/receive/')]");
        XPath maintileXpath = XPath.newInstance("//metadata/maintitles/maintitle");

        XMLOutputter xo = new XMLOutputter();
        xo.setFormat(Format.getPrettyFormat());
        for (Element node : nodes) {
            // find journal entry in navigation.xml
            // retrieve journal ID --> get the maintitle from metadata XML
            Element item = (Element) itemXpath.selectSingleNode(node);
            String href = item.getAttributeValue("href");
            String mcrID = href.replaceFirst("/receive/", "").substring(0, 26);

            if (mcrID.startsWith("jportal_jpjournal")) {
                Element mcrObj = MCRURIResolver.instance().resolve("mcrobject:" + mcrID);
                String label = ((Element) maintileXpath.selectSingleNode(mcrObj)).getText();

                // change title in webcontext webpage XML eg. alz.xml
                String webPageXmlLocation = node.getAttributeValue("href");
                Element webPageXML = MCRURIResolver.instance().resolve("webapp:" + webPageXmlLocation);
                Element section = webPageXML.getChild("section");
                section.setAttribute("title", label);
                xo.output(webPageXML, new FileOutputStream(mcrBasedir + "/build/webapps" +webPageXmlLocation));

                // change label in navigation.xml
                String oldLabel = node.getChildText("label");
                node.getChild("label").setText(label);
                LOGGER.info("change " + oldLabel + " to " + label);
            }
        }

        xo.output(naviJDOM, new FileOutputStream(naviFileLocation));
        LOGGER.info("Fixed " + nodes.size() + " labels.");
    }

    public static void cleanNavi() throws JDOMException, MCRException, IOException {
        String naviFielLocation = MCRConfiguration.instance().getString("MCR.basedir") + "/build/webapps/config/navigation.xml";
        Document naviJDOM = MCRXMLHelper.getParser().parseXML(new FileInputStream(naviFielLocation), false);

        List<Element> nodes = XPath.selectNodes(naviJDOM, "//item[contains(@href,'/receive/jportal_jpjournal')]");

        for (Element node : nodes) {
            String href = node.getAttributeValue("href");
            String mcrID = href.replaceFirst("/receive/", "").substring(0, 26);
            // there are inconsistency in the schema /receive/jportal_jpjournal
            // some starts with http://zs.thulb.uni-jena.de/
            if (mcrID.startsWith("jportal_jpjournal")) {
                // test if the journal exists
                if (!MCRXMLTableManager.instance().exist(new MCRObjectID(mcrID))) {
                    // we have to remove the parent node
                    // <item href="/content/main/journals/pam/internal.xml....
                    //      .......
                    //      <item href="/receive/jportal_jpjournal....
                    node.getParentElement().detach();
                    LOGGER.info("Remove navigation entry for: " + mcrID);
                }
            }
        }
        XMLOutputter xo = new XMLOutputter();
        xo.setFormat(Format.getPrettyFormat());
        xo.output(naviJDOM, new FileOutputStream(naviFielLocation));
    }
}
