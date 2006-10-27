/*
 * $RCSfile: MCRPRIVSLOOKUP.java,v $
 * $Revision: 1.8 $ $Date: 2005/09/28 07:29:46 $
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

/**
 * 
 * @deprecated
 */
public class MCRPRIVSLOOKUP {
    private MCRPRIVSLOOKUPPK key;

    public MCRPRIVSLOOKUP() {
        this.key = new MCRPRIVSLOOKUPPK();
    }

    public MCRPRIVSLOOKUP(String gid, String name) {
        this.key = new MCRPRIVSLOOKUPPK();
        key.setGid(gid);
        key.setName(name);
    }

    /**
     * @hibernate.property column="Primary Key" not-null="true" update="true"
     */
    public MCRPRIVSLOOKUPPK getKey() {
        return key;
    }

    public void setKey(MCRPRIVSLOOKUPPK key) {
        this.key = key;
    }

    /**
     * @hibernate.property column="key" not-null="true" update="true"
     */
    public String getGid() {
        return key.getGid();
    }

    public void setGid(String gid) {
        key.setGid(gid);
    }

    /**
     * @hibernate.property column="NAME" not-null="true" update="true"
     */
    public String getName() {
        return key.getName();
    }

    public void setName(String name) {
        key.setName(name);
    }
}
