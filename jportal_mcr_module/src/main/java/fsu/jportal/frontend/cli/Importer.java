package fsu.jportal.frontend.cli;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.mycore.common.MCRException;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.metadata.MCRObjectUtils;
import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.frontend.cli.annotation.MCRCommand;
import org.mycore.frontend.cli.annotation.MCRCommandGroup;
import org.mycore.mets.model.Mets;
import org.mycore.mets.model.files.FLocat;
import org.mycore.mets.model.files.FileGrp;
import org.mycore.mets.model.files.FileSec;
import org.mycore.mets.model.struct.Fptr;
import org.mycore.mets.model.struct.LOCTYPE;
import org.mycore.mets.model.struct.LogicalDiv;
import org.mycore.mets.model.struct.LogicalStructMap;
import org.mycore.mets.model.struct.PhysicalStructMap;
import org.mycore.mets.model.struct.PhysicalSubDiv;
import org.mycore.mets.model.struct.SmLink;

import fsu.jportal.backend.JPContainer;
import fsu.jportal.backend.JPDerivateComponent;
import fsu.jportal.backend.JPVolume;
import fsu.jportal.backend.io.ImportSink;
import fsu.jportal.backend.io.RecursiveImporter;
import fsu.jportal.frontend.cli.io.HttpImportSource;
import fsu.jportal.frontend.cli.io.LocalSystemSink;
import fsu.jportal.mets.JPortalMetsGenerator;
import fsu.jportal.mets.LLZMetsConverter;
import fsu.jportal.mets.MetsImportUtils;
import fsu.jportal.mets.MetsImporter;
import fsu.jportal.util.JPComponentUtil;
import fsu.jportal.util.MetsUtil;

/**
 * Created by chi on 22.04.15.
 * @author Huu Chi Vu
 */
@MCRCommandGroup(name = "JPortal Importer")
public class Importer {

    private static final String ALTO_FOLDER = "mcralto";

    static Logger LOGGER = LogManager.getLogger(Importer.class);

    @MCRCommand(syntax = "importObj {0} {1}", help = "importObj webappURL id")
    public static void importObj(String urlStr, String id) {
        HttpImportSource httpImportSource = new HttpImportSource(urlStr, id);
        ImportSink localSystem = new LocalSystemSink(urlStr);
        new RecursiveImporter(httpImportSource, localSystem).start();
    }

    /**
     * Does a mets.xml import.
     * 
     * @param derivateId the derivate requires a mets.xml inside
     * @param importerClassName the mets importer class
     * @throws Exception something went wrong
     */
    @MCRCommand(syntax = "importMets {0} {1}", help = "importMets {derivateId} {fully qualified classname of the importer}")
    public static void importMets(String derivateId, String importerClassName) throws Exception {
        MetsImportUtils.checkPermission(derivateId);

        // get mets
        Document metsXML = MetsUtil.getMetsXMLasDocument(derivateId);
        Mets mets = new Mets(metsXML);

        // get importer
        Class<? extends MetsImporter> importerClass = Class.forName(importerClassName).asSubclass(MetsImporter.class);
        MetsImporter importer = importerClass.newInstance();

        MetsImportUtils.importMets(derivateId, mets, importer);
    }

