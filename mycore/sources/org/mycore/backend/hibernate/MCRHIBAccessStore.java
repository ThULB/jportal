/*
 * 
 * $Revision: 13279 $ $Date: 2008-03-18 09:13:47 +0100 (Di, 18 Mär 2008) $
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

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;

import org.mycore.access.mcrimpl.MCRAccessStore;
import org.mycore.access.mcrimpl.MCRRuleMapping;
import org.mycore.backend.hibernate.tables.MCRACCESS;
import org.mycore.backend.hibernate.tables.MCRACCESSPK;
import org.mycore.backend.hibernate.tables.MCRACCESSRULE;

/**
 * Hibernate implementation of acceess store to manage access rights
 * 
 * @author Arne Seifert
 * 
 */
public class MCRHIBAccessStore extends MCRAccessStore {
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat(sqlDateformat);

    public MCRHIBAccessStore() {
        createTables();
    }

    public String getRuleID(String objID, String ACPool) {

        Session session = MCRHIBConnection.instance().getSession();
        Criteria c = session.createCriteria(MCRACCESS.class).setProjection(Projections.property("rule.rid")).add(Restrictions.eq("key.objid", objID)).add(
                Restrictions.eq("key.acpool", ACPool));
        return (String) c.uniqueResult();
    }

    public void createTables() {
        try {
            // update schema -> first time create table
            new SchemaUpdate(MCRHIBConnection.instance().getConfiguration()).execute(true, true);
        } catch (Exception e) {
            logger.error("error at createTables()", e);
        }
    }

    /**
     * method creates a new AccessDefinition in db
     * 
     * @param rulemapping
     *            with values
     */
    public void createAccessDefinition(MCRRuleMapping rulemapping) {

        if (!existAccessDefinition(rulemapping.getPool(), rulemapping.getObjId())) {
            Session session = MCRHIBConnection.instance().getSession();
            MCRACCESSRULE accessRule = getAccessRule(rulemapping.getRuleId());
            if (accessRule==null){
                throw new NullPointerException("Cannot map a null rule.");
            }
            MCRACCESS accdef = new MCRACCESS();

            accdef.setKey(new MCRACCESSPK(rulemapping.getPool(), rulemapping.getObjId()));
            accdef.setRule(accessRule);
            accdef.setCreator(rulemapping.getCreator());
            accdef.setCreationdate(Timestamp.valueOf(DATE_FORMAT.format(rulemapping.getCreationdate())));
            session.save(accdef);
        }
    }

    /**
     * internal helper method to check existance of object
     * 
     * @param ruleid
     * @param pool
     * @param objid
     * @return boolean value
     */
    private boolean existAccessDefinition(String pool, String objid) {
        Session session = MCRHIBConnection.instance().getSession();
        MCRACCESSPK key = new MCRACCESSPK(pool, objid);
        List l = session.createCriteria(MCRACCESS.class).add(Restrictions.eq("key", key)).list();
        if (l.size() == 1) {
            return true;
        }
        return false;
    }

    public boolean existsRule(String objid, String pool) {
        Session session = MCRHIBConnection.instance().getSession();

        if (objid == null || objid.equals("")) {
            logger.warn("empty parameter objid in existsRule");
            return false;
        }

        StringBuffer query = new StringBuffer("select count(*) from MCRACCESS ").append("where key.objid like '").append(objid).append("'");
        if (pool != null && !pool.equals("")) {
            query.append(" and key.acpool like '").append(pool).append("'");
        }

        int count = ((Number) session.createQuery(query.toString()).iterate().next()).intValue();
        if (count > 0) {
            return true;
        }
        return false;
    }

    /**
     * delete given definition in db
     * 
     * @param rulemapping
     *            rule to be deleted
     */
    public void deleteAccessDefinition(MCRRuleMapping rulemapping) {

        Session session = MCRHIBConnection.instance().getSession();
        session.createQuery("delete MCRACCESS " + "where ACPOOL = '" + rulemapping.getPool() + "'" + " AND OBJID = '" + rulemapping.getObjId() + "'")
                .executeUpdate();
    }

