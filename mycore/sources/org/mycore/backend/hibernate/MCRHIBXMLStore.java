/*
 * 
 * $Revision: 13740 $ $Date: 2008-07-11 19:07:43 +0200 (Fr, 11 Jul 2008) $
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

package org.mycore.backend.hibernate;

import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import org.mycore.backend.hibernate.tables.MCRXMLTABLE;
import org.mycore.backend.hibernate.tables.MCRXMLTABLEPK;
import org.mycore.common.MCRConfiguration;
import org.mycore.common.MCRPersistenceException;
import org.mycore.datamodel.common.MCRXMLTableInterface;
import org.mycore.datamodel.metadata.MCRObjectID;

/**
 * This class implements the MCRXMLInterface.
 */
public class MCRHIBXMLStore implements MCRXMLTableInterface {
    // logger
    static Logger logger = Logger.getLogger(MCRHIBXMLStore.class.getName());

    private String classname = "org.mycore.backend.hibernate.tables.MCRXMLTABLE";

    private String type;

    /**
     * The constructor for the class MCRHIBXMLStore.
     */
    public MCRHIBXMLStore() {
    }

    private Session getSession() {
        return MCRHIBConnection.instance().getSession();
    }

    /**
     * The initializer for the class MCRHIBXMLStore. It reads the configuration
     * and checks the table names and create the table if they does'n exist..
     * 
     * @param type
     *            the type String of the MCRObjectID
     * @exception MCRPersistenceException
     *                if the type is not correct
     */
    public final void init(String type) throws MCRPersistenceException {
        MCRConfiguration config = MCRConfiguration.instance();

        // Check the parameter
        if ((type == null) || ((type = type.trim()).length() == 0)) {
            throw new MCRPersistenceException("The type of the constructor" + " is null or empty.");
        }

        boolean test = config.getBoolean("MCR.Metadata.Type." + type, false);

        if (!test) {
            throw new MCRPersistenceException("The type " + type + " of the constructor" + " is false.");
        }

        this.type = type;
    }

    /**
     * The method create a new item in the datastore.
     * 
     * @param mcrid
     *            a MCRObjectID
     * @param xml
     *            a byte array with the XML file
     * @param version
     *            the version of the XML Blob as integer
     * @exception MCRPersistenceException
     *                the method arguments are not correct
     */
    public synchronized final void create(String mcrid, byte[] xml, int version) throws MCRPersistenceException {
        if (mcrid == null) {
            throw new MCRPersistenceException("The MCRObjectID is null.");
        }
        if ((xml == null) || (xml.length == 0)) {
            throw new MCRPersistenceException("The XML array is null or empty.");
        }

        Session session = getSession();
        MCRXMLTABLEPK pk = new MCRXMLTABLEPK(mcrid, version);
        MCRXMLTABLE tab = (MCRXMLTABLE) session.get(MCRXMLTABLE.class, pk);
        if (tab == null) {
            tab = new MCRXMLTABLE();
            tab.setKey(pk);
            tab.setType(this.type);
        }
        tab.setXmlByteArray(xml);
        logger.debug("Inserting " + mcrid + "/" + version + "/" + this.type + " into database MCRXMLTABLE");
        session.save(tab);
    }

    /**
     * The method remove a item for the MCRObjectID from the datastore.
     * 
     * @param mcrid
     *            a MCRObjectID
     * @param version
     *            the version of the XML Blob as integer
     * @exception MCRPersistenceException
     *                the method argument is not correct
     */
    public synchronized final void delete(String mcrid, int version) throws MCRPersistenceException {
        Session session = getSession();
        logger.debug("Deleting " + mcrid + "/" + version + " from database MCRXMLTABLE");
        session.delete(session.get(MCRXMLTABLE.class, new MCRXMLTABLEPK(mcrid, version)));
    }

    /**
     * The method update an item in the datastore.
     * 
     * @param mcrid
     *            a MCRObjectID
     * @param xml
     *            a byte array with the XML file
     * @param version
     *            the version of the XML Blob as integer
     * @exception MCRPersistenceException
     *                the method arguments are not correct
     */
    public synchronized final void update(String mcrid, byte[] xml, int version) throws MCRPersistenceException {
        Session session = getSession();
        MCRXMLTABLE xmlEntry = (MCRXMLTABLE) session.load(MCRXMLTABLE.class, new MCRXMLTABLEPK(mcrid, version));
        xmlEntry.setVersion(version);
        xmlEntry.setType(this.type);
        xmlEntry.setXmlByteArray(xml);
        logger.debug("Updateing " + mcrid + "/" + version + "/" + this.type + " in database");
        session.update(xmlEntry);
    }

