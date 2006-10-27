/*
 * $RCSfile: MCRCM8MetaLinkID.java,v $
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

package org.mycore.backend.cm8.datatypes;

/**
 * This class implements the interface for the CM8 persistence layer for the
 * data model type MetaLinkID base on MetaLink.
 * 
 * @author Thomas Scheffler (yagee)
 * @version $Revision: 1.1 $ $Date: 2006/07/12 08:59:08 $
 */

public class MCRCM8MetaLinkID extends MCRCM8MetaLink {

    /**
     * {@inheritDoc}
     * 
     * @return 'L'
     */
    protected char getDataTypeChar() {
        return 'L';
    }
}
