/**
 * 
 * $Revision: 14450 $ $Date: 2008-11-21 11:54:03 +0100 (Fr, 21. Nov 2008) $
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;

import org.mycore.backend.hibernate.MCRHIBConnection;
import org.mycore.common.MCRException;
import org.mycore.common.MCRPersistenceException;
import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryDAO;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.classifications2.MCRLabel;

/**
 * 
 * @author Thomas Scheffler (yagee)
 * 
 * @version $Revision: 14450 $ $Date: 2008-02-06 17:27:24 +0000 (Mi, 06 Feb
 *          2008) $
 * @since 2.0
 */
public class MCRCategoryDAOImpl implements MCRCategoryDAO {

    private static final int LEVEL_START_VALUE = 0;

    private static final int LEFT_START_VALUE = 0;

    private static long LAST_MODIFIED = System.currentTimeMillis();

    private static final Logger LOGGER = Logger.getLogger(MCRCategoryDAOImpl.class);

    private static final Class<MCRCategoryImpl> CATEGRORY_CLASS = MCRCategoryImpl.class;

    public void addCategory(MCRCategoryID parentID, MCRCategory category) {
        if (exist(category.getId())) {
            throw new MCRException("Cannot add category. A category with ID " + category.getId() + " allready exists");
        }
        int leftStart = LEFT_START_VALUE;
        int levelStart = LEVEL_START_VALUE;
        final MCRHIBConnection connection = MCRHIBConnection.instance();
        Session session = connection.getSession();
        MCRCategoryImpl parent = null;
        if (parentID != null) {
            parent = getByNaturalID(session, parentID);
            levelStart = parent.getLevel() + 1;
            leftStart = parent.getLeft() + 1;
        }
        LOGGER.debug("Calculating LEFT,RIGHT and LEVEL attributes...");
        final MCRCategoryImpl wrapCategory = MCRCategoryImpl.wrapCategory(category, parent, (parent == null) ? category.getRoot() : parent.getRoot());
        calculateLeftRightAndLevel(wrapCategory, leftStart, levelStart);
        // always add +1 for the current node
        int nodes = 1 + (wrapCategory.getRight() - wrapCategory.getLeft()) / 2;
        LOGGER.debug("Calculating LEFT,RIGHT and LEVEL attributes. Done! Nodes: " + nodes);
        if (parentID != null) {
            final int increment = nodes * 2;
            updateLeftRightValue(connection, parentID.getRootID(), leftStart, increment);
            parent.getChildren().add(category);
        }
        session.save(category);
        LOGGER.info(new StringBuilder("Category ").append(category.getId()).append(" saved.").toString());
        updateTimeStamp();
    }

