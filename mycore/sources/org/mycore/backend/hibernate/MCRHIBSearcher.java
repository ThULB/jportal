/*
 * $RCSfile: MCRHIBSearcher.java,v $
 * $Revision: 1.27 $ $Date: 2006/11/27 15:18:51 $
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

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;
import org.hibernate.type.StringType;
import org.mycore.common.MCRConfiguration;
import org.mycore.parsers.bool.MCRCondition;
import org.mycore.services.fieldquery.MCRFieldDef;
import org.mycore.services.fieldquery.MCRFieldValue;
import org.mycore.services.fieldquery.MCRHit;
import org.mycore.services.fieldquery.MCRResults;
import org.mycore.services.fieldquery.MCRSearcher;
import org.mycore.services.fieldquery.MCRSortBy;

/**
 * Hibernate implementation of MCRSearcher
 * 
 * @author Arne Seifert
 * @author Frank L�tzenkirchen
 */
public class MCRHIBSearcher extends MCRSearcher {
    /** The logger */
    private static Logger LOGGER = Logger.getLogger(MCRHIBSearcher.class.getName());

    public static HashMap<String, String> indexClassMapping = new HashMap<String, String>();

    private String tableName;

    private String mappedClass;

    public void init(String ID) {
        super.init(ID);
        this.tableName = MCRConfiguration.instance().getString(prefix + "TableName");
        this.mappedClass = tableName + "Bean";
        indexClassMapping.put(index, "org.mycore.backend.query." + this.mappedClass);
        updateConfiguration();
    }

    protected void addToIndex(String entryID, String returnID, List fields) {
        MCRHIBQuery query = new MCRHIBQuery((String) indexClassMapping.get(index));
        Hashtable<MCRFieldDef, MCRFieldDef> used = new Hashtable<MCRFieldDef, MCRFieldDef>();

        query.setValue("setmcrid", entryID);
        query.setValue("setreturnid", returnID);

        for (int i = 0; i < fields.size(); i++) {
            MCRFieldValue fv = (MCRFieldValue) (fields.get(i));

            // Store only first occurrence of field
            if (used.containsKey(fv.getField())) {
                continue;
            }
            used.put(fv.getField(), fv.getField());

            query.setValue("set" + fv.getField().getName(), fv.getValue());
        }

        Session session = MCRHIBConnection.instance().getSession();
        Transaction tx = session.beginTransaction();

        try {
            session.saveOrUpdate(query.getQueryObject());
            tx.commit();
        } catch (Exception ex) {
            tx.rollback();
            LOGGER.error("addToIndex: " , ex);
        } finally {
             if ( session != null ) session.close();
        }
    }

    protected void removeFromIndex(String entryID) {
        Session session = MCRHIBConnection.instance().getSession();
        Transaction tx = session.beginTransaction();

        try {
            String query = new StringBuffer("delete ").append(mappedClass).append(" where MCRID = \'").append(entryID).append("\'").toString();
            session.createQuery(query).executeUpdate();
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            LOGGER.error("removeFromIndex:", e);
        } finally {
             if ( session != null ) session.close();
        }
    }

    public MCRResults search(MCRCondition condition, int maxResults, List sortBy, boolean addSortData) {
        Session session = MCRHIBConnection.instance().getSession();
        MCRResults results = new MCRResults();

        try {
            MCRHIBQuery hibquery = new MCRHIBQuery(condition, sortBy, (String) indexClassMapping.get(index));
            for (Iterator it = session.createQuery(hibquery.getHIBQuery()).iterate(); it.hasNext();) {
                MCRHIBQuery tmpquery = new MCRHIBQuery(it.next());
                MCRHit hit = new MCRHit((String) (tmpquery.getValue("getreturnid")));

                // Add hit sort data
                for (int j = 0; addSortData && (j < sortBy.size()); j++) {
                    MCRSortBy by = (MCRSortBy) (sortBy.get(j));
                    Object tmpObj = indexClassMapping.get(by.getField().getIndex());
                    if (tmpObj != null) {
                        MCRHIBQuery sortQuery = new MCRHIBQuery((String) tmpObj);
                        Object valueObj = sortQuery.getValue("get" + by.getField().getName());
                        String value;
                        if (valueObj instanceof java.sql.Date) {
                            value = ((java.sql.Date) valueObj).toString();
                        } else {
                            value = (String) valueObj;
                        }
                        hit.addSortData(new MCRFieldValue(by.getField(), value));
                    }
                }
                results.addHit(hit);
                if ((maxResults > 0) && (results.getNumHits() >= maxResults))
                    break;
            }
            results.setSorted((!addSortData) && (sortBy.size() > 0));
        } catch (Exception ex) {
            LOGGER.error("search: Exception ", ex);
        } finally {
             if ( session != null ) session.close();
        }

        return results;
    }

    /**
     * method updates the hibernate configuration and introduces the new table
     * for searching
     */
    public void updateConfiguration() {
        try {
            MCRHIBConnection hibconnection = MCRHIBConnection.instance();
            // update schema -> first time create table
            Configuration cfg = hibconnection.getConfiguration();

            if (!hibconnection.containsMapping(tableName)) {
                MCRTableGenerator map = new MCRTableGenerator(tableName, "org.mycore.backend.query." + mappedClass, "", 1);
                map.addIDColumn("mcrid", "MCRID", new StringType(), 64, "assigned", false);
                map.addColumn("returnid", "RETURNID", new StringType(), 64, true, false, false);
                List fds = MCRFieldDef.getFieldDefs(getIndex());
                for (int i = 0; i < fds.size(); i++) {
                    MCRFieldDef fd = (MCRFieldDef) (fds.get(i));
                    if (!MCRFieldDef.SEARCHER_HIT_METADATA.equals(fd.getSource()))
                        map.addColumn(fd.getName(), fd.getName(), hibconnection.getHibType(fd.getDataType()), 2147483647, false, false, false);
                }

                cfg.addXML(map.getTableXML());
                cfg.createMappings();
                hibconnection.buildSessionFactory(cfg);

                new SchemaUpdate(MCRHIBConnection.instance().getConfiguration()).execute(true, true);
            }
        } catch (Exception e) {
            LOGGER.error("updateConfiguration: catched error ", e);
        }
    }
}
