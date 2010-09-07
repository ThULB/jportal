package org.mycore.resources;

import javax.ws.rs.Path;

import org.jdom.Document;
import org.mycore.services.fieldquery.MCRHit;
import org.mycore.services.fieldquery.MCRQuery;
import org.mycore.services.fieldquery.MCRQueryManager;
import org.mycore.services.fieldquery.MCRResults;
import org.mycore.services.fieldquery.MCRSearcherFactory;

@Path("/calendar")
public class CalenderResource {

    public Object calendar(){
        Document doc = null;
        MCRQuery query = MCRQuery.parseXML(doc);
        MCRResults results = MCRQueryManager.search(query);
        //TODO: Jaxb wrapper for MCRResults
        return null;
    }
}
