/*
 * $RCSfile: MCRSearchMaskServlet.java,v $
 * $Revision: 1.23 $ $Date: 2005/09/28 07:49:54 $
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

package org.mycore.services.query;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.mycore.common.MCRConfigurationException;
import org.mycore.common.MCRException;
import org.mycore.common.MCRSessionMgr;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.frontend.servlets.MCRServletJob;

/**
 * This servlet provides a web interface to create a search mask and analyze the
 * output of them to create a XQuery and start the MCRQueryServlet
 * 
 * @author Jens Kupferschmidt
 * @author Heiko Helmbrecht
 * @version $Revision: 1.23 $ $Date: 2005/09/28 07:49:54 $
 */
public class MCRSearchMaskServlet extends MCRServlet {
    private static final long serialVersionUID = 1L;
    // logger
    protected static Logger LOGGER = Logger.getLogger(MCRSearchMaskServlet.class.getName());

    /**
     * This method handles HTTP requests and resolves them to output. The method
     * can get two modi : 'CreateSearchMask' to generate a new search mask or
     * 'CreateQuery' to read the data from the search mask and start the
     * MCRQueryServlet.
     * 
     * @exception IOException
     *                for java I/O errors.
     * @exception ServletException
     *                for errors from the servlet engine.
     */
    public void doGetPost(MCRServletJob job) throws IOException, ServletException {
        HttpServletRequest request = job.getRequest();
        HttpServletResponse response = job.getResponse();

        String mode = request.getParameter("mode");

        if (mode == null) {
            mode = "CreateSearchMask";
        }

        if (mode.equals("CreateSearchMask")) {
            createSearchMask(request, response, mode);
        }

        if (mode.equals("CreateQuery")) {
            createQuery(request, response);
        }
    }

    private static Map jdomCache = new HashMap();

    /**
     * This method returns a search mask definition jdom for a given layout.
     */
    public static org.jdom.Document getJDOM(String layout) throws IOException {
        org.jdom.Document jdom = (org.jdom.Document) jdomCache.get(layout);

        if (jdom != null) {
            return jdom;
        }

        String smc = CONFIG.getString("MCR.searchmask_config_" + layout.toLowerCase());

        try {
            InputStream in = MCRSearchMaskServlet.class.getResourceAsStream("/" + smc);

            if (in == null) {
                throw new MCRConfigurationException("Can't read config file " + smc);
            }

            jdom = new org.jdom.input.SAXBuilder().build(in);
            jdomCache.put(layout, jdom);
        } catch (org.jdom.JDOMException e) {
            throw new MCRException("SearchMaskServlet : Can't read config file " + smc + " or it has a parse error.", e);
        }

        return jdom;
    }

    /**
     * This method handles the CreateSearchMask mode. It create the request for
     * MCRLayoutServlet and starts them.
     * 
     * @param request
     *            the HTTP request instance
     * @param response
     *            the HTTP response instance
     * @exception IOException
     *                for java I/O errors.
     * @exception ServletException
     *                for errors from the servlet engine.
     */
    protected void createSearchMask(HttpServletRequest request, HttpServletResponse response, String mode) throws IOException, ServletException {
        String type = request.getParameter("type");
        String layout = request.getParameter("layout");

        if (type == null) {
            return;
        }

        if (layout == null) {
            layout = type;
        }

        type = type.toLowerCase();

        org.jdom.Document jdom = getJDOM(layout);

        // prepare the stylesheet name
        String style = mode + "-" + layout + "-" + MCRSessionMgr.getCurrentSession().getCurrentLanguage();

        // start Layout servlet
        request.setAttribute("MCRLayoutServlet.Input.JDOM", jdom);
        request.setAttribute("XSL.Style", style);

        RequestDispatcher rd = getServletContext().getNamedDispatcher("MCRLayoutServlet");
        rd.forward(request, response);
    }

