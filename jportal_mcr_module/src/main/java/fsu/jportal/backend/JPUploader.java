package fsu.jportal.backend;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.access.MCRAccessException;
import org.mycore.common.MCRException;
import org.mycore.common.MCRPersistenceException;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.fileupload.MCRUploadHandlerIFS;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

/**
 * Helper class to upload in a bundled MCRUploadHandlerIFS session.
 * 
 * @author Matthias Eichner
 */
public abstract class JPUploader {

    private static Logger LOGGER = LogManager.getLogger();

    private static Cache<UUID, MCRUploadHandlerIFS> CACHE;

    static {
        CACHE = CacheBuilder.newBuilder()
                            .expireAfterAccess(60, TimeUnit.MINUTES)
                            .removalListener(new RemovalListener<UUID, MCRUploadHandlerIFS>() {
                                @Override
                                public void onRemoval(RemovalNotification<UUID, MCRUploadHandlerIFS> notification) {
                                    MCRUploadHandlerIFS uploadHandler = notification.getValue();
                                    try {
                                        uploadHandler.finishUpload();
                                    } catch (IOException exc) {
                                        LOGGER.error("Unable to finish upload for " + uploadHandler.getDerivateID(),
                                            exc);
                                    } finally {
                                        uploadHandler.unregister();
                                    }
                                }
                            })
                            .build();
    }

    /**
     * Returns the upload handler assigned to the given id.
     * 
     * @param uuid unique identifier for this upload process
     * @return the upload handler or null
     */
    public static MCRUploadHandlerIFS get(UUID uuid) {
        return CACHE.getIfPresent(uuid);
    }

    /**
     * Starts the upload process and returns a UUID for this.
     * 
     * @param documentID the document id where the files are added
     * @param derivateID the derivate id, if null, a new one will be created
     * @param numFiles the amount of files which should be uploaded
     * @return a unique identifier for this upload process
     */
    public static UUID start(String documentID, String derivateID, int numFiles) {
        if (derivateID == null) {
            String projectID = MCRConfiguration.instance().getString("MCR.SWF.Project.ID", "MCR");
            derivateID = MCRObjectID.getNextFreeId(projectID + '_' + "derivate").toString();
        }
        UUID uuid = UUID.randomUUID();
        MCRUploadHandlerIFS uploadHandler = new MCRUploadHandlerIFS(documentID, derivateID);
        uploadHandler.startUpload(numFiles);
        CACHE.put(uuid, uploadHandler);
        return uuid;
    }

    /**
     * Call this if your upload is finished.
     * 
     * @param uuid the unique identifier of the upload process
     * @throws IOException 
     */
    public static void finish(UUID uuid) throws IOException {
        CACHE.invalidate(uuid);
    }

    /**
     * Uploads a single file.
     * 
     * @param uuid the unique identifier of the upload process
     * @param filePath path of the file
     * @param inputStream file content
     * @param fileSize size of the file in bytes
     * @throws IOException 
     * @throws MCRAccessException 
     * @throws MCRPersistenceException 
     */
    public static void upload(UUID uuid, String filePath, InputStream inputStream, long fileSize)
        throws MCRPersistenceException, MCRAccessException, IOException {
        MCRUploadHandlerIFS uploadHandler = CACHE.getIfPresent(uuid);
        if (uploadHandler == null) {
            throw new MCRException("Couldn't find cache entry " + uuid + ". Unable to upload " + filePath + ".");
        }
        uploadHandler.receiveFile(filePath, inputStream, fileSize, null);
    }

}
