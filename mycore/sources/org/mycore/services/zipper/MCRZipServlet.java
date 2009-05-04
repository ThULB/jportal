/*
 * 
 * $Revision: 14994 $ $Date: 2009-03-24 13:01:57 +0100 (Di, 24. Mär 2009) $
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

package org.mycore.services.zipper;

import static org.mycore.common.MCRConstants.XLINK_NAMESPACE;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Templates;
import javax.xml.transform.sax.TransformerHandler;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.transform.JDOMSource;
import org.mycore.access.MCRAccessManager;
import org.mycore.common.MCRConfiguration;
import org.mycore.common.MCRConfigurationException;
import org.mycore.common.MCRException;
import org.mycore.common.xml.MCRLayoutService;
import org.mycore.common.xml.MCRXMLResource;
import org.mycore.common.xml.MCRXSLTransformation;
import org.mycore.datamodel.common.MCRXMLTableManager;
import org.mycore.datamodel.ifs.MCRDirectory;
import org.mycore.datamodel.ifs.MCRFile;
import org.mycore.datamodel.ifs.MCRFilesystemNode;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.frontend.servlets.MCRServletJob;

/**
 * This servlet delivers the contents of MycoreObects to the client as
 * zip-Files. There are three modes a) if id=mycoreobjectID (delivers the
 * metadata, including all derivates) b) if id=derivateID (delivers all files of
 * the derivate) c) if id=derivateID/directoryPath (delivers all files of a
 * special directory of one derivate)
 * 
 * TODO: include an AccessCheck, when ACLs in MyCoRe are realized
 * 
 * call the Servlet via Browser:
 * ServletsBaseURL/MCRZipServlet?id=DocPortal_document_000001
 * 
 * @author Heiko Helmbrecht
 * 
 * @version $Revision: 14994 $ $Date: 2009-03-24 13:01:57 +0100 (Di, 24. Mär 2009) $
 */
public class MCRZipServlet extends MCRServlet {
    private static final long serialVersionUID = 1L;

    // The Log4J logger
    private static Logger LOGGER = Logger.getLogger(MCRZipServlet.class.getName());

    private static String accessErrorPage = CONFIG.getString("MCR.Access.Page.Error");

    protected MCRXMLTableManager xmltable = null;

    protected String stylesheet;

    /**
     * Initializes the servlet and reads the transforming stylesheet from the
     * configuration.
     */
    public void init() throws MCRConfigurationException, ServletException {
        super.init();
        xmltable = MCRXMLTableManager.instance();
        stylesheet = MCRConfiguration.instance().getString("MCR.zip.metadata.transformer");
    }

    /**
     * Handles the HTTP request
     */
    public void doGetPost(MCRServletJob job) throws IOException {
        HttpServletRequest req = job.getRequest();
        HttpServletResponse res = job.getResponse();

        String id;
        String path;
        ZipOutputStream out = null;

        // get Parameter
        String paramid = getProperty(req, "id");

        Matcher ma = Pattern.compile("\\A([\\w]+)/([\\w/]+)\\z").matcher(paramid);

        if (ma.find()) {
            id = ma.group(1);
            path = ma.group(2);
        } else {
            id = paramid;
            path = null;
        }

        MCRObjectID mcrid = null;

        try {
            mcrid = new MCRObjectID(id);
        } catch (MCRException e1) {
            String msg = "Error: HTTP request id is not in the allowed id-list";
            LOGGER.error(msg + ":" + id);
            generateErrorPage(req, res, HttpServletResponse.SC_BAD_REQUEST, msg, new MCRException(id + " is a wrong ID!"), false);

            return;
        } catch (NullPointerException e2) {
            String msg = "Error: Wrong Parameters given";
            LOGGER.error(msg);
            generateErrorPage(req, res, HttpServletResponse.SC_BAD_REQUEST, msg, new MCRException(" wrong Parameters!"), false);

            return;
        }

        try {
            if (id.indexOf("_derivate_") > 0) {
                if (!MCRAccessManager.checkPermissionForReadingDerivate(id)) {
                    LOGGER.info("MCRFileNodeServlet: AccessForbidden to " + id);
                    res.sendRedirect(res.encodeRedirectURL(getBaseURL() + accessErrorPage));
                    return;
                }
                out = buildZipOutputStream(res, id, path);
                sendDerivate(id, path, out);
                out.close();
            } else {
                Document jdom = xmltable.readDocument(mcrid);
                if (!MCRAccessManager.checkPermission(id, "read")) {
                    LOGGER.info("MCRFileNodeServlet: AccessForbidden to " + id);
                    res.sendRedirect(res.encodeRedirectURL(getBaseURL() + accessErrorPage));
                    return;
                }
                out = buildZipOutputStream(res, id, path);
                sendObject(jdom, req, out);
                out.close();
            }
        } catch (Exception e) {
            String msg = "Das Zip-File konnte nicht ordnungsgem�ss erstellt werden, "
                    + "Bitte �berpr�fen Sie die eingegebenen Parameter";
            res.reset();
            generateErrorPage(req, res, HttpServletResponse.SC_BAD_REQUEST, msg, new MCRException("zip-Error!", e), false);
        }
    }

    /**
     * sendZipped adds a single File to the ZipOutputStream,
     * 
     * @param file
     *            MCRFile, that has to be zipped
     */
    protected void sendZipped(MCRFile file, ZipOutputStream out) throws IOException {
        LOGGER.debug("zipping " + file.getPath());
        ZipEntry ze = new ZipEntry(file.getPath());
        ze.setTime(file.getLastModified().getTime().getTime());
        out.putNextEntry(ze);
        file.getContentTo(out);
        out.closeEntry();
    }

