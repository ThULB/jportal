package fsu.jportal.json.test;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.impl.MCRCategoryImpl;

import com.google.gson.Gson;

import fsu.jportal.gson.GsonManager;
import fsu.jportal.wrapper.MCRCategoryListWrapper;


public class MCRCategoryListDoJoSerializer extends GsonSerializationTest{
    @Test
    public void serializeList() throws Exception {
        List<MCRCategory> categList = new ArrayList<MCRCategory>();
        String rootID = "rootID";
        MCRCategoryImpl categ1 = createCateg(rootID, "categ1", "categ1");
        List<MCRCategory> subCategs = new ArrayList<MCRCategory>();
        subCategs.add(createCateg(rootID, "subcateg1", "subcateg1"));
        categ1.setChildren(subCategs);
        categList.add(categ1);
        categList.add(createCateg(rootID, "categ2", "categ2"));
        
        Gson gson = GsonManager.instance().createGson();
        System.out.println(gson.toJson(new MCRCategoryListWrapper(categList)));
    }

}
