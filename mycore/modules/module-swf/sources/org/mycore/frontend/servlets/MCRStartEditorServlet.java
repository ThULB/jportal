/*
 * $RCSfile: MCRStartEditorServlet.java,v $
 * $Revision: 1.50 $ $Date: 2006/11/27 12:35:18 $
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
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.jdom.Element;

import org.mycore.access.MCRAccessManager;
import org.mycore.common.MCRException;
import org.mycore.common.MCRMailer;
import org.mycore.common.MCRSession;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.MCRUtils;
import org.mycore.datamodel.ifs.MCRDirectory;
import org.mycore.datamodel.metadata.MCRActiveLinkException;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.editor.MCREditorServletHelper;
import org.mycore.frontend.fileupload.MCRUploadHandlerIFS;
import org.mycore.frontend.fileupload.MCRUploadHandlerMyCoRe;
import org.mycore.frontend.workflow.MCRSimpleWorkflowManager;

/**
 * The servlet start the MyCoRe editor session or other workflow actions with
 * some parameters from a HTML form. The parameters are: <br />
 * <li>type - the MCRObjectID type like schrift, text ...</li>
 * <br />
 * <li>step - the name of the step like author, editor ...</li>
 * <br />
 * <li>layout - the name of the layout like firststep, secondstep ...</li>
 * <br />
 * <li>todo - the mode of the editor start like new or edit or change or delete
 * </li>
 * <br />
 * <li>tf_mcrid - the MCRObjectID of the data they came from a input field
 * </li>
 * <br />
 * <li>se_mcrid - the MCRObjectID of the data they came from a select field
 * </li>
 * <br />
 * <li>re_mcrid - the MCRObjectID of the data they is in relation to
 * tf_mcrid/se_mcrid</li>
 * <br />
 * 
 * @author Jens Kupferschmidt
 * @version $Revision: 1.50 $ $Date: 2006/11/27 12:35:18 $
 */
public class MCRStartEditorServlet extends MCRServlet {

    private static final long serialVersionUID = 1L;

    // The configuration
    protected static Logger LOGGER = Logger.getLogger(MCRStartEditorServlet.class);

    // The workflow manager
    protected static MCRSimpleWorkflowManager WFM = null;

    // The file slash
    protected static String SLASH = System.getProperty("file.separator");;

    // static pages
    protected static String pagedir = CONFIG.getString("MCR.editor_page_dir", "");

    protected static String cancelpage = pagedir + CONFIG.getString("MCR.editor_page_cancel", "editor_cancel.xml");

    protected static String deletepage = pagedir + CONFIG.getString("MCR.editor_page_delete", "editor_delete.xml");

    protected static String usererrorpage = pagedir + CONFIG.getString("MCR.editor_page_error_user", "editor_error_user.xml");

    protected static String mcriderrorpage = pagedir + CONFIG.getString("MCR.editor_page_error_mcrid", "editor_error_mcrid.xml");

    protected static String storeerrorpage = pagedir + CONFIG.getString("MCR.editor_page_error_store", "editor_error_store.xml");

    protected static String deleteerrorpage = pagedir + CONFIG.getString("MCR.editor_page_error_delete", "editor_error_delete.xml");

    // common data
    protected String mystep = null; // the special step for todo

    protected String mytype = null; // the metadata type

    protected String myfile = null; // the formular file to be called

    protected String mytfmcrid = null; // the metadata ID

    protected String mysemcrid = null; // the metadata ID

    protected MCRObjectID mcrmysemcrid = null;

    protected String myremcrid = null; // the metadata ID

    protected String extparm = null; // the extra parameter

    /** Initialisation of the servlet */
    public void init() throws ServletException {
        super.init();

        // Workflow Manager
        WFM = MCRSimpleWorkflowManager.instance();

    }