    /**
     * update AccessDefinition in db for given MCRAccessData
     */
    public void updateAccessDefinition(MCRRuleMapping rulemapping) {
        Session session = MCRHIBConnection.instance().getSession();
        MCRACCESSRULE accessRule = getAccessRule(rulemapping.getRuleId());
        if (accessRule==null){
            throw new NullPointerException("Cannot map a null rule.");
        }
        // update
        MCRACCESS accdef = (MCRACCESS) session.get(MCRACCESS.class, new MCRACCESSPK(rulemapping.getPool(), rulemapping.getObjId()));
        accdef.setRule(accessRule);
        accdef.setCreator(rulemapping.getCreator());
        accdef.setCreationdate(Timestamp.valueOf(DATE_FORMAT.format(rulemapping.getCreationdate())));
        session.update(accdef);
    }

    /**
     * method returns AccessDefinition for given key values
     * 
     * @param ruleid
     *            name of rule
     * @param pool
     *            name of accesspool
     * @param objid
     *            objectid of MCRObject
     * @return MCRAccessData
     */
    public MCRRuleMapping getAccessDefinition(String pool, String objid) {

        Session session = MCRHIBConnection.instance().getSession();
        MCRRuleMapping rulemapping = new MCRRuleMapping();
        MCRACCESS data = ((MCRACCESS) session.createCriteria(MCRACCESS.class).add(Restrictions.eq("key", new MCRACCESSPK(pool, objid))).list().get(0));
        if (data != null) {
            rulemapping.setCreationdate(data.getCreationdate());
            rulemapping.setCreator(data.getCreator());
            rulemapping.setObjId(data.getKey().getObjid());
            rulemapping.setPool(data.getKey().getAcpool());
            rulemapping.setRuleId(data.getRule().getRid());
        }
        session.evict(data);
        return rulemapping;
    }

    public ArrayList getMappedObjectId(String pool) {

        Session session = MCRHIBConnection.instance().getSession();
        ArrayList<String> ret = new ArrayList<String>();

        List l = session.createQuery("from MCRACCESS where ACPOOL = '" + pool + "'").list();
        for (int i = 0; i < l.size(); i++) {
            ret.add(((MCRACCESS) l.get(i)).getKey().getObjid());
        }

        return ret;
    }

    public ArrayList getPoolsForObject(String objid) {

        Session session = MCRHIBConnection.instance().getSession();
        ArrayList<String> ret = new ArrayList<String>();
        List l = session.createQuery("from MCRACCESS where OBJID = '" + objid + "'").list();
        for (int i = 0; i < l.size(); i++) {
            MCRACCESS access = (MCRACCESS) l.get(i);
            ret.add(access.getKey().getAcpool());
        }

        return ret;
    }

    public ArrayList getDatabasePools() {

        ArrayList<String> ret = new ArrayList<String>();
        Session session = MCRHIBConnection.instance().getSession();
        List l = session.createCriteria(MCRACCESS.class).list();
        for (int i = 0; i < l.size(); i++) {
            if (!ret.contains(((MCRACCESS) l.get(i)).getKey().getAcpool())) {
                ret.add(((MCRACCESS) l.get(i)).getKey().getAcpool());
            }
        }
        return ret;
    }

    public List getDistinctStringIDs() {
        List ret = new ArrayList();
        Session session = MCRHIBConnection.instance().getSession();
        String query = "select distinct(key.objid) from MCRACCESS order by OBJID";
        ret = session.createQuery(query).list();
        return ret;
    }

    private static MCRACCESSRULE getAccessRule(String rid) {
        Session session = MCRHIBConnection.instance().getSession();
        return (MCRACCESSRULE) session.get(MCRACCESSRULE.class, rid);
    }
}