    /**
     * Does a mets import for a year.
     *
     * <pre>
     * importMetsYear jportal_jpvolume_00000002 /data/temp/mnt/images/Jenaer_Volksblatt_1914_167758667_tif/mets.xml /data/temp/mnt/images/OCRbearbInnsbruck_2/1914
     * </pre>
     * 
     * @param targetID the target volume
     * @param metsPath path to the mets.xml file
     * @param contentPath path on the file system to the images and the alto files
     * @param missingNumbers comma separated list of numbers which are missing. those missing number
     *        are created as volumes but have no content. If no numbers are missing use the 0 value.
     * 
     * @throws Exception darn! something went wrong
     */
    @MCRCommand(syntax = "importMetsYear {0} {1} {2}", help = "importMetsYear {targetID} {path to mets.xml} {path to ocr folder}")
    public static List<String> importMetsYear(String targetID, String metsPath, String contentPath) throws Exception {
        MCRObjectID target = MCRObjectID.getInstance(targetID);
        if (!MCRMetadataManager.exists(target)) {
            throw new MCRException("Unable to find " + target);
        }
        LOGGER.info("Import mets to " + targetID);
        Mets mets = getMets(metsPath);
        LogicalDiv rootDiv = getLogicalRootDiv(mets);

        List<String> monthCommands = new ArrayList<>();

        for (LogicalDiv monthDiv : rootDiv.getChildren()) {
            String command = "importMetsMonth " + targetID + " " + metsPath + " " + contentPath + " "
                + monthDiv.getOrder();
            monthCommands.add(command);
        }
        return monthCommands;
    }

    /**
     * importMetsMonth jportal_jpvolume_00000374 /data/temp/mnt/images/Jenaer_Volksblatt_1915_167758667_tif/mets.xml /data/temp/mnt/images/OCRbearbInnsbruck_1915_2/1915 1
     * 
     * @param targetID
     * @param metsPath
     * @param contentPath
     * @param monthIndex
     */
    @MCRCommand(syntax = "importMetsMonth {0} {1} {2} {3}", help = "importMetsMonth {targetID} {path to mets.xml} {path to ocr folder} {number of month [1-12]}")
    public static List<String> importMetsMonth(String targetID, String metsPath, String contentPath, int monthIndex)
        throws Exception {
        // create month
        MCRObjectID target = MCRObjectID.getInstance(targetID);
        if (!MCRMetadataManager.exists(target)) {
            throw new MCRException("Unable to find " + target);
        }
        Optional<JPContainer> parentOptional = JPComponentUtil.getContainer(target);

        JPVolume month = new JPVolume();
        month.setTitle(MetsImportUtils.MONTH_NAMES.get(monthIndex));
        month.setParent(target);
        month.setHiddenPosition(monthIndex);
        parentOptional.ifPresent(parent -> {
            MetsImportUtils.setPublishedDate(monthIndex, month, parent);
        });
        month.store();

        // create day commands
        List<String> dayCommands = new ArrayList<>();
        Mets mets = getMets(metsPath);
        LogicalDiv rootDiv = getLogicalRootDiv(mets);
        Optional<LogicalDiv> monthDiv = getMonth(rootDiv, monthIndex);
        optionalToStream(monthDiv).flatMap(div -> div.getChildren().stream()).forEach(day -> {
            String command = "importMetsDay " + month.getObject().getId() + " " + metsPath + " " + contentPath + " "
                + monthIndex + " " + day.getOrder();
            dayCommands.add(command);
        });
        return dayCommands;
    }

    /**
     * importMetsDay jportal_jpvolume_00000374 /data/temp/mnt/images/Jenaer_Volksblatt_1915_167758667_tif/mets.xml /data/temp/mnt/images/OCRbearbInnsbruck_1915_2/1915 1 1
     * 
     * @param targetID
     * @param metsPath
     * @param contentPath
     * @param monthIndex
     * @param dayOrder
     */
    @MCRCommand(syntax = "importMetsDay {0} {1} {2} {3} {4}", help = "importMetsDay {targetID} {path to mets.xml} {path to ocr folder} {number of month [1-12]} {order number of day}")
    public static void importMetsDay(String targetID, String metsPath, String contentPath, int monthIndex, int dayOrder)
        throws Exception {

        Mets mets = getMets(metsPath);
        LogicalDiv rootDiv = getLogicalRootDiv(mets);
        LogicalDiv dayDiv = getDay(rootDiv, monthIndex, dayOrder).orElse(null);
        if (dayDiv == null) {
            LOGGER.warn("Unable to get day (order) " + dayOrder + " of month (order) " + monthIndex);
            return;
        }

        // parse label
        String[] labels = dayDiv.getLabel().split(" ");
        Integer nr = Integer.valueOf(labels[1].substring(0, labels[1].length() - 1));
        Integer year = Integer.valueOf(labels[4]);
        Integer month = MetsImportUtils.MONTH_NAMES.inverse().get(labels[3]);
        Integer dayOfMonth = Integer.valueOf(labels[2].substring(0, labels[2].length() - 1));

        // create day
        JPVolume day = new JPVolume();
        String title = getDayTitle(year, month, dayOfMonth, nr);
        String date = String.format("%d-%02d-%02d", year, month, dayOfMonth);
        day.setTitle(title);
        day.setDate(date, null);
        day.setHiddenPosition(dayOfMonth);
        day.setParent(MCRObjectID.getInstance(targetID));

        // derivate
        Path metsFolderPath = Paths.get(metsPath).getParent();
        Path altoFolderPath = Paths.get(contentPath);

        JPDerivateComponent derivate = buildDerivate(mets, dayDiv, metsFolderPath, altoFolderPath);
        day.addDerivate(derivate);

        // store the day and its derivate
        day.store();
    }

