package fsu.jportal.frontend.cli;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.DirectoryStream;
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
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import com.google.common.collect.Lists;
import fsu.jportal.backend.JPComponent.StoreOption;
import fsu.jportal.backend.JPDerivateComponent;
import fsu.jportal.backend.JPPeriodicalComponent;
import fsu.jportal.backend.JPVolume;
import fsu.jportal.backend.io.HttpImportSource;
import fsu.jportal.backend.io.ImportSink;
import fsu.jportal.backend.io.LocalSystemSink;
import fsu.jportal.backend.io.RecursiveImporter;
import fsu.jportal.mets.JPMetsHierarchyGenerator;
import fsu.jportal.mets.LLZMetsConverter;
import fsu.jportal.mets.MetsImportUtils;
import fsu.jportal.mets.MetsImporter;
import fsu.jportal.mets.MetsVersionStore;
import fsu.jportal.util.MetsUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.mycore.access.MCRAccessException;
import org.mycore.common.MCRPersistenceException;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.common.MCRMarkManager;
import org.mycore.datamodel.metadata.MCRMetaLinkID;
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
import org.mycore.mets.model.struct.Fptr;
import org.mycore.mets.model.struct.LOCTYPE;
import org.mycore.mets.model.struct.LogicalDiv;
import org.mycore.mets.model.struct.LogicalStructMap;
import org.mycore.mets.model.struct.PhysicalStructMap;
import org.mycore.solr.MCRSolrClientFactory;
import org.mycore.solr.search.MCRSolrSearchUtils;

/**
 * Created by chi on 22.04.15.
 * @author Huu Chi Vu
 */
@MCRCommandGroup(name = "JPortal Importer")
public class Importer {

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

