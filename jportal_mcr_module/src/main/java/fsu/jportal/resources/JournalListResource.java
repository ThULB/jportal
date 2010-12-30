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

import org.mycore.common.MCRConfiguration;

import fsu.jportal.backend.api.JournalListBackend;
import fsu.jportal.backend.impl.JournalListIFS2Backend;
import fsu.jportal.jaxb.JournalList;
import fsu.jportal.jaxb.JournalList.Journal;

@Path("journalList")
public class JournalListResource {
    @GET
    @Path("{type}")
    @Produces(MediaType.APPLICATION_XML)
    public JournalList journalList(@PathParam("type") String type) {
        JournalListBackend journalListBackend = getBackend();

        JournalList journalList = journalListBackend.getList(type);
        if(journalList == null){
            journalList = new JournalList();
            journalList.setType(type);
        }
        
        return journalList;
    }

    @POST
    @Path("{type}")
    @Consumes(MediaType.APPLICATION_XML)
    public Response addJournal(@PathParam("type") String type, Journal journal) {
        JournalList journalList = getOrCreateJournalList(type);
        journalList.addJournal(journal);
        getBackend().saveList(journalList);
        return Response.created(URI.create("../")).build();
    }

    @DELETE
    @Path("{type}/{journalID}")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response delJournal(@PathParam("type") String type, @PathParam("journalID") String journalID) {
        JournalList journalList = getOrCreateJournalList(type);

        if (journalList.delJournal(journalID)) {
            return Response.ok().build();
        }

        return Response.notModified().build();
    }

    private JournalList getOrCreateJournalList(String type) {
        JournalListBackend journalListBackend = getBackend();
        JournalList journalList = journalListBackend.getList(type);

        if (journalList == null) {
            journalList = new JournalList();
            journalList.setType(type);
        }

        return journalList;
    }

    private JournalListBackend getBackend() {
        String backendName = MCRConfiguration.instance().getString(JournalListBackend.PROP_NAME, JournalListIFS2Backend.class.getName());
        try {
            Class<JournalListBackend> journalListBackendClass = (Class<JournalListBackend>) Class.forName(backendName);
            return journalListBackendClass.newInstance();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }
}
