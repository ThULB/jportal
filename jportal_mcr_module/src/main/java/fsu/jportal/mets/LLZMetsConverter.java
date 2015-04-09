package fsu.jportal.mets;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.mets.misc.StructLinkGenerator;
import org.mycore.mets.model.IMetsElement;
import org.mycore.mets.model.Mets;
import org.mycore.mets.model.files.FLocat;
import org.mycore.mets.model.files.File;
import org.mycore.mets.model.files.FileGrp;
import org.mycore.mets.model.struct.AbstractLogicalDiv;
import org.mycore.mets.model.struct.Area;
import org.mycore.mets.model.struct.Fptr;
import org.mycore.mets.model.struct.LOCTYPE;
import org.mycore.mets.model.struct.LogicalDiv;
import org.mycore.mets.model.struct.LogicalStructMap;
import org.mycore.mets.model.struct.LogicalSubDiv;
import org.mycore.mets.model.struct.PhysicalDiv;
import org.mycore.mets.model.struct.PhysicalStructMap;
import org.mycore.mets.model.struct.PhysicalSubDiv;
import org.mycore.mets.model.struct.Seq;

import fsu.jportal.mets.LLZMetsUtils.AltoHrefStrategy;
import fsu.jportal.mets.LLZMetsUtils.FileHrefStrategy;
import fsu.jportal.mets.LLZMetsUtils.TiffHrefStrategy;

/**
 * Converts the llz output format from uibk to the mycore mets format.
 * 
 * @author Matthias Eichner
 */
public class LLZMetsConverter {

    public Mets convert(Document llzDocument) throws ConvertException {
        try {
            Mets mets = new Mets();
            Element llz = llzDocument.getRootElement();
            handleFileSection(llz, mets);
            handlePhysicalStructure(llz, mets);
            handleLogicalStructure(llz, mets);
            new StructLinkGenerator().generate(mets);
            return mets;
        } catch (Exception exc) {
            throw new ConvertException("Unable to convert mets document", exc);
        }
    }

    private void handleFileSection(Element uibk, Mets mcr) throws URISyntaxException {
        FileGrp masterGroup = handleFileGroup(uibk, "MASTER",
            "mets:fileSec//mets:fileGrp[@ID='OCRMasterFiles']/mets:file", "image/tiff", new TiffHrefStrategy());
        FileGrp altoGroup = handleFileGroup(uibk, "ALTO",
            "mets:fileSec//mets:fileGrp[@ID='Index_ALTO_Files']/mets:file", "text/xml", new AltoHrefStrategy());
        mcr.getFileSec().addFileGrp(masterGroup);
        mcr.getFileSec().addFileGrp(altoGroup);
    }

    private FileGrp handleFileGroup(Element llzElement, String fileGroup, String xpath, String mimeType,
        FileHrefStrategy hrefStrategy) throws URISyntaxException {
        FileGrp group = new FileGrp(fileGroup);
        XPathExpression<Element> xpathExp = XPathFactory.instance().compile(xpath, Filters.element(), null,
            IMetsElement.METS);
        List<Element> files = xpathExp.evaluate(llzElement);
        for (Element fileElement : files) {
            String id = fileElement.getAttributeValue("ID");
            Element flocatElement = fileElement.getChild("FLocat", IMetsElement.METS);
            String href = flocatElement.getAttributeValue("href", IMetsElement.XLINK);
            File file = new File(id, mimeType);
            file.setFLocat(new FLocat(LOCTYPE.URL, hrefStrategy.get(href)));
            group.addFile(file);
        }
        return group;
    }

    private void handlePhysicalStructure(Element llzElement, Mets mets) {
        PhysicalStructMap structMap = (PhysicalStructMap) mets.getStructMap(PhysicalStructMap.TYPE);
        PhysicalDiv physicalDiv = new PhysicalDiv();
        structMap.setDivContainer(physicalDiv);

        XPathExpression<Element> xpathExp = XPathFactory.instance().compile(
            "mets:structMap[@TYPE='PHYSICAL']//mets:div[mets:fptr]", Filters.element(), null,
            IMetsElement.METS);
        List<Element> divs = new ArrayList<Element>(xpathExp.evaluate(llzElement));
        Collections.sort(divs, new Comparator<Element>() {
            @Override
            public int compare(Element e1, Element e2) {
                int o1 = Integer.valueOf(e1.getAttributeValue("ORDER"));
                int o2 = Integer.valueOf(e2.getAttributeValue("ORDER"));
                return Integer.compare(o1, o2);
            }
        });
        for (int i = 0; i < divs.size(); i++) {
            Element divElement = divs.get(i);
            PhysicalSubDiv div = new PhysicalSubDiv(divElement.getAttributeValue("ID"), "page", i + 1);
            List<Element> fptrs = divElement.getChildren("fptr", IMetsElement.METS);
            div.add(new Fptr(fptrs.get(0).getAttributeValue("FILEID")));
            div.add(new Fptr(fptrs.get(1).getAttributeValue("FILEID")));
            physicalDiv.add(div);
        }
    }

