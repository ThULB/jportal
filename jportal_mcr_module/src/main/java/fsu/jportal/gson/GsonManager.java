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
        gsonBuilder.registerTypeAdapter(Rubric.class, new RubricSerializer());
        gsonBuilder.registerTypeAdapter(Rubric.class, new RubricDeserializer());
        gsonBuilder.registerTypeAdapter(MCRCategoryImpl.class, new MCRCategoryDoJoSerializer());
        gsonBuilder.registerTypeAdapter(MCRCategoryListWrapper.class, new MCRCategoryListDoJoSerializer());
        gsonBuilder.registerTypeAdapter(MCRLabelSetWrapper.class, new MCRLabelSetDoJoSerializer());
        gsonBuilder.registerTypeAdapter(MCRCategoryImpl.class, new MCRCategoryDeserializer());
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
