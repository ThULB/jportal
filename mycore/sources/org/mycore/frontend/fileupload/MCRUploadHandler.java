/*
 * $Revision: 12994 $ $Date: 2008-01-25 11:38:13 +0100 (Fr, 25 Jan 2008) $
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

package org.mycore.frontend.fileupload;

import java.io.InputStream;

import org.apache.log4j.Logger;
import org.mycore.common.MCRException;
import org.mycore.frontend.MCRWebsiteWriteProtection;

/**
 * This class does the server-side of uploading files from a client browser,
 * which runs the upload applet. This is an abstract base class that must be
 * subclassed to implement the storage of files at the server side for miless,
 * MyCoRe or other usages of the upload framework. Every instance of
 * MCRUploadHandler handles one singe upload session with the applet.
 * 
 * @author Harald Richter
 * @author Frank L�tzenkirchen
 * 
 * @version $Revision: 12994 $ $Date: 2008-01-25 11:38:13 +0100 (Fr, 25 Jan 2008) $
 * 
 * @see MCRUploadHandlerManager
 */
public abstract class MCRUploadHandler {
    /** The logger * */
    protected Logger logger = Logger.getLogger(MCRUploadHandler.class);

    /** The unique ID of this upload session * */
    protected String uploadID;

    /** The url where to go after upload is finished. * */
    protected String url;

    /** Creates a new upload handler and registers it at the handler manager * */
    protected MCRUploadHandler() {
        if(MCRWebsiteWriteProtection.isActive())
            throw new MCRException("System is currently in read-only mode");

        this.uploadID = Long.toString(System.currentTimeMillis(), 36);
        MCRUploadHandlerManager.register(this);
    }

    /** Returns the unique ID of this upload session * */
    public final String getID() {
        return uploadID;
    }

    /** Returns the url where to go after upload is finished * */
    public String getRedirectURL() {
        return url;
    }

    /**
     * Starts the upload session.
     * 
     * @param numFiles
     *            the number of files that the applet will upload
     */
    public abstract void startUpload(int numFiles) throws Exception;

    /**
     * Before the applet sends each file, this method is called to ask if this
     * file should be uploaded and will be accepted by the server. The default
     * implementation always returns true (always upload file), but subclasses
     * should overwrite this method to decide whether the file's content must be
     * uploaded. Decision can be based on the MD5 checksum that the applet
     * calculated on the client side, so unchanged files do not have to be
     * uploaded again.
     * 
     * @param path
     *            the path and filename of the file
     * @param checksum
     *            the MD5 checksum computed at the client applet side
     * @param length 
     *            the length of the file in bytes (file size)
     * @return true, if the file should be uploaded, false if the file should be
     *         skipped
     * @throws Exception
     */
    public boolean acceptFile(String path, String checksum, long length) throws Exception {
        return true;
    }

    /**
     * When the applet uploads a file, this method is called so that the
     * UploadHandler subclass can store the file on the server side.
     * When the UploadHandler could read less than length bytes from the
     * InputStream at the time the InputStream has no data any more, the user
     * at the remote side canceled upload during file transfer. The UploadHandler
     * then can decide to delete the file, but must return the number of 
     * bytes stored. The UploadHandler can also compare the MD5 checksum calculated
     * at the client side with its own checksum, to detect magical transfer errors.
     * 
     * @param path
     *            the path and filename of the file
     * @param in
     *            the inputstream to read the content of the file from
     * @param length
     *            the total file size as number of bytes. This may be 0,
     *            meaning that the file is empty or the file size is not known.
     * @param md5
     *            the md5 checksum calculated at the client applet side. This
     *            may be null, meaning that the md5 checksum is not known.
     * @return 
     *            the number of bytes that have been stored.
     * @throws Exception
     */
    public abstract long receiveFile(String path, InputStream in, long length, String md5) throws Exception;

    /**
     * When the applet finished uploading all files, this method is called so
     * that the UploadHandler subclass can finish work and commit all saved
     * files.
     * 
     * @throws Exception
     */
    public abstract void finishUpload() throws Exception;

    /**
     * After the remote user canceled the upload process in the applet, 
     * this method is called so that the UploadHandler subclass can finish 
     * or cancel work. The implementation is optional, by default finishUpload()
     * is called 
     * 
     * @throws Exception
     */
    public void cancelUpload() throws Exception
    {
      finishUpload();
    }

    /**
     * When the applet is closed after uploading all files, the servlet calls
     * this method automatically to unregister this upload handler from the
     * UploadHandlerManager.
     */
    public void unregister() {
        MCRUploadHandlerManager.unregister(this.uploadID);
    }
}
