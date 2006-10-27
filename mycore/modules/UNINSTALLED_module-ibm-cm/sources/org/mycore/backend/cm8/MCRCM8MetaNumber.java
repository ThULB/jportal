/*
 * $RCSfile: MCRCM8MetaNumber.java,v $
 * $Revision: 1.9 $ $Date: 2006/01/26 14:32:41 $
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

package org.mycore.backend.cm8;

import org.mycore.common.MCRPersistenceException;
import org.mycore.datamodel.metadata.MCRMetaNumber;

import com.ibm.mm.sdk.common.DKAttrDefICM;
import com.ibm.mm.sdk.common.DKComponentTypeDefICM;
import com.ibm.mm.sdk.common.DKConstantICM;
import com.ibm.mm.sdk.common.DKDatastoreDefICM;
import com.ibm.mm.sdk.common.DKTextIndexDefICM;
import com.ibm.mm.sdk.server.DKDatastoreICM;

/**
 * This class implements the interface for the CM8 persistence layer for the
 * data model type MetaNumber.
 * 
 * @author Jens Kupferschmidt
 * @version $Revision: 1.9 $ $Date: 2006/01/26 14:32:41 $
 */
public class MCRCM8MetaNumber implements DKConstantICM, MCRCM8MetaInterface {
    /**
     * This method create a DKComponentTypeDefICM to create a complete ItemType
     * from the configuration.
     * 
     * @param element
     *            a MCR datamodel element as JDOM Element
     * @param connection
     *            the connection to the CM8 datastore
     * @param dsDefICM
     *            the datastore definition
     * @param prefix
     *            the prefix name for the item type
     * @param textindex
     *            the definition of the text search index
     * @param textserach
     *            the flag to use textsearch as string (this value has no effect
     *            for this class)
     * @return a DKComponentTypeDefICM for the MCR datamodel element
     * @exception MCRPersistenceException
     *                general Exception of MyCoRe
     */
    public DKComponentTypeDefICM createItemType(org.jdom.Element element, DKDatastoreICM connection, DKDatastoreDefICM dsDefICM, String prefix, DKTextIndexDefICM textindex, String textsearch) throws MCRPersistenceException {
        String subtagname = prefix + element.getAttribute("name").getValue();
        String dimname = prefix + "dimension";
        String measname = prefix + "measurement";

        DKComponentTypeDefICM lt = new DKComponentTypeDefICM(connection);

        try {
            // create component child
            lt.setName(subtagname);
            lt.setDeleteRule(DK_ICM_DELETE_RULE_CASCADE);

            // add lang attribute
            DKAttrDefICM attr = (DKAttrDefICM) dsDefICM.retrieveAttr(prefix + "lang");
            attr.setNullable(true);
            attr.setUnique(false);
            lt.addAttr(attr);

            // add type attribute
            attr.setNullable(true);
            attr.setUnique(false);
            attr = (DKAttrDefICM) dsDefICM.retrieveAttr(prefix + "type");
            lt.addAttr(attr);

            // create the dimension attribute for the data content
            MCRCM8ItemTypeCommon.createAttributeVarChar(connection, dimname, MCRMetaNumber.MAX_DIMENSION_LENGTH, false);

            // add the value attribute
            attr = (DKAttrDefICM) dsDefICM.retrieveAttr(dimname);
            attr.setNullable(true);
            attr.setUnique(false);
            lt.addAttr(attr);

            // create the measurement attribute for the data content
            MCRCM8ItemTypeCommon.createAttributeVarChar(connection, measname, MCRMetaNumber.MAX_MEASUREMENT_LENGTH, false);

            // add the value attribute
            attr = (DKAttrDefICM) dsDefICM.retrieveAttr(measname);
            attr.setNullable(true);
            attr.setUnique(false);
            lt.addAttr(attr);

            // create the attribute for the data content in date form
            MCRCM8ItemTypeCommon.createAttributeDouble(connection, subtagname);

            // add the value attribute
            attr = (DKAttrDefICM) dsDefICM.retrieveAttr(subtagname);
            attr.setNullable(true);
            attr.setUnique(false);
            lt.addAttr(attr);
        } catch (Exception e) {
            throw new MCRPersistenceException(e.getMessage(), e);
        }

        return lt;
    }
}
