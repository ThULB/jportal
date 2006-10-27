/*
 * $RCSfile: MCRObjectSearchStoreInterface.java,v $
 * $Revision: 1.4 $ $Date: 2005/09/28 07:40:25 $
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

package org.mycore.datamodel.metadata;

import org.mycore.common.MCRConfigurationException;
import org.mycore.common.MCRPersistenceException;

/**
 * This interface is designed to choose the search store for the project.
 * 
 * @author Jens Kupferschmidt
 * @version $Revision: 1.4 $ $Date: 2005/09/28 07:40:25 $
 */
public interface MCRObjectSearchStoreInterface {

    /**
     * The methode create a object in the search stores.
     * 
     * @param obj
     *            the MCRBase to put in the search stores
     * @exception MCRConfigurationException
     *                if the configuration is not correct
     * @exception MCRPersistenceException
     *                if a persistence problem is occured
     */
    public void create(MCRBase obj) throws MCRConfigurationException, MCRPersistenceException;

    /**
     * The methode delete a object from the search store.
     * 
     * @param mcr_id
     *            the MyCoRe object ID
     * @exception MCRConfigurationException
     *                if the configuration is not correct
     * @exception MCRPersistenceException
     *                if a persistence problem is occured
     */
    public void delete(MCRObjectID mcr_id) throws MCRConfigurationException, MCRPersistenceException;

    /**
     * The methode update a object in the search store.
     * 
     * @param obj
     *            the MCRObject to put in the search stores
     * @exception MCRConfigurationException
     *                if the configuration is not correct
     * @exception MCRPersistenceException
     *                if a persistence problem is occured
     */
    public void update(MCRBase obj) throws MCRConfigurationException, MCRPersistenceException;
}
