package fsu.jportal.gson;

import org.mycore.datamodel.classifications2.impl.MCRCategoryImpl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import fsu.jportal.metadata.Rubric;

public class GsonManager {
    private GsonBuilder gsonBuilder;
    private static GsonManager instance;
    
    private GsonManager() {
        gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Rubric.class, new RubricSerializer());
        gsonBuilder.registerTypeAdapter(Rubric.class, new RubricDeserializer());
        gsonBuilder.registerTypeAdapter(MCRCategoryImpl.class, new MCRCategorySerializer());
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
