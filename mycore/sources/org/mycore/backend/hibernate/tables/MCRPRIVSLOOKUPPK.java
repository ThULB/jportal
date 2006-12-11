/*
 * $RCSfile: MCRPRIVSLOOKUPPK.java,v $
 * $Revision: 1.4 $ $Date: 2006/02/15 15:47:02 $
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

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * 
 * @deprecated
 */
public class MCRPRIVSLOOKUPPK implements Serializable {
    private String gid;

    private String name;

    /**
     * @hibernate.property column="GID" not-null="true" update="true"
     */
    public String getGid() {
        return gid;
    }

    public void setGid(String gid) {
        this.gid = gid;
    }

    /**
     * @hibernate.property column="NAME" not-null="true" update="true"
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean equals(Object other) {
        if (!(other instanceof MCRPRIVSLOOKUPPK)) {
            return false;
        }

        MCRPRIVSLOOKUPPK castother = (MCRPRIVSLOOKUPPK) other;

        return new EqualsBuilder().append(this.getGid(), castother.getGid()).append(this.getName(), castother.getName()).isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder().append(getGid()).append(getName()).toHashCode();
    }
}