    @MCRCommand(syntax = "fixLLZ {0} {1}", help = "fixLLZ {mycore object id} {path to the mets with the coordination}")
    public static void fixLLZ(String targetID, String pathToCoordsMets) throws Exception {
        // get object
        MCRObjectID objId = MCRObjectID.getInstance(targetID);
        MCRObject mcrObj = MCRMetadataManager.retrieveMCRObject(objId);
        // get derivate
        MCRObjectID derId = mcrObj.getStructure().getDerivates().get(0).getXLinkHrefID();

        // save the old mets.xml
        MCRPath metsPath = MCRPath.getPath(derId.toString(), "mets.xml");
        if (Files.exists(metsPath)) {
            MetsVersionStore.store(derId);
        }

        // build mets from object structure
        LOGGER.info("build mets.xml from jportal volume/article structure...");
        ImporterMetsGenerator metsGenerator = new ImporterMetsGenerator();
        metsGenerator.setDerivatePath(MCRPath.getPath(derId.toString(), "/"));
        Mets newMets = metsGenerator.generate();

        // add alto
        LOGGER.info("add ALTO stuff to generated mets.xml...");
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
        LOGGER.info("load the llz mets.xml...");
        LLZMetsConverter llzConverter = new LLZMetsConverter();
        llzConverter.setFailOnEmptyAreas(false);
        llzConverter.setFailEasyOnStructLinkGeneration(false);
        SAXBuilder builder = new SAXBuilder();
        Document llzDoc = builder.build(new File(pathToCoordsMets));
        Mets llzMets = llzConverter.convert(llzDoc, MCRPath.getPath(derId.toString(), "/"));

        // build the logical structure fptr's of the newMets
        LOGGER.info("combine generated mets.xml and llz mets.xml...");
        LogicalStructMap llZLogicalStructMap = (LogicalStructMap) llzMets.getStructMap(LogicalStructMap.TYPE);
        buildLogicalFptr(llZLogicalStructMap.getDivContainer(), llzMets, newMets);

        // replace
        LOGGER.info("replacing mets.xml...");
        MCRJDOMContent newMetsContent = new MCRJDOMContent(newMets.asDocument());
        try (InputStream metsStream = newMetsContent.getInputStream()) {
            Files.copy(metsStream, metsPath, StandardCopyOption.REPLACE_EXISTING);
        }
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
     * @param fromDiv where to start
     * @param hierarchy index hierarchy
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

    /**
     * https://bugs.openjdk.java.net/browse/JDK-8050820
     * 
     * TODO: remove in jdk 9
     * 
     * @param optional the optional to convert to a stream
     * @return stream of the optional
     */
    public static <T> Stream<T> optionalToStream(Optional<T> optional) {
        return optional.map(Stream::of).orElse(Stream.empty());
    }

    private static String getDayTitle(Integer year, Integer month, Integer dayOfMonth, Integer nr) {
        StringBuilder title = new StringBuilder();
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

    private static class ImporterMetsGenerator extends JPMetsHierarchyGenerator {

        @Override
        protected List<MCRObject> getChildren(MCRObject parentObject) {
            List<MCRObject> children = MCRObjectUtils.getChildren(parentObject);
            children.sort(Comparator.comparing(o -> o.getId().toString()));
            return children;
        }

    }

    @MCRCommand(syntax = "jvbEasyImport {0} {1} {2}", help = "Imports a whole jvb year more easy." + " {year} (1890)"
        + " {goobiId} (25636)" + " {mntPath} (where the goobi id will be mounted locally)")
    public static List<String> jvbEasyImport(String year, String goobiId, String mntPath) throws Exception {
        // unmount always
        try {
            Process unmount = Runtime.getRuntime().exec("fusermount -u " + mntPath);
            unmount.waitFor();
        } catch (Exception exc) {
            LOGGER.error("unable to unmount " + mntPath, exc);
            return Lists.newArrayList();
        }
        // mount
        try {
            String mntCommand = "sshfs root@141.35.20.232:/opt/digiverso/goobi/metadata/" + goobiId + " " + mntPath
                + " -o allow_root";
            Process mount = Runtime.getRuntime().exec(mntCommand);
            if (!mount.waitFor(1, TimeUnit.MINUTES)) {
                LOGGER.error("timeout while mount '" + mntCommand + "'");
                return Lists.newArrayList();
            }

        } catch (Exception exc) {
            LOGGER.error("unable to mount " + mntPath, exc);
            return Lists.newArrayList();
        }
        // check mounted files
        Path imagesPath = Paths.get(mntPath).resolve("images");
        if (!Files.exists(imagesPath)) {
            throw new FileNotFoundException(
                "there should be an images folder " + imagesPath.toAbsolutePath().toString());
        }
        Path ocrPath = imagesPath.resolve("OCRausInnsbruck/" + year);
        if (!Files.exists(ocrPath)) {
            throw new FileNotFoundException("there should be an ocr folder " + ocrPath.toAbsolutePath().toString());
        }
        Path tifPath = imagesPath.resolve("Jenaer_Volksblatt_" + year + "_167758667_JVB_tif");
        if (!Files.exists(tifPath)) {
            throw new FileNotFoundException("there should be a tif folder " + tifPath.toAbsolutePath().toString());
        }
        // get volume
        String yearId;
        String yearQuery = "+parent:jportal_jpjournal_00000109 +date.published:" + year;
        try {
            List<String> ids = MCRSolrSearchUtils.listIDs(MCRSolrClientFactory.getSolrMainClient(), yearQuery);
            if (ids.isEmpty()) {
                LOGGER.error("unable to find volume for year " + year);
                return Lists.newArrayList();
            }
            if (ids.size() > 1) {
                LOGGER.error("cannot import due multiple results for query '" + yearQuery + "'");
                return Lists.newArrayList();
            }
            yearId = ids.get(0);
        } catch (Exception exc) {
            LOGGER.error("unable to retrieve volume by query '" + yearQuery + "'", exc);
            return Lists.newArrayList();
        }
        // empty volume
        // - delete derivate
        MCRObjectID mcrYearId = MCRObjectID.getInstance(yearId);
        MCRObject mcrYear = MCRMetadataManager.retrieveMCRObject(mcrYearId);
        List<MCRMetaLinkID> derivates = mcrYear.getStructure().getDerivates();
        if (!derivates.stream().map(MCRMetaLinkID::getXLinkHrefID).allMatch(derivateId -> {
            try {
                MCRMetadataManager.deleteMCRDerivate(derivateId);
                return true;
            } catch (Exception exc) {
                LOGGER.error("unable to delete derivate " + derivateId, exc);
                return false;
            }
        })) {
            return Lists.newArrayList();
        }
        // - delete children
        List<MCRMetaLinkID> children = mcrYear.getStructure().getChildren();
        if (!children.stream().map(MCRMetaLinkID::getXLinkHrefID).allMatch(objectId -> {
            try {
                MCRMetadataManager.deleteMCRObject(objectId);
                return true;
            } catch (Exception exc) {
                LOGGER.error("unable to delete object " + objectId, exc);
                return false;
            }
        })) {
            LOGGER.error("unable to remove children of " + yearId);
            return Lists.newArrayList();
        }
        // fire import command
        String jvbImport = "jvbImport " + yearId + " " + ocrPath.toAbsolutePath().toString() + " mcraltok "
            + tifPath.toAbsolutePath().toString();
        return Lists.newArrayList(jvbImport);
    }

    @MCRCommand(syntax = "jvbImport {0} {1} {2} {3}", help = "Imports a whole jvb year."
        + " {target mycore object} (jportal_jpvolume_00134247)"
        + " {base path to the JVB_* folders (/mcr/jp/tmp/mnt/images/OCRausInnsbruck/1890)}"
        + " {ocr folder (mcralto|mcraltok)}"
        + " {image path (/mcr/jp/tmp/mnt/images/Jenaer_Volksblatt_1890_167758667_JVB_tif)}")
    public static List<String> jvbImport(String targetID, String ocrPath, String ocrFolder, String imgPath)
        throws IOException {
        List<String> commands = new ArrayList<>();

        Path base = Paths.get(ocrPath);
        String year = base.getFileName().toString().substring(0, 4);
        Set<Integer> months = getMonths(base);
        for (Integer monthIndex : months) {
            String date = year + "-" + String.format("%02d", monthIndex);
            String title = MetsImportUtils.MONTH_NAMES.get(monthIndex);
            try {
                JPVolume volume = new JPVolume();
                volume.setTitle(title);
                volume.setDate(date, JPPeriodicalComponent.DateType.published.name());
                volume.setParent(targetID);
                volume.store();

                String objId = volume.getObject().getId().toString();
                String command = "jvbImportMonth " + objId + " " + monthIndex + " " + ocrPath + " " + ocrFolder + " "
                    + imgPath;
                commands.add(command);
            } catch (Exception exc) {
                LOGGER.error("unable to import jvb month " + title, exc);
            }
        }
        return commands;
    }

    @MCRCommand(syntax = "jvbImportMonth {0} {1} {2} {3} {4}", help = "Imports the nr of a single month. {target mycore object} {month} {path to the innsbruck folder} {ocr folder} {image path}")
    public static List<String> jvbImportMonth(String targetID, String month, String ocrPath, String ocrFolder,
        String imgPath) throws IOException {
        List<String> commands = new ArrayList<>();
        Path base = Paths.get(ocrPath);
        getNrs(base).stream().map(Path::getFileName).map(Path::toString).forEach(monthFolder -> {
            String[] parts = monthFolder.split("_");
            String date = parts[1];
            String nr = parts[2];

            String dyear = date.substring(0, 4);
            String dmonth = date.substring(4, 6);
            String dday = date.substring(6, 8);
            if (!Objects.equals(Integer.valueOf(dmonth), Integer.valueOf(month))) {
                return;
            }
            String title = getDayTitle(Integer.valueOf(dyear), Integer.valueOf(dmonth), Integer.valueOf(dday),
                Integer.valueOf(nr));
            try {
                JPVolume day = new JPVolume();
                day.setTitle(title);
                day.setDate(dyear + "-" + dmonth + "-" + dday, JPPeriodicalComponent.DateType.published.name());
                day.setParent(targetID);
                day.store();
                String altoPath = Paths.get(ocrPath).resolve(monthFolder).resolve(ocrFolder).toString();
                commands.add("jvbUpload " + day.getId() + " " + altoPath + " " + imgPath);
            } catch (Exception exc) {
                LOGGER.error("unable to import jvb nr. " + title, exc);
            }
        });
        return commands;
    }

    @MCRCommand(syntax = "jvbUpload {0} {1} {2}", help = "uploads the files to a number. {target mycore object} {path to alto files} {path to img files}")
    public static void jvbUpload(String targetID, String altoPath, String imgPath)
        throws IOException, MCRPersistenceException, MCRAccessException {
        JPDerivateComponent derivate = new JPDerivateComponent();
        MCRMarkManager.instance().mark(derivate.getId(), MCRMarkManager.Operation.IMPORT);
        try (DirectoryStream<Path> altoStream = Files.newDirectoryStream(Paths.get(altoPath), "*.xml")) {
            String mainDoc = null;
            for(Path alto : altoStream) {
                String altoFileName = alto.getFileName().toString();
                String imgFileName = altoFileName.replaceAll(".xml", ".tif");
                if (mainDoc == null) {
                    derivate.setMainDoc(imgFileName);
                    mainDoc = imgFileName;
                }
                try {
                    derivate.add(Paths.get(imgPath).resolve(imgFileName).toUri().toURL(), imgFileName);
                    derivate.add(alto.toUri().toURL(), "alto/" + altoFileName);
                } catch (MalformedURLException e) {
                    LOGGER.error("Unable to add file to derivate", e);
                }
            }
        }
        JPVolume volume = new JPVolume(targetID);
        volume.addDerivate(derivate);
        volume.store(StoreOption.derivate);
        MCRMarkManager.instance().remove(derivate.getId());
    }

    private static Set<Integer> getMonths(Path dir) throws IOException {
        Set<Integer> months = new TreeSet<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "JVB_*")) {
            stream.forEach(path -> {
                if (!Files.isDirectory(path)) {
                    return;
                }
                String fileName = path.getFileName().toString();
                Integer month = Integer.valueOf(fileName.split("_")[1].substring(4, 6));
                months.add(month);
            });
        }
        return months;

    }

    private static List<Path> getNrs(Path dir) throws IOException {
        List<Path> paths = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "JVB_*")) {
            stream.forEach(path -> {
                if (!Files.isDirectory(path)) {
                    return;
                }
                paths.add(path);
            });
        }
        return paths;
    }

    public static void main(String[] args) throws Exception {
        Set<Integer> months = Importer.getMonths(Paths.get("/data/temp/mnt/images/OCRausInsbruck/1897/"));
        months.forEach(System.out::println);

        List<Path> numbers = Importer.getNrs(Paths.get("/data/temp/mnt/images/OCRausInsbruck/1897/"));
        numbers.forEach(System.out::println);
    }

}
