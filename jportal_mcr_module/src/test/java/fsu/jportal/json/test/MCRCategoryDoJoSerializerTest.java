package fsu.jportal.json.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.impl.MCRCategoryImpl;

import com.google.gson.Gson;

import fsu.jportal.gson.GsonManager;


public class MCRCategoryDoJoSerializerTest extends GsonSerializationTest{
    
    
    @Test
    public void serialize() throws Exception {
        MCRCategoryImpl mcrCategoryImpl = createCateg("rootID", "", "RootCateg");
        List<MCRCategory> children = new ArrayList<MCRCategory>();
        MCRCategoryImpl categ1 = createCateg("rootID", "categ1", "categ1");
        List<MCRCategory> subcateg = new ArrayList<MCRCategory>();
        subcateg.add(createCateg("rootID", "subCateg1", "subCateg1"));
        categ1.setChildren(subcateg);
        children.add(categ1);
        children.add(createCateg("rootID", "categ2", "categ2"));
        mcrCategoryImpl.setChildren(children);
        
        Gson gson = GsonManager.instance().createGson();
        
        String json = gson.toJson(mcrCategoryImpl);
        System.out.println(json);
        MCRCategoryImpl fromJson = gson.fromJson(json, MCRCategoryImpl.class);
        System.out.println("CategID: " + fromJson.getId());
        System.out.println("Labels: " + fromJson.getLabel("de").getText());
    }
}
