/*
 * $Revision: 14844 $ 
 * $Date: 2009-03-10 12:17:43 +0100 (Di, 10. Mär 2009) $
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

/**
 * A virtual node in a file collection, which may be a child node of a container
 * file type like zip or tar. Such files can be browsed and read using this node
 * type.
 * 
 * @author Frank L�tzenkirchen
 */
public class MCRVirtualNode extends MCRNode {
    /**
     * Creates a new virtual node
     * 
     * @param parent
     *            the parent node containing this node
     * @param fo
     *            the file object in Apache VFS representing this node
     */
    protected MCRVirtualNode(MCRNode parent, FileObject fo) {
        super(parent, fo);
    }

    /**
     * Returns a virtual node that is a child of this virtual node.
     */
    protected MCRVirtualNode buildChildNode(FileObject fo) throws Exception {
        return new MCRVirtualNode(this, fo);
    }
}
