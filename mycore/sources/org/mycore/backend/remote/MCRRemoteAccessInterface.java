/*
 * $RCSfile: MCRConfiguration.java,v $
 * $Revision: 1.25 $ $Date: 2005/09/02 14:26:23 $
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

package org.mycore.backend.remote;

import java.io.BufferedInputStream;

import org.mycore.common.MCRException;
import org.mycore.common.xml.MCRXMLContainer;

/**
 * This interface is designed to choose the communication methodes for the
 * connection between a local MyCoRe-Client and remote MyCoRe-Server.
 * 
 * @author Jens Kupferschmidt
 * @author Mathias Zarick
 * @version $Revision: 1.4 $ $Date: 2005/08/15 15:02:30 $
 */
public interface MCRRemoteAccessInterface {
    /**
     * This method represide the XPATH query request to a remote MyCoRe-Server.
     * For the connection parameter would the MCRConfiguration used.
     * 
     * @param hostAlias
     *            the host alias as string that shall be requested.
     * @param mcrtype
     *            the type value of the MCRObjectId
     * @param query
     *            the query as a stream
     * @return the result of the query as MCRQueryResultArray
     * @exception MCRException
     *                general Exception of MyCoRe
     */
    public MCRXMLContainer requestQuery(String hostAlias, String mcrtype, String query) throws MCRException;

    /**
     * This methode represide the IFS request methode for the communication. For
     * the connection parameter would the MCRConfiguration used.
     * 
     * @param hostAlias
     *            the list of hostnames as string they should requested.
     * @param path
     *            the path to the IFS data
     * @exception MCRException
     *                general Exception of MyCoRe
     * @return the result of the query as MCRXMLContainer
     */
    public BufferedInputStream requestIFS(String hostAlias, String path) throws MCRException;

    /**
     * This method returns the HPPT header content string, if a requestIFS was
     * successful running.
     * 
     * @return HPPT header content string
     */
    public String getHeaderContent();
}