    @MCRCommand(syntax = "fixLLZ {0} {1}", help = "fixLLZ {mycore object id} {path to the mets with the coordination}")
    public static void fixLLZ(String targetID, String pathToCoordsMets) throws Exception {
        // get object
        MCRObjectID objId = MCRObjectID.getInstance(targetID);
        MCRObject mcrObj = MCRMetadataManager.retrieveMCRObject(objId);
        // get derivate
        MCRObjectID derId = mcrObj.getStructure().getDerivates().get(0).getXLinkHrefID();

        // build mets from object structure
        ImporterMetsGenerator metsGenerator = new ImporterMetsGenerator();
        HashSet<MCRPath> ignoreNodes = new HashSet<MCRPath>();
        Mets newMets = metsGenerator.getMETS(MCRPath.getPath(derId.toString(), "/"), ignoreNodes);

        // add alto
        FileGrp altoGrp = new FileGrp("ALTO");
        newMets.getFileSec().addFileGrp(altoGrp);
        newMets.getFileSec().getFileGroup("MASTER").getFileList().forEach(tifFile -> {
            String id = tifFile.getId().replaceAll("master_", "alto_");
            String href = "alto/" + tifFile.getFLocat().getHref().replaceAll(".tif", ".xml");
            org.mycore.mets.model.files.File altoFile = new org.mycore.mets.model.files.File(id, "text/xml");
            altoFile.setFLocat(new FLocat(LOCTYPE.URL, href));
            altoGrp.addFile(altoFile);
        });
        PhysicalStructMap physicalStructMap = (PhysicalStructMap) newMets.getStructMap("PHYSICAL");
        physicalStructMap.getDivContainer().getChildren().forEach(div -> {
            Fptr tifFptr = div.getChildren().get(0);
            div.add(new Fptr(tifFptr.getFileId().replaceAll("master_", "alto_")));
        });

        // build the llz
        LLZMetsConverter llzConverter = new LLZMetsConverter();
        llzConverter.setFailOnEmptyAreas(false);
        SAXBuilder builder = new SAXBuilder();
        Document llzDoc = builder.build(new File(pathToCoordsMets));
        Mets llzMets = llzConverter.convert(llzDoc, Paths.get(pathToCoordsMets).getParent());
        List<String> emptyAreas = llzConverter.getEmptyAreas();
        if (!emptyAreas.isEmpty()) {
            LOGGER.warn(
                MetsImportUtils.buildBlockReferenceError("There are unresolved <mets:area>", emptyAreas, llzDoc));
        }

        // build the logical structure fptr's of the newMets
        LogicalStructMap llZLogicalStructMap = (LogicalStructMap) llzMets.getStructMap(LogicalStructMap.TYPE);
        buildLogicalFptr(llZLogicalStructMap.getDivContainer(), llzMets, newMets);

        // save the old mets.xml
        MCRPath metsPath = MCRPath.getPath(derId.toString(), "mets.xml");
        Path saveDirectoryPath = Paths.get(System.getProperty("user.home")).resolve("jportal");
        Files.createDirectories(saveDirectoryPath);
        Path savePath = saveDirectoryPath.resolve(derId.toString() + "_mets.xml");
        LOGGER.info("Saving old mets.xml to " + savePath.toAbsolutePath().toString());
        Files.copy(metsPath, savePath, StandardCopyOption.REPLACE_EXISTING);

        // replace
        LOGGER.info("replacing mets.xml...");
        MCRJDOMContent newMetsContent = new MCRJDOMContent(newMets.asDocument());
        Files.copy(newMetsContent.getInputStream(), metsPath, StandardCopyOption.REPLACE_EXISTING);
    }