    /**
     * This method handles the CreateQuery mode. It create the request for
     * MCRQueryServlet and starts them.
     * 
     * @param request
     *            the HTTP request instance
     * @param response
     *            the HTTP response instance
     * @exception IOException
     *                for java I/O errors.
     * @exception ServletException
     *                for errors from the servlet engine.
     */
    protected void createQuery(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String type = request.getParameter("type");
        String layout = request.getParameter("layout");
        String host = "";
        String[] hosts = request.getParameterValues("hosts");

        if (hosts != null) {
            StringBuffer hostsb = new StringBuffer("");

            for (int i = 0; i < hosts.length; i++) {
                if (i != 0) {
                    hostsb.append(',');
                }

                hostsb.append(hosts[i]);
            }

            host = hostsb.toString();
        }

        if (host.length() == 0) {
            host = "local";
        }

        if (type == null) {
            return;
        }

        if (layout == null) {
            layout = type;
        }

        String query = createQueryString(layout, request);

        // start Query servlet
        request.setAttribute("saveResults", "true");
        request.removeAttribute("mode");
        request.setAttribute("mode", "ResultList");
        request.removeAttribute("type");
        request.setAttribute("type", type);
        layout = type;
        request.removeAttribute("layout");
        request.setAttribute("layout", layout);
        request.removeAttribute("hosts");
        request.setAttribute("hosts", host);
        request.removeAttribute("lang");
        request.removeAttribute("query");
        request.setAttribute("query", query);

        RequestDispatcher rd = getServletContext().getNamedDispatcher("MCRQueryServlet");
        rd.forward(request, response);
    }

    /**
     * This method creates a query string from a number of request parameters
     * and the xml definition of the corresponding search mask (read from the
     * configuration via the layout parameter).
     */
    public static String createQueryString(String layout, HttpServletRequest request) throws IOException {
        StringBuffer query = new StringBuffer("");

        org.jdom.Document jdom = getJDOM(layout);

        org.jdom.Element searchpage = jdom.getRootElement().getChild("searchpage");
        List element_list = searchpage.getChildren();
        int len = element_list.size();
        System.out.println("searchpage has " + len + " childs");

        for (int i = 0; i < len; i++) {
            org.jdom.Element element = (org.jdom.Element) element_list.get(i);

            if (!element.getName().equals("element")) {
                continue;
            }

            if (!element.getAttributeValue("type").equals("query")) {
                continue;
            }

            String name = element.getAttributeValue("name");
            String tempquery = (element.getAttributeValue("query")).replace('\'', '\"');
            System.out.println("name:" + name + " tempquery:" + tempquery);

            int tempfields = 1;

            try {
                tempfields = (new Integer(element.getAttributeValue("fields"))).intValue();
            } catch (NumberFormatException e) {
                throw new MCRException("SearchMaskServlet : The field attribute is " + "not a number.");
            }

            ArrayList param = new ArrayList();
            ArrayList varia = new ArrayList();

            for (int j = 0; j < tempfields; j++) {
                StringBuffer sb = (new StringBuffer(name)).append(j + 1);
                param.add(request.getParameter(sb.toString()));
                sb = (new StringBuffer("$")).append(j + 1);
                varia.add(sb.toString());
            }

            // check to all attributes for a line are filled
            int k = 0;

            for (int j = 0; j < param.size(); j++) {
                LOGGER.debug(name + "   " + param.get(j) + "   " + varia.get(j));

                if (param.get(j) == null) {
                    k = 1;

                    break;
                }

                if (((String) param.get(j)).trim().length() == 0) {
                    k = 1;

                    break;
                }
            }

            if (k != 0) {
                continue;
            }

            for (int j = 0; j < param.size(); j++) {
                int start = 0;
                int l = ((String) varia.get(j)).length();
                k = tempquery.indexOf((String) varia.get(j), 0);

                if (k == -1) {
                    throw new MCRException("SearchMaskServlet : The query attribute " + "has not the elemnt " + ((String) varia.get(j)));
                }

                StringBuffer qsb = new StringBuffer(128);

                while (k != -1) {
                    if (tempquery.charAt(k - 1) == '\'') {
                        qsb.append(tempquery.substring(start, k - 1)).append("\"").append(((String) param.get(j))).append("\"");
                        start = k + 1 + l;
                    } else {
                        qsb.append(tempquery.substring(start, k)).append(((String) param.get(j)));
                        start = k + l;
                    }

                    k = tempquery.indexOf((String) varia.get(j), start);
                }

                qsb.append(tempquery.substring(start, tempquery.length()));
                tempquery = qsb.toString();
            }

            if (query.length() != 0) {
                query.append(" and ");
            }

            System.out.println("query:" + query.toString() + " tempquery:" + tempquery);
            query.append(tempquery);
        }

        return query.toString();
    }
}
