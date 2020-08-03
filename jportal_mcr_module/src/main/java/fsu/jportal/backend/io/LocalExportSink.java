package fsu.jportal.backend.io;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;
import java.util.zip.Deflater;

import javax.imageio.ImageIO;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.imagetiler.MCRImage;
import org.mycore.imagetiler.MCRTiledPictureProps;
import org.mycore.iview2.services.MCRIView2Tools;

import fsu.jportal.backend.ImportDerivateObject;

public class LocalExportSink implements ImportSink {

    private static Logger LOGGER = LogManager.getLogger(LocalExportSink.class);
    private Path saveTo;

    public LocalExportSink(Path dest) {
        this.saveTo = dest;
    }

    @Override
    public void save(Document objXML) {
    }

    @Override
    public void saveClassification(Document classificationXML) {
    }

    @Override
    public void saveDerivate(ImportDerivateObject deriObj) {
        try {
            Document objectXml = MCRXMLMetadataManager.instance().retrieveXML(
                    MCRObjectID.getInstance(deriObj.getDocumentID()));
            String maintitle = objectXml.getRootElement().getChild("metadata").getChild("maintitles")
                    .getChildText("maintitle");

            Path destFolder = saveTo.resolve(maintitle);

            if (!Files.exists(destFolder)) {
                try {
                    Files.createDirectories(destFolder);
                } catch (IOException e) {
                    LOGGER.error("Unable to create Directory: " + e.getMessage());
                }
            }

            MCRPath derivateRoot = MCRPath.getPath(deriObj.getDerivateID(), "/");
            JPZipClone zous = new JPZipClone(derivateRoot, 4);
            OutputStream out = new FileOutputStream(destFolder.resolve(deriObj.getDerivateID()).toFile());
            zous.write(out);
        } catch (Exception e) {
            LOGGER.error("Failed to save Derivate: " + e.getMessage());
            LOGGER.error("derivate id: " + deriObj.getDerivateID() + " object ID: " + deriObj.getDocumentID());
            e.printStackTrace();
        }
    }

    @Override
    public void saveDerivateLinks() {
    }

    protected void saveDerivateLink(MCRObject obj) {
    }

    /*
     * copied from 
     * org.mycore.iview2.frontend.resources.MCRIViewZipResource.ZipStreamingOutput
     * commented out transaction start and end
     */
    private static class JPZipClone implements StreamingOutput {
        protected MCRPath derivateRoot;

        protected Integer zoom;

        public JPZipClone(MCRPath derivateRoot, Integer zoom) {
            this.derivateRoot = derivateRoot;
            this.zoom = zoom;
        }

        @Override
        public void write(OutputStream out) throws IOException, WebApplicationException {
            //            MCRSessionMgr.getCurrentSession().beginTransaction();
            try {
                final ZipArchiveOutputStream zipStream = new ZipArchiveOutputStream(new BufferedOutputStream(out));
                zipStream.setLevel(Deflater.BEST_SPEED);
                SimpleFileVisitor<java.nio.file.Path> zipper = new SimpleFileVisitor<java.nio.file.Path>() {
                    @Override
                    public FileVisitResult visitFile(java.nio.file.Path file, BasicFileAttributes attrs)
                            throws IOException {
                        Objects.requireNonNull(file);
                        Objects.requireNonNull(attrs);
                        MCRPath mcrPath = MCRPath.toMCRPath(file);
                        if (MCRIView2Tools.isFileSupported(file)) {
                            java.nio.file.Path iviewFile = MCRImage.getTiledFile(MCRIView2Tools.getTileDir(),
                                    mcrPath.getOwner(), mcrPath.subpathComplete().toString());
                            if (!Files.exists(iviewFile)) {
                                return super.visitFile(iviewFile, attrs);
                            }
                            try {
                                MCRTiledPictureProps imageProps = MCRTiledPictureProps.getInstanceFromFile(iviewFile);
                                Integer zoomLevel = (zoom == null || zoom > imageProps.getZoomlevel()) ? imageProps
                                        .getZoomlevel() : zoom;
                                BufferedImage image = MCRIView2Tools.getZoomLevel(iviewFile, zoomLevel);
                                ZipArchiveEntry entry = new ZipArchiveEntry(file.getFileName() + ".jpg");
                                zipStream.putArchiveEntry(entry);
                                ImageIO.write(image, "jpg", zipStream);
                            } catch (JDOMException e) {
                                throw new WebApplicationException(e);
                            }
                            zipStream.closeArchiveEntry();
                        }
                        return FileVisitResult.CONTINUE;
                    }
                };
                Files.walkFileTree(derivateRoot, zipper);
                zipStream.close();
            } catch (Exception exc) {
                exc.printStackTrace();
            } finally {
                //                MCRSessionMgr.getCurrentSession().commitTransaction();
            }
        }
    }
}
