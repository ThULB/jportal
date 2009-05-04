/*
 * 
 * $Revision: 15107 $ $Date: 2009-04-23 11:52:43 +0200 (Do, 23. Apr 2009) $
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

import java.util.List;

import org.jdom.Document;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.workflow.MCREditorOutValidator;
import org.mycore.frontend.workflow.MCRSimpleWorkflowManager;
import org.mycore.user.MCRUserMgr;

/**
 * This class is the superclass of servlets which checks the MCREditorServlet
 * output XML for metadata object and derivate objects.
 * 
 * @author Jens Kupferschmidt
 * @version $Revision: 15107 $ $Date: 2009-04-23 11:52:43 +0200 (Do, 23. Apr 2009) $
 */
abstract public class MCRCheckBase extends MCRServlet {

    private static final long serialVersionUID = 1L;

    // The file separator
    String NL = System.getProperty("file.separator");

    // The Workflow Manager
    protected static MCRSimpleWorkflowManager WFM = MCRSimpleWorkflowManager.instance();

    // The User Manager
    protected static MCRUserMgr UM = MCRUserMgr.instance();

    // pagedir
    protected static String pagedir = CONFIG.getString("MCR.SWF.PageDir", "");

    protected List<String> errorlog;

    protected static String usererrorpage = pagedir + CONFIG.getString("MCR.SWF.PageErrorUser", "editor_error_user.xml");

    /**
     * The method return an URL with the next working step. If okay flag is
     * true, the object will present else it shows the error page.
     * 
     * @param ID
     *            the MCRObjectID of the MCRObject
     * @param okay
     *            the return value of the store operation
     * @return the next URL as String
     */
    abstract protected String getNextURL(MCRObjectID ID, boolean okay) throws MCRActiveLinkException;

    /**
     * The method send a message to the mail address for the MCRObjectType.
     * 
     * @param ID
     *            the MCRObjectID of the MCRObject
     */
    abstract protected void sendMail(MCRObjectID ID);

    /**
     * A method to handle IO errors.
     * 
     * @param job
     *            the MCRServletJob
     */
    protected void errorHandlerIO(MCRServletJob job) throws Exception {
        String pagedir = CONFIG.getString("MCR.SWF.PageDir", "");
        String page = CONFIG.getString("MCR.SWF.PageErrorStore", "");
        job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + pagedir + page));
    }

    /**
     * provides a wrappe for editor validation and MCRObject creation.
     * 
     * For a new MetaDataType, e.g. MCRMetaFooBaar, create a method
     * 
     * <pre>
     *     boolean checkMCRMetaFooBar(Element)
     * </pre>
     * 
     * use the following methods in that method to do common tasks on element
     * validation
     * <ul>
     * <li>checkMetaObject(Element,Class)</li>
     * <li>checkMetaObjectWithLang(Element,Class)</li>
     * <li>checkMetaObjectWithLangNotEmpty(Element,Class)</li>
     * <li>checkMetaObjectWithLinks(Element,Class)</li>
     * </ul>
     * 
     * @author Thomas Scheffler (yagee)
     * 
     * @version $Revision: 15107 $ $Date: 2009-04-23 11:52:43 +0200 (Do, 23. Apr 2009) $
     */
    protected class EditorValidator extends MCREditorOutValidator {
        /**
         * instantiate the validator with the editor input <code>jdom_in</code>.
         * 
         * <code>id</code> will be set as the MCRObjectID for the resulting
         * object that can be fetched with
         * <code>generateValidMyCoReObject()</code>
         * 
         * @param jdom_in
         *            editor input
         */
        public EditorValidator(Document jdom_in, MCRObjectID id) {
            super(jdom_in, id);
        }

    }

    /**
     * check the access permission
     * @param ID the mycore ID
     * @return true if the access is set
     */
    protected boolean checkAccess(MCRObjectID ID) {
        return false;
    }

}
