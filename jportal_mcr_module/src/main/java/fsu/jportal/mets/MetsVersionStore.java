package fsu.jportal.mets;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.datamodel.niofs.MCRPath;

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
     * @throws FileNotFoundException the mets.xml does not exists in the derivate
     */
    public static synchronized void store(String derivateId) throws IOException, FileNotFoundException {
        MCRPath metsPath = MCRPath.getPath(derivateId, "mets.xml");
        if (!Files.exists(metsPath)) {
            throw new FileNotFoundException("mets.xml does not exists in derivate " + derivateId);
        }
        Path derivatePath = STORE_PATH.resolve(derivateId);
        if (!Files.exists(derivatePath)) {
            Files.createDirectories(derivatePath);
        }
        long count = Files.list(derivatePath).count();
        Path newMetsPath = derivatePath.resolve("mets." + count + ".xml");
        LOGGER.info("Saving mets.xml to " + newMetsPath.toAbsolutePath().toString());
        Files.copy(metsPath, newMetsPath, StandardCopyOption.REPLACE_EXISTING);
    }

}
