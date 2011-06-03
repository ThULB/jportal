package fsu.jportal.mocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryDAO;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.classifications2.MCRLabel;

public class FakeCategoryDAO implements MCRCategoryDAO{
    HashMap<MCRCategoryID, MCRCategory> categMap = new HashMap<MCRCategoryID, MCRCategory>();

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
        // TODO Auto-generated method stub
        return false;
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
        return new ArrayList<MCRCategory>(categMap.values());
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
        if(!categMap.containsKey(newCategory.getId())){
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