    /**
     * sendZipped adds a whole Directory of an Derivate to the ZipOutputStream
     * 
     * @param directory
     *            MCRDirectory, that has to be zipped
     */
    protected void sendZipped(MCRDirectory directory, ZipOutputStream out) throws IOException {
        MCRFilesystemNode[] nodeArray;
        nodeArray = directory.getChildren();

        for (int i = 0; i < nodeArray.length; i++) {
            if (nodeArray[i] instanceof MCRFile) {
                sendZipped((MCRFile) nodeArray[i], out);
            } else {
                sendZipped((MCRDirectory) nodeArray[i], out);
            }
        }
    }

    /**
     * sendZipped zips a File with the Metadata of the Object, makes a
     * XSL-Transformation with the Metadata
     * 
     * @param jdom
     *            MycoreObject-ResultContainer as org.jdom.Document
     * @param parameters
     *            Parameters, that can be needed in the transforming
     *            XSL-Stylesheet
     * @throws JDOMException 
     */
    protected void sendZipped(Document jdom, Properties parameters, ZipOutputStream out) throws IOException, JDOMException {
        ZipEntry ze = new ZipEntry("metadata.xml");
        ze.setTime(new Date().getTime());
        out.putNextEntry(ze);

        Document stylesheet = MCRXMLResource.instance().getResource("xsl/" + this.stylesheet);
        MCRXSLTransformation transformation = MCRXSLTransformation.getInstance();
        Templates templates = transformation.getStylesheet(new JDOMSource(stylesheet));
        TransformerHandler th = transformation.getTransformerHandler(templates);
        MCRXSLTransformation.setParameters(th, parameters);
        transformation.transform(jdom, th, out);
        out.closeEntry();

        return;
    }

    /**
     * sendDerivate sends all files of a derivate or of a special folder of the
     * derivate
     * 
     * @param ownerID
     *            the derivateID that should be zipped
     * @param dirpath
     *            relative path zu a special folder, if given, only this folder
     *            is zipped
     */
    protected void sendDerivate(String ownerID, String dirpath, ZipOutputStream out) throws IOException {

        MCRFilesystemNode root;
        MCRDirectory rootdirectory;
        MCRFilesystemNode zipdirectory;
        root = MCRFilesystemNode.getRootNode(ownerID);

        if (root == null) {
            String msg = "Error: No root node found for owner ID " + ownerID;
            LOGGER.error(msg);

            return;
        }

        if (root instanceof MCRFile) {
            sendZipped((MCRFile) root, out);
            LOGGER.debug("file " + root.getName() + " zipped");

            return;
        }
        // root is a directory
        if ((dirpath == null) || (dirpath.equals(""))) {
            sendZipped((MCRDirectory) root, out);
            LOGGER.debug("directory " + root.getName() + " zipped");

            return;
        }
        rootdirectory = (MCRDirectory) root;
        zipdirectory = rootdirectory.getChildByPath(dirpath);

        if (zipdirectory == null) {
            String msg = "Error: No such file or directory " + dirpath;
            LOGGER.error(msg);

            return;
        } else if (zipdirectory instanceof MCRFile) {
            sendZipped((MCRFile) zipdirectory, out);
        } else if (zipdirectory instanceof MCRDirectory) {
            sendZipped((MCRDirectory) zipdirectory, out);
        } else {
            String msg = "Error: could not found the dir: " + dirpath;
            LOGGER.error(msg);

            return;
        }

        return;
    }

    /**
     * sendObject: zips all derivates of a Object and a metadata-xml file, that
     * was built via a given xsl-stylesheet
     * 
     * @param jdom
     *            the JDOM of the given MycoreObject
     * @throws JDOMException 
     *  
     */
    @SuppressWarnings("unchecked")
    protected void sendObject(Document jdom, HttpServletRequest req, ZipOutputStream out) throws IOException, JDOMException {
        // zip the object's Metadata
        Properties parameters = MCRLayoutService.buildXSLParameters(req);
        sendZipped(jdom, parameters, out);

        // zip all derivates
        List<Element> li = jdom.getRootElement().getChild("structure").getChild("derobjects").getChildren("derobject");

        for (Element el : li) {
            LOGGER.debug(el.getName());

            if (el.getAttributeValue("inherited").equals("0")) {
                String ownerID = el.getAttributeValue("href", XLINK_NAMESPACE);
                // here the access check is tested only against the derivate
                if (MCRAccessManager.checkPermission(ownerID, "read")) {
                    String dir = null;
                    sendDerivate(ownerID, dir, out);
                }
            }
        }

        return;
    }

    /**
     * buildZipOutputStream sets the contenttype and name of the zip-file
     * Returns the ZipOutputStream
     * 
     * @param id
     *            the id of the object or derivate that is zipped, builds the
     *            name
     * @param dirpath
     *            if given, it is concatenated to the name of the zip-file
     * 
     */
    protected ZipOutputStream buildZipOutputStream(HttpServletResponse res, String id, String dirpath) throws IOException {
        String filename = ((dirpath == null) || dirpath.equals("")) ? (id + ".zip") : (id + "-" + dirpath.replaceAll("/", "-") + ".zip");
        res.setContentType("multipart/x-zip");
        res.addHeader("Content-Disposition", "atachment; filename=\"" + filename + "\"");

        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(res.getOutputStream()));
        out.setLevel(Deflater.BEST_COMPRESSION);

        return out;
    }
}
