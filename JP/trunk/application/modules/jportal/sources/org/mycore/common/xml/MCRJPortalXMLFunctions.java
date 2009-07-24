package org.mycore.common.xml;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.mycore.common.MCRConfiguration;
import org.mycore.datamodel.metadata.MCRMetaISO8601Date;
import org.mycore.services.i18n.MCRTranslation;

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
}