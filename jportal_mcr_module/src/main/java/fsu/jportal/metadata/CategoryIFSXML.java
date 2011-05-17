package fsu.jportal.metadata;

import java.net.URI;
import java.util.List;
import java.util.Set;

import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.classifications2.MCRLabel;

public class CategoryIFSXML implements MCRCategory {

    @Override
    public boolean isClassification() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isCategory() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean hasChildren() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public List<MCRCategory> getChildren() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MCRCategoryID getId() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setId(MCRCategoryID id) {
        // TODO Auto-generated method stub

    }

    @Override
    public Set<MCRLabel> getLabels() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MCRLabel getCurrentLabel() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MCRLabel getLabel(String lang) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getLevel() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public MCRCategory getRoot() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MCRCategory getParent() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public URI getURI() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setURI(URI uri) {
        // TODO Auto-generated method stub

    }

}
