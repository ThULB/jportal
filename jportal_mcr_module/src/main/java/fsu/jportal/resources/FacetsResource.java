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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.BiConsumer;

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
