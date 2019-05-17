package fsu.jportal.util;

import java.io.IOException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import fsu.archiv.mycore.sru.GBVKeywordStore;
import fsu.archiv.mycore.sru.SRUQueryParser;
import fsu.archiv.mycore.sru.impex.pica.model.Datafield;
import fsu.archiv.mycore.sru.impex.pica.model.PicaRecord;
import fsu.archiv.mycore.sru.impex.pica.model.Subfield;
import fsu.jportal.backend.pica.JPInstitutionProducer;
import fsu.jportal.backend.pica.JPPersonProducer;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.ElementFilter;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.datamodel.metadata.MCRMetaLangText;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.solr.MCRSolrClientFactory;
import org.mycore.sru.SRUConnector;
import org.mycore.sru.SRUConnectorFactory;

public class GndUtil {

    /**
     * Returns the mycore object with the given gnd id. Returns null if there
     * is no such object. Uses solr gnd.id as query.
     *
     * @param gndId gnd id
     * @return mycore object with the given id
     * @throws SolrServerException if a solr error occur
     */
    public static SolrDocument getMCRObject(String gndId) throws SolrServerException, IOException {
        SolrClient solrClient = MCRSolrClientFactory.getMainSolrClient();
        ModifiableSolrParams p = new ModifiableSolrParams();
        p.set("q", "id.gnd:" + gndId);
        p.set("rows", 1);
        QueryResponse query = solrClient.query(p);
        SolrDocumentList results = query.getResults();
        if (results.isEmpty()) {
            return null;
        }
        return results.get(0);
    }

    /**
     * Returns the gnd identifier of a mycore object.
     *
     * @param mcrObject the mycore object
     * @return optional string of the gnd identifier
     */
    public static Optional<String> getGND(MCRObject mcrObject) {
        return StreamSupport.stream(mcrObject.getMetadata().spliterator(), false)
                .filter(me -> me.getTag().equals("def.identifier"))
                .flatMap(me -> StreamSupport.stream(me.spliterator(), false))
                .filter(meta -> meta instanceof MCRMetaLangText)
                .map(meta -> (MCRMetaLangText) meta)
                .filter(meta -> Optional.ofNullable(meta.getType())
                        .filter(s -> s.toLowerCase(Locale.ROOT).equals("gnd"))
                        .isPresent())
                .map(MCRMetaLangText::getText)
                .findFirst();
    }

    /**
     * Returns the mycore object id of the given gnd id. Returns null if there
     * is no such object. Uses solr gnd.id as query.
     *
     * @param gndId gnd id
     * @return mcr id or null
     * @throws SolrServerException if a solr error occur
     */
    public static String getMCRId(String gndId) throws SolrServerException, IOException {
        SolrDocument doc = getMCRObject(gndId);
        return doc == null ? null : (String) doc.getFieldValue("id");
    }

    /**
     * Checks if a mycore object with the given gnd id exists. Returns true if so.
     *
     * @param gndId gnd id to check
     * @return true if an mycore object exists, otherwise false
     * @throws SolrServerException if a solr error occur
     */
    public static boolean exists(String gndId) throws SolrServerException, IOException {
        return getMCRId(gndId) != null;
    }

    /**
     * Opens a url connection to the gbv sru interface and tries to get the pica record
     * with the given gnd id. You can convert this pica record to a mycore object with
     * {@link GndUtil#toMCRObjectDocument(PicaRecord)}.
     *
     * @param gndId gnd identifier
     * @return a <code>PicaRecord</code> instance or null when there is no such record
     * @throws ConnectException is thrown when cannot connect to gbv sru interface
     */
    public static PicaRecord retrieveFromSRU(String gndId) throws ConnectException {
        Document xml = retrieveFromSRU("num " + gndId, 1);
        return xml != null ? getPicaRecord(xml.getRootElement()) : null;
    }

    /**
     * For whatever reason, it is possible that the http://sru.gbv.de/gbvcat interface can return multiple results
     * when searching with a gnd identifier. This is basically the same as {@link #retrieveFromSRU(String)} but it
     * returns the record which has the required fields. E.g. 065A.
     *
     * <p>
     * <a href="http://sru.gbv.de/gbvcat?query=pica.num+%3D+"4037680-1"&version=1.1&operation=searchRetrieve&recordSchema=picaxml&recordPacking=xml&maximumRecords=2&startRecord=1">
     * double record example</a>
     * </p>
     *
     * @param gndId          gnd identifier
     * @param requiredFields list of fields which are required to be in the record
     * @return a <code>PicaRecord</code> instance or null when there is no such record
     * @throws ConnectException is thrown when cannot connect to gbv sru interface
     */
    public static PicaRecord retrieveFromSRU(String gndId, String... requiredFields) throws ConnectException {
        Document xml = retrieveFromSRU("num " + gndId, 10);
        if (xml == null) {
            return null;
        }
        List<PicaRecord> picaRecords = getPicaRecords(xml.getRootElement());
        for (PicaRecord record : picaRecords) {
            boolean isValid = true;
            for (String requiredField : requiredFields) {
                if (record.getDatafieldsByName(requiredField).isEmpty()) {
                    isValid = false;
                    break;
                }
            }
            if (isValid) {
                return record;
            }
        }
        return null;
    }

