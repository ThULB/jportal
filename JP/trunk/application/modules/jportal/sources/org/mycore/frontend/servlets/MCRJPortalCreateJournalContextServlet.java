/*
 * $RCSfile$
 * $Revision: 727 $ $Date: 2009-02-11 09:23:44 +0100 (Wed, 11 Feb 2009) $
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
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.mycore.access.MCRAccessManager;
import org.mycore.common.MCRException;
import org.mycore.common.MCRSession;
import org.mycore.common.MCRSessionMgr;
import org.mycore.frontend.MCRJPortalJournalContextForUserManagement;
import org.mycore.frontend.MCRJPortalJournalContextForWebpages;
import org.mycore.user.MCRUserMgr;

public class MCRJPortalCreateJournalContextServlet extends MCRServlet {

    private static final long serialVersionUID = 1L;

    private static final String JOURNAL_ID = "XSL.MCR.JPortal.Create-JournalContext.ID";

    private static Logger LOGGER = Logger.getLogger(MCRJPortalCreateJournalContextServlet.class);

    public void init() throws ServletException {
        super.init();
    }

    public void doGetPost(MCRServletJob job) throws JDOMException, IOException {
        if (!access())
            throw new MCRException("Access denied. Please authorise yourself.");

        // get requested mode
        String mode = null;
        if (job.getRequest().getParameter("mode") != null && !job.getRequest().getParameter("mode").equals(""))
            mode = job.getRequest().getParameter("mode");
        else
            throw new MCRException("Request can't be processed, because no 'mode' parameter given.");

        // dispatch mode
        if (mode.equals("createContext"))
            createContext(job);
        else if (mode.equals("getUsers"))
            getLayoutService().sendXML(job.getRequest(), job.getResponse(), new Document(getUsers()));
        else if (mode.equals("getGroups"))
            getLayoutService().sendXML(job.getRequest(), job.getResponse(), new Document(getGroups()));

    }

    public void createContext(MCRServletJob job) {
        LOGGER.info("create journal context");
        HttpServletRequest req = job.getRequest();

        // create webpages
        LOGGER.debug("start creating webpages");
        String jid = getJournalID();
        String precHref = req.getParameter("jp.cjc.preceedingItemHref");
        String shortCut = req.getParameter("jp.cjc.shortCut");
        String layoutTemplate;
        if (req.getParameter("jp.cjc.layoutTemplate").equals("default"))
            layoutTemplate = "template_" + shortCut;
        else
            layoutTemplate = req.getParameter("jp.cjc.layoutTemplate");
        MCRJPortalJournalContextForWebpages wc = new MCRJPortalJournalContextForWebpages(jid, precHref, layoutTemplate, shortCut);
        try {
            wc.create();
        } catch (MCRException exception) {
            try {
                generateErrorPage(job.getRequest(), job.getResponse(), 500, exception.getMessage(), exception, true);
                return;
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }

        // setup user, groups and assign acl's
        LOGGER.debug("start setting up user management");
        MCRJPortalJournalContextForUserManagement uc = new MCRJPortalJournalContextForUserManagement(jid, shortCut);
        String reqKeyUsersTOC = "jp.cjc.usersTOC";
        String reqKeyUsersArt = "jp.cjc.usersART";
        String reqKeyusersALL = "jp.cjc.usersALL";
        String reqKeygroup = "jp.cjc.group";
        if (req.getParameter(reqKeyUsersTOC) != null && req.getParameterValues(reqKeyUsersTOC).length > 0)
            uc.setUserListTOC(req.getParameterValues(reqKeyUsersTOC));
        if (req.getParameter(reqKeyUsersArt) != null && req.getParameterValues(reqKeyUsersArt).length > 0)
            uc.setUserListArt(req.getParameterValues(reqKeyUsersArt));
        if (req.getParameter(reqKeyusersALL) != null && req.getParameterValues(reqKeyusersALL).length > 0)
            uc.setUserListTOCArt(req.getParameterValues(reqKeyusersALL));
        if (req.getParameter(reqKeygroup) != null && req.getParameterValues(reqKeygroup).length > 0)
            uc.setGroup(req.getParameterValues(reqKeygroup));
        uc.setup();

        // forward to journal page
        try {
            String forwardURL = super.getBaseURL() + "receive/" + getJournalID();
            job.getResponse().sendRedirect(forwardURL);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param session
     * @return
     */
    private String getJournalID() {
        MCRSession session = MCRSessionMgr.getCurrentSession();
        String jid = (String) session.get(JOURNAL_ID);
        return jid;
    }

    private boolean access() {
        String jid = getJournalID();
        if (jid != null && MCRAccessManager.checkPermission(jid, "writedb"))
            return true;
        else
            return false;
    }

    public Element getUsers() {
        Element users = new Element("users");
        MCRUserMgr um = MCRUserMgr.instance();
        List<String> ul = um.getAllUserIDs();
        Iterator<String> ulIt = ul.iterator();
        while (ulIt.hasNext()) {
            String userID = (String) ulIt.next();
            String userName = um.retrieveUser(userID).getUserContact().getFirstName() + " " + um.retrieveUser(userID).getUserContact().getLastName();
            Element userElem = new Element("user").setAttribute("id", userID).setText(userName);
            users.addContent(userElem);
        }
        return users;
    }
    
    public Element getGroups() {
        Element users = new Element("groups");
        MCRUserMgr um = MCRUserMgr.instance();
        // grouplist
        List<String> gl = um.getAllGroupIDs();
        Iterator<String> glIt = gl.iterator();
        while (glIt.hasNext()) {
            String groupID = (String) glIt.next();
            Element userElem = new Element("group").setAttribute("id", groupID);
            users.addContent(userElem);
        }
        return users;
    }
}


















