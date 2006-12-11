/*
 * $RCSfile: MCRSearcherFactory.java,v $
 * $Revision: 1.9 $ $Date: 2006/12/08 14:21:37 $
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

package org.mycore.services.fieldquery;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.mycore.common.MCRConfiguration;
import org.mycore.common.MCRConfigurationException;

/**
 * This class manages instances of MCRSearcher and provides methods to get these
 * for a given searcher ID. The class is responsible for looking up, loading,
 * instantiating and remembering the implementations of MCRSearcher used in the
 * system.
 * 
 * @author Frank L�tzenkirchen
 * @version $Revision: 1.9 $ $Date: 2006/12/08 14:21:37 $
 */
public class MCRSearcherFactory {
    /** Hashtable SearcherID to MCRSearcher instance */
    protected static Hashtable<String,MCRSearcher> table = new Hashtable<String,MCRSearcher>();

    /** The logger */
    private static final Logger LOGGER = Logger.getLogger(MCRSearcherFactory.class);

    /**
     * Returns the MCRSearcher instance that is configured for this SearcherID.
     * The instance that is returned is configured by the property
     * <tt>MCR.Searcher.<ID>.Class</tt> in mycore.properties.
     * 
     * @param searcherID
     *            the non-null ID of the MCRSearcher implementation
     * @return the MCRSearcher instance that uses this searcherID
     * @throws MCRConfigurationException
     *             if no MCRSearcher implementation is configured for this ID
     */
    public static MCRSearcher getSearcher(String searcherID) {
        if (!table.containsKey(searcherID)) {
            try {
                String searcherClass = "MCR.Searcher." + searcherID + ".Class";
                LOGGER.debug("Reading searcher implementation for ID " + searcherID + ": " + searcherClass);

                Object obj = MCRConfiguration.instance().getSingleInstanceOf(searcherClass);
                MCRSearcher s = (MCRSearcher) (obj);
                s.init(searcherID);

                table.put(searcherID, s);
            } catch (Exception ex) {
                String msg = "Could not load MCRSearcher with searcher ID = " + searcherID;
                throw new MCRConfigurationException(msg, ex);
            }
        }

        return table.get(searcherID);
    }

    /**
     * Returns the MCRSearcher instance that manages the given index.
     * 
     * @param indexID
     *            the ID of the search index as declared in searchfields.xml
     * @return the MCRSearcher instance that manages this index
     * @throws MCRConfigurationException
     *             if no MCRSearcher implementation is configured for this index
     */
    public static MCRSearcher getSearcherForIndex(String indexID) {
        String prefix = "MCR.Searcher.";
        String suffix = ".Index";

        Properties props = MCRConfiguration.instance().getProperties(prefix);
        Enumeration names = props.keys();
        while (names.hasMoreElements()) {
            String name = (String) (names.nextElement());
            if (name.endsWith(suffix) && MCRConfiguration.instance().getString(name).equals(indexID)) {
                String searcherID = name.substring(prefix.length(), name.indexOf(suffix));
                return getSearcher(searcherID);
            }
        }

        String msg = "No searcher configured for search index " + indexID;
        throw new MCRConfigurationException(msg);
    }
}
