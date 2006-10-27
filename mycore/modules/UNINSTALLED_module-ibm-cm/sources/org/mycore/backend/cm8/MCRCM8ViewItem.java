/*
 * $RCSfile: MCRCM8ViewItem.java,v $
 * $Revision: 1.16 $ $Date: 2006/01/26 14:32:41 $
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

import org.mycore.common.MCRConfiguration;
import org.mycore.common.MCRException;
import org.mycore.datamodel.metadata.MCRObjectID;

import com.ibm.mm.sdk.common.DKChildCollection;
import com.ibm.mm.sdk.common.DKConstant;
import com.ibm.mm.sdk.common.DKConstantICM;
import com.ibm.mm.sdk.common.DKDDO;
import com.ibm.mm.sdk.common.DKException;
import com.ibm.mm.sdk.common.DKLobICM;
import com.ibm.mm.sdk.common.DKNVPair;
import com.ibm.mm.sdk.common.DKPidICM;
import com.ibm.mm.sdk.common.DKResults;
import com.ibm.mm.sdk.common.DKSequentialCollection;
import com.ibm.mm.sdk.common.dkIterator;
import com.ibm.mm.sdk.server.DKDatastoreICM;

/**
 * This class implements a main program to show the CM8 content of an item.
 * <br />
 * Start the program with <br />
 * java CRCM8ViewItem doc <em>MCRObjectID</em> or <br />
 * java CRCM8ViewItem ifs <em>MCRObjectID</em><br />
 * 
 * @author Holger K�nig
 * @author Jens Kupferschmidt
 * @author Kathleen Krebs
 * @version $Revision: 1.16 $ $Date: 2006/01/26 14:32:41 $
 */
public class MCRCM8ViewItem implements DKConstantICM {
    // The configuration
    static MCRConfiguration conf = null;

    static String database = "";

    static String userid = "";

    static String password = "";

    static String itemtype = "";

    static String prefix = "";

    static String ifsfile = "";

    static String query = "";

    static MCRObjectID mcrid = null;

    public static void main(String[] argv) throws DKException, Exception {
        // read the configuration
        conf = MCRConfiguration.instance();
        database = conf.getString("MCR.persistence_cm8_library_server");
        userid = conf.getString("MCR.persistence_cm8_user_id");
        password = conf.getString("MCR.persistence_cm8_password");

        // Read the arguments
        if ((!argv[0].equals("doc")) && (!argv[0].equals("ifs"))) {
            System.out.println("The argument 1 is not doc or ifs.");
            System.out.println();
        }

        if (argv[0].equals("doc")) {
            if (argv[1].equals("id")) {
                try {
                    mcrid = new MCRObjectID(argv[2]);
                } catch (MCRException e) {
                    System.out.println("The argument 3 is not a MCRObjectID.");
                    System.out.println();
                    System.exit(0);
                }

                itemtype = conf.getString("MCR.persistence_cm8_" + mcrid.getTypeId());
                prefix = conf.getString("MCR.persistence_cm8_" + mcrid.getTypeId() + "_prefix");
                query = "/" + itemtype + "[@" + prefix + "ID=\"" + mcrid.getId() + "\"]";

                // query =
                // "/"+itemtype+"["+prefix+"service/"+prefix+"servdates/"+prefix+"servdate[@"+prefix+"servdate
                // = 60010821 and @"+prefix+"type = \"validfromdate\"]]";
            }
        }

        if (argv[0].equals("ifs")) {
            itemtype = conf.getString("MCR.IFS.ContentStore.CM8.ItemType");
            ifsfile = conf.getString("MCR.IFS.ContentStore.CM8.Attribute.File");

            if (argv[1].equals("id")) {
                try {
                    mcrid = new MCRObjectID(argv[2]);
                } catch (MCRException e) {
                    System.out.println("The argument 3 is not a MCRObjectID.");
                    System.out.println();
                    System.exit(0);
                }

                query = "/" + itemtype + "[@ifsowner=\"" + mcrid.getId() + "\"]";
            }

            // show a nse items
            if (argv[1].equals("nse")) {
                query = "/" + itemtype + "[contains-text (@TIEREF,\"\'" + argv[2] + "\'\")=1]";
            }

            // show all items
            if (argv[1].equals("all")) {
                query = "/" + itemtype;
            }
        }

        if (query.length() == 0) {
            query = "/" + itemtype;
        }

        System.out.println(query);

        // Open connection
        DKDatastoreICM dsICM = new DKDatastoreICM(); // Create new datastore
        // object.

        dsICM.connect(database, userid, password, ""); // Connect to the
        // datastore.

        System.out.println("| All items in " + itemtype + ", server " + database);
        System.out.println("| Query >>>" + query + "<<<");

        // Specify Search / Query Options
        DKNVPair[] options = new DKNVPair[3];
        options[0] = new DKNVPair(DKConstant.DK_CM_PARM_MAX_RESULTS, "0"); // No
        // Maximum
        // (Default)

        options[1] = new DKNVPair(DKConstant.DK_CM_PARM_RETRIEVE, new Integer(DKConstant.DK_CM_CONTENT_YES));
        options[2] = new DKNVPair(DKConstant.DK_CM_PARM_END, null);

        DKResults results = (DKResults) dsICM.evaluate(query, DKConstantICM.DK_CM_XQPE_QL_TYPE, options);
        dkIterator iter = results.createIterator();
        System.out.println("| Number of Results:  " + results.cardinality());

        while (iter.more()) {
            System.out.println("|");

            Object ddo = iter.next(); // Move pointer to next element and
                                        // obtain
            // that object.

            processDDO(ddo, "");
        }

        dsICM.disconnect();
    }

