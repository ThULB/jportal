package fsu.jportal.mets.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.BasicConfigurator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.Text;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mycore.mets.model.IMetsElement;
import org.mycore.mets.model.Mets;
import org.mycore.mets.model.sections.DmdSec;
import org.mycore.mets.model.sections.MdWrapSection;
import org.mycore.mets.model.struct.LOCTYPE;
import org.mycore.mets.model.struct.LogicalDiv;
import org.mycore.mets.model.struct.LogicalStructMap;
import org.mycore.mets.model.struct.MDTYPE;
import org.mycore.mets.model.struct.MdRef;
import org.mycore.mets.model.struct.MdWrap;
import org.mycore.mets.validator.validators.SchemaValidator;

import fsu.jportal.mets.ConvertException;
import fsu.jportal.mets.LLZMetsConverter;
import fsu.jportal.mets.LLZMetsUtils;
import mockit.Invocation;
import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;

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
        private static Logger LOGGER = LogManager.getLogger(LLZMetsParser.class);

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
    @Ignore
    public void testLLZImport() throws Exception {

        BasicConfigurator.configure();

        InputStream xmlStream = getClass().getResourceAsStream("/mets/mets.xml");
        Document metsXML = new SAXBuilder().build(xmlStream);

        LLZMetsParser llzMetsParser = new LLZMetsParser();
        llzMetsParser.parse(metsXML);
    }

    @Test
    @Ignore
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
    @Ignore
    public void testCountIssue() throws Exception {
        ParserMock parserMock = new ParserMock();

        BasicConfigurator.configure();

        LLZMetsParser llzMetsParser = new LLZMetsParser();
        InputStream metsStream = getClass().getResourceAsStream("/mets/mets_1.xml");
        Document metsXML = new SAXBuilder().build(metsStream);

        llzMetsParser.parse(metsXML);
        int mets_Vol = parserMock.getVolCount();
        int mets_Art = parserMock.getArtCount();
        int mets_Order = parserMock.getOrderCount();
        int mets_Link = parserMock.getLinkCount();

        parserMock.resetCounter();

        InputStream mets_wfs2_Stream = getClass().getResourceAsStream("/mets/mets_1_wfs2_Abgleich.xml");
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

    @Test
    @Ignore
    public void testValidation() throws Exception {
        InputStream mets_wfs2_Stream = getClass().getResourceAsStream("/mets/mets_1_wfs2_Abgleich.xml");
        Document mets_wfs2_XML = new SAXBuilder().build(mets_wfs2_Stream);
        InputStream metsStream = getClass().getResourceAsStream("/mets/mets_1.xml");
        Document metsXML = new SAXBuilder().build(metsStream);
        SchemaValidator schemaValidator = new SchemaValidator();
        schemaValidator.validate(metsXML);
        schemaValidator.validate(mets_wfs2_XML);

    }

    @Test
    @Ignore
    public void testConverter() throws Exception {
        LLZMetsConverter llzMetsConverter = new LLZMetsConverter();
        InputStream mets_wfs2_Stream = getClass().getResourceAsStream("/mets/mets_wfs2_Abgleich.xml");
        Document mets_wfs2_XML = new SAXBuilder().build(mets_wfs2_Stream);
        InputStream metsStream = getClass().getResourceAsStream("/mets/mets.xml");
        Document metsXML = new SAXBuilder().build(metsStream);
        Mets mets_wfs2 = llzMetsConverter.convert(mets_wfs2_XML);
        Mets mets = llzMetsConverter.convert(metsXML);

        System.out.println("size: " + mets_wfs2.getFileSec().getFileGroup("MASTER").getFileList().size());
        System.out.println("size: " + mets.getFileSec().getFileGroup("MASTER").getFileList().size());
        assertEquals(mets_wfs2.getFileSec().getFileGroup("MASTER").getFileList().size(),
                mets.getFileSec().getFileGroup("MASTER").getFileList().size());

        LogicalStructMap structMap = (LogicalStructMap) mets_wfs2.getStructMap(LogicalStructMap.TYPE);

        List<LogicalDiv> children = structMap.getDivContainer().getChildren();
        for (LogicalDiv child : children) {
            System.out.println("Label: " + child.getLabel());

        }

    }

    @Test
    @Ignore
    public void testMets() throws Exception {
        InputStream mets_wfs2_Stream = getClass().getResourceAsStream("/mets/mets_wfs2_Abgleich.xml");
        Document mets_wfs2_XML = new SAXBuilder().build(mets_wfs2_Stream);
        createDmdSec(mets_wfs2_XML);
//        Mets mets = new Mets(mets_wfs2_XML);
//        for (FileGrp fileGrp : mets.getFileSec().getFileGroups()) {
//            XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
//            xmlOutputter.output(fileGrp.asElement(), System.out);
//        }

    }

    private void createDmdSec(Document source) throws JDOMException {
        XPathExpression<Element> xp = getXpathExpression("mets:mets/mets:dmdSec");
        XPathFactory.instance().compile("mets:mets/mets:dmdSec", Filters.element(), null, IMetsElement.METS);

        for (Element section : xp.evaluate(source)) {
            DmdSec dmdSec = new DmdSec(section.getAttributeValue("ID"));
//            dmdsecs.put(dmdSec.getId(), dmdSec);

            // handle mdWrap
            xp = getXpathExpression("mets:mdWrap");

            for (Element wrap : xp.evaluate(section)) {
                XPathExpression<Element> xmlDataXP = getXpathExpression("mets:xmlData/*");
                Element element = xmlDataXP.evaluateFirst(wrap);
                MdWrap mdWrap = new MdWrap(MdWrapSection.findTypeByName(wrap.getAttributeValue("MDTYPE")),
                        (Element) element.clone());
                dmdSec.setMdWrap(mdWrap);
            }

            // handle mdRef
            xp = getXpathExpression("mets:mdRef");

            for (Element refElem : xp.evaluate(section)) {
                LOCTYPE loctype = LOCTYPE.valueOf(refElem.getAttributeValue("LOCTYPE"));
                String mimetype = refElem.getAttributeValue("MIMETYPE");
                MDTYPE mdtype = MdWrapSection.findTypeByName(refElem.getAttributeValue("MDTYPE"));
                String href = refElem.getAttributeValue("href", IMetsElement.XLINK);
                MdRef mdRef = new MdRef(href, loctype, mimetype, mdtype, refElem.getText());
                dmdSec.setMdRef(mdRef);
            }

            XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
            try {
                xmlOutputter.output(dmdSec.asElement(), System.out);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private XPathExpression<Element> getXpathExpression(String xpath) {
        return XPathFactory.instance().compile(xpath, Filters.element(), null, IMetsElement.METS);
    }
}

