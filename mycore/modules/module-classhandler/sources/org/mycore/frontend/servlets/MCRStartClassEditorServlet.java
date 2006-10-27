/**
 * $RCSfile: MCRStartClassEditorServlet.java,v $
 * $Revision: 1.5 $ $Date: 2006/09/26 09:53:49 $
 *
 * This file is part of ** M y C o R e **
 * Visit our homepage at http://www.mycore.de/ for details.
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
 * along with this program, normally in the file license.txt.
 * If not, write to the Free Software Foundation Inc.,
 * 59 Temple Place - Suite 330, Boston, MA  02111-1307 USA
 *
 **/

package org.mycore.frontend.servlets;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Enumeration;

import java.util.Properties;

import org.apache.commons.fileupload.FileItem;
import org.apache.log4j.Logger;
import org.jdom.output.XMLOutputter;
import org.mycore.common.MCRSession;
import org.mycore.common.MCRSessionMgr;
import org.mycore.datamodel.classifications.MCRClassificationEditor;
import org.mycore.frontend.editor.MCREditorSubmission;
import org.mycore.frontend.editor.MCRRequestParameters;

/**
 * The servlet start the MyCoRe class editor session with some parameters from a
 * HTML form. The parameters are:<br />
 * <li> name="todo" values like 'create-classification, modify-classification,
 * delete-classification, up and down' </li>
 * <li> name="path" uri to page after editactions </li>
 * <li> name="clid" classification id </li>
 * <li> name="categid" category id </li>
 * 
 * @author Anja Schaar
 * @author Jens Kupferschmidt
 * @version $Revision: 1.5 $ $Date: 2006/09/26 09:53:49 $
 */

public class MCRStartClassEditorServlet extends MCRServlet {

    private static final long serialVersionUID = 1L;

    private static Logger LOGGER = Logger.getLogger(MCRStartClassEditorServlet.class);

    private String todo = "";

    private String todo2 = "";

    private String clid = "";

    private String categid = "";

    private String path = "";

    private static MCRClassificationEditor clE = new MCRClassificationEditor();

