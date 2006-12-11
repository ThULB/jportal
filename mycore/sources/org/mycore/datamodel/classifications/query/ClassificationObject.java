/*
 * $RCSfile: ClassificationObject.java,v $
 * $Revision: 1.3 $ $Date: 2006/11/24 15:39:10 $
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

package org.mycore.datamodel.classifications.query;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Thomas Scheffler (yagee)
 * 
 * @version $Revision: 1.3 $ $Date: 2006/11/24 15:39:10 $
 */
public class ClassificationObject {

    String id;

    List<Label> labels;
    List<Category> catgegories;

    public List<Category> getCategories() {
        if (catgegories == null) {
            catgegories = new ArrayList<Category>();
        }
        return catgegories;
    }

    public void setCatgegories(List<Category> catgegories) {
        this.catgegories = catgegories;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Label> getLabels() {
        if (labels == null) {
            labels = new ArrayList<Label>();
        }
        return labels;
    }

    public void setLabels(List<Label> labels) {
        this.labels = labels;
    }

    public boolean equals(Object arg0) {
        if (!(arg0 instanceof ClassificationObject)) {
            return false;
        }
        ClassificationObject o = (ClassificationObject) arg0;
        return o.getId().equals(getId());
    }

    public int hashCode() {
        return getId().hashCode();
    }

}
