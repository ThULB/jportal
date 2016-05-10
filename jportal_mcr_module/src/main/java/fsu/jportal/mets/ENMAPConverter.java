package fsu.jportal.mets;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
import org.mycore.mets.model.struct.Area;
import org.mycore.mets.model.struct.Fptr;
import org.mycore.mets.model.struct.LOCTYPE;
import org.mycore.mets.model.struct.LogicalDiv;
import org.mycore.mets.model.struct.LogicalStructMap;
import org.mycore.mets.model.struct.PhysicalDiv;
import org.mycore.mets.model.struct.PhysicalStructMap;
import org.mycore.mets.model.struct.PhysicalSubDiv;
import org.mycore.mets.model.struct.Seq;
import org.mycore.mets.model.struct.StructLink;

import fsu.jportal.mets.LLZMetsUtils.AltoHrefStrategy;
import fsu.jportal.mets.LLZMetsUtils.FileHrefStrategy;
import fsu.jportal.mets.LLZMetsUtils.TiffHrefStrategy;

/**
 * Converts an ENMAP Profile mets.xml to a MYCORE Profile mets.xml.
 * 
 * @author Matthias Eichner
 */
public class ENMAPConverter {

    /**
     * Do the conversation.
     * 
     * @param enmap jdom document to convert
     * @return new mycore mets.xml
     * 
     * @throws ConvertException
     */
    public Mets convert(Document enmap) throws ConvertException {
        try {
            Mets mets = new Mets();
            Element enmapRootElement = enmap.getRootElement();
            handleFileSection(enmapRootElement, mets);
            handlePhysicalStructure(enmapRootElement, mets);
            handleLogicalStructure(enmapRootElement, mets);
            StructLink structLink = new StructLinkGenerator().generate(mets);
            mets.setStructLink(structLink);
            return mets;
        } catch (Exception exc) {
            throw new ConvertException("Unable to convert mets document", exc);
        }
    }

    protected void handleFileSection(Element enmap, Mets mcr) throws URISyntaxException {
        FileGrp masterGroup = handleFileGroup(enmap, "MASTER",
            "mets:fileSec//mets:fileGrp[@ID='OCRMasterFiles']/mets:file", "image/tiff", new TiffHrefStrategy());
        FileGrp altoGroup = handleFileGroup(enmap, "ALTO", "mets:fileSec//mets:fileGrp[@ID='ALTOFiles']/mets:file",
            "text/xml", new AltoHrefStrategy());
        mcr.getFileSec().addFileGrp(masterGroup);
        mcr.getFileSec().addFileGrp(altoGroup);
    }

