package fsu.jportal.resources;

import java.net.ConnectException;
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
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.mycore.sru.SRUConnector;
import org.mycore.sru.SRUConnectorFactory;

import fsu.archiv.mycore.sru.GBVKeywordStore;
import fsu.archiv.mycore.sru.SRUQueryParser;
import fsu.archiv.mycore.sru.impex.pica.model.PicaRecord;
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
        SRUQueryParser queryParser = new SRUQueryParser(new GBVKeywordStore(GBVKeywordStore.KEYWORDS_URL_GBV_DB_11));
        SRUConnector connector = SRUConnectorFactory.getSRUConnector(SRUConnectorFactory.GBV_SRU_CONNECTION_DB_11,
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
        List<PicaRecord> recordList = new SRURecordProvider().getPicaRecords(xml);
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

}
