package fsu.jportal.backend.store;

import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRFileContent;
import org.mycore.datamodel.ifs.MCRContentInputStream;
import org.mycore.datamodel.ifs.MCRFile;
import org.mycore.datamodel.ifs2.MCRCStoreIFS2;

import java.io.File;
import java.io.IOException;

public class FSStore extends MCRCStoreIFS2 {

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

//    @Override
//    protected boolean exists(MCRFile mcrFile) {
//        return false;
//    }
//
//    @Override
//    public File getBaseDir() throws IOException {
//        return null;
//    }

    @Override
    protected String doStoreContent(MCRFile mcrFile, MCRContentInputStream mcrContentInputStream) throws Exception {
        throw new Exception("This store with store ID " + mcrFile.getStoreID() + "do not support write operation!" );
    }

    @Override
    protected void doDeleteContent(String storageId) throws Exception {
        boolean deleted = getLocalFile(storageId).delete();
        if (!deleted) {
            throw new Exception("Could not delete content with storage ID " + storageId);
        }
    }
}
