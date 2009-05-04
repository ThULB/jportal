/*
 * 
 * $Revision: 15078 $ $Date: 2009-04-16 12:52:54 +0200 (Do, 16. Apr 2009) $
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
 */
package org.mycore.datamodel.classifications2.impl;

import java.net.URISyntaxException;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;

import org.hibernate.criterion.Projections;
import org.jdom.Document;

import org.mycore.common.MCRException;
import org.mycore.common.MCRHibTestCase;
import org.mycore.common.xml.MCRXMLHelper;
import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.classifications2.MCRLabel;
import org.mycore.datamodel.classifications2.utils.MCRStringTransformer;
import org.mycore.datamodel.classifications2.utils.MCRXMLTransformer;

public class MCRCategoryDAOImplTest extends MCRHibTestCase {

    static final String WORLD_CLASS_RESOURCE_NAME = "/org/mycore/datamodel/classifications2/impl/resources/worldclass.xml";

    private static final String WORLD_CLASS2_RESOURCE_NAME = "/org/mycore/datamodel/classifications2/impl/resources/worldclass2.xml";

    static final String CATEGORY_MAPPING_RESOURCE_NAME = "/org/mycore/datamodel/classifications2/impl/MCRCategoryImpl.hbm.xml";

    static final MCRCategoryDAOImpl DAO = new MCRCategoryDAOImpl();

    private MCRCategory category, category2;

    @Override
    protected void setUp() throws Exception {
        // TODO Auto-generated method stub
        super.setUp();
        loadWorldClassification();
    }

    public void testAddCategory() throws MCRException {
        addWorldClassification();
        assertTrue("Exist check failed for Category " + category.getId(), DAO.exist(category.getId()));
        MCRCategoryImpl india = new MCRCategoryImpl();
        india.setId(new MCRCategoryID(category.getId().getRootID(), "India"));
        india.setLabels(new HashSet<MCRLabel>());
        india.getLabels().add(new MCRLabel("de", "Indien", null));
        india.getLabels().add(new MCRLabel("en", "India", null));
        DAO.addCategory(new MCRCategoryID(category.getId().getRootID(), "Asia"), india);
        startNewTransaction();
        assertTrue("Exist check failed for Category " + india.getId(), DAO.exist(india.getId()));
        MCRCategoryImpl rootCategory = getRootCategoryFromSession();
        assertEquals("Child category count does not match.", category.getChildren().size(), rootCategory.getChildren().size());
        int allNodes = (Integer) sessionFactory.getCurrentSession().createCriteria(MCRCategoryImpl.class).setProjection(
                Projections.rowCount()).uniqueResult();
        // category + india
        assertEquals("Complete category count does not match.", countNodes(category) + 1, allNodes);
        assertTrue("No root category present", rootCategory.getRoot() != null);
    }

    public void testDeleteCategory() {
        addWorldClassification();
        DAO.deleteCategory(category.getId());
        startNewTransaction();
        // check if classification is present
        assertFalse("Category is not deleted: " + category.getId(), DAO.exist(category.getId()));
        // check if any subcategory is present
        assertFalse("Category is not deleted: " + category.getChildren().get(0).getId(), DAO.exist(category.getChildren().get(0).getId()));
    }

    public void testGetCategoriesByLabel() {
        addWorldClassification();
        MCRCategory find = category.getChildren().get(0).getChildren().get(0);
        MCRCategory dontFind = category.getChildren().get(1);
        MCRLabel label = find.getLabels().iterator().next();
        List<MCRCategory> results = DAO.getCategoriesByLabel(category.getId(), label.getLang(), label.getText());
        assertFalse("No search results found", results.isEmpty());
        assertTrue("Could not find Category: " + find.getId(), results.get(0).getLabels().contains(label));
        assertTrue("No search result expected.", DAO.getCategoriesByLabel(dontFind.getId(), label.getLang(), label.getText()).isEmpty());
    }

