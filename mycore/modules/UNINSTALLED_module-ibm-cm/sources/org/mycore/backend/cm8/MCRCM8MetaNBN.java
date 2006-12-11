/*
 * $RCSfile: MCRCM8MetaNBN.java,v $
 * $Revision: 1.1 $ $Date: 2006/07/12 08:59:08 $
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

import org.apache.log4j.Logger;
import org.mycore.common.MCRPersistenceException;
import org.mycore.datamodel.metadata.MCRMetaDefault;

import com.ibm.mm.sdk.common.DKAttrDefICM;
import com.ibm.mm.sdk.common.DKComponentTypeDefICM;
import com.ibm.mm.sdk.common.DKConstantICM;
import com.ibm.mm.sdk.common.DKDatastoreDefICM;
import com.ibm.mm.sdk.common.DKTextIndexDefICM;
import com.ibm.mm.sdk.server.DKDatastoreICM;

/**
 * This class implements the interface for the CM8 persistence layer for the
 * data model type MetaNBN.
 * 
 * @author Jens Kupferschmidt
 * @version $Revision: 1.1 $ $Date: 2006/07/12 08:59:08 $
 */
public class MCRCM8MetaNBN implements DKConstantICM, MCRCM8MetaInterface {
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
     *            the flag to use textsearch as string
     * @return a DKComponentTypeDefICM for the MCR datamodel element
     * @exception MCRPersistenceException
     *                general Exception of MyCoRe CM8
     */
    public DKComponentTypeDefICM createItemType(org.jdom.Element element, DKDatastoreICM connection, DKDatastoreDefICM dsDefICM, String prefix, DKTextIndexDefICM textindex, String textsearch) throws MCRPersistenceException {
        Logger logger = MCRCM8ConnectionPool.getLogger();
        String subtagname = prefix + element.getAttribute("name").getValue();

        // String length
        String subtaglen = element.getAttribute("length").getValue();
        int len = MCRMetaDefault.DEFAULT_STRING_LENGTH;

        try {
            len = Integer.parseInt(subtaglen);
        } catch (NumberFormatException e) {
            throw new MCRPersistenceException(e.getMessage(), e);
        }

        // Text search option
        boolean ts = false;

        try {
            if (textsearch.toLowerCase().equals("true")) {
                ts = true;
            }
        } catch (Exception e) {
        }

        logger.debug("Set TextSearch for " + subtagname + " to " + ts);

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
            attr = (DKAttrDefICM) dsDefICM.retrieveAttr(prefix + "type");
            attr.setNullable(true);
            attr.setUnique(false);
            lt.addAttr(attr);

            // create the attribute for the data content in string form
            // with given textsearch flag
            MCRCM8ItemTypeCommon.createAttributeVarChar(connection, subtagname, len, ts);

            // add the value attribute
            attr = (DKAttrDefICM) dsDefICM.retrieveAttr(subtagname);
            attr.setNullable(true);
            attr.setUnique(false);

            if (ts) {
                attr.setTextSearchable(true);
                attr.setTextIndexDef(textindex);
            }

            lt.addAttr(attr);
        } catch (Exception e) {
            throw new MCRPersistenceException(e.getMessage(), e);
        }

        return lt;
    }
}
