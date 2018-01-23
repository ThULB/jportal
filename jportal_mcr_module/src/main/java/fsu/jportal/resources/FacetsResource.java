package fsu.jportal.resources;

import com.google.gson.JsonObject;
import fsu.jportal.util.ResolverUtil;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.backend.jpa.MCREntityManagerProvider;
import org.mycore.common.MCRSessionMgr;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * Created by Huu Chi Vu on 15.06.17.
 */
@Path("facets")
public class FacetsResource {
    @GET
    @Path("label/{categID}")
    public String label(@PathParam("categID") String categID) {
        return ResolverUtil.getClassLabel(categID)
                           .orElse("undefined:" + categID);
    }

    @GET
    @Path("labelsMap")
    @Produces(MediaType.APPLICATION_JSON)
    public Response labels() {
        String currentLang = MCRSessionMgr.getCurrentSession().getCurrentLanguage();
        InputStream layoutSettingsXML = this.getClass().getResourceAsStream("/xml/layoutDefaultSettings.xml");
        SAXBuilder saxBuilder = new SAXBuilder();

        try {
            Document layoutSettingsDoc = saxBuilder.build(layoutSettingsXML);
            XPathFactory xPathFactory = XPathFactory.instance();
            XPathExpression<Element> xpath = xPathFactory
                    .compile("/layoutSettings/editor/jpjournal/bind/row", Filters.element());

            String classIDOr = xpath
                    .evaluate(layoutSettingsDoc)
                    .stream()
                    .map(e -> e.getAttributeValue("class"))
                    .map(classID -> "c.classid = '" + classID + "'")
                    .collect(Collectors.joining(" or "));

            String query = "SELECT c.classid, c.categid, l.text, l.lang "
                    + "FROM mcrcategory c INNER JOIN mcrcategorylabels l "
                    + "on c.internalid = l.category WHERE l.lang='" + currentLang + "' "
                    + "and (" + classIDOr + ");";

            EntityManager em = MCREntityManagerProvider.getCurrentEntityManager();
            Query joinQuery = em.createNativeQuery(query);

            List<Object[]> resultList = joinQuery.getResultList();

            BiConsumer<JsonObject, CategLabels> accu = (jsonObj, l) -> jsonObj
                    .add(l.id, l.labelJson);

            JsonObject jsonObject = resultList.stream()
                                           .map(CategLabels::new)
                                           .collect(JsonObject::new, accu, (j1, j2) -> {});
            return Response.ok(jsonObject.toString()).build();
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Response.serverError().build();
    }

    private class CategLabels {
        String id;

        JsonObject labelJson;

        public CategLabels(Object[] obj) {
            id = obj[1].toString().equals("") ? obj[0].toString() : obj[0].toString() + ":" + obj[1].toString();
            labelJson = new JsonObject();
            labelJson.addProperty("label", obj[2].toString());
            labelJson.addProperty("lang", obj[3].toString());
        }
    }

    @GET
    @Path("lookupTable")
    @Produces(MediaType.APPLICATION_JSON)
    public Response lookupTable() {
        InputStream layoutSettingsXML = this.getClass().getResourceAsStream("/xml/layoutDefaultSettings.xml");
        SAXBuilder saxBuilder = new SAXBuilder();
        try {
            Document layoutSettingsDoc = saxBuilder.build(layoutSettingsXML);
            XPathFactory xPathFactory = XPathFactory.instance();
            XPathExpression<Element> xpath = xPathFactory
                    .compile("/layoutSettings/editor/jpjournal/bind/row", Filters.element());
            BiConsumer<JsonObject, ? super Element> accu = (jsonObj, elem) -> jsonObj
                    .addProperty(elem.getAttributeValue("class"), elem.getAttributeValue("on"));
            JsonObject jsonObject = xpath
                    .evaluate(layoutSettingsDoc)
                    .stream()
                    .collect(JsonObject::new, accu, (j1, j2) -> {});

            return Response.ok(jsonObject.toString()).build();
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Response.serverError().build();
    }
}
