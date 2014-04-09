package fsu.jportal.resources;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.ElementFilter;
import org.jdom2.filter.Filters;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.sru.SRUConnector;
import org.mycore.sru.SRUConnectorFactory;

import fsu.archiv.mycore.sru.GBVKeywordStore;
import fsu.archiv.mycore.sru.SRUQueryParser;
import fsu.archiv.mycore.sru.impex.pica.model.Datafield;
import fsu.archiv.mycore.sru.impex.pica.model.PicaRecord;
import fsu.archiv.mycore.sru.impex.pica.model.Subfield;
import fsu.archiv.mycore.sru.impex.pica.model.provider.SRURecordProvider;
import fsu.jportal.mycore.sru.impex.pica.producer.InstitutionProducer;
import fsu.jportal.mycore.sru.impex.pica.producer.JPPersonProducer;

@Path("sru")
public class SRUResource {

    static Logger LOGGER = Logger.getLogger(SRUResource.class);

    @GET
    @Path("search")
    @Produces(MediaType.APPLICATION_XML)
    public Response query(@QueryParam("q") String query) {
        SRUQueryParser queryParser = new SRUQueryParser(GBVKeywordStore.getInstance());
        SRUConnector connector = SRUConnectorFactory.getSRUConnector(SRUConnectorFactory.GBV_SRU_STANDARD_CONNECTION,
                queryParser.parse("num " + query));
        connector.setMaximumRecords(10);
        Document xml;
        try {
            xml = connector.getDocument();
        } catch (ConnectException connectException) {
            LOGGER.error("while retrieving document", connectException);
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        }
        XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
        if (LOGGER.isDebugEnabled()) {
            LOGGER.info(out.outputString(xml));
        }
        List<PicaRecord> recordList = getPicaRecords(xml);
        Element returnElement = new Element("sruobjects");
        for (PicaRecord picaRecord : recordList) {
            String objectType = picaRecord.getValue("002@", "0");
            if (isPerson(objectType)) {
                try {
                    Document doc = new JPPersonProducer().procudeRawMCRObject(picaRecord);
                    returnElement.addContent(doc.getRootElement().detach());
                } catch (Exception exc) {
                    LOGGER.error("unable to parse pica record", exc);
                    if(LOGGER.isDebugEnabled()) {
                        LOGGER.debug(picaRecord);
                    }
                }
            } else if (isInstitution(objectType)) {
                try {
                    Document doc = new InstitutionProducer().produceRawMCRObject(picaRecord);
                    returnElement.addContent(doc.getRootElement().detach());
                } catch (Exception exc) {
                    LOGGER.error("unable to parse pica record", exc);
                    if(LOGGER.isDebugEnabled()) {
                        LOGGER.debug(picaRecord);
                    }
                }
            } else {
                LOGGER.error("pica record with invalid type '" + objectType +"' found");
                if(LOGGER.isDebugEnabled()) {
                    LOGGER.debug(picaRecord);
                }
            }
        }
        return Response.ok(out.outputString(returnElement), MediaType.APPLICATION_XML).build();
    }

    private boolean isPerson(String picaObjectType) {
        return picaObjectType.charAt(0) == 'T' && picaObjectType.charAt(1) == 'p';
    }

    private boolean isInstitution(String picaObjectType) {
        return picaObjectType.charAt(0) == 'T' && picaObjectType.charAt(1) == 'b';
    }
    
    // copy from fsu.archiv.mycore.sru.impex.pica.model.provider.SRURecordProvider
    public List<PicaRecord> getPicaRecords(Document source) {
        List<PicaRecord> records = new ArrayList<>();
        if(source == null){
            return records;
        }
        
        Document doc = (Document) source;
        ArrayList<Namespace> namespaces = new ArrayList<Namespace>();
        namespaces.add(Namespace.getNamespace("zs", "http://www.loc.gov/zing/srw/"));
        namespaces.add(Namespace.getNamespace("pica", "info:srw/schema/5/picaXML-v1.0"));
        XPathExpression<Element> xp = XPathFactory.instance().compile("//pica:record", Filters.element(), null, namespaces);
        List<Element> recordElements = xp.evaluate(doc);
        for (Element recordElement : recordElements) {
            records.add(convertToPica(recordElement));
        }
        return records;
    }
    
    private PicaRecord convertToPica(Element record) {
        PicaRecord pr = new PicaRecord();
        Iterator<Element> it = record.getDescendants(new ElementFilter("datafield"));

        while (it.hasNext()) {
            Element dfElem = it.next();
            String tag = dfElem.getAttributeValue("tag");
            String occ = dfElem.getAttributeValue("occurrence");
            Datafield df = new Datafield(tag, occ);
            pr.addDatafield(df);

            Iterator<Element> subfieldIterator = dfElem.getDescendants(new ElementFilter("subfield"));
            while (subfieldIterator.hasNext()) {
                Element sfElem = subfieldIterator.next();
                String code = sfElem.getAttributeValue("code");
                String value = sfElem.getText();
                Subfield sf = new Subfield(code, value);
                df.addSubField(sf);
            }
        }

        return pr;
    }

}
