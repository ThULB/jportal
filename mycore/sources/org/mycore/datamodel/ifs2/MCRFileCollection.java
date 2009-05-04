/*
 * $Revision: 15009 $ 
 * $Date: 2009-03-25 11:09:54 +0100 (Mi, 25. Mär 2009) $
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

package org.mycore.datamodel.ifs2;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.VFS;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;

/**
 * Represents a set of files and directories belonging together, that are stored
 * in a persistent MCRFileStore. A FileCollection has a unique ID within the
 * store, it is the root folder of all files and directories in the collection.
 * 
 * @author Frank L�tzenkirchen
 */
public class MCRFileCollection extends MCRDirectory {

    /**
     * The logger
     */
    private final static Logger LOGGER = Logger.getLogger(MCRFileCollection.class);

    /**
     * The store this file collection is stored in.
     */
    private MCRStore store;

    /**
     * The ID of this file collection
     */
    private int id;

    /**
     * Creates a new file collection in the given store, or retrieves an
     * existing one.
     * 
     * @see MCRFileStore
     * 
     * @param store
     *            the store this file collection is stored in
     * @param id
     *            the ID of this file collection
     */
    protected MCRFileCollection(MCRStore store, int id) throws Exception {
        super(null, store.getSlot(id), new Element("collection"));
        this.store = store;
        this.id = id;
        if (fo.exists())
            readAdditionalData();
        else {
            fo.createFolder();
            new Document(data);
            saveAdditionalData();
        }
    }

    private final static String dataFile = "mcrdata.xml";

    private void readAdditionalData() throws Exception {
        FileObject src = VFS.getManager().resolveFile(fo, dataFile);
        if (!src.exists()) {
            LOGGER.warn("Metadata file is missing, repairing metadata...");
            this.data = new Element("collection");
            new Document(data);
            repairMetadata();
        }
        data = MCRContent.readFrom(src).asXML().getRootElement();
    }

    protected void saveAdditionalData() throws Exception {
        FileObject target = VFS.getManager().resolveFile(fo, dataFile);
        MCRContent.readFrom(data.getDocument()).sendTo(target);
    }

    /**
     * Throws a exception, because a file collection's name is always the empty
     * string and therefore can not be renamed.
     */
    public void renameTo(String name) {
        throw new UnsupportedOperationException("File collections can not be renamed");
    }

    /**
     * Returns the store this file collection is stored in.
     * 
     * @return the store this file collection is stored in.
     */
    public MCRStore getStore() {
        return store;
    }

    /**
     * Returns the ID of this file collection
     * 
     * @return the ID of this file collection
     */
    public int getID() {
        return id;
    }

    /**
     * Returns this object, because the FileCollection instance is the root of
     * all files and directories contained in the collection.
     * 
     * @return this
     */
    public MCRFileCollection getRoot() {
        return this;
    }

    public int getNumChildren() throws Exception {
        return super.getNumChildren() - 1;
    }

    public MCRNode getChild(String name) throws Exception {
        if (dataFile.equals(name))
            return null;
        else
            return super.getChild(name);
    }

    public String getName() {
        return "";
    }

    /**
     * Repairs additional metadata stored for all files and directories in this
     * collection
     */
    public void repairMetadata() throws Exception {
        super.repairMetadata();
        data.setName("collection");
        data.removeAttribute("name");
        saveAdditionalData();
    }

    /**
     * Returns additional metadata stored for all files and directories in this
     * collection
     */
    Document getMetadata() throws Exception {
        return data.getDocument();
    }
}