    private static void processDDO(Object obj, String pre) throws Exception {
        System.out.println(pre + "|--+-- Class name: " + obj.getClass().getName());

        DKDDO ddo = (DKDDO) obj;
        ddo.retrieve();

        String itemId = ((DKPidICM) ddo.getPidObject()).getItemId();
        System.out.println(pre + "+  +-- Item ID:  " + itemId + "  (" + ddo.getPidObject().getObjectType() + ")");

        if (ddo instanceof DKLobICM) {
            DKLobICM lob = (DKLobICM) ddo;
            System.out.println(pre + "+  +-- Size: " + lob.getSize() + " Bytes, Mime-Type: " + lob.getMimeType());

            String[] url = lob.getContentURLs(DK_CM_RETRIEVE, DK_CM_CHECKOUT, -1, -1, DK_ICM_GETINITIALRMURL);
            System.out.println(pre + "+  +-- URL: " + url[0]);

            // System.out.println(pre+"+ +-- URL:
            // "+lob.getContentURL(-1,-1,-1));
        }

        for (short i = 1; i <= ddo.dataCount(); i++) {
            System.out.println(pre + "|  +-- " + ddo.getDataNameSpace(i) + ":" + ddo.getDataName(i) + "=" + ddo.getData(i));

            if (ddo.getDataNameSpace(i).equals("CHILD")) {
                DKChildCollection col = (DKChildCollection) ddo.getData(i);
                System.out.println(pre + "|  |   Cardinality: " + col.cardinality());

                dkIterator iter = col.createIterator();

                while (iter.more()) {
                    processDDO(iter.next(), "|  " + pre);
                }
            }

            if (ddo.getDataName(i).equals("DKParts")) {
                DKSequentialCollection col = (DKSequentialCollection) ddo.getData(i);
                System.out.println(pre + "|  |   Cardinality: " + col.cardinality());

                dkIterator iter = col.createIterator();

                while (iter.more()) {
                    processDDO(iter.next(), "|  " + pre);
                }
            }
        }

        // uncomment the next lines if you will delete the items from the
        // itemtype
        // String storageID = ddo.getPidObject().pidString();
        // doDeleteContent(storageID);
        // System.out.println("Item Deleted");
    }
}
