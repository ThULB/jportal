package org.mycore.frontend.servlets;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.mycore.common.MCRConfiguration;
import org.mycore.datamodel.metadata.MCRBase;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.services.fieldquery.MCRFieldDef;
import org.mycore.services.fieldquery.MCRFieldValue;
import org.mycore.services.fieldquery.MCRHit;
import org.mycore.services.fieldquery.MCRQuery;
import org.mycore.services.fieldquery.MCRQueryCondition;
import org.mycore.services.fieldquery.MCRQueryManager;
import org.mycore.services.fieldquery.MCRResults;

public class MCRRecycleBinPageGenerationServlet extends MCRServlet {

    private static final String FS = System.getProperty("file.seperator", "/");
    private static final String DIR = MCRConfiguration.instance().getString("MCR.basedir") + FS + "build" + FS + "webapps" + FS;

    @Override
    protected void doGetPost(MCRServletJob job) throws Exception {
        ArrayList<RecycleBinEntry> entries = new ArrayList<RecycleBinEntry>();
        // load mcr objects
        entries.addAll(getDeletedMCRObjects());
        // load derivate objects
        entries.addAll(getDeletedMCRDerivates());

        // create xml document
        Element rootElement = new Element("recycleBin");
        Element entriesElement = new Element("entries");
        rootElement.addContent(entriesElement);
        for (RecycleBinEntry recEntry : entries) {
            Element entry = new Element("entry");
            entry.setAttribute("id", recEntry.objId);
            entry.setAttribute("type", recEntry.getType());
            entry.setAttribute("deletedAt", recEntry.deletedAt.toString());
            entry.setAttribute("deletedFrom", recEntry.deletedFrom);
            entriesElement.addContent(entry);
        }

        // write the xml document to the file system
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        FileOutputStream output = new FileOutputStream(DIR + "recycleBin.xml");
        outputter.output(new Document(rootElement), output);

        // go to generated xml file
        job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + "recycleBin.xml"));
    }

    protected ArrayList<RecycleBinEntry> getDeletedMCRObjects() {
        ArrayList<RecycleBinEntry> entries = new ArrayList<RecycleBinEntry>();

        MCRFieldDef def = MCRFieldDef.getDef("deletedFlag");
        MCRQueryCondition qryCond = new MCRQueryCondition(def, "=", "true");
        MCRQuery qry = new MCRQuery(qryCond);
        MCRResults results = MCRQueryManager.search(qry);

        for (int i = 0; i < results.getNumHits(); i++) {
            MCRHit hit = results.getHit(i);
            entries.add(getRecEntryFromId(hit.getID()));
        }
        return entries;
    }

    protected ArrayList<RecycleBinEntry> getDeletedMCRDerivates() {
        Hashtable<String, RecycleBinEntry> subEntries = new Hashtable<String, RecycleBinEntry>();

        MCRFieldDef def = MCRFieldDef.getDef("fileDeleted");
        MCRQueryCondition qryCond = new MCRQueryCondition(def, "=", "true");
        MCRQuery qry = new MCRQuery(qryCond);
        MCRResults results = MCRQueryManager.search(qry);

        for(int i = 0; i < results.getNumHits(); i++) {
            MCRHit hit = results.getHit(i);
            String id = getMetaData(hit, MCRFieldDef.getDef("DerivateID"));
            // it is possible that derivates will be found more than one time
            if(!subEntries.containsKey(id))
                subEntries.put(id, getRecEntryFromId(id));
        }
        return new ArrayList<RecycleBinEntry>(subEntries.values());
    }

    protected String getMetaData(MCRHit hit, MCRFieldDef compareDef) {
        List<MCRFieldValue> list = hit.getMetaData();
        for(MCRFieldValue v : list) {
            if(v.getField().equals(compareDef))
                return v.getValue();
        }
        return null;
    }
    
    
    protected RecycleBinEntry getRecEntryFromId(String id) {
        RecycleBinEntry recEntry = new RecycleBinEntry();
        recEntry.objId = id;
        MCRBase mcrBase = null;
        if (recEntry.getType().toLowerCase().equals("derivate"))
            mcrBase = new MCRDerivate();
        else
            mcrBase = new MCRObject();
        mcrBase.receiveFromDatastore(recEntry.objId);
        recEntry.deletedAt = mcrBase.getService().getDate("modifydate");
        recEntry.deletedFrom = mcrBase.getService().getFlags("deletedFrom").get(0);
        return recEntry;
    }

    private class RecycleBinEntry {
        public String objId;

        public Date deletedAt;

        public String deletedFrom;

        public String getType() {
            int beginIndex = objId.indexOf('_') + 1;
            int endIndex = objId.indexOf('_', beginIndex);
            return objId.substring(beginIndex, endIndex);
        }
    }
}