    private void handleLogicalStructure(Element llzElement, Mets mets) {
        LogicalStructMap structMap = (LogicalStructMap) mets.getStructMap(LogicalStructMap.TYPE);
        XPathExpression<Element> xpathExp = XPathFactory.instance().compile(
            "mets:structMap[@TYPE='logical_structmap']/mets:div", Filters.element(), null, IMetsElement.METS);
        Element volumeDiv = xpathExp.evaluateFirst(llzElement);
        LogicalDiv volume = new LogicalDiv(volumeDiv.getAttributeValue("ID"), "volume",
            volumeDiv.getAttributeValue("LABEL"));
        structMap.setDivContainer(volume);
        handleLogicalDivs(volumeDiv, volume);
    }

    private void handleLogicalDivs(Element divElement, AbstractLogicalDiv div) {
        List<Element> children = divElement.getChildren("div", IMetsElement.METS);
        for (Element subDivElement : children) {
            // create sub div
            String type = subDivElement.getAttributeValue("TYPE").toLowerCase();
            LogicalSubDiv subdDiv = getLogicalDiv(subDivElement);
            if (type.equals("issue") || type.equals("volumeparts")) {
                handleLogicalDivs(subDivElement, subdDiv);
                div.add(subdDiv);
                continue;
            } else if (type.equals("rezension") && subDivElement.getChildren().size() > 0) {
                handleRecension(subDivElement, subdDiv);
            } else if (type.equals("tp")) {
                subdDiv.setLabel("Titelblatt");
                subdDiv.setType("titlepage");
            } else if (type.equals("preface")) {
                subdDiv.setLabel("Vorwort");
                subdDiv.setType("preface");
            } else if (type.equals("toc")) {
                subdDiv.setLabel("Register");
                subdDiv.setType("toc");
            } else {
                continue;
            }
            handleLogicalFilePointer(subDivElement, subdDiv);
            div.add(subdDiv);
        }
    }

    private void handleRecension(Element recensionDiv, LogicalSubDiv recension) {
        // type
        recension.setType("article");
        // label
        String bibLabel = LLZMetsUtils.getBibLabel(recensionDiv);
        if (bibLabel != null) {
            recension.setLabel(bibLabel);
        }
        if (recension.getLabel() == null) {
            recension.setLabel("unknown " + recension.getType());
        }
    }

    private void handleLogicalFilePointer(Element divElement, AbstractLogicalDiv div) {
        XPathExpression<Element> areaExp = XPathFactory.instance().compile(
            "mets:div/mets:fptr/mets:area[contains(@FILEID, 'INDEXALTO')]", Filters.element(), null, IMetsElement.METS);
        List<Element> areas = areaExp.evaluate(divElement);
        String lastFileID = null, lastContentID = null;
        Fptr fptr = new Fptr();
        Seq seq = new Seq();
        fptr.getSeqList().add(seq);
        Area area = null;
        for (Element areaElement : areas) {
            String fileID = areaElement.getAttributeValue("FILEID");
            String contentID = areaElement.getAttributeValue("CONTENTIDS");
            if (!fileID.equals(lastFileID)) {
                if (area != null) {
                    area.setEnd(lastContentID);
                }
                area = new Area();
                area.setBetype("IDREF");
                area.setFileId(fileID);
                area.setBegin(contentID);
                seq.getAreaList().add(area);
            }
            lastFileID = fileID;
            lastContentID = contentID;
        }
        if (area != null) {
            area.setEnd(lastContentID);
            div.getFptrList().add(fptr);
        }
    }

    private LogicalSubDiv getLogicalDiv(Element divElement) {
        String id = divElement.getAttributeValue("ID");
        String type = divElement.getAttributeValue("TYPE").toLowerCase();
        String label = divElement.getAttributeValue("LABEL");
        String order = divElement.getAttributeValue("ORDER");
        LogicalSubDiv div = new LogicalSubDiv(id, type, label, Integer.valueOf(order));
        return div;
    }

}
