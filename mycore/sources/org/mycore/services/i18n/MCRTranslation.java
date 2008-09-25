/**
 * 
 * $Revision: 13770 $ $Date: 2008-07-28 13:11:00 +0200 (Mo, 28 Jul 2008) $
 *
 * This file is part of ** M y C o R e **
 * Visit our homepage at http://www.mycore.de/ for details.
 *
 * This program is free software; you can use it, redistribute it
 * and / or modify it under the terms of the GNU General Public License
 * (GPL) as published by the Free Software Foundation; either version 2
 * of the License or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program, normally in the file license.txt.
 * If not, write to the Free Software Foundation Inc.,
 * 59 Temple Place - Suite 330, Boston, MA  02111-1307 USA
 *
 **/
package org.mycore.services.i18n;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import org.mycore.common.MCRSessionMgr;

/**
 * provides services for internationalization in mycore application.
 * 
 * You have to provide a property file named messages.properties in your
 * classpath for this class to work.
 * 
 * @author Radi Radichev
 * @author Thomas Scheffler (yagee)
 */
public class MCRTranslation {

    private static final String DEPRECATED_MESSAGES_PROPERTIES = "/deprecated-messages.properties";

    private static final Logger LOGGER = Logger.getLogger(MCRTranslation.class);

    private static final Pattern ARRAY_DETECTOR = Pattern.compile(";");

    private static boolean DEPRECATED_MESSAGES_PRESENT = false;

    private static Properties DEPRECATED_MAPPING = loadProperties();

    /**
     * provides translation for the given label (property key).
     * 
     * The current locale that is needed for translation is gathered by the
     * language of the current MCRSession.
     * 
     * @param label
     * @return translated String
     */
    public static String translate(String label) {
        String result = null;
        Locale currentLocale = getCurrentLocale();
        LOGGER.debug("Translation for current locale: " + currentLocale.getLanguage());
        ResourceBundle message = ResourceBundle.getBundle("messages", currentLocale);

        try {
            result = message.getString(label);
            LOGGER.debug("Translation for " + label + "=" + result);
        } catch (java.util.MissingResourceException mre) {
            // try to get new key if 'label' is deprecated
            if (!DEPRECATED_MESSAGES_PRESENT) {
                LOGGER.warn("Could not load resource '" + DEPRECATED_MESSAGES_PROPERTIES + "' to check for depreacted I18N keys.");
            } else if (DEPRECATED_MAPPING.keySet().contains(label)) {
                String newLabel = DEPRECATED_MAPPING.getProperty(label);
                try {
                    result = message.getString(newLabel);
                } catch (java.util.MissingResourceException e) {
                }
                if (result != null) {
                    LOGGER.warn("Usage of deprected I18N key '" + label + "'. Please use '" + newLabel + "' instead.");
                    return result;
                }
            }
            result = "???" + label + "???";
            LOGGER.debug(mre.getMessage());
        }

        return result;
    }

    /**
     * provides translation for the given label (property key).
     * 
     * The current locale that is needed for translation is gathered by the
     * language of the current MCRSession.
     * 
     * @param label
     * @param arguments
     *            Objects that are inserted instead of placeholders in the
     *            property values
     * @return translated String
     */
    public static String translate(String label, Object[] arguments) {
        Locale currentLocale = getCurrentLocale();
        MessageFormat formatter = new MessageFormat(translate(label), currentLocale);
        String result = formatter.format(arguments);
        LOGGER.debug("Translation for " + label + "=" + result);
        return result;
    }

    /**
     * provides translation for the given label (property key).
     * 
     * The current locale that is needed for translation is gathered by the
     * language of the current MCRSession. Be aware that any occurence of ';'
     * and '\' in <code>argument</code> has to be masked by '\'. You can use
     * ';' to build an array of arguments: "foo;bar" would result in
     * {"foo","bar"} (the array)
     * 
     * @param label
     * @param argument
     *            String that is inserted instead of placeholders in the
     *            property values
     * @return translated String
     * @see #translate(String, Object[])
     */
    public static String translate(String label, String argument) {
        return translate(label, getStringArray(argument));
    }

    private static Locale getCurrentLocale() {
        return new Locale(MCRSessionMgr.getCurrentSession().getCurrentLanguage());
    }

    static String[] getStringArray(String masked) {
        List<String> a = new LinkedList<String>();
        boolean mask = false;
        StringBuffer buf = new StringBuffer();
        if (masked == null) {
            return new String[0];
        }
        if (!isArray(masked)) {
            a.add(masked);
        } else {
            for (int i = 0; i < masked.length(); i++) {
                switch (masked.charAt(i)) {
                case ';':
                    if (mask) {
                        buf.append(';');
                        mask = false;
                    } else {
                        a.add(buf.toString());
                        buf.setLength(0);
                    }
                    break;
                case '\\':
                    if (mask) {
                        buf.append('\\');
                        mask = false;
                    } else {
                        mask = true;
                    }
                    break;
                default:
                    buf.append(masked.charAt(i));
                    break;
                }
            }
            a.add(buf.toString());
        }
        return (String[]) a.toArray(new String[a.size()]);
    }

    static boolean isArray(String masked) {
        Matcher m = ARRAY_DETECTOR.matcher(masked);
        while (m.find()) {
            int pos = m.start();
            int count = 0;
            for (int i = pos - 1; i > 0; i--) {
                if (masked.charAt(i) == '\\')
                    count++;
                else
                    break;
            }
            if (count % 2 == 0) {
                return true;
            }
        }
        return false;
    }

    static Properties loadProperties() {
        Properties deprecatedMapping = new Properties();
        try {
            final InputStream propertiesStream = MCRTranslation.class.getResourceAsStream(DEPRECATED_MESSAGES_PROPERTIES);
            if (propertiesStream == null) {
                LOGGER.warn("Could not find resource '" + DEPRECATED_MESSAGES_PROPERTIES + "'.");
                return deprecatedMapping;
            }
            deprecatedMapping.load(propertiesStream);
            DEPRECATED_MESSAGES_PRESENT = true;
        } catch (IOException e) {
            LOGGER.warn("Could not load resource '" + DEPRECATED_MESSAGES_PROPERTIES + "'.", e);
        }
        return deprecatedMapping;
    }

}