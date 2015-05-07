package fsu.jportal.resources;

import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.transform.JDOMResult;
import org.jdom2.transform.JDOMSource;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.xml.MCRLayoutService;
import org.mycore.datamodel.common.MCRLinkTableManager;

import com.google.gson.Gson;

import fsu.jportal.backend.ImprintFS;
import fsu.jportal.backend.ImprintManager;
import fsu.jportal.pref.JournalConfig;
import fsu.jportal.util.ImprintUtil;
import fsu.jportal.xml.MCRWebpage;
import fsu.jportal.xml.XMLTools;
import static fsu.jportal.util.ImprintUtil.*;

@Path("fs/{fsType}")
//@MCRRestrictedAccess(IPRuleAccess.class)
public class ImprintResource {

    private static final Logger LOGGER = Logger.getLogger(ImprintResource.class);
    private @PathParam("fsType") String fsType;
    private ImprintFS imprintFS;
    private JournalConfig journalConfig;
    
    public ImprintFS getImprintFS() {
        if(imprintFS == null){
            imprintFS = ImprintManager.createFS(fsType);
        }
        return imprintFS;
    }

    @GET
    @Path("retrieve/{imprintID}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response retrieve(@PathParam("imprintID") String imprintID) {
        Element section = getImprintContent(imprintID);
        XMLOutputter xout = new XMLOutputter(Format.getCompactFormat());
        return Response.ok(xout.outputString(section.getContent())).build(); 
    }

    @POST
    @Path("save")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response save(String data) {
        SaveObj save = new Gson().fromJson(data, SaveObj.class);
        saveImprint(save.imprintID, save.content);
        return Response.ok().build();
    }
    
    private static class SaveObj {
        private String imprintID;
        private String content;
        
        @Override
        public String toString() {
            return imprintID + " - " + content;
        }
    }

    @DELETE
    @Path("delete/{imprintID}")
    public Response delete(@PathParam("imprintID") String imprintID) {
        deleteImprint(imprintID);
        return Response.ok().build();
    }

    @POST
    @Path("edit")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response edit(String data) throws IOException {
        EditObj editO = new Gson().fromJson(data, EditObj.class);

        if (!editO.oldImprintID.equals(editO.newImprintID)){
            editImprintID(editO.oldImprintID, editO.newImprintID);
        }

        if (!editO.oldContent.equals(editO.newContent)) {
            saveImprint(editO.newImprintID, editO.newContent);
        }

        return Response.ok().build();
    }

    private static class EditObj {
        private String oldImprintID;
        private String newImprintID;
        private String oldContent;
        private String newContent;
    }

