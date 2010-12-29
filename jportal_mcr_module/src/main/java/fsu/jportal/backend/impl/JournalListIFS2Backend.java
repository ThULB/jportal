package fsu.jportal.backend.impl;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.mycore.common.MCRConfiguration;
import org.mycore.datamodel.ifs2.MCRContent;
import org.mycore.datamodel.ifs2.MCRFile;
import org.mycore.datamodel.ifs2.MCRFileCollection;
import org.mycore.datamodel.ifs2.MCRFileStore;
import org.mycore.datamodel.ifs2.MCRNode;
import org.mycore.datamodel.ifs2.MCRStore.MCRStoreConfig;
import org.mycore.datamodel.ifs2.MCRStoreManager;
import org.w3c.dom.Document;

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
        setStoreConfig(config);
    }

    public JournalListIFS2Backend() {
        this(new DefaultStoreConfig());
    }

    @Override
    public JournalList getList(String type) {
        try {
            MCRFileCollection fileCollection = getJournalListStore().retrieve(collectionID);
            if (fileCollection != null) {
                MCRNode listFile = fileCollection.getNodeByPath(buildListFileName(type));
                return JaxbTools.unmarschall(listFile.getContent().getInputStream(), JournalList.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private MCRFileStore getJournalListStore() {
        return MCRStoreManager.getStore(getStoreConfig().getID(), MCRFileStore.class);
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
        
        MCRContent source = MCRContent.readFrom(journalListAsString);
        MCRFile listFile = getOrCreateFileInBackend(listFileName);
        listFile.setContent(source);
    }

    private MCRFile getOrCreateFileInBackend(String listFileName) throws Exception {
        MCRFileCollection fileCollection = getOrCreateFileCollection();
        
        MCRFile listFile = (MCRFile) fileCollection.getNodeByPath(listFileName);
        if(listFile == null) {
            listFile = fileCollection.createFile(listFileName);
        }
        return listFile;
    }

    private MCRFileCollection getOrCreateFileCollection() throws Exception {
        MCRFileCollection fileCollection = getJournalListStore().retrieve(collectionID);
        if(fileCollection == null) {
            fileCollection = getJournalListStore().create(collectionID);
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
        
        if(type == null){
            throw new Exception("Malformed journal list.");
        }
        return type;
    }
    
    private Document newDocument() throws ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        Document doc = documentBuilderFactory.newDocumentBuilder().newDocument();
        return doc;
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


}
