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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.common.MCRException;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.cli.annotation.MCRCommand;
import org.mycore.frontend.cli.annotation.MCRCommandGroup;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;

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

    static Map<Integer, String> MONTH_NAMES = ImmutableMap.<Integer, String> builder().put(1, "Januar")
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
     * importJVB jportal_jpvolume_00000001 /data/temp/ThULB_TIFF.xml /data/temp/mnt/images 175,178
     * </pre>
     * 
     * @param targetID the target volume
     * @param thulbTiffXmlPath path to the xml file
     * @param contentPath path on the file system to the images and the alto files
     * @param missingNumbers comma separated list of numbers which are missing. those missing number
     *        are created as volumes but have no content. If no numbers are missing use the 0 value.
     * 
     * @throws Exception darn! something went wrong
     */
    @MCRCommand(syntax = "importJVB {0} {1} {2} {3}", help = "importJVB {targetID} {path to ThULB_TIFF.xml} {path to the images and alto files} {missing numbers}")
    public static List<String> importJVB(String targetID, String thulbTiffXmlPath, String contentPath,
        String missingNumbers) throws Exception {
        MCRObjectID target = MCRObjectID.getInstance(targetID);
        if (!MCRMetadataManager.exists(target)) {
            throw new MCRException("Unable to find " + target);
        }
        LOGGER.info("Import JVB to " + targetID);
        Element yearElement = getYearElement(thulbTiffXmlPath);

        AtomicInteger nr = new AtomicInteger();
        int month = 0;
        int day = 0;

        List<Integer> missingNumberList = missingNumbers(missingNumbers);
        List<String> monthCommands = new ArrayList<>();

        XPathExpression<Element> issueXPath = XPathFactory.instance().compile("Issue", Filters.element());
        List<Element> issues = issueXPath.evaluate(yearElement);
        for (Element issueElement : issues) {
            while (missingNumberList.contains(nr.get())) {
                nr.incrementAndGet();
            }
            Integer issueMonth = Integer.valueOf(issueElement.getAttributeValue("month"));
            Integer issueDay = Integer.valueOf(issueElement.getAttributeValue("day"));
            if (month != issueMonth) {
                month = issueMonth;
                day = issueDay;
                String command = "importJVBMonth " + targetID + " " + thulbTiffXmlPath + " " + contentPath + " "
                    + missingNumbers + " " + month + " " + nr.incrementAndGet();
                monthCommands.add(command);
            } else if (day != issueDay) {
                day = issueDay;
                nr.incrementAndGet();
            }
        }
        return monthCommands;
    }

    /**
     * importJVBMonth jportal_jpvolume_00000003 /data/temp/ThULB_TIFF.xml /data/temp/mnt/images 175,178 1 1
     * 
     * @param targetID
     * @param thulbTiffXmlPath
     * @param contentPath
     * @param missingNumbers comma separated list of numbers which are missing. those missing number
     *        are created as volumes but have no content. If no numbers are missing use the 0 value.
     * @param monthIndex
     * @param issueNumber
     * @return
     * @throws Exception
     */
    @MCRCommand(syntax = "importJVBMonth {0} {1} {2} {3} {4} {5}", help = "importJVBMonth {targetID} {path to ThULB_TIFF.xml} {path to the images and alto files} {missing numbers} {number of month [1-12]} {nr. of issue}")
    public static List<String> importJVBMonth(String targetID, String thulbTiffXmlPath, String contentPath,
        String missingNumbers, int monthIndex, int issueNumber) throws Exception {
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
        List<Integer> missingNumberList = missingNumbers(missingNumbers);
        List<String> dayCommands = new ArrayList<>();
        Element yearElement = getYearElement(thulbTiffXmlPath);
        XPathExpression<Attribute> daysXPath = XPathFactory.instance()
            .compile("Issue[@month='" + monthIndex + "']/@day", Filters.attribute());
        List<Attribute> days = daysXPath.evaluate(yearElement);
        AtomicInteger nr = new AtomicInteger(issueNumber);
        days.stream().filter(distinctByKey(a -> a.getValue())).forEach(dayAttr -> {
            while (missingNumberList.contains(nr.get())) {
                nr.incrementAndGet();
            }
            String command = "importJVBDay " + month.getObject().getId() + " " + thulbTiffXmlPath + " " + contentPath
                + " " + monthIndex + " " + dayAttr.getValue() + " " + nr.getAndIncrement();
            dayCommands.add(command);
        });
        return dayCommands;
    }

    /**
     * importJVBDay jportal_jpvolume_00000003 /data/temp/ThULB_TIFF.xml /data/temp/mnt/images 1 1 1
     * 
     * @param targetID
     * @param thulbTiffXmlPath
     * @param contentPath
     * @param monthIndex
     * @param dayOfMonth
     * @param issueNumber
     * @throws Exception
     */
    @MCRCommand(syntax = "importJVBDay {0} {1} {2} {3} {4} {5}", help = "importJVBDay {targetID} {path to ThULB_TIFF.xml} {path to the images and alto files} {number of month [1-12]} {number of day [1-31]} {nr. of issue}")
    public static void importJVBDay(String targetID, String thulbTiffXmlPath, String contentPath, int monthIndex,
        int dayOfMonth, int issueNumber) throws Exception {
        // get the first issue
        Element yearElement = getYearElement(thulbTiffXmlPath);
        XPathExpression<Element> issuesXPath = XPathFactory.instance()
            .compile("Issue[@month='" + monthIndex + "' and @day='" + dayOfMonth + "']", Filters.element());
        Element issueElement = issuesXPath.evaluateFirst(yearElement);

        String issueValue = issueElement.getAttributeValue("value");
        String issueDayValue = issueValue.substring(0, issueValue.lastIndexOf("_"));
        Integer yearIndex = Integer.valueOf(issueElement.getAttributeValue("year"));

        // create day
        JPVolume day = new JPVolume();
        String title = getDayTitle(yearIndex, monthIndex, dayOfMonth, issueNumber);
        String date = String.format("%d-%02d-%02d", yearIndex, monthIndex, dayOfMonth);
        day.setTitle(title);
        day.setDate(date, null);
        day.setHiddenPosition(dayOfMonth);
        day.setParent(MCRObjectID.getInstance(targetID));

        // derivate
        JPDerivateComponent derivate = new JPDerivateComponent();
        day.addDerivate(derivate);
        XPathExpression<Element> imagesXPath = XPathFactory.instance()
            .compile("Issue[@month='" + monthIndex + "' and @day='" + dayOfMonth + "']/Image", Filters.element());
        List<Element> images = imagesXPath.evaluate(yearElement);
        Path rootPath = Paths.get(contentPath);
        for (Element imageElement : images) {
            String imageName = imageElement.getAttributeValue("name").replace(".TIF", ".tif");
            String windowsImagePath = imageElement.getAttributeValue("path").replace(".TIF", ".tif");
            String imageFileSizeString = imageElement.getAttributeValue("size");
            Integer imageFileSize = imageFileSizeString != null ? Integer.valueOf(imageFileSizeString) : null;

            // ThULB_TIFF/ThULB/1915/JVB_19150101_001_167758667_B1/JVB_19150101_001_167758667_B1_001.TIF
            Path relativeImagePath = convertWindowsPath(windowsImagePath, 5);
            // OCRbearbInnsbruck_1915_2/1915/JVB_19150413_085_167758667/alto/JVB_19150413_085_167758667_B1_001.xml
            String altoName = imageName.replace(".tif", ".xml");
            Path relativeAltoPath = Paths.get("OCRbearbInnsbruck_1915", String.valueOf(yearIndex), issueDayValue,
                "alto", altoName);

            // add to derivate
            Path absoluteImagePath = rootPath.resolve(relativeImagePath);
            Path absoluteAltoPath = rootPath.resolve(relativeAltoPath);
            if (!Files.exists(absoluteImagePath) || Files.isDirectory(absoluteImagePath)) {
                throw new FileNotFoundException(absoluteImagePath.toString() + " not found");
            }
            if (!Files.exists(absoluteAltoPath) || Files.isDirectory(absoluteAltoPath)) {
                throw new FileNotFoundException(absoluteAltoPath.toString() + " not found");
            }
            derivate.add(absoluteImagePath.toUri().toURL(), imageName, imageFileSize);
            derivate.add(absoluteAltoPath.toUri().toURL(), "alto/" + altoName);
        }

        // store the day and its derivate
        day.store();
    }

    private static Element getYearElement(String thulbTiffXmlPath) throws JDOMException, IOException {
        SAXBuilder builder = new SAXBuilder();
        Document xml = builder.build(new File(thulbTiffXmlPath));
        return xml.getRootElement().getChild("NewsID").getChild("Year");
    }

    /**
     * Returns a path where the last amount of parts are combined.
     * 
     * @param windowsPath windows path
     * @param lastNumParts number of parts which should be combined
     * @return
     */
    private static Path convertWindowsPath(String windowsPath, int lastNumParts) {
        String[] pathParts = windowsPath.split("\\\\");
        int startIndex = pathParts.length - lastNumParts;
        Path path = Paths.get(pathParts[startIndex]);
        for (++startIndex; startIndex < pathParts.length; startIndex++) {
            path = path.resolve(pathParts[startIndex]);
        }
        return path;
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

    /**
     * http://stackoverflow.com/questions/23699371/java-8-distinct-by-property
     * 
     * @param keyExtractor
     * @return
     */
    private static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    /**
     * Converts a string of comma separated numbers to a list of integers.
     * 
     * @param missingNumbersString comma separated numbers
     * @return list of those numbers
     */
    private static List<Integer> missingNumbers(String missingNumbersString) {
        return Arrays.asList(missingNumbersString.split(",")).stream().map(Integer::valueOf)
            .collect(Collectors.toList());
    }

}
