package fsu.jportal.resources;

import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.transform.JDOMResult;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.xml.MCRLayoutService;
import org.mycore.datamodel.common.MCRLinkTableManager;
import org.mycore.services.i18n.MCRTranslation;

import com.google.gson.Gson;

import fsu.jportal.backend.GreetingsFS;
import fsu.jportal.backend.GreetingsManager;
import fsu.jportal.backend.ImprintFS;
import fsu.jportal.backend.ImprintManager;
import fsu.jportal.backend.JPObjectConfiguration;
import fsu.jportal.util.ImprintUtil;
import static fsu.jportal.util.ImprintUtil.getImprintID;
import static fsu.jportal.util.ImprintUtil.getJournalConf;
import fsu.jportal.xml.MCRWebpage;
import fsu.jportal.xml.XMLTools;

@Path("fs/{fsType}")
//@MCRRestrictedAccess(IPRuleAccess.class)
public class ImprintResource {

    private static final Logger LOGGER = LogManager.getLogger(ImprintResource.class);

    private @PathParam("fsType") String fsType;

    private ImprintFS imprintFS;

    private GreetingsFS greetingFS;

    public ImprintFS getImprintFS() {
        if (imprintFS == null) {
            imprintFS = ImprintManager.createFS(fsType);
        }
        return imprintFS;
    }

    public GreetingsFS getGreetingFS(String journalID) {
        if (greetingFS == null) {
            greetingFS = GreetingsManager.createFS(journalID);
        }
        return greetingFS;
    }

    @GET
    @Path("retrieve/{imprintID}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response retrieve(@PathParam("imprintID") String imprintID, @QueryParam("objID") String objID) {
        if (fsType.equals("link")) {
            Map<String, String> map = getPropAsMap(objID);
            return Response.ok(map.get(imprintID)).build();
        }
        Element section = null;
        if (fsType.equals("greeting")) {
            if (imprintID.equals("master")) {
                section = ImprintUtil.getDefaultGreeting(objID, MCRTranslation.getCurrentLocale().getLanguage());
            } else {
                section = ImprintUtil.getGreetingContent(getGreetingFS(objID),
                    MCRTranslation.getCurrentLocale().getLanguage());
            }
            if (section == null) {
                return Response.ok("").build();
            }
        } else {
            section = ImprintUtil.getImprintContent(imprintID, getImprintFS(),
                MCRTranslation.getCurrentLocale().getLanguage());
        }
        XMLOutputter xout = new XMLOutputter(Format.getRawFormat());
        return Response.ok(xout.outputString(section.getContent())).build();
    }

    @POST
    @Path("save")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response save(String data) {
        SaveObj save = new Gson().fromJson(data, SaveObj.class);
        if (fsType.equals("link")) {
            saveLink(save.objID, save.imprintID, save.content, null);
        } else {
            saveImprint(save.imprintID, save.content, save.objID);
        }
        return Response.ok().build();
    }

    private static class SaveObj {
        private String objID;

        private String imprintID;

        private String content;

        @Override
        public String toString() {
            return imprintID + " - " + content;
        }
    }

    @DELETE
    @Path("removeLink/{objID}")
    public Response removeLink(@PathParam("objID") String objID) {
        MCRLinkTableManager ltm = MCRLinkTableManager.instance();
        String imprintID = getImprintID(objID, fsType);
        if (imprintID != null) {
            ltm.deleteReferenceLink(objID, imprintID, fsType);
            JPObjectConfiguration journalConf = getJournalConf(objID);
            try {
                journalConf.remove(fsType);
                journalConf.store();
            } catch (Exception exc) {
                LOGGER.error("Unable to remove " + fsType + "=" + imprintID + " from journal config", exc);
            }
        }
        return Response.ok().build();
    }

    @DELETE
    @Path("delete/{imprintID}")
    public Response delete(@PathParam("imprintID") String imprintID, @QueryParam("objID") String objID) {
        if (fsType.equals("link")) {
            deleteLink(objID, imprintID);
        } else {
            deleteImprint(imprintID, objID);
        }
        return Response.ok().build();
    }

