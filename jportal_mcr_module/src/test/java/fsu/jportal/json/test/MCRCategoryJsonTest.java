package fsu.jportal.json.test;

import java.util.Properties;

import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.junit.Test;
import org.mycore.common.MCRConfiguration;
import org.mycore.datamodel.classifications2.impl.MCRCategoryImpl;

import com.google.gson.Gson;

import fsu.jportal.gson.GsonManager;
import fsu.jportal.mocks.CategoryDAOMock;


public class MCRCategoryJsonTest {
    @Test
    public void deserialize() throws Exception {
        System.setProperty("MCR.Configuration.File", "config/test.properties");
        Properties mcrProperties = MCRConfiguration.instance().getProperties();
        mcrProperties.setProperty("MCR.Metadata.DefaultLang", "de");
        mcrProperties.setProperty("MCR.Category.DAO", CategoryDAOMock.class.getName());
        
        SAXBuilder saxBuilder = new SAXBuilder();
        Document doc = saxBuilder.build(getClass().getResourceAsStream("/classi/categoryJsonErr.xml"));
        String json = doc.getRootElement().getText();
        
        Gson gson = GsonManager.instance().createGson();
        try {
            MCRCategoryImpl fromJson = gson.fromJson(json, MCRCategoryImpl.class);
            System.out.println("FOO");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
