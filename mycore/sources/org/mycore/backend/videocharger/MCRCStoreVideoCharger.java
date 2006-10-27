/*
 * $RCSfile: MCRCStoreVideoCharger.java,v $
 * $Revision: 1.8 $ $Date: 2006/07/04 12:45:31 $
 *
 * This file is part of ***  M y C o R e  ***
 * See http://www.mycore.de/ for details.
 *
 * This program is free software; you can use it, redistribute it
 * and / or modify it under the terms of the GNU General Public License
 * (GPL) as published by the Free Software Foundation; either version 2
 * of the License or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program, in a file called gpl.txt or license.txt.
 * If not, write to the Free Software Foundation Inc.,
 * 59 Temple Place - Suite 330, Boston, MA  02111-1307 USA
 */

package org.mycore.backend.videocharger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;
import org.mycore.common.MCRConfiguration;
import org.mycore.common.MCRPersistenceException;
import org.mycore.datamodel.ifs.MCRContentInputStream;
import org.mycore.datamodel.ifs.MCRContentStore;
import org.mycore.datamodel.ifs.MCRFileReader;

import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPTransferType;

/**
 * This class implements the MCRContentStore interface to store the content of
 * MCRFile objects in IBM VideoCharger Server. This allows the content to be
 * streamed. This implementation uses FTP to manage the files in VideoCharger.
 * The FTP connection parameters are configured in mycore.properties:
 * 
 * <code>
 *   MCR.IFS.ContentStore.<StoreID>.Hostname   Hostname of VideoCharger Server
 *   MCR.IFS.ContentStore.<StoreID>.FTPPort    Port of VideoCharger FTP interface, default is 4324
 *   MCR.IFS.ContentStore.<StoreID>.UserID     User ID for FTP connections, e. g. vsloader
 *   MCR.IFS.ContentStore.<StoreID>.Password   Password for this user
 *   MCR.IFS.ContentStore.<StoreID>.DebugFTP   If true, FTP debug messages are written to stdout, default is false
 *   MCR.IFS.ContentStore.<StoreID>.AssetGroup Asset group, default is 'AG'
 * </code>
 * 
 * This class also provides a method to backup all assets stored in VideoCharger
 * to a directory.
 * 
 * @author Frank L�tzenkirchen
 * @version $Revision: 1.8 $ $Date: 2006/07/04 12:45:31 $
 * 
 * @see MCRAVExtVideoCharger
 */
public class MCRCStoreVideoCharger extends MCRContentStore {
    private static Logger logger = Logger.getLogger(MCRCStoreVideoCharger.class.getName());

    /** Hostname of VideoCharger server */
    protected String host;

    /** Port of VideoCharger server FTP interface */
    protected int port;

    /** User ID for FTP login */
    protected String user;

    /** Password for FTP login */
    protected String password;

    /** If true, FTP debug messages are written to stdout */
    protected boolean debugFTP;
    
    /** Asset group **/
    protected String assetGroup;

    /** FTP Return code if "quote site avs attr" is successful */
    protected final static String[] ok = { "200" };

    public void init(String storeID) {
        super.init(storeID);

        MCRConfiguration config = MCRConfiguration.instance();

        host = config.getString(prefix + "Hostname");
        port = config.getInt(prefix + "FTPPort", 4324);
        user = config.getString(prefix + "UserID");
        password = config.getString(prefix + "Password");
        debugFTP = config.getBoolean(prefix + "DebugFTP", false);
        assetGroup = config.getString(prefix + "AssetGroup", "AG" );
    }

    protected String doStoreContent(MCRFileReader file, MCRContentInputStream source) throws Exception {
        String storageID = buildNextID(file);

        FTPClient connection = connect();

        try {
            connection.quote("site avs attr assetgroup=" + assetGroup, ok);
            connection.quote("site avs attr title=" + storageID, ok);
            connection.put(source, storageID);

            return storageID;
        } finally {
            disconnect(connection);
        }
    }

    protected void doDeleteContent(String storageID) throws Exception {
        FTPClient connection = connect();

        try {
            connection.quote("site avs attr assetgroup=" + assetGroup, ok);
            connection.delete(storageID);
        } finally {
            disconnect(connection);
        }
    }

    protected void doRetrieveContent(MCRFileReader file, OutputStream target) throws Exception {
        retrieveContent(file.getStorageID(), target);
    }

    protected InputStream doRetrieveContent(MCRFileReader file) throws Exception {
        //FTPClient does not provide GET and InputStreams, we need to copy
        ByteArrayOutputStream bout=new ByteArrayOutputStream();
        doRetrieveContent(file, bout);
        bout.close();
        return new ByteArrayInputStream(bout.toByteArray());
    }

    protected void retrieveContent(String assetID, OutputStream target) throws Exception {
        FTPClient connection = connect();

        try {
            connection.quote("site avs attr assetgroup=" + assetGroup, ok);
            connection.get(target, assetID);
        } finally {
            disconnect(connection);
        }
    }

    /**
     * Reads all assets stored in VideoCharger server and writes the contents to
     * a directory for backup. If the directory already contains an asset with
     * the same name, that assets is skipped and not backed up.
     * 
     * @param storeID
     *            the store ID fo the VideoCharger store to be backed up
     * @param directory
     *            the local directory to write the assets to
     */
    public static void backupContentTo(String storeID, String directory) throws MCRPersistenceException, Exception {
        MCRAVExtVideoCharger extender = new MCRAVExtVideoCharger();
        extender.readConfig(storeID);
        MCRCStoreVideoCharger store = new MCRCStoreVideoCharger();
        store.init( storeID );

        String[] list = extender.listAssets();

        for (int i = 0; i < list.length; i++) {
            logger.info("Backup of asset with ID = " + list[i]);

            File local = new File(directory, list[i]);

            if (local.exists()) {
                continue;
            }

            FileOutputStream target = new FileOutputStream(local);
            store.retrieveContent(list[i], target);
            target.close();
        }
    }

    /**
     * Connects to IBM VideoCharger Server via FTP
     */
    protected FTPClient connect() throws MCRPersistenceException {
        try {
            FTPClient connection = new FTPClient(host, port);
            connection.debugResponses(debugFTP);
            connection.login(user, password);
            connection.setType(FTPTransferType.BINARY);

            return connection;
        } catch (Exception exc) {
            String msg = "Could not connect to " + host + ":" + port + " via FTP";
            throw new MCRPersistenceException(msg, exc);
        }
    }

    /**
     * Closes the FTP connection to VideoCharger server
     * 
     * @param connection
     *            the FTP connection to close
     */
    protected void disconnect(FTPClient connection) {
        try {
            connection.quit();
        } catch (Exception ignored) {
        }
    }
}
