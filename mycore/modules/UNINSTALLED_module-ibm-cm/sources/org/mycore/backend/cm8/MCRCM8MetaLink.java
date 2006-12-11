/*
 * $RCSfile: MCRCM8MetaLink.java,v $
 * $Revision: 1.1 $ $Date: 2006/07/12 08:59:07 $
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

import com.ibm.mm.sdk.common.DKAttrDefICM;
import com.ibm.mm.sdk.common.DKComponentTypeDefICM;
import com.ibm.mm.sdk.common.DKConstantICM;
import com.ibm.mm.sdk.common.DKDatastoreDefICM;
import com.ibm.mm.sdk.common.DKTextIndexDefICM;
import com.ibm.mm.sdk.server.DKDatastoreICM;

/**
 * This class implements the interface for the CM8 persistence layer for the
 * data model type MetaLink.
 * 
 * @author Jens Kupferschmidt
 * @version $Revision: 1.1 $ $Date: 2006/07/12 08:59:07 $
 */

// DO NOT DELETE THIS. This class is needed for automatic class invocation!!
public class MCRCM8MetaLink implements DKConstantICM, MCRCM8MetaInterface {
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
     *            the flag to use textsearch as string (this is a dummy value)
     * @return a DKComponentTypeDefICM for the MCR datamodel element
     * @exception MCRPersistenceException
     *                general Exception of MyCoRe CM8
     */
    public DKComponentTypeDefICM createItemType(org.jdom.Element element, DKDatastoreICM connection, DKDatastoreDefICM dsDefICM, String prefix, DKTextIndexDefICM textindex, String textsearch) throws MCRPersistenceException {
        String subtagname = prefix + element.getAttribute("name").getValue();
        String typename = prefix + "xlinktype";
        String hrefname = prefix + "xlinkhref";
        String labelname = prefix + "xlinklabel";
        String titlename = prefix + "xlinktitle";
        String fromname = prefix + "xlinkfrom";
        String toname = prefix + "xlinkto";
        int typelen = org.mycore.datamodel.metadata.MCRMetaLink.MAX_XLINK_TYPE_LENGTH;
        int hreflen = org.mycore.datamodel.metadata.MCRMetaLink.MAX_XLINK_HREF_LENGTH;
        int labellen = org.mycore.datamodel.metadata.MCRMetaLink.MAX_XLINK_LABEL_LENGTH;
        int titlelen = org.mycore.datamodel.metadata.MCRMetaLink.MAX_XLINK_TITLE_LENGTH;
        int fromlen = org.mycore.datamodel.metadata.MCRMetaLink.MAX_XLINK_FROM_LENGTH;
        int tolen = org.mycore.datamodel.metadata.MCRMetaLink.MAX_XLINK_TO_LENGTH;

        DKComponentTypeDefICM lt = new DKComponentTypeDefICM(connection);

        try {
            // create component child
            lt.setName(subtagname);
            lt.setDeleteRule(DK_ICM_DELETE_RULE_CASCADE);

            DKAttrDefICM attr;

            // create the type attribute for the data content
            MCRCM8ItemTypeCommon.createAttributeVarChar(connection, typename, typelen, false);

            // add the value attribute
            attr = (DKAttrDefICM) dsDefICM.retrieveAttr(typename);
            attr.setNullable(true);
            attr.setUnique(false);
            lt.addAttr(attr);

            // create the href attribute for the data content
            MCRCM8ItemTypeCommon.createAttributeVarChar(connection, hrefname, hreflen, false);

            // add the value attribute
            attr = (DKAttrDefICM) dsDefICM.retrieveAttr(hrefname);
            attr.setNullable(true);
            attr.setUnique(false);
            lt.addAttr(attr);

            // create the label attribute for the data content
            MCRCM8ItemTypeCommon.createAttributeVarChar(connection, labelname, labellen, false);

            // add the value attribute
            attr = (DKAttrDefICM) dsDefICM.retrieveAttr(labelname);
            attr.setNullable(true);
            attr.setUnique(false);
            lt.addAttr(attr);

            // create the title attribute for the data content
            MCRCM8ItemTypeCommon.createAttributeVarChar(connection, titlename, titlelen, false);

            // add the value attribute
            attr = (DKAttrDefICM) dsDefICM.retrieveAttr(titlename);
            attr.setNullable(true);
            attr.setUnique(false);
            lt.addAttr(attr);

            // create the from attribute for the data content
            MCRCM8ItemTypeCommon.createAttributeVarChar(connection, fromname, fromlen, false);

            // add the value attribute
            attr = (DKAttrDefICM) dsDefICM.retrieveAttr(fromname);
            attr.setNullable(true);
            attr.setUnique(false);
            lt.addAttr(attr);

            // create the to attribute for the data content
            MCRCM8ItemTypeCommon.createAttributeVarChar(connection, toname, tolen, false);

            // add the value attribute
            attr = (DKAttrDefICM) dsDefICM.retrieveAttr(toname);
            attr.setNullable(true);
            attr.setUnique(false);
            lt.addAttr(attr);
        } catch (Exception e) {
            throw new MCRPersistenceException(e.getMessage(), e);
        }

        return lt;
    }
}
