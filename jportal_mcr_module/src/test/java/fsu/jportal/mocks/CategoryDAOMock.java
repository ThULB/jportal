package fsu.jportal.mocks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryDAO;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.classifications2.MCRLabel;

import fsu.jportal.gson.Category;

public class CategoryDAOMock implements MCRCategoryDAO {
    HashMap<MCRCategoryID, MCRCategory> categMap = null;
    HashMap<MCRCategoryID, MCRCategory> rootCategMap = null;

    private void buildTestCategs() {
        Category root_01 = createCategory("rootID_01", "", null);
        Category root_02 = createCategory("rootID_02", "", null);
        Category categ_01 = createCategory("rootID_01", "categ_01", null);
        Category categ_02 = createCategory("rootID_01", "categ_02", null);
        
        List<MCRCategory> children = new ArrayList<MCRCategory>();
        children.add(categ_01);
        children.add(categ_02);
        root_01.setChildren(children);
        
        rootCategMap.put(root_01.getId(), root_01);
        rootCategMap.put(root_02.getId(), root_02);
        categMap.put(root_01.getId(), root_01);
        categMap.put(root_02.getId(), root_02);
        categMap.put(categ_01.getId(), categ_01);
        categMap.put(categ_02.getId(), categ_02);
    }
    
    public void init(){
        categMap = new HashMap<MCRCategoryID, MCRCategory>();
        rootCategMap = new HashMap<MCRCategoryID, MCRCategory>();
        buildTestCategs();
    }
    
    public Set<MCRCategoryID> getIds(){
        return categMap.keySet();
    }
    
    public Collection<MCRCategory> getCategs(){
        return categMap.values();
    }

    private Category createCategory(String rootID, String categID, MCRCategoryID parentID) {
        MCRCategoryID id = new MCRCategoryID(rootID, categID);
        Set<MCRLabel> labels = new HashSet<MCRLabel>();
        labels.add(new MCRLabel("de", id + "_text", id + "_descr"));
        labels.add(new MCRLabel("en", id + "_text", id + "_descr"));
        Category newCategory = new Category();
        newCategory.setId(id);
        newCategory.setLabels(labels);
        newCategory.setParentID(parentID);
        return newCategory;
    }

    @Override
    public void addCategory(MCRCategoryID parentID, MCRCategory category) {
        categMap.put(category.getId(), category);
    }

    @Override
    public void deleteCategory(MCRCategoryID id) {
        MCRCategory mcrCategory = categMap.get(id);
        for (MCRCategory child : mcrCategory.getChildren()) {
            categMap.remove(child.getId());
        }

        categMap.remove(id);
    }

    @Override
    public boolean exist(MCRCategoryID id) {
        return categMap.containsKey(id);
    }

    @Override
    public List<MCRCategory> getCategoriesByLabel(MCRCategoryID baseID, String lang, String text) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MCRCategory getCategory(MCRCategoryID id, int childLevel) {
        return categMap.get(id);
    }

    @Override
    public List<MCRCategory> getChildren(MCRCategoryID id) {
        return new ArrayList<MCRCategory>();
    }

    @Override
    public List<MCRCategory> getParents(MCRCategoryID id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<MCRCategoryID> getRootCategoryIDs() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<MCRCategory> getRootCategories() {
        return new ArrayList<MCRCategory>(rootCategMap.values());
    }

    @Override
    public MCRCategory getRootCategory(MCRCategoryID baseID, int childLevel) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean hasChildren(MCRCategoryID id) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void moveCategory(MCRCategoryID id, MCRCategoryID newParentID) {
        // TODO Auto-generated method stub

    }

    @Override
    public void moveCategory(MCRCategoryID id, MCRCategoryID newParentID, int index) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeLabel(MCRCategoryID id, String lang) {
        // TODO Auto-generated method stub

    }

    @Override
    public void replaceCategory(MCRCategory newCategory) throws IllegalArgumentException {
        if (!categMap.containsKey(newCategory.getId())) {
            throw new IllegalArgumentException();
        }

        categMap.put(newCategory.getId(), newCategory);
    }

    @Override
    public void setLabel(MCRCategoryID id, MCRLabel label) {
        // TODO Auto-generated method stub

    }

    @Override
    public long getLastModified() {
        // TODO Auto-generated method stub
        return 0;
    }
}