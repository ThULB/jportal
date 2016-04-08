package fsu.jportal.frontend.cli;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.cli.annotation.MCRCommand;
import org.mycore.frontend.cli.annotation.MCRCommandGroup;
import org.mycore.mets.model.Mets;
import org.mycore.mets.model.files.FileGrp;
import org.mycore.mets.model.files.FileSec;
import org.mycore.mets.model.struct.Fptr;
import org.mycore.mets.model.struct.LogicalDiv;
import org.mycore.mets.model.struct.LogicalStructMap;
import org.mycore.mets.model.struct.PhysicalStructMap;
import org.mycore.mets.model.struct.PhysicalSubDiv;
import org.mycore.mets.model.struct.SmLink;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

import fsu.jportal.backend.JPDerivateComponent;
import fsu.jportal.backend.JPVolume;
import fsu.jportal.backend.io.ImportSink;
import fsu.jportal.backend.io.RecursiveImporter;
import fsu.jportal.frontend.cli.io.HttpImportSource;
import fsu.jportal.frontend.cli.io.LocalSystemSink;

/**
 * Created by chi on 22.04.15.
 * @author Huu Chi Vu
 */
@MCRCommandGroup(name = "Jportal Importer")
public class Importer {

    static Logger LOGGER = LogManager.getLogger(Importer.class);

    static BiMap<Integer, String> MONTH_NAMES = ImmutableBiMap.<Integer, String> builder().put(1, "Januar")
        .put(2, "Februar").put(3, "März").put(4, "April").put(5, "Mai").put(6, "Juni").put(7, "Juli").put(8, "August")
        .put(9, "September").put(10, "Oktober").put(11, "November").put(12, "Dezember").build();

    @MCRCommand(syntax = "importObj {0} {1}", help = "importObj webappURL id")
    public static void importObj(String urlStr, String id) {
        HttpImportSource httpImportSource = new HttpImportSource(urlStr, id);
        ImportSink localSystem = new LocalSystemSink(urlStr);
        new RecursiveImporter(httpImportSource, localSystem).start();
    }

    /**
     * Does the jvb import for a year.
     *
     * <pre>
     * importJVB jportal_jpvolume_00000403 /data/temp/mnt/images/Jenaer_Volksblatt_1915_167758667_tif/mets.xml /data/temp/mnt/images/OCRbearbInnsbruck_1915_2/1915
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
    @MCRCommand(syntax = "importJVB {0} {1} {2}", help = "importJVB {targetID} {path to mets.xml} {path to ocr folder}")
    public static List<String> importJVB(String targetID, String metsPath, String contentPath) throws Exception {
        MCRObjectID target = MCRObjectID.getInstance(targetID);
        if (!MCRMetadataManager.exists(target)) {
            throw new MCRException("Unable to find " + target);
        }
        LOGGER.info("Import JVB to " + targetID);
        Mets mets = getMets(metsPath);
        LogicalDiv rootDiv = getLogicalRootDiv(mets);

        List<String> monthCommands = new ArrayList<>();

        for (LogicalDiv monthDiv : rootDiv.getChildren()) {
            String command = "importJVBMonth " + targetID + " " + metsPath + " " + contentPath + " "
                + monthDiv.getOrder();
            monthCommands.add(command);
        }
        return monthCommands;
    }

    /**
     * importJVBMonth jportal_jpvolume_00000374 /data/temp/mnt/images/Jenaer_Volksblatt_1915_167758667_tif/mets.xml /data/temp/mnt/images/OCRbearbInnsbruck_1915_2/1915 1
     * 
     * @param targetID
     * @param metsPath
     * @param contentPath
     * @param monthIndex
     */
    @MCRCommand(syntax = "importJVBMonth {0} {1} {2} {3}", help = "importJVBMonth {targetID} {path to mets.xml} {path to ocr folder} {number of month [1-12]}")
    public static List<String> importJVBMonth(String targetID, String metsPath, String contentPath, int monthIndex)
        throws Exception {
        // create month
        MCRObjectID target = MCRObjectID.getInstance(targetID);
        if (!MCRMetadataManager.exists(target)) {
            throw new MCRException("Unable to find " + target);
        }
        JPVolume month = new JPVolume();
        month.setTitle(MONTH_NAMES.get(monthIndex));
        month.setParent(target);
        month.setHiddenPosition(monthIndex);
        month.store();

        // create day commands
        List<String> dayCommands = new ArrayList<>();
        Mets mets = getMets(metsPath);
        LogicalDiv rootDiv = getLogicalRootDiv(mets);
        Optional<LogicalDiv> monthDiv = getMonth(rootDiv, monthIndex);
        optionalToStream(monthDiv).flatMap(div -> div.getChildren().stream()).forEach(day -> {
            String command = "importJVBDay " + month.getObject().getId() + " " + metsPath + " " + contentPath + " "
                + monthIndex + " " + day.getOrder();
            dayCommands.add(command);
        });
        return dayCommands;
    }

