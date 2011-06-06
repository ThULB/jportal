package fsu.jportal.wrapper;

import java.util.List;

import org.mycore.datamodel.classifications2.MCRCategory;

public class MCRCategoryListWrapper {

    private List<MCRCategory> categList;

    public MCRCategoryListWrapper(List<MCRCategory> categList) {
        this.categList = categList;
    }

    public List<MCRCategory> getList() {
        return categList;
    }

}
