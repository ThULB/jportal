package fsu.jportal.mets;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

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

public class UIBKtoMCRConverter {

    public Document convert(Document uibkMetsDocument) throws ConvertException {
        try {
            Mets mets = new Mets();
            Element uibk = uibkMetsDocument.getRootElement();
            handleFileSection(uibk, mets);
            handlePhysicalStructure(uibk, mets);
            handleLogicalStructure(uibk, mets);
            new StructLinkGenerator().generate(mets);
            return mets.asDocument();
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

    private FileGrp handleFileGroup(Element uibk, String fileGroup, String xpath, String mimeType,
        FileHrefStrategy hrefStrategy) throws URISyntaxException {
        FileGrp group = new FileGrp(fileGroup);
        XPathExpression<Element> xpathExp = XPathFactory.instance().compile(xpath, Filters.element(), null,
            IMetsElement.METS);
        List<Element> files = xpathExp.evaluate(uibk);
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

    private void handlePhysicalStructure(Element uibk, Mets mets) {
        PhysicalStructMap structMap = (PhysicalStructMap) mets.getStructMap(PhysicalStructMap.TYPE);
        PhysicalDiv physicalDiv = new PhysicalDiv();
        structMap.setDivContainer(physicalDiv);

        XPathExpression<Element> xpathExp = XPathFactory.instance().compile(
            "mets:structMap[@TYPE='physical_structmap']//mets:div[mets:fptr]", Filters.element(), null,
            IMetsElement.METS);
        List<Element> divs = new ArrayList<Element>(xpathExp.evaluate(uibk));
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

    private void handleLogicalStructure(Element uibk, Mets mets) {
        LogicalStructMap structMap = (LogicalStructMap) mets.getStructMap(LogicalStructMap.TYPE);
        XPathExpression<Element> xpathExp = XPathFactory.instance().compile(
            "mets:structMap[@TYPE='logical_structmap']/mets:div", Filters.element(), null, IMetsElement.METS);
        Element volumeDiv = xpathExp.evaluateFirst(uibk);
        LogicalDiv volume = new LogicalDiv(volumeDiv.getAttributeValue("ID"), "volume",
            volumeDiv.getAttributeValue("LABEL"));
        structMap.setDivContainer(volume);
        handleLogicalRootDivs(volumeDiv, volume);
    }

    private void handleLogicalRootDivs(Element volumeDiv, LogicalDiv volume) {
        List<Element> children = volumeDiv.getChildren("div", IMetsElement.METS);
        for (Element div : children) {
            String type = div.getAttributeValue("TYPE").toLowerCase();
            if (type.equals("issue")) {
                handleIssue(div, volume);
            } else if (type.equals("volumeparts")) {
                handleVolumeParts(div, volume);
            }
        }
    }

    private void handleVolumeParts(Element div, LogicalDiv volume) {
        // TODO Auto-generated method stub

    }

    private void handleIssue(Element issueDiv, LogicalDiv volume) {
        LogicalSubDiv issue = getLogicalDiv(issueDiv);
        List<Element> children = issueDiv.getChildren("div", IMetsElement.METS);
        for (Element child : children) {
            String type = child.getAttributeValue("TYPE").toLowerCase();
            if (type.equals("rezension")) {
                handleRecension(child, issue);
            }
        }
        volume.add(issue);
    }

    private void handleRecension(Element recensionDiv, LogicalSubDiv issue) {
        LogicalSubDiv recension = getLogicalDiv(recensionDiv);
        // type
        recension.setType("article");
        // label
        XPathExpression<Element> bibExp = XPathFactory.instance().compile(
            "mets:div[@TYPE='Bibliographischer Eintrag']", Filters.element(), null, IMetsElement.METS);
        Element bibEntry = bibExp.evaluateFirst(recensionDiv);
        if (bibEntry != null) {
            String label = bibEntry.getAttributeValue("LABEL");
            if (label != null) {
                recension.setLabel(label);
            }
        }
        if (recension.getLabel() == null) {
            recension.setLabel("unknown " + recension.getType());
        }
        // fptr
        XPathExpression<Element> areaExp = XPathFactory.instance().compile(
            "mets:div/mets:fptr/mets:area[contains(@FILEID, 'INDEXALTO')]", Filters.element(), null, IMetsElement.METS);
        List<Element> areas = areaExp.evaluate(recensionDiv);
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
            recension.getFptrList().add(fptr);
        }
        issue.add(recension);
    }

    private LogicalSubDiv getLogicalDiv(Element divElement) {
        String id = divElement.getAttributeValue("ID");
        String type = divElement.getAttributeValue("TYPE").toLowerCase();
        String label = divElement.getAttributeValue("LABEL");
        String order = divElement.getAttributeValue("ORDER");
        LogicalSubDiv div = new LogicalSubDiv(id, type, label, Integer.valueOf(order));
        return div;
    }

    // hell, how bad i'm waiting for java 8...
    private static interface FileHrefStrategy {
        public String get(String href) throws URISyntaxException;
    }

    private static class TiffHrefStrategy implements FileHrefStrategy {
        private static Pattern JPG_PATTERN = Pattern.compile("\\.jpg", Pattern.CASE_INSENSITIVE);

        @Override
        public String get(String href) throws URISyntaxException {
            URI uri = new URI(href);
            return JPG_PATTERN.matcher(uri.getPath().replaceAll("^[^\\w]*", "")).replaceAll(".tiff");
        }
    }

    private static class AltoHrefStrategy implements FileHrefStrategy {
        @Override
        public String get(String href) throws URISyntaxException {
            URI uri = new URI(href);
            return uri.getPath().replaceAll("^[^\\w]*", "").replaceFirst("idx_alto", "alto");
        }
    }

}
