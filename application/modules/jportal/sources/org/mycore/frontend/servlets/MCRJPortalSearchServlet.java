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

    protected void doQuery(HttpServletRequest request, HttpServletResponse response) throws IOException {
        MCREditorSubmission sub = (MCREditorSubmission) (request.getAttribute("MCREditorSubmission"));
        Document input = null;
        if (sub != null) // query comes from editor search mask
        {
            input = sub.getXML();
        } else // query comes from HTTP request parameters
        {
            Element query = new Element("query");
            query.setAttribute("mask", getReqParameter(request, "mask", "-"));
            query.setAttribute("maxResults", getReqParameter(request, "maxResults", "0"));
            query.setAttribute("numPerPage", getReqParameter(request, "numPerPage", "0"));
            input = new Document(query);

            
            Element sortBy = new Element("sortBy");
            query.addContent(sortBy);
            
            Enumeration sortNames = request.getParameterNames();
            Vector<String> sf = new Vector<String>();
            while (sortNames.hasMoreElements()) {
                String name = (String) (sortNames.nextElement());
                if (name.contains(".sortField"))
                {
                  sf.add(name);
                }
            }
            
            Collections.sort(sf, new MCRSortfieldComparator());
            for (int i=0;i<sf.size();i++)
            {
              String name = (String)sf.elementAt(i);
              String order = getReqParameter(request, name, "ascending");
              name = name.substring(0, name.indexOf(".sortField"));
              Element field = new Element("field");
              field.setAttribute("name", name);
              field.setAttribute("order", order);
              sortBy.addContent(field);
            }
            
            Element conditions = new Element("conditions");
            query.addContent(conditions);

            if (request.getParameter("search") != null) {
                // Search in default field with default operator

                String defaultOperator = MCRFieldType.getDefaultOperator(MCRFieldDef.getDef(defaultSearchField).getDataType()); 
                Element cond = new Element("condition");
                cond.setAttribute("field", defaultSearchField);
                cond.setAttribute("operator", defaultOperator );
                cond.setAttribute("value", getReqParameter(request, "search", null));

                Element b = new Element("boolean");
                b.setAttribute("operator", "and");
                b.addContent(cond);

                conditions.setAttribute("format", "xml");
                conditions.addContent(b);
            } else if (request.getParameter("query") != null) {
                // Search for a complex query expression

                conditions.setAttribute("format", "text");
                conditions.addContent(request.getParameter("query"));
            } else {
                // Search for name-operator-value conditions given as request
                // parameters

                conditions.setAttribute("format", "xml");
                Element b = new Element("boolean");
                b.setAttribute("operator", "and");
                conditions.addContent(b);

                Enumeration names = request.getParameterNames();
                while (names.hasMoreElements()) {
                    String name = (String) (names.nextElement());
                    if (name.endsWith(".operator") || name.contains(".sortField"))
                        continue;
                    if (" maxResults numPerPage mask ".indexOf(" " + name + " ") >= 0)
                        continue;

                    String operator = request.getParameter(name + ".operator");
                    if (operator == null)
                        operator = MCRFieldType.getDefaultOperator(MCRFieldDef.getDef(name).getDataType()); 

                    Element parent = b;

                    String[] values = request.getParameterValues(name);
                    if (values.length > 1) // Multiple fields with same name,
                    // combine with OR
                    {
                        parent = new Element("boolean");
                        parent.setAttribute("operator", "or");
                        b.addContent(parent);
                    }
                    for (int i = 0; i < values.length; i++) {
                        Element cond = new Element("condition");
                        cond.setAttribute("field", name);
                        cond.setAttribute("operator", operator);
                        cond.setAttribute("value", values[i].trim());
                        parent.addContent(cond);
                    }
                }
            }
        }

        Document clonedQuery = (Document)(input.clone()); // Keep for later re-use

        // Show incoming query document
        if (LOGGER.isDebugEnabled()) {
            XMLOutputter out = new XMLOutputter(org.jdom.output.Format.getPrettyFormat());
            LOGGER.debug(out.outputString(input));
        }

        org.jdom.Element root = input.getRootElement();
        MCRCondition cond =  cleanupQuery(input);
        // add deletedFlag to input except deletedFlag is already set
        if(!isConditionFlagSet(cond, "deletedFlag"))
            addDeletedFlags(input, cond);

        // Execute query
        MCRResults result = MCRQueryManager.search(MCRQuery.parseXML(input));

        String npp = root.getAttributeValue("numPerPage", "0");

        // Store query and results in cache
        new MCRCachedQueryData( result, clonedQuery, cond );

        // Redirect browser to first results page
        sendRedirect(request, response, result.getID(), npp);
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
        }
        // create document
        Element conditionsElement = (Element)doc.getRootElement().getChild("conditions");
        conditionsElement.setAttribute("format", "xml");
        conditionsElement.removeContent();
        // add new conditions element
        conditionsElement.addContent(cond.toXML());
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