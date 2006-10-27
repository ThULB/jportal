/*
 * $RCSfile: MCRSQLNBNManager.java,v $
 * $Revision: 1.16 $ $Date: 2006/02/19 20:20:50 $
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

package org.mycore.backend.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.mycore.common.MCRConfiguration;
import org.mycore.common.MCRPersistenceException;
import org.mycore.services.nbn.MCRNBN;
import org.mycore.services.nbn.MCRNBNManager;

/**
 * Provides persistency functions for managing NBN URNs, using tables in SQL for
 * persistent storage. This is still work in progress.
 * 
 * @author Frank L�tzenkirchen
 * @author Werner Gre�hoff
 * @version $Revision: 1.16 $ $Date: 2006/02/19 20:20:50 $
 * @deprecated
 */
public class MCRSQLNBNManager implements MCRNBNManager {
    // logger
    static Logger logger = Logger.getLogger(MCRSQLNBNManager.class);

    private static String table = MCRConfiguration.instance().getString("MCR.nbn_store_sql_table","MCRNBN");

    /**
     * Method MCRSQLNBNManager. Creates a new MCRNBNManager.
     */
    public MCRSQLNBNManager() {
        if (!tableExists()) {
            createTable();
        }
    }

    /**
     * Method tableExists. look, if NBN persistency table exists.
     * 
     * @return boolean true, if table exists, otherwise false.
     */
    private boolean tableExists() {
        logger.info("Looking for NBN table.");

        return MCRSQLConnection.doesTableExist(table);
    }

    /**
     * Method createTable. create the table for NBN persistence.
     */
    private void createTable() {
        MCRSQLConnection.justDoUpdate(new MCRSQLStatement(table).addColumn("NISS VARCHAR(12) NOT NULL PRIMARY KEY").addColumn("URL VARCHAR(250)").addColumn("AUTHOR VARCHAR(80) NOT NULL").addColumn("COMMENT BLOB").addColumn("DATE TIMESTAMP NOT NULL").addColumn("DOCUMENTID VARCHAR(64)").toCreateTableStatement());
        logger.info("NBN table created.");
    }

    /**
     * Method reserveURN. Reserves a NBN for later use. In a later step, that
     * NBN can be assigned to a document.
     * 
     * @param urn
     *            the NBN URN to be reserved.
     */
    public void reserveURN(MCRNBN urn) {
        MCRSQLConnection mcrConnection = MCRSQLConnectionPool.instance().getConnection();
        Date now = new Date();
        Connection connection = mcrConnection.getJDBCConnection();

        try {
            PreparedStatement statement;
            statement = connection.prepareStatement("insert into " + table + " values (?, ?, ?, ?, ?, ?)");

            statement.setString(1, urn.getNISSandChecksum());
            statement.setNull(2, Types.VARCHAR);
            statement.setString(3, urn.getAuthor());

            if (urn.getComment() == null) {
                statement.setNull(4, Types.VARCHAR);
            } else {
                statement.setString(4, urn.getComment());
            }

            statement.setTimestamp(5, new Timestamp(now.getTime()));
            statement.setNull(6, Types.VARCHAR);

            statement.execute();
            statement.close();
        } catch (Exception exc) {
            String msg = "Error in database while reserving a new NBN.";
            logger.info(msg);
            throw new MCRPersistenceException(msg, exc);
        } finally {
            mcrConnection.release();
        }

        logger.debug("NISS " + urn.getNISSandChecksum() + " reserved.");
    }

    /**
     * Method getURN. Gets an URN for a given URL
     * 
     * @param url
     *            the URL of the given document
     * @return MCRNBN the NBN URN for the given URL, or null
     */
    public MCRNBN getURN(String url) {
        MCRSQLConnection conn = MCRSQLConnectionPool.instance().getConnection();
        String niss = null;

        try {
            MCRSQLRowReader reader = conn.doQuery(new MCRSQLStatement(table).setCondition("URL", url).toSelectStatement());
            if (reader.next()) {
                niss = reader.getString("NISS");
            }
            reader.close();
        } finally {
            conn.release();
        }

        if (niss == null) {
            logger.debug("No URN found for URL " + url);
            return null;
        }

        return new MCRNBN(MCRNBN.getLocalPrefix() + niss);
    }

