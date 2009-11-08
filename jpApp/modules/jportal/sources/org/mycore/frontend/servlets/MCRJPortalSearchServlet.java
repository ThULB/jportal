package org.mycore.frontend.servlets;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.filter.ElementFilter;
import org.jdom.filter.Filter;
import org.jdom.output.XMLOutputter;
import org.mycore.common.MCRConfiguration;
import org.mycore.frontend.editor.MCREditorSubmission;
import org.mycore.parsers.bool.MCRAndCondition;
import org.mycore.parsers.bool.MCRCondition;
import org.mycore.parsers.bool.MCRNotCondition;
import org.mycore.parsers.bool.MCROrCondition;
import org.mycore.services.fieldquery.MCRCachedQueryData;
import org.mycore.services.fieldquery.MCRFieldDef;
import org.mycore.services.fieldquery.MCRFieldType;
import org.mycore.services.fieldquery.MCRQuery;
import org.mycore.services.fieldquery.MCRQueryCondition;
import org.mycore.services.fieldquery.MCRQueryManager;
import org.mycore.services.fieldquery.MCRResults;
import org.mycore.services.fieldquery.MCRSearchServlet;

public class MCRJPortalSearchServlet extends MCRSearchServlet {
    private static final long serialVersionUID = 1L;
    private final Logger LOGGER = Logger.getLogger(this.getClass());

    private String defaultSearchField;

    /**
     * do defaultSearchField protected in MCRSearchServlet -> this
     * method is not needed
     */
    public void init() throws ServletException {
        super.init();
        MCRConfiguration config = MCRConfiguration.instance();
        String prefix = "MCR.SearchServlet.";
        defaultSearchField = config.getString(prefix + "DefaultSearchField", "allMeta");
    }

    /**
     * Executes a query that comes from editor search mask, and redirects the
     * browser to the first results page
     */
    protected void doQuery(HttpServletRequest request, HttpServletResponse response) throws IOException {

        MCREditorSubmission sub = (MCREditorSubmission) (request.getAttribute("MCREditorSubmission"));
        String searchString = getReqParameter(request, "search", null);
        String queryString = getReqParameter(request, "query", null);

        Document input;
        MCRQuery query;

        if (sub != null) {
            input = (Document) (sub.getXML().clone());
            query = buildFormQuery(sub.getXML().getRootElement());
        } else {
            if (queryString != null)
                query = buildComplexQuery(queryString);
            else if (searchString != null)
                query = buildDefaultQuery(searchString);
            else
                query = buildNameValueQuery(request);

            input = setQueryOptions(query, request);
        }

        // Show incoming query document
        if (LOGGER.isDebugEnabled()) {
            XMLOutputter out = new XMLOutputter(org.jdom.output.Format.getPrettyFormat());
            LOGGER.debug(out.outputString(input));
        }

        addDeletedFlags(input, query.getCondition());
        
        MCRCachedQueryData qd = MCRCachedQueryData.cache(query, input);
        sendRedirect(request,response,qd,input);
    }

    protected boolean isConditionFlagSet(MCRCondition cond, String flag) {
        if(cond.toString().contains(flag))
            return true;
        return false;
    }

    /**
     * changes the condition in a form like: ((old cond) and (deletedFlag = false)) or fileDeleted = false
     * @param doc the document to change
     */
    protected void addDeletedFlags(Document doc, MCRCondition cond) {
        if(!isConditionFlagSet(cond, "deletedFlag")) {
            // create deletedFlag condition
            MCRFieldDef fieldDef = MCRFieldDef.getDef("deletedFlag");
            String op = "=";
            String value = "false";
            MCRQueryCondition deletedFlagCond = new MCRQueryCondition(fieldDef, op, value);
            cond = new MCRAndCondition(cond, deletedFlagCond);
            
            // create document
            Element conditionsElement = (Element)doc.getRootElement().getChild("conditions");
            conditionsElement.setAttribute("format", "xml");
            conditionsElement.removeContent();
            // add new conditions element
            conditionsElement.addContent(cond.toXML());
        }
    }

    private String getReqParameter(HttpServletRequest req, String name, String defaultValue) {
        String value = req.getParameter(name);
        if ((value == null) || (value.trim().length() == 0))
            return defaultValue;
        else
            return value.trim();
    }

    private class MCRSortfieldComparator implements Comparator {
        public int compare(Object arg0, Object arg1) {
            String s0 = (String) arg0;
            s0 = s0.substring(s0.indexOf(".sortField"));
            String s1 = (String) arg1;
            s1 = s1.substring(s1.indexOf(".sortField"));
            return s0.compareTo(s1);
        }
    };
}