package fsu.jportal.mets.test;

import static org.junit.Assert.*;

import fsu.jportal.backend.JPArticle;
import fsu.jportal.backend.JPVolume;
import fsu.jportal.mets.ConvertException;
import fsu.jportal.mets.LLZMetsImporter;
import fsu.jportal.mets.LLZMetsUtils;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.jdom2.*;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mycore.common.MCRConstants;
import org.mycore.common.MCRSystemUserInformation;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.datamodel.metadata.*;
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

        static {
            NS_LIST = new ArrayList<Namespace>();
            NS_LIST.add(Namespace.getNamespace("mets", "http://www.loc.gov/METS/"));
            NS_LIST.add(Namespace.getNamespace("mods", "http://www.loc.gov/mods/v3"));
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
                } else if (type.equals("volumeparts")) {
                    buildVolume(div, "Volume Parts");
                } else if (type.equals("rezension")) {
                    buildArticle(div);
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

            LOGGER.info("Article: " + logicalId + " - " + title.getValue());
        }

        private void buildVolume(Element logicalDiv, String defaultTitle) throws ConvertException {
            // title
            // order
            String order = logicalDiv.getAttributeValue("ORDER");

            LOGGER.info("Title: " + defaultTitle + " - Order: " +order);
            // recursive calls for children
            handleLogicalDivs(logicalDiv);
        }
    }

    @Test
//    @Ignore
    public void testLLZImport() throws Exception {
        BasicConfigurator.configure();

        InputStream xmlStream = getClass().getResourceAsStream("/mets/mets.xml");
        Document metsXML = new SAXBuilder().build(xmlStream);

        LLZMetsParser llzMetsParser = new LLZMetsParser();
        llzMetsParser.parse(metsXML);
    }
}

