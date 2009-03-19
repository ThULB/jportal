package org.mycore.common.xml;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Hashtable;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import org.mycore.common.xml.MCRURIResolver.MCRResolver;
import org.mycore.services.fieldquery.MCRQuery;
import org.mycore.services.fieldquery.MCRQueryManager;
import org.mycore.services.fieldquery.MCRResults;

public class MCRJPortalQueryResolver implements MCRResolver {

    private static final Logger LOGGER = Logger.getLogger(MCRJPortalQueryResolver.class);

    private static final String QUERY_PARAM = "term";
    private static final String SORT_PARAM = "sortby";
    private static final String ORDER_PARAM = "order";
    private static final String MAXRESULTS_PARAM = "maxResults";

    /**
     * Returns query results for query in "term" parameter
     */
    public Element resolveElement(String uri) {
        String key = uri.substring(uri.indexOf(":") + 1);
        LOGGER.debug("Reading xml from query result using key :" + key);

        Hashtable<String, String> params = getParameterMap(key);

        String query;
        try {
            query = URLDecoder.decode(params.get(QUERY_PARAM), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
        String sortby = params.get(SORT_PARAM);
        String order = params.get(ORDER_PARAM);
        String maxResults = getMaxResults(params);

        if (query == null) {
            return null;
        }
        // add deletedFlag condition if its not set
        query = addDeletedFlag(query);

        Document input = getQueryDocument(query, sortby, order, maxResults);
        // Execute query
        long start = System.currentTimeMillis();
        MCRResults result = MCRQueryManager.search(MCRQuery.parseXML(input));
        long qtime = System.currentTimeMillis() - start;
        LOGGER.debug("MCRSearchServlet total query time: " + qtime);
        return result.buildXML();
    }

    private static String getMaxResults(Hashtable<String, String> params) {
        String maxResults = params.get(MAXRESULTS_PARAM);
        if (maxResults != null && !maxResults.equals(""))
            return maxResults;
        return "0";
    }

    private static String addDeletedFlag(String oldQry) {
        String newQry = oldQry;
        if(!newQry.contains("deletedFlag"))
            newQry = "(" + newQry + ") and (deletedFlag = false)";
        return newQry;
    }

    private static Document getQueryDocument(String query, String sortby, String order, String maxResults) {
        Element queryElement = new Element("query");
        queryElement.setAttribute("maxResults", maxResults);
        queryElement.setAttribute("numPerPage", "0");
        Document input = new Document(queryElement);

        Element conditions = new Element("conditions");
        queryElement.addContent(conditions);
        conditions.setAttribute("format", "text");
        conditions.addContent(query);
        org.jdom.Element root = input.getRootElement();
        if (sortby != null) {
            final Element fieldElement = new Element("field").setAttribute("name", sortby);
            if (order != null) {
                fieldElement.setAttribute("order", order);
            }
            root.addContent(new Element("sortBy").addContent(fieldElement));
        }
        if (LOGGER.isDebugEnabled()) {
            XMLOutputter out = new XMLOutputter(org.jdom.output.Format.getPrettyFormat());
            LOGGER.debug(out.outputString(input));
        }
        return input;
    }

    private static Hashtable<String, String> getParameterMap(String key) {
        String[] param;
        StringTokenizer tok = new StringTokenizer(key, "&");
        Hashtable<String, String> params = new Hashtable<String, String>();

        while (tok.hasMoreTokens()) {
            param = tok.nextToken().split("=");
            params.put(param[0], param[1]);
        }
        return params;
    }
}