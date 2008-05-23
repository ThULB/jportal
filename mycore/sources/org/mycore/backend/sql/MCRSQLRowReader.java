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

package org.mycore.backend.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.GregorianCalendar;

import org.apache.log4j.Logger;
import org.mycore.common.MCRPersistenceException;

/**
 * Instances of this class are used to read the rows of a result set when an SQL
 * query is done using MCRSQLConnection. This is a wrapper around
 * java.sql.ResultSet that provides some convenience methods.
 * 
 * @author Frank L�tzenkirchen
 * @version $Revision: 13085 $ $Date: 2008-02-06 18:27:24 +0100 (Mi, 06 Feb 2008) $
 * @see java.sql.ResultSet
 * @see MCRSQLConnection#doQuery( String )
 */
public class MCRSQLRowReader {
    protected final static Logger LOGGER = Logger.getLogger(MCRSQLRowReader.class);

    /** The wrapped JDBC result set */
    protected ResultSet rs;

    /**
     * Creates a new MCRSQLRowReader. This constructor is called by
     * MCRSQLConnection methods that execute an SQL query.
     * 
     * @see MCRSQLConnection#doQuery( String )
     */
    MCRSQLRowReader(ResultSet rs) {
        this.rs = rs;
    }

    /**
     * Points the cursor to the next result row, returning true if there is such
     * a next row.
     * 
     * @see java.sql.ResultSet#next()
     * @return true, if there was a next row; false, if the end is reached
     */
    public boolean next() throws MCRPersistenceException {
        try {
            return rs.next();
        } catch (SQLException ex) {
            throw new MCRPersistenceException("Could not call next() on JDBC result set", ex);
        }
    }

    /**
     * Returns the value of a column in the current result row as a String, or
     * null.
     * 
     * @param index
     *            the number of the column in the result row
     * @return the String value of a column in the current result row, or null
     */
    public String getString(int index) throws MCRPersistenceException {
        try {
            String value = rs.getString(index);

            return (rs.wasNull() ? null : value);
        } catch (SQLException ex) {
            throw new MCRPersistenceException("Could not get value from JDBC result set", ex);
        }
    }

    public GregorianCalendar getDate(int index) throws MCRPersistenceException {
        try {
            Timestamp value = rs.getTimestamp(index);

            if (rs.wasNull()) {
                return null;
            }

            GregorianCalendar cal = new GregorianCalendar();
            cal.setTime(value);

            return cal;
        } catch (SQLException ex) {
            throw new MCRPersistenceException("Could not get value from JDBC result set", ex);
        }
    }

    /**
     * Returns the value of a column in the current result row as a String, or
     * null.
     * 
     * @param columnName
     *            the name of the column in the result row
     * @return the String value of a column in the current result row, or null
     */
    public String getString(String columnName) throws MCRPersistenceException {
        try {
            String value = rs.getString(columnName);

            return (rs.wasNull() ? null : value);
        } catch (SQLException ex) {
            throw new MCRPersistenceException("Could not get value from JDBC result set", ex);
        }
    }

    public GregorianCalendar getDate(String columnName) throws MCRPersistenceException {
        try {
            Timestamp value = rs.getTimestamp(columnName);

            if (rs.wasNull()) {
                return null;
            }

            GregorianCalendar cal = new GregorianCalendar();
            cal.setTime(value);

            return cal;
        } catch (SQLException ex) {
            throw new MCRPersistenceException("Could not get value from JDBC result set", ex);
        }
    }

    /**
     * Returns the value of a column in the current result rowa as an int.
     * 
     * @param index
     *            the number of the column in the result row
     * @return the int value of a column in the current result row, or
     *         Integer.MIN_VALUE if the column was null
     */
    public int getInt(int index) throws MCRPersistenceException {
        try {
            int value = rs.getInt(index);

            return (rs.wasNull() ? Integer.MIN_VALUE : value);
        } catch (SQLException ex) {
            throw new MCRPersistenceException("Could not get value from JDBC result set", ex);
        }
    }

    public long getLong(int index) throws MCRPersistenceException {
        try {
            long value = rs.getLong(index);

            return (rs.wasNull() ? Long.MIN_VALUE : value);
        } catch (SQLException ex) {
            throw new MCRPersistenceException("Could not get value from JDBC result set", ex);
        }
    }

    /**
     * Returns the value of a column in the current result rowa as an int.
     * 
     * @param columnName
     *            the name of the column in the result row
     * @return the int value of a column in the current result row, or
     *         Integer.MIN_VALUE if the column was null
     */
    public int getInt(String columnName) throws MCRPersistenceException {
        try {
            int value = rs.getInt(columnName);

            return (rs.wasNull() ? Integer.MIN_VALUE : value);
        } catch (SQLException ex) {
            throw new MCRPersistenceException("Could not get value from JDBC result set", ex);
        }
    }

    public long getLong(String columnName) throws MCRPersistenceException {
        try {
            long value = rs.getLong(columnName);

            return (rs.wasNull() ? Long.MIN_VALUE : value);
        } catch (SQLException ex) {
            throw new MCRPersistenceException("Could not get value from JDBC result set", ex);
        }
    }

    public synchronized void close() {
        if (rs == null) {
            return;
        }

        try {
            rs.close();
        } catch (SQLException ex) {
            LOGGER.debug("Could not close result set: " + ex.getMessage());
        } finally {
            rs = null;
        }
    }

    public void finalize() throws Throwable {
        close();
    }
}