    public void testGetCategory() {
        addWorldClassification();
        MCRCategory rootCategory = DAO.getCategory(category.getId(), 0);
        assertTrue("Children present with child Level 0.", rootCategory.getChildren().isEmpty());
        rootCategory = DAO.getCategory(category.getId(), 1);
        MCRCategory origSubCategory = rootCategory.getChildren().get(0);
        assertTrue("Children present with child Level 1.", origSubCategory.getChildren().isEmpty());
        assertEquals("Category count does not match with child Level 1.\n" + MCRStringTransformer.getString(rootCategory), category
                .getChildren().size(), rootCategory.getChildren().size());
        assertEquals("Children of Level 1 do not know that they are at the first level.\n" + MCRStringTransformer.getString(rootCategory),
                1, origSubCategory.getLevel());
        MCRCategory europe = DAO.getCategory(category.getChildren().get(0).getId(), -1);
        assertFalse("No children present in " + europe.getId(), europe.getChildren().isEmpty());
        europe = DAO.getCategory(category.getChildren().get(0).getId(), 1);
        assertFalse("No children present in " + europe.getId(), europe.getChildren().isEmpty());
        rootCategory = DAO.getCategory(category.getId(), -1);
        assertEquals("Did not get all categories." + MCRStringTransformer.getString(rootCategory), countNodes(category),
                countNodes(rootCategory));
        assertEquals("Children of Level 1 do not match", category.getChildren().size(), rootCategory.getChildren().size());
        MCRCategory subCategory = DAO.getCategory(origSubCategory.getId(), 0);
        assertNotNull("Did not return ", subCategory);
        assertEquals("ObjectIDs did not match", origSubCategory.getId(), subCategory.getId());
    }

    public void testGetChildren() {
        addWorldClassification();
        List<MCRCategory> children = DAO.getChildren(category.getId());
        assertEquals("Did not get all children of :" + category.getId(), category.getChildren().size(), children.size());
        for (int i = 0; i < children.size(); i++) {
            assertEquals("Category IDs of children do not match.", category.getChildren().get(i).getId(), children.get(i).getId());
        }
    }

    public void testGetParents() {
        addWorldClassification();
        MCRCategory find = category.getChildren().get(0).getChildren().get(0);
        List<MCRCategory> parents = DAO.getParents(find.getId());
        MCRCategory findParent = find;
        for (MCRCategory parent : parents) {
            findParent = findParent.getParent();
            assertNotNull("Returned too much parents.", findParent);
            assertEquals("Parents did not match.", findParent.getId(), parent.getId());
        }
    }

    public void testGetRootCategoryIDs() {
        addWorldClassification();
        MCRCategoryID find = category.getId();
        List<MCRCategoryID> classIds = DAO.getRootCategoryIDs();
        assertEquals("Result size does not match.", 1, classIds.size());
        assertEquals("Returned MCRCategoryID does not match.", find, classIds.get(0));
    }

    public void testGetRootCategories() {
        addWorldClassification();
        MCRCategoryID find = category.getId();
        List<MCRCategory> classes = DAO.getRootCategories();
        assertEquals("Result size does not match.", 1, classes.size());
        assertEquals("Returned MCRCategoryID does not match.", find, classes.get(0).getId());
    }

    public void testGetRootCategory() {
        addWorldClassification();
        // Europe
        MCRCategory find = category.getChildren().get(0);
        MCRCategory rootCategory = DAO.getRootCategory(find.getId(), 0);
        assertEquals("Category count does not match.", 2, countNodes(rootCategory));
        assertEquals("Did not get root Category.", find.getRoot().getId(), rootCategory.getId());
        rootCategory = DAO.getRootCategory(find.getId(), -1);
        assertEquals("Category count does not match.", 1 + countNodes(find), countNodes(rootCategory));
    }

    public void testChildren() {
        addWorldClassification();
        assertTrue("Category '" + category.getId() + "' should have children.", DAO.hasChildren(category.getId()));
        assertFalse("Category '" + category.getChildren().get(1).getId() + "' shouldn't have children.", DAO.hasChildren(category
                .getChildren().get(1).getId()));
    }

    public void testMoveCategoryWithoutIndex() throws SQLException {
        addWorldClassification();
        checkLeftRightLevelValue(getRootCategoryFromSession(), 0, 0);
        startNewTransaction();
        MCRCategory moveNode = category.getChildren().get(1);
        // Europe conquer Asia
        DAO.moveCategory(moveNode.getId(), category.getChildren().get(0).getId());
        startNewTransaction();
        MCRCategoryImpl rootNode = getRootCategoryFromSession();
        checkLeftRightLevelValue(rootNode, 0, 0);
    }

    public void testMoveCategoryInParent() throws SQLException {
        addWorldClassification();
        MCRCategory moveNode = category.getChildren().get(1);
        DAO.moveCategory(moveNode.getId(), moveNode.getParent().getId(), 0);
        startNewTransaction();
        MCRCategoryImpl rootNode = getRootCategoryFromSession();
        checkLeftRightLevelValue(rootNode, 0, 0);
        MCRCategory movedNode = rootNode.getChildren().get(0);
        assertEquals("Did not expect this category on position 0.", moveNode.getId(), movedNode.getId());
    }

