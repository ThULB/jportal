package fsu.jportal.util;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.ElementFilter;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.solr.MCRSolrServerFactory;
import org.mycore.sru.SRUConnector;
import org.mycore.sru.SRUConnectorFactory;

import fsu.archiv.mycore.sru.GBVKeywordStore;
import fsu.archiv.mycore.sru.SRUQueryParser;
import fsu.archiv.mycore.sru.impex.pica.model.Datafield;
import fsu.archiv.mycore.sru.impex.pica.model.PicaRecord;
import fsu.archiv.mycore.sru.impex.pica.model.Subfield;
import fsu.jportal.mycore.sru.impex.pica.producer.InstitutionProducer;
import fsu.jportal.mycore.sru.impex.pica.producer.JPPersonProducer;

public class GndUtil {

    /**
     * Returns the mycore object id of the given gnd id. Returns null if there
     * is no object which such an id. Uses the solr gnd.id as query.
     * 
     * @param gndId gnd id
     * @return mcr id or null
     * @throws SolrServerException if a solr error occur
     */
    public static String getMCRId(String gndId) throws SolrServerException {
        SolrServer solrServer = MCRSolrServerFactory.getSolrServer();
        ModifiableSolrParams p = new ModifiableSolrParams();
        p.set("id.gnd:" + gndId);
        p.set("rows", 1);
        p.set("fl", "id");
        QueryResponse query = solrServer.query(p);
        return query.getResults().isEmpty() ? null : (String) query.getResults().get(0).getFieldValue("id");
    }

    /**
     * Checks if a mycore object with the given gnd id exists. Returns true if so.
     * 
     * @param gndId gnd id to check
     * @return true if an mycore object exists, otherwise false
     * @throws SolrServerException if a solr error occur
     */
    public static boolean exists(String gndId) throws SolrServerException {
        return getMCRId(gndId) != null;
    }

    /**
     * Opens a url connection to the gbv sru interface and tries to get the pica record
     * with the given gnd id. You can convert this pica record to a mycore object with
     * {@link GndUtil#convertPicaRecord}.
     * 
     * @param gndId gnd identifier
     * @return a <code>PicaRecord</code> instance or null when there is no such record
     * @throws ConnectException is thrown when cannot connect to gbv sru interface
     */
    public static PicaRecord retrieveFromSRU(String gndId) throws ConnectException {
        SRUQueryParser queryParser = new SRUQueryParser(GBVKeywordStore.getInstance());
        SRUConnector connector = SRUConnectorFactory.getSRUConnector(SRUConnectorFactory.GBV_SRU_STANDARD_CONNECTION,
            queryParser.parse("num " + gndId));
        connector.setMaximumRecords(1);
        Document xml = connector.getDocument();
        return getPicaRecord(xml);
    }

    /**
     * Converts the given pica record to a mycore xml object. Be aware that no
     * mycore id is set.
     * 
     * @param picaRecord the pica record to convert
     * @throws IllegalArgumentException if the {@link PicaRecord} cannot be parsed due an invalid object type
     * @return jdom2 document
     */
    public static Document convertPicaRecord(PicaRecord picaRecord) {
        String objectType = picaRecord.getValue("002@", "0");
        if (isPerson(objectType)) {
            return new JPPersonProducer().procudeRawMCRObject(picaRecord);
        } else if (isInstitution(objectType)) {
            return new InstitutionProducer().produceRawMCRObject(picaRecord);
        }
        throw new IllegalArgumentException("Invalid object type. Cannot parse " + objectType);
    }

    private static boolean isPerson(String picaObjectType) {
        return picaObjectType.charAt(0) == 'T' && picaObjectType.charAt(1) == 'p';
    }

    private static boolean isInstitution(String picaObjectType) {
        return picaObjectType.charAt(0) == 'T' && picaObjectType.charAt(1) == 'b';
    }

    // copy from fsu.archiv.mycore.sru.impex.pica.model.provider.SRURecordProvider
    public static PicaRecord getPicaRecord(Document source) {
        if (source == null) {
            return null;
        }
        Document doc = (Document) source;
        ArrayList<Namespace> namespaces = new ArrayList<Namespace>();
        namespaces.add(Namespace.getNamespace("zs", "http://www.loc.gov/zing/srw/"));
        namespaces.add(Namespace.getNamespace("pica", "info:srw/schema/5/picaXML-v1.0"));
        XPathExpression<Element> xp = XPathFactory.instance().compile("//pica:record", Filters.element(), null,
            namespaces);
        Element recordElement = xp.evaluateFirst(doc);
        return convertToPica(recordElement);
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
        Datafield df = new Datafield(tag, occ);
        return df;
    }

    private static Subfield parseSubfield(Element sfElem) {
        String code = sfElem.getAttributeValue("code");
        String value = sfElem.getText();
        Subfield sf = new Subfield(code, value);
        return sf;
    }

}
