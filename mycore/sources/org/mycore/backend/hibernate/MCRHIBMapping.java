/*
 * $RCSfile: MCRHIBMapping.java,v $
 * $Revision: 1.33 $ $Date: 2006/11/23 06:41:48 $
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

import java.util.Enumeration;
import java.util.Properties;

import org.hibernate.cfg.Configuration;
import org.hibernate.type.BlobType;
import org.hibernate.type.IntegerType;
import org.hibernate.type.LongType;
import org.hibernate.type.StringType;
import org.hibernate.type.TimestampType;
import org.mycore.common.MCRConfiguration;

/**
 * Creater class for mapping files This class generates the xml mapping for the
 * hibernate configuration and adds each file to the current configuration
 * 
 * @author Arne Seifert
 */
public class MCRHIBMapping {
    private StringType dbString = new StringType();

    private IntegerType dbInt = new IntegerType();

    private LongType dbLong = new LongType();

    private BlobType dbBlob = new BlobType();

    private TimestampType dbTimestamp = new TimestampType();

    MCRConfiguration config = MCRConfiguration.instance();

    public void generateTables(Configuration cfg) {
        try {
            MCRTableGenerator map;

            // Category
            map = new MCRTableGenerator(config.getString("MCR.classifications_store_sql_table_categ", "MCRCATEG"), "org.mycore.backend.hibernate.tables.MCRCATEG", "", 2);
            map.addIDColumn("id", "ID", dbString, 128, "assigned", true);
            map.addIDColumn("clid", "CLID", dbString, 64, "assigned", true);
            map.addColumn("pid", "PID", dbString, 128, false, false, false);
            map.addColumn("url", "URL", dbString, 254, true, false, false);
            cfg.addXML(map.getTableXML());

            // Categorylabel
            map = new MCRTableGenerator(config.getString("MCR.classifications_store_sql_table_categlabel", "MCRCATEGLAB"), "org.mycore.backend.hibernate.tables.MCRCATEGLABEL", "", 3);
            map.addIDColumn("id", "ID", dbString, 128, "assigned", false);
            map.addIDColumn("clid", "CLID", dbString, 64, "assigned", false);
            map.addIDColumn("lang", "LANG", dbString, 8, "assigned", false);
            map.addColumn("text", "TEXT", dbString, 254, true, false, false);
            map.addColumn("mcrdesc", "MCRDESC", dbString, 254, true, false, false);
            cfg.addXML(map.getTableXML());

            // Classification
            map = new MCRTableGenerator(config.getString("MCR.classifications_store_sql_table_class", "MCRCLASS"), "org.mycore.backend.hibernate.tables.MCRCLASS", "", 1);
            map.addIDColumn("id", "ID", dbString, 64, "assigned", true);
            cfg.addXML(map.getTableXML());

            // Classification Label
            map = new MCRTableGenerator(config.getString("MCR.classifications_store_sql_table_classlabel", "MCRCLASSLAB"), "org.mycore.backend.hibernate.tables.MCRCLASSLABEL", "", 2);
            map.addIDColumn("id", "ID", dbString, 64, "assigned", false);
            map.addIDColumn("lang", "LANG", dbString, 8, "assigned", false);
            map.addColumn("text", "TEXT", dbString, 254, false, false, false);
            map.addColumn("mcrdesc", "MCRDESC", dbString, 254, false, false, false);
            cfg.addXML(map.getTableXML());

            // CStore
            map = new MCRTableGenerator(config.getString("MCR.content_store_sql_table", "MCRCSTORE"), "org.mycore.backend.hibernate.tables.MCRCSTORE", "", 1);
            map.addIDColumn("storageid", "STORAGEID", dbInt, 0, "assigned", true);
            map.addColumn("content", "CONTENT", dbBlob, 0, false, false, false);
            cfg.addXML(map.getTableXML());

            // FS Nodes
            map = new MCRTableGenerator(config.getString("MCR.IFS.FileMetadataStore.SQL.TableName", "MCRFSNODES"), "org.mycore.backend.hibernate.tables.MCRFSNODES", "", 1);
            map.addIDColumn("id", "ID", dbString, 16, "assigned", false);
            map.addColumn("pid", "PID", dbString, 16, false, false, false);
            map.addColumn("type", "TYPE", dbString, 1, true, false, false);
            map.addColumn("owner", "OWNER", dbString, 64, true, false, false);
            map.addColumn("name", "NAME", dbString, 250, true, false, false);
            map.addColumn("label", "LABEL", dbString, 250, false, true, false);
            map.addColumn("size", "SIZE", dbLong, 0, true, false, false);
            map.addColumn("date", "DATE", dbTimestamp, 0, false, false, false);
            map.addColumn("storeid", "STOREID", dbString, 32, false, false, false);
            map.addColumn("storageid", "STORAGEID", dbString, 250, false, false, false);
            map.addColumn("fctid", "FCTID", dbString, 32, false, false, false);
            map.addColumn("md5", "MD5", dbString, 32, false, false, false);
            map.addColumn("numchdd", "NUMCHDD", dbInt, 0, false, false, false);
            map.addColumn("numchdf", "NUMCHDF", dbInt, 0, false, false, false);
            map.addColumn("numchtd", "NUMCHTD", dbInt, 0, false, false, false);
            map.addColumn("numchtf", "NUMCHTF", dbInt, 0, false, false, false);
            cfg.addXML(map.getTableXML());

            // Group Admins
            map = new MCRTableGenerator(config.getString("MCR.users_store_sql_table_group_admins", "MCRGROUPADMINS"), "org.mycore.backend.hibernate.tables.MCRGROUPADMINS", "", 3);
            map.addIDColumn("gid", "GID", dbString, 20, "native", false);
            map.addIDColumn("userid", "USERID", dbString, 20, "native", false);
            map.addIDColumn("groupid", "GROUPID", dbString, 20, "native", false);
            cfg.addXML(map.getTableXML());

            // Group Members
            map = new MCRTableGenerator(config.getString("MCR.users_store_sql_table_group_members", "MCRGROUPMEMBERS"), "org.mycore.backend.hibernate.tables.MCRGROUPMEMBERS", "", 3);
            map.addIDColumn("gid", "GID", dbString, 20, "native", false);
            map.addIDColumn("userid", "USERID", dbString, 20, "native", false);
            cfg.addXML(map.getTableXML());

            // Group
            map = new MCRTableGenerator(config.getString("MCR.users_store_sql_table_groups", "MCRGROUPS"), "org.mycore.backend.hibernate.tables.MCRGROUPS", "", 1);
            map.addIDColumn("gid", "GID", dbString, 20, "assigned", false);
            map.addColumn("creator", "CREATOR", dbString, 20, true, false, false);
            map.addColumn("creationdate", "CREATIONDATE", dbTimestamp, 0, true, false, false);
            map.addColumn("modifieddate", "MODIFIEDDATE", dbTimestamp, 0, true, false, false);
            map.addColumn("description", "DESCRIPTION", dbString, 200, true, false, false);
            cfg.addXML(map.getTableXML());

            // Link Href
            map = new MCRTableGenerator(config.getString("MCR.linktable_store_sql_table_href", "MCRLINKHREF"), "org.mycore.backend.hibernate.tables.MCRLINKHREF", "", 3);
            map.addIDColumn("mcrfrom", "MCRFROM", dbString, 64, "assigned", false);
            map.addIDColumn("mcrto", "MCRTO", dbString, 194, "assigned", false);
            map.addIDColumn("mcrtype", "MCRTYPE", dbString, 75, "assigned", false);
            map.addColumn("mcrattr", "MCRATTR", dbString, 194, false, false, false);
            cfg.addXML(map.getTableXML());

            // URN store
            map = new MCRTableGenerator(config.getString("MCR.urn_store_sql_table", "MCRURN"), "org.mycore.backend.hibernate.tables.MCRURN", "", 2);
            map.addIDColumn("mcrid", "MCRID", dbString, 64, "assigned", false);
            map.addIDColumn("mcrurn", "MCRURN", dbString, 194, "assigned", false);
            cfg.addXML(map.getTableXML());

            // User
            map = new MCRTableGenerator(config.getString("MCR.users_store_sql_table_users", "MCRUSERS"), "org.mycore.backend.hibernate.tables.MCRUSERS", "", 2);
            map.addIDColumn("numid", "NUMID", dbInt, 0, "assigned", false);
            map.addIDColumn("uid", "UID", dbString, 20, "assigned", false);
            map.addColumn("creator", "CREATOR", dbString, 20, true, false, false);
            map.addColumn("creationdate", "CREATIONDATE", dbTimestamp, 0, false, false, false);
            map.addColumn("modifieddate", "MODIFIEDDATE", dbTimestamp, 0, false, false, false);
            map.addColumn("description", "DESCRIPTION", dbString, 200, false, false, false);
            map.addColumn("passwd", "PASSWD", dbString, 128, true, false, false);
            map.addColumn("enabled", "ENABLED", dbString, 8, true, false, false);
            map.addColumn("upd", "UPD", dbString, 8, true, false, false);
            map.addColumn("salutation", "SALUTATION", dbString, 24, false, false, false);
            map.addColumn("firstname", "FIRSTNAME", dbString, 64, false, false, false);
            map.addColumn("lastname", "LASTNAME", dbString, 32, false, false, false);
            map.addColumn("street", "STREET", dbString, 64, false, false, false);
            map.addColumn("city", "CITY", dbString, 32, false, false, false);
            map.addColumn("postalcode", "POSTALCODE", dbString, 32, false, false, false);
            map.addColumn("country", "COUNTRY", dbString, 32, false, false, false);
            map.addColumn("state", "STATE", dbString, 32, false, false, false);
            map.addColumn("institution", "INSTITUTION", dbString, 64, false, false, false);
            map.addColumn("faculty", "FACULTY", dbString, 64, false, false, false);
            map.addColumn("department", "DEPARTMENT", dbString, 64, false, false, false);
            map.addColumn("institute", "INSTITUTE", dbString, 64, false, false, false);
            map.addColumn("telephone", "TELEPHONE", dbString, 32, false, false, false);
            map.addColumn("fax", "FAX", dbString, 32, false, false, false);
            map.addColumn("email", "EMAIL", dbString, 64, false, false, false);
            map.addColumn("cellphone", "CELLPHONE", dbString, 32, false, false, false);
            map.addColumn("primgroup", "PRIMGROUP", dbString, 20, true, false, false);
            cfg.addXML(map.getTableXML());

            // XML Table
            map = new MCRTableGenerator(config.getString("MCR.xml_store_sql_table", "MCRXMLTABLE"), "org.mycore.backend.hibernate.tables.MCRXMLTABLE", "", 3);
            map.addIDColumn("id", "MCRID", dbString, 64, "assigned", false);
            map.addIDColumn("version", "MCRVERSION", dbInt, 64, "assigned", false);
            map.addIDColumn("type", "MCRTYPE", dbString, 64, "assigned", false);
            map.addColumn("xml", "MCRXML", dbBlob, 7000000, false, false, false);
            cfg.addXML(map.getTableXML());

            // create additional tables, that are defined in hibernate standard
            // hbm.xml-mapping files
            // and configurable via properties like
            // MCR.hibernate.hbm.xml.TABLENAME=TABLENAME.hbm.xml
            // the -.hbm.xml files must be copied in the classpath before
            // starting ant create.hibernate or ant create.metastore
            Properties hbmxmlProps = config.getProperties("MCR.hibernate.hbm.xml");
            for (Enumeration en = hbmxmlProps.propertyNames(); en.hasMoreElements();) {
                cfg.addResource(config.getString((String) en.nextElement()));
            }

            cfg.createMappings();
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException("couldn't create hibernate mappings");
        }
    }
}
