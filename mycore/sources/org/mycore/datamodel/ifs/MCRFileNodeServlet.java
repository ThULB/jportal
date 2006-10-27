/*
 * $RCSfile: MCRFileNodeServlet.java,v $
 * $Revision: 1.40 $ $Date: 2006/08/25 11:13:44 $
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

package org.mycore.datamodel.ifs;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import org.mycore.access.MCRAccessManager;
import org.mycore.backend.remote.MCRRemoteAccessInterface;
import org.mycore.common.MCRConfigurationException;
import org.mycore.common.MCRException;
import org.mycore.common.MCRUtils;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.frontend.servlets.MCRServletJob;

/**
 * This servlet delivers the contents of an MCRFilesystemNode to the client
 * browser. If the node is a ordinary MCRFile, the contents of that file will be
 * sent to the browser. If the node is an MCRFile with a MCRAudioVideoExtender,
 * the message that starts the associated streaming player will be delivered. If
 * the node is a MCRDirectory, the contents of that directory will be forwareded
 * to MCRLayoutServlet as XML data to display a detailed directory listing.
 * 
 * @author Frank L�tzenkirchen
 * @author Jens Kupferschmidt
 * @author Thomas Scheffler (yagee)
 * 
 * @version $Revision: 1.40 $ $Date: 2006/08/25 11:13:44 $
 */
public class MCRFileNodeServlet extends MCRServlet {
    private static final long serialVersionUID = 1L;

    // The Log4J logger
    private static Logger LOGGER = Logger.getLogger(MCRFileNodeServlet.class.getName());

    // initialize it with an empty string -if propertie is missing,
    // because in a case of MCRConfigurationException,
    // no Servlet will be instantiated, and thats more bad then a missing
    // property!
    private static String accessErrorPage = CONFIG.getString("MCR.access_page_error", "");

    // The list of hosts from the configuration
    private static ArrayList remoteAliasList;

    private static final SAXBuilder SAX_BUILDER = new SAXBuilder();

    /**
     * Initializes the servlet and reads the default language and the remote
     * host list from the configuration.
     */
    public void init() throws MCRConfigurationException, ServletException {
        super.init();

        // read host list from configuration
        String hostconf = CONFIG.getString("MCR.remoteaccess_hostaliases", "local");
        remoteAliasList = new ArrayList();

        if (hostconf.indexOf("local") < 0) {
            remoteAliasList.add("local");
        }

        StringTokenizer st = new StringTokenizer(hostconf, ", ");

        while (st.hasMoreTokens())
            remoteAliasList.add(st.nextToken());
    }

    /**
     * Handles the HTTP request
     */
    public void doGetPost(MCRServletJob job) throws IOException, ServletException {
        HttpServletRequest request = job.getRequest();
        HttpServletResponse response = job.getResponse();
        if (!isParametersValid(request, response)) {
            return;
        }
        String hostAlias = getHostAlias(request);

        if (hostAlias.equals("local")) {
            handleLocalRequest(request, response);
        } else {
            handleRemoteRequest(request, response, hostAlias);
        }
    }

    private boolean isParametersValid(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String hostAlias = getHostAlias(request);

        if (!remoteAliasList.contains(hostAlias)) {
            String msg = "Error: HTTP request host is not in the alias list";
            LOGGER.error(msg);
            errorPage(request, response, HttpServletResponse.SC_BAD_REQUEST, msg, new MCRException(hostAlias + " is not in the host alias list!"),
                    false);

            return false;
        }

        String requestPath = request.getPathInfo();
        LOGGER.info("MCRFileNodeServlet: request path = " + requestPath);

        if (requestPath == null) {
            String msg = "Error: HTTP request path is null";
            LOGGER.error(msg);
            errorPage(request, response, HttpServletResponse.SC_BAD_REQUEST, msg, new MCRException("No path was given in the request"), false);

            return false;
        }
        return true;
    }

    /**
     * @param req
     * @return
     */
    private static String getHostAlias(HttpServletRequest req) {
        // get the host alias
        String hostAlias = getProperty(req, "hosts");

        if ((hostAlias == null) || (hostAlias.trim().length() == 0)) {
            hostAlias = "local";
        }
        LOGGER.debug("host = " + hostAlias);
        return hostAlias;
    }

    /**
     * @param request
     * @param response
     * @throws IOException
     * @throws ServletException
     */
    private void handleLocalRequest(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String ownerID = getOwnerID(request);
        // local node to be retrieved
        MCRFilesystemNode root;

        try {
            root = MCRFilesystemNode.getRootNode(ownerID);
        } catch (org.mycore.common.MCRPersistenceException e) {
            // Could not get value from JDBC result set
            LOGGER.error("MCRFileNodeServlet: Error while getting root node!", e);
            root = null;
        }

        if (root == null) {
            String msg = "Error: No root node found for owner ID " + ownerID;
            LOGGER.error(msg);
            errorPage(request, response, HttpServletResponse.SC_NOT_FOUND, msg, new MCRException(msg), false);

            return;
        }

        if (root instanceof MCRFile) {
            if (request.getPathInfo().length() > ownerID.length() + 1) {
                // request path is too long
                String msg = "Error: No such file or directory " + request.getPathInfo();
                LOGGER.error(msg);
                errorPage(request, response, HttpServletResponse.SC_NOT_FOUND, msg, new MCRException(msg), false);
                return;
            }
            sendFile(request, response, (MCRFile) root);
            return;
        }

        // root node is a directory
        int pos = ownerID.length() + 1;
        StringBuffer path = new StringBuffer(request.getPathInfo().substring(pos));
        if ((path.charAt(path.length() - 1) == '/') && path.length() > 1) {
            path.deleteCharAt(path.length() - 1);
        }
        MCRDirectory dir = (MCRDirectory) root;
        MCRFilesystemNode node = dir.getChildByPath(path.toString());

        if (node == null) {
            String msg = "Error: No such file or directory " + path;
            LOGGER.error(msg);
            errorPage(request, response, HttpServletResponse.SC_NOT_FOUND, msg, new MCRException(msg), false);
            return;
        } else if (node instanceof MCRFile) {
            sendFile(request, response, (MCRFile) node);
            return;
        } else {
            sendDirectory(request, response, (MCRDirectory) node);
            return;
        }
    }

