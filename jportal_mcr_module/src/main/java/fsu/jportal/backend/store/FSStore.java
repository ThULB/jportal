package fsu.jportal.backend.store;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRFileContent;
import org.mycore.datamodel.ifs.MCRContentInputStream;
import org.mycore.datamodel.ifs.MCRFile;
import org.mycore.datamodel.ifs2.MCRCStoreIFS2;

import java.io.File;
import java.io.IOException;

public class FSStore extends MCRCStoreIFS2 {
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    protected MCRContent doRetrieveMCRContent(MCRFile file) throws IOException {
        String storageID = file.getStorageID();
        File localFile = getLocalFile(storageID);
        return new MCRFileContent(localFile);
    }

    @Override
    public File getLocalFile(String storageId) throws IOException {
        if (storageId == null || storageId.isEmpty()) {
            throw new IOException("No storage id");
        }
        return getFile(storageId);
    }

    private File getFile(String storageID){
        try {
            return getBaseDir().toPath().resolve(storageID).toFile();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    protected String doStoreContent(MCRFile mcrFile, MCRContentInputStream mcrContentInputStream) throws Exception {
        throw new Exception("This store with store ID " + mcrFile.getStoreID() + "do not support write operation!" );
    }

    @Override
    protected void doDeleteContent(String storageId) throws Exception {
        File localFile = getLocalFile(storageId);
        if (localFile.exists()) {
            boolean deleted = localFile.delete();
            if (deleted) {
                LOGGER.info("Content  with storage ID {} deleted.", storageId);
            } else {
                LOGGER.warn("Could not delete content with storage ID {}.", storageId);
            }
        } else {
            LOGGER.warn("Content with storage ID {} does not exists.", storageId);
        }
    }
}
