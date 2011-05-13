/**
 * 
 */
package fsu.jportal.laws.frontend.mets;

/**
 * @author shermann
 */
public class LabelProvider {

    public static final String getLabel(String key) {

        if (key.equals("T"))
            return "Titelblatt";

        if (key.equals("CR"))
            return "Chronologisches Repertorium";

        if (key.equals("AR"))
            return "Alphabetisches Repertorium";

        if (key.startsWith("GSN"))
            return "Gesetznummer " + key.substring("GSN".length());

        if (key.equals("DSF"))
            return "Druck- und Schreibfehler";

        if (key.equals("ABK"))
            return "Abkürzungen";

        if (key.equals("ALLGR"))
            return "Allgemeines Repertorium";

        if (key.equals("TAR"))
            return "Tarife";

        if (key.equals("BKM"))
            return "Bekanntmachung";

        if (key.equals("RGL"))
            return "Regulativ";

        if (key.equals("BLG"))
            return "Beilage";

        if (key.equals("CI"))
            return "Chronologisches Inhaltsverzeichnis";

        if (key.equals("AI"))
            return "Aphabetisches Inhaltsverzeichnis";

        if (key.equals("BR"))
            return "Berichtigung";

        if (key.equals("HZA"))
            return "Herzogtum Altenburg";

        if (key.equals("HZSA"))
            return "Herzogtum Sachsen-Altenburg";

        if (key.equals("SA"))
            return "Sachsen-Altenburgische Gesetzsammlung";

        if (key.equals("AAI"))
            return "Allgemeines alphabetisches Inhaltsverzeichnis zur Gesetzsammlung";

        if (key.equals("AAR"))
            return "Allgemeines alphabetisches Repertorium der Gesetzsammlung";

        if (key.equals("WW"))
            return "Wegweiser durch die Gesetzsammlung";

        if (key.equals("VBM"))
            return "Vorbemerkungen";

        if (key.equals("VW"))
            return "Vorwort";

        return null;
    }
}
