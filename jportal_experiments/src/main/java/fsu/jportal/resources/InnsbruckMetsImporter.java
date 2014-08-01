package fsu.jportal.resources;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.common.MCRConstants;
import org.mycore.datamodel.metadata.MCRDerivate;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class InnsbruckMetsImporter extends MetsImporterBase {

    public InnsbruckMetsImporter(Document metsDocument, MCRDerivate derivate) {
        super(metsDocument, derivate);
    }

    @Override
    protected Map<String, Element> parseLogicalStructMap(Element mets) {
        XPathExpression<Element> exp = XPathFactory.instance().compile(
            "mets:structMap[@TYPE='logical_structmap']//mets:div[@DMDID != '']", Filters.element(), null,
            getNameSpaceList());
        List<Element> logicalDmds = exp.evaluate(mets);
        Map<String, Element> logicalStructMap = new HashMap<>(logicalDmds.size());
        for (Element e : logicalDmds) {
            logicalStructMap.put(e.getAttributeValue("DMDID"), e);
        }
        return logicalStructMap;
    }

    @Override
    protected BiMap<String, String> parseImageALTOMap(Element mets) {
        // get all pages of the physical structure map
        XPathExpression<Element> exp = XPathFactory.instance().compile(
            "mets:structMap[@TYPE='PHYSICAL']//mets:div[@TYPE='page']", Filters.element(), null, getNameSpaceList());
        List<Element> pageDivs = exp.evaluate(mets);
        HashBiMap<String, String> map = HashBiMap.create(pageDivs.size());
        for (Element pageDiv : pageDivs) {
            List<Element> filePointers = pageDiv.getChildren("fptr", MCRConstants.METS_NAMESPACE);
            if (filePointers.size() >= 2) {
                map.put(filePointers.get(0).getAttributeValue("FILEID"), filePointers.get(1)
                    .getAttributeValue("FILEID"));
            }
        }
        return map;
    }

    @Override
    protected Map<String, String> parseALTOFiles(Element mets) {
        XPathExpression<Element> exp = XPathFactory.instance().compile(
            "mets:fileSec/mets:fileGrp[@ID='TextGroup']/mets:fileGrp[@ID='Index_ALTO_Files']/mets:file",
            Filters.element(), null, getNameSpaceList());
        return parseFiles(exp, mets);
    }

    @Override
    protected Map<String, String> parseImageFiles(Element mets) {
        XPathExpression<Element> exp = XPathFactory.instance().compile(
            "mets:fileSec/mets:fileGrp[@ID='ImageGroup']/mets:fileGrp[@ID='OCRMasterFiles']/mets:file",
            Filters.element(), null, getNameSpaceList());
        return parseFiles(exp, mets);
    }

    @Override
    protected String getFirstALTOIdOfLogicalDiv(Element div) {
        XPathExpression<Attribute> exp = XPathFactory.instance().compile("mets:div/mets:fptr/mets:area/@FILEID",
            Filters.attribute(), null, getNameSpaceList());
        Attribute fileId = exp.evaluateFirst(div);
        return fileId != null ? fileId.getValue() : null;
    }

}
