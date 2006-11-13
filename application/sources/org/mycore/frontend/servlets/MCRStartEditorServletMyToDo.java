/*
 * $RCSfile: MCRStartEditorServletMyToDo.java,v $
 * $Revision: 1.1 $ $Date: 2006/11/03 09:04:50 $
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

package org.mycore.frontend.servlets;

import java.io.IOException;
import java.util.ArrayList;

import org.mycore.access.MCRAccessManager;
import org.mycore.datamodel.metadata.MCRActiveLinkException;
import org.mycore.datamodel.metadata.MCRMetaElement;
import org.mycore.datamodel.metadata.MCRMetaNBN;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.services.urn.MCRURNManager;

/**
 * The class extends the MCRStartEditorServlet with methods for NBN integration and email service to the DNB.
* 
* @author Jens Kupferschmidt
* @version $Revision: 1.1 $ $Date: 2006/11/03 09:04:50 $
*/
public class MCRStartEditorServletMyToDo extends MCRStartEditorServlet {

    private static final long serialVersionUID = 1L;
    
    /**
     * The method add a new NBN to the dataset with type <b>document</b> . The access right is writedb.
     * 
     * @param job
     *            the MCRServletJob instance
     */
    public void saddnbn(MCRServletJob job) throws IOException {
        // access right
        if (!MCRAccessManager.checkPermission(mysemcrid, "writedb")) {
            job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + usererrorpage));
            return;
        }
        if (mysemcrid.length() == 0) {
            job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + mcriderrorpage));
            return;
        }
        // check type
        if (mytype.equals("document")) {
            MCRObject obj = new MCRObject();
            obj.receiveFromDatastore(mysemcrid);
            MCRMetaElement elm = obj.getMetadataElement("nbns");
            if (elm == null) {
                String urn = MCRURNManager.buildURN("UBL");
                MCRMetaNBN nbn = new MCRMetaNBN("metadata","nbn",0,urn);
                ArrayList list = new ArrayList();
                elm = new MCRMetaElement("de","MCRMeatNBN","nbns",true,false,true,false,list);
                elm.addMetaObject(nbn);
                obj.getMetadata().setMetadataElement(elm,"nbns");
                try {
                obj.updateInDatastore();
                MCRURNManager.assignURN(urn,obj.getId().toString());
                } catch (MCRActiveLinkException e) {
                    LOGGER.warn("Can't store NBN for "+mysemcrid);
                    e.printStackTrace();
                }
                LOGGER.info("Add the NBN "+urn);
            } else {
                LOGGER.warn("The NBN already exists for "+mysemcrid);
            }
            
        }
        // back to the metadata view
        StringBuffer sb = new StringBuffer();
        sb.append(getBaseURL()).append("receive/").append(mysemcrid);
        job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(sb.toString()));
    }
    
}
