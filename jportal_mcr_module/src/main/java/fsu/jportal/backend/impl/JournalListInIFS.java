package fsu.jportal.backend.impl;

import org.mycore.common.MCRConfiguration;

import fsu.jportal.backend.api.JournalListBackend;
import fsu.jportal.jaxb.JournalList;
import fsu.jportal.jaxb.JournalList.Journal;

public class JournalListInIFS{

    public JournalList getOrCreateJournalList(String type) {
        JournalList journalList = getBackend().getList(type);
        if(journalList == null){
            journalList = new JournalList();
            journalList.setType(type);
        }
        return journalList;
    }

    public void addJournalToListOfType(String type, Journal journal) {
        JournalList journalList = new JournalListInIFS().getOrCreateJournalList(type);
        journalList.addJournal(journal);
        getBackend().saveList(journalList);
    }

    public boolean deleteJournalInListOfType(String type, String journalID) {
        JournalList journalList = getOrCreateJournalList(type);
        return journalList.delJournal(journalID);
    }

    private JournalListBackend getBackend() {
        String backendName = MCRConfiguration.instance().getString(JournalListBackend.PROP_NAME, JournalListIFS2Backend.class.getName());
        try {
            Class<JournalListBackend> journalListBackendClass = (Class<JournalListBackend>) Class.forName(backendName);
            return journalListBackendClass.newInstance();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    
        return null;
    }
    
}