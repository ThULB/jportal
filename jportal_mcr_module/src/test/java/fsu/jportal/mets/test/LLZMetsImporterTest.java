package fsu.jportal.mets.test;

import org.jdom2.*;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Attributes;

/**
 * Created by chi on 02.04.15.
 */
public class LLZMetsImporterTest {
    @Test
    public void handleDerivateLink() {
//        LLZMetsImporter metsImporter = new LLZMetsImporter();
        InputStream metsIS = getClass().getResourceAsStream("/mets/mets.xml");
        ArrayList<Namespace> NS_LIST = new ArrayList<>();

        NS_LIST.add(Namespace.getNamespace("mets", "http://www.loc.gov/METS/"));
        NS_LIST.add(Namespace.getNamespace("mods", "http://www.loc.gov/mods/v3"));
        NS_LIST.add(Namespace.getNamespace("xlink", "http://www.w3.org/1999/xlink"));

//        mets:fptr FILEID="FID-THULB_129846422_1801_1802_LLZ_001_18010701_001-INDEXALTO"
        XPathExpression<Attribute> expression  = XPathFactory.instance().compile(
                "mets:structMap[@TYPE='PHYSICAL']/mets:div[@TYPE='physSequence']/mets:div[mets:fptr/@FILEID='FID-THULB_129846422_1801_1802_LLZ_001_18010701_001-INDEXALTO']/mets:fptr[contains(@FILEID,'-OCRMASTER')]/@FILEID", Filters.attribute(), null, NS_LIST);
        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("id", null);
        XPathExpression<Attribute> FILE_EXPRESSION = XPathFactory.instance().compile(
                "mets:fileSec/mets:fileGrp/mets:fileGrp[@ID='OCRMasterFiles']/mets:file[@ID=$id]/mets:FLocat/@xlink:href",
                Filters.attribute(), vars, NS_LIST);

        SAXBuilder saxBuilder = new SAXBuilder();
        try {
            Document metsDoc = saxBuilder.build(metsIS);
            Element rootElement = metsDoc.getRootElement();
            Attribute element = expression.evaluateFirst(rootElement);
            String ocrId = element.getValue();
            System.out.println("Elem: " + ocrId);
            FILE_EXPRESSION.setVariable("id", ocrId);
            Attribute hrefAttr = FILE_EXPRESSION.evaluateFirst(rootElement);
            System.out.println("File: " + hrefAttr.getValue());
//            for (Element child : metsDoc.getRootElement().getChildren()) {
//                System.out.println("Elem: " + child.getName());
//            }
            ;
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

