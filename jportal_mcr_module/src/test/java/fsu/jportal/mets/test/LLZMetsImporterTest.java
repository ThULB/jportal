package fsu.jportal.mets.test;

import static org.junit.Assert.*;

import fsu.jportal.backend.JPArticle;
import fsu.jportal.mets.ConvertException;
import fsu.jportal.mets.LLZMetsUtils;
import mockit.Invocation;
import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.jdom2.*;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mycore.common.MCRConstants;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.transformer.MCRContentTransformer;
import org.mycore.common.xml.MCRLayoutTransformerFactory;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.mets.model.IMetsElement;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chi on 02.04.15.
 */
@RunWith(JMockit.class)
public class LLZMetsImporterTest {

    public static class ImportObject{
        private String title;
        private int order;
        private String type;
        private ArrayList<ImportObject> children = new ArrayList<ImportObject>();

        public ImportObject(String type) {
            this.type = type;
        }

        public void add(ImportObject child){
            children.add(child);
        }

        public void setTitle(String title){
            this.title = title;
        }
    }

    public static class LLZMetsParser{
        private static Logger LOGGER = Logger.getLogger(LLZMetsParser.class);

        private static final ArrayList<org.jdom2.Namespace> NS_LIST;

        private static final XPathExpression<Element> LOGICAL_STRUCTMAP_EXPRESSION;

        private static final XPathExpression<Element> MODS_EXPRESSION;

        private static final XPathExpression<Text> TITLE_EXPRESSION;

        private static final XPathExpression<Text> HEADING_EXPRESSION;

        private static final XPathExpression<Attribute> FILEID_EXPRESSION;

        private static final XPathExpression<Attribute> FILE_EXPRESSION;

        private static final XPathExpression<Attribute> OCR_EXPRESSION;
        private static final Namespace MODS_NS = Namespace.getNamespace("mods", "http://www.loc.gov/mods/v3");

        static {
            NS_LIST = new ArrayList<Namespace>();
            NS_LIST.add(Namespace.getNamespace("mets", "http://www.loc.gov/METS/"));
            NS_LIST.add(MODS_NS);
            NS_LIST.add(Namespace.getNamespace("xlink", "http://www.w3.org/1999/xlink"));
            Map<String, Object> vars = new HashMap<String, Object>();
            vars.put("id", null);
            LOGICAL_STRUCTMAP_EXPRESSION = XPathFactory.instance().compile(
                    "mets:structMap[@TYPE='logical_structmap']/mets:div", Filters.element(), null, NS_LIST);
            MODS_EXPRESSION = XPathFactory.instance().compile("mets:dmdSec[@ID=$id]/mets:mdWrap/mets:xmlData/mods:mods",
                    Filters.element(), vars, NS_LIST);
            TITLE_EXPRESSION = XPathFactory.instance().compile("mods:recordInfo/mods:recordOrigin/text()", Filters.text(),
                    null, NS_LIST);
            HEADING_EXPRESSION = XPathFactory.instance().compile("/add/doc/field[@name='heading_base']/text()",
                    Filters.text());
            FILEID_EXPRESSION = XPathFactory.instance().compile(
                    "mets:div/mets:fptr/mets:area/@FILEID", Filters.attribute(), null, NS_LIST);
            FILE_EXPRESSION = XPathFactory.instance().compile(
                    "mets:fileSec/mets:fileGrp/mets:fileGrp[@ID='OCRMasterFiles']/mets:file[@ID=$id]/mets:FLocat/@xlink:href",
                    Filters.attribute(), vars, NS_LIST);
            OCR_EXPRESSION  = XPathFactory.instance().compile(
                    "mets:structMap[@TYPE='PHYSICAL']/mets:div[@TYPE='physSequence']/mets:div[mets:fptr/@FILEID=$id]/mets:fptr[contains(@FILEID,'-OCRMASTER')]/@FILEID", Filters.attribute(), vars, NS_LIST);

        }

        private String lastDmdID;

        private Document mets;

        public void parse(Document metsDocument) {
            this.mets = metsDocument;
            Element rootDiv = LOGICAL_STRUCTMAP_EXPRESSION.evaluateFirst(mets.getRootElement());
            try {
                handleLogicalDivs(rootDiv);
            } catch (ConvertException e) {
                e.printStackTrace();
            }
        }

