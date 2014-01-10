package fsu.jportal.pref;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import fsu.jportal.resolver.JournalFilesResolver;

public class JournalConfig {
    private String objID;
    private Properties journalPrefs;

    public JournalConfig(String objID) {
        this.objID = objID;
    }
    
    public JournalConfig(String objID, String prefID) {
        this.objID = objID;
        journalPrefs = new Properties();
        loadJournalPref(objID, prefID);
    }
    
    public String getKey(String key){
        return getKey(key, null);
    }
    
    public String getKey(String key, String defaultVal){
        return journalPrefs.getProperty(key, defaultVal);
    }
    
    private void loadJournalPref(String objid, String prefID) {
        JournalFilesResolver journalFilesResolver = new JournalFilesResolver();
        try {
            FileInputStream journalConfig = journalFilesResolver.getJournalFile(objid + "/conf/"+ prefID +".properties");
            if (journalConfig != null) {
                journalPrefs.load(journalConfig);
            }
            
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}