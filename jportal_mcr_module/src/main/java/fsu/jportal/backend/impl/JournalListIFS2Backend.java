package fsu.jportal.backend.impl;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.mycore.common.MCRConfiguration;
import org.mycore.common.MCRConfigurationException;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRStringContent;
import org.mycore.datamodel.ifs2.MCRFile;
import org.mycore.datamodel.ifs2.MCRFileCollection;
import org.mycore.datamodel.ifs2.MCRFileStore;
import org.mycore.datamodel.ifs2.MCRNode;
import org.mycore.datamodel.ifs2.MCRStore.MCRStoreConfig;
import org.mycore.datamodel.ifs2.MCRStoreManager;

import fsu.jportal.backend.api.JournalListBackend;
import fsu.jportal.jaxb.JournalList;
import fsu.thulb.jaxb.JaxbTools;

public class JournalListIFS2Backend implements JournalListBackend {
    private static final int collectionID = 1;

    private MCRStoreConfig storeConfig;

    private static class DefaultStoreConfig implements MCRStoreConfig {
        @Override
        public String getID() {
            return "journalLists";
        }

        @Override
        public String getBaseDir() {
            return MCRConfiguration.instance().getString("MCR.IFS2.Store." + getID() + ".BaseDir", getID());
        }

        @Override
        public String getSlotLayout() {
            return MCRConfiguration.instance().getString("MCR.IFS2.Store." + getID() + ".SlotLayout", "4-2-2");
        }

    }

    public JournalListIFS2Backend(MCRStoreConfig config) {
        if (config == null) {
            try {
                config = (MCRStoreConfig) MCRConfiguration.instance().getInstanceOf("JP.JournalList.IFS.Backend");
            } catch (MCRConfigurationException e) {
                config = new DefaultStoreConfig();
            }
        }

        setStoreConfig(config);
    }

    public JournalListIFS2Backend() {
        // OK this looks pretty ugly, but why still stick with it?
        // because I'm too lazy to change MyCoRe at this moment.
        // I tried this((MCRStoreConfig) MCRConfiguration.instance().getInstanceOf("JP.JournalList.IFS.Backend", DefaultStoreConfig.class.getName()))
        // but DefaultStoreConfig has to be public, which I won't
        // next was this((MCRStoreConfig) MCRConfiguration.instance().getInstanceOf("JP.JournalList.IFS.Backend"))
        // throws MCRConfigurationException when property is not set
        // there are things you can do with MyCore, for the rest there are ugly code.
        this(null);
    }

    @Override
    public JournalList getList(String type) {
        try {
            MCRFileCollection fileCollection = getOrCreateJournalListStore().retrieve(collectionID);
            if (fileCollection != null) {
                MCRNode listFile = fileCollection.getNodeByPath(buildListFileName(type));
                
                if(listFile == null){
                    return null;
                }
                
                return JaxbTools.unmarschall(listFile.getContent().getInputStream(), JournalList.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private MCRFileStore getOrCreateJournalListStore() {
        MCRFileStore store = MCRStoreManager.getStore(getStoreConfig().getID(), MCRFileStore.class);
        if (store == null) {
            try {
                store = MCRStoreManager.createStore(getStoreConfig(), MCRFileStore.class);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return store;
    }

    @Override
    public JournalList saveList(JournalList list) {
        try {
            String listType = getJournalListType(list);
            writeJournalListToBackend(buildListFileName(listType), journalListAsString(list));
            return list;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private void writeJournalListToBackend(String listFileName, String journalListAsString) throws IOException,
            UnsupportedEncodingException, Exception {

        MCRContent source = new MCRStringContent(journalListAsString);
        MCRFile listFile = getOrCreateFileInBackend(listFileName);
        listFile.setContent(source);
    }

    private MCRFile getOrCreateFileInBackend(String listFileName) throws Exception {
        MCRFileCollection fileCollection = getOrCreateFileCollection();

        MCRFile listFile = (MCRFile) fileCollection.getNodeByPath(listFileName);
        if (listFile == null) {
            listFile = fileCollection.createFile(listFileName);
        }
        return listFile;
    }

    private MCRFileCollection getOrCreateFileCollection() throws Exception {
        MCRFileCollection fileCollection = getOrCreateJournalListStore().retrieve(collectionID);
        if (fileCollection == null) {
            fileCollection = getOrCreateJournalListStore().create(collectionID);
        }
        return fileCollection;
    }

    private String journalListAsString(JournalList list) throws JAXBException {
        Marshaller marshaller = JAXBContext.newInstance(JournalList.class).createMarshaller();
        StringWriter stringWriter = new StringWriter();
        marshaller.marshal(list, stringWriter);

        String journalListAsString = stringWriter.toString();
        return journalListAsString;
    }

    private String getJournalListType(JournalList list) throws Exception {
        String type = list.getType();

        if (type == null) {
            throw new Exception("Malformed journal list.");
        }
        return type;
    }

    public String buildListFileName(String type) {
        return type + ".xml";
    }

    private void setStoreConfig(MCRStoreConfig storeConfig) {
        this.storeConfig = storeConfig;
    }

    private MCRStoreConfig getStoreConfig() {
        return storeConfig;
    }

    @Override
    public List<JournalList> getLists() {
        ArrayList<JournalList> arrayList = new ArrayList<JournalList>();
        try {
            MCRFileCollection fileCollection = getOrCreateJournalListStore().retrieve(collectionID);
            if (fileCollection != null) {
                List<MCRNode> listFile = fileCollection.getChildren();
                
                for (MCRNode mcrNode : listFile) {
                    arrayList.add(JaxbTools.unmarschall(mcrNode.getContent().getInputStream(), JournalList.class));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return arrayList;
    }

}
