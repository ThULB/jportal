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

package org.mycore.backend.extend;

import java.util.HashSet;

import org.mycore.common.MCRException;
import org.mycore.common.MCRUtils;
import org.mycore.common.xml.MCRXMLContainer;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.services.query.MCRMetaSearchInterface;
import org.mycore.services.query.MCRQueryBase;

/**
 * This is the implementation of the MCRQueryInterface for the default case
 * 
 * @author Jens Kupferschmidt
 * @author Thomas Scheffler (yagee)
 * @version $Revision: 1.6 $ $Date: 2005/08/15 15:01:54 $
 */
public class MCRExtendSearchMerger extends MCRQueryBase {
    /** The default query * */
    public static final String DEFAULT_QUERY = "";

    private String metaclassname = null;

    private MCRMetaSearchInterface msif;

    /**
     * The constructor.
     */
    public MCRExtendSearchMerger() {
        super();

        String temp = config.getString("MCR.XMLStore.Type", "jdom");
        metaclassname = config.getString("MCR.persistence_" + temp + "_query_name");
        logger.debug("Load the metadata search class " + metaclassname);

        try {
            msif = (MCRMetaSearchInterface) Class.forName(metaclassname).newInstance();
        } catch (ClassNotFoundException e) {
            throw new MCRException(metaclassname + " ClassNotFoundException");
        } catch (IllegalAccessException e) {
            throw new MCRException(metaclassname + " IllegalAccessException");
        } catch (InstantiationException e) {
            throw new MCRException(metaclassname + " InstantiationException");
        }
    }

    /**
     * This method start the Query over one object type and return the result as
     * MCRXMLContainer.
     * 
     * @param type
     *            the MCRObject type
     * @return a result list as MCRXMLContainer
     */
    protected final MCRXMLContainer startQuery(String type) {
        MCRXMLContainer result = new MCRXMLContainer();
        boolean hasts = false; // true if we have a full text search
        boolean hasmeta = false; // true if we have a metadata search

        // Make all document searches
        HashSet idts = new HashSet();

        for (int i = 0; i < subqueries.size(); i++) {
            if (((String) subqueries.get(i)).indexOf(XPATH_ATTRIBUTE_DOCTEXT) != -1) {
                hasts = true;
                flags.set(i, Boolean.TRUE);
                logger.debug("TextSearch query : " + (String) subqueries.get(i));

                // start the query against the textsearch
                if (searchfulltext) {
                    for (int j = 0; j < tsint.length; j++) {
                        String[] der = tsint[j].getDerivateIDs((String) subqueries.get(i));

                        for (int k = 0; k < der.length; k++) {
                            MCRObjectID oid = getObjectID(der[k]);

                            if (oid != null) {
                                idts.add(oid);
                            } else {
                                logger.warn("Ignoring ObjectID=null");
                            }
                        }
                    }
                }
            }
        }

        // prepare the query over the rest of the metadata
        HashSet idmeta = new HashSet();
        String metaquery = "";

        if (subqueries.size() == 0) {
            metaquery = DEFAULT_QUERY;
            hasmeta = true;
        } else {
            StringBuffer qsb = new StringBuffer(1024);

            for (int i = 0; i < subqueries.size(); i++) {
                if (((Boolean) flags.get(i)).booleanValue()) {
                    continue;
                }

                hasmeta = true;
                qsb.append(" #####").append((String) subqueries.get(i)).append("#####");

                boolean fl = false;

                for (int j = i + 1; j < subqueries.size(); j++) {
                    if (!((Boolean) flags.get(j)).booleanValue()) {
                        fl = true;
                    }
                }

                if (fl) {
                    qsb.append(' ').append((String) andor.get(i));
                }

                flags.set(i, Boolean.TRUE);
            }

            metaquery = qsb.toString();
            logger.debug("Metadate query : " + metaquery);
        }

        if (hasmeta) {
            idmeta = msif.getResultIDs(root, metaquery, type);
        }

        // merge the results
        HashSet myresult = null;

        if (!hasts) {
            myresult = idmeta;
        }

        if (!hasmeta) {
            myresult = idts;
        }

        if ((hasts) && (hasmeta)) {
            myresult = MCRUtils.mergeHashSets(idts, idmeta, MCRUtils.COMMAND_AND);
        }

        logger.debug("Number of items in HashSet befor cutting " + Integer.toString(myresult.size()));
        myresult = MCRUtils.cutHashSet(myresult, maxresults);
        logger.debug("Number of items in HashSet after cutting " + Integer.toString(myresult.size()));

        // put the XML files in the result container
        result = createResultContainer(myresult);

        return result;
    }
}
