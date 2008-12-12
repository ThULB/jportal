/**
 * 
 * $Revision: 14437 $ $Date: 2008-11-18 15:39:31 +0100 (Di, 18. Nov 2008) $
 *
 * This file is part of ** M y C o R e **
 * Visit our homepage at http://www.mycore.de/ for details.
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
 * along with this program, normally in the file license.txt.
 * If not, write to the Free Software Foundation Inc.,
 * 59 Temple Place - Suite 330, Boston, MA  02111-1307 USA
 *
 **/
package org.mycore.datamodel.classifications2.impl;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.mycore.common.MCRConfiguration;
import org.mycore.common.MCRSessionMgr;
import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryDAOFactory;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.classifications2.MCRLabel;

/**
 * @author Thomas Scheffler (yagee)
 * 
 * @version $Revision: 14437 $ $Date: 2008-11-18 15:39:31 +0100 (Di, 18. Nov 2008) $
 * @since 2.0
 */
public abstract class MCRAbstractCategoryImpl implements MCRCategory {

    protected MCRCategory root;

    protected MCRCategory parent;

    private MCRCategoryID id;

    private URI URI;

    protected Collection<MCRLabel> labels;

    protected List<MCRCategory> children;

    protected final ReentrantReadWriteLock childrenLock = new ReentrantReadWriteLock();

    private static final String defaultLang = MCRConfiguration.instance().getString("MCR.Metadata.DefaultLang", "en");

    public MCRAbstractCategoryImpl() {
        super();
    }

    public List<MCRCategory> getChildren() {
        childrenLock.readLock().lock();
        boolean childrenPresent = true;
        if (children == null) {
            childrenPresent = false;
            childrenLock.readLock().unlock();
            setChildren(MCRCategoryDAOFactory.getInstance().getChildren(this.id));
        }
        if (childrenPresent) {
            childrenLock.readLock().unlock();
        }
        return children;
    }

    abstract void setChildren(List<MCRCategory> children);

    public MCRCategoryID getId() {
        return id;
    }

    public Collection<MCRLabel> getLabels() {
        return labels;
    }

    public MCRCategory getRoot() {
        if (getId().isRootID())
            return this;
        if (this.root == null && getParent() != null) {
            this.root = getParent().getRoot();
        }
        return root;
    }

    public URI getURI() {
        return URI;
    }

    public boolean hasChildren() {
        childrenLock.readLock().lock();
        try {
            if (children != null) {
                return (children.size() == 0) ? false : true;
            }
        } finally {
            childrenLock.readLock().unlock();
        }
        return MCRCategoryDAOFactory.getInstance().hasChildren(this.id);
    }

    public final boolean isCategory() {
        return !isClassification();
    }

    public final boolean isClassification() {
        return getId().isRootID();
    }

    public void setId(MCRCategoryID id) {
        this.id = id;
    }

    public void setURI(URI uri) {
        URI = uri;
    }

    public MCRCategory getParent() {
        return parent;
    }

    public void setParent(MCRCategory parent) {
        if (this.parent == parent) {
            return;
        }
        detachFromParent();
        this.parent = parent;
        if (parent != null) {
            parent.getChildren().add(this);
        }
    }

    /**
     * 
     */
    void detachFromParent() {
        if (this.parent != null) {
            // remove this from current parent
            this.parent.getChildren().remove(this);
            this.parent = null;
        }
    }

    public MCRLabel getCurrentLabel() {
        MCRLabel label = getLabel(MCRSessionMgr.getCurrentSession().getCurrentLanguage());
        if (label != null)
            return label;
        label = getLabel(defaultLang);
        if (label != null)
            return label;
        return labels.iterator().next();
    }

    public MCRLabel getLabel(String lang) {
        for (MCRLabel label : labels) {
            if (label.getLang().equals(lang))
                return label;
        }
        return null;
    }

}