    private static Document retrieveFromSRU(String query, int numRecords) throws ConnectException {
        SRUQueryParser queryParser = new SRUQueryParser(GBVKeywordStore.getInstance());

        SRUConnector connector = getSRUConnection(queryParser.parse(query));
        connector.setMaximumRecords(numRecords);
        return connector.getDocument();
    }

    private static SRUConnector getSRUConnection(String query) {
        String sruConnectionURL = MCRConfiguration.instance()
                .getString("JP.SRU.Connection.url", "https://sru.k10plus.de/k10plus");
        try {
            return SRUConnectorFactory.getSRUConnector(sruConnectionURL, query);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Converts the given pica record to mycore object.
     *
     * @param picaRecord the pica record to convert
     * @return new mycore object
     * @throws IllegalArgumentException if the {@link PicaRecord} cannot be parsed due an invalid object type
     */
    public static MCRObject toMCRObject(PicaRecord picaRecord) throws IOException {
        return new MCRObject(toMCRObjectDocument(picaRecord));
    }

    /**
     * Converts the given pica record to a mycore xml object.
     *
     * @param picaRecord the pica record to convert
     * @return jdom2 document
     * @throws IllegalArgumentException if the {@link PicaRecord} cannot be parsed due an invalid object type
     */
    public static Document toMCRObjectDocument(PicaRecord picaRecord) throws IOException {
        String objectType = picaRecord.getValue("002@", "0");
        if (isPerson(objectType)) {
            return new JPPersonProducer().procudeRawMCRObject(picaRecord);
        } else if (isInstitution(objectType)) {
            return new JPInstitutionProducer().produceRawMCRObject(picaRecord);
        }
        throw new IllegalArgumentException("Invalid object type. 002@ has to be either 'Tp' or 'Tb' but is '" +
                objectType + "'.");
    }

    private static boolean isPerson(String picaObjectType) {
        return picaObjectType.charAt(0) == 'T' && picaObjectType.charAt(1) == 'p';
    }

    private static boolean isInstitution(String picaObjectType) {
        return picaObjectType.charAt(0) == 'T' && picaObjectType.charAt(1) == 'b';
    }

    public static List<PicaRecord> getPicaRecords(Element source) {
        XPathExpression<Element> xp = getPicaRecordXPathExpression();
        List<Element> recordList = xp.evaluate(source);
        if (recordList.isEmpty()) {
            return new ArrayList<>();
        }
        return recordList.stream().map(GndUtil::convertToPica).collect(Collectors.toList());
    }

    public static PicaRecord getPicaRecord(Element source) {
        XPathExpression<Element> xp = getPicaRecordXPathExpression();
        Element recordElement = xp.evaluateFirst(source);
        if (recordElement == null) {
            return null;
        }
        return convertToPica(recordElement);
    }

    private static XPathExpression<Element> getPicaRecordXPathExpression() {
        ArrayList<Namespace> namespaces = new ArrayList<>();
        namespaces.add(Namespace.getNamespace("zs", "http://www.loc.gov/zing/srw/"));
        namespaces.add(Namespace.getNamespace("pica", "info:srw/schema/5/picaXML-v1.0"));
        return XPathFactory.instance().compile("//pica:record", Filters.element(), null, namespaces);
    }

    private static PicaRecord convertToPica(Element record) {
        PicaRecord pr = new PicaRecord();
        Iterator<Element> it = record.getDescendants(new ElementFilter("datafield"));

        while (it.hasNext()) {
            Element dfElem = it.next();
            Datafield df = parseDatafield(dfElem);
            pr.addDatafield(df);

            Iterator<Element> subfieldIterator = dfElem.getDescendants(new ElementFilter("subfield"));
            while (subfieldIterator.hasNext()) {
                Element sfElem = subfieldIterator.next();
                Subfield sf = parseSubfield(sfElem);
                df.addSubField(sf);
            }
        }

        return pr;
    }

    private static Datafield parseDatafield(Element dfElem) {
        String tag = dfElem.getAttributeValue("tag");
        String occ = dfElem.getAttributeValue("occurrence");
        return new Datafield(tag, occ);
    }

    private static Subfield parseSubfield(Element sfElem) {
        String code = sfElem.getAttributeValue("code");
        String value = sfElem.getText();
        return new Subfield(code, value);
    }

}
