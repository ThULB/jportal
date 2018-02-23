package org.mycore.datamodel.metadata;

import java.time.temporal.Temporal;

import com.google.gson.JsonObject;
import fsu.jportal.util.JPDateUtil;
import org.apache.logging.log4j.LogManager;
import org.jdom2.Element;
import org.mycore.common.MCRException;

/**
 * Date metadata class of jportal. Supports a single date (a year, a month, or a day) or a range (from -> until)
 * and a description.
 *
 * @author Matthias Eichner
 */
public class JPMetaDate extends MCRMetaDefault implements Comparable<JPMetaDate> {

    protected Temporal date, from, until = null;

    protected String description = null;

    public JPMetaDate() {
        super();
    }

    public JPMetaDate(String subtag, String type, int inherited) {
        super(subtag, null, type, inherited);
    }

    @Override
    public MCRMetaInterface clone() {
        return null;
    }

    @Override
    public void setFromDOM(Element element) throws MCRException {
        super.setFromDOM(element);
        String dateString = element.getAttributeValue("date");
        if (dateString != null) {
            this.date = JPDateUtil.parse(dateString);
        } else {
            String fromString = element.getAttributeValue("from");
            if (fromString == null) {
                LogManager.getLogger()
                          .warn("Unable to parse either 'date' or 'from' attribute of " + element.getName());
                return;
            }
            this.from = JPDateUtil.parse(fromString);
            String untilString = element.getAttributeValue("until");
            if (untilString != null) {
                this.until = JPDateUtil.parse(untilString);
            }
        }
        this.description = element.getText();
    }

    @Override
    public Element createXML() throws MCRException {
        Element element = super.createXML();
        if (this.date != null) {
            element.setAttribute("date", JPDateUtil.format(this.date));
        } else {
            if (this.from == null) {
                throw new MCRException("Unable to createXML either 'date' or 'from' has to be set.");
            }
            element.setAttribute("from", JPDateUtil.format(this.from));
            if (this.until != null) {
                element.setAttribute("until", JPDateUtil.format(this.until));
            }
        }
        if (this.description != null && !"".equals(this.description)) {
            element.setText(this.description);
        }
        return element;
    }

    /**
     * Creates the JSON representation. Extends the {@link MCRMetaDefault#createJSON()} method
     * with the following data.
     *
     * <pre>
     *   {
     *     date: "2016-02-08",
     *     from: "2017",
     *     until: "2018",
     *     description: "between 2017 and 2018"
     *   }
     * </pre>
     */
    @Override
    public JsonObject createJSON() {
        JsonObject obj = super.createJSON();
        if (this.date != null) {
            obj.addProperty("date", JPDateUtil.format(this.date));
        } else {
            if (this.from == null) {
                throw new MCRException("Unable to createXML either 'date' or 'from' has to be set.");
            }
            obj.addProperty("from", JPDateUtil.format(this.from));
            if (this.until != null) {
                obj.addProperty("until", JPDateUtil.format(this.until));
            }
        }
        if (this.description != null && "".equals(this.description)) {
            obj.addProperty("description", this.description);
        }
        return obj;
    }

    public Temporal getDate() {
        return this.date;
    }

    public Temporal getFrom() {
        return this.from;
    }

    public Temporal getUntil() {
        return this.until;
    }

    public void setDate(Temporal date) {
        this.date = date;
    }

    public void setFrom(Temporal from) {
        this.from = from;
    }

    public void setUntil(Temporal until) {
        this.until = until;
    }

    public Temporal getDateOrFrom() {
        return this.date != null ? this.date : this.from;
    }

    @Override
    public String toString() {
        if (this.date != null) {
            return JPDateUtil.format(this.date);
        }
        String dateAsString = JPDateUtil.format(this.from) + " -";
        if (this.until != null) {
            dateAsString += " " + JPDateUtil.format(this.until);
        }
        return dateAsString;
    }

    @Override
    public int compareTo(JPMetaDate other) {
        if (other == null) {
            return -1;
        }
        int compare = JPDateUtil.compare(this.getDateOrFrom(), other.getDateOrFrom(), true);
        if (compare != 0) {
            return compare;
        }
        return JPDateUtil.compare(this.getUntil(), other.getUntil(), false);
    }

}