        private void handleLogicalDivs(Element parentDiv) throws ConvertException {
            List<Element> children = parentDiv.getChildren("div", IMetsElement.METS);
            for (Element div : children) {
                String type = div.getAttributeValue("TYPE").toLowerCase();
                if (type.equals("issue")) {
                    String title = div.getAttributeValue("LABEL");
                    LOGGER.info("Issue: " + title);
                    buildVolume(div, title);

                    ImportObject vol = new ImportObject("vol");
                    vol.setTitle(title);
                } else if (type.equals("volumeparts")) {
                    buildVolume(div, "Volume Parts");
                } else if (type.equals("rezension")) {
                    buildArticle(div);
                } else if (type.equals("tp") || type.equals("preface") || type.equals("toc")) {
                    String title = type.equals("tp") ? "Titelblatt" : type.equals("preface") ? "Vorwort" : "Register";
                    handleArticleOrder(div);
                    handleDerivateLink(div);
                }
            }
        }

        private void buildArticle(Element div) throws ConvertException {
            String dmdId = LLZMetsUtils.getDmDId(div);
            if (dmdId != null) {
                lastDmdID = dmdId;
            } else if(lastDmdID != null){
                dmdId = lastDmdID;
            } else {
                throw new ConvertException("Cannot create article cause of missing DMDID. ID="
                        + div.getAttributeValue("ID"));
            }

            MODS_EXPRESSION.setVariable("id", dmdId);
            Element mods = MODS_EXPRESSION.evaluateFirst(mets.getRootElement());
            if (mods == null) {
                throw new ConvertException("Could not find referenced dmd entry " + dmdId + " in dmd section.");
            }

            buildArticle(mods, div);
        }

        private void buildArticle(Element mods, Element logicalDiv) {
            String logicalId = logicalDiv.getAttributeValue("ID");
            // title
            Text title = TITLE_EXPRESSION.evaluateFirst(mods);

            // participants - we only create participants which have a gnd id
            for (Element name : mods.getChildren("name", MODS_NS)) {
                String gndId = getGNDId(name);
            }
            // order
            handleArticleOrder(logicalDiv);

            // id's
            List<Element> identifiers = mods.getChildren("identifier", MODS_NS);
            for (Element identifier : identifiers) {
                String attr = identifier.getAttributeValue("type");
                if (attr != null) {
                    String type = attr.toLowerCase();
                    type = type.equals("gbv") ? "ppn" : type;
                    String id = identifier.getTextNormalize();
                    if (id.startsWith("(")) {
                        // don't store the queries
                        id = id.substring(6);
                    }
                }
            }
            // derivate link
            handleDerivateLink(logicalDiv);

            LOGGER.info("Article: " + logicalId + " - " + title.getValue());
        }

        private String getGNDId(Element name) {
            String authorityURI = name.getAttributeValue("authorityURI");
            if (!"http://d-nb.info/gnd/".equals(authorityURI)) {
                return null;
            }

            String valueURI = name.getAttributeValue("valueURI");
            if (valueURI == null) {
                return null;
            }

            return valueURI.substring(valueURI.lastIndexOf("/") + 1);
        }

        private void buildVolume(Element logicalDiv, String defaultTitle) throws ConvertException {
            // title
            // order
            String order = logicalDiv.getAttributeValue("ORDER");

            LOGGER.info("Title: " + defaultTitle + " - Order: " +order);
            // recursive calls for children
            handleLogicalDivs(logicalDiv);
        }

        private void handleArticleOrder(Element logicalDiv) {
            String order = logicalDiv.getAttributeValue("ORDER");
            if (order == null) {
                String msg = "ORDER attribute of logical div "
                        + logicalDiv.getAttributeValue("ID") + " is not set!";
                LOGGER.warn(msg);
            }
        }

        public void handleDerivateLink(Element logicalDiv) {
            String logicalId = logicalDiv.getAttributeValue("ID");
            Attribute fileIdAttr = FILEID_EXPRESSION.evaluateFirst(logicalDiv);
            if (fileIdAttr != null) {
                Element rootElement = mets.getRootElement();
                OCR_EXPRESSION.setVariable("id",fileIdAttr);
                Attribute ocrIdAttr = OCR_EXPRESSION.evaluateFirst(rootElement);
                if (ocrIdAttr != null) {
                    String ocrId = ocrIdAttr.getValue();
                    FILE_EXPRESSION.setVariable("id", ocrId);
                    Attribute hrefAttr = FILE_EXPRESSION.evaluateFirst(rootElement);
                    if (hrefAttr != null) {
                        String href = hrefAttr.getValue();
                    }
                }
            }
        }
    }

