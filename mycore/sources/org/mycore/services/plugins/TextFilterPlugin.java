/*
 * 
 * $Revision: 13085 $ $Date: 2008-02-06 18:27:24 +0100 (Mi, 06. Feb 2008) $
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

package org.mycore.services.plugins;

import java.io.InputStream;
import java.io.Reader;
import java.util.HashSet;

import org.mycore.datamodel.ifs.MCRFileContentType;

/**
 * The Plugin spec for filtering several documents for the fulltext search. A
 * class implementing this interface may throw a
 * FilterPluginInstantiationException if it fails to initialize correctly and
 * though is not usable.
 * 
 * @author Thomas Scheffler (yagee)
 */
public interface TextFilterPlugin {
    /**
     * should return a Name of the plugin
     * 
     * @return Plugin name
     */
    public String getName();

    /**
     * should return the major version number
     * 
     * @return major version number
     */
    public int getMajorNumber();

    /**
     * should return the minor version number
     * 
     * @return minor version number
     */
    public int getMinorNumber();

    /**
     * may contain some additional Information on the plugin
     * 
     * @return further Informations on the plugin
     */
    public String getInfo();

    /**
     * returns a list of all supported MCRFileContentTypes.
     * 
     * These file extensions must be delivered without the leading dot.
     * 
     * @return HashSet List of file extensions
     */
    public HashSet getSupportedContentTypes();

    /**
     * onverts a given Inputstream to Textstream which should contain a textual
     * representation of the source.
     * 
     * @param input
     *            File in foreign format
     * @return Inputstream textual representation of input
     */
    public Reader transform(MCRFileContentType ct, InputStream input) throws FilterPluginTransformException;
}
