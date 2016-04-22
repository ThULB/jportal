package fsu.jportal.mets;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.mets.model.IMetsElement;
import org.mycore.mets.model.Mets;
import org.mycore.mets.model.files.FileGrp;
import org.mycore.mets.model.struct.Area;
import org.mycore.mets.model.struct.Fptr;
import org.mycore.mets.model.struct.LogicalDiv;
import org.mycore.mets.model.struct.Seq;

import fsu.jportal.mets.LLZMetsUtils.AltoHrefStrategy;
import fsu.jportal.mets.LLZMetsUtils.TiffHrefStrategy;

public class JVBMetsConverter extends ENMAPConverter {

    private static Logger LOGGER = LogManager.getLogger(JVBMetsConverter.class);

    private LogicalDiv lastSerialNovel;

    private List<ALTO> altoReferences;

    private Path metsRootPath;

    @Override
    public Mets convert(Document enmap) throws ConvertException {
        lastSerialNovel = null;
        ABBYYtoALTOConverter.convert(enmap);
        altoReferences = loadAltoReferences(enmap);
        Mets mets = super.convert(enmap);
        return mets;
    }

    /**
     * Sets the path to the folder where the mets file lies within.
     * 
     * @param metsRootPath
     */
    public void setPath(Path metsRootPath) {
        this.metsRootPath = metsRootPath;
    }

    protected List<ALTO> loadAltoReferences(Document enmap) {
        SAXBuilder saxBuilder = new SAXBuilder();
        XPathExpression<Element> fileExpression = XPathFactory.instance().compile(
            "mets:fileSec/mets:fileGrp[@ID='TextGroup']/mets:fileGrp/mets:file", Filters.element(), null,
            IMetsElement.METS, IMetsElement.XLINK);
        List<Element> altoFiles = fileExpression.evaluate(enmap.getRootElement());
        return altoFiles.stream().map(altoFile -> {
            try {
                String path = altoFile.getChild("FLocat", IMetsElement.METS).getAttributeValue("href",
                    IMetsElement.XLINK);
                path = new AltoHrefStrategy().get(path);
                Path resolve = metsRootPath.resolve(path);
                Document altoDocument = saxBuilder.build(resolve.toFile());
                return new ALTO(altoFile.getAttributeValue("ID"), altoDocument);
            } catch (Exception exc) {
                throw new RuntimeException(
                    "Unable to build alto file reference for " + altoFile.getAttributeValue("ID"), exc);
            }
        }).collect(Collectors.toList());
    }

    protected void handleFileSection(Element enmap, Mets mcr) throws URISyntaxException {
        FileGrp masterGroup = handleFileGroup(enmap, "MASTER",
            "mets:fileSec//mets:fileGrp[@ID='OCRMasterFiles']/mets:file", "image/tiff", new TiffHrefStrategy());
        FileGrp altoGroup = handleFileGroup(enmap, "ALTO", "mets:fileSec//mets:fileGrp[@ID='ABBYYFiles']/mets:file",
            "text/xml", new AltoHrefStrategy());
        mcr.getFileSec().addFileGrp(masterGroup);
        mcr.getFileSec().addFileGrp(altoGroup);
    }

    @Override
    protected LogicalDiv getLogicalSubDiv(Element enmap, Element enmapDiv) {
        LogicalDiv logicalSubDiv = this.buildLogicalSubDiv(enmapDiv);
        String type = enmapDiv.getAttributeValue("TYPE").toLowerCase();
        if (type.equals("issue")) {
            if (enmapDiv.getChildren().isEmpty()) {
                LOGGER.warn("Issue has no content! " + logicalSubDiv.getId());
                return null;
            }
            handleLogicalDivs(enmap, enmapDiv, logicalSubDiv);
            return logicalSubDiv;
        }
        if (type.equals("article") || type.equals("serialnovel")) {
            logicalSubDiv.setType("article");
            if (type.equals("serialnovel")) {
                lastSerialNovel = logicalSubDiv;
            }
        } else if (type.equals("serialnovelcontinue")) {
            if (lastSerialNovel == null) {
                throw new RuntimeException(
                    "There is no serial novel defined before SerialNovelContinue appears " + logicalSubDiv.getId());
            }
        } else {
            return null;
        }
        handleLogicalFilePointer(enmapDiv, logicalSubDiv);
        return logicalSubDiv;
    }

    protected void handleLogicalFilePointer(Element enmapDiv, LogicalDiv mcrDiv) {
        XPathExpression<Element> areaExp = XPathFactory.instance().compile(
            "mets:div/mets:fptr/mets:area[contains(@FILEID, 'ALTO')]", Filters.element(), null, IMetsElement.METS);
        List<Element> areas = areaExp.evaluate(enmapDiv);
        if (areas.isEmpty()) {
            return;
        }

        Map<String, Set<Block>> altoBlockMap = new CoordinatesToIdMapper().map(altoReferences, areas);

        // try to combine blocks
        Fptr fptr = new Fptr();
        Seq seq = new Seq();
        fptr.getSeqList().add(seq);

        Block lastBlock = null;
        Area area = null;
        for (Map.Entry<String, Set<Block>> entry : altoBlockMap.entrySet()) {
            String altoID = entry.getKey();
            ArrayList<Block> blockList = new ArrayList<>(entry.getValue());
            blockList.sort(new Comparator<Block>() {
                public int compare(Block block1, Block block2) {
                    String num1 = block1.id.replaceAll(altoBlockNumberRegex(), "");
                    String num2 = block2.id.replaceAll(altoBlockNumberRegex(), "");
                    return Integer.compare(Integer.valueOf(num1), Integer.valueOf(num2));
                }
            });
            for (Block block : blockList) {
                if (area != null && !isConsecutive(lastBlock, block)) {
                    area.setEnd(lastBlock.id);
                    seq.getAreaList().add(area);
                    area = null;
                }
                if (area == null) {
                    area = new Area();
                    area.setBegin(block.id);
                    area.setBetype("IDREF");
                    area.setFileId(altoID);
                }
                lastBlock = block;
            }
            if (area != null && area.getEnd() == null) {
                area.setEnd(lastBlock.id);
                seq.getAreaList().add(area);
                mcrDiv.getFptrList().add(fptr);
            }
        }
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

        public Map<String, Set<Block>> map(List<ALTO> altoReferences, List<Element> metsAreaElements) {
            // build references
            List<AreaAltoBlockRef> refs = buildRef(altoReferences, metsAreaElements);
            // do the area to block mapping
            mapFittingBlocks(refs);
            // return the finished block references
            Map<String, Set<Block>> altoBlockMap = new HashMap<>();
            for (AreaAltoBlockRef ref : refs) {
                if (ref.blocks.isEmpty()) {
                    throw new RuntimeException("Unable to find block references for area "
                        + ref.areaRectangle.toMetsCoordinates() + " in " + ref.alto.getId());
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
            public Rectangle areaRectangle;

            public ALTO alto;

            public List<Block> blocks = new ArrayList<>();
        }
    }

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
