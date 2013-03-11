package org.mycore.common.xml;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.mycore.datamodel.metadata.MCRMetaISO8601Date;
import org.mycore.services.i18n.MCRTranslation;
import org.mycore.user2.MCRUserManager;
import org.mycore.common.MCRConfiguration;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.MCRTextResolver;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class MCRJPortalXMLFunctions {

    private static final Logger LOGGER = Logger.getLogger(MCRJPortalXMLFunctions.class);
    private static final ThreadLocal<DocumentBuilder> BUILDER_LOCAL = new ThreadLocal<DocumentBuilder>() {
        @Override
        protected DocumentBuilder initialValue() {
            try {
                return DocumentBuilderFactory.newInstance().newDocumentBuilder();
            } catch (ParserConfigurationException pce) {
                LOGGER.error("Unable to create document builder", pce);
                return null;
            }
        }
    };

    public static String formatISODate(String isoDate, String iso639Language) throws ParseException {
        if (LOGGER.isDebugEnabled()) {
            StringBuffer sb = new StringBuffer("isoDate=");
            sb.append(isoDate).append(", iso649Language=").append(iso639Language);
            LOGGER.debug(sb.toString());
        }
        Locale locale = new Locale(iso639Language);
        SimpleDateFormat df = new SimpleDateFormat(getFormat(isoDate), locale);
        MCRMetaISO8601Date mcrdate = new MCRMetaISO8601Date();
        mcrdate.setDate(isoDate);
        Date date = mcrdate.getDate();
        return (date == null) ? "?" + isoDate + "?" : df.format(date);
    }

    public static String getUserID() {
        return MCRUserManager.getCurrentUser().getUserID();
    }

    public static String getFormat(String date) {
        if(date != null && !date.equals("")) {
            String split[] = date.split("-");
            switch(split.length) {
                case 1: return MCRTranslation.translate("metaData.dateYear");
                case 2: return MCRTranslation.translate("metaData.dateYearMonth");
                case 3: return MCRTranslation.translate("metaData.dateYearMonthDay");
            }
        }
        return MCRTranslation.translate("metaData.date");
    }

    public static String toSolrQuery(String input) throws UnsupportedEncodingException {
        String[] queries = input.split("#");
        String contentQuery = null;
        String solrQuery = null;
        for (String query : queries) {
            SolrFieldQuery solrFieldQuery = new SolrFieldQuery(query);
            if (solrFieldQuery.isValueSet()) {
                if (solrFieldQuery.field.equals("content")) {
                    contentQuery = contentQuery == null ? solrFieldQuery.value : contentQuery + " " + solrFieldQuery.value;
                } else {
                    solrQuery = solrQuery == null ? solrFieldQuery.toString() : solrQuery + " " + solrFieldQuery.toString();
                }
            }
        }
        contentQuery = contentQuery == null ? null : "({!join from=returnId to=id}" + contentQuery + ")";
        return solrQuery == null && contentQuery == null ? "*" : contentQuery == null ? solrQuery : solrQuery == null ? contentQuery
                : contentQuery + " AND " + solrQuery;
    }

    public static Document getLanguages() {
        String languagesString = MCRConfiguration.instance().getString("MCR.Metadata.Languages");
        String[] languagesArray = languagesString.split(",");
        LOGGER.debug(languagesArray);
        Document document = BUILDER_LOCAL.get().newDocument();
        Element languages = document.createElement("languages");
        document.appendChild(languages);
        for (String lang : languagesArray) {
            Element langElement = document.createElement("lang");
            langElement.setTextContent(lang);
            languages.appendChild(langElement);
        }
        return document;
    }

    /**
     * Checks if the given path exists.
     * 
     * @param path
     * @return
     */
    public static boolean resourceExist(String webResource) {
        String webDir = MCRConfiguration.instance().getString("MCR.webappsDir");
        File f = new File(new File(webDir), webResource);
        return f.exists();
    }

    public String resolveText(String text, String varList) {
        String[] vars = varList.split(",");
        Map<String, String> varMap = new HashMap<String, String>();
        for(String var : vars) {
            String[] split = var.split("=");
            if(split.length == 2) {
                varMap.put(split[0], split[1]);
            }
        }
        MCRTextResolver r = new MCRTextResolver(varMap);
        return r.resolve(text);
    }

    private static class SolrFieldQuery {
        public String field;
        public String value;

        public SolrFieldQuery(String base) {
            this.field = base.substring(0, base.indexOf("="));
            this.value = base.substring(base.indexOf("=") + 1);
        }

        public boolean isValueSet() {
            return this.value != null && !this.value.equals("");
        }

        @Override
        public String toString() {
            return "+" + this.field + ":" + this.value;
        }
    }

    public static String getLastValidPageID() {
        String page = (String) MCRSessionMgr.getCurrentSession().get("lastPageID");
        return page == null ? "" : page;
    }

    public static String setLastValidPageID(String pageID) {
        MCRSessionMgr.getCurrentSession().put("lastPageID", pageID);
        return "";
    }
    
    public static int getCentury(int year){
        return (int) Math.ceil((float)year /100);
    }

}