/*
 * $RCSfile: MCRCM8TransformXPathToCM8.java,v $
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

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.mycore.common.MCRConfiguration;
import org.mycore.common.MCRException;
import org.mycore.common.MCRPersistenceException;
import org.mycore.common.MCRUtils;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.metadata.MCRXMLTableManager;
import org.mycore.services.query.MCRMetaSearchInterface;

import com.ibm.mm.sdk.common.DKConstantICM;
import com.ibm.mm.sdk.common.DKDDO;
import com.ibm.mm.sdk.common.DKNVPair;
import com.ibm.mm.sdk.common.DKResults;
import com.ibm.mm.sdk.common.dkIterator;
import com.ibm.mm.sdk.server.DKDatastoreICM;

/**
 * This is the tranformer implementation for CM 8 from XPath language to the CM
 * Search Engine language (this is XPath like).
 * 
 * @author Jens Kupferschmidt
 * @version $Revision: 1.1 $ $Date: 2006/07/12 08:59:07 $
 */
public class MCRCM8TransformXPathToCM8 implements MCRMetaSearchInterface, DKConstantICM {
    // private data
    public static final String DEFAULT_QUERY = "";

    // the logger
    protected static Logger logger = Logger.getLogger(MCRCM8TransformXPathToCM8.class.getName());

    private MCRConfiguration config = null;

    /**
     * The constructor.
     */
    public MCRCM8TransformXPathToCM8() {
        config = MCRConfiguration.instance();
    }

    /**
     * This method start the Query over the XML:DB persitence layer for one
     * object type and and return the query result as HashSet of MCRObjectIDs.
     * 
     * @param root
     *            the query root
     * @param query
     *            the metadata queries
     * @param type
     *            the MCRObject type
     * @param maxresults
     *            the maximum of results
     * @return a result list as MCRXMLContainer
     */
    public final HashSet getResultIDs(String root, String query, String type) {
        // prepare the query over the rest of the metadata
        HashSet idmeta = new HashSet();
        logger.debug("Incomming condition : " + query);

        // set the default
        String newquery = "";

        if ((root == null) && (query.length() == 0)) {
            newquery = DEFAULT_QUERY;
        }

        int maxresults = config.getInt("MCR.query_max_results", 100);

        // read prefix from configuration
        String sb = new String("MCR.persistence_cm8_" + type.toLowerCase());
        String itemtypename = config.getString(sb);
        String itemtypeprefix = config.getString(sb + "_prefix");

        // Select the query strings
        StringBuffer cond = new StringBuffer(1024);
        String ss = "";
        int i = 0;
        int j = 0;
        int l = query.length();

        while (i < l) {
            j = query.indexOf("#####", i);

            if (j == -1) {
                try {
                    cond.append(' ').append(traceOneCondition(query.substring(i, l), itemtypeprefix));
                } catch (MCRException me) {
                    logger.error(me.getMessage());
                }

                break;
            }

            ss = query.substring(i, j);

            if (ss.equals(" and ")) {
                cond.append(ss);
            } else {
                if (ss.equals(" or ")) {
                    cond.append(ss);
                } else {
                    try {
                        cond.append(' ').append(traceOneCondition(ss, itemtypeprefix));
                    } catch (MCRException me) {
                        cond.append(' ');
                    }
                }
            }

            i = j + 5;
        }

        logger.debug("Codition transformation " + cond.toString());

        // build the query
        if (cond.toString().trim().length() != 0) {
            StringBuffer nq = new StringBuffer(1024);
            nq.append('/').append(itemtypename);
            nq.append('[').append(cond.toString().trim()).append(']');
            newquery = nq.toString();
        } else {
            StringBuffer nq = new StringBuffer(1024);
            nq.append('/').append(itemtypename);
            newquery = nq.toString();
        }

        logger.debug("Transformed query " + newquery);

        // Start the search
        DKDatastoreICM connection = null;

        try {
            connection = MCRCM8ConnectionPool.instance().getConnection();

            DKNVPair[] parms = new DKNVPair[3];
            parms[0] = new DKNVPair(DK_CM_PARM_MAX_RESULTS, new Integer(maxresults).toString());
            parms[1] = new DKNVPair(DK_CM_PARM_RETRIEVE,

            // new Integer(DK_CM_CONTENT_ATTRONLY |
                    // DK_CM_CONTENT_LINKS_OUTBOUND));
                    new Integer(DK_CM_CONTENT_YES));
            parms[2] = new DKNVPair(DK_CM_PARM_END, null);

            DKResults rsc = (DKResults) connection.evaluate(newquery, DK_CM_XQPE_QL_TYPE, parms);
            dkIterator iter = rsc.createIterator();
            logger.debug("Results :" + rsc.cardinality());

            MCRXMLTableManager xmltable = MCRXMLTableManager.instance();
            String id = "";

            short dataId = 0;

            while (iter.more()) {
                DKDDO resitem = (DKDDO) iter.next();
                resitem.retrieve();
                dataId = resitem.dataId(DK_CM_NAMESPACE_ATTR, itemtypeprefix + "ID");
                id = (String) resitem.getData(dataId);
                idmeta.add(new MCRObjectID(id));
            }
        } catch (Exception e) {
            throw new MCRPersistenceException("CM8 Search error." + e.getMessage());
        } finally {
            MCRCM8ConnectionPool.instance().releaseConnection(connection);
        }

        for (Iterator it = idmeta.iterator(); it.hasNext();) {
            logger.debug("IDMETA = " + ((MCRObjectID) it.next()).getId());
        }

        return idmeta;
    }

