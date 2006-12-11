/*
 * $RCSfile: MCRCM8MetaBoolean.java,v $
 * $Revision: 1.1 $ $Date: 2006/09/08 10:21:00 $
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

package org.mycore.backend.cm8.datatypes;

import org.jdom.Element;

import com.ibm.mm.sdk.common.DKAttrDefICM;
import com.ibm.mm.sdk.common.DKComponentTypeDefICM;
import com.ibm.mm.sdk.common.DKException;

/**
 * This class implements the interface for the CM8 persistence layer for the
 * data model type MetaBoolean.
 * 
 * @author Thomas Scheffler (yagee)
 * @version $Revision: 1.1 $ $Date: 2006/09/08 10:21:00 $
 */
public class MCRCM8MetaBoolean extends MCRAbstractCM8ComponentType {

    public DKComponentTypeDefICM createComponentType(final Element element) throws DKException, Exception {
        final DKComponentTypeDefICM typeDef = getBaseItemTypeDef(element);
        typeDef.addAttr(getBooleanAttr("mcrBoolean", getAttrDescription(element)));
        return typeDef;
    }

    private DKAttrDefICM getBooleanAttr(final String name, final String description) throws Exception, DKException {
        MCRCM8AttributeUtils.createAttributeBoolean(getDsDefICM(), name);
        final DKAttrDefICM attr = (DKAttrDefICM) getDsDefICM().retrieveAttr(name);
        attr.setDescription(description);
        attr.setNullable(true);
        attr.setUnique(false);
        return attr;
    }

    /**
     * {@inheritDoc}
     * 
     * @return 'b'
     */
    protected char getDataTypeChar() {
        return 'b';
    }
}
