package fsu.jportal.mets;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.niofs.MCRPath;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is just a simple helper store to version mets.xml
 * files because we don't use ifs2 for files.
 * 
 * @author Matthias Eichner
 */
public class MetsVersionStore {

    static Logger LOGGER = LogManager.getLogger(MetsVersionStore.class);

    private static Path STORE_PATH;

    static {
        String mcrDataDir = MCRConfiguration.instance().getString("MCR.datadir");
        STORE_PATH = Paths.get(mcrDataDir, "metsStore");
    }

    /**
     * Stores the mets.xml of the given derivate to the store.
     * 
     * @param derivateId the derivate id
     * @throws IOException some storing or reading went wrong
     */
    public static synchronized void store(MCRObjectID derivateId) throws IOException {
        String id = derivateId.toString();
        MCRPath metsPath = MCRPath.getPath(id, "mets.xml");
        if (!Files.exists(metsPath)) {
            throw new FileNotFoundException("mets.xml does not exists in derivate " + derivateId);
        }
        Path derivatePath = STORE_PATH.resolve(id);
        if (!Files.exists(derivatePath)) {
            Files.createDirectories(derivatePath);
        }
        long count = Files.list(derivatePath).count();
        Path newMetsPath = derivatePath.resolve("mets." + count + ".xml");
        LOGGER.info("Saving mets.xml to " + newMetsPath.toAbsolutePath().toString());
        Files.copy(metsPath, newMetsPath, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Gets the path to stored version of the mets.xml for the given derivate.
     *
     * @param derivateId the derivate identifier
     * @param version the version number
     * @return the path to the file
     * @throws FileNotFoundException there is no mets.xml for this derivate and this version
     */
    public static synchronized Path get(MCRObjectID derivateId, int version) throws FileNotFoundException {
        Path metsToRestorePath = STORE_PATH.resolve(derivateId.toString()).resolve("mets." + version + ".xml");
        if (!Files.exists(metsToRestorePath)) {
            throw new FileNotFoundException(
                    "Unable to locate " + metsToRestorePath.toAbsolutePath().toString() + ". Cannot restore!");
        }
        return metsToRestorePath;
    }

    /**
     * Lists all versions of the given derivate.
     *
     * @param derivateId the derivate to list
     * @return list list of versions
     * @throws IOException something went wrong
     */
    public static synchronized List<Integer> list(MCRObjectID derivateId) throws IOException {
        return Files.list(STORE_PATH.resolve(derivateId.toString())).map(Path::getFileName).map(Path::toString)
                    .map(fileName -> fileName.substring(0, fileName.length() - 4).substring(5)).map(Integer::valueOf)
                    .sorted().collect(Collectors.toList());
    }

    /**
     * Copies the mets.xml from the mets store to the derivate.
     *
     * @param derivateId the derivate to restore
     * @param version the restore version
     * @throws IOException something went wrong
     */
    public static synchronized void restore(MCRObjectID derivateId, int version) throws IOException {
        Path metsToRestorePath = get(derivateId, version);
        MCRPath derivateMetsPath = MCRPath.getPath(derivateId.toString(), "mets.xml");
        // store current mets.xml first (if ther is one)
        if (Files.exists(derivateMetsPath)) {
            store(derivateId);
        }
        // overwrite with old version
        Files.copy(metsToRestorePath, derivateMetsPath, StandardCopyOption.REPLACE_EXISTING);
    }

}
