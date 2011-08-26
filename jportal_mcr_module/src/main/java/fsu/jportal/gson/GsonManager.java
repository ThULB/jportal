package fsu.jportal.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonManager {
    private GsonBuilder gsonBuilder;
    private static GsonManager instance;
    
    private GsonManager() {
        gsonBuilder = new GsonBuilder();
        registerAdapter(new MCRCategoryTypeAdapter());
        registerAdapter(new MCRCategoryIDTypeAdapter());
        registerAdapter(new MCRLabelSetTypeAdapter());
        registerAdapter(new MCRCategoryListTypeAdapter());
    }

    public void registerAdapter(GsonTypeAdapter<?> categIDAdapter) {
        gsonBuilder.registerTypeAdapter(categIDAdapter.bindTo(), categIDAdapter);
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
