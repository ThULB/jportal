package fsu.jportal.resources;

import java.net.URI;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import fsu.jportal.backend.api.JournalListBackend;
import fsu.jportal.backend.impl.JournalListBackendImpl;
import fsu.jportal.jaxb.JournalList;
import fsu.jportal.jaxb.JournalList.Journal;
import fsu.jportal.jaxb.JournalList.Section;

@Path("journalList.xml")
public class JournalListResource {
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public JournalList journalList(@QueryParam("type") String type) {
        JournalListBackend journalListBackend = getBackend();

        return journalListBackend.getList(type);
    }

    @POST
    @Path("{type}")
    @Consumes(MediaType.APPLICATION_XML)
    public Response addJournal(@PathParam("type") String type, Journal journal) {
        JournalList journalList = getJournalList(type);

        String sectionName = getSectionName(journal);
        Section section = getSection(journalList, sectionName);

        section.addJournal(journal);

        return Response.created(URI.create("../")).build();
    }

    @DELETE
    @Path("{type}/{journalID}")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response delJournal(@PathParam("type") String type, @PathParam("journalID") String journalID) {
        JournalList journalList = getJournalList(type);
        
        if(journalList.delJournal(journalID)){
            return Response.ok().build();
        }
        
        return Response.notModified().build();
    }

    private Section getSection(JournalList journalList, String sectionName) {
        Section section = journalList.getSection(sectionName);
        if (section == null) {
            section = journalList.newSection(sectionName);
        }
        return section;
    }

    private JournalList getJournalList(String type) {
        JournalListBackend journalListBackend = getBackend();
        JournalList journalList = journalListBackend.getList(type);

        if (journalList == null) {
            journalList = journalListBackend.createList(type);
        }

        return journalList;
    }

    private String getSectionName(Journal journal) {
        return journal.getTitle().toUpperCase().substring(0, 1);
    }

    private JournalListBackend getBackend() {
        String backendName = System.getProperty(JournalListBackend.PROP_NAME, JournalListBackendImpl.class.getName());
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
