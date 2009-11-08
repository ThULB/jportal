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
            if(recEntry.deletedAt != null)
                entry.setAttribute("deletedAt", recEntry.deletedAt.toString());
            if(recEntry.deletedFrom != null)
                entry.setAttribute("deletedFrom", recEntry.deletedFrom);
            entriesElement.addContent(entry);
        }

        // write the xml document to the file system
//        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
//        FileOutputStream output = new FileOutputStream(DIR + "recycleBin.xml");
//        outputter.output(new Document(rootElement), output);

        
        // go to generated xml file
//        job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + "recycleBin.xml"));
        getLayoutService().doLayout(job.getRequest(), job.getResponse(), new Document(rootElement));
    }

    /**
     * Returns an arraylist of all deleted mcr objects.
     * @return arraylist of deleted mcr objects
     */
    protected ArrayList<RecycleBinEntry> getDeletedMCRObjects() {
        ArrayList<RecycleBinEntry> entries = new ArrayList<RecycleBinEntry>();

        MCRFieldDef def = MCRFieldDef.getDef("deletedFlag");
        MCRQueryCondition qryCond = new MCRQueryCondition(def, "=", "true");
        MCRQuery qry = new MCRQuery(qryCond);
        MCRResults results = MCRQueryManager.search(qry);

        for (int i = 0; i < results.getNumHits(); i++) {
            MCRHit hit = results.getHit(i);
            RecycleBinEntry entry = getRecEntryFromId(hit.getID());
            if(entry != null)
                entries.add(entry);            
        }
        return entries;
    }

    /**
     * Returns an arraylist of all deleted mcr derivates.
     * @return arraylist of deleted mcr derivates
     */
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
            if(!subEntries.containsKey(id)) {
                RecycleBinEntry entry = getRecEntryFromId(id);
                if(entry != null)
                    subEntries.put(id, entry);
            }
        }
        return new ArrayList<RecycleBinEntry>(subEntries.values());
    }

    /**
     * Gets the metadata value of an lucene search hit and a field definition.
     * @param hit from which hit you want the meta data
     * @param compareDef of which field definition
     * @return metadatavalue of an field or null if none definied
     */
    protected String getMetaData(MCRHit hit, MCRFieldDef compareDef) {
        List<MCRFieldValue> list = hit.getMetaData();
        for(MCRFieldValue v : list) {
            if(v.getField().equals(compareDef))
                return v.getValue();
        }
        return null;
    }

    /**
     * Creates an RecycleBinEntry instance from the specified id.
     * Try to set the deletedAt and the deletedFrom parameter.
     * @param id id of the mcrbase object
     * @return a new RecycleBinEntry instance
     */
    protected RecycleBinEntry getRecEntryFromId(String id) {
        RecycleBinEntry recEntry = new RecycleBinEntry();
        recEntry.objId = id;
        MCRBase mcrBase = null;
        if (recEntry.getType().toLowerCase().equals("derivate"))
            mcrBase = new MCRDerivate();
        else
            mcrBase = new MCRObject();

        try {
            mcrBase.receiveFromDatastore(recEntry.objId);
        } catch(Exception exc) {
            /* do not throw an exception here because most of the time 
            the object is still in the deleting process.*/
            return null;
        }
        recEntry.deletedAt = mcrBase.getService().getDate("modifydate");
        ArrayList<String> flags = mcrBase.getService().getFlags("deletedFrom");
        if(flags.size() > 0)
            recEntry.deletedFrom = flags.get(0);
        return recEntry;
    }

    /**
     * Helperclass which represents an xml entry. 
     */
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