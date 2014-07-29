package fsu.jportal.resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
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

    /**
     * get all pages of the physical structure map
     */
    final static XPathExpression<Element> PYHS_STRUCT_EXPRESSION;

    static {
        List<Namespace> nsList = new ArrayList<>();
        nsList.add(MCRConstants.METS_NAMESPACE);
        nsList.add(MCRConstants.MODS_NAMESPACE);
        XPathFactory xf = XPathFactory.instance();

        PYHS_STRUCT_EXPRESSION = xf.compile("mets:structMap[@TYPE='PHYSICAL']//mets:div[@TYPE='page']",
            Filters.element(), null, nsList);
    }

    @Override
    protected BiMap<String, String> parseImageALTOMap(Element mets) {
        List<Element> pageDivs = PYHS_STRUCT_EXPRESSION.evaluate(mets);
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

    protected Map<String, String> parseFiles(XPathExpression<Element> expression, Element context) {
        List<Element> fileElements = expression.evaluate(context);
        Map<String, String> returnMap = new HashMap<>();
        for (Element fileElement : fileElements) {
            String key = fileElement.getAttributeValue("ID");
            String value = fileElement.getChild("FLocat", MCRConstants.METS_NAMESPACE).getAttributeValue("href",
                MCRConstants.XLINK_NAMESPACE);
            returnMap.put(key, value);
        }
        return returnMap;
    }

    @Override
    protected String getFirstALTOIdOfLogicalDiv(Element div) {
        XPathExpression<Attribute> exp = XPathFactory.instance().compile("mets:div/mets:fptr/mets:area/@FILEID",
            Filters.attribute(), null, getNameSpaceList());
        Attribute fileId = exp.evaluateFirst(div);
        return fileId != null ? fileId.getValue() : null;
    }

}

//        JAXBContext context = JAXBContext.newInstance(Mets.class.getPackage().getName());
//        Unmarshaller createUnmarshaller = context.createUnmarshaller();
//        Object mets = createUnmarshaller.unmarshal(new File(path));
//        mets.toString();
//        Mets mets = METSUtils.unmarshal(new File(path));
//        List<MdSec> dmdSecs = mets.getDmdSec();
//        for(MdSec dmdSec : dmdSecs) {
//            MdWrap mdWrap = dmdSec.getMdWrap();
//            if(MetadataMDTYPE.MODS.equals(mdWrap.getAttrMDTYPE())) {
//                mdWrap.getXmlData().getNodes()
//            }
//        }
