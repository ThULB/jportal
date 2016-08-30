package fsu.jportal.frontend.cli;

import static fsu.jportal.util.ImprintUtil.getJournalConf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.hibernate.Session;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Text;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.transform.JDOMResult;
import org.jdom2.transform.JDOMSource;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.backend.hibernate.MCRHIBConnection;
import org.mycore.common.MCRPersistenceException;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.xml.MCRXSLTransformation;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.metadata.MCRMetaLinkID;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.cli.annotation.MCRCommand;
import org.mycore.frontend.cli.annotation.MCRCommandGroup;
import org.mycore.solr.MCRSolrClientFactory;
import org.mycore.solr.search.MCRSolrSearchUtils;

import fsu.jportal.backend.JPObjectConfiguration;
import fsu.jportal.backend.sort.JPLevelSorting;
import fsu.jportal.resolver.JournalFilesResolver;
import fsu.jportal.util.JPLevelSortingUtil;

@MCRCommandGroup(name = "JP Migrating Commands")
public class MigratingCMDs {
    private static Logger LOGGER = LogManager.getLogger(MigratingCMDs.class);

    @MCRCommand(helpKey = "Set intro xml in journal properties.", syntax = "set intro prop")
    public static void setIntroProp() {
        List<String> jpjournalIDs = MCRXMLMetadataManager.instance().listIDsOfType("jpjournal");
        for (String jpjournalID : jpjournalIDs) {
            JournalFilesResolver journalFilesResolver = new JournalFilesResolver();
            try {
                Source resolve = journalFilesResolver.resolve("journalFile:" + jpjournalID + "/intro.xml+", null);
                if (resolve != null) {
                    JPObjectConfiguration journalConf = getJournalConf(jpjournalID);
                    journalConf.set("greeting", "intro");
                    journalConf.store();
                    LOGGER.info("Set intro for " + jpjournalID);
                }
            } catch (TransformerException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    @MCRCommand(helpKey = "Move intro xml from webapp into data folder.", syntax = "migrate intro xml")
    public static void migrateIntroXML() throws IOException {
        List<String> journalIDs = (List<String>) MCRXMLMetadataManager.instance().listIDsOfType("jpjournal");
        XPathExpression<Element> hiddenWebContextXpath = XPathFactory.instance().compile(
            "/mycoreobject/metadata/hidden_websitecontexts/hidden_websitecontext", Filters.element());

        String mcrBaseDir = MCRConfiguration.instance().getString("MCR.basedir");
        String webappDir = mcrBaseDir + "/build/webapps";
        String journalFileBase = MCRConfiguration.instance().getString("JournalFileFolder");

        for (String journalID : journalIDs) {
            Document journalXML;
            try {
                journalXML = MCRXMLMetadataManager.instance().retrieveXML(MCRObjectID.getInstance(journalID));
            } catch (Exception exc) {
                LOGGER.error("Unable to retrieve journal " + journalID, exc);
                continue;
            }
            Element journalContextElement = hiddenWebContextXpath.evaluateFirst(journalXML);
            if (journalContextElement != null) {
                String pathToJournalXML = webappDir + journalContextElement.getText();
                File journalXMLFile = new File(pathToJournalXML);
                LOGGER.info("Move " + pathToJournalXML + " for journal " + journalID);

                if (journalXMLFile.exists()) {
                    String journalFilePath = journalFileBase + File.separator + journalID;
                    File journalFile = new File(journalFilePath);
                    if (!journalFile.exists()) {
                        journalFile.mkdirs();
                    }

                    String introXMLPath = journalFilePath + File.separator + "intro.xml";
                    File introXMLFile = new File(introXMLPath);
                    FileInputStream journalXMLInputStream = new FileInputStream(journalXMLFile);
                    FileOutputStream introXMLOutputStream = new FileOutputStream(introXMLFile);
                    IOUtils.copy(journalXMLInputStream, introXMLOutputStream);
                    introXMLOutputStream.close();
                    journalXMLInputStream.close();
                    LOGGER.info("Successful moved " + pathToJournalXML + " to " + introXMLPath);
                }
            } else {
                LOGGER.info("Journal " + journalID + " has no journal context!");
            }
        }
    }

    @MCRCommand(helpKey = "Migrate template name from navigation.xml", syntax = "migrate template")
    public static void migrateTemplate() throws JDOMException, IOException {
        List<String> journalIDs = (List<String>) MCRXMLMetadataManager.instance().listIDsOfType("jpjournal");
        XPathExpression<Text> hiddenWebContextXpath = XPathFactory.instance().compile(
            "/mycoreobject/metadata/hidden_websitecontexts/hidden_websitecontext/text()", Filters.text());
        //load navigation.xml
        String mcrBaseDir = MCRConfiguration.instance().getString("MCR.basedir");
        String navigationDir = mcrBaseDir + "/build/webapps/config/navigation.xml";
        File navigationXMLFile = new File(navigationDir);
        SAXBuilder builder = new SAXBuilder();
        Document navigationXML = (Document) builder.build(navigationXMLFile);

        for (String journalID : journalIDs) {
            Document journalXML;
            try {
                journalXML = MCRXMLMetadataManager.instance().retrieveXML(MCRObjectID.getInstance(journalID));
            } catch (Exception exc) {
                LOGGER.error("Unable to retrieve journal " + journalID, exc);
                continue;
            }
            String journalContextPath = hiddenWebContextXpath.evaluateFirst(journalXML).getText().trim();

            if (journalContextPath != null && !journalContextPath.equals("")) {
                LOGGER.info("Add Termplate a Template  to " + journalContextPath);
                XPathExpression<Attribute> templateXpath = XPathFactory.instance()
                                                                       .compile(
                                                                           "/navigation/navi-main/item[@href='/content/main/journalList.xml']/item[@href='"
                                                                               + journalContextPath + "']/@template",
                                                                           Filters.attribute());
                String journalTemplate = templateXpath.evaluateFirst(navigationXML).getValue();
                if (journalTemplate != null && !journalTemplate.equals("")) {
                    LOGGER.info("Template: " + journalTemplate);
                    Element journalMetadata = journalXML.getRootElement().getChild("metadata");
                    //create hidden_template            
                    Element template = new Element("hidden_template");
                    template.setText(journalTemplate);
                    template.setAttribute("inherited", "0");
                    template.setAttribute("form", "plain");

                    //create hidden_templates  
                    Element templates = new Element("hidden_templates");
                    templates.setAttribute("class", "MCRMetaLangText");
                    templates.setAttribute("heritable", "false");
                    templates.setAttribute("notinherit", "false");
                    templates.addContent(template);

                    journalMetadata.addContent(templates);

                    Date date = new Date();
                    MCRXMLMetadataManager.instance().update(MCRObjectID.getInstance(journalID), journalXML, date);
                    LOGGER.info("Successful add " + journalTemplate + " to " + journalContextPath);
                } else {
                    LOGGER.info("No Template found for " + journalID);
                }
            }
        }
    }

    @MCRCommand(help = "Replace ':' in categID with '_'", syntax = "fix colone in categID")
    public static void fixCategID() throws TransformerException {
        Session dbSession = MCRHIBConnection.instance().getSession();
        dbSession.createSQLQuery("update MCRCATEGORY set CATEGID=replace(categid,':','-') where CATEGID like '%:%'")
                 .executeUpdate();

        MCRXMLMetadataManager xmlMetaManager = MCRXMLMetadataManager.instance();
        List<String> listIDs = xmlMetaManager.listIDs();

        InputStream resourceAsStream = MigratingCMDs.class.getResourceAsStream("/xsl/replaceColoneInCategID.xsl");
        Source stylesheet = new StreamSource(resourceAsStream);
        Transformer xsltTransformer = MCRXSLTransformation.getInstance().getStylesheet(stylesheet).newTransformer();

        XPathExpression<Element> xlinkLabel = XPathFactory.instance().compile(
            "/mycoreobject/metadata/*[@class='MCRMetaClassification']/*[contains(@categid,':')]", Filters.element());

        for (String ID : listIDs) {
            MCRObjectID mcrid = MCRObjectID.getInstance(ID);
            Document mcrObjXML;
            try {
                mcrObjXML = xmlMetaManager.retrieveXML(mcrid);
            } catch (Exception exc) {
                LOGGER.error("Unable to retrieve mcr object " + mcrid, exc);
                continue;
            }
            if (!xlinkLabel.evaluate(mcrObjXML).isEmpty()) {
                Source xmlSource = new JDOMSource(mcrObjXML);
                JDOMResult jdomResult = new JDOMResult();
                xsltTransformer.transform(xmlSource, jdomResult);
                Document migratedMcrObjXML = jdomResult.getDocument();
                try {
                    xmlMetaManager.update(mcrid, migratedMcrObjXML, new Date());
                    LOGGER.info("Replace ':' in categID for " + mcrid);
                } catch (IOException e) {
                    LOGGER.error("Failed replace ':' in categID for " + mcrid);
                    e.printStackTrace();
                }
            } else {
                LOGGER.info("Nothing to replace for " + mcrid);
            }
        }
    }

    @MCRCommand(help = "fixes child doublets and removes all object which doesn't exist", syntax = "fix children of journal {0}")
    public static List<String> fixJournalChildren(String journalId) throws SolrServerException {
        String query = "+journalID:" + journalId + " +objectType:jpvolume";
        SolrClient solrClient = MCRSolrClientFactory.getSolrClient();
        return MCRSolrSearchUtils.listIDs(solrClient, query).stream().map(id -> {
            return "fix children of volume " + id.toString();
        }).collect(Collectors.toList());
    }

    @MCRCommand(help = "fixes child doublets and removes all object which doesn't exist", syntax = "fix children of volume {0}")
    public static void fixVolumeChildren(String volumeId) throws MCRPersistenceException, IOException {
        MCRObjectID mcrVolumeId = MCRObjectID.getInstance(volumeId);
        MCRObject volume = MCRMetadataManager.retrieveMCRObject(mcrVolumeId);
        List<MCRMetaLinkID> toRemove = new ArrayList<>();
        List<MCRMetaLinkID> children = volume.getStructure().getChildren();
        int oldSize = children.size();
        // remove invalid
        for (MCRMetaLinkID id : children) {
            if (!MCRMetadataManager.exists(id.getXLinkHrefID())) {
                toRemove.add(id);
                continue;
            }
        }
        toRemove.forEach(children::remove);
        // remove doublets
        int index = 0;
        while (index < children.size()) {
            MCRMetaLinkID id = children.get(index);
            if (children.stream().filter(id::equals).count() > 1) {
                children.remove(id);
            } else {
                index++;
            }
        }
        if (oldSize != children.size()) {
            Document xml = volume.createXML();
            MCRXMLMetadataManager.instance().update(mcrVolumeId, new MCRJDOMContent(xml).asByteArray(), new Date());
        }
    }

    @MCRCommand(help = "analyzes the structure of all journals and adds level sorting", syntax = "add level sorting")
    public static List<String> addLevelSorting() throws Exception {
        List<String> journalIds = MCRSolrSearchUtils.listIDs(MCRSolrClientFactory.getSolrClient(),
            "objectType:jpjournal");
        return journalIds.stream().map(id -> {
            return "_add level sorting for journal " + id;
        }).collect(Collectors.toList());
    }

    @MCRCommand(help = "analyzes the journal structure and adds a new level sorting for {journal}", syntax = "_add level sorting for journal {0}")
    public static void addLevelSortingForJournal(String id) throws Exception {
        MCRObjectID journalId = MCRObjectID.getInstance(id);
        JPLevelSorting levelSorting = JPLevelSortingUtil.analyze(journalId);

        LOGGER.info("use level sorting: " + levelSorting.toJSON().toString());
        LOGGER.info("store level sorting...");
        JPLevelSortingUtil.store(journalId, levelSorting);

        JPLevelSortingUtil.apply(journalId, levelSorting);
    }

    @MCRCommand(help = "apply level sorting on all journals", syntax = "apply level sorting")
    public static List<String> applyLevelSorting() throws Exception {
        List<String> journalIds = MCRSolrSearchUtils.listIDs(MCRSolrClientFactory.getSolrClient(),
            "objectType:jpjournal");
        return journalIds.stream().map(id -> {
            return "_apply level sorting for journal " + id;
        }).collect(Collectors.toList());
    }

    @MCRCommand(help = "apply level sorting for {journal}", syntax = "_apply level sorting for journal {0}")
    public static void applyLevelSortingForJournal(String id) throws Exception {
        JPLevelSortingUtil.reapply(MCRObjectID.getInstance(id));
    }

}
