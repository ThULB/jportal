package org.mycore.common.xml;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.mycore.datamodel.metadata.MCRMetaISO8601Date;
import org.mycore.services.i18n.MCRTranslation;
import org.mycore.user.MCRUserMgr;

public class MCRJPortalXMLFunctions {

    private static final Logger LOGGER = Logger.getLogger(MCRJPortalXMLFunctions.class);

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
        return MCRUserMgr.instance().getCurrentUser().getID();
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
        String solrQuery = null;
        for(String query : queries) {
            SolrFieldQuery solrFieldQuery = new SolrFieldQuery(query);
            if(solrFieldQuery.isValueSet()) {
                solrQuery = solrQuery == null ? solrFieldQuery.toString() : solrQuery + " " + solrFieldQuery.toString();
            }
        }
        return solrQuery == null ? "*" : URLEncoder.encode(solrQuery, "UTF-8");
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
            return this.field + ":" + this.value;
        }
    }
}