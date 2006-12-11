/*
 * $RCSfile: WCMSServlet.java,v $
 * $Revision: 1.10 $ $Date: 2006/09/22 10:30:44 $
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

package wcms;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.mycore.common.MCRSessionMgr;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.frontend.servlets.MCRServletJob;

/**
 * @author Thomas Scheffler (yagee)
 * 
 * Need to insert some things here
 * 
 */
public abstract class WCMSServlet extends MCRServlet {
    protected static final String OUTPUT_ENCODING = "UTF-8";

    protected static final String VALIDATOR = "JTidy";

    /*
     * (non-Javadoc)
     * 
     * @see org.mycore.frontend.servlets.MCRServlet#doGetPost(org.mycore.frontend.servlets.MCRServletJob)
     */
    protected void doGetPost(MCRServletJob job) throws Exception {
        if (isValidUser()) {
        	processRequest(job.getRequest(), job.getResponse());
        } else {
       	    job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(CONFIG.getString("MCR.WCMS.sessionError")));     	 
        }
    }

    protected final boolean isValidUser() {
        String status = (String) MCRSessionMgr.getCurrentSession().get("status");

        return ((status != null) && status.equals("loggedIn"));
    }

    public Element getTemplates() {
        Element templates = new Element("templates");

        // content

        /*
         * File [] contentTemplates = new
         * File(super.CONFIG.getString("MCR.WCMS.templatePath")+"content/".replace('/',
         * File.separatorChar)).listFiles(); Element content = new
         * Element("content"); content.addContent(new
         * Element("template").setText(conTemp.toString()));
         */

        // master
        File[] masterTemplates = new File(CONFIG.getString("MCR.WCMS.templatePath") + "master/".replace('/', File.separatorChar)).listFiles();
        Element master = new Element("master");

        for (int i = 0; i < masterTemplates.length; i++) {
            if (masterTemplates[i].isDirectory() && (masterTemplates[i].getName().compareToIgnoreCase("cvs") != 0)) {
                master.addContent(new Element("template").setText(masterTemplates[i].getName()));
            }
        }

        // templates.addContent(content);
        templates.addContent(master);

        return templates;
    }

    final Document XMLFile2JDOM(String pathOfFile) throws IOException, JDOMException {
        File XMLFile = new File(pathOfFile);
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(XMLFile);

        return doc;
    }

    /*
     * final void WriteJDOM2XMLFile(Document doc, String pathOfFile) { }
     */
    protected abstract void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;
}
