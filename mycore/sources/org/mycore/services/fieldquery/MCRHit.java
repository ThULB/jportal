/*
 * $RCSfile: MCRHit.java,v $
 * $Revision: 1.20 $ $Date: 2006/12/08 14:21:37 $
 *
 * This file is part of ***  M y C o R e  ***
 * See http://www.mycore.de/ for details.
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
 * along with this program, in a file called gpl.txt or license.txt.
 * If not, write to the Free Software Foundation Inc.,
 * 59 Temple Place - Suite 330, Boston, MA  02111-1307 USA
 */

package org.mycore.services.fieldquery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.mycore.common.MCRException;

/**
 * Represents a single result hit of a query. The hit has an ID which is the
 * MCRObjectID of the document that matched the query. The hit may have
 * MCRFieldValue objects set for sorting data or representing hit metadata like
 * score or rank.
 * 
 * If the same hit (hit with same ID) is in different result sets A and B, the
 * data of the hit objects is merged. The hit sort data is copied from one of
 * the hits that contains sort data. There is only on sort data set for each
 * hit. The hit metadata of both hits is preserved and copied from both hits, so
 * there can be multiple metadata sets from different searches for the same hit.
 * 
 * @see MCRResults
 * @author Arne Seifert
 * @author Frank L�tzenkirchen
 */
public class MCRHit {
    /** logger */
    static Logger LOGGER = Logger.getLogger(MCRHit.class.getName());

    /** The ID of this object that matched the query */
    private String id;

    /** The unique key of this hit */
    private String key;

    /** Identifies a hit that comes from the local server */
    public final static String LOCAL = "local";

    /** The alias of the host where this hit comes from */
    private String host = LOCAL;

    /** List of MCRFieldValue objects that are hit metadata */
    private List<MCRFieldValue> metaData = new ArrayList<MCRFieldValue>();

    /** List of MCRFieldValue objects that are sort data */
    private List<MCRFieldValue> sortData = new ArrayList<MCRFieldValue>();

    /** Map from field to field value, used for sorting */
    private Map<MCRFieldDef,String> sortValues = new HashMap<MCRFieldDef,String>();

    /**
     * Creates a new result hit with the given object ID
     * 
     * @param id
     *            the ID of the object that matched the query
     */
    public MCRHit(String id) {
        this.id = id;
        this.key = id + "@" + LOCAL;
    }

    /**
     * Creates a new result hit with the given object ID
     * 
     * @param id
     *            the ID of the object that matched the query
     * @param hostAlias
     *            the remote host alias (may be null)
     */
    private MCRHit(String id, String hostAlias) {
        this.id = id;
        this.host = hostAlias;
        this.key = id + "@" + hostAlias;
    }

    /**
     * Returns the ID of the object that matched the query
     * 
     * @return the ID of the object that matched the query
     */
    public String getID() {
        return id;
    }

    /**
     * Returns the alias of the host where this hit comes from
     * 
     * @return the remote host alias, or MCRHit.LOCAL
     */
    public String getHost() {
        return host;
    }

    /**
     * Returns a combination of ID and host alias to be used as key
     * 
     * @return a unique key for this MCRHit
     */
    String getKey() {
        return key;
    }

    /**
     * Adds hit metadata like score or rank
     * 
     * @param value
     *            the value of the metadata field
     */
    public void addMetaData(MCRFieldValue value) {
        metaData.add(value);
    }

    /**
     * Adds data for sorting this hit
     * 
     * @param fieldValue
     *            the value of a sortable search field
     */
    public void addSortData(MCRFieldValue fieldValue) {
        String value = fieldValue.getValue();
        MCRFieldDef field = fieldValue.getField();

        // If field is repeated (multiple values for same field):
        // for text fields, combine all values for sorting
        // for dates and numbers, use only first value for sorting

        if (!sortValues.containsKey(field)) {
            sortData.add(fieldValue);
            sortValues.put(field, value);
        } else if ("text name identifier".indexOf(field.getDataType()) >= 0) {
            String oldValue = sortValues.get(field);
            String newValue = oldValue.concat(" ").concat(value);
            sortData.add(fieldValue);
            sortValues.put(field, newValue);
        }
    }

    /** Returns true if this MCRHit has any sort data added */
    boolean hasSortData() {
        return !sortData.isEmpty();
    }