    /**
     * Renders a imprint as webpage.
     * 
     * @param objID
     * @return
     */
    @GET
    @Path("webpage/{objID}")
    @Produces(MediaType.TEXT_HTML)
    public Response webpage(@PathParam("objID") String objID, @Context HttpServletRequest request, @Context HttpServletResponse response) {
        String imprintID = ImprintUtil.getImprintID(objID, fsType);
        if(imprintID == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        Source xmlSource = null;
        try {
            xmlSource = getImprintFS().receive(imprintID);
        } catch (Exception exc) {
            LOGGER.error("while retrieving imprint " + imprintID, exc);
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }
        InputStream guiXSL = getClass().getResourceAsStream("/xsl/jp-imprint-webpage.xsl");
        JDOMResult result = new JDOMResult();
        Map<String, Object> params = new HashMap<>();
        params.put("journalID", objID);
        try {
            new XMLTools().transform(xmlSource, new StreamSource(guiXSL), params, result);
            MCRLayoutService.instance().doLayout(request, response, new MCRJDOMContent(result.getDocument()));
        } catch (Exception exc) {
            LOGGER.error("while transform imprint " + objID, exc);
            throw new WebApplicationException(exc, Status.INTERNAL_SERVER_ERROR);
        }
        return Response.ok().build();
    }

    /**
     * Returns a list of all imprint id's.
     * 
     * @return
     */
    @GET
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    public Response list() {
        List<String> idList;
        try {
            idList = getImprintFS().list();
        } catch (Exception exc) {
            LOGGER.error("while retrieving imprint list", exc);
            throw new WebApplicationException(exc, Status.INTERNAL_SERVER_ERROR);
        }
        String jsonList = new Gson().toJson(idList);
        String normalizedJsonList = Normalizer.normalize(jsonList, Form.NFC);
        return Response.ok(normalizedJsonList).build();
    }

    /**
     * Returns the imprint id by the given object id.
     * 
     * @param objID
     * @return
     */
    @GET
    @Path("get/{objID}")
    public Response get(@PathParam("objID") String objID) {
        String imprintID = getImprintID(objID, fsType);
//        if (imprintID == null) {
//            return Response.status(Status.NOT_FOUND).entity("no imprint id found").build();
//        }
        return Response.ok(imprintID).build();
    }

    /**
     * Sets the link between the mycore object and the imprint.
     * If the imprintID is equals "null" the link will be removed.
     * 
     * @param objID
     * @param imprintID
     */
    @POST
    @Path("set")
    public void set(@QueryParam("objID") String objID, @QueryParam("imprintID") String imprintID) {
        setLink(objID, imprintID);
    }

    private void deleteImprint (String imprintID) throws WebApplicationException {
        // delete file
        try {
            getImprintFS().delete(imprintID);
        } catch(Exception exc) {
            LOGGER.error("unable to delete imprint file '" + imprintID + "'", exc);
            throw new WebApplicationException(exc, Status.INTERNAL_SERVER_ERROR);
        }
        // remove links
        removeLinks(imprintID);
    }

    private Element getImprintContent(String imprintID) throws WebApplicationException {
        JDOMSource xmlSource = null;
        try {
            xmlSource = getImprintFS().receive(imprintID);
        } catch(JDOMException jdomExc) {
            LOGGER.error("unable to parse imprint webpage of " + imprintID, jdomExc);
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        } catch (Exception exc) {
            LOGGER.error("while retrieving imprint " + imprintID, exc);
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }
        Element section = xmlSource.getDocument().getRootElement().getChild("section");
        if (section == null) {
            LOGGER.error("unable to get section of imprint " + imprintID);
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }
        return section;
    }

    private void setLink(String objID, String imprintID) {
        JournalConfig journalConf = getJournalConf(objID);
        String oldImprintID = getImprintID(objID, fsType);
        if (oldImprintID != null && oldImprintID.equals(imprintID)) {
            return;
        } else if (oldImprintID != null) {
            journalConf.removeKey(fsType);
        }
        if(!imprintID.equals("null")) {
            journalConf.setKey(fsType, imprintID);
        }
        MCRLinkTableManager ltm = MCRLinkTableManager.instance();
        ltm.addReferenceLink(objID, imprintID, fsType, null);
    }

    private void setLinks(Collection<String> objIDs, String imprintID) {
        for(String objID : objIDs) {
            setLink(objID, imprintID);
        }
    }

    private void removeLinks(String imprintID) {
        MCRLinkTableManager ltm = MCRLinkTableManager.instance();
        Collection<String> references = ltm.getSourceOf(imprintID);
        for(String reference : references) {
            ltm.deleteReferenceLink(reference, imprintID, fsType);
            JournalConfig journalConf = getJournalConf(reference);
            String oldImprintID = getImprintID(reference, fsType);
            if (oldImprintID != null) {
                journalConf.removeKey(fsType);
            }
        }
    }

    private void editImprintID(String oldImprintID, String newImprintID) throws IOException {
        // get links
        MCRLinkTableManager ltm = MCRLinkTableManager.instance();
        Collection<String> references = ltm.getSourceOf(oldImprintID);

        // get content
        Element content = getImprintContent(oldImprintID).getParentElement();
        content.detach();

        // create new
        getImprintFS().store(newImprintID, new MCRJDOMContent(content));

        // delete file
        deleteImprint(oldImprintID);

        // set links
        setLinks(references, newImprintID);
    }

    private void saveImprint(String imprintID, String content) {
        try {
            MCRWebpage mcrWebpage = new MCRWebpage();
            mcrWebpage.addSection(new MCRWebpage.Section().addContent(content));
            getImprintFS().store(imprintID, new MCRJDOMContent(mcrWebpage.toXML()));
        } catch (Exception exc) {
            LOGGER.error("unable to store imprint content '" + imprintID + "'", exc);
            throw new WebApplicationException(exc, Status.INTERNAL_SERVER_ERROR);
        }
    }
}