    @Test
    public void testLLZImport() throws Exception {

        BasicConfigurator.configure();

        InputStream xmlStream = getClass().getResourceAsStream("/mets/mets.xml");
        Document metsXML = new SAXBuilder().build(xmlStream);

        LLZMetsParser llzMetsParser = new LLZMetsParser();
        llzMetsParser.parse(metsXML);
    }

    @Test
    public void testNewImporter() throws Exception {
        new MockUp<LLZMetsParser>(){
            int invCount = 0;
            @Mock(invocations = 2)
            void handleLogicalDivs(Element parentDiv){
                assertNotNull(parentDiv);
                System.out.println(invCount + ": " + parentDiv.getName());
                invCount++;
            }
        };

        BasicConfigurator.configure();

        LLZMetsParser llzMetsParser = new LLZMetsParser();
        InputStream metsStream = getClass().getResourceAsStream("/mets/mets.xml");
        Document metsXML = new SAXBuilder().build(metsStream);

        llzMetsParser.parse(metsXML);

        InputStream mets_wfs2_Stream = getClass().getResourceAsStream("/mets/mets_wfs2_Abgleich.xml");
        Document mets_wfs2_XML = new SAXBuilder().build(mets_wfs2_Stream);

        llzMetsParser.parse(mets_wfs2_XML);
    }

    public class ParserMock extends MockUp<LLZMetsParser>{
        private int volCount = 0;
        private int artCount = 0;
        private int orderCount = 0;
        private int linkCount = 0;

        @Mock
        void buildVolume(Invocation inv, Element logicalDiv, String defaultTitle){
            assertNotNull(logicalDiv);
            assertNotNull(defaultTitle);
            volCount++;
            inv.proceed(logicalDiv, defaultTitle);
        }

        @Mock
        void buildArticle(Invocation inv, Element mods, Element logicalDiv){
            assertNotNull(logicalDiv);
            artCount++;
            inv.proceed(mods, logicalDiv);
        }

        @Mock
        void handleArticleOrder(Invocation inv, Element logicalDiv) {
            assertNotNull(logicalDiv);
            orderCount++;
            inv.proceed(logicalDiv);
        }

        @Mock
        void handleDerivateLink(Invocation inv, Element logicalDiv) {
            assertNotNull(logicalDiv);
            linkCount++;
            inv.proceed(logicalDiv);
        }

        public void resetCounter(){
            volCount = 0;
            artCount = 0;
            orderCount = 0;
            linkCount = 0;
        }

        public int getVolCount(){
            return volCount;
        }

        public int getArtCount(){
            return artCount;
        }

        public int getOrderCount(){
            return orderCount;
        }

        public int getLinkCount(){
            return linkCount;
        }
    }

    @Test
    public void testCountIssue() throws Exception {
        ParserMock parserMock = new ParserMock();

        BasicConfigurator.configure();

        LLZMetsParser llzMetsParser = new LLZMetsParser();
        InputStream metsStream = getClass().getResourceAsStream("/mets/mets.xml");
        Document metsXML = new SAXBuilder().build(metsStream);

        llzMetsParser.parse(metsXML);
        int mets_Vol = parserMock.getVolCount();
        int mets_Art = parserMock.getArtCount();
        int mets_Order = parserMock.getOrderCount();
        int mets_Link = parserMock.getLinkCount();

        parserMock.resetCounter();

        InputStream mets_wfs2_Stream = getClass().getResourceAsStream("/mets/mets_wfs2_Abgleich.xml");
        Document mets_wfs2_XML = new SAXBuilder().build(mets_wfs2_Stream);

        llzMetsParser.parse(mets_wfs2_XML);
        int mets_wfs2_Vol = parserMock.getVolCount();
        int mets_wfs2_Art = parserMock.getArtCount();
        int mets_wfs2_Order = parserMock.getOrderCount();
        int mets_wfs2_Link = parserMock.getLinkCount();

        System.out.println("Vol: " + mets_Vol + " # " + mets_wfs2_Vol);
        assertEquals(mets_Vol, mets_wfs2_Vol);
        System.out.println("Art: " + mets_Art + " # " + mets_wfs2_Art);
        assertEquals(mets_Art, mets_wfs2_Art);
        System.out.println("Order: " + mets_Order + " # " + mets_wfs2_Order);
        assertEquals(mets_Order, mets_wfs2_Order);
        System.out.println("Link: " + mets_Link + " # " + mets_wfs2_Link);
        assertEquals(mets_Link, mets_wfs2_Link);
    }
}