    /**
     * Method setURL. Sets the URL for the NBN URN given. This is the URL that
     * the NBN points to. The NBN has to be already reserved.
     * 
     * @param urn
     *            the NBN URN that represents the URL
     * @param url
     *            the URL the NBN points to
     */
    public void setURL(MCRNBN urn, String url) {
        MCRSQLConnection.justDoUpdate(new MCRSQLStatement(table).setValue("URL", url).setCondition("NISS", urn.getNISSandChecksum()).toUpdateStatement());
        logger.debug("URL " + url + " set for NISS " + urn.getNISSandChecksum());
    }

    /**
     * Method getURL. Gets the URL for the NBN URN given. This is the URL that
     * the NBN points to. If there is no URL for this NBN, the method returns
     * null.
     * 
     * @param urn
     *            the NBN URN that represents a URL
     * @return String the URL the NBN points to, or null
     */
    public String getURL(MCRNBN urn) {
        MCRSQLConnection conn = MCRSQLConnectionPool.instance().getConnection();
        String url = null;
        
        try {
            MCRSQLRowReader reader = conn.doQuery(new MCRSQLStatement(table).setCondition("NISS", urn.getNISSandChecksum()).toSelectStatement());
            if (reader.next()) {
                url = reader.getString("URL");
            }
            reader.close();
        } finally {
            conn.release();
        }

        if (url == null) {
            logger.debug("URN " + urn.getNISSandChecksum() + "not found.");
        }

        return url;
    }

    /**
     * Method getAuthor. Gets the Author for the NBN URN given.
     * 
     * @param urn
     *            the NBN URN that represents a URL
     * @return String the author
     */
    public String getAuthor(MCRNBN urn) {
        MCRSQLConnection conn = MCRSQLConnectionPool.instance().getConnection();
        String author = null;

        try {
            MCRSQLRowReader reader = conn.doQuery(new MCRSQLStatement(table).setCondition("NISS", urn.getNISSandChecksum()).toSelectStatement());
            if (reader.next()) {
                author = reader.getString("AUTHOR");
            }
            reader.close();
        } finally {
            conn.release();
        }

        if (author == null) {
            logger.debug("URN " + urn.getNISSandChecksum() + "not found.");
        }

        return author;
    }

    /**
     * Method getComment. Gets the Comment for the NBN URN given.
     * 
     * @param urn
     *            the NBN URN that represents a URL
     * @return String the Comment
     */
    public String getComment(MCRNBN urn) {
        MCRSQLConnection conn = MCRSQLConnectionPool.instance().getConnection();
        String comment = null;

        try {
            MCRSQLRowReader reader = conn.doQuery(new MCRSQLStatement(table).setCondition("NISS", urn.getNISSandChecksum()).toSelectStatement());
            if (reader.next()) {
                comment = reader.getString("COMMENT");
            }
            reader.close();
        } finally {
            conn.release();
        }

        if (comment == null) {
            logger.debug("URN " + urn.getNISSandChecksum() + "not found.");
        }

        return comment;
    }

    /**
     * Method getDate. Gets the timestamp for the NBN
     * 
     * @param urn
     *            the NBN
     * @return GregorianCalendar the date
     */
    public GregorianCalendar getDate(MCRNBN urn) {
        MCRSQLConnection conn = MCRSQLConnectionPool.instance().getConnection();
        GregorianCalendar date = null;
        
        try {
            MCRSQLRowReader reader = conn.doQuery(new MCRSQLStatement(table).setCondition("NISS", urn.getNISSandChecksum()).toSelectStatement());
            if (reader.next()) {
                date = reader.getDate("DATE");
            }
            reader.close();
        } finally {
            conn.release();
        }

        if (date == null) {
            logger.debug("URN " + urn.getNISSandChecksum() + "not found.");
        }

        return date;
    }

    /**
     * Method removeURN. Removes a stored NBN URN from the persistent datastore.
     * 
     * @param urn
     *            the NBN URN that should be removed
     */
    public void removeURN(MCRNBN urn) {
        MCRSQLConnection.justDoUpdate(new MCRSQLStatement(table).setCondition("NISS", urn.getNISSandChecksum()).toDeleteStatement());
    }

