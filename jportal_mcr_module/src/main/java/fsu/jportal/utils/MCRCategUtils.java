package fsu.jportal.utils;

import java.util.Set;

import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.classifications2.MCRLabel;
import org.mycore.datamodel.classifications2.impl.MCRCategoryImpl;

public class MCRCategUtils{
    public static MCRCategory newCategory(MCRCategoryID id, Set<MCRLabel> labels, MCRCategory parent) {
        MCRCategoryImpl category = new MCRCategoryImpl();
        category.setId(id);
        category.setLabels(labels);
        category.setParent(parent);
    
        return category;
    }

    public static String maskCategID(MCRCategoryID categoryID) {
        String rootID = categoryID.getRootID();
        String id = categoryID.getID();
        
        return rootID + "." + (id == null? "" : id);
    }
}