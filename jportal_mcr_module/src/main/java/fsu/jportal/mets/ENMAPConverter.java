package fsu.jportal.mets;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.content.MCRPathContent;
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
import org.xml.sax.SAXException;

import fsu.jportal.mets.LLZMetsUtils.AltoHrefStrategy;
import fsu.jportal.mets.LLZMetsUtils.FileHrefStrategy;
import fsu.jportal.mets.LLZMetsUtils.TiffHrefStrategy;

/**
 * Converts an ENMAP Profile mets.xml to a MYCORE Profile mets.xml.
 * 
 * @author Matthias Eichner
 */
public abstract class ENMAPConverter {

    private static Logger LOGGER = LogManager.getLogger(ENMAPConverter.class);

    /**
     * If failOnEmptyAreas is true and if some coordinates of some <mets:area /> couldn't
     * be assigned to an alto block, a {@link BlockReferenceException} is thrown. To build the
     * mets document anyway, set this to false.
     */
    private boolean failOnEmptyAreas = true;

    private boolean failEasyOnStructLinkGeneration = true;

    private List<String> emptyAreas;

    public void setFailOnEmptyAreas(boolean failOnEmptyAreas) {
        this.failOnEmptyAreas = failOnEmptyAreas;
    }

    public List<String> getEmptyAreas() {
        return emptyAreas;
    }

    public void setFailEasyOnStructLinkGeneration(boolean failEasyOnStructLinkGeneration) {
        this.failEasyOnStructLinkGeneration = failEasyOnStructLinkGeneration;
    }

    /**
     * Do the conversation.
     * 
     * @param enmap jdom document to convert
     * @return new mycore mets.xml
     * 
     * @throws ConvertException
     */
    public Mets convert(Document enmap, Path basePath) throws ConvertException {
        this.emptyAreas = new ArrayList<>();
        try {
            // convert abbyy stuff to alto
            ABBYYtoALTOConverter.convert(enmap);

            // handle fileSec and physical structure
            Mets mets = new Mets();
            Element enmapRootElement = enmap.getRootElement();
            handleFileSection(enmapRootElement, mets);
            handlePhysicalStructure(enmapRootElement, mets);

            // load alto references
            List<ALTO> altoReferences = loadAltoReferences(mets, basePath);

            // handle logical structure and struct link
            handleLogicalStructure(enmapRootElement, mets, altoReferences);

            handleStructLink(enmapRootElement, mets, failEasyOnStructLinkGeneration);
            return mets;
        } catch (ConvertException exc) {
            throw exc;
        } catch (Exception exc) {
            throw new ConvertException(exc.getMessage(), exc);
        }
    }

    /**
     * Returns a list of all {@link ALTO} references. This method iterates over the
     * file section and loads each ALTO file. 
     * 
     * @param mets the mycore mets document
     * @param basePath the path where the ALTO files lies within
     * @return list of ALTO objects
     */
    protected List<ALTO> loadAltoReferences(Mets mets, Path basePath) {
        FileGrp altoGroup = mets.getFileSec().getFileGroup("ALTO");
        return altoGroup.getFileList().stream().map(altoFile -> {
            try {
                return loadAlto(basePath, altoFile);
            } catch (Exception exc) {
                throw new ConvertException("Unable to build alto file reference for " + altoFile.getId(), exc);
            }
        }).collect(Collectors.toList());
    }

