package fsu.jportal.mocks;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mycore.datamodel.classifications2.MCRCategLinkService;
import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryDAOFactory;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.classifications2.MCRObjectReference;

public class CategoryLinkServiceMock implements MCRCategLinkService{

    @Override
    public Map<MCRCategoryID, Boolean> hasLinks(MCRCategory category) {
        List<MCRCategory> categories;
        if(category == null) {
            categories = MCRCategoryDAOFactory.getInstance().getRootCategories();
        }else{
            categories = category.getChildren();
        }
        
        Map<MCRCategoryID, Boolean> linkMap = new HashMap<MCRCategoryID, Boolean>();
        int i = 0;
        for (MCRCategory mcrCategory : categories) {
            boolean haslink = false;
            if(i++%2 == 0) {
                haslink = true;
            }
            
            linkMap.put(mcrCategory.getId(), haslink);
        }
        return linkMap;
    }

    @Override
    public boolean hasLink(MCRCategory classif) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Map<MCRCategoryID, Number> countLinks(MCRCategory category, boolean childrenOnly) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<MCRCategoryID, Number> countLinksForType(MCRCategory category, String type, boolean childrenOnly) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void deleteLink(String id) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void deleteLinks(Collection<String> ids) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Collection<String> getLinksFromCategory(MCRCategoryID id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isInCategory(String objectId, MCRCategoryID id) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Collection<String> getLinksFromCategoryForType(MCRCategoryID id, String type) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<MCRCategoryID> getLinksFromObject(String id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setLinks(MCRObjectReference objectReference, Collection<MCRCategoryID> categories) {
        // TODO Auto-generated method stub
        
    }

}
