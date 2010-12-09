package fsu.jportal.backend.api;

import fsu.jportal.jaxb.JournalList;

public interface JournalListBackend {

    public String PROP_NAME = "JP.JournalList.backend";

    public JournalList getList(String type);

    public JournalList createList(String type);

}
