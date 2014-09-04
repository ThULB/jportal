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

public class PPSMetsImporter extends MetsImporterBase {

    public PPSMetsImporter(Document metsDocument, MCRDerivate derivate) {
        super(metsDocument, derivate);
    }

    @Override
    protected Map<String, Element> parseLogicalStructMap(Element mets) {
        XPathExpression<Element> exp = XPathFactory.instance().compile(
            "mets:structMap[@TYPE='LOGICAL']//mets:div[@DMDID != '']", Filters.element(), null,
            getNameSpaceList());
        List<Element> logicalDmds = exp.evaluate(mets);
        Map<String, Element> logicalStructMap = new HashMap<>(logicalDmds.size());
        for (Element e : logicalDmds) {
            logicalStructMap.put(e.getAttributeValue("DMDID"), e);
        }
        return logicalStructMap;
    }

    @Override
    protected String getFirstALTOIdOfLogicalDiv(Element div) {
        XPathExpression<Attribute> exp = XPathFactory.instance().compile("mets:div//mets:fptr/mets:area/@FILEID",
            Filters.attribute(), null, getNameSpaceList());
        Attribute fileId = exp.evaluateFirst(div);
        return fileId != null ? fileId.getValue() : null;
    }

    @Override
    protected Map<String, String> parseALTOFiles(Element mets) {
        XPathExpression<Element> exp = XPathFactory.instance().compile(
            "mets:fileSec/mets:fileGrp[@ID='ALTOGRP']/mets:file", Filters.element(), null, getNameSpaceList());
        return parseFiles(exp, mets);
    }

    protected BiMap<String, String> parseImageALTOMap(Element mets) {
        HashBiMap<String, String> map = HashBiMap.create();

        XPathExpression<Element> exp = XPathFactory.instance().compile(
            "mets:structMap[@TYPE='PHYSICAL']//mets:div[@TYPE='Page']", Filters.element(), null, getNameSpaceList());

        List<Element> pageDivs = exp.evaluate(mets);
        for (Element pageDiv : pageDivs) {
            Element par = pageDiv.getChild("fptr", MCRConstants.METS_NAMESPACE).getChild("par",
                MCRConstants.METS_NAMESPACE);
            String imageID = null;
            String altoID = null;
            List<Element> areaList = par.getChildren("area", MCRConstants.METS_NAMESPACE);
            for (Element area : areaList) {
                Attribute fileIDAttr = area.getAttribute("FILEID");
                if (fileIDAttr == null) {
                    continue;
                }
                String fileID = fileIDAttr.getValue();
                if (fileID.startsWith("IMG")) {
                    imageID = fileID;
                } else if (fileID.startsWith("ALTO")) {
                    altoID = fileID;
                }
            }
            if (imageID != null && altoID != null) {
                map.put(imageID, altoID);
            }
        }
        return map;
    }

    @Override
    protected Map<String, String> parseImageFiles(Element mets) {
        XPathExpression<Element> exp = XPathFactory.instance().compile(
            "mets:fileSec/mets:fileGrp[@ID='IMGGRP']/mets:file", Filters.element(), null, getNameSpaceList());
        return parseFiles(exp, mets);
    }

    @Override
    protected Map<String, Element> parsePages(Element mets) {
        // TODO Auto-generated method stub
        return null;
    }

}