    /**
     * Creates a new {@link FileGrp}.
     * 
     * @param enmap enmap mets
     * @param fileGroupName name of the new file group
     * @param xpath where to extract file group data from enmap
     * @param mimeType mimetype of files
     * @param hrefStrategy href rename strategy
     * @return a new file group
     * @throws URISyntaxException
     */
    protected FileGrp handleFileGroup(Element enmap, String fileGroupName, String xpath, String mimeType,
        FileHrefStrategy hrefStrategy) throws URISyntaxException {
        FileGrp group = new FileGrp(fileGroupName);
        XPathExpression<Element> xpathExp = XPathFactory.instance().compile(xpath, Filters.element(), null,
            IMetsElement.METS);
        List<Element> files = xpathExp.evaluate(enmap);
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

    protected void handlePhysicalStructure(Element enmap, Mets mcr) {
        PhysicalStructMap structMap = (PhysicalStructMap) mcr.getStructMap(PhysicalStructMap.TYPE);
        PhysicalDiv physicalDiv = new PhysicalDiv();
        structMap.setDivContainer(physicalDiv);

        setID(enmap, physicalDiv);

        XPathExpression<Element> xpathExp = XPathFactory.instance().compile(
            "mets:structMap[@TYPE='physical_structmap' or @TYPE='PHYSICAL']//mets:div[mets:fptr]", Filters.element(),
            null, IMetsElement.METS);
        List<Element> divs = new ArrayList<Element>(xpathExp.evaluate(enmap));
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

    protected void setID(Element enmap, PhysicalDiv mcrPhysicalDiv) {
        XPathExpression<Attribute> physMapExp = XPathFactory.instance().compile(
            "mets:structMap[@TYPE='physical_structmap' or @TYPE='PHYSICAL']/mets:div[@TYPE='physSequence']/@id",
            Filters.attribute(), null, IMetsElement.METS);
        Attribute idAttr = physMapExp.evaluateFirst(enmap);
        String id = null;
        if (idAttr != null) {
            id = idAttr.getValue();
        }
        if (id == null || id.equals("")) {
            id = "physSequence_1";
        }
        mcrPhysicalDiv.setId(id);
    }

    protected void handleLogicalStructure(Element enmap, Mets mcrMets) {
        LogicalStructMap structMap = (LogicalStructMap) mcrMets.getStructMap(LogicalStructMap.TYPE);
        XPathExpression<Element> xpathExp = XPathFactory.instance()
            .compile("mets:structMap[@TYPE='logical_structmap']/mets:div", Filters.element(), null, IMetsElement.METS);
        Element enmapRootDiv = xpathExp.evaluateFirst(enmap);
        LogicalDiv mcrDiv = getLogicalDiv(enmap, enmapRootDiv, mcrMets);
        structMap.setDivContainer(mcrDiv);
    }

    /**
     * Handles the logical div's. To improve performance and get smaller mets files the
     * last level of div's is removed. Before removal the file pointers are collected to
     * area elements and added to the parent div (this is only done for alto files!).
     * The resulting logical_structmap is much more compact without sacrificing to much
     * information.
     * 
     * @param enmap
     * @param enmapDiv
     * @param mcrDiv
     */
    protected void handleLogicalDivs(Element enmap, Element enmapDiv, LogicalDiv mcrDiv, Mets mcrMets) {
        List<Element> children = enmapDiv.getChildren("div", IMetsElement.METS);
        for (Element enmapSubDiv : children) {
            LogicalDiv mcrSubdDiv = getLogicalSubDiv(enmap, enmapSubDiv, mcrMets);
            if (mcrSubdDiv != null) {
                mcrDiv.add(mcrSubdDiv);
            }
        }
    }

    protected void handleLogicalFilePointer(Element enmapDiv, LogicalDiv mcrDiv, Mets mcrMets) {
        XPathExpression<Element> areaExp = XPathFactory.instance().compile(
            "mets:div/mets:fptr/mets:area[contains(@FILEID, 'ALTO')]", Filters.element(), null, IMetsElement.METS);
        List<Element> areas = areaExp.evaluate(enmapDiv);
        if (areas.isEmpty()) {
            return;
        }
        String lastFileID = null, lastContentID = null;
        Fptr fptr = new Fptr();
        Seq seq = new Seq();
        fptr.getSeqList().add(seq);
        Area area = null;
        for (Element areaElement : areas) {
            String fileID = areaElement.getAttributeValue("FILEID");
            String contentID = areaElement.getParentElement().getParentElement().getAttributeValue("ID").replace("LS",
                "Group_");
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
            mcrDiv.getFptrList().add(fptr);
        }
    }

    /**
     * Gets the logical subdiv by enmap div.
     * 
     * @param enmap
     * @param enmapDiv
     * @return
     */
    protected LogicalDiv getLogicalSubDiv(Element enmap, Element enmapDiv, Mets mcrMets) {
        LogicalDiv mcrDiv = buildLogicalSubDiv(enmapDiv);
        // handle children
        handleLogicalDivs(enmap, enmapDiv, mcrDiv, mcrMets);
        // handle fptr
        handleLogicalFilePointer(enmapDiv, mcrDiv, mcrMets);
        return mcrDiv;
    }

    protected LogicalDiv buildLogicalSubDiv(Element enmapDiv) {
        String id = enmapDiv.getAttributeValue("ID");
        String type = enmapDiv.getAttributeValue("TYPE").toLowerCase();
        String label = enmapDiv.getAttributeValue("LABEL");
        String order = enmapDiv.getAttributeValue("ORDER");
        return new LogicalDiv(id, type, (label == null || label.equals("")) ? type : label, Integer.valueOf(order));
    }

    /**
     * Returns the root logical div object. Reads the enmapDiv id, type and label
     * attributes and add it to the created class.
     * 
     * @param enmap
     * @param enmapDiv
     * @return new logical div
     */
    protected LogicalDiv getLogicalDiv(Element enmap, Element enmapDiv, Mets mcrMets) {
        String id = enmapDiv.getAttributeValue("ID");
        String type = enmapDiv.getAttributeValue("TYPE").toLowerCase();
        String label = enmapDiv.getAttributeValue("LABEL");
        LogicalDiv mcrDiv = new LogicalDiv(id, type, (label == null || label.equals("")) ? type : label);
        // handle children
        handleLogicalDivs(enmap, enmapDiv, mcrDiv, mcrMets);
        return mcrDiv;
    }

}
