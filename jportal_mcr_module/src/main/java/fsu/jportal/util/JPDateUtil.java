package fsu.jportal.util;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.mycore.common.MCRException;
import org.mycore.common.xml.MCRXMLFunctions;
import org.mycore.datamodel.metadata.JPMetaDate;
import org.mycore.services.i18n.MCRTranslation;

/**
 * JPortal utility class for the java.time API.
 *
 * @author Matthias Eichner
 */
public abstract class JPDateUtil {

    /**
     * Converts a string to a temporal. Supports the following formats:
     *
     * <ul>
     *  <li>2000</li>
     *  <li>2000-01</li>
     *  <li>2000-01-01</li>
     *  <li>-2000</li>
     *  <li>-2000-01</li>
     *  <li>-2000-01-01</li>
     * </ul>
     *
     * @param dateString the date as string to parse
     * @return a temporal
     */
    public static Temporal parse(String dateString) {
        try {
            if (dateString.length() >= 4 && dateString.length() <= 5) {
                return Year.parse(dateString);
            } else if (dateString.length() >= 7 && dateString.length() <= 8) {
                return YearMonth.parse(dateString);
            }
            return LocalDate.parse(dateString);
        } catch (DateTimeParseException exc) {
            throw new MCRException("Unable to parse " + dateString, exc);
        }
    }

    /**
     * Converts a temporal to a string. Supports the following ChronoFields:
     *
     * <ul>
     *  <li>YEAR</li>
     *  <li>MONTH_OF_YEAR</li>
     *  <li>DAY_OF_MONTH</li>
     * </ul>
     *
     * The resulting string is in the form of YYYY, YYYY-MM or YYYY-MM-DD.
     *
     * @param temporal the temporal to format
     * @return the temporal as string
     */
    public static String format(Temporal temporal) {
        String dateAsString = String.format("%04d", temporal.get(ChronoField.YEAR));
        if (temporal.isSupported(ChronoField.MONTH_OF_YEAR)) {
            dateAsString += "-" + String.format("%02d", temporal.get(ChronoField.MONTH_OF_YEAR));
            if (temporal.isSupported(ChronoField.DAY_OF_MONTH)) {
                dateAsString += "-" + String.format("%02d", temporal.get(ChronoField.DAY_OF_MONTH));
            }
        }
        return dateAsString;
    }

    /**
     * Checks if the given temporal has a year part without month or day information. Something like 2000.
     * 
     * @param temporal the temporal to check
     * @return true if the temporal has a year but not a day and/or month chrono field
     */
    public static boolean isYear(Temporal temporal) {
        return temporal.isSupported(ChronoField.YEAR) &&
            !temporal.isSupported(ChronoField.MONTH_OF_YEAR) &&
            !temporal.isSupported(ChronoField.DAY_OF_MONTH);
    }

    /**
     * Checks if the given temporal has a year and a month part but the day is not available. Something like 2000-10.
     *
     * @param temporal the temporal to check
     * @return true if the temporal has a year and month chrono field but not a day
     */
    public static boolean isMonth(Temporal temporal) {
        return temporal.isSupported(ChronoField.YEAR) &&
            temporal.isSupported(ChronoField.MONTH_OF_YEAR) &&
            !temporal.isSupported(ChronoField.DAY_OF_MONTH);
    }

    /**
     * Checks if the given temporal has a year, a month and a day part. Something like 2000-10-15.
     *
     * @param temporal the temporal to check
     * @return true if the temporal has a year, month and day chrono field
     */
    public static boolean isDay(Temporal temporal) {
        return temporal.isSupported(ChronoField.YEAR) &&
            temporal.isSupported(ChronoField.MONTH_OF_YEAR) &&
            temporal.isSupported(ChronoField.DAY_OF_MONTH);
    }

    /**
     * Helper method to build a {@link LocalDate} out of a {@link Temporal}.
     * If the month or day are not present, they are set to 1.
     *
     * @param temporal the temporal accessor
     * @return a local date
     */
    public static LocalDate startOf(Temporal temporal) {
        if (!temporal.isSupported(ChronoField.YEAR)) {
            return null;
        }
        int year = temporal.get(ChronoField.YEAR);
        int month = temporal.isSupported(ChronoField.MONTH_OF_YEAR) ? temporal.get(ChronoField.MONTH_OF_YEAR) : 1;
        int day = temporal.isSupported(ChronoField.DAY_OF_MONTH) ? temporal.get(ChronoField.DAY_OF_MONTH) : 1;
        return LocalDate.of(year, month, day);
    }