    @POST
    @Path("edit")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response edit(String data) throws IOException {
        EditObj editO = new Gson().fromJson(data, EditObj.class);
        if (fsType.equals("link")) {
            saveLink(editO.objID, editO.newImprintID, editO.newContent, editO.oldImprintID);
        } else {
            if (!editO.oldImprintID.equals(editO.newImprintID)) {
                editImprintID(editO.oldImprintID, editO.newImprintID);
            }

            if (!editO.oldContent.equals(editO.newContent)) {
                saveImprint(editO.newImprintID, editO.newContent, editO.objID);
            }
        }

        return Response.ok().build();
    }

    private static class EditObj {
        private String objID;

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
    public Response webpage(@PathParam("objID") String objID, @Context HttpServletRequest request,
        @Context HttpServletResponse response) {
        String imprintID = ImprintUtil.getImprintID(objID, fsType);
        if (imprintID == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        Source xmlSource;
        try {
            xmlSource = getImprintFS().receive(imprintID);
        } catch (Exception exc) {
            throw new InternalServerErrorException("while retrieving imprint " + imprintID, exc);
        }
        InputStream guiXSL = getClass().getResourceAsStream("/xsl/jp-imprint-webpage.xsl");
        JDOMResult result = new JDOMResult();
        Map<String, Object> params = new HashMap<>();
        if (!objID.equals("index") && !objID.equals("")) {
            params.put("journalID", objID);
        }
        try {
            new XMLTools().transform(xmlSource, new StreamSource(guiXSL), params, result);
            MCRLayoutService.instance().doLayout(request, response, new MCRJDOMContent(result.getDocument()));
        } catch (Exception exc) {
            throw new InternalServerErrorException("while transform imprint " + objID, exc);
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
    public Response list(@QueryParam("objID") String objID) {
        List<String> idList;
        if (fsType.equals("link")) {
            Map<String, String> map = getPropAsMap(objID);
            if (map != null) {
                idList = new ArrayList<>(map.keySet());
            } else {
                idList = new ArrayList<>();
            }
        } else {
            try {
                idList = getImprintFS().list();
            } catch (Exception exc) {
                throw new InternalServerErrorException("while retrieving imprint list", exc);
            }
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

    private void deleteImprint(String imprintID, String journalID) {
        // delete file
        try {
            if (fsType.equals("greeting")) {
                getGreetingFS(journalID).delete();
            } else {
                getImprintFS().delete(imprintID);
            }
        } catch (Exception exc) {
            throw new InternalServerErrorException(String.format("unable to delete imprint file '%s'", imprintID), exc);
        }
        // remove links
        removeLinks(imprintID);
    }

    //    private Element getImprintContent(String imprintID) throws WebApplicationException {
    //        JDOMSource xmlSource = null;
    //        try {
    //            xmlSource = getImprintFS().receive(imprintID);
    //        } catch(JDOMException jdomExc) {
    //            LOGGER.error("unable to parse imprint webpage of " + imprintID, jdomExc);
    //            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    //        } catch (Exception exc) {
    //            LOGGER.error("while retrieving imprint " + imprintID, exc);
    //            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    //        }
    //        Element section = xmlSource.getDocument().getRootElement().getChild("section");
    //        if (section == null) {
    //            LOGGER.error("unable to get section of imprint " + imprintID);
    //            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    //        }
    //        return section;
    //    }

    private void setLink(String objID, String imprintID) {
        JPObjectConfiguration journalConf = getJournalConf(objID);
        String oldImprintID = getImprintID(objID, fsType);
        if (oldImprintID != null && oldImprintID.equals(imprintID)) {
            return;
        } else if (oldImprintID != null) {
            journalConf.remove(fsType);
        }
        if (!imprintID.equals("null")) {
            try {
                journalConf.set(fsType, imprintID);
                journalConf.store();
            } catch (Exception exc) {
                LOGGER.error("Unable to store " + fsType + "=" + imprintID + " to journal config", exc);
            }
        }
        MCRLinkTableManager ltm = MCRLinkTableManager.instance();
        ltm.addReferenceLink(objID, imprintID, fsType, null);
    }

    private void setLinks(Collection<String> objIDs, String imprintID) {
        for (String objID : objIDs) {
            setLink(objID, imprintID);
        }
    }

    private void removeLinks(String imprintID) {
        MCRLinkTableManager ltm = MCRLinkTableManager.instance();
        Collection<String> references = ltm.getSourceOf(imprintID);
        for (String reference : references) {
            ltm.deleteReferenceLink(reference, imprintID, fsType);
            JPObjectConfiguration journalConf = getJournalConf(reference);
            String oldImprintID = getImprintID(reference, fsType);
            if (oldImprintID != null) {
                try {
                    journalConf.remove(fsType);
                    journalConf.store();
                } catch (Exception exc) {
                    LOGGER.error("Unable to remove " + fsType + "=" + imprintID + " from journal config", exc);
                }
            }
        }
    }

    private void editImprintID(String oldImprintID, String newImprintID) throws IOException {
        // get links
        MCRLinkTableManager ltm = MCRLinkTableManager.instance();
        Collection<String> references = ltm.getSourceOf(oldImprintID);

        // get content
        Element content = ImprintUtil
            .getImprintContent(oldImprintID, getImprintFS(), MCRTranslation.getCurrentLocale().getLanguage())
            .getParentElement();
        content.detach();

        // create new
        getImprintFS().store(newImprintID, new MCRJDOMContent(content));

        // delete file
        deleteImprint(oldImprintID, "");

        // set links
        setLinks(references, newImprintID);
    }

    private void saveLink(String objID, String imprintID, String content, String oldImprintID) {
        if (objID == null) {
            throw new InternalServerErrorException("unable to store link content, ObjectID is null");
        }
        JPObjectConfiguration journalConf = getJournalConf(objID);
        Map<String, String> newMap = getPropAsMap(objID);
        if (newMap == null) {
            newMap = new HashMap<>();
            newMap.put(imprintID, content);
        } else {
            try {
                journalConf.remove(fsType);
                journalConf.store();
            } catch (Exception exc) {
                LOGGER.error("Unable to remove {}={} from journal config", fsType, imprintID, exc);
            }
            if (oldImprintID != null && !oldImprintID.equals("") && !oldImprintID.equals(imprintID)) {
                newMap.remove(oldImprintID);
            }
            newMap.put(imprintID, content);
        }
        storeToObjectConfig(journalConf, newMap);
    }

    private void deleteLink(String objID, String imprintID) {
        if (objID == null) {
            throw new InternalServerErrorException("unable to store link content, ObjectID is null");
        }
        JPObjectConfiguration journalConf = getJournalConf(objID);
        Map<String, String> newMap = getPropAsMap(objID);
        if (newMap != null) {
            try {
                journalConf.remove(fsType);
                journalConf.store();
            } catch (Exception exc) {
                LOGGER.error("Unable to remove {}={} from journal config", fsType, imprintID, exc);
            }
            newMap.remove(imprintID);
        }
        if (newMap.size() > 0) {
            storeToObjectConfig(journalConf, newMap);
        }
    }

    private void storeToObjectConfig(JPObjectConfiguration objectConfig, Map<String, String> newMap) {
        String value = mapToProp(newMap);
        try {
            objectConfig.set(fsType, value);
            objectConfig.store();
        } catch (Exception exc) {
            LOGGER.error("Unable to set {}={} on journal configuration", fsType, value, exc);
        }
    }

    private Map<String, String> getPropAsMap(String objID) {
        String prop = getImprintID(objID, fsType);
        Map<String, String> map = new HashMap<>();
        if (prop != null && !prop.equals("")) {
            Gson gson = new Gson();
            map = gson.fromJson(prop, map.getClass());
        }
        return map;
    }

    private String mapToProp(Map<String, String> map) {
        Gson gson = new Gson();
        return gson.toJson(map);
    }

    private void saveImprint(String imprintID, String content, String journalID) {
        try {
            MCRWebpage mcrWebpage = new MCRWebpage();
            mcrWebpage.addSection(new MCRWebpage.Section().addContent(content));
            if (fsType.equals("greeting")) {
                getGreetingFS(journalID).store(new MCRJDOMContent(mcrWebpage.toXML()));
            } else {
                getImprintFS().store(imprintID, new MCRJDOMContent(mcrWebpage.toXML()));
            }
        } catch (Exception exc) {
            throw new InternalServerErrorException(String.format("unable to store imprint content '%s'", imprintID),
                exc);
        }
    }
}
