package fsu.thulb.webapp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fsu.thulb.model.EpicurLite;
import fsu.thulb.persistence.DNBUrnStorage;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

/**
 * Created by chi on 2019-12-10
 *
 * @author Huu Chi Vu
 */
@Path("/")
public class DNBUrnResource {
    private static final Logger LOGGER = LogManager.getLogger();

    @Context
    private UriInfo info;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public UrnList get() throws Exception {
        UrnList urnList = new UrnList();
        DNBUrnStorage urnStorage = getUrnStorage();
        urnList.addAll(urnStorage.values());
        return urnList;
    }

    @GET()
    @Path("urns/{urn}")
    public Response getUrn(@PathParam("urn") String urn) throws Exception {
        LOGGER.info("Get urns!");
        DNBUrnStorage urnStorage = getUrnStorage();
        if(urnStorage.containsKey(urn)){
            return Response.noContent().build();
        } else {
            return Response.status(NOT_FOUND).build();
        }
    }

    private DNBUrnStorage getUrnStorage() throws Exception {
        return DNBUrnStorage.instance();
    }

    @PUT
    @Path("urns/{urn}")
    @Consumes(MediaType.APPLICATION_XML)
    public Response putUrn(EpicurLite epicurLite, @PathParam("urn") String urn) throws Exception {
        LOGGER.info("Put: " + urn + " - " + epicurLite.getURL());
        DNBUrnStorage urnStorage = getUrnStorage();
        urnStorage.put(urn, epicurLite);

        return Response
                .created(info.getRequestUri())
                .lastModified(new Date())
                .build();
    }

    @POST
    @Path("urns/{urn}/links")
    @Consumes(MediaType.APPLICATION_XML)
    public Response postUrn(EpicurLite epicurLite, @PathParam("urn") String urn) throws Exception {
        LOGGER.info("Post: " + urn + " - " + epicurLite.getURL());
        DNBUrnStorage urnStorage = getUrnStorage();
        urnStorage.update(urn, epicurLite);

        return Response
                .noContent()
                .lastModified(new Date())
                .build();
    }

    @XmlRootElement
    private static class UrnList {
        @XmlElement
        private List<EpicurLite> epicurlite = new ArrayList<>();

        public void add(EpicurLite epicur){
            epicurlite.add(epicur);
        }

        public void addAll(Collection<EpicurLite> epicurs){
            epicurlite.addAll(epicurs);
        }
    }
}
