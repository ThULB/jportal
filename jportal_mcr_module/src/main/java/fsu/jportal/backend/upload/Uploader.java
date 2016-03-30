package fsu.jportal.backend.upload;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.niofs.MCRPath;

/**
 * Simple uploader. Just adds files to derivates. Requires a valid
 * database transaction.
 * 
 * TODO: move this class to mycore
 * 
 * @author Matthias Eichner
 */
public class Uploader {

    private MCRDerivate derivate;

    public Uploader(MCRDerivate targetDerivate) {
        this.derivate = targetDerivate;
    }

    /**
     * Gets the root directory and creates it if it does not exist.
     * 
     * @throws FileSystemException the root directory couldn't be created
     */
    private MCRPath getOrCreateRootDir() throws FileSystemException {
        MCRPath rootDir = MCRPath.getPath(getDerivateIdAsString(), "/");
        if (Files.notExists(rootDir)) {
            rootDir.getFileSystem().createRoot(getDerivateIdAsString());
        }
        return rootDir;
    }

    /**
     * Uploads the given input stream and creates a file on the targetPath.
     * This method checks if the expected file size is equal the uploaded
     * file size. If the file size differs the file will not be stored.
     * If the expectedFileSize is null, the check is ignored.
     * 
     * @param in the input stream to upload
     * @param targetPath where to store
     * @param expectedFileSize in bytes
     * @throws IOException something went wrong or the file size differs
     * @return the path to the uploaded file
     */
    public MCRPath upload(InputStream in, String targetPath, Integer expectedFileSize) throws IOException {
        MCRPath path = this.upload(in, targetPath);
        if(expectedFileSize == null) {
            return path;
        }
        long uploadedFileSize = Files.size(path);
        if (uploadedFileSize != expectedFileSize) {
            Files.delete(path);
            throw new IOException("Length of transmitted data does not match promised length: " + expectedFileSize
                + "!=" + uploadedFileSize);
        }
        return path;
    }

    /**
     * Uploads the given input stream and creates a file on the targetPath.
     * 
     * @param in the input stream to upload
     * @param targetPath where to store
     * @throws IOException something went wrong
     * @return the path to the uploaded file
     */
    public MCRPath upload(InputStream in, String targetPath) throws IOException {
        MCRPath path = getPath(targetPath);
        Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
        return path;
    }

    private String getDerivateIdAsString() {
        return this.derivate.getId().toString();
    }

    /**
     * Returns the <code>MCRPath</code> to the given path.
     * Does create folder structure too.
     * 
     * @param path path as string
     * @return the MCRPath
     * @throws IOException when folder creation fails
     */
    private MCRPath getPath(String path) throws IOException {
        MCRPath pathToFile = MCRPath.toMCRPath(getOrCreateRootDir().resolve(path));
        MCRPath parentDirectory = pathToFile.getParent();
        if (!Files.isDirectory(parentDirectory)) {
            Files.createDirectories(parentDirectory);
        }
        return pathToFile;
    }

}