    private static void buildLogicalFptr(LogicalDiv llzDiv, Mets llzMets, Mets newMets) {
        LogicalStructMap newLogicalStructMap = (LogicalStructMap) newMets.getStructMap(LogicalStructMap.TYPE);
        List<Fptr> fptrList = llzDiv.getFptrList();
        if (!fptrList.isEmpty()) {
            List<Integer> hierarchy = new ArrayList<>();
            buildDivHierarchy(llzDiv, hierarchy);
            try {
                LogicalDiv newDiv = getDivFromHierarchy(newLogicalStructMap.getDivContainer(), hierarchy);
                fptrList.stream()
                        .flatMap(fptr -> fptr.getSeqList().stream())
                        .flatMap(seq -> seq.getAreaList().stream())
                        .forEach(area -> {
                            String oldId = area.getFileId();
                            String href = llzMets.getFileSec()
                                                 .getFileGroup("ALTO")
                                                 .getFileById(oldId)
                                                 .getFLocat()
                                                 .getHref();
                            String newId = newMets.getFileSec().getFileGroup("ALTO").getFileByHref(href).getId();
                            area.setFileId(newId);
                        });
                newDiv.getFptrList().addAll(fptrList);
            } catch (Exception exc) {
                LOGGER.warn("Unable to get corresponding 'new div' for 'llz div id' " + llzDiv.getId(), exc);
            }
            return;
        }
        for (LogicalDiv subDiv : llzDiv.getChildren()) {
            buildLogicalFptr(subDiv, llzMets, newMets);
        }
    }

    /**
     * Builds the hierarchy for the given fromDiv. The hierarchy list contains
     * the index position for each level. The list starts with the root element
     * index. 
     * 
     * @param fromDiv
     * @param hierarchy
     */
    private static void buildDivHierarchy(LogicalDiv fromDiv, List<Integer> hierarchy) {
        LogicalDiv parent = fromDiv.getParent();
        if (parent != null) {
            int indexOf = parent.getChildren().indexOf(fromDiv);
            buildDivHierarchy(parent, hierarchy);
            hierarchy.add(indexOf);
        }
    }

    private static LogicalDiv getDivFromHierarchy(LogicalDiv div, List<Integer> hierarchy) {
        for (Integer indexOf : hierarchy) {
            div = div.getChildren().get(indexOf);
        }
        return div;
    }

    private static JPDerivateComponent buildDerivate(Mets mets, LogicalDiv logicalDiv, Path metsFolderPath,
        Path altoFolderPath) {
        JPDerivateComponent derivate = new JPDerivateComponent();

        // get stream of images for the logical day
        Stream<String> images = getImagesOfLogicalDiv(mets, logicalDiv);

        images.forEach(imageFile -> {
            try {
                Path absoluteImagePath = metsFolderPath.resolve(imageFile);
                String volumeFolder = String.join("_", Arrays.copyOf(imageFile.split("_"), 4));
                String altoFile = imageFile.replace(".tif", ".xml");
                Path absoluteAltoPath = altoFolderPath.resolve(volumeFolder).resolve(ALTO_FOLDER).resolve(altoFile);

                if (Files.exists(absoluteImagePath) && !Files.isDirectory(absoluteImagePath)) {
                    derivate.add(absoluteImagePath.toUri().toURL(), imageFile);
                } else {
                    LOGGER.warn("Image not found " + absoluteImagePath.toString());
                }
                if (Files.exists(absoluteAltoPath) && !Files.isDirectory(absoluteAltoPath)) {
                    derivate.add(absoluteAltoPath.toUri().toURL(), "alto/" + altoFile);
                } else {
                    LOGGER.warn("ALTO not found " + absoluteAltoPath.toString());
                }
            } catch (Exception exc) {
                LOGGER.error("Unable to add image to derivate", exc);
            }
        });
        return derivate;
    }