    /**
     * Method listURNs. Returns all URNs that match the given pattern. The
     * pattern may be null to select all stored URNs, or may be a pattern
     * containing '*' or '?' wildcard characters.
     * 
     * @param pattern
     *            the pattern the URNs should match, or null
     * @return a Map containing the matched URNs as keys, and their URLs as
     *         values
     */
    public Map listURNs(String pattern) {
        Map results = new HashMap();
        MCRSQLConnection connection = MCRSQLConnectionPool.instance().getConnection();

        try {
            PreparedStatement statement;

            if (pattern != null) {
                String sqlPattern = null;
                sqlPattern = pattern.replace('?', '_').replace('*', '%');
                statement = connection.getJDBCConnection().prepareStatement("select * from " + table + " where niss like ?");
                logger.debug("Using 'like'-statement: select * from " + table + " where niss like " + sqlPattern);
                statement.setString(1, sqlPattern);
                logger.debug("Statement completed with " + sqlPattern);
            } else {
                statement = connection.getJDBCConnection().prepareStatement("select * from " + table);
            }

            ResultSet set = statement.executeQuery();
            logger.debug("Query executed.");

            ResultSetMetaData rsmd = set.getMetaData();
            logger.debug("Number of columns: " + rsmd.getColumnCount());
            logger.debug("Column 1: " + rsmd.getColumnTypeName(1));
            logger.debug("Column 2: " + rsmd.getColumnTypeName(2));

            while (set.next()) {
                results.put(set.getString(1), set.getString(2));
            }
        } catch (Exception exc) {
            String msg = "Error in database while executing query.";
            logger.info(msg);
            throw new MCRPersistenceException(msg, exc);
        }

        connection.release();

        return results;
    }

    /**
     * Method listReservedURNs. Returns all URNs that are reserved for later use
     * with a document.
     * 
     * @return a Set containing the URNs
     */
    public Set listReservedURNs() {
        Set results = new HashSet();
        MCRSQLConnection connection = MCRSQLConnectionPool.instance().getConnection();

        try {
            PreparedStatement statement = connection.getJDBCConnection().prepareStatement("select NISS from " + table + " where URL = NULL");
            ResultSet set = statement.executeQuery();

            while (set.next()) {
                results.add(set.getString(1));
            }
        } catch (Exception exc) {
            String msg = "Error in database while executing query.";
            logger.info(msg);
            throw new MCRPersistenceException(msg, exc);
        }

        connection.release();

        return results;
    }

    /**
     * Method getDocumentId. Gets the document id for the NBN
     * 
     * @param urn
     *            the NBN
     * @return String the document id
     */
    public String getDocumentId(MCRNBN urn) {
        MCRSQLConnection conn = MCRSQLConnectionPool.instance().getConnection();
        String documentId = null;

        try {
            MCRSQLRowReader reader = conn.doQuery(new MCRSQLStatement(table).setCondition("NISS", urn.getNISSandChecksum()).toSelectStatement());
            if (reader.next()) {
                documentId = reader.getString("DOCUMENTID");
            }
            reader.close();
        } finally {
            conn.release();
        }

        if (documentId == null) {
            logger.debug("URN " + urn.getNISSandChecksum() + "not found.");
        }

        return documentId;
    }

    /**
     * Sets the document id for the NBN URN given.
     * 
     * @param urn
     *            the NBN URN that represents the URL
     * @param documentId
     *            the document id the NBN points to
     */
    public void setDocumentId(MCRNBN urn, String documentId) {
        MCRSQLConnection.justDoUpdate(new MCRSQLStatement(table).setValue("DOCUMENTID", documentId).setCondition("NISS", urn.getNISSandChecksum()).toUpdateStatement());
        logger.debug("Document id " + documentId + " set for NISS " + urn.getNISSandChecksum());
    }

    /**
     * Finds the urn for a given document id
     * 
     * @param documentId
     *            the document id
     * @return the nbn or null
     */
    public MCRNBN getNBNByDocumentId(String documentId) {
        MCRSQLConnection connection = MCRSQLConnectionPool.instance().getConnection();

        try {
            PreparedStatement statement = connection.getJDBCConnection().prepareStatement("select NISS from " + table + " where DOCUMENTID = '" + documentId + "'");
            ResultSet set = statement.executeQuery();
            logger.debug("Got result set");

            if (set.next()) {
                logger.debug("Data set for id " + documentId + " found.");

                String NISS = set.getString(1);

                return new MCRNBN(MCRNBN.getLocalPrefix() + NISS);
            }
            return null;
        } catch (Exception exc) {
            String msg = "Error in database while executing query.";
            logger.info(msg);
            throw new MCRPersistenceException(msg, exc);
        } finally {
            connection.release();
        }
    }
}
