/*
 * 
 * $Revision: 15105 $ $Date: 2009-04-23 11:23:28 +0200 (Do, 23. Apr 2009) $
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

package org.mycore.frontend.indexbrowser;

import java.util.Enumeration;

import org.jdom.Document;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.frontend.servlets.MCRServletJob;

/**
 * @author Anja Schaar, Andreas Trappe
 * 
 * 
 */
public class MCRIndexBrowserServlet extends MCRServlet {

    private static final long serialVersionUID = 4963472470316616461L;

    protected void doGetPost(MCRServletJob job) throws Exception {
        @SuppressWarnings("unchecked")
        Enumeration<String> ee = job.getRequest().getParameterNames();
        while (ee.hasMoreElements()) {
            String param = ee.nextElement();
            System.out.println("PARAM: " + param + " VALUE: " + job.getRequest().getParameter(param));
        }

        String search = job.getRequest().getParameter("search");
        String mode = getMode(job);
        String searchclass = job.getRequest().getParameter("searchclass");
        String fromTo = job.getRequest().getParameter("fromTo");
        String path = job.getRequest().getParameter("path");

        MCRIndexBrowserData indexbrowser = new MCRIndexBrowserData(search, mode, searchclass, fromTo, path);
        indexbrowser.getQuery();
        indexbrowser.getResultList();
        Document pageContent = indexbrowser.getXMLContent();
        if (getProperty(job.getRequest(), "XSL.Style") == null) {
            job.getRequest().setAttribute("XSL.Style", searchclass);
        }
        getLayoutService().doLayout(job.getRequest(), job.getResponse(), pageContent);
    }

    private String getMode(MCRServletJob job) {
        if (job.getRequest().getParameter("mode") != null && !job.getRequest().getParameter("mode").trim().equals("")) {
            return job.getRequest().getParameter("mode").toLowerCase().trim();
        } else
            return "prefix";
    }
}