    protected static String getOwnerID(HttpServletRequest request) {
        String pI = request.getPathInfo();
        StringBuffer ownerID = new StringBuffer(request.getPathInfo().length());
        boolean running = true;
        for (int i = (pI.charAt(0) == '/') ? 1 : 0; (i < pI.length() && running); i++) {
            switch (pI.charAt(i)) {
            case '/':
                running = false;
                break;
            default:
                ownerID.append(pI.charAt(i));
                break;
            }
        }
        return ownerID.toString();
    }

    /**
     * @param req
     * @param res
     * @param hostAlias
     * @throws IOException
     * @throws ServletException
     */
    private void handleRemoteRequest(HttpServletRequest req, HttpServletResponse res, String hostAlias) throws IOException, ServletException {
        // remote node to be retrieved
        String prop = "MCR.remoteaccess_" + hostAlias + "_query_class";
        MCRRemoteAccessInterface comm = (MCRRemoteAccessInterface) (CONFIG.getInstanceOf(prop));

        BufferedInputStream in = comm.requestIFS(hostAlias, req.getPathInfo());

        if (in == null) {
            return;
        }

        String headercontext = comm.getHeaderContent();

        if (!headercontext.equals("text/xml")) {
            res.setContentType(headercontext);
            OutputStream out = new BufferedOutputStream(res.getOutputStream());
            MCRUtils.copyStream(in, out);
            out.close();
            return;
        }

        org.jdom.Document jdom = null;
        try {
            jdom = SAX_BUILDER.build(in);
        } catch (JDOMException e) {
            LOGGER.warn("Error while parsing remote document", e);
            errorPage(req, res, HttpServletResponse.SC_EXPECTATION_FAILED, "Error while parsing remote document.", e, false);
        } catch (IOException e) {
            LOGGER.warn("Error while parsing remote document", e);
            errorPage(req, res, HttpServletResponse.SC_EXPECTATION_FAILED, "Error while parsing remote document.", e, false);
        }

        forwardRequest(req, res, jdom);
    }

    /**
     * Sends the contents of an MCRFile to the client. If the MCRFile provides
     * an MCRAudioVideoExtender, the file's content is NOT sended to the client,
     * instead the stream that starts the associated streaming player is sended
     * to the client. The HTTP request may then contain StartPos and StopPos
     * parameters that contain the timecodes where to start and/or stop
     * streaming.
     */
    private void sendFile(HttpServletRequest req, HttpServletResponse res, MCRFile file) throws IOException {
        if (!MCRAccessManager.checkPermissionForReadingDerivate(file.getOwnerID())) {
            LOGGER.info("MCRFileNodeServlet: AccessForbidden to " + file.getName());
            res.sendRedirect(res.encodeRedirectURL(getBaseURL() + accessErrorPage));
            return;
        }

        LOGGER.info("MCRFileNodeServlet: Sending file " + file.getName());

        if (file.hasAudioVideoExtender()) // Start streaming player
        {
            MCRAudioVideoExtender ext = file.getAudioVideoExtender();

            String startPos = req.getParameter("StartPos");
            String stopPos = req.getParameter("StopPos");

            res.setContentType(ext.getPlayerStarterContentType());
            ext.getPlayerStarterTo(res.getOutputStream(), startPos, stopPos);
        } else // Send contents of ordinary file
        {
            res.setContentType(file.getContentType().getMimeType());
            res.setContentLength((int) (file.getSize()));

            OutputStream out = new BufferedOutputStream(res.getOutputStream());
            file.getContentTo(out);
            out.close();
        }
    }

    /**
     * Sends the contents of an MCRDirectory as XML data to the client
     */
    private void sendDirectory(HttpServletRequest req, HttpServletResponse res, MCRDirectory dir) throws IOException, ServletException {
        LOGGER.info("MCRFileNodeServlet: Sending listing of directory " + dir.getName());
        Document jdom = MCRDirectoryXML.getInstance().getDirectoryXML(dir);
        forwardRequest(req, res, jdom);

    }

    /** 
     *  Forwards the document to the output
     *  @author A.Schaar
     *  @see its overwritten in jspdocportal 
     */
    protected void forwardRequest(HttpServletRequest req, HttpServletResponse res, Document jdom) throws IOException, ServletException {
    	req.setAttribute("MCRLayoutServlet.Input.JDOM", jdom);
        RequestDispatcher rd = getServletContext().getNamedDispatcher("MCRLayoutServlet");
        rd.forward(req, res);    	
    }
    
    
    /** 
     *  Forwards the error to generate the output
     *  @author A.Schaar
     *  @see its overwritten in jspdocportal 
     */
    protected void errorPage ( HttpServletRequest req, HttpServletResponse res, int error, String msg, Exception ex, boolean xmlstyle)  throws IOException, ServletException {
        generateErrorPage(req, res, error,msg, ex, xmlstyle);    	    
    }
}