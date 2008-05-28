/*
 * $RCSfile$
 * $Revision$ $Date$
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.mycore.access.MCRAccessManager;
import org.mycore.common.MCRConfiguration;
import org.mycore.common.MCRSession;
import org.mycore.common.MCRSessionMgr;
import org.mycore.user.MCRUserMgr;

public class MCRDublicateFinderServlet extends MCRServlet {

    private static final long serialVersionUID = 1L;

    private static Logger LOGGER = Logger.getLogger(MCRDublicateFinderServlet.class);;

    private static final String FS = System.getProperty("file.seperator", "/");

    private static final String ROOT_DIR = MCRConfiguration.instance().getString("MCR.basedir") + FS + "build" + FS + "webapps" + FS;

    public void init() throws ServletException {
        super.init();
    }

    public synchronized void doGetPost(MCRServletJob job) throws JDOMException, IOException {

        // init params
        MCRSession session = MCRSessionMgr.getCurrentSession();
        String redMap = (String) session.get("XSL.RedunMap");
        String redMapPath = ROOT_DIR + redMap;

        // get redun list
        SAXBuilder builder = new SAXBuilder();
        Document redunMap = builder.build(redMapPath);
        LOGGER.debug("read dublicate list from file=" + redMapPath);

        // get dublicate entry
        String dublicateID = job.getRequest().getParameter("dublicate-id").trim();
        String xPathExpr = "//redundancyID[text()='" + dublicateID + "']";
        LOGGER.debug("finding double entry with xpath=" + xPathExpr);
        org.jdom.xpath.XPath xp = org.jdom.xpath.XPath.newInstance(xPathExpr);
        Element doublet = (Element) xp.selectSingleNode(redunMap);
        String id = doublet.getTextTrim();
        LOGGER.debug("received double entry=" + id);

        // edit doublet entry
        if (MCRAccessManager.checkPermission(id, "writedb") && doubletChangeable(job, doublet)) {
            // change mode of doublet
            String status = job.getRequest().getParameter("status");
            String user = session.getCurrentUserID();
            String userRealName = MCRUserMgr.instance().retrieveUser(user).getUserContact().getFirstName() + " "
                            + MCRUserMgr.instance().retrieveUser(user).getUserContact().getLastName();
            long time = System.currentTimeMillis();
            java.util.Date date = new java.util.Date(time);
            doublet.setAttribute("status", status);
            doublet.setAttribute("user", user);
            doublet.setAttribute("userRealName", userRealName);
            doublet.setAttribute("time", Long.toString(time));
            doublet.setAttribute("timePretty", date.toGMTString());
            LOGGER.info("changed mode of doublet=" + id + " to status=" + status + ", user=" + user + ", date=" + date);

            // save redun list
            Format format = Format.getPrettyFormat();
            FileOutputStream fos = new FileOutputStream(new File(redMapPath));
            XMLOutputter xo = new XMLOutputter(format);
            xo.output(redunMap, fos);
            fos.flush();
            fos.close();
            LOGGER.debug("saved changed  dublicate list to file=" + redMapPath);
        } else
            LOGGER.info("NOT changed mode of doublet=" + id + ", because already changed or no permission");

        // send to client
        forwardToClient(job, id);
    }

    /**
     * @param job
     * @param id
     * @throws IOException
     */
    private synchronized void forwardToClient(MCRServletJob job, String id) throws IOException {
        String redunMapURL = job.getRequest().getParameter("redunMap");
        String ankerPosition = job.getRequest().getParameter("ankerPosition");
        String returnURL = MCRConfiguration.instance().getString("MCR.baseurl") + redunMapURL + "#" + ankerPosition;
        job.getResponse().sendRedirect(returnURL);
    }

    /**
     * @param job
     * @param doublet
     * @param id
     * @return
     */
    private synchronized boolean doubletChangeable(MCRServletJob job, Element doublet) {
        if (job.getRequest().getParameter("status") != null && !job.getRequest().getParameter("status").equals("open")
                        && doublet.getAttribute("status") != null && !doublet.getAttributeValue("status").equals("open"))
            return false;
        return true;
    }
}
