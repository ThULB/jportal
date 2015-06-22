package fsu.jportal.mets;

        import fsu.jportal.mets.LLZMetsUtils.AltoHrefStrategy;
        import fsu.jportal.mets.LLZMetsUtils.FileHrefStrategy;
        import fsu.jportal.mets.LLZMetsUtils.TiffHrefStrategy;
        import org.jdom2.Attribute;
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
        import org.mycore.mets.model.struct.*;

        import java.net.URISyntaxException;
        import java.util.*;

/**
 * Converts the llz output format from uibk to the mycore mets format.
 *
 * @author Matthias Eichner
 */
public class LLZMetsConverter {

    private String lastBibLabel;

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
                "mets:fileSec//mets:fileGrp[@ID='ALTOFiles']/mets:file", "text/xml", new AltoHrefStrategy());
        mcr.getFileSec().addFileGrp(masterGroup);
        mcr.getFileSec().addFileGrp(altoGroup);
    }

    private FileGrp handleFileGroup(Element llzElement, String fileGroup, String xpath, String mimeType,
            FileHrefStrategy hrefStrategy) throws URISyntaxException {
        FileGrp group = new FileGrp(fileGroup);
        XPathExpression<Element> xpathExp = XPathFactory.instance()
                .compile(xpath, Filters.element(), null, IMetsElement.METS);
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

    private void handlePhysicalStructure(Element llz, Mets mets) {
        PhysicalStructMap structMap = (PhysicalStructMap) mets.getStructMap(PhysicalStructMap.TYPE);
        PhysicalDiv physicalDiv = new PhysicalDiv();
        structMap.setDivContainer(physicalDiv);

        setID(physicalDiv, llz);

        XPathExpression<Element> xpathExp = XPathFactory.instance()
                .compile("mets:structMap[@TYPE='physical_structmap']//mets:div[mets:fptr]", Filters.element(), null,
                        IMetsElement.METS);
        List<Element> divs = new ArrayList<Element>(xpathExp.evaluate(llz));
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
            for (Element ftpr : fptrs) {
                String fileID = ftpr.getAttributeValue("FILEID");
                if (fileID.endsWith("OCRMASTER") || fileID.endsWith("ALTO")) {
                    div.add(new Fptr(fileID));
                }
            }
            physicalDiv.add(div);
        }
    }

    private void setID(PhysicalDiv physicalDiv, Element llzElement) {
        XPathExpression<Attribute> physMapExp = XPathFactory.instance()
                .compile("mets:structMap[@TYPE='PHYSICAL']/mets:div[@TYPE='physSequence']/@id", Filters.attribute(),
                        null, IMetsElement.METS);

        Attribute idAttr = physMapExp.evaluateFirst(llzElement);
        String id = null;
        if (idAttr != null) {
            id = idAttr.getValue();
        }

        if (id == null || id.equals("")) {
            id = "physSequence_1";

        }

        physicalDiv.setId(id);
    }

    private void handleLogicalStructure(Element llz, Mets mets) {
        LogicalStructMap structMap = (LogicalStructMap) mets.getStructMap(LogicalStructMap.TYPE);
        XPathExpression<Element> xpathExp = XPathFactory.instance()
                .compile("mets:structMap[@TYPE='logical_structmap']/mets:div", Filters.element(), null,
                        IMetsElement.METS);
        Element volumeDiv = xpathExp.evaluateFirst(llz);
        LogicalDiv volume = new LogicalDiv(volumeDiv.getAttributeValue("ID"), "volume",
                volumeDiv.getAttributeValue("LABEL"));
        String order = volumeDiv.getAttributeValue("ORDER");
        if (order != null) {
            volume.setOrder(Integer.valueOf(order));
        }
        structMap.setDivContainer(volume);
        handleLogicalDivs(volumeDiv, volume , llz);
    }

    private void handleLogicalDivs(Element divElement, AbstractLogicalDiv div, Element llz) {
        List<Element> children = divElement.getChildren("div", IMetsElement.METS);
        for (Element subDivElement : children) {
            // create sub div
            String type = subDivElement.getAttributeValue("TYPE").toLowerCase();
            String dmdID = LLZMetsUtils.getDmDId(subDivElement);
            LogicalSubDiv subdDiv = getLogicalDiv(subDivElement, dmdID, llz);
            if (type.equals("issue") || type.equals("volumeparts")) {
                handleLogicalDivs(subDivElement, subdDiv, llz);
                div.add(subdDiv);
                continue;
            } else if (type.equals("rezension") && subDivElement.getChildren().size() > 0) {
                handleRecension(subDivElement, subdDiv, dmdID, llz);
            } else if (type.equals("tp")) {
                subdDiv.setLabel("Titelblatt");
                subdDiv.setType("title_page");
            } else if (type.equals("preface")) {
                subdDiv.setLabel("Vorwort");
                subdDiv.setType("preface");
            } else if (type.equals("toc")) {
                subdDiv.setLabel("Register");
                subdDiv.setType("index");
            } else {
                continue;
            }
            handleLogicalFilePointer(subDivElement, subdDiv);
            div.add(subdDiv);
        }
    }

    private void handleRecension(Element recensionDiv, LogicalSubDiv recension, String dmdID, Element llz) {
        // type
        recension.setType("article");
        // label
        String bibLabel;
        if (dmdID != null) {
            bibLabel = LLZMetsUtils.getFullLabel(dmdID, llz);
        }
        else {
            bibLabel = LLZMetsUtils.getBibLabel(recensionDiv);
        }
        if (bibLabel != null) {
            recension.setLabel(bibLabel);
            this.lastBibLabel = bibLabel;
        }
        if (recension.getLabel() == null) {
            if (lastBibLabel == null) {
                recension.setLabel("unknown " + recension.getType());
            } else {
                recension.setLabel(lastBibLabel);
            }
        }
    }

    private void handleLogicalFilePointer(Element divElement, AbstractLogicalDiv div) {
        XPathExpression<Element> areaExp = XPathFactory.instance()
                .compile("mets:div/mets:fptr/mets:area[contains(@FILEID, 'ALTO')]", Filters.element(), null,
                        IMetsElement.METS);
        List<Element> areas = areaExp.evaluate(divElement);
        String lastFileID = null, lastContentID = null;
        Fptr fptr = new Fptr();
        Seq seq = new Seq();
        fptr.getSeqList().add(seq);
        Area area = null;
        for (Element areaElement : areas) {
            String fileID = areaElement.getAttributeValue("FILEID");
            String contentID = areaElement.getParentElement().getParentElement().getAttributeValue("ID").replace("LS", "Group_");
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

    private LogicalSubDiv getLogicalDiv(Element divElement, String dmdID, Element llz) {
        String id = divElement.getAttributeValue("ID");
        String type = divElement.getAttributeValue("TYPE").toLowerCase();
        String label = divElement.getAttributeValue("LABEL");
        if (dmdID != null){
            label = LLZMetsUtils.getFullLabel(dmdID, llz);
        }
        String order = divElement.getAttributeValue("ORDER");
        return new LogicalSubDiv(id, type, label, Integer.valueOf(order));
    }
}