    /**
     * The method retrieve a dataset for the given MCRObjectID and returns the
     * corresponding XML file as byte array.
     * 
     * @param mcrid
     *            a MCRObjectID
     * @param version
     *            the version of the XML Blob as integer
     * @return the XML-File as byte array or null
     * @exception MCRPersistenceException
     *                the method arguments are not correct
     */
    public final InputStream retrieve(String mcrid, int version) throws MCRPersistenceException {
        Session session = getSession();
        MCRXMLTABLEPK pk = new MCRXMLTABLEPK(mcrid, version);
        Blob blob = (Blob) session.createCriteria(MCRXMLTABLE.class).setProjection(Projections.property("xml")).add(Restrictions.eq("key", pk)).uniqueResult();
        try {
            return blob.getBinaryStream();
        } catch (SQLException e) {
            throw new MCRPersistenceException("Cannot get Blob for " + mcrid, e);
        }
    }

    /**
     * This method returns the next free ID number for a given MCRObjectID base.
     * This method ensures that any invocation returns a new, exclusive ID by
     * remembering the highest ID ever returned and comparing it with the
     * highest ID stored in the related index class.
     * 
     * @param project
     *            the project ID part of the MCRObjectID base
     * @param type
     *            the type ID part of the MCRObjectID base
     * 
     * @exception MCRPersistenceException
     *                if a persistence problem is occured
     * 
     * @return the next free ID number as a String
     */
    public final synchronized int getNextFreeIdInt(String project, String type) throws MCRPersistenceException {

        Session session = getSession();
        //TODO: SQL -> Criteria
        List<?> l = session.createQuery("select max(key.id) from " + classname + " where MCRID like '" + project + "_" + type + "%'").list();
        if (l.size() > 0 && l.get(0) != null) {
            String max = (String) l.get(0);
            if (max == null)
                return 1;
            return new MCRObjectID(max).getNumberAsInteger() + 1;
        }
        return 1;
    }

    /**
     * This method check that the MCRObjectID exist in this store.
     * 
     * @param mcrid
     *            a MCRObjectID
     * @param version
     *            the version of the XML Blob as integer
     * @return true if the MCRObjectID exist, else return false
     */
    public final boolean exist(String mcrid, int version) {
        MCRXMLTABLEPK pk = new MCRXMLTABLEPK(mcrid, version);
        if (getSession().get(MCRXMLTABLE.class, pk) != null) {
            return true;
        }
        return false;
    }

    /**
     * The method return a Array list with all stored MCRObjectID's of the XML
     * table of a MCRObjectID type.
     * 
     * @param type
     *            a MCRObjectID type string
     * @return a ArrayList of MCRObjectID's
     */
    public List<String> retrieveAllIDs(String type) {
        Session session = getSession();
        List<?> l;
        ArrayList<String> a = new ArrayList<String>();

        l = session.createQuery("select distinct(key.id) from MCRXMLTABLE where MCRTYPE = '" + type + "'").list();
        for (int t = 0; t < l.size(); t++) {
            a.add((String) l.get(t));
        }

        return a;
    }

    /**
     * The method return a Array list with all stored MCRObjectID's of the XML
     * table.
     * 
     * @return a ArrayList of MCRObjectID's
     */
    public List<String> retrieveAllIDs() {
        Session session = getSession();
        List<?> l;
        ArrayList<String> a = new ArrayList<String>();
        l = session.createQuery("select distinct(key.id) from MCRXMLTABLE").list();
        for (int t = 0; t < l.size(); t++) {
            a.add((String) l.get(t));
        }

        return a;
    }

    public static void test() {
        MCRHIBXMLStore store = new MCRHIBXMLStore();
        List<String> l = store.retrieveAllIDs(null);
        int t;

        for (t = 0; t < l.size(); t++) {
            logger.debug(l.get(0));
        }
    }
}
