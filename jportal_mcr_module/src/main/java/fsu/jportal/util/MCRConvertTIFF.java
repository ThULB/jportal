/**
 * 
 */
package fsu.jportal.util;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.imagetiler.MCRImage;
import org.mycore.imagetiler.MCRTiledPictureProps;
import org.mycore.iview2.services.MCRIView2Tools;

import fsu.jportal.backend.DerivateTools;

public class MCRConvertTIFF {
    private static final Logger LOGGER = LogManager.getLogger(MCRConvertTIFF.class);

    private static List<String> com = new ArrayList<String>(Arrays.asList("tiffcp", "-c", "none", "-p", "contig", "-t", "-L",
            "-f", "msb2lsb", "-w", "256", "-l", "256"));

    public static void startProcessTiff(MCRDerivate derivate, String fileName) throws IOException, UnsupportedOperationException, Exception {
        if (fileName == null || fileName.isEmpty()) {
            MCRPath root = MCRPath.getPath(derivate.getId().toString(), "/");
            Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (file.getFileName().toString().toLowerCase(Locale.getDefault()).endsWith(".tiff")
                            || file.getFileName().toString().toLowerCase(Locale.getDefault()).endsWith(".tif")) {
                        try {
                            processTiffStep(derivate, file.getFileName().toString());
                        } catch (UnsupportedOperationException uoEx) {
                            LOGGER.error("Something gone wrong. Maybe the file is not ready, please wait a bit and try again. ", uoEx);
                        } catch (Exception e) {
                            LOGGER.error("Problems to process the File! ", e);
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } else {
            processTiffStep(derivate, fileName);
        }
    }

    private static void processTiffStep(MCRDerivate derivate, String filename) throws IOException, UnsupportedOperationException, Exception {
        processTiff(derivate, filename);
    }

    private static void processTiff(MCRDerivate derivate, String filename) throws Exception {
        String[] fileNameTockens = filename.split("\\.(?=[^\\.]+$)");

        String mainDoc = derivate.getDerivate().getInternals().getMainDoc();
        String ifsid = derivate.getDerivate().getInternals().getIFSID();
        //get the file and save it
        Path tiledFile = MCRImage.getTiledFile(MCRIView2Tools.getTileDir(), derivate.getId().toString(),
                fileNameTockens[0]);
        MCRTiledPictureProps imageProps = MCRTiledPictureProps.getInstanceFromFile(tiledFile);
        Integer zoomLevel = imageProps.getZoomlevel();
        BufferedImage image = MCRIView2Tools.getZoomLevel(tiledFile, zoomLevel);

        //create tmp files
        Path tempDir = Files.createTempDirectory("editTiff-");
        Path tempImage = tempDir.resolve("old_" + filename);
        Path editedTempImage = tempDir.resolve(filename);
        //save bufferedImage to tmp file
        ImageIO.write(image, fileNameTockens[1], tempImage.toFile());

        //add the paths to command
        com.add(tempImage.toAbsolutePath().toString());
        com.add(editedTempImage.toAbsolutePath().toString());

        //start process
        ProcessBuilder prb = new ProcessBuilder(com);
        Process process = prb.start();
        process.waitFor();

        //remove the paths from command
        com.remove(com.size() - 1);
        com.remove(com.size() - 1);

        //delete derivate file
        MCRPath pathToFile = MCRPath.getPath(derivate.getId().toString(), filename);
        Files.delete(pathToFile);

        Files.delete(tempImage);

        try {
            DerivateTools.uploadFile(Files.newInputStream(editedTempImage), Files.size(editedTempImage), derivate.getOwnerID().toString(), derivate.getId().toString(), editedTempImage.getFileName().toString());
        } finally {
            if (filename.equals(mainDoc)) {
                derivate.getDerivate().getInternals().setMainDoc(filename);
                derivate.getDerivate().getInternals().setIFSID(ifsid);
            }

            try {
                Files.delete(editedTempImage);
            } catch (IOException ex) {
                LOGGER.warn("Could not delete " + editedTempImage.getFileName());
            } finally {
                try {
                    Files.delete(tempDir);
                } catch (IOException ex) {
                    LOGGER.warn("Could not delete " + tempDir.getFileName());
                }
            }
        }
    }

    public static boolean isLibTiffInstalled() {
        try {
            new ProcessBuilder("tiffinfo").start();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
