package fsu.thulb.webapp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fsu.thulb.urn.EpicurLite;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

/**
 * Created by chi on 2019-12-10
 *
 * @author Huu Chi Vu
 */
@Path("dnb")
public class DNBUrnResource {
    private static final Logger LOGGER = LogManager.getLogger();
    private static Map<String, EpicurLite> urns = new ConcurrentHashMap<>();

    @Context
    private UriInfo info;

    @GET
    public UrnList get(){
        UrnList urnList = new UrnList();
        urnList.addAll(urns.values());
        return urnList;
    }

    @GET()
    @Path("urns/{urn}")
    public Response getUrn(@PathParam("urn") String urn){
        LOGGER.info("Get urns!");
        if(urns.containsKey(urn)){
            return Response.noContent().build();
        } else {
            return Response.status(NOT_FOUND).build();
        }
    }

    @PUT
    @Path("urns/{urn}")
    @Consumes(MediaType.APPLICATION_XML)
    public Response putUrn(EpicurLite epicurLite, @PathParam("urn") String urn){
        urns.put(urn, epicurLite);

        urns.forEach((u,e) -> LOGGER.info("Putted: " + u + " - " + e.getURL()));

        return Response
                .created(info.getRequestUri())
                .lastModified(new Date())
                .build();
    }

    @POST
    @Path("urns/{urn}/links")
    @Consumes(MediaType.APPLICATION_XML)
    public Response postUrn(EpicurLite epicurLite, @PathParam("urn") String urn){
        urns.put(urn, epicurLite);

        urns.forEach((u,e) -> LOGGER.info("Posted: " + u + " - " + e.getURL()));
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
