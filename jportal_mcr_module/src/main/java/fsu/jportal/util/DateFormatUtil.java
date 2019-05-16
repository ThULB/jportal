package fsu.jportal.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;

public class DateFormatUtil {
    private final Locale locale;
    private final HashMap<String, Date> cache = new HashMap<>();

    public DateFormatUtil(Locale local) {
        this.locale = local;
    }

    public Function<String, String> format(String fromPattern, String toPattern) {
        return dateStr -> Optional.ofNullable(cache.computeIfAbsent(dateStr, k -> parse(k, fromPattern)))
                .map(createFormater(toPattern)::format)
                .orElse("ParseExp: " + dateStr);
    }

    private Date parse(String dateStr, String pattern) {
        try {
            return createFormater(pattern).parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    private SimpleDateFormat createFormater(String pattern) {
        return new SimpleDateFormat(pattern, locale);
    }
}