    /**
     * Compares this hit with another hit by comparing the value of the given
     * search field. Used for sorting results.
     * 
     * @param field
     *            the field to compare
     * @param other
     *            the other hit to compare with
     * @return 0 if the two hits are equal, a positive value if this hit is
     *         "greater" than the other, a negative value if this hit is
     *         "smaller" than the other
     * 
     * @see MCRResults#sortBy(List)
     */
    int compareTo(MCRFieldDef field, MCRHit other) {
        String va = this.sortValues.get(field);
        String vb = other.sortValues.get(field);

        if ((va == null) || (va.trim().length() == 0)) {
            return (((vb == null) || (vb.trim().length() == 0)) ? 0 : (-1));
        } else if ((vb == null) || (vb.trim().length() == 0)) {
            return (((va == null) || (va.trim().length() == 0)) ? 0 : 1);
        } else if ("decimal".equals(field.getDataType())) {
            return (int) ((Double.parseDouble(va) - Double.parseDouble(vb)) * 10.0);
        } else if ("integer".equals(field.getDataType())) {
            return (int) (Long.parseLong(va) - Long.parseLong(vb));
        } else {
            return va.compareTo(vb);
        }
    }

    /**
     * Merges the data of a MCRHit object with the same ID to the data of this
     * MCRHit object. The sort data and meta data of the other hit is added to
     * this object's data.
     * 
     * @param other
     *            the other hit
     */
    void merge(MCRHit other) {
        // Copy other hit sort data
        if (this.sortData.isEmpty() && !other.sortData.isEmpty()) {
            this.sortData.addAll(other.sortData);
            this.sortValues.putAll(other.sortValues);
        }

        // Copy other hit meta data
        if (other.metaData.size() > 0) {
            this.metaData.add(null); // used as a delimiter
            this.metaData.addAll(other.metaData);
        }
    }

    /**
     * Creates a XML representation of this hit and its sort data and meta data
     * 
     * @return a 'hit' element with attribute 'id', optionally one 'sortData'
     *         child element and multiple 'metaData' child elements
     */
    public Element buildXML() {
        Element eHit = new Element("hit", MCRFieldDef.mcrns);
        eHit.setAttribute("id", this.id);
        eHit.setAttribute("host", this.host);

        if (!sortData.isEmpty()) {
            Element eSort = new Element("sortData", MCRFieldDef.mcrns);
            eHit.addContent(eSort);

            for (int i = 0; i < sortData.size(); i++) {
                MCRFieldValue fv = sortData.get(i);
                eSort.addContent(fv.buildXML());
            }
        }

        Element eMeta = null;
        int count = 0;

        for (int i = 0; i < metaData.size(); i++) {
            MCRFieldValue fv = metaData.get(i);
            if ((i == 0) || (fv == null)) {
                if ((eMeta != null) && (count == 0))
                    continue;

                eMeta = new Element("metaData", MCRFieldDef.mcrns);
                eHit.addContent(eMeta);
                count = 0;
                if (i > 0)
                    continue;
            }

            eMeta.addContent(fv.buildXML());
            count++;
        }

        return eHit;
    }

    /**
     * Parses a XML representation of a hit and its sort data and meta data
     * 
     * @param xml
     *            the XML element
     * @param hostAlias
     *            the remote host alias
     * @return the parsed MCRHit object
     */
    static MCRHit parseXML(Element xml, String hostAlias) {
        String id = xml.getAttributeValue("id", "");
        if (id.length() == 0)
            throw new MCRException("MCRHit id attribute is empty");

        MCRHit hit = new MCRHit(id, hostAlias);

        Element eSort = xml.getChild("sortData", MCRFieldDef.mcrns);
        if (eSort != null) {
            List children = eSort.getChildren();
            for (Iterator it = children.iterator(); it.hasNext();) {
                Element child = (Element) (it.next());
                hit.addSortData(MCRFieldValue.parseXML(child));
            }
        }

        List metaList = xml.getChildren("metaData");
        for (Iterator itm = metaList.iterator(); itm.hasNext();) {
            Element md = (Element) (itm.next());
            List children = md.getChildren();

            for (Iterator it = children.iterator(); it.hasNext();) {
                Element child = (Element) (it.next());
                hit.addMetaData(MCRFieldValue.parseXML(child));
            }
            if (itm.hasNext())
                hit.metaData.add(null);
        }

        return hit;
    }

    /*
     * Builds a string representation of this hit for debugging.
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("---- MCRHit ----");
        sb.append("\nID       = ").append(id);
        sb.append("\nHost     = ").append(host);
        for (int i = 0; i < metaData.size(); i++) {
            sb.append("\nMetaData[" + i + "] = ");
            if (metaData.get(i) == null)
                sb.append("-----");
            else
                sb.append(metaData.get(i));
        }
        for (int i = 0; i < sortData.size(); i++) {
            sb.append("\nSortData[" + i + "] = ").append(sortData.get(i));
        }
        sb.append("\n----------------\n");
        return sb.toString();
    }
}
