package fsu.jportal.mets;

import com.google.common.collect.Lists;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.common.MCRException;
import org.mycore.mets.model.IMetsElement;
import org.mycore.mets.model.Mets;
import org.mycore.mets.model.struct.*;

import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PerthesMetsConverter extends ENMAPConverter {

    private static final XPathExpression<Attribute> LAST_LOGICAL_DIV_ID;

    private static final XPathExpression<Attribute> FOLLOWING_FILE_ID;

    private static final XPathExpression<Attribute> PRECEDING_FILE_ID;

    private static final Pattern NUMBER_PATTERN;

    static {
        LAST_LOGICAL_DIV_ID = XPathFactory.instance().compile(
            "mets:structMap[@TYPE='logical_structmap']/mets:div/mets:div[last()]/@ID", Filters.attribute(), null,
            IMetsElement.METS);
        FOLLOWING_FILE_ID = XPathFactory.instance().compile("following::mets:area/@FILEID", Filters.attribute(), null,
            IMetsElement.METS);
        PRECEDING_FILE_ID = XPathFactory.instance().compile("preceding::mets:area[1]/@FILEID", Filters.attribute(),
            null, IMetsElement.METS);
        NUMBER_PATTERN = Pattern.compile("(\\d+)");
    }

    private String lastLogicalDivId = null;

    @Override
    public Mets convert(Document enmap, Path basePath) throws ConvertException {
        lastLogicalDivId = getLastLogicalDivId(enmap.getRootElement()).orElseThrow(() -> new MCRException(
            "Unable to find the last logical div identifier. This is required cause usally the last 'Geographischer Literatur Bericht'"));
        Mets mets = super.convert(enmap, basePath);
        fixMetsAreas(mets);
        return mets;
    }

    @Override
    protected LogicalDiv getLogicalSubDiv(Element enmap, Element enmapDiv, Mets mcrMets, List<ALTO> altoReferences) {
        LogicalDiv logicalSubDiv = super.getLogicalSubDiv(enmap, enmapDiv, mcrMets, altoReferences);
        boolean maps = logicalSubDiv.getLabel().startsWith("Karten, Abbildungen");
        boolean lastGeoLiteratur = logicalSubDiv.getLabel().startsWith("Geographischer ")
            && lastLogicalDivId.equals(logicalSubDiv.getId());

        if (maps || lastGeoLiteratur) {
            logicalSubDiv.getFptrList().clear();
            addIdRef(enmapDiv, logicalSubDiv, mcrMets);
        }

        return logicalSubDiv;
    }

    protected Optional<String> getLastLogicalDivId(Element enmap) {
        Attribute lastLogicalDivIdAttr = LAST_LOGICAL_DIV_ID.evaluateFirst(enmap);
        return lastLogicalDivIdAttr != null ? Optional.of(lastLogicalDivIdAttr.getValue()) : Optional.empty();
    }

    /**
     * Adds /mets:fptr/mets:seq/mets:area/@FILEID to a logical div which has no file id.
     * The ids are calculated by the mets:area/@FILEID of the preceding and following
     * logical divs.
     *
     * @param enmapDiv
     * @param logicalSubDiv
     * @param mcrMets
     */
    protected void addIdRef(Element enmapDiv, LogicalDiv logicalSubDiv, Mets mcrMets) {
        // get preceding and following file ids
        final Attribute precedingFileIDAttr = PRECEDING_FILE_ID.evaluateFirst(enmapDiv);
        final Attribute followingFileIDAttr = FOLLOWING_FILE_ID.evaluateFirst(enmapDiv);
        final String precedingFileID = getFileID(precedingFileIDAttr, mcrMets, true);
        final String followingFileID = getFileID(followingFileIDAttr, mcrMets, false);

        // calc from & to
        int from = getPage(precedingFileID).orElseThrow(
            () -> new MCRException("Unable to get page number of file id " + precedingFileID)) + (precedingFileIDAttr != null ? 1 : 0);
        int to = getPage(followingFileID).orElseThrow(
            () -> new MCRException("Unable to get page number of file id " + followingFileID)) - (followingFileIDAttr != null ? 1 : 0);

        if (to < from) {
            throw new MCRException("Cannot calculate from - to pages cause from is lower than to. " + precedingFileID
                + " to " + followingFileID);
        }

        // add fptr, seq & areas
        Fptr fptr = new Fptr();
        Seq seq = new Seq();
        for (int i = from; i <= to; i++) {
            String fileId = replacePage(precedingFileID, i).orElseThrow(
                () -> new MCRException("Error while replacing page in " + precedingFileID));
            Area area = new Area(fileId, null, null, null);
            seq.getAreaList().add(area);
        }
        if (seq.getAreaList().isEmpty()) {
            throw new MCRException("Unable to add mets:areas in " + logicalSubDiv.getId());
        }
        fptr.getSeqList().add(seq);
        logicalSubDiv.getFptrList().add(fptr);
    }

    /**
     * Get file id of the first or the last physical div.
     *
     * @param attr the fileID attribute
     * @param mcrMets the mcr mets
     * @param first the first or the last file id
     *
     * @return optional of the file ID
     */
    private String getFileID(Attribute attr, Mets mcrMets, boolean first) {
        if (attr != null) {
            return attr.getValue();
        }
        return getFileID(mcrMets, first).orElseThrow(
            () -> new MCRException("unable to find first FILEID of physical struct map"));
    }

    /**
     * Get file id of the first or the last physical div.
     *
     * @param mcrMets the mcr mets
     * @param first the first or the last file id
     *
     * @return optional of the file ID
     */
    private Optional<String> getFileID(Mets mcrMets, boolean first) {
        List<PhysicalSubDiv> children = mcrMets.getPhysicalStructMap().getDivContainer().getChildren();
        if (children != null && !children.isEmpty()) {
            int index = first ? 0 : children.size() - 1;
            PhysicalSubDiv physicalSubDiv = children.get(index);
            for (Fptr fptr : physicalSubDiv.getChildren()) {
                if (fptr.getFileId().contains("-ALTO")) {
                    return Optional.of(fptr.getFileId());
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Returns the page as integer of the the given fileID.
     *
     * @param fileID the file id to get the page
     * @return the page
     */
    private Optional<Integer> getPage(String fileID) {
        Matcher m = NUMBER_PATTERN.matcher(fileID);
        Integer lastMatch = null;
        while (m.find()) {
            lastMatch = Integer.valueOf(m.group());
        }
        return lastMatch != null ? Optional.of(lastMatch) : Optional.empty();
    }

    private Optional<String> replacePage(String fileID, int number) {
        Matcher m = NUMBER_PATTERN.matcher(fileID);
        Integer lastFrom = null;
        Integer lastTo = null;
        while (m.find()) {
            lastFrom = m.start();
            lastTo = m.end();
        }
        if (lastFrom != null && lastTo != null) {
            int length = lastTo - lastFrom;
            String numberAsString = String.format("%0" + length + "d", number);
            StringBuffer result = new StringBuffer();
            result.append(fileID.substring(0, lastFrom));
            result.append(numberAsString);
            result.append(fileID.substring(lastTo));
            return Optional.of(result.toString());
        }
        return Optional.empty();
    }

    /**
     * It can happen that some 
     * &lt;mets:area FILEID="FID-ThULB_129489824_1887_Perthes_XXXX-ALTO" /&gt; are
     * duplicated. Thats only important for areas which have no BEGIN,END,BETYPE
     * attribute. Remove duplicated entries.
     *
     * @param mets the mets to fix
     */
    private void fixMetsAreas(Mets mets) {
        List<LogicalDiv> divs = mets.getLogicalStructMap().getDivContainer().getChildren();
        divs = Lists.reverse(divs);
        Set<String> uniqueFILEIDs = new HashSet<>();
        for (LogicalDiv div : divs) {
            for (Fptr fptr : div.getFptrList()) {
                for (Seq seq : fptr.getSeqList()) {
                    List<Area> areaList = new ArrayList<>(seq.getAreaList());
                    for (Area area : areaList) {
                        if (area.getBetype() == null) {
                            if (!uniqueFILEIDs.contains(area.getFileId())) {
                                uniqueFILEIDs.add(area.getFileId());
                            } else {
                                seq.getAreaList().remove(area);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    protected Map<String, Set<Block>> getAltoBlockMap(List<Element> enmapAreas, List<ALTO> altoReferences) {
        Map<String, Set<Block>> altoBlockMap = super.getAltoBlockMap(enmapAreas, altoReferences);

        // add missing pages to block map, some images don't have mets:area's. Add those.
        List<String> sortedFileIDs = new ArrayList<String>(altoBlockMap.keySet());
        if(sortedFileIDs.isEmpty()) {
            return altoBlockMap;
        }
        sortedFileIDs.sort((s1, s2) -> s1.compareTo(s2));
        List<String> missingFileIDs = new ArrayList<>();

        String baseFileID = sortedFileIDs.get(0);
        Integer from = getPage(baseFileID).get();
        Integer to = getPage(sortedFileIDs.get(sortedFileIDs.size() - 1)).get();

        for(int i = from + 1; i < to; i++) {
            String fileId = replacePage(baseFileID, i).get();
            if(!sortedFileIDs.contains(fileId)) {
                missingFileIDs.add(fileId);
            }
        }

        // add missing file ids
        missingFileIDs.forEach(fileId -> {
            altoBlockMap.put(fileId, new HashSet<>());
        });

        return altoBlockMap;
    }

}