    /**
     * Replace the doGetPost method of MCRServlet. This method will be called
     * two times when using the classification editor. Firtst time it prepare
     * date for the editor and second time it execute the operation.
     */
    public void doGetPost(MCRServletJob job) throws Exception {

        MCRSession session = MCRSessionMgr.getCurrentSession();
        
        // read the XML data if given from Editorsession
        MCREditorSubmission sub = (MCREditorSubmission) (job.getRequest().getAttribute("MCREditorSubmission"));

        // read the parameter
        MCRRequestParameters parms;
        if (sub == null)
            parms = new MCRRequestParameters(job.getRequest());
        else {
            parms = sub.getParameters();
        }

        // read the parameter
        todo = parms.getParameter("todo"); // getProperty(job.getRequest(),
        // "todo");
        todo2 = parms.getParameter("todo2"); // getProperty(job.getRequest(),
        // "todo2");
        path = parms.getParameter("path"); // getProperty(job.getRequest(),
        // "path");

        // get the Classification
        clid = parms.getParameter("clid"); // getProperty(job.getRequest(),
        // "clid");
        categid = parms.getParameter("categid"); // getProperty(job.getRequest(),
        // "categid");

        if (todo == null)
            todo = "";
        if (todo2 == null)
            todo2 = "";

        LOGGER.debug("MCRStartClassEditorServlet TODO: " + todo);
        LOGGER.debug("MCRStartClassEditorServlet TODO2: " + todo2);
        LOGGER.debug("MCRStartClassEditorServlet CLID: " + clid);
        LOGGER.debug("MCRStartClassEditorServlet CATEGID: " + categid);

        String pagedir = CONFIG.getString("MCR.classeditor_page_dir", "");
        String myfile = "editor_form_" + todo + ".xml";

        String usererrorpage = pagedir + CONFIG.getString("MCR.classeditor_page_error_user", "editor_error_user.xml");
        String cancelpage = pagedir + CONFIG.getString("MCR.classeditor_page_cancel", "classeditor_cancel.xml");
        String icerrorpage = pagedir + CONFIG.getString("MCR.classeditor_page_error_id", "classeditor_error_clid.xml");
        String iderrorpage = pagedir + CONFIG.getString("MCR.classeditor_page_error_delete", "editor_error_delete.xml");
        String imerrorpage = pagedir + CONFIG.getString("MCR.classeditor_page_error_move", "classeditor_error_move.xml");
        String imperrorpage = pagedir + CONFIG.getString("MCR.classeditor_page_error_import", "classeditor_error_import.xml");

        if (!(AI.checkPermission("create-classification"))) {
            job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + usererrorpage));
            return;
        }

        // nach Editoraufruf von new/modify auf commit
        if ("commit-classification".equals(todo)) {
            org.jdom.Document indoc = sub.getXML();
            boolean bret = false;

            // for debug
            XMLOutputter outputter = new XMLOutputter();
            LOGGER.debug(outputter.outputString(indoc));

            if ("create-category".equals(todo2) || "modify-category".equals(todo2)) {
                if ("create-category".equals(todo2)) {
                    // create
                    bret = clE.createCategoryInClassification(indoc, clid, categid);
                    session.BData.cleanClassificationFromCache(clid);
                } else {
                    // modify
                    bret = clE.modifyCategoryInClassification(indoc, clid, categid);
                    session.BData.cleanClassificationFromCache(clid);
                }
                if (bret)
                    job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(path + "&categid=" + categid + "&clid=" + clid));
            } else {
                if (path.indexOf("&clid") > 0) {
                    // Classification abschneiden um wieder auf der
                    // Classifikationsstartseite zu landen
                    path = path.substring(0, path.indexOf("&clid"));
                }
                if ("create-classification".equals(todo2))
                    bret = clE.createNewClassification(indoc);
                else if ("modify-classification".equals(todo2))
                    bret = clE.modifyClassificationDescription(indoc, clid);
                else if ("import-classification".equals(todo2)) {
                    String fname = parms.getParameter("/mycoreclass/pathes/path").trim();
                    fname = clE.setTempFile(fname, (FileItem) sub.getFiles().get(0));
                    String sUpdate = parms.getParameter("/mycoreclass/update");
                    bret = clE.importClassification(("true".equals(sUpdate)), fname);
                    clE.deleteTempFile();
                    if (!bret) {
                        job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + imperrorpage));
                        return;
                    }
                }
                if (bret)
                    session.BData.cleanClassificationFromCache(clid);
                    job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(path));

            }
            if (!bret)
                job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + icerrorpage));
            return;
        }

        if ("up-category".equals(todo) || "down-category".equals(todo) || "left-category".equals(todo) || "right-category".equals(todo)) {
            boolean bret = clE.moveCategoryInClassification(categid, clid, todo.substring(0, todo.indexOf("-")));
            if (bret) {
                session.BData.cleanClassificationFromCache(clid);
                job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(path + "&categid=" + categid + "&clid=" + clid));
            } else {
                job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + imerrorpage));
            }
            return;
        }

        // first call, direct without editor
        else if ("delete-category".equals(todo)) {
            // l�schen
            int cnt = clE.deleteCategoryInClassification(clid, categid);
            if (cnt == 0) { // deleted, no more references
                session.BData.cleanClassificationFromCache(clid);
                job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(path + "&clid=" + clid));
            } else { // not delete cause references exist
                job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + iderrorpage));
            }
            return;
        }

        // first call, direct without editor
        else if ("delete-classification".equals(todo)) {
            boolean cnt = clE.deleteClassification(clid);
            if (cnt) { // deleted, no more references
                if (path.indexOf("&clid") > 0) {
                    // Classification cut
                    path = path.substring(0, path.indexOf("&clid"));
                }
                session.BData.cleanClassificationFromCache(clid);
                job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(path));
            } else { // not delete cause references exist
                job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + iderrorpage));
            }
            return;
        }

        // first call of editor, build the import dialogue
        else if ("import-classification".equals(todo)) {
            String base = getBaseURL() + myfile;
            Properties params = new Properties();
            params.put("XSL.editor.source.new", "true");
            params.put("XSL.editor.cancel.url", getBaseURL() + cancelpage);
            params.put("clid", clid);
            params.put("path", path);
            params.put("todo2", todo);
            params.put("todo", "commit-classification");
            job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(buildRedirectURL(base, params)));
            return;

        }

        // first call of editor, build the editor dialogue
        if ("create-category".equals(todo) || "modify-category".equals(todo) || "create-classification".equals(todo) || "modify-classification".equals(todo)) {

            String base = getBaseURL() + myfile;
            Properties params = new Properties();
            StringBuffer sb = new StringBuffer();

            if ("modify-classification".equals(todo)) {
                sb.append("classification:metadata:0:children:").append(clid);
                params.put("XSL.editor.source.url", sb.toString());

            }
            if ("create-classification".equals(todo)) {
                params.put("XSL.editor.source.new", "true");
            }
            if ("modify-category".equals(todo)) {
                sb.append("classification:metadata:0:children:").append(clid).append(':').append(categid);
                params.put("XSL.editor.source.url", sb.toString());
                params.put("categid", categid);
            }
            if ("create-category".equals(todo)) {
                sb.append("classification:metadata:0:children:").append(clid);
                params.put("XSL.editor.source.url", sb.toString());
                params.put("categid", categid);
            }
            params.put("XSL.editor.cancel.url", getBaseURL() + cancelpage);
            params.put("clid", clid);
            params.put("path", path);
            params.put("todo2", todo);
            params.put("todo", "commit-classification");
            job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(buildRedirectURL(base, params)));
            return;
        }

        /* Wrong input data, write warning log */
        LOGGER.warn("MCRStartClassEditorServlet default Case - Nothing to do ? " + todo);
        job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(path));

    }

    private final String buildRedirectURL(String baseURL, Properties parameters) {
        StringBuffer redirectURL = new StringBuffer(baseURL);
        boolean first = true;
        for (Enumeration e = parameters.keys(); e.hasMoreElements();) {
            if (first) {
                redirectURL.append("?");
                first = false;
            } else
                redirectURL.append("&");

            String name = (String) (e.nextElement());
            String value = null;
            try {
                value = URLEncoder.encode(parameters.getProperty(name), "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                value = parameters.getProperty(name);
            }
            redirectURL.append(name).append("=").append(value);
        }
        LOGGER.debug("Sending redirect to " + redirectURL.toString());
        return redirectURL.toString();
    }
}