    private static Stream<String> getImagesOfLogicalDiv(Mets mets, LogicalDiv dayDiv) {
        PhysicalStructMap physicalStructMap = (PhysicalStructMap) mets.getStructMap(PhysicalStructMap.TYPE);
        FileSec fileSec = mets.getFileSec();
        FileGrp masterGroup = fileSec.getFileGroup("MASTER");

        Stream<String> images = dayDiv.getChildren().stream().map(LogicalDiv::getId).flatMap(logId -> {
            return mets.getStructLink().getSmLinkByFrom(logId).stream().map(SmLink::getTo);
        }).distinct().flatMap(physId -> {
            PhysicalSubDiv physDiv = physicalStructMap.getDivContainer().get(physId);
            return physDiv.getChildren().stream().map(Fptr::getFileId);
        }).distinct().map(fileId -> {
            return masterGroup.getFileById(fileId).getFLocat().getHref();
        }).distinct();
        return images;
    }

    private static Mets getMets(String metsPath) throws JDOMException, IOException {
        SAXBuilder builder = new SAXBuilder();
        Document xml = builder.build(new File(metsPath));
        return new Mets(xml);
    }

    private static LogicalDiv getLogicalRootDiv(Mets mets) {
        LogicalStructMap logicalStructMap = (LogicalStructMap) mets.getStructMap(LogicalStructMap.TYPE);
        return logicalStructMap.getDivContainer();
    }

    private static Optional<LogicalDiv> getMonth(LogicalDiv rootDiv, int orderNumber) {
        return rootDiv.getChildren().stream().filter(div -> div.getOrder() == orderNumber).findAny();
    }

    private static Optional<LogicalDiv> getDay(LogicalDiv rootDiv, int monthOrder, int dayOrder) {
        return optionalToStream(getMonth(rootDiv, monthOrder)).flatMap(div -> div.getChildren().stream())
                                                              .filter(day -> day.getOrder() == dayOrder)
                                                              .findFirst();
    }

    /**
     * https://bugs.openjdk.java.net/browse/JDK-8050820
     * 
     * TODO: remove in jdk 9
     * 
     * @param optional
     * @return
     */
    public static <T> Stream<T> optionalToStream(Optional<T> optional) {
        return optional.map(Stream::of).orElse(Stream.empty());
    }

    private static String getDayTitle(Integer year, Integer month, Integer dayOfMonth, Integer nr) {
        StringBuffer title = new StringBuffer();
        title.append("Nr. ").append(nr).append(" : ");
        // date
        LocalDate localDate = LocalDate.of(year, month, dayOfMonth);
        ZonedDateTime dateTime = ZonedDateTime.of(localDate, LocalTime.MIDNIGHT, ZoneId.of("UTC+1"));
        String dayAsString = dateTime.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.GERMANY);
        String monthAsString = dateTime.getMonth().getDisplayName(TextStyle.FULL, Locale.GERMANY);
        title.append(dayAsString).append(", den ");
        title.append(dayOfMonth).append(". ").append(monthAsString).append(" ").append(year);
        return title.toString();
    }

    private static class ImporterMetsGenerator extends JPortalMetsGenerator {

        @Override
        protected List<MCRObject> getChildren(MCRObject parentObject) {
            List<MCRObject> children = MCRObjectUtils.getChildren(parentObject);
            children.sort((o1, o2) -> o1.getId().toString().compareTo(o2.getId().toString()));
            return children;
        }

    }

}