    /**
     * This method overrides doGetPost of MCRServlet. <br />
     * <br />
     * The <b>todo </b> value corresponds with <b>tf_mcrid</b> or <b>se_mcridor</b>
     * value and with the type of the data model for the permissions that the
     * user need. For some actions you need a third value of <b>re_mcrid</b>
     * for relations (object - derivate). <br />
     * 
     * <li>If the permission is not correct it calls
     * <em>editor_error_user.xml</em>.</li>
     * <br />
     * <li>If the MCRObjectID is not correct it calls
     * <em>editor_error_mcrid.xml</em>.</li>
     * <br />
     * <li>If a store error is occured it calls <em>editor_error_store.xml</em>.
     * </li>
     * <br />
     * <li>If <b>CANCEL </b> was pressed it calls <em>editor_cancel.xml</em>.
     * </li>
     * <br />
     * <li>If the permission is correct it starts the file editor_form_
     * <em>step-type</em> .xml.</li>
     * <br />
     */
    public void doGetPost(MCRServletJob job) throws Exception {
        // get the MCRSession object for the current thread from the session
        // manager.
        MCRSession mcrSession = MCRSessionMgr.getCurrentSession();
        
        //Store Request-Params="*.SESSION" in MCRSession
        putParamsToSession(job.getRequest());
        
        // get the current language
        String mylang = mcrSession.getCurrentLanguage();
        LOGGER.info("LANG = " + mylang);

        // read the parameter
        // get the step
        mystep = getProperty(job.getRequest(), "step");
        if (mystep == null) {
            mystep = "";
        }
        LOGGER.info("STEP = " + mystep);

        // get the type
        mytype = getProperty(job.getRequest(), "type");
        if (mytype == null) {
            mytype = CONFIG.getString("MCR.default_project_type", "document");
        }
        if (mytype.length() == 0) {
            mytype = CONFIG.getString("MCR.default_project_type", "document");
        }
        LOGGER.info("TYPE = " + mytype);

        // get the layout
        String mylayout = getProperty(job.getRequest(), "layout");
        if (mylayout == null) {
            mylayout = "";
        }
        LOGGER.info("LAYOUT = " + mylayout);

        // get what is to do
        String mytodo = getProperty(job.getRequest(), "todo");
        if ((mytodo == null) || ((mytodo = mytodo.trim()).length() == 0)) {
            mytodo = "wrongtodo";
        }
        LOGGER.info("TODO = " + mytodo);

        // get the MCRObjectID from the text filed (TF)
        mytfmcrid = getProperty(job.getRequest(), "tf_mcrid");
        try {
            new MCRObjectID(mytfmcrid);
        } catch (Exception e) {
            mytfmcrid = "";
        }

        if ((mytfmcrid == null) || ((mytfmcrid = mytfmcrid.trim()).length() == 0)) {
            String defaproject = CONFIG.getString("MCR.default_project_id", "MCR");
            String myproject = CONFIG.getString("MCR." + mytype + "_project_id", "MCR");

            if (myproject.equals("MCR")) {
                myproject = defaproject;
            }

            myproject = myproject + "_" + mytype;

            MCRObjectID mcridnext = new MCRObjectID();
            mcridnext.setNextFreeId(myproject);

            String workdir = CONFIG.getString("MCR.editor_" + mytype + "_directory", "/");
            File workf = new File(workdir);

            if (workf.isDirectory()) {
                String[] list = workf.list();

                for (int i = 0; i < list.length; i++) {
                    if (!list[i].startsWith(myproject)) {
                        continue;
                    }

                    try {
                        MCRObjectID mcriddir = new MCRObjectID(list[i].substring(0, list[i].length() - 4));

                        if (mcridnext.getNumberAsInteger() <= mcriddir.getNumberAsInteger()) {
                            mcriddir.setNumber(mcriddir.getNumberAsInteger() + 1);
                            mcridnext = mcriddir;
                        }
                    } catch (Exception e) {
                    }
                }
            }

            mytfmcrid = mcridnext.getId();
        }
        LOGGER.info("MCRID (TF) = " + mytfmcrid);

        // get the MCRObjectID from the selcet field (SE)
        mysemcrid = getProperty(job.getRequest(), "se_mcrid");
        if (mysemcrid == null) {
            mysemcrid = "";
        } else {
            try {
                mcrmysemcrid = new MCRObjectID(mysemcrid);
            } catch (Exception e) {
                mysemcrid = "";
            }
        }
        LOGGER.info("MCRID (SE) = " + mysemcrid);

        // get the MCRObjectID from the relation field (RE)
        myremcrid = getProperty(job.getRequest(), "re_mcrid");
        if (myremcrid == null) {
            myremcrid = "";
        } else {
            try {
                new MCRObjectID(myremcrid);
            } catch (Exception e) {
                myremcrid = "";
            }
        }
        LOGGER.info("MCRID (RE) = " + myremcrid);

        // appending parameter
        extparm = getProperty(job.getRequest(), "extparm");
        LOGGER.info("EXTPARM = " + extparm);

        LOGGER.debug("Base URL : " + getBaseURL());

        // set the pages
        StringBuffer sb = new StringBuffer();
        sb.append(pagedir).append("editor_form_").append(mystep).append('-').append(mytype);
        if (mylayout.length() != 0) {
            sb.append('-').append(mylayout);
        }
        myfile = sb.append(".xml").toString();

        // call method named like todo
        Method meth[] = this.getClass().getMethods();
        for (int i = 0; i < meth.length; i++) {
            LOGGER.debug("Methods for SWF " + meth[i].getName());
        }
        try {
            Method method = this.getClass().getMethod(mytodo, new Class[] { job.getClass() });
            method.invoke(this, new Object[] { job });
            return;
        } catch (Exception e) {
            LOGGER.error("Error while execution of method " + mytodo);
            e.printStackTrace();
        }

        sb = new StringBuffer();
        sb.append(getBaseURL()).append("index.html");
        job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(sb.toString()));
    }

    /**
     * Builds an url that can be used to redirect the client browser to another
     * page, including http request parameters. The request parameters will be
     * encoded as http get request.
     * 
     * @param baseURL
     *            the base url of the target webpage
     * @param parameters
     *            the http request parameters
     */
    protected String buildRedirectURL(String baseURL, Properties parameters) {
        StringBuffer redirectURL = new StringBuffer(baseURL);
        boolean first = true;

        for (Enumeration e = parameters.keys(); e.hasMoreElements();) {
            if (first) {
                redirectURL.append("?");
                first = false;
            } else {
                redirectURL.append("&");
            }

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

    /**
     * The method start the editor add a file to a derivate object that is
     * stored in the server. The method use the input parameter: <b>type</b>,<b>step</b>
     * <b>se_mcrid</b> and <b>re_mcrid</b>. Access rigths must be 'writedb'.
     * 
     * @param job
     *            the MCRServletJob instance
     */
    public void saddfile(MCRServletJob job) throws IOException {
        if (!MCRAccessManager.checkPermission(myremcrid, "writedb")) {
            job.getResponse().sendRedirect(getBaseURL() + usererrorpage);
            return;
        }

        StringBuffer sb = new StringBuffer(getBaseURL()).append("receive/").append(myremcrid);
        MCRUploadHandlerIFS fuh = new MCRUploadHandlerIFS(myremcrid, mysemcrid, sb.toString());
        String fuhid = fuh.getID();
        myfile = pagedir + "fileupload_commit.xml";

        String base = getBaseURL() + myfile;
        Properties params = new Properties();
        params.put("XSL.UploadID", fuhid);
        params.put("XSL.editor.source.new", "true");
        params.put("XSL.editor.cancel.url", getBaseURL() + cancelpage);
        params.put("XSL.target.param.1", "method=formBasedUpload");
        params.put("XSL.target.param.2", "uploadId=" + fuhid);
        params.put("mcrid", mysemcrid);
        params.put("type", mytype);
        params.put("step", mystep);
        params.put("remcrid", myremcrid);
        job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(buildRedirectURL(base, params)));
    }

    /**
     * The method delete a derivate object that is stored in the server. The
     * method use the input parameter: <b>type</b>,<b>step</b> and
     * <b>tf_mcrid</b>. Access rigths must be 'writedb'.
     * 
     * @param job
     *            the MCRServletJob instance
     */
    public void sdelder(MCRServletJob job) throws IOException {
        if (!MCRAccessManager.checkPermission(myremcrid, "deletedb")) {
            job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + usererrorpage));
            return;
        }
        if (mysemcrid.length() == 0) {
            job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + mcriderrorpage));
            return;
        }

        MCRDerivate der = new MCRDerivate();

        try {
            der.deleteFromDatastore(mysemcrid);

            MCRObjectID ID = new MCRObjectID(myremcrid);
            StringBuffer sb = (new StringBuffer("MCR.type_")).append(ID.getTypeId()).append("_in");

            String searchtype = CONFIG.getString(sb.toString(), ID.getTypeId());
            sb = new StringBuffer();
            sb.append("receive/").append(myremcrid);
            myfile = sb.toString();
        } catch (Exception e) {
            myfile = deleteerrorpage;
        }

        List addr = WFM.getMailAddress(mytype, "sdelder");

        if (addr.size() != 0) {
            String sender = WFM.getMailSender();
            String appl = CONFIG.getString("MCR.editor_mail_application_id", "DocPortal");
            String subject = "Automaticaly message from " + appl;
            StringBuffer text = new StringBuffer();
            text.append("Es wurde ein Derivate mit der ID ").append(mysemcrid).append(" des Objektes mit der ID ").append(mysemcrid).append(" aus dem Server gel�scht.");
            LOGGER.info(text.toString());

            try {
                MCRMailer.send(sender, addr, subject, text.toString(), false);
            } catch (Exception ex) {
                LOGGER.error("Can't send a mail to " + addr);
            }
        }
        job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + myfile));
    }

    /**
     * The method delete a file from a derivate object that is stored in the
     * server. The method use the input parameter: <b>type</b>,<b>step</b>
     * <b>se_mcrid</b> and <b>re_mcrid</b>. Access rigths must be 'deletedb'.
     * 
     * @param job
     *            the MCRServletJob instance
     */
    public void sdelfile(MCRServletJob job) throws IOException {
        if (!MCRAccessManager.checkPermission(myremcrid, "deletedb")) {
            job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + usererrorpage));
            return;
        }
        if (mysemcrid.length() == 0) {
            job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + mcriderrorpage));
            return;
        }

        int all = 0;

        int i = extparm.indexOf("####nrall####");
        int j = 0;

        if (i != -1) {
            j = extparm.indexOf("####", i + 13);
            all = Integer.parseInt(extparm.substring(i + 13, j));
        }

        i = extparm.indexOf("####nrthe####");

        if (i != -1) {
            j = extparm.indexOf("####", i + 13);
            Integer.parseInt(extparm.substring(i + 13, j));
        }

        if (all > 1) {
            i = extparm.indexOf("####filename####");

            if (i != -1) {
                String filename = extparm.substring(i + 16, extparm.length());

                try {
                    MCRDirectory rootdir = MCRDirectory.getRootDirectory(mysemcrid);
                    rootdir.getChildByPath(filename).delete();
                } catch (Exception ex) {
                    LOGGER.warn("Can't remove file " + filename, ex);
                }
            }
        }

        StringBuffer sb = new StringBuffer();
        sb.append(getBaseURL()).append("servlets/MCRFileNodeServlet/").append(mysemcrid).append("/?hosts=local");
        job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(sb.toString()));
    }

    /**
     * The method delete a metadata object that is stored in the server. The
     * method use the input parameter: <b>type</b>,<b>step</b> and
     * <b>tf_mcrid</b>. Access rigths must be 'writedb'.
     * 
     * @param job
     *            the MCRServletJob instance
     */
    public void sdelobj(MCRServletJob job) throws IOException {
        if (!MCRAccessManager.checkPermission(mytfmcrid, "deletedb")) {
            job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + usererrorpage));
            return;
        }
        if (mytfmcrid.length() == 0) {
            job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + mcriderrorpage));
            return;
        }

        MCRObject obj = new MCRObject();

        try {
            obj.deleteFromDatastore(mytfmcrid);
            myfile = deletepage;
        } catch (Exception e) {
            myfile = deleteerrorpage;
        }

        List addr = WFM.getMailAddress(mytype, "sdelobj");

        if (addr.size() != 0) {
            String sender = WFM.getMailSender();
            String appl = CONFIG.getString("MCR.editor_mail_application_id", "DocPortal");
            String subject = "Automaticaly message from " + appl;
            StringBuffer text = new StringBuffer();
            text.append("Es wurde ein Objekt vom Typ ").append(mytype).append(" mit der ID ").append(mytfmcrid).append(" aus dem Server gel�scht.");
            LOGGER.info(text.toString());

            try {
                MCRMailer.send(sender, addr, subject, text.toString(), false);
            } catch (Exception ex) {
                LOGGER.error("Can't send a mail to " + addr);
            }
        }
        job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + myfile));
    }

    /**
     * The method start the editor to modify ACL of a metadata object that is
     * stored in the server. The method use the input parameter: <b>type</b>,<b>step</b>
     * and <b>tf_mcrid</b>. Access rigths must be 'writedb'.
     * 
     * @param job
     *            the MCRServletJob instance
     */
    public void seditacl(MCRServletJob job) throws IOException {
        if (!MCRAccessManager.checkPermission(mysemcrid, "writedb")) {
            job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + usererrorpage));
            return;
        }
        if (mysemcrid.length() == 0) {
            job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + mcriderrorpage));
            return;
        }

        // read object
        org.jdom.Element serviceelm = null;
        MCRObject obj = new MCRObject();
        obj.receiveFromDatastore(mysemcrid);
        List permlist = AI.getPermissionsForID(mysemcrid);
        for (int i = 0; i < permlist.size(); i++) {
            org.jdom.Element ruleelm = AI.getRule(mysemcrid, (String) permlist.get(i));
            org.jdom.Document dof = new org.jdom.Document();
            org.jdom.Element eof = new org.jdom.Element("test");
            dof.addContent(eof);
            eof.addContent((org.jdom.Element) ruleelm.detach().clone());
            byte[] xml = MCRUtils.getByteArray(dof);
            System.out.println(new String(xml));
            obj.getService().addRule((String) permlist.get(i), ruleelm);
        }
        serviceelm = obj.getService().createXML();

        MCRSession session = MCRSessionMgr.getCurrentSession();
        session.put("service", serviceelm);
        String base = getBaseURL() + myfile;
        Properties params = new Properties();
        params.put("XSL.editor.source.url", "session:service");
        params.put("XSL.editor.cancel.url", getBaseURL() + cancelpage);
        params.put("mcrid", mysemcrid);
        params.put("type", mytype);
        params.put("step", mystep);
        job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(buildRedirectURL(base, params)));
    }

    /**
     * The method start the editor to modify a derivate object that is stored in
     * the server. The method use the input parameter: <b>type</b>,<b>step</b>
     * <b>se_mcrid</b> and <b>se_mcrid</b>. Access rigths must be 'writedb'.
     * 
     * @param job
     *            the MCRServletJob instance
     */
    public void seditder(MCRServletJob job) throws IOException {
        if (!MCRAccessManager.checkPermission(mysemcrid, "writedb")) {
            job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + usererrorpage));
            return;
        }
        if (mysemcrid.length() == 0) {
            job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + mcriderrorpage));
            return;
        }

        MCRDerivate der = new MCRDerivate();
        der.receiveFromDatastore(mysemcrid);

        org.jdom.Element textfield = new org.jdom.Element("textfield");
        org.jdom.Element defa = new org.jdom.Element("default");
        defa.setText(der.getLabel());
        textfield.addContent(defa);
        MCRSessionMgr.getCurrentSession().put("seditder", textfield);
        StringBuffer sb = new StringBuffer();
        sb.append(getBaseURL()).append(pagedir).append("editor_form_commit-derivate.xml");

        Properties params = new Properties();
        params.put("XSL.editor.source.new", "true");
        params.put("XSL.editor.cancel.url", getBaseURL() + cancelpage);
        params.put("se_mcrid", mysemcrid);
        params.put("re_mcrid", myremcrid);
        params.put("type", mytype);
        params.put("step", mystep);
        job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(buildRedirectURL(sb.toString(), params)));
    }

    /**
     * The method start the editor to modify a metadata object that is stored in
     * the server. The method use the input parameter: <b>type</b>,<b>step</b>
     * and <b>tf_mcrid</b>. Access rigths must be 'writedb'.
     * 
     * @param job
     *            the MCRServletJob instance
     */
    public void seditobj(MCRServletJob job) throws IOException {
        if (!MCRAccessManager.checkPermission(mytfmcrid, "writedb")) {
            job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + usererrorpage));
            return;
        }
        if (mytfmcrid.length() == 0) {
            job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + mcriderrorpage));
            return;
        }

        StringBuffer sb = new StringBuffer();
        sb.append(getBaseURL()).append("receive/").append(mytfmcrid).append("?XSL.Style=editor");

        String base = getBaseURL() + myfile;
        Properties params = new Properties();
        params.put("XSL.editor.source.url", sb.toString());
        params.put("XSL.editor.cancel.url", getBaseURL() + cancelpage);
        params.put("mcrid", mytfmcrid);
        params.put("type", mytype);
        params.put("step", mystep);
        job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(buildRedirectURL(base, params)));
    }

    /**
     * The method set a new derivate object that is stored in the server. The
     * method use the input parameter: <b>type</b>,<b>step</b> <b>se_mcrid</b>
     * and <b>re_mcrid</b>. Access rigths must be 'writedb'.
     * 
     * @param job
     *            the MCRServletJob instance
     */
    public void snewder(MCRServletJob job) throws IOException {
        if (!MCRAccessManager.checkPermission(myremcrid, "writedb")) {
            job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + usererrorpage));
            return;
        }

        mysemcrid = WFM.createDerivateInServer(myremcrid);
        mystep = "addfile";
        saddfile(job);
    }

    /**
     * The method set the main file of a derivate object that is stored in the
     * server. The method use the input parameter: <b>type</b>,<b>step</b>
     * <b>se_mcrid</b> and <b>re_mcrid</b>. Access rigths must be 'writedb'.
     * 
     * @param job
     *            the MCRServletJob instance
     */
    public void ssetfile(MCRServletJob job) throws IOException {
        if (!MCRAccessManager.checkPermission(myremcrid, "writedb")) {
            job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + usererrorpage));
            return;
        }
        if (mysemcrid.length() == 0) {
            job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + mcriderrorpage));
            return;
        }

        MCRDerivate der = new MCRDerivate();
        der.receiveFromDatastore(mysemcrid);
        der.getDerivate().getInternals().setMainDoc(extparm);

        try {
            der.updateXMLInDatastore();
        } catch (MCRException ex) {
            LOGGER.error("Exception while store to derivate " + mysemcrid);
        }

        StringBuffer sb = new StringBuffer();
        sb.append(getBaseURL()).append("servlets/MCRFileNodeServlet/").append(mysemcrid).append("/?hosts=local");
        job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(sb.toString()));
    }

    /**
     * The method set the label of a derivate object that is stored in the
     * server. The method use the input parameter: <b>type</b>,<b>step</b>
     * <b>se_mcrid</b> and <b>re_mcrid</b>. Access rigths must be 'writedb'.
     * 
     * @param job
     *            the MCRServletJob instance
     */
    public void ssetlabel(MCRServletJob job) throws IOException {
        if (!MCRAccessManager.checkPermission(myremcrid, "writedb")) {
            job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + usererrorpage));
            return;
        }
        if (mysemcrid.length() == 0) {
            job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + mcriderrorpage));
            return;
        }

        MCRDerivate der = new MCRDerivate();
        der.receiveFromDatastore(mysemcrid);
        der.setLabel(extparm);

        try {
            der.updateXMLInDatastore();
        } catch (MCRException ex) {
            LOGGER.error("Exception while store to derivate " + mysemcrid);
        }

        MCRObjectID ID = new MCRObjectID(myremcrid);
        StringBuffer sb = new StringBuffer();
        sb = (new StringBuffer("MCR.type_")).append(ID.getTypeId()).append("_in");

        sb = new StringBuffer(getBaseURL());
        sb.append("receive/").append(myremcrid);
        job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(sb.toString()));
    }

    /**
     * The method start the file upload to add to the metadata object that is
     * stored in the workflow. The method use the input parameter: <b>type</b>,
     * <b>step</b>, <b>re_mcrid</b> and <b>se_mcrid</b>. Access rigths must
     * be 'create-'type.
     * 
     * @param job
     *            the MCRServletJob instance
     */
    public void waddfile(MCRServletJob job) throws IOException {
        if (!AI.checkPermission("create-" + mytype)) {
            job.getResponse().sendRedirect(getBaseURL() + usererrorpage);
            return;
        }

        StringBuffer sb = new StringBuffer(pagedir);
        sb.append("editor_").append(mytype).append("_editor.xml");

        String fuhid = new MCRUploadHandlerMyCoRe(myremcrid, mysemcrid, "new", getBaseURL() + sb.toString()).getID();
        myfile = pagedir + "fileupload_new.xml";

        String base = getBaseURL() + myfile;
        Properties params = new Properties();
        params.put("XSL.UploadID", fuhid);
        params.put("XSL.editor.source.new", "true");
        params.put("XSL.editor.cancel.url", getBaseURL() + cancelpage);
        params.put("XSL.target.param.1", "method=formBasedUpload");
        params.put("XSL.target.param.2", "uploadId=" + fuhid);
        params.put("mcrid", mysemcrid);
        params.put("type", mytype);
        params.put("step", mystep);
        params.put("remcrid", myremcrid);
        job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(buildRedirectURL(base, params)));
    }

    /**
     * The method commit a object including all derivates that is stored in the
     * workflow to the server. The method use the input parameter: <b>type</b>,<b>step</b>
     * and <b>se_mcrid</b>. Access rigths must be 'writedb'.
     * 
     * @param job
     *            the MCRServletJob instance
     */
    public void wcommit(MCRServletJob job) throws IOException {
        org.jdom.Element rule = WFM.getRuleFromFile(mysemcrid, "writedb");
        if (!AI.checkPermission(rule)) {
            job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + usererrorpage));
            return;
        }
        if (mysemcrid.length() == 0) {
            job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + mcriderrorpage));
            return;
        }

        try {
            if (WFM.commitMetadataObject(mytype, mysemcrid)) {
                WFM.deleteMetadataObject(mytype, mysemcrid);

                List addr = WFM.getMailAddress(mytype, "wcommit");

                if (addr.size() != 0) {
                    String sender = WFM.getMailSender();
                    String appl = CONFIG.getString("MCR.editor_mail_application_id", "DocPortal");
                    String subject = "Automaticaly message from " + appl;
                    StringBuffer text = new StringBuffer();
                    text.append("Es wurde ein Objekt vom Typ ").append(mytype).append(" mit der ID ").append(mysemcrid).append(" aus dem Workflow in das System geladen.");
                    LOGGER.info(text.toString());

                    try {
                        MCRMailer.send(sender, addr, subject, text.toString(), false);
                    } catch (Exception ex) {
                        LOGGER.error("Can't send a mail to " + addr);
                    }
                }

                StringBuffer sb = new StringBuffer("receive/").append(mysemcrid);
                myfile = sb.toString();
            } else {
                myfile = storeerrorpage;
            }
        } catch (MCRActiveLinkException e) {
            try {
                generateActiveLinkErrorpage(job.getRequest(), job.getResponse(), "Error while commiting work to the server.", e);
                return;
            } catch (Exception se) {
                myfile = storeerrorpage;
            }
        } catch (MCRException e) {
            myfile = storeerrorpage;
        }
        job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + myfile));
    }

    /**
     * The method delete a derivate from an object that is stored in the
     * workflow. The method use the input parameter: <b>type</b>,<b>step</b>
     * <b>se_mcrid</b> and <b>re_mcrid</b>. Access rigths must be 'deletewf'.
     * 
     * @param job
     *            the MCRServletJob instance
     */
    public void wdelder(MCRServletJob job) throws IOException {
        org.jdom.Element rule = WFM.getRuleFromFile(myremcrid, "deletewf");
        if (!AI.checkPermission(rule)) {
            job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + usererrorpage));
            return;
        }
        if (mysemcrid.length() == 0) {
            job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + mcriderrorpage));
            return;
        }

        StringBuffer sb = new StringBuffer(pagedir);
        sb.append("editor_").append(mytype).append("_editor.xml");

        new MCRObjectID(mysemcrid);
        WFM.deleteDerivateObject(mytype, mysemcrid);

        List addr = WFM.getMailAddress(mytype, "wdelder");

        if (addr.size() != 0) {
            String sender = WFM.getMailSender();
            String appl = CONFIG.getString("MCR.editor_mail_application_id", "DocPortal");
            String subject = "Automaticaly message from " + appl;
            StringBuffer text = new StringBuffer();
            text.append("Es wurde ein Derivate mit der ID ").append(mysemcrid).append(" aus dem Workflow gel�scht.");
            LOGGER.info(text.toString());

            try {
                MCRMailer.send(sender, addr, subject, text.toString(), false);
            } catch (Exception ex) {
                LOGGER.error("Can't send a mail to " + addr);
            }
        }
        job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + sb.toString()));
    }

    /**
     * The method delete a file from the derivate object that is stored in the
     * workflow. The method use the input parameter: <b>type</b>,<b>step</b>
     * <b>se_mcrid</b> and <b>re_mcrid</b>. Access rigths must be 'deletewf'.
     * 
     * @param job
     *            the MCRServletJob instance
     */
    public void wdelfile(MCRServletJob job) throws IOException {
        org.jdom.Element rule = WFM.getRuleFromFile(myremcrid, "deletewf");
        if (!AI.checkPermission(rule)) {
            job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + usererrorpage));
            return;
        }
        if (mysemcrid.length() == 0) {
            job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + mcriderrorpage));
            return;
        }

        int all = 0;

        int i = extparm.indexOf("####nrall####");
        int j = 0;

        if (i != -1) {
            j = extparm.indexOf("####", i + 13);
            all = Integer.parseInt(extparm.substring(i + 13, j));
        }

        i = extparm.indexOf("####nrthe####");

        if (i != -1) {
            j = extparm.indexOf("####", i + 13);
            Integer.parseInt(extparm.substring(i + 13, j));
        }

        if (all > 1) {
            String derpath = WFM.getDirectoryPath(mytype);
            i = extparm.indexOf("####filename####");

            if (i != -1) {
                String filename = extparm.substring(i + 16, extparm.length());

                try {
                    File fi = new File(derpath, filename);
                    fi.delete();
                } catch (Exception ex) {
                    LOGGER.warn("Can't remove file " + filename);
                }
            }
        }

        StringBuffer sb = new StringBuffer();
        sb.append(getBaseURL()).append(pagedir).append("editor_").append(mytype).append("_editor.xml");
        job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(sb.toString()));
    }

    /**
     * The method delete a metadata object that is stored in the workflow. The
     * method use the input parameter: <b>type</b>,<b>step</b> and
     * <b>se_mcrid</b>. Access rigths must be 'deletewf'.
     * 
     * @param job
     *            the MCRServletJob instance
     */
    public void wdelobj(MCRServletJob job) throws IOException {
        org.jdom.Element rule = WFM.getRuleFromFile(mysemcrid, "deletewf");
        if (!AI.checkPermission(rule)) {
            job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + usererrorpage));
            return;
        }
        if (mysemcrid.length() == 0) {
            job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + mcriderrorpage));
            return;
        }

        StringBuffer sb = new StringBuffer(pagedir);
        sb.append("editor_").append(mytype).append("_editor.xml");

        new MCRObjectID(mysemcrid);
        WFM.deleteMetadataObject(mytype, mysemcrid);

        List addr = WFM.getMailAddress(mytype, "wdelobj");

        if (addr.size() != 0) {
            String sender = WFM.getMailSender();
            String appl = CONFIG.getString("MCR.editor_mail_application_id", "DocPortal");
            String subject = "Automaticaly message from " + appl;
            StringBuffer text = new StringBuffer();
            text.append("Es wurde ein Objekt vom Typ ").append(mytype).append(" mit der ID ").append(mysemcrid).append(" aus dem Workflow gel�scht.");
            LOGGER.info(text.toString());

            try {
                MCRMailer.send(sender, addr, subject, text.toString(), false);
            } catch (Exception ex) {
                LOGGER.error("Can't send a mail to " + addr);
            }
        }
        job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + sb.toString()));
    }

    /**
     * The method start the editor to modify the metadata object ACL that is
     * stored in the workflow. The method use the input parameter: <b>type</b>,<b>step</b>
     * and <b>se_mcrid</b>. Access rigths must be 'writewf'.
     * 
     * @param job
     *            the MCRServletJob instance
     */
    public void weditacl(MCRServletJob job) throws IOException {
        org.jdom.Element rule = WFM.getRuleFromFile(mysemcrid, "writewf");
        if (!AI.checkPermission(rule)) {
            job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + usererrorpage));
            return;
        }
        if (mysemcrid.length() == 0) {
            job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + mcriderrorpage));
            return;
        }

        // read file
        StringBuffer sb = new StringBuffer();
        sb.append(CONFIG.getString("MCR.editor_" + mcrmysemcrid.getTypeId() + "_directory")).append(SLASH).append(mysemcrid).append(".xml");
        org.jdom.Element service = null;
        try {
            File fi = new File(sb.toString());
            if (fi.isFile() && fi.canRead()) {
                MCRObject obj = new MCRObject();
                obj.setFromURI(sb.toString());
                service = obj.getService().createXML();
            } else {
                LOGGER.error("Can't read file " + sb.toString());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            LOGGER.error("Can't read file " + sb.toString());
        }

        MCRSession session = MCRSessionMgr.getCurrentSession();
        session.put("service", service);
        String base = getBaseURL() + myfile;
        Properties params = new Properties();
        params.put("XSL.editor.source.url", "session:service");
        params.put("XSL.editor.cancel.url", getBaseURL() + cancelpage);
        params.put("mcrid", mysemcrid);
        params.put("type", mytype);
        params.put("step", mystep);
        job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(buildRedirectURL(base, params)));
    }

    /**
     * The method start the editor modify derivate metadata that is stored in
     * the workflow. The method use the input parameter: <b>type</b>,<b>step</b>
     * <b>re_mcrid</b> and <b>se_mcrid</b>. Access rigths must be 'writewf'.
     * 
     * @param job
     *            the MCRServletJob instance
     */
    public void weditder(MCRServletJob job) throws IOException {
        org.jdom.Element rule = WFM.getRuleFromFile(myremcrid, "writewf");
        if (!AI.checkPermission(rule)) {
            job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + usererrorpage));
            return;
        }
        if (mysemcrid.length() == 0) {
            job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + mcriderrorpage));
            return;
        }

        StringBuffer sb = new StringBuffer();
        sb.append(WFM.getDirectoryPath(mytype)).append(SLASH).append(mysemcrid).append(".xml");

        MCRDerivate der = new MCRDerivate();
        der.setFromURI(sb.toString());

        org.jdom.Element textfield = new org.jdom.Element("textfield");
        org.jdom.Element defa = new org.jdom.Element("default");
        defa.setText(der.getLabel());
        textfield.addContent(defa);
        MCRSessionMgr.getCurrentSession().put("weditder", textfield);
        sb = new StringBuffer();
        sb.append(getBaseURL()).append(pagedir).append("editor_form_editor-derivate.xml");

        Properties params = new Properties();
        params.put("XSL.editor.source.new", "true");
        params.put("XSL.editor.cancel.url", getBaseURL() + cancelpage);
        params.put("se_mcrid", mysemcrid);
        params.put("re_mcrid", myremcrid);
        params.put("type", mytype);
        params.put("step", mystep);
        job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(buildRedirectURL(sb.toString(), params)));
    }

    /**
     * The method start the editor to modify a metadata object that is stored in
     * the workflow. The method use the input parameter: <b>type</b>,<b>step</b>
     * and <b>se_mcrid</b>. Access rigths must be 'writewf'.
     * 
     * @param job
     *            the MCRServletJob instance
     */
    public void weditobj(MCRServletJob job) throws IOException {
        org.jdom.Element rule = WFM.getRuleFromFile(mysemcrid, "writewf");
        if (!AI.checkPermission(rule)) {
            job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + usererrorpage));
            return;
        }
        if (mysemcrid.length() == 0) {
            job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + mcriderrorpage));
            return;
        }

        StringBuffer sb = new StringBuffer();
        sb.append("file://").append(CONFIG.getString("MCR.editor_" + mytype + "_directory")).append('/').append(mysemcrid).append(".xml");

        String base = getBaseURL() + myfile;
        Properties params = new Properties();
        params.put("XSL.editor.source.url", sb.toString());
        params.put("XSL.editor.cancel.url", getBaseURL() + cancelpage);
        params.put("mcrid", mysemcrid);
        params.put("type", mytype);
        params.put("step", mystep);
        job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(buildRedirectURL(base, params)));
    }

    /**
     * The method build a new derivate and start the file upload to add to the
     * metadata object that is stored in the workflow. The method use the input
     * parameter: <b>type</b>, <b>step</b>, <b>re_mcrid</b> and <b>se_mcrid</b>.
     * Access rigths must be 'create-'type.
     * 
     * @param job
     *            the MCRServletJob instance
     */
    public void wnewder(MCRServletJob job) throws IOException {
        if (!AI.checkPermission("create-" + mytype)) {
            (new MCRObjectID()).decrementOneFreeId((new MCRObjectID(mytfmcrid)).getBase());
            job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + usererrorpage));
            return;
        }

        myremcrid = mysemcrid;
        mysemcrid = WFM.createDerivateInWorkflow(myremcrid);
        waddfile(job);
    }

    /**
     * The method start the editor to create new metadata object that will be
     * stored in the workflow. The method use the input parameter: <b>type</b>,<b>step</b>
     * and <b>tf_mcrid</b>. Access rigths must be 'create-'type.
     * 
     * @param job
     *            the MCRServletJob instance
     */
    public void wnewobj(MCRServletJob job) throws IOException {
        if (!AI.checkPermission("create-" + mytype)) {
            (new MCRObjectID()).decrementOneFreeId((new MCRObjectID(mytfmcrid)).getBase());
            job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + usererrorpage));
            return;
        }

        String base = getBaseURL() + myfile;
        Properties params = new Properties();
        // start changes for submitting xml templates
        Element root = new Element("mycoreobject");
        LOGGER.debug("calling buildXMLTemplate...");
        if (MCREditorServletHelper.buildXMLTemplate(job.getRequest(), root)) {
            MCRSessionMgr.getCurrentSession().put("wnewobj", root);
            params.put("XSL.editor.source.url", "session:wnewobj");
        } else {
            LOGGER.debug("XMLTemplate is empty");
            params.put("XSL.editor.source.new", "true");// old code
        }
        // end changes
        params.put("XSL.editor.cancel.url", getBaseURL() + cancelpage);
        params.put("mcrid", mytfmcrid);
        params.put("type", mytype);
        params.put("step", mystep);
        job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(buildRedirectURL(base, params)));
    }

    /**
     * The method start the editor to modify a derivate object that is stored in
     * the workflow. The method use the input parameter: <b>type</b>,<b>step</b>
     * <b>re_mcrid</b> and <b>se_mcrid</b>. Access rigths must be 'writewf'.
     * 
     * @param job
     *            the MCRServletJob instance
     */
    public void wsetfile(MCRServletJob job) throws IOException {
        org.jdom.Element rule = WFM.getRuleFromFile(myremcrid, "writewf");
        if (!AI.checkPermission(rule)) {
            job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + usererrorpage));
            return;
        }
        if (mysemcrid.length() == 0) {
            job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + mcriderrorpage));
            return;
        }

        StringBuffer sb = new StringBuffer();
        sb.append(WFM.getDirectoryPath(mytype)).append(SLASH).append(mysemcrid).append(".xml");

        MCRDerivate der = new MCRDerivate();
        der.setFromURI(sb.toString());

        if (extparm.startsWith("####main####")) {
            der.getDerivate().getInternals().setMainDoc(extparm.substring(mysemcrid.length() + 1 + 12, extparm.length()));
        }

        if (extparm.startsWith("####label####")) {
            der.setLabel(extparm.substring(13, extparm.length()));
        }

        byte[] outxml = MCRUtils.getByteArray(der.createXML());

        try {
            FileOutputStream out = new FileOutputStream(sb.toString());
            out.write(outxml);
            out.flush();
        } catch (IOException ex) {
            LOGGER.error("Exception while store to file " + sb.toString());
        }

        sb = new StringBuffer();
        sb.append(pagedir).append("editor_").append(mytype).append("_editor.xml");
        job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + sb.toString()));
    }

    /**
     * The method start the editor to modify a derivate object that is stored in
     * the workflow. The method use the input parameter: <b>type</b>,<b>step</b>
     * <b>re_mcrid</b> and <b>se_mcrid</b>. Access rigths must be 'writewf'.
     * 
     * @param job
     *            the MCRServletJob instance
     */
    public void wsetlabel(MCRServletJob job) throws IOException {
        extparm = "####label####" + extparm;
        wsetfile(job);
    }

    /**
     * This method implements the error exit to <em>index.xml</em> if the
     * action method was not found or has an error. It implemets the TODO
     * 'wrongtodo'.
     * 
     * @param job
     *            the MCRServletJob instance
     */
    public void wrongtodo(MCRServletJob job) throws IOException {
        StringBuffer sb = new StringBuffer();
        sb.append(getBaseURL()).append("index.html");
        job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(sb.toString()));
    }

}
