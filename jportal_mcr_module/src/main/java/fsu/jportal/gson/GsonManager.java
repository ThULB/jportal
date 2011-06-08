package fsu.jportal.gson;

import org.mycore.datamodel.classifications2.impl.MCRCategoryImpl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import fsu.jportal.metadata.Rubric;
import fsu.jportal.wrapper.MCRCategoryListWrapper;
import fsu.jportal.wrapper.MCRLabelSetWrapper;

public class GsonManager {
    private GsonBuilder gsonBuilder;
    private static GsonManager instance;
    
    private GsonManager() {
        gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(MCRCategoryImpl.class, new MCRCategoryJson.Serializer());
        gsonBuilder.registerTypeAdapter(MCRCategoryImpl.class, new MCRCategoryJson.Deserializer());
        gsonBuilder.registerTypeAdapter(MCRCategoryListWrapper.class, new MCRCategoryListJson.Serializer());
        gsonBuilder.registerTypeAdapter(MCRCategoryListWrapper.class, new MCRCategoryListJson.Deserializer());
        gsonBuilder.registerTypeAdapter(MCRLabelSetWrapper.class, new MCRLabelSetJson.Serializer());
        gsonBuilder.registerTypeAdapter(MCRLabelSetWrapper.class, new MCRLabelSetJson.Deserializer());
    }
    
    public static GsonManager instance(){
        if(instance == null){
            instance = new GsonManager();
        }
    
        return instance;
    }
    
    public Gson createGson(){
        return gsonBuilder.create();
    }
}
