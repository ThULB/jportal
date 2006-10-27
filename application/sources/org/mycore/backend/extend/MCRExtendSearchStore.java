/*
 * $RCSfile: MCRExtendSearchStore.java,v $
 * $Revision: 1.5 $ $Date: 2005/09/28 08:00:29 $
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

package org.mycore.backend.extend;

import org.apache.log4j.Logger;
import org.mycore.common.MCRConfiguration;
import org.mycore.common.MCRConfigurationException;
import org.mycore.common.MCRException;
import org.mycore.common.MCRPersistenceException;
import org.mycore.datamodel.metadata.MCRBase;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.metadata.MCRObjectSearchStoreInterface;

/**
 * This class is the persistence layer for XML:DB databases.
 * 
 * @author Jens Kupferschmidt
 * 
 * @version $Revision: 1.5 $ $Date: 2005/09/28 08:00:29 $
 */
public final class MCRExtendSearchStore implements MCRObjectSearchStoreInterface {
    static final private Logger logger = Logger.getLogger(MCRExtendSearchStore.class.getName());

    static final private MCRConfiguration config = MCRConfiguration.instance();

    static private String defaultclassname = null;

    static private MCRObjectSearchStoreInterface defaultclass = null;

    /**
     * Creates a new MCRExtendSearchStore.
     */
    public MCRExtendSearchStore() throws MCRPersistenceException {
        String storetype = config.getString("MCR.XMLStore.Type", "jdom");
        defaultclassname = config.getString("MCR.persistence_" + storetype + "_default_name");

        try {
            defaultclass = (MCRObjectSearchStoreInterface) Class.forName(defaultclassname).newInstance();
        } catch (Exception e) {
            throw new MCRException(e.getMessage(), e);
        }
    }

    /**
     * This method creates and stores the searchable data from MCRObject in the
     * extended search store.
     * 
     * @param obj
     *            the MCRObject to put in the search store
     * @exception MCRPersistenceException
     *                if an error was occured
     */
    public final void create(MCRBase obj) throws MCRPersistenceException {
        // only metadata of MCRObject should be extended
        if (obj instanceof MCRObject) {
            // select the MCRObject type that should be extended
            if (obj.getId().getTypeId().equals("document")) {
                logger.debug("Create the extended search in the store.");

                // now we use the instance of the metadate element and do
                // something with them
            }
        }

        // call the default search store
        defaultclass.create(obj);
    }

    /**
     * Updates the searchable content in the database. Currently this is the
     * same like delete and then a new create. Should be made with XUpdate in
     * the future.
     * 
     * @param obj
     *            the MCRObject to put in the search store
     * @exception MCRPersistenceException
     *                if an error was occured
     */
    public void update(MCRBase obj) throws MCRPersistenceException {
        // only metadata of MCRObject should be extended
        if (obj instanceof MCRObject) {
            // select the MCRObject type that should be extended
            if (obj.getId().getTypeId().equals("document")) {
                logger.debug("Update the extended search in the store.");

                // now we use the instance of the metadate element and do
                // something with them
            }
        }

        // call the default search store
        defaultclass.update(obj);
    }

    /**
     * Deletes the object with the given object id in the datastore.
     * 
     * @param mcr_id
     *            id of the object to delete
     * 
     * @throws MCRPersistenceException
     *             something goes wrong during delete
     */
    public void delete(MCRObjectID mcr_id) throws MCRPersistenceException {
        // only metadata of MCRObject should be extended
        // look for the MCRObjectID in the extended search store and delete the
        // data
        logger.debug("Delete the extended search in the store.");

        // call the default search store
        defaultclass.delete(mcr_id);
    }
}
