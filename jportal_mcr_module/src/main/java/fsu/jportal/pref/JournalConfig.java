package fsu.jportal.pref;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import fsu.jportal.resolver.JournalFilesResolver;

public class JournalConfig {
    private String prefFileName;
    private Properties journalPrefs;
    private String prefFolderName;

    public JournalConfig(String objID, String prefID) {
        journalPrefs = new Properties();
        prefFolderName = objID + "/conf/";
        prefFileName = prefID + ".properties";
        loadJournalPref(objID, prefID);
    }
    
    public String getKey(String key){
        return getKey(key, "");
    }
    
    public String getKey(String key, String defaultVal){
        return journalPrefs.getProperty(key, defaultVal);
    }
    
    public void setKey(String fsType, String imprintName){
        journalPrefs.setProperty(fsType, imprintName);
        storePref();
    }
    
    public void removeKey(String key){
        journalPrefs.remove(key);
        storePref();
    }

    private void storePref() {
        JournalFilesResolver journalFilesResolver = new JournalFilesResolver();
        File prefFolder = journalFilesResolver.getJournalFileFolder(prefFolderName);
        
        if(!prefFolder.exists()){
            prefFolder.mkdirs();
        }
        
        File prefFile = new File(prefFolder, prefFileName);
        try {
            journalPrefs.store(new FileOutputStream(prefFile), null);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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