    public void deleteCategory(MCRCategoryID id) {
        final MCRHIBConnection connection = MCRHIBConnection.instance();
        Session session = connection.getSession();
        LOGGER.debug("Will get: " + id);
        MCRCategoryImpl category = getByNaturalID(session, id);
        if (category == null) {
            throw new MCRPersistenceException("Category " + id + " was not found. Delete aborted.");
        }
        LOGGER.debug("Will delete: " + category.getId());
        MCRCategory parent = category.parent;
        category.detachFromParent();
        session.delete(category);
        if (parent != null) {
            LOGGER.debug("Left: " + category.getLeft() + " Right: " + category.getRight());
            // always add +1 for the currentNode
            int nodes = 1 + (category.getRight() - category.getLeft()) / 2;
            final int increment = nodes * -2;
            // decrement left and right values by nodes
            updateLeftRightValue(connection, category.getRootID(), category.getLeft(), increment);
        }
        updateTimeStamp();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mycore.datamodel.classifications2.MCRCategoryDAO#exist(org.mycore.datamodel.classifications2.MCRCategoryID)
     */
    public boolean exist(MCRCategoryID id) {
        Criteria criteria = MCRHIBConnection.instance().getSession().createCriteria(CATEGRORY_CLASS);
        criteria.setProjection(Projections.rowCount()).add(getCategoryCriterion(id));
        criteria.setCacheable(true);
        Number result = (Number) criteria.uniqueResult();
        if (result == null) {
            return false;
        }
        return result.intValue() > 0;
    }

    @SuppressWarnings("unchecked")
    public List<MCRCategory> getCategoriesByLabel(MCRCategoryID baseID, String lang, String text) {
        Session session = MCRHIBConnection.instance().getSession();
        Integer[] leftRight = getLeftRightValues(baseID);
        Query q = session.getNamedQuery(CATEGRORY_CLASS.getName() + ".byLabel");
        q.setString("rootID", baseID.getRootID());
        q.setInteger("left", leftRight[0]);
        q.setInteger("right", leftRight[1]);
        q.setString("lang", lang);
        q.setString("text", text);
        q.setCacheable(true);
        return (List<MCRCategory>) q.list();
    }

    @SuppressWarnings("unchecked")
    public MCRCategory getCategory(MCRCategoryID id, int childLevel) {
        Session session = MCRHIBConnection.instance().getSession();
        final boolean fetchAllChildren = childLevel < 0;
        Query q = null;
        if (id.isRootID()) {
            q = session.getNamedQuery(CATEGRORY_CLASS.getName() + (fetchAllChildren ? ".prefetchClassQuery" : ".prefetchClassLevelQuery"));
            if (!fetchAllChildren)
                q.setInteger("endlevel", childLevel);
            q.setString("classID", id.getRootID());
        } else {
            //normal category
            MCRCategoryImpl category = getByNaturalID(session, id);
            q = session.getNamedQuery(CATEGRORY_CLASS.getName() + (fetchAllChildren ? ".prefetchCategQuery" : ".prefetchCategLevelQuery"));
            if (!fetchAllChildren)
                q.setInteger("endlevel", category.getLevel() + childLevel);
            q.setString("classID", id.getRootID());
            q.setInteger("left", category.getLeft());
            q.setInteger("right", category.getLeft());
        }
        List<MCRCategoryImpl> result = q.list();
        return buildCategoryFromPrefetchedList(result);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mycore.datamodel.classifications2.MCRClassificationService#getChildren(org.mycore.datamodel.classifications2.MCRCategoryID)
     */
    @SuppressWarnings("unchecked")
    public List<MCRCategory> getChildren(MCRCategoryID cid) {
        LOGGER.debug("Get children of category: " + cid);
        if (!exist(cid)) {
            return new MCRCategoryImpl.ChildList(null, null);
        }
        Session session = MCRHIBConnection.instance().getSession();
        Criteria c = session.createCriteria(CATEGRORY_CLASS).add(
                Subqueries.propertyEq("parent", DetachedCriteria.forClass(CATEGRORY_CLASS).setProjection(Projections.property("internalID")).add(
                        getCategoryCriterion(cid))));
        c.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        c.setCacheable(true);
        return (List<MCRCategory>) c.list();
    }

    public List<MCRCategory> getParents(MCRCategoryID id) {
        // TODO: Make use of left and right value here
        Session session = MCRHIBConnection.instance().getSession();
        List<MCRCategory> parents = new ArrayList<MCRCategory>();
        MCRCategory category = getByNaturalID(session, id);
        while (category.getParent() != null) {
            category = category.getParent();
            parents.add(category);
        }
        return parents;
    }

    @SuppressWarnings("unchecked")
    public List<MCRCategoryID> getRootCategoryIDs() {
        Session session = MCRHIBConnection.instance().getSession();
        Criteria c = session.createCriteria(CATEGRORY_CLASS);
        c.add(Restrictions.eq("left", LEFT_START_VALUE));
        c.setProjection(Projections.projectionList().add(Projections.property("rootID")).add(Projections.property("categID")));
        c.setCacheable(true);
        List<Object[]> result = c.list();
        List<MCRCategoryID> classIds = new ArrayList<MCRCategoryID>(result.size());
        for (Object[] cat : result) {
            classIds.add(new MCRCategoryID(cat[0].toString(), cat[1].toString()));
        }
        return classIds;
    }

    @SuppressWarnings("unchecked")
    public List<MCRCategory> getRootCategories() {
        Session session = MCRHIBConnection.instance().getSession();
        Criteria c = session.createCriteria(CATEGRORY_CLASS);
        c.add(Restrictions.eq("left", LEFT_START_VALUE));
        c.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        c.setCacheable(true);
        List<MCRCategoryImpl> result=c.list();
        List<MCRCategory> classes=new ArrayList<MCRCategory>(result.size());
        for (MCRCategoryImpl cat:result){
            classes.add(copyDeep(cat, 0));
        }
        return classes;
    }

    public MCRCategory getRootCategory(MCRCategoryID baseID, int childLevel) {
        if (baseID.isRootID()) {
            return getCategory(baseID, childLevel);
        }
        Session session = MCRHIBConnection.instance().getSession();
        List<MCRCategory> parents = getParents(baseID);
        List<MCRCategoryImpl> parentsCopy = new ArrayList<MCRCategoryImpl>(parents.size());
        for (int i = parents.size() - 1; i >= 0; i--) {
            parentsCopy.add(copyDeep((MCRCategoryImpl) parents.get(i), 0));
        }
        MCRCategoryImpl root = parentsCopy.get(0);
        for (int i = 1; i < parentsCopy.size(); i++) {
            parentsCopy.get(i).setRoot(root);
            parentsCopy.get(i).setParent(parentsCopy.get(i - 1));
        }
        MCRCategoryImpl node = getByNaturalID(session, baseID);
        // prepare a temporary copy for the deepCopy process
        MCRCategoryImpl tempCopy = new MCRCategoryImpl();
        tempCopy.setInternalID(node.getInternalID());
        tempCopy.setId(node.getId());
        tempCopy.setLabels(node.getLabels());
        tempCopy.setLevel(node.getLevel());
        tempCopy.children = node.children;
        tempCopy.root = root;
        // attach deep node copy to its parent
        copyDeep(tempCopy, childLevel).setParent(parentsCopy.get(parentsCopy.size() - 1));
        // return root node
        return root;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mycore.datamodel.classifications2.MCRClassificationService#hasChildren(org.mycore.datamodel.classifications2.MCRCategoryID)
     */
    public boolean hasChildren(MCRCategoryID cid) {
        // SELECT * FROM MCRCATEGORY WHERE PARENTID=(SELECT INTERNALID FROM
        // MCRCATEGORY WHERE rootID=cid.getRootID() and ID...);
        return getNumberOfChildren(cid) > 0;
    }

    public void moveCategory(MCRCategoryID id, MCRCategoryID newParentID) {
        int index = getNumberOfChildren(newParentID);
        moveCategory(id, newParentID, index);
    }

    public void moveCategory(MCRCategoryID id, MCRCategoryID newParentID, int index) {
        final MCRHIBConnection connection = MCRHIBConnection.instance();
        Session session = connection.getSession();
        MCRCategoryImpl subTree = getByNaturalID(session, id);
        int left = subTree.getLeft();
        int right = subTree.getRight();
        int oldIndex = subTree.getPositionInParent();
        MCRCategoryImpl oldParent = (MCRCategoryImpl) subTree.getParent();
        MCRCategoryImpl newParent = getByNaturalID(session, newParentID);
        subTree.detachFromParent();
        LOGGER.debug("Add subtree to new Parent at index: " + index);
        newParent.getChildren().add(index, subTree);
        subTree.parent = newParent;
        // update needed for old and newParent;
        // Update Left, Right values of other categories
        boolean movedToRight = isCategoryMovedRight(oldParent, newParent, index, oldIndex);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("OldParent left: " + oldParent.getLeft() + " Right: " + oldParent.getRight() + " Level: " + oldParent.getLevel());
            LOGGER.debug("NewParent left: " + newParent.getLeft() + " Right: " + newParent.getRight() + " Level: " + newParent.getLevel());
            LOGGER.debug("SubTree   left: " + subTree.getLeft() + " Right: " + subTree.getRight() + " Level: " + subTree.getLevel());
            if (movedToRight) {
                LOGGER.debug("Category '" + id + "' is moved right.");
            } else {
                LOGGER.debug("Category '" + id + "' is moved left.");
            }
        }
        if (movedToRight)
            updateMoveRight(connection, oldParent, newParent, left, right, index, oldIndex);
        else
            updateMoveLeft(connection, oldParent, newParent, left, right, index, oldIndex);
        // use newParent.left+1 if no left sibling else leftSibling.right+1
        int leftStart = (index == 0) ? (getLeftRightValues(newParent.getId())[0] + 1)
                : (getLeftRightValues(newParent.getChildren().get(index - 1).getId())[1] + 1);
        // update Left, Right and Level values
        calculateLeftRightAndLevel(subTree, leftStart, newParent.getLevel() + 1);
        // only update oldParent if newParent is not its ancestor
        boolean updateOldParent = (oldParent.getLeft() < newParent.getLeft() || oldParent.getRight() > newParent.getRight()) ? true : false;
        // only update newParent if newParent!=oldParent and
        // oldParent is not its ancestor
        boolean updateNewParent = (!oldParent.getId().equals(newParent.getId()) && (newParent.getLeft() < oldParent.getLeft() || newParent.getRight() > oldParent
                .getRight())) ? true : false;
        if (updateOldParent) {
            LOGGER.debug("Updating old parent " + oldParent.getId());
            session.update(oldParent);
        }
        if (updateNewParent) {
            LOGGER.debug("Updating new parent " + newParent.getId());
            session.update(newParent);
        }
        updateTimeStamp();
    }

    public void removeLabel(MCRCategoryID id, String lang) {
        Session session = MCRHIBConnection.instance().getSession();
        MCRCategoryImpl category = getByNaturalID(session, id);
        MCRLabel oldLabel = category.getLabel(lang);
        if (oldLabel != null) {
            category.getLabels().remove(oldLabel);
            updateTimeStamp();
        }
    }

    public void replaceCategory(MCRCategory newCategory) throws IllegalArgumentException {
        if (!exist(newCategory.getId())) {
            throw new IllegalArgumentException("MCRCategory can not be replaced. MCRCategoryID '" + newCategory.getId() + "' is unknown.");
        }
        final MCRHIBConnection connection = MCRHIBConnection.instance();
        Session session = connection.getSession();
        MCRCategoryImpl oldCategory = getByNaturalID(session, newCategory.getId());
        // old Map with all Categories referenced by ID
        Map<MCRCategoryID, MCRCategoryImpl> oldMap = new HashMap<MCRCategoryID, MCRCategoryImpl>();
        fillIDMap(oldMap, oldCategory);
        MCRCategoryImpl newCategoryImpl = MCRCategoryImpl.wrapCategory(copyDeep(newCategory, -1), oldCategory.getParent(), oldCategory.getRoot());
        // new Map with all Categories referenced by ID
        Map<MCRCategoryID, MCRCategoryImpl> newMap = new HashMap<MCRCategoryID, MCRCategoryImpl>();
        fillIDMap(newMap, newCategoryImpl);
        /*
         * copy elements from the new tree to the old tree. fixes bug #1848710
         * (Classification save fails after shifting categories)
         * 
         * set internalID for the new rootCategory
         */
        session.evict(oldCategory);
        for (MCRCategoryImpl category : newMap.values()) {
            MCRCategoryImpl oldValue = oldMap.get(category.getId());
            if (oldValue != null) {
                copyCategoryToNewTree(newCategoryImpl, category, oldValue);
            }
        }
        // calculate left, right and level values
        int diffNodes = newMap.size() - oldMap.size();
        LOGGER.debug("Update changes classification node size by: " + diffNodes);
        int increment = diffNodes * 2;
        if (increment != 0 && oldCategory.isCategory()) {
            final int left = getRightSiblingOrOfAncestor((MCRCategoryImpl) oldCategory.getParent(), oldCategory.getPositionInParent() + 1).getLeft();
            final int maxLeft = ((MCRCategoryImpl) oldCategory.getRoot()).getRight();
            final int right = getRightSiblingOrParent((MCRCategoryImpl) oldCategory.getParent(), oldCategory.getPositionInParent() + 1).getRight();
            final int maxRight = maxLeft;
            updateLeftRightValueMax(connection, newCategory.getId().getRootID(), left, maxLeft, right, maxRight, increment);
        }
        calculateLeftRightAndLevel(newCategoryImpl, oldCategory.getLevel(), oldCategory.getLeft());
        if (oldCategory.isCategory()) {
            MCRCategoryImpl parent = (MCRCategoryImpl) oldCategory.getParent();
            parent.getChildren().set(oldCategory.getPositionInParent(), newCategoryImpl);
        }
        // delete removed categories
        for (MCRCategoryImpl category : oldMap.values()) {
            if (!newMap.containsKey(category.getId())) {
                LOGGER.info("Deleting category :" + category.getId());
                category.detachFromParent();
                session.delete(category);
            }
        }
        session.saveOrUpdate(newCategoryImpl);
        updateTimeStamp();
    }

    public void setLabel(MCRCategoryID id, MCRLabel label) {
        Session session = MCRHIBConnection.instance().getSession();
        MCRCategoryImpl category = getByNaturalID(session, id);
        MCRLabel oldLabel = category.getLabel(label.getLang());
        if (oldLabel != null) {
            category.getLabels().remove(oldLabel);
        }
        category.getLabels().add(label);
        session.update(category);
        updateTimeStamp();
    }

    public long getLastModified() {
        return LAST_MODIFIED;
    }

    private static void updateTimeStamp() {
        LAST_MODIFIED = System.currentTimeMillis();
    }

    /**
     * calculates left and right value throug the subtree rooted at
     * <code>co</code>.
     * 
     * @param co
     *            root node of subtree
     * @param leftStart
     *            co.left will be set to this value
     * @param levelStart
     *            co.getLevel() will return this value
     * @return co.right
     */
    static int calculateLeftRightAndLevel(MCRCategoryImpl co, int leftStart, int levelStart) {
        int curValue = leftStart;
        final int nextLevel = levelStart + 1;
        co.setLeft(leftStart);
        co.setLevel(levelStart);
        for (MCRCategory child : co.getChildren()) {
            LOGGER.debug(child.getId());
            curValue = calculateLeftRightAndLevel((MCRCategoryImpl) child, ++curValue, nextLevel);
        }
        co.setRight(++curValue);
        return curValue;
    }

    static MCRCategoryImpl getLeftSiblingOrOfAncestor(MCRCategoryImpl newParent, int index) {
        if (index > 0) {
            // has left sibling
            return (MCRCategoryImpl) newParent.getChildren().get(index - 1);
        }
        if (newParent.getParent() != null) {
            // recursive call to get left sibling of parent
            int positionInParent = newParent.getPositionInParent();
            return getLeftSiblingOrOfAncestor((MCRCategoryImpl) newParent.getParent(), positionInParent);
        }
        return newParent;// is root
    }

    static MCRCategoryImpl getLeftSiblingOrParent(MCRCategoryImpl newParent, int index) {
        if (index == 0) {
            return newParent;
        }
        return (MCRCategoryImpl) newParent.getChildren().get(index - 1);
    }

    static MCRCategoryImpl getRightSiblingOrOfAncestor(MCRCategoryImpl newParent, int index) {
        if (index + 1 < newParent.getChildren().size()) {
            // has right sibling
            return (MCRCategoryImpl) newParent.getChildren().get(index + 1);
        }
        if (newParent.getParent() != null) {
            // recursive call to get right sibling of parent
            int positionInParent = newParent.getPositionInParent();
            return getRightSiblingOrOfAncestor((MCRCategoryImpl) newParent.getParent(), positionInParent);
        }
        return newParent;// is root
    }

    static MCRCategoryImpl getRightSiblingOrParent(MCRCategoryImpl newParent, int index) {
        if (index + 1 == newParent.getChildren().size()) {
            return newParent;
        }
        // get Element at index that would be at index+1 after insert
        return (MCRCategoryImpl) newParent.getChildren().get(index + 1);
    }

    /**
     * return true if a node moved in Category tree is moved to the right.
     * 
     * If the <code>newParent</code> is not an ancestor node of
     * <code>oldParent</code>, a simple comparison of their <code>left</code>
     * values is done to determine if a node is moved to the right.
     * 
     * If the <code>newParent</code> is an ancestor node of
     * <code>oldParent</code>, the ancestor axis is walked up to the direct
     * child of <code>newParent</code>. The position of the direct child in
     * the childrenList of <code>newParent</code> is compared to
     * <code>newIndex</code> to determine if a node is moved to the right. If
     * <code>oldParent</code> is an ancestor <code>newParent</code> the
     * solution is analog.
     * 
     * @param oldParent
     * @param newParent
     * @param newIndex
     * @param oldIndex
     * @return
     */
    static boolean isCategoryMovedRight(MCRCategoryImpl oldParent, MCRCategoryImpl newParent, int newIndex, int oldIndex) {
        int newParentLevel = newParent.getLevel();
        int oldParentLevel = oldParent.getLevel();
        if (newParent == oldParent) {
            LOGGER.debug("oldParent is newParent");
            return (newIndex > oldIndex);
        }
        if (newParentLevel < oldParentLevel && !((oldParent.getLeft() < newParent.getLeft() || oldParent.getRight() > newParent.getRight()))) {
            // newParent is ancestor of oldParent
            MCRCategory node = oldParent;
            while (!node.getParent().getId().equals(newParent.getId())) {
                // walk ancestor axis up while node not direct child of
                // newParent
                node = node.getParent();
            }
            if (((MCRCategoryImpl) node).getPositionInParent() < newIndex) {
                // old ancestor axis is left from new index
                return true;
            }
            // old ancestor axis is right from new index
            return false;
        } else if (newParentLevel > oldParentLevel && !((newParent.getLeft() < oldParent.getLeft() || newParent.getRight() > oldParent.getRight()))) {
            MCRCategory node = newParent;
            while (!node.getParent().getId().equals(oldParent.getId())) {
                // walk ancestor axis up while node not direct child of
                // newParent
                node = node.getParent();
            }
            if (((MCRCategoryImpl) node).getPositionInParent() > oldIndex) {
                // new ancestor axis is left from old index
                return true;
            }
            return false;
        }
        /*
         * oldParent and newParent are on the same level in the tree OR neither
         * newParent is ancestor of oldParent nor oldParent is ancestor of
         * newParent.
         */
        LOGGER.debug("oldParent and newParent are on the same level in the tree");
        return (newParent.getLeft() > oldParent.getLeft());
    }

    private static Criterion getCategoryCriterion(MCRCategoryID id) {
        return Restrictions.naturalId().set("rootID", id.getRootID()).set("categID", id.getID());
    }

    private static final MCRCategoryImpl buildCategoryFromPrefetchedList(List<MCRCategoryImpl> list) {
        MCRCategoryImpl baseCat = null;
        MCRCategoryImpl currentParent = null;
        for (MCRCategoryImpl currentCat : list) {
            //make a save copy
            currentCat = copyDeep(currentCat, 0);
            if (baseCat == null) {
                baseCat = currentCat;
                currentParent = currentCat;
            } else {
                //check if currentCat is child of currentParent
                if (currentParent.getLevel() + 1 < currentCat.getLevel()) {
                    currentParent = (MCRCategoryImpl) currentParent.getChildren().get(currentParent.getChildren().size() - 1);
                } else
                    while (currentParent.getLevel() >= currentCat.getLevel()) {
                        //we have to go some levels up
                        currentParent = (MCRCategoryImpl) currentParent.getParent();
                    } //got parent of currentCat as currentParent
                currentParent.getChildren().add(currentCat);
            }
        }
        return baseCat;
    }

    private static MCRCategoryImpl copyDeep(MCRCategory category, int level) {
        if (category == null) {
            return null;
        }
        MCRCategoryImpl newCateg = new MCRCategoryImpl();
        int childAmount;
        try {
            childAmount = (level != 0) ? category.getChildren().size() : 0;
        } catch (RuntimeException e) {
            LOGGER.error("Cannot get children size for category: " + category.getId(), e);
            throw e;
        }
        newCateg.setChildren(new ArrayList<MCRCategory>(childAmount));
        newCateg.setId(category.getId());
        newCateg.setLabels(new HashSet<MCRLabel>(category.getLabels()));
        newCateg.setRoot(category.getRoot());
        newCateg.setURI(category.getURI());
        newCateg.setLevel(category.getLevel());
        if (category instanceof MCRCategoryImpl) {
            //to allow optimized hasChildren() to work without db query
            newCateg.setLeft(((MCRCategoryImpl) category).getLeft());
            newCateg.setRight(((MCRCategoryImpl) category).getRight());
        }
        if (childAmount > 0) {
            for (MCRCategory child : category.getChildren()) {
                newCateg.getChildren().add(copyDeep((MCRCategoryImpl) child, level - 1));
            }
        }
        return newCateg;
    }

    static MCRCategoryImpl getByNaturalID(Session session, MCRCategoryID id) {
        final Criteria criteria = session.createCriteria(CATEGRORY_CLASS);
        return (MCRCategoryImpl) criteria.setCacheable(true).add(getCategoryCriterion(id)).uniqueResult();
    }

    /**
     * copies <code>previousVersion</code> into the place of
     * <code>category</code> in <code>newTree</code>. All changes of
     * <code>category</code> are applied to <code>previousVersion</code>.
     * 
     * @param newTree
     *            new root of MCRCategory tree
     * @param category
     *            new version containing all changes
     * @param previousVersion
     *            oldVersion allready stored in database
     */
    private static void copyCategoryToNewTree(MCRCategoryImpl newTree, MCRCategoryImpl category, MCRCategoryImpl previousVersion) {
        category.setInternalID(previousVersion.getInternalID());
        MCRCategoryImpl parent = (MCRCategoryImpl) category.getParent();
        if (parent != null) {
            // get new parent of modified category tree here (by parent
            // category id)
            parent = (MCRCategoryImpl) findCategory(newTree, category.getParent().getId());
            int pos = category.getPositionInParent();
            parent.getChildren().remove(pos);
            previousVersion.detachFromParent();
            parent.getChildren().add(pos, previousVersion);
            // set parent here
            previousVersion.parent = parent;
            previousVersion.getChildren().clear();
            previousVersion.getChildren().addAll(
                    MCRCategoryImpl.wrapCategories(detachCategories(category.getChildren()), previousVersion, previousVersion.getRoot()));
            for (MCRLabel newLabel : category.getLabels()) {
                MCRLabel oldLabel = previousVersion.getLabel(newLabel.getLang());
                if (oldLabel == null) {
                    // copy new label
                    previousVersion.getLabels().add(newLabel);
                } else {
                    if (!oldLabel.getText().equals(newLabel.getText())) {
                        oldLabel.setText(newLabel.getText());
                    }
                    if (!oldLabel.getDescription().equals(newLabel.getDescription())) {
                        oldLabel.setDescription(newLabel.getDescription());
                    }
                }
            }
            Iterator<MCRLabel> labels = previousVersion.getLabels().iterator();
            while (labels.hasNext()) {
                // remove labels that are not present in new version
                if (category.getLabel(labels.next().getLang()) == null)
                    labels.remove();
            }
        } else {
            category.setInternalID(previousVersion.getInternalID());
        }
    }

    /**
     * finds a MCRCategory with <code>id</code> somewhere below
     * <code>root</code>.
     */
    private static MCRCategory findCategory(MCRCategory root, MCRCategoryID id) {
        if (root.getId().equals(id)) {
            return root;
        }
        for (MCRCategory child : root.getChildren()) {
            MCRCategory rv = findCategory(child, id);
            if (rv != null) {
                return rv;
            }
        }
        return null;
    }

    /**
     * detaches all elements in list from their parent.
     */
    private static List<MCRCategory> detachCategories(List<MCRCategory> list) {
        ArrayList<MCRCategory> categories = new ArrayList<MCRCategory>(list.size());
        categories.addAll(list);
        for (MCRCategory category : categories) {
            if (!(category instanceof MCRCategoryImpl)) {
                throw new IllegalArgumentException("This method just works for MCRCategoryImpl");
            }
            ((MCRAbstractCategoryImpl) category).detachFromParent();
        }
        return categories;
    }

    private static void fillIDMap(Map<MCRCategoryID, MCRCategoryImpl> map, MCRCategoryImpl category) {
        LOGGER.info("fillIDMap: categID: " + category.getId() + " category: " + category);
        map.put(category.getId(), category);
        for (MCRCategory subCategory : category.getChildren()) {
            LOGGER.info("fillIDMap: subCategory: " + subCategory);
            fillIDMap(map, (MCRCategoryImpl) subCategory);
        }
    }

    private static Integer[] getLeftRightValues(MCRCategoryID id) {
        Session session = MCRHIBConnection.instance().getSession();
        Criteria c = session.createCriteria(CATEGRORY_CLASS).setProjection(
                Projections.projectionList().add(Projections.property("left")).add(Projections.property("right")));
        c.add(getCategoryCriterion(id));
        Object[] result = (Object[]) c.uniqueResult();
        Integer[] iResult = new Integer[] { (Integer) result[0], (Integer) result[1] };
        return iResult;
    }

    private static int getNumberOfChildren(MCRCategoryID id) {
        Session session = MCRHIBConnection.instance().getSession();
        Criteria c = session.createCriteria(CATEGRORY_CLASS).setProjection(Projections.rowCount());
        c.add(Subqueries.propertyEq("parent", DetachedCriteria.forClass(CATEGRORY_CLASS).setProjection(Projections.property("internalID")).add(
                getCategoryCriterion(id))));
        return ((Number) c.uniqueResult()).intValue();
    }

    /**
     * @param session
     * @param left
     * @param increment
     */
    private static void updateLeftRightValue(MCRHIBConnection connection, String classID, int left, final int increment) {
        Session session = connection.getSession();
        LOGGER.debug("LEFT AND RIGHT values need updates. Left=" + left + ", increment by: " + increment);
        Query leftQuery = session.getNamedQuery(CATEGRORY_CLASS.getName() + ".updateLeft");
        leftQuery.setInteger("left", left);
        leftQuery.setInteger("increment", increment);
        leftQuery.setString("classID", classID);
        int leftChanges = leftQuery.executeUpdate();
        Query rightQuery = session.getNamedQuery(CATEGRORY_CLASS.getName() + ".updateRight");
        rightQuery.setInteger("left", left);
        rightQuery.setInteger("increment", increment);
        rightQuery.setString("classID", classID);
        int rightChanges = rightQuery.executeUpdate();
        LOGGER.debug("Updated " + leftChanges + " left and " + rightChanges + " right values.");
    }

    private static void updateLeftRightValueMax(MCRHIBConnection connection, String classID, int left, int maxLeft, int right, int maxRight, final int increment) {
        Session session = connection.getSession();
        LOGGER.debug("LEFT AND RIGHT values need updates. Left=" + left + ", MaxLeft=" + maxLeft + ", Right=" + right + ", MaxRight=" + maxRight
                + " increment by: " + increment);
        Query leftQuery = session.getNamedQuery(CATEGRORY_CLASS.getName() + ".updateLeftWithMax");
        leftQuery.setInteger("left", left);
        leftQuery.setInteger("max", maxLeft);
        leftQuery.setInteger("increment", increment);
        leftQuery.setString("classID", classID);
        int leftChanges = leftQuery.executeUpdate();
        Query rightQuery = session.getNamedQuery(CATEGRORY_CLASS.getName() + ".updateRightWithMax");
        rightQuery.setInteger("right", right);
        rightQuery.setInteger("max", maxRight);
        rightQuery.setInteger("increment", increment);
        rightQuery.setString("classID", classID);
        int rightChanges = rightQuery.executeUpdate();
        LOGGER.debug("Updated " + leftChanges + " left and " + rightChanges + " right values.");
    }

    private static void updateMoveLeft(MCRHIBConnection connection, MCRCategoryImpl oldParent, MCRCategoryImpl newParent, int left, int right, int index,
            int oldIndex) {
        int nodes = 1 + (right - left) / 2;
        int increment = 2 * nodes;
        if (newParent == oldParent) {
            MCRCategoryImpl leftSiblingOrParent = getLeftSiblingOrParent(newParent, index);
            int newLeft = leftSiblingOrParent.getLeft();
            int newRight = (leftSiblingOrParent == newParent) ? newLeft + 1 : leftSiblingOrParent.getRight();
            updateLeftRightValueMax(connection, newParent.getRootID(), newLeft, left, newRight, right, increment);
            connection.getSession().refresh(newParent);
        } else {
            MCRCategoryImpl rightSiblingOrOfAncestor = getRightSiblingOrOfAncestor(newParent, index);
            int maxLeft = rightSiblingOrOfAncestor.getLeft();
            MCRCategoryImpl rightSiblingOrParent = getRightSiblingOrParent(newParent, index);
            int maxRight = rightSiblingOrParent.getRight();
            int minRight = right;
            if (maxRight <= right) {
                // Update at least newParent
                minRight = maxRight - 1;
            }
            updateLeftRightValueMax(connection, newParent.getRootID(), left, maxLeft, minRight, maxRight, increment);
            connection.getSession().refresh(rightSiblingOrOfAncestor);
        }
    }

    private static void updateMoveRight(MCRHIBConnection connection, MCRCategoryImpl oldParent, MCRCategoryImpl newParent, int left, int right, int index,
            int oldIndex) {
        int nodes = 1 + (right - left) / 2;
        int increment = -2 * nodes;
        if (newParent == oldParent) {
            MCRCategoryImpl leftSibling = getLeftSiblingOrParent(newParent, index);
            int maxLeft = leftSibling.getLeft();
            int maxRight = leftSibling.getRight();
            updateLeftRightValueMax(connection, newParent.getRootID(), right, maxLeft, left, maxRight, increment);
            connection.getSession().refresh(newParent);
        } else {
            int maxLeft = getLeftSiblingOrParent(newParent, index).getLeft();
            int minLeft = left;
            if (maxLeft <= left) {
                // update at least newParent
                minLeft = maxLeft - 1;
            }
            MCRCategoryImpl leftSiblingOrOfAncestor = getLeftSiblingOrOfAncestor(newParent, index);
            int maxRight = leftSiblingOrOfAncestor.getRight();
            updateLeftRightValueMax(connection, newParent.getRootID(), minLeft, maxLeft, right, maxRight, increment);
            connection.getSession().refresh(leftSiblingOrOfAncestor);
        }
    }
}
