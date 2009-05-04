/*
 * $Revision: 14954 $ 
 * $Date: 2009-03-18 16:38:08 +0100 (Mi, 18. Mär 2009) $
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
import org.mycore.common.MCRConfiguration;
import org.mycore.common.MCRException;

/**
 * Stores XML metadata documents (or optionally any other BLOB data) in a
 * persistent filesystem structure
 * 
 * For each object type, a store must be defined as follows:
 * 
 * MCR.IFS2.Store.DocPortal_document.Class=org.mycore.datamodel.ifs2.MCRMetadataStore 
 * MCR.IFS2.Store.DocPortal_document.BaseDir=/foo/bar
 * MCR.IFS2.Store.DocPortal_document.SlotLayout=4-2-2 MCR.IFS2.
 * MCR.IFS2.Store.DocPortal_document.ForceXML=true (which is default)
 * 
 * @author Frank L�tzenkirchen
 */
public class MCRMetadataStore extends MCRStore {

    /**
     * If true (which is default), store will enforce it gets
     * XML to store, otherwise any binary content can be stored here.
     * 
     * Override with MCR.IFS2.Store.<ObjectType>.ForceXML=true|false
     */
    protected boolean forceXML = true;
    
    /**
     * Returns the store for the given metadata document type
     * 
     * @param type
     *            the type of metadata to store
     * @return the store for this metadata type
     */
    public static MCRMetadataStore getStore(String type) {
        return (MCRMetadataStore) (MCRStore.getStore(type));
    }

    /**
     * Initializes a new metadata store instance.
     * 
     * @param type
     *            the document type that is stored in this store
     */
    protected void init(String type) {
        super.init(type);
        this.prefix = type + "_";
        this.suffix = ".xml";
        this.forceXML = MCRConfiguration.instance().getBoolean("MCR.IFS2.Store." + type + ".ForceXML", true);
    }
    
    protected boolean shouldForceXML() {
        return forceXML;
    }

    /**
     * Stores a newly created document, using the next free ID.
     * 
     * @param xml
     *            the XML document to be stored
     * @return the stored metadata object
     */
    public MCRStoredMetadata create(MCRContent xml) throws Exception {
        int id = getNextFreeID();
        return create(xml, id);
    }

    /**
     * Stores a newly created document under the given ID.
     * 
     * @param xml
     *            the XML document to be stored
     * @param id
     *            the ID under which the document should be stored
     * @return the stored metadata object
     */
    public MCRStoredMetadata create(MCRContent xml, int id) throws Exception {
        FileObject fo = getSlot(id);
        if (fo.exists()) {
            String msg = "Metadata object with ID " + id + " already exists in store";
            throw new MCRException(msg);
        }
        fo.createFile();
        MCRStoredMetadata meta = buildMetadataObject(fo, id);
        meta.create(xml);
        return meta;
    }

    /**
     * Returns the metadata stored under the given ID, or null
     * 
     * @param id
     *            the ID of the XML document
     * @return the metadata stored under that ID, or null when there is no such
     *         metadata object
     */
    public MCRStoredMetadata retrieve(int id) throws Exception {
        FileObject fo = getSlot(id);
        if (!fo.exists())
            return null;
        else
            return buildMetadataObject(fo, id);
    }

    /**
     * Builds a new stored metadata object in this store
     * 
     * @param fo
     *            the FileObject that stores the data
     * @param id
     *            the ID of the metadata object
     */
    protected MCRStoredMetadata buildMetadataObject(FileObject fo, int id) {
        return new MCRStoredMetadata(this, fo, id);
    }
}
