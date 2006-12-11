/*
 * $RCSfile: MCRURNResolver.java,v $
 * $Revision: 1.2 $ $Date: 2006/06/23 11:51:23 $
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

package org.mycore.services.urn;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.mycore.common.MCRConfiguration;

/**
 * This servlet resolves a given URN (urn:nbn:de) from a HTTP request and
 * redirects the client to the document that is registered for this URN. The URN
 * can be either given as the query string or as the request path. If the URN is
 * assigned to a local document, the request is redirected to the frontpage that
 * displays the document's metadata, as specified by the configuration property
 * MCR.URN.Resolver.DocumentURL. If the URN is not local, the request is
 * redirected to another URN resolver, as specified by the configuration
 * property MCR.URN.Resolver.MasterURL.
 * 
 * 
 * @author Frank L�tzenkirchen
 * @version $Revision: 1.2 $ $Date: 2006/06/23 11:51:23 $
 */
public class MCRURNResolver extends HttpServlet {

    private final static Logger LOGGER = Logger.getLogger(MCRURNResolver.class);

    protected String masterURL;

    protected String documentURL;

    public void init() {
        String base = "MCR.URN.Resolver.";
        masterURL = MCRConfiguration.instance().getString(base + "MasterURL");
        documentURL = MCRConfiguration.instance().getString(base + "DocumentURL");
    }

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String path = req.getPathInfo();
        String param = req.getQueryString();

        String urn = param;

        if ((urn == null) && (path != null))
            urn = path.substring(1).trim();

        if (urn == null) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        LOGGER.info("Resolving URN " + urn);

        String docID = MCRURNManager.getDocumentIDforURN(urn);

        if (docID == null)
            res.sendRedirect(masterURL + urn);
        else
            res.sendRedirect(documentURL + docID);
    }
}
