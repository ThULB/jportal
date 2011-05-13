/**
 * 
 */
package fsu.jportal.laws.frontend.mets;

/**
 * @author shermann
 */
public class DFGTypeProvider {

    /**
     * @param key
     *            the lookup key
     * @return the value for the given key, or "section" if there is no value
     *         for the given key
     */
    public static String getDFGType(String key) {
        if (key.equals("T")) {
            return ("title_page");
        }
        return "section";
    }
}
