package org.mycore.resources;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.jdom.Element;
import org.mycore.parsers.bool.MCRCondition;
import org.mycore.services.fieldquery.MCRFieldValue;
import org.mycore.services.fieldquery.MCRHit;
import org.mycore.services.fieldquery.MCRQuery;
import org.mycore.services.fieldquery.MCRQueryManager;
import org.mycore.services.fieldquery.MCRQueryParser;
import org.mycore.services.fieldquery.MCRResults;

import fsu.thulb.http.UriTools;
import fsu.thulb.jp.searchpojo.AtomLink;
import fsu.thulb.jp.searchpojo.FieldValue;
import fsu.thulb.jp.searchpojo.Hit;
import fsu.thulb.jp.searchpojo.SearchResults;

@Path("/calendar")
public class CalenderResource {

    @GET
    @Produces(MediaType.APPLICATION_XML)
    public SearchResults calendar(@Context UriInfo uriInfo, @Context HttpHeaders headers){
        String host = headers.getRequestHeader("host").get(0);
        String baseURI = uriInfo.getBaseUri().toString();
        if(!baseURI.endsWith(host + "/")){
            new UriTools();
            baseURI = UriTools.removeLastPathSegment(baseURI);
        }
        
        MCRQueryParser parser = new MCRQueryParser();
        Element condition = new Element("condition");
        condition.setAttribute("field", "identis_vol");
        condition.setAttribute("operator", "like");
        condition.setAttribute("value", "KAL1:*");
        MCRCondition mcrCondition = parser.parse(condition);
        
        MCRResults results = MCRQueryManager.search(new MCRQuery(mcrCondition));
        return createSearchResults(results, baseURI);
    }

    private SearchResults createSearchResults(MCRResults results, String baseUri) {
        ArrayList<Hit> hitList = new ArrayList<Hit>();
        for (MCRHit mcrHit : results) {
            Hit hit = new Hit();
            AtomLink atomLink = createAtomLink(mcrHit.getID(), baseUri);
            List<FieldValue> fieldValues = createFieldValues(mcrHit.getMetaData());
            hit.setLink(atomLink);
            hit.setFieldValues(fieldValues);
            
            hitList.add(hit);
        }
        
        return new SearchResults(hitList);
    }

    private AtomLink createAtomLink(String id, String baseUri) {
        AtomLink atomLink = new AtomLink();
        String href = baseUri + "receive/" + id + "?XSL.Style=xml";
        atomLink.setHref(href);
        
        return atomLink;
    }

    private List<FieldValue> createFieldValues(List<MCRFieldValue> metaData) {
        ArrayList<FieldValue> fieldValues = new ArrayList<FieldValue>();
        for (MCRFieldValue mcrFieldValue : metaData) {
            String field = mcrFieldValue.getField().getName();
            String value = mcrFieldValue.getValue();
            fieldValues.add(new FieldValue(field, value));
        }
        return fieldValues;
    }
    
}