    /**
     * Loads a single ALTO reference based on the given mets:file.
     * 
     * @param basePath 
     * @param altoFile the mets:file element in the mets:fileSec
     * 
     * @return a single ALTO reference
     * @throws URISyntaxException
     * @throws JDOMException 
     * @throws IOException
     * @throws SAXException
     */
    protected ALTO loadAlto(Path basePath, File altoFile)
        throws URISyntaxException, JDOMException, IOException, SAXException {
        String path = altoFile.getFLocat().getHref();
        path = new AltoHrefStrategy().get(path);
        Path altoPath = basePath.resolve(path);
        Document altoDocument = new MCRPathContent(altoPath).asXML();
        return new ALTO(altoFile.getId(), altoDocument);
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
        for (Element divElement : divs) {
            PhysicalSubDiv div = new PhysicalSubDiv(divElement.getAttributeValue("ID"), "page");
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

    protected void handleLogicalStructure(Element enmap, Mets mcrMets, List<ALTO> altoReferences) {
        LogicalStructMap structMap = (LogicalStructMap) mcrMets.getStructMap(LogicalStructMap.TYPE);
        XPathExpression<Element> xpathExp = XPathFactory.instance().compile(
            "mets:structMap[@TYPE='logical_structmap']/mets:div", Filters.element(), null, IMetsElement.METS);
        Element enmapRootDiv = xpathExp.evaluateFirst(enmap);
        LogicalDiv mcrDiv = getLogicalDiv(enmap, enmapRootDiv, mcrMets, altoReferences);
        structMap.setDivContainer(mcrDiv);

        if (!emptyAreas.isEmpty()) {
            if (failOnEmptyAreas) {
                BlockReferenceException blockReferenceException = new BlockReferenceException(
                    "Error while referencing logical div coordinates to alto blocks.");
                emptyAreas.forEach(blockReferenceException::addDiv);
                throw blockReferenceException;
            } else {
                LOGGER.warn(MetsImportUtils.buildBlockReferenceError("There are unresolved <mets:area>", emptyAreas,
                    enmap.getDocument()));
            }
        }
    }

    protected void handleStructLink(Element enmap, Mets mets, boolean failEasy) throws IOException, Exception {
        try {
            StructLinkGenerator structLinkGenerator = new StructLinkGenerator();
            structLinkGenerator.setFailEasy(failEasy);
            StructLink structLink = structLinkGenerator.generate(mets);
            mets.setStructLink(structLink);
        } catch (Exception exc) {
            Path tempFile = Files.createTempFile("mets", ".xml");
            LOGGER.warn("Unable to create struct link section. For debugging purposes store mets.xml here: "
                + tempFile.toAbsolutePath().toString());
            Files.write(tempFile, new MCRJDOMContent(mets.asDocument()).asByteArray(), StandardOpenOption.CREATE);
            throw exc;
        }
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
    protected void handleLogicalDivs(Element enmap, Element enmapDiv, LogicalDiv mcrDiv, Mets mcrMets,
        List<ALTO> altoReferences) {
        List<Element> children = enmapDiv.getChildren("div", IMetsElement.METS);
        for (Element enmapSubDiv : children) {
            LogicalDiv mcrSubdDiv = getLogicalSubDiv(enmap, enmapSubDiv, mcrMets, altoReferences);
            if (mcrSubdDiv != null) {
                mcrDiv.add(mcrSubdDiv);
            }
        }
    }

    /**
     * Gets the logical subdiv by enmap div.
     * 
     * @param enmap
     * @param enmapDiv
     * @return
     */
    protected LogicalDiv getLogicalSubDiv(Element enmap, Element enmapDiv, Mets mcrMets, List<ALTO> altoReferences) {
        LogicalDiv mcrDiv = buildLogicalSubDiv(enmapDiv);
        // handle children
        handleLogicalDivs(enmap, enmapDiv, mcrDiv, mcrMets, altoReferences);
        // handle fptr
        handleLogicalFilePointer(enmapDiv, mcrDiv, mcrMets, altoReferences);
        return mcrDiv;
    }

    protected LogicalDiv buildLogicalSubDiv(Element enmapDiv) {
        String id = enmapDiv.getAttributeValue("ID");
        String type = enmapDiv.getAttributeValue("TYPE").toLowerCase();
        String label = enmapDiv.getAttributeValue("LABEL");
        return new LogicalDiv(id, type, (label == null || label.equals("")) ? type : label);
    }

    /**
     * Returns the root logical div object. Reads the enmapDiv id, type and label
     * attributes and add it to the created class.
     * 
     * @param enmap
     * @param enmapDiv
     * @return new logical div
     */
    protected LogicalDiv getLogicalDiv(Element enmap, Element enmapDiv, Mets mcrMets, List<ALTO> altoReferences) {
        String id = enmapDiv.getAttributeValue("ID");
        String type = enmapDiv.getAttributeValue("TYPE").toLowerCase();
        String label = enmapDiv.getAttributeValue("LABEL");
        LogicalDiv mcrDiv = new LogicalDiv(id, type, (label == null || label.equals("")) ? type : label);
        // handle children
        handleLogicalDivs(enmap, enmapDiv, mcrDiv, mcrMets, altoReferences);
        return mcrDiv;
    }

    protected void handleLogicalFilePointer(Element enmapDiv, LogicalDiv mcrDiv, Mets mcrMets,
        List<ALTO> altoReferences) {
        XPathExpression<Element> areaExp = XPathFactory.instance().compile(
            "mets:div/mets:fptr/mets:area[contains(@FILEID, 'ALTO')]", Filters.element(), null, IMetsElement.METS);
        List<Element> areas = areaExp.evaluate(enmapDiv);
        if (areas.isEmpty()) {
            return;
        }
        Map<String, Set<Block>> altoBlockMap = new CoordinatesToIdMapper().map(altoReferences, areas, emptyAreas);
        if (failOnEmptyAreas && !emptyAreas.isEmpty()) {
            // there are empty block -> no need to continue;
            return;
        }
        // try to combine blocks
        Fptr fptr = new Fptr();
        fptr.getSeqList().add(buildSequence(mcrMets, altoBlockMap));
        if (!fptr.getSeqList().isEmpty()) {
            mcrDiv.getFptrList().add(fptr);
        }
    }

    /**
     * Builds the Seq for the given altoBlockMap.
     * 
     * @param mcrMets
     * @param altoBlockMap
     * @return
     */
    private Seq buildSequence(Mets mcrMets, Map<String, Set<Block>> altoBlockMap) {
        Seq seq = new Seq();
        PhysicalStructMap physicalStructMap = (PhysicalStructMap) mcrMets.getStructMap(PhysicalStructMap.TYPE);
        PhysicalDiv divContainer = physicalStructMap.getDivContainer();

        AtomicReference<Block> lastBlockRef = new AtomicReference<>();
        AtomicReference<Area> areaRef = new AtomicReference<>();

        altoBlockMap.entrySet().stream().sorted((entry1, entry2) -> {
            String fileId1 = entry1.getKey();
            String fileId2 = entry2.getKey();
            PhysicalSubDiv div1 = divContainer.byFileId(fileId1);
            PhysicalSubDiv div2 = divContainer.byFileId(fileId2);
            return div1.getPositionInParent().orElse(0) - div2.getPositionInParent().orElse(0);
        }).forEachOrdered(entry -> {
            String altoID = entry.getKey();
            ArrayList<Block> blockList = new ArrayList<>(entry.getValue());
            blockList.sort(new Comparator<Block>() {
                public int compare(Block block1, Block block2) {
                    String num1 = block1.id.replaceAll(altoBlockNumberRegex(), "");
                    String num2 = block2.id.replaceAll(altoBlockNumberRegex(), "");
                    return Integer.compare(Integer.valueOf(num1), Integer.valueOf(num2));
                }
            });
            Area area = areaRef.get();
            Block lastBlock = lastBlockRef.get();
            for (Block block : blockList) {
                if (area != null && !isConsecutive(lastBlock, block)) {
                    area.setEnd(lastBlock.id);
                    seq.getAreaList().add(area);
                    areaRef.set(area = null);
                }
                if (area == null) {
                    areaRef.set(area = new Area());
                    area.setBegin(block.id);
                    area.setBetype("IDREF");
                    area.setFileId(altoID);
                }
                lastBlockRef.set(lastBlock = block);
            }
            if (area != null && area.getEnd() == null) {
                area.setEnd(lastBlock.id);
                seq.getAreaList().add(area);
                areaRef.set(null);
                lastBlockRef.set(null);
            }
        });
        return seq;
    }

    /**
     * Checks if the next block is the following sibling of the
     * base block.
     * 
     * @param base the base block
     * @param next the following sibling?
     * @return true if its consecutive
     */
    protected boolean isConsecutive(Block base, Block next) {
        Pattern pattern = Pattern.compile("(.*?)_(\\d*)");
        Matcher baseMatcher = pattern.matcher(base.id);
        Matcher nextMatcher = pattern.matcher(next.id);
        if (baseMatcher.find() && nextMatcher.find()) {
            String basePrefix = baseMatcher.group(1);
            String baseNumber = baseMatcher.group(2);
            String nextPrefix = nextMatcher.group(1);
            String nextNumber = nextMatcher.group(2);
            return basePrefix.equals(nextPrefix) && Integer.valueOf(nextNumber) - Integer.valueOf(baseNumber) == 1;
        }
        return false;
    }

    protected String altoBlockNumberRegex() {
        return "\\w*_";
    }

    /**
     * In mets you can reference alto by id or by coordinates. This class tries to map
     * a mets using coordinates (x1,y1,x2,y2) to block references. 
     */
    static class CoordinatesToIdMapper {

        /**
         * 
         * @param altoReferences
         * @param metsAreaElements
         * @param emptyAreas
         * 
         * @return map where key = ALTO FILEID; value = set of alto blocks
         */
        public Map<String, Set<Block>> map(List<ALTO> altoReferences, List<Element> metsAreaElements,
            List<String> emptyAreas) {
            // build references
            List<AreaAltoBlockRef> refs = buildRef(altoReferences, metsAreaElements);
            // do the area to block mapping
            mapFittingBlocks(refs);
            // return the finished block references
            Map<String, Set<Block>> altoBlockMap = new HashMap<>();
            for (AreaAltoBlockRef ref : refs) {
                if (ref.blocks.isEmpty()) {
                    emptyAreas.add(ref.logicalDivId);
                }
                Set<Block> blockSet = altoBlockMap.get(ref.alto.getId());
                if (blockSet == null) {
                    blockSet = new HashSet<>();
                    altoBlockMap.put(ref.alto.getId(), blockSet);
                }
                for (Block block : ref.blocks) {
                    blockSet.add(block);
                }
            }
            return altoBlockMap;
        }

        protected List<AreaAltoBlockRef> buildRef(List<ALTO> altoReferences, List<Element> metsAreaElements) {
            List<AreaAltoBlockRef> refs = new ArrayList<>();
            for (Element areaElement : metsAreaElements) {
                String fileID = areaElement.getAttributeValue("FILEID");
                ALTO alto = altoReferences.stream().filter(ref -> ref.getId().equals(fileID)).findFirst().orElse(null);
                if (alto != null) {
                    AreaAltoBlockRef ref = new AreaAltoBlockRef();
                    ref.logicalDivId = areaElement.getParentElement().getParentElement().getAttributeValue("ID");
                    ref.areaRectangle = getRectangleForArea(areaElement);
                    ref.alto = alto;
                    refs.add(ref);
                } else {
                    LOGGER.error("Unable to find ALTO file for id: " + fileID);
                }
            }
            return refs;
        }

        /**
         * Maps the alto blocks to their corresponding mets area.
         * 
         * @param refs the area-alto-block reference which will be updated
         */
        protected void mapFittingBlocks(List<AreaAltoBlockRef> refs) {
            refs.stream().filter(ref -> ref.blocks.isEmpty()).forEach(ref -> {
                // by default a bunch of blocks lies within the area rectangle
                ref.blocks = ref.alto.liesWithin(ref.areaRectangle);
                if (ref.blocks.isEmpty()) {
                    // it looks like there are no blocks within the area.
                    // so we find the blocks which cuts the area
                    ref.blocks = ref.alto.touch(ref.areaRectangle);
                }
            });
        }

        protected Rectangle getRectangleForArea(Element area) {
            String COORDS = area.getAttributeValue("COORDS");
            String[] areaCoordinates = COORDS.split(" ");
            int areaX1 = Integer.valueOf(areaCoordinates[0]);
            int areaY1 = Integer.valueOf(areaCoordinates[1]);
            int areaX2 = Integer.valueOf(areaCoordinates[2]);
            int areaY2 = Integer.valueOf(areaCoordinates[3]);
            return new Rectangle().setBounds(areaX1, areaY1, areaX2, areaY2);
        }

        static class AreaAltoBlockRef {
            public String logicalDivId;

            public Rectangle areaRectangle;

            public ALTO alto;

            public List<Block> blocks = new ArrayList<>();
        }
    }

    /**
     * Helper class for an ALTO file. Consists of an id and a list of {@link Block}.
     */
    static class ALTO {

        private String id;

        private List<Block> blocks;

        public ALTO(String id, Document alto) {
            this.id = id;
            this.blocks = buildBlocks(alto);
        }

        private List<Block> buildBlocks(Document alto) {
            //"alto:Layout/alto:Page//*[name()='TextBlock' or name()='Illustration']",
            XPathExpression<Element> blockExpression = XPathFactory.instance().compile(
                "alto:Layout/alto:Page//*[name()='TextBlock' or name()='Illustration']", Filters.element(), null,
                Namespace.getNamespace("alto", "http://www.loc.gov/standards/alto/ns-v2#"));
            List<Element> blockElements = blockExpression.evaluate(alto.getRootElement());
            return blockElements.stream().map(be -> {
                Block block = new Block();
                block.id = be.getAttributeValue("ID");
                block.rect = new Rectangle();
                block.rect.x = Integer.valueOf(be.getAttributeValue("HPOS"));
                block.rect.y = Integer.valueOf(be.getAttributeValue("VPOS"));
                block.rect.width = Integer.valueOf(be.getAttributeValue("WIDTH"));
                block.rect.height = Integer.valueOf(be.getAttributeValue("HEIGHT"));
                return block;
            }).collect(Collectors.toList());
        }

        public String getId() {
            return id;
        }

        /**
         * Returns a list of blocks which lie's within the bounds of the rectangle.
         * A block has to be fully covered by the rectangle.
         * 
         * @param rectangle
         * @return list of blocks
         */
        public List<Block> liesWithin(Rectangle rectangle) {
            return blocks.stream().filter(block -> {
                return rectangle.contains(block.rect);
            }).collect(Collectors.toList());
        }

        /**
         * Returns a list of blocks which touches the bounds of the rectangle.
        
         * @param rectangle
         * @return list of blocks
         */
        public List<Block> touch(Rectangle rectangle) {
            return blocks.stream().filter(block -> {
                return rectangle.touch(block.rect);
            }).collect(Collectors.toList());
        }

    }

    /**
     * An ALTO block element. 
     */
    static class Block {

        String id;

        Rectangle rect;

        @Override
        public String toString() {
            return id + ": " + rect;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((id == null) ? 0 : id.hashCode());
            result = prime * result + ((rect == null) ? 0 : rect.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Block other = (Block) obj;
            if (id == null) {
                if (other.id != null)
                    return false;
            } else if (!id.equals(other.id))
                return false;
            if (rect == null) {
                if (other.rect != null)
                    return false;
            } else if (!rect.equals(other.rect))
                return false;
            return true;
        }

    }

    /**
     * A rectangle.
     */
    static class Rectangle {

        private int x, y, width, height;

        /**
         * Creates an rectangle with x=0,y=0,width=0,height=0
         */
        public Rectangle() {
            this(0, 0, 0, 0);
        }

        public Rectangle(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public Rectangle setBounds(int x1, int y1, int x2, int y2) {
            this.x = x1;
            this.y = y1;
            this.width = x2 - x1;
            this.height = y2 - y1;
            return this;
        }

        /**
         * Checks if the other rectangle lies within the bounds.
         * 
         * @param other the other rectangle to compare
         * @return true if its within the bounds
         */
        public boolean contains(Rectangle other) {
            return (other.x >= x) && (other.y >= y) && (other.x + other.width <= x + width)
                && (other.y + other.height <= y + height);
        }

        /**
         * Checks if this rectangle contains the point at x,y.
         * 
         * @param pointX x position of the point
         * @param pointY y position of the point
         * @return true if the point lies within this rectangle
         */
        public boolean contains(int pointX, int pointY) {
            return this.x <= pointX && (this.x + this.width >= pointX) && this.y <= pointY
                && (this.y + this.height >= pointY);
        }

        /**
         * Checks if the other rectangle touches this one.
         * 
         * @param other the other rectangle
         * @return true of the other touches this one
         */
        public boolean touch(Rectangle other) {
            return this.contains(other.x, other.y) || this.contains(other.x, other.y + other.height)
                || this.contains(other.x + other.width, other.y)
                || this.contains(other.x + other.width, other.y + other.height);
        }

        /**
         * Returns the area of this rectangle.
         * 
         * @return
         */
        public int area() {
            return width * height;
        }

        @Override
        public String toString() {
            return "x=" + x + " y=" + y + " width=" + width + " height=" + height;
        }

        public String toMetsCoordinates() {
            return "HPOS=" + x + " VPOS=" + y + " WIDTH=" + width + " HEIGHT=" + height;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + height;
            result = prime * result + width;
            result = prime * result + x;
            result = prime * result + y;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Rectangle other = (Rectangle) obj;
            if (height != other.height)
                return false;
            if (width != other.width)
                return false;
            if (x != other.x)
                return false;
            if (y != other.y)
                return false;
            return true;
        }

    }

}