    public void testRemoveLabel() {
        addWorldClassification();
        final MCRCategory labelNode = category.getChildren().get(0);
        int labelCount = labelNode.getLabels().size();
        DAO.removeLabel(labelNode.getId(), "en");
        startNewTransaction();
        final MCRCategory labelNodeNew = getRootCategoryFromSession().getChildren().get(0);
        assertEquals("Label count did not match.", labelCount - 1, labelNodeNew.getLabels().size());
    }

    public void testReplaceCategory() throws URISyntaxException {
        loadWorldClassification2();
        addWorldClassification();
        DAO.replaceCategory(category2);
        startNewTransaction();
        MCRCategoryImpl rootNode = getRootCategoryFromSession();
        assertEquals("Category count does not match.", countNodes(category2), countNodes(rootNode));
        assertEquals("Label count does not match.", category2.getChildren().get(0).getLabels().size(), rootNode.getChildren().get(0)
                .getLabels().size());
        checkLeftRightLevelValue(rootNode, 0, 0);
    }

    public void testSetLabel() {
        addWorldClassification();
        startNewTransaction();
        // test add
        int count = category.getLabels().size();
        final String lang = "ju";
        final String text = "JUnit-Test";
        DAO.setLabel(category.getId(), new MCRLabel(lang, text, "Added by JUnit"));
        startNewTransaction();
        MCRCategory rootNode = getRootCategoryFromSession();
        assertEquals("Label count does not match.", count + 1, rootNode.getLabels().size());
        // test modify
        String description = "Modified by JUnit";
        DAO.setLabel(category.getId(), new MCRLabel(lang, text, description));
        startNewTransaction();
        rootNode = getRootCategoryFromSession();
        assertEquals("Label count does not match.", count + 1, rootNode.getLabels().size());
        assertEquals("Label does not match.", description, rootNode.getLabel(lang).getDescription());
    }

    /**
     * tests relink child to grantparent and removal of parent.
     * 
     * @throws URISyntaxException
     */
    public void testReplaceCategoryShiftCase() {
        addWorldClassification();
        MCRCategory europe = category.getChildren().get(0);
        MCRCategory germany = europe.getChildren().get(0);
        europe.getChildren().remove(0);
        category.getChildren().remove(0);
        category.getChildren().add(germany);
        DAO.replaceCategory(category);
        startNewTransaction();
        MCRCategory rootNode = getRootCategoryFromSession();
        assertEquals("Category count does not match.", countNodes(category), countNodes(rootNode));
        assertEquals("Label count does not match.", category.getChildren().get(0).getLabels().size(), rootNode.getChildren().get(0)
                .getLabels().size());
    }

    private MCRCategoryImpl getRootCategoryFromSession() {
        return (MCRCategoryImpl) sessionFactory.getCurrentSession()
                .get(MCRCategoryImpl.class, ((MCRCategoryImpl) category).getInternalID());
    }

    private void addWorldClassification() {
        DAO.addCategory(null, category);
        startNewTransaction();
    }

    /**
     * @throws URISyntaxException
     */
    private void loadWorldClassification() throws URISyntaxException {
        URL worlClassUrl = this.getClass().getResource(WORLD_CLASS_RESOURCE_NAME);
        Document xml = MCRXMLHelper.parseURI(worlClassUrl.toURI().toString());
        category = MCRXMLTransformer.getCategory(xml);
    }

    private void loadWorldClassification2() throws URISyntaxException {
        URL worlClassUrl = this.getClass().getResource(WORLD_CLASS2_RESOURCE_NAME);
        Document xml = MCRXMLHelper.parseURI(worlClassUrl.toURI().toString());
        category2 = MCRXMLTransformer.getCategory(xml);
    }

    private static int countNodes(MCRCategory category) {
        int i = 1;
        for (MCRCategory child : category.getChildren()) {
            i += countNodes(child);
        }
        return i;
    }

    private int checkLeftRightLevelValue(MCRCategoryImpl node, int leftStart, int levelStart) {
        int curValue = leftStart;
        final int nextLevel = levelStart + 1;
        assertEquals("Left value did not match on ID: " + node.getId(), leftStart, node.getLeft());
        assertEquals("Level value did not match on ID: " + node.getId(), levelStart, node.getLevel());
        for (MCRCategory child : node.children) {
            curValue = checkLeftRightLevelValue((MCRCategoryImpl) child, ++curValue, nextLevel);
        }
        assertEquals("Right value did not match on ID: " + node.getId(), ++curValue, node.getRight());
        return curValue;
    }

}
