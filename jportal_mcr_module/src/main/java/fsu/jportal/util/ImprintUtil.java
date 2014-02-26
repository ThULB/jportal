package fsu.jportal.util;

import fsu.jportal.pref.JournalConfig;

public abstract class ImprintUtil {

    /**
     * Returns the imprint of the given object id or throws a 404 not
     * found web application exception.
     * 
     * @param objID mycore object id
     * @return id of imprint
     */
    public static String getImprintID(String objID, String fsType) {
        return getJournalConf(objID).getKey(fsType);
    }

    /**
     * Checks if the given object id is assigned to an imprint.
     * 
     * @param objID mycore object id to check
     * @return true if an imprint is assigned, otherwise false
     */
    public static boolean has(String objID, String fsType) {
        return getImprintID(objID, fsType) != null;
    }

    public static JournalConfig getJournalConf(String objID) {
        return new JournalConfig(objID, "imprint.partner");
    }
}
