/*
 * 
 * $Revision: 13085 $ $Date: 2008-02-06 18:27:24 +0100 (Mi, 06 Feb 2008) $
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

package org.mycore.services.fieldquery;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.DOMOutputter;
import org.mycore.common.MCRConfigurationException;
import org.mycore.common.MCRException;
import org.mycore.common.xml.MCRURIResolver;

/**
 * Executes a query on remote hosts using a webservice
 * 
 * @author Frank L�tzenkirchen 
 * @author Jens Kupferschmidt
 */
public class MCRQueryClient {
    /** The logger */
    private final static Logger LOGGER = Logger.getLogger(MCRQueryClient.class);

    /** A list containing the aliases of all hosts */
    public final static List<String> ALL_HOSTS;

    /** A map from host alias to classes for access types */
    private static Properties accessclass = new Properties();

    static {
        // Read hosts.xml configuration file
        Element hosts = MCRURIResolver.instance().resolve("resource:hosts.xml");

        ALL_HOSTS = new ArrayList<String>();
        List children = hosts.getChildren();
        for (int i = 0; i < children.size(); i++) {
            Element host = (Element) (children.get(i));
            String classname = host.getAttributeValue("class");
            if ((classname == null) || ((classname = classname.trim()).length() == 0)) {
                classname = "org.mycore.services.fieldquery.MCRQueryClientWebService";
                LOGGER.warn("The class attribute for the host is null or empty, MCRQueryClientWebService was set.");
            }
            MCRQueryClientInterface qi = null;
            try {
                qi = (MCRQueryClientInterface) Class.forName(classname).newInstance();
            } catch (ClassNotFoundException e) {
                throw new MCRException(classname + " ClassNotFoundException",e);
            } catch (IllegalAccessException e) {
                throw new MCRException(classname + " IllegalAccessException",e);
            } catch (InstantiationException e) {
                throw new MCRException(classname + " InstantiationException",e);
            }
            qi.init(host);
            String alias = qi.getAlias();
            LOGGER.debug("Host " + alias + " uses class "+classname);
            accessclass.put(alias, qi);
            ALL_HOSTS.add(alias);
        }
    }

    /**
     * Executes a query on the given list of remote hosts
     * and merges the results to the given result list.
     * 
     * @param query the query to execute
     * @param results the result list to add the hits to
     */
    static void search(MCRQuery query, MCRResults results) {
        List hosts = query.getHosts();
        if (hosts.isEmpty())
            return;
        query.setHosts(null);

        if (!query.getSortBy().isEmpty())
            query.setMaxResults(0);

        Document xml = query.buildXML();
        org.w3c.dom.Document inDoc = null;
        try {
            inDoc = new DOMOutputter().output(xml);
        } catch (JDOMException ex) {
            String msg = "Could not convert query JDOM to DOM";
            LOGGER.error(msg, ex);
        }

        for (int i = 0; i < hosts.size(); i++) {
            String alias = (String) (hosts.get(i));
            MCRQueryClient.search(alias, inDoc, results);
        }

    }

    /**
     * Executes a query on a single remote host using the webservice.
     * 
     * @param hostAlias the alias of the remote host as defined in hosts.xml
     * @param inDoc the query as W3C DOM document
     * @param results the result list to add the hits to
     */
    private static void search(String hostAlias, org.w3c.dom.Document inDoc, MCRResults results) {
        if (!accessclass.containsKey(hostAlias)) {
            String msg = "No configuration for host " + hostAlias;
            throw new MCRConfigurationException(msg);
        }
        LOGGER.info("Starting remote query at host " + hostAlias);
        MCRQueryClientInterface qi = (MCRQueryClientInterface) accessclass.get(hostAlias);
        qi.search(inDoc,results);
    }

    /**
     * Retrieves an Object from remote host using the webservice.
     * 
     * @param hostAlias the alias of the remote host as defined in hosts.xml
     * @param ID   the ID of the Object to retrieve
     * 
     */
    public static org.w3c.dom.Document doRetrieveObject(String hostAlias, String ID) {
        if (!accessclass.containsKey(hostAlias)) {
            String msg = "No configuration for host " + hostAlias;
            throw new MCRConfigurationException(msg);
        }
        LOGGER.info("Starting remote retrieval at host " + hostAlias);
        MCRQueryClientInterface qi = (MCRQueryClientInterface) accessclass.get(hostAlias);
        return qi.doRetrieveObject(ID);
    }

    /**
     * Retrieves an classification part from remote host using the WebService.
     * 
     * @param hostAlias the alias of the remote host as defined in hosts.xml
     * @param level   the level of the classification to retrieve
     * @param type   the type of the classification to retrieve
     * @param classID   the class ID of the classification to retrieve
     * @param categID   the category ID of the classification to retrieve
     * @param format
     *             of retrieved classification, valid values are: editor['['formatAlias']']|metadata
     * @return the classification document
     */
    public static org.w3c.dom.Document doRetrieveClassification(String hostAlias, String level, String type, String classID, String categID, String format) {
        if (!accessclass.containsKey(hostAlias)) {
            String msg = "No configuration for host " + hostAlias;
            throw new MCRConfigurationException(msg);
        }
        LOGGER.info("Starting remote retrieval at host " + hostAlias);
        MCRQueryClientInterface qi = (MCRQueryClientInterface) accessclass.get(hostAlias);
        return qi.doRetrieveClassification(level,type,classID,categID,format);
    }
    
    /**
     * Retrieves an link from remote host using the WebService.
     * 
     * @param hostAlias
     *            the alias of the remote host as defined in hosts.xml
     * @param from
     *            the source of the link
     * @param to
     *            the target of the link
     * @param type
     *            the type of the link
     * @return the mcr:result document
     */
    public static org.w3c.dom.Document doRetrieveLinks(String hostAlias, String from, String to, String type) {
        if (!accessclass.containsKey(hostAlias)) {
            String msg = "No configuration for host " + hostAlias;
            throw new MCRConfigurationException(msg);
        }
        LOGGER.info("Starting remote retrieval at host " + hostAlias);
        MCRQueryClientInterface qi = (MCRQueryClientInterface) accessclass.get(hostAlias);
        return qi.doRetrieveLinks(from,to,type);
    }
    
}
