/*
 * 
 * $Revision: 13085 $ $Date: 2008-02-06 18:27:24 +0100 (Mi, 06. Feb 2008) $
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

package org.mycore.backend.hibernate.tables;

import org.mycore.common.MCRPersistenceException;

public class MCRCATEG {
    private MCRCATEGPK key;

    private String pid;

    private String url;

    public MCRCATEG() {
        this.key = new MCRCATEGPK();
    }

    public MCRCATEG(String id, String clid, String pid, String url) throws MCRPersistenceException {
        if ((id == null) || (id.trim().length() == 0) || (clid == null) || (clid.trim().length() == 0)) {
            throw new MCRPersistenceException("The label of a category has an empty ID, CLID or lang element.");
            }
        this.key = new MCRCATEGPK(id, clid);
        this.pid = pid;
        this.url = url;
    }

    /**
     * @hibernate.property columns="PRIMARY KEY" not-null="true" update="true"
     */
    public MCRCATEGPK getKey() {
        return key;
    }

    public void setKey(MCRCATEGPK key) {
        this.key = key;
    }

    /**
     * @hibernate.property columns="CLID" not-null="true" update="true"
     */
    public String getClid() {
        return key.getClid();
    }

    public void setClid(String clid) {
        key.setClid(clid);
    }

    /**
     * @hibernate.property columns="ID" not-null="true" update="true"
     */
    public String getId() {
        return key.getId();
    }

    public void setId(String id) {
        key.setId(id);
    }

    /**
     * @hibernate.property column="PID" not-null="true" update="true"
     */
    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    /**
     * @hibernate.property column="URL" not-null="true" update="true"
     */
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