    /**
     * importJVBDay jportal_jpvolume_00000374 /data/temp/mnt/images/Jenaer_Volksblatt_1915_167758667_tif/mets.xml /data/temp/mnt/images/OCRbearbInnsbruck_1915_2/1915 1 1
     * 
     * @param targetID
     * @param metsPath
     * @param contentPath
     * @param monthIndex
     * @param dayOrder
     */
    @MCRCommand(syntax = "importJVBDay {0} {1} {2} {3} {4}", help = "importJVBDay {targetID} {path to mets.xml} {path to ocr folder} {number of month [1-12]} {order number of day}")
    public static void importJVBDay(String targetID, String metsPath, String contentPath, int monthIndex, int dayOrder)
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
        Integer month = MONTH_NAMES.inverse().get(labels[3]);
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
        JPDerivateComponent derivate = new JPDerivateComponent();
        day.addDerivate(derivate);

        PhysicalStructMap physicalStructMap = (PhysicalStructMap) mets.getStructMap(PhysicalStructMap.TYPE);
        FileSec fileSec = mets.getFileSec();
        FileGrp masterGroup = fileSec.getFileGroup("MASTER");

        // get stream of images for the logical day
        Stream<String> images = dayDiv.getChildren().stream().map(LogicalDiv::getId).flatMap(logId -> {
            return mets.getStructLink().getSmLinkByFrom(logId).stream().map(SmLink::getTo);
        }).distinct().flatMap(physId -> {
            PhysicalSubDiv physDiv = physicalStructMap.getDivContainer().get(physId);
            return physDiv.getChildren().stream().map(Fptr::getFileId);
        }).distinct().map(fileId -> {
            return masterGroup.getFileById(fileId).getFLocat().getHref();
        }).distinct();

        Path metsFolderPath = Paths.get(metsPath).getParent();
        Path altoFolderPath = Paths.get(contentPath);
        images.forEach(imageFile -> {
            try {
                Path absoluteImagePath = metsFolderPath.resolve(imageFile);
                String volumeFolder = String.join("_", Arrays.copyOf(imageFile.split("_"), 4));
                String altoFile = imageFile.replace(".tif", ".xml");
                Path absoluteAltoPath = altoFolderPath.resolve(volumeFolder).resolve("alto").resolve(altoFile);

                if (!Files.exists(absoluteImagePath) || Files.isDirectory(absoluteImagePath)) {
                    throw new FileNotFoundException(absoluteImagePath.toString() + " not found");
                }
                if (!Files.exists(absoluteAltoPath) || Files.isDirectory(absoluteAltoPath)) {
                    throw new FileNotFoundException(absoluteAltoPath.toString() + " not found");
                }
                derivate.add(absoluteImagePath.toUri().toURL(), imageFile);
                derivate.add(absoluteAltoPath.toUri().toURL(), "alto/" + altoFile);
            } catch (Exception exc) {
                LOGGER.error("Unable to add image to derivate", exc);
            }
        });

        // store the day and its derivate
        day.store();
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
            .filter(day -> day.getOrder() == dayOrder).findFirst();
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

}
