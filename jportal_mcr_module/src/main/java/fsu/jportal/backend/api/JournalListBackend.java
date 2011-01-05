package fsu.jportal.backend.api;

import java.util.List;

import fsu.jportal.jaxb.JournalList;

public interface JournalListBackend {

    public String PROP_NAME = "JP.JournalList.backend";

    public JournalList getList(String type);
    
    public List<JournalList> getLists();

    public JournalList saveList(JournalList list);

}
