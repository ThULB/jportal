package fsu.jportal.backend.marc;

import java.util.Optional;

public abstract class Marc21Util {

    /**
     * Returns 245|a of the full title.
     *
     * @param title the title
     * @return the 245|a part of the title
     */
    public static String getMaintitle(String title) {
        return substring(substring(title, " : "), " / ");
    }

    /**
     * Returns 245|b of the full title (everything after " : ").
     *
     * @param title the title
     * @return the remainder title
     */
    public static Optional<String> getRemainderTitle(String title) {
        if (title.contains(" : ")) {
            return Optional.of(substring(title.substring(title.indexOf(" : ") + 3), " / "));
        }
        return Optional.empty();
    }

    /**
     * Returns 245|c of the full title (everything after " / ").
     *
     * @param title the title
     * @return the statement of responsibility
     */
    public static Optional<String> getStatementOfResponsibility(String title) {
        if (title.contains(" / ")) {
            return Optional.of(substring(title.substring(title.indexOf(" / ") + 3), " : "));
        }
        return Optional.empty();
    }

    private static String substring(String string, String match) {
        return string.substring(0, endIndex(string, match));
    }

    private static int endIndex(String string, String match) {
        int index = string.indexOf(match);
        index = index < 0 ? string.length() : index;
        return index;
    }


}
