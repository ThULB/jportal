/*
 * 
 * $Revision: 14665 $ $Date: 2009-01-29 07:41:35 +0100 (Do, 29. Jan 2009) $
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

import javax.servlet.ServletException;

import org.jdom.Document;
import org.mycore.common.MCRSessionMgr;

/**
 * This servlet provides some common methods for the editors of the user
 * management of the mycore system.
 * 
 * @author Detlev Degenhardt
 * @version $Revision: 14665 $ $Date: 2009-01-29 07:41:35 +0100 (Do, 29. Jan 2009) $
 */
public class MCRUserAdminGUICommons extends MCRServlet {

    private static final long serialVersionUID = 1L;

    protected String pageDir = null;

    protected String noPrivsPage = null;

    protected String cancelPage = null;

    protected String okPage = null;

    /** Initialisation of the servlet */
    public void init() throws ServletException {
        super.init();
        pageDir = CONFIG.getString("MCR.Useradmin.PageDir", "");
        noPrivsPage = pageDir + CONFIG.getString("MCR.Useradmin.Page.ErrorPrivileges", "useradmin_error_privileges.xml");
        cancelPage = pageDir + CONFIG.getString("MCR.Useradmin.Page.Cancel", "useradmin_cancel.xml");
        okPage = pageDir + CONFIG.getString("MCR.Useradmin.Page.OK", "useradmin_ok.xml");
    }

    /**
     * This method simply redirects to a page providing information that the
     * privileges for a use case are not sufficient.
     * 
     * @param job
     *            The MCRServletJob instance
     */
    protected void showNoPrivsPage(MCRServletJob job) throws IOException {
        job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + noPrivsPage));

        return;
    }

    /**
     * This method simply redirects to a page providing information that the
     * current use case was fulfilled successfully.
     * 
     * @param job
     *            The MCRServletJob instance
     */
    protected void showOkPage(MCRServletJob job) throws IOException {
        job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + okPage));

        return;
    }

    /**
     * Gather information about the XML document to be shown and the
     * corresponding XSLT stylesheet and redirect the request to the
     * LayoutService
     * 
     * @param job
     *            The MCRServletJob instance
     * @param styleSheet
     *            String value to select the correct XSL stylesheet
     * @param jdomDoc
     *            The XML representation to be presented by the LayoutService
     * @param useStrict
     *            If true, the parameter styleSheet must be used directly as
     *            name of a stylesheet when forwarding to the MCRLayoutService.
     *            If false, styleSheet will be appended by the signature of the
     *            current language. useStrict=true is used when not using a
     *            stylesheet at all because one simply needs the raw XML output.
     * 
     * @throws ServletException
     *             for errors from the servlet engine.
     * @throws IOException
     *             for java I/O errors.
     */
    protected void doLayout(MCRServletJob job, String styleSheet, Document jdomDoc, boolean useStrict) throws IOException {
        String language = MCRSessionMgr.getCurrentSession().getCurrentLanguage();

        if (!useStrict) {
            styleSheet = styleSheet + "-" + language;
        }

        job.getRequest().getSession().setAttribute("mycore.language", language);
        job.getRequest().setAttribute("XSL.Style", styleSheet);
        getLayoutService().doLayout(job.getRequest(), job.getResponse(), jdomDoc);
    }
}
