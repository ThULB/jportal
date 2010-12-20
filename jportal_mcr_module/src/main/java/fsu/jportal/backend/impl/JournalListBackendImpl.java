package fsu.jportal.backend.impl;

import org.mycore.datamodel.ifs2.MCRFileStore;

import fsu.jportal.backend.api.JournalListBackend;
import fsu.jportal.jaxb.JournalList;

public class JournalListBackendImpl implements JournalListBackend{
    
    private MCRFileStore store;

    public JournalListBackendImpl() {
        setStore(new MCRFileStore());
    }

    public JournalListBackendImpl(MCRFileStore store) {
        this.setStore(store);
    }

    @Override
    public JournalList getList(String type) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JournalList createList(String type) {
        // TODO Auto-generated method stub
        return null;
    }

    private void setStore(MCRFileStore store) {
        this.store = store;
    }

    private MCRFileStore getStore() {
        return store;
    }

}