    /**
     * This is a private routine they trace one condition.
     * 
     * @param onecond
     *            one single condition
     * @param itemtypeprefix
     * the prefix of the itme type @ return the transfromed query for CM8.
     */
    private final String traceOneCondition(String condstr, String itemtypeprefix) {
        // search operations
        int maxcount = 10;
        String[] pathin = new String[maxcount];
        String[] pathout = new String[maxcount];
        String[] tag = new String[maxcount];
        String[] op = new String[maxcount];
        String[] value = new String[maxcount];
        String[] bool = new String[maxcount];
        int counter = 0;
        boolean klammer = false;

        // search for []
        String cond = "";
        int i = condstr.indexOf("[");

        if (i != -1) {
            int j = condstr.indexOf("]");

            if (j == -1) {
                throw new MCRPersistenceException("Error while analyze the query string.");
            }

            klammer = true;
            cond = condstr.substring(i + 1, j);

            String p = condstr.substring(0, i);

            for (int k = 0; k < maxcount; k++) {
                pathout[k] = p;
                pathin[k] = "";
            }
        } else {
            for (int k = 0; k < maxcount; k++) {
                pathin[k] = "";
                pathout[k] = "";
            }

            cond = condstr;
        }

        logger.debug("Incomming condition : " + cond);

        // analyze cond
        int tippelauf = 0;
        int tippelzu = 0;
        int tippelauf1 = 0;
        int tippelauf2 = 0;
        int tagstart = 0;
        int opstart = 0;

        while ((tippelauf != -1) && (tippelzu != -1)) {
            tippelauf1 = cond.indexOf("\"", tippelzu + 1);
            tippelauf2 = cond.indexOf("'", tippelzu + 1);

            if (tippelauf1 != -1) {
                tippelauf = tippelauf1;
                tippelzu = cond.indexOf("\"", tippelauf + 1);

                if (tippelzu == -1) {
                    break;
                }
            } else {
                if (tippelauf2 != -1) {
                    tippelauf = tippelauf2;
                    tippelzu = cond.indexOf("'", tippelauf + 1);

                    if (tippelzu == -1) {
                        break;
                    }
                } else {
                    break;
                }
            }

            value[counter] = new String(cond.substring(tippelauf + 1, tippelzu).trim());

            boolean opset = false;

            if (!opset) {
                opstart = cond.toUpperCase().indexOf("CONTAINS(", tagstart);

                if ((opstart != -1) && (opstart < tippelauf)) {
                    op[counter] = "contains";
                    tag[counter] = cond.substring(tagstart, opstart).trim();
                    opset = true;
                }
            }

            if (!opset) {
                opstart = cond.toUpperCase().indexOf("LIKE", tagstart);

                if ((opstart != -1) && (opstart < tippelauf)) {
                    op[counter] = "like";
                    tag[counter] = cond.substring(tagstart, opstart).trim();
                    opset = true;
                }
            }

            if (!opset) {
                opstart = cond.indexOf("!=", tagstart);

                if ((opstart != -1) && (opstart < tippelauf)) {
                    op[counter] = "!=";
                    tag[counter] = cond.substring(tagstart, opstart).trim();
                    opset = true;
                }
            }

            if (!opset) {
                opstart = cond.indexOf(">=", tagstart);

                if ((opstart != -1) && (opstart < tippelauf)) {
                    op[counter] = ">=";
                    tag[counter] = cond.substring(tagstart, opstart).trim();
                    opset = true;
                }
            }

            if (!opset) {
                opstart = cond.indexOf("<=", tagstart);

                if ((opstart != -1) && (opstart < tippelauf)) {
                    op[counter] = "<=";
                    tag[counter] = cond.substring(tagstart, opstart).trim();
                    opset = true;
                }
            }

            if (!opset) {
                opstart = cond.indexOf("=", tagstart);

                if ((opstart != -1) && (opstart < tippelauf)) {
                    op[counter] = "=";
                    tag[counter] = cond.substring(tagstart, opstart).trim();
                    opset = true;
                }
            }

            if (!opset) {
                opstart = cond.indexOf("<", tagstart);

                if ((opstart != -1) && (opstart < tippelauf)) {
                    op[counter] = "<";
                    tag[counter] = cond.substring(tagstart, opstart).trim();
                    opset = true;
                }
            }

            if (!opset) {
                opstart = cond.indexOf(">", tagstart);

                if ((opstart != -1) && (opstart < tippelauf)) {
                    op[counter] = ">";
                    tag[counter] = cond.substring(tagstart, opstart).trim();
                    opset = true;
                }
            }

            if (!opset) {
                return "";
            }

            bool[counter] = "";

            if ((tippelzu + 5) < cond.length()) {
                tagstart = cond.toLowerCase().indexOf(" and ", tippelzu + 1);

                if (tagstart == -1) {
                    tagstart = cond.toLowerCase().indexOf(" or ", tippelzu + 1);

                    if (tagstart == -1) {
                        return "";
                    }

                    tagstart += 4;
                    bool[counter] = " or ";
                } else {
                    tagstart += 5;
                    bool[counter] = " and ";
                }
            }

            // has the tag a path (if true split them)
            StringBuffer sbpath = new StringBuffer("");
            int j = 0;
            int l;
            int lastl = 0;
            int k = tag[counter].length();

            while (j < k) {
                l = tag[counter].indexOf("/", j);

                if (l == -1) {
                    String nt = "";

                    if (tag[counter].charAt(j) == '@') {
                        nt = tag[counter].substring(j, tag[counter].length());
                    }

                    if (tag[counter].charAt(j) == '*') {
                        nt = "*";
                    }

                    if (tag[counter].indexOf("ts()", j) != -1) {
                        nt = "*";
                    }

                    if (tag[counter].indexOf("text()", j) != -1) {
                        nt = "text()";
                    }

                    if (nt.length() == 0) {
                        nt = "text()";

                        if (lastl != 0) {
                            sbpath.append('/');
                        }

                        sbpath.append(tag[counter].substring(j, tag[counter].length()));
                    }

                    if (sbpath.length() != 0) {
                        pathin[counter] = sbpath.toString();
                    } else {
                        pathin[counter] = "";
                    }

                    tag[counter] = nt;

                    break;
                }

                if (lastl != 0) {
                    sbpath.append('/');
                }

                sbpath.append(tag[counter].substring(j, l));
                lastl = l;
                j = l + 1;
            }

            // add the itemtypeprefix to the pathout
            sbpath = new StringBuffer("");
            j = 0;
            k = pathout[counter].length();

            while (j < k) {
                l = pathout[counter].indexOf("/", j);
                String iType=itemtypeprefix;
                if(pathout[counter].substring(j, k).startsWith("*")) 
                	iType="";
                	
                if (l == -1) {
                 	sbpath.append(iType).append(pathout[counter].substring(j, k));	
                 	pathout[counter] = sbpath.toString();
                    break;
                }
                
                sbpath.append(iType).append(pathout[counter].substring(j, l + 1));
                j = l + 1;
            }

            // add the itemtypeprefix to the pathin
            sbpath = new StringBuffer("");
            j = 0;
            k = pathin[counter].length();

            while (j < k) {
                l = pathin[counter].indexOf("/", j);

                if (l == -1) {
                    sbpath.append(itemtypeprefix).append(pathin[counter].substring(j, k));
                    pathin[counter] = sbpath.toString();

                    break;
                }

                sbpath.append(itemtypeprefix).append(pathin[counter].substring(j, l + 1));
                j = l + 1;
            }

            // replace the tag if it is text()
            if (tag[counter].equals("text()")) {
                j = 0;

                if (pathin[counter].length() != 0) {
                    k = pathin[counter].length();

                    while (j < k) {
                        l = pathin[counter].indexOf("/", j);

                        if (l == -1) {
                            tag[counter] = "@" + pathin[counter].substring(j + 2, k);

                            break;
                        }

                        j = l + 1;
                    }
                } else {
                    if (pathout[counter].length() == 0) {
                        tag[counter] = "*";
                    }

                    k = pathout[counter].length();

                    while (j < k) {
                        l = pathout[counter].indexOf("/", j);

                        if (l == -1) {
                            tag[counter] = "@" + pathout[counter].substring(j + 2, k);

                            break;
                        }

                        j = l + 1;
                    }
                }
            }

            // increment the counter
            counter++;
        }

        // debug

        /*
         * for (i=0;i <counter;i++) { logger.debug("PATHOUT="+pathout[i]);
         * logger.debug("PATHIN="+pathin[i]); logger.debug("TAG="+tag[i]);
         * logger.debug("OPER="+op[i]); logger.debug("VALUE="+value[i]);
         * logger.debug("BOOLEAN="+bool[i]); logger.debug(""); }
         */
        StringBuffer sbout = new StringBuffer();

        // if we have a common path
        if (klammer) {
            sbout.append(pathout[0]).append('[');
        }

        for (i = 0; i < counter; i++) {
            // if we have a xml namespace
            int x = tag[i].indexOf("@xml:");

            if (x != -1) {
                tag[i] = "@" + tag[i].substring(x + 5, tag[i].length());
            }

            // if we have a xlink namespace
            x = tag[i].indexOf("@xlink:");

            if (x != -1) {
                tag[i] = "@xlink" + tag[i].substring(x + 7, tag[i].length());
            }

            // expand the attributes with itemtypeprefix
            if (!tag[i].equals("*")) {
                tag[i] = "@" + itemtypeprefix + tag[i].substring(1, tag[i].length());
            }

            // create for CONTAINS
            if (op[i].equals("contains")) {
                value[i] = value[i].replace('*', '%');

                StringTokenizer st = new StringTokenizer(value[i]);
                int stcount = st.countTokens();

                while (st.hasMoreTokens()) {
                    System.out.println(tag[i]);

                    if (tag[i].equals("*")) {
                        sbout.append(" contains-text(@").append(itemtypeprefix).append("ts,\"\'").append(st.nextToken()).append("\'\")=1");
                    } else {
                        sbout.append(" contains-text(");

                        if (pathin[i].length() != 0) {
                            sbout.append(pathin[i]).append('/');
                        }

                        sbout.append(tag[i]).append(",\"\'").append(st.nextToken()).append("\'\")=1");
                    }

                    if ((stcount > 1) && (st.countTokens() > 0)) {
                        sbout.append(" and ");
                    }
                }

                sbout.append(bool[i]).append(' ');

                continue;
            }

            if (pathin[i].length() != 0) {
                sbout.append(pathin[i]).append('/');
            }

            sbout.append(tag[i]);

            if (op[i].equals("like")) {
                // replace * with % and add one % if it is not in the string
                if (value[i].indexOf("*") == -1) {
                    value[i] = value[i] + "*";
                }

                value[i] = value[i].replace('*', '%');
            }

            // is value[0] a date
            try {
                GregorianCalendar date = MCRUtils.covertDateToGregorianCalendar(value[i]);
                long number = 0;

                if (date.get(Calendar.ERA) == GregorianCalendar.AD) {
                    number = ((4000 + date.get(Calendar.YEAR)) * 10000) + (date.get(Calendar.MONTH) * 100) + date.get(Calendar.DAY_OF_MONTH);
                } else {
                    number = ((4000 - date.get(Calendar.YEAR)) * 10000) + (date.get(Calendar.MONTH) * 100) + date.get(Calendar.DAY_OF_MONTH);
                }

                logger.debug("Date " + value[i] + " as number = " + Long.toString(number));
                value[i] = Long.toString(number);
                sbout.append(' ').append(op[i]).append(' ').append(value[i]).append(bool[i]);

                continue;
            } catch (Exception e) {
            }

            // is value[0] a number
            try {
                double numb = Double.parseDouble(value[i]);
                sbout.append(' ').append(op[i]).append(' ').append(value[i]).append(' ').append(bool[i]);

                continue;
            } catch (Exception e) {
            }

            sbout.append(' ').append(op[i]).append(" \"").append(value[i]).append("\"").append(bool[i]);
        }

        if (klammer) {
            sbout.append(']');
        }

        return sbout.toString();
    }
}
