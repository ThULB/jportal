package fsu.jportal.resources;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.jdom2.Element;
@Path("wp")
public class MCRWebpageResource {
    
    @GET
    @Path("{fileName}/{pathParams: .+}")
    public Response get(@PathParam("fileName") String fileName, @PathParam("pathParams") List<PathSegment> pathSegments, @Context UriInfo uriInfo) {
        Element pathSegmentsXMl = createXML(pathSegments);
        Element queryParamsXMl = createXML(uriInfo.getQueryParameters());
        
        return null;
    }

    private Element createXML(MultivaluedMap<String, String> queryParameters) {
        Element queryParamXML = new Element("queryParam");
        Set<Entry<String, List<String>>> entrySet = queryParameters.entrySet();
        for (Entry<String, List<String>> query : entrySet) {
            Element queryXLM = new Element("query");
            queryXLM.setAttribute("name", query.getKey());
            
            List<String> values = query.getValue();
            for (String value : values) {
                Element valueXML = new Element("value");
                valueXML.addContent(value);
                queryXLM.addContent(valueXML);
            }
            queryParamXML.addContent(queryXLM);
        }
        return queryParamXML;
    }

    private Element createXML(List<PathSegment> pathSegments) {
        Element pathParamXML = new Element("pathParam");
        for (PathSegment pathSegment : pathSegments) {
            Element segmentXML = new Element("path");
            segmentXML.addContent(pathSegment.getPath());
            pathParamXML.addContent(segmentXML);
        }
        return pathParamXML;
    }
    
    
}
