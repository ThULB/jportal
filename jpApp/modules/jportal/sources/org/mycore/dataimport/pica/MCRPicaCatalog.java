package org.mycore.dataimport.pica;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.mycore.importer.MCRImportRecord;

/**
 * <p>
 * This is the default class for getting <code>MCRImportRecords</code> from
 * a pica catalog. You have to extend this class and overwrite
 * <code>getConnector(String query)</code> to create a connection to a data
 * source.
 * </p>
 * <p>
 * You have several options to set:
 * <ul>
 * <li>record schema (pica): pica or pica_b - pica_b contains only important fields of a record</li>
 * <li>start record (1): start record of the result list</li>
 * <li>maximum records (20): how much records are converted</li>
 * <li>filter list: records are checked against these filters, so they can be rejected</li>
 * </ul>
 * </p>
 * @author Matthias Eichner
 */
public abstract class MCRPicaCatalog {

    public enum RecordSchema {
        pica, pica_b
    }
    private RecordSchema recordSchema = RecordSchema.pica;

    private int startRecord = 1;

    private int maximumRecords = 20;

    private List<MCRPicaRecordFilter> filterList = new ArrayList<MCRPicaRecordFilter>();

    public void setRecordSchema(RecordSchema recordSchema) {
        this.recordSchema = recordSchema;
    }
    public RecordSchema getRecordSchema() {
        return recordSchema;
    }
    public void setMaximumRecords(int maximumRecords) {
        this.maximumRecords = maximumRecords;
    }
    public int getMaximumRecords() {
        return maximumRecords;
    }
    public void setStartRecord(int startRecord) {
        this.startRecord = startRecord;
    }
    public int getStartRecord() {
        return startRecord;
    }
    public void addRecordFilter(MCRPicaRecordFilter filter) {
        filterList.add(filter);
    }
    public void removeRecordFilter(MCRPicaRecordFilter filter) {
        filterList.remove(filter);
    }
    public List<MCRPicaRecordFilter> getRecordFilterList() {
        return filterList;
    }

    protected abstract MCRPicaConnector getConnector(String query) throws MalformedURLException;

    /**
     * This method creates a connection to the catalog, fires the query and
     * returns the result in a list of <code>MCRImportRecords</code>. The name
     * of the records is equal the recordName.
     * 
     * @param query the query which is fired
     * @param recordName names of the records which are returned
     * @return a list with the result of the query organized as <code>MCRImportRecord</code>
     * @throws IOException
     * @throws JDOMException
     */
    public List<MCRImportRecord> getCatalogData(String query, String recordName) throws IOException, JDOMException {
        MCRPicaConnector connector = getConnector(query);
        HttpURLConnection connection = connector.connect();
        InputStream iStream = connection.getInputStream();
        SAXBuilder builder = new SAXBuilder();
        Document document = builder.build(iStream);

        MCRPicaElementConverter pC = new MCRPicaElementConverter(recordName);
        List<MCRImportRecord> recordList = new ArrayList<MCRImportRecord>();

        MCRPicaElementIterator it = new MCRPicaElementIterator(document);
        while (it.hasNext()) {
            Element recordElement = it.next();
            MCRImportRecord record = pC.convert(recordElement);
            if(applyFilters(record))
                recordList.add(record);
        }
        connector.close();
        return recordList;
    }

    /**
     * This method applies all filters on the record. If the record
     * passes all filters true is returned, otherwise false. 
     * 
     * @param record the record to test
     * @return true if the record passes all filters, otherwise false
     */
    protected boolean applyFilters(MCRImportRecord record) {
        for(MCRPicaRecordFilter filter : filterList) {
            if(!filter.filter(record))
                return false;
        }
        return true;
    }
}