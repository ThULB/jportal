package fsu.jportal.resources;

import java.net.URI;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


import fsu.jportal.backend.impl.JournalListInIFS;
import fsu.jportal.jaxb.JournalList;
import fsu.jportal.jaxb.JournalList.Journal;

@Path("journalList")
public class JournalListResource {
    @GET
    @Path("{type}")
    @Produces(MediaType.APPLICATION_XML)
    public JournalList journalList(@PathParam("type") String type) {
        return new JournalListInIFS().getOrCreateJournalList(type);
    }

    @POST
    @Path("{type}")
    @Consumes(MediaType.APPLICATION_XML)
    public Response addJournal(@PathParam("type") String type, Journal journal) {
        new JournalListInIFS().addJournalToListOfType(type, journal);
        return Response.created(URI.create("../")).build();
    }

    @DELETE
    @Path("{type}/{journalID}")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response delJournal(@PathParam("type") String type, @PathParam("journalID") String journalID) {
        if (new JournalListInIFS().deleteJournalInListOfType(type, journalID)) {
            return Response.ok().build();
        }

        return Response.notModified().build();
    }
}