    /**
     * Helper method to build a {@link LocalDate} out of a {@link Temporal}.
     * If the month is not present its set to 12. If the day is not present its set to the last day of the month.
     *
     * @param temporal the temporal accessor
     * @return a local date
     */
    public static LocalDate endOf(Temporal temporal) {
        if (!temporal.isSupported(ChronoField.YEAR)) {
            return null;
        }
        int year = temporal.get(ChronoField.YEAR);
        int month = temporal.isSupported(ChronoField.MONTH_OF_YEAR) ? temporal.get(ChronoField.MONTH_OF_YEAR) : 12;
        int day = temporal.isSupported(ChronoField.DAY_OF_MONTH) ? temporal.get(ChronoField.DAY_OF_MONTH)
            : YearMonth.of(year, month).atEndOfMonth().getDayOfMonth();
        return LocalDate.of(year, month, day);
    }

    /**
     * Returns the iso format of the given date.
     *
     * <ul>
     *  <li>yyyy</li>
     *  <li>MMMM yyyy</li>
     *  <li>dd MMMM yyyy</li>
     * </ul>
     *
     * @param date the date e.g. 2010-11
     * @return the format
     */
    public static String getSimpleFormat(String date) {
        if (date != null && !date.equals("")) {
            String split[] = date.split("-");
            switch (split.length) {
                case 1:
                    return MCRTranslation.translate("metaData.dateYear");
                case 2:
                    return MCRTranslation.translate("metaData.dateYearMonth");
                case 3:
                    return MCRTranslation.translate("metaData.dateYearMonthDay");
            }
        }
        return MCRTranslation.translate("metaData.date");
    }

    /**
     * Returns a "pretty" date string for the given isoDate.
     *
     * @param isoDate a string in the format of YYYY, YYYY-MM or YYYY-MM-DD
     * @param iso639Language the language
     * @return pretty date
     */
    private static String prettify(String isoDate, String iso639Language) {
        String format = getSimpleFormat(isoDate);
        try {
            return MCRXMLFunctions.formatISODate(isoDate, format, iso639Language);
        } catch (Exception exc) {
            LogManager.getLogger().error("Unable to format " + isoDate, exc);
            return isoDate;
        }
    }

    /**
     * Returns a "pretty" date string for the given temporal.
     *
     * @param temporal the temporal to format
     * @param iso639Language the language
     * @return pretty date
     */
    public static String prettify(Temporal temporal, String iso639Language) {
        String isoDate = format(temporal);
        return prettify(isoDate, iso639Language);
    }

    /**
     * Prettifies a JPMetaDate.
     *
     * @param metaDate the meta date
     * @param iso639Language the language
     * @return prettified date
     */
    public static String prettify(JPMetaDate metaDate, String iso639Language) {
        if (metaDate.getFrom() != null) {
            Locale locale = new Locale(iso639Language);
            String from = prettify(metaDate.getFrom(), iso639Language);
            if (metaDate.getUntil() != null) {
                String until = prettify(metaDate.getUntil(), iso639Language);
                String separator = MCRTranslation.translate("metaData.date.until", locale);
                return String.format("%s %s %s", from, separator, until);
            } else {
                String since = MCRTranslation.translate("metaData.date.since", locale);
                return String.format("%s %s", since, from);
            }
        } else {
            return prettify(metaDate.getDate(), iso639Language);
        }
    }

    /**
     * Compares two temporal.
     *
     * @param a the first temporal
     * @param b the second temporal
     * @param startOf Only used if the month or day information of a temporal does not exists.
     *                If set to true, this will use {@link #startOf(Temporal)} otherwise {@link #endOf(Temporal)}.
     * @return compare value 0 == equal
     */
    public static int compare(Temporal a, Temporal b, boolean startOf) {
        if (a == null && b == null) {
            return 0;
        }
        if (a == null) {
            return 1;
        }
        if (b == null) {
            return -1;
        }
        LocalDate aLocalDate = startOf ? JPDateUtil.startOf(a) : JPDateUtil.endOf(a);
        if (aLocalDate == null) {
            return -1;
        }
        LocalDate bLocalDate = startOf ? JPDateUtil.startOf(b) : JPDateUtil.endOf(b);
        if (bLocalDate == null) {
            return 1;
        }
        return aLocalDate.compareTo(bLocalDate);
    }

}
