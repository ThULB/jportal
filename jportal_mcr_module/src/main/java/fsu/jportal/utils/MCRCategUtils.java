package fsu.jportal.utils;

import java.util.Set;

import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.classifications2.MCRLabel;

import fsu.jportal.gson.Category;

public class MCRCategUtils{
    public static MCRCategory newCategory(MCRCategoryID id, Set<MCRLabel> labels, MCRCategoryID mcrCategoryID) {
        Category category = new Category();
        category.setId(id);
        category.setLabels(labels);
        category.setParentID(mcrCategoryID);
    
        return category;
    }

    public static String maskCategID(MCRCategoryID categoryID) {
        String rootID = categoryID.getRootID();
        String id = categoryID.getID();
        
        return rootID + "." + (id == null? "" : id);
    }
}