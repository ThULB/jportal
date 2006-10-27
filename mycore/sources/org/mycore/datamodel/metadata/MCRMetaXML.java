/*
 * $RCSfile: MCRMetaXML.java,v $
 * $Revision: 1.10 $ $Date: 2005/09/28 07:40:25 $
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

package org.mycore.datamodel.metadata;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Namespace;
import org.mycore.common.MCRException;

/**
 * This class implements all method for handling with the MCRMetaLangText part
 * of a metadata object. The MCRMetaLangText class present a single item, which
 * has triples of a text and his corresponding language and optional a type.
 * 
 * @author Jens Kupferschmidt
 * @author Johannes B�hler
 * @version $Revision: 1.10 $ $Date: 2005/09/28 07:40:25 $
 */
public class MCRMetaXML extends MCRMetaDefault implements MCRMetaInterface {
    // XML data list
    protected ArrayList xmllist = null;

    /**
     * This is the constructor. <br>
     * Set the java.util.ArrayList of child elements to new.
     */
    public MCRMetaXML() {
        super();
        xmllist = new ArrayList();
    }

    /**
     * This is the constructor. <br>
     * The language element was set. If the value of <em>default_lang</em> is
     * null, empty or false <b>en </b> was set. The subtag element was set to
     * the value of <em>set_subtag<em>. If the value of <em>set_subtag</em>
     * is null or empty an exception was throwed. The type element was set to
     * the value of <em>set_type<em>, if it is null, an empty string was set
     * to the type element. The xml element was set to the value of
     * <em>set_xml<em>.
     *
     * @param set_datapart     the global part of the elements like 'metadata'
     *                         or 'service'
     * @param set_subtag       the name of the subtag
     * @param default_lang     the default language
     * @param set_type         the optional type string
     * @param set_inherted     a value >= 0
     * @param set_xml          a java.util.ArrayList of org.jdom.Element
     * @exception MCRException if the set_subtag value is null or empty
     */
    public MCRMetaXML(String set_datapart, String set_subtag, String default_lang, String set_type, int set_inherted, ArrayList set_xml) throws MCRException {
        super(set_datapart, set_subtag, default_lang, set_type, set_inherted);

        if (set_xml != null) {
            xmllist = set_xml;
        }
    }

    /**
     * This method set the java.util.List of org.jdom.Element
     * 
     * @param set_xml
     *            the java.util.ArrayList of org.jdom.Element
     */
    public final void set(ArrayList set_xml) {
        if (set_xml != null) {
            xmllist = set_xml;
        }
    }

    /**
     * This method get the ArrayList of org.jdom.Element.
     * 
     * @return the ArrayList of org.jdom.Element
     */
    public final ArrayList get() {
        return xmllist;
    }

    /**
     * This method add a org.jdom.Element to the java.util.ArrayList.
     * 
     * @param set_xml
     *            the XML stream as org.jdom.Element
     */
    public final void addElement(org.jdom.Element set_xml) {
        if (set_xml != null) {
            xmllist.add(set_xml.detach());
        }
    }

    /**
     * This method get org.jdom.Element with index i from the ArrayList.
     * 
     * @return the XML as org.jdom.Element
     */
    public final org.jdom.Element getElement(int i) {
        if ((i < 0) || (i > xmllist.size())) {
            return null;
        }

        return ((org.jdom.Element) xmllist.get(i));
    }

    /**
     * This method read the XML input stream part from a DOM part for the
     * metadata of the document.
     * 
     * @param element
     *            a relevant JDOM element for the metadata
     */
    public void setFromDOM(org.jdom.Element element) {
        super.setFromDOM(element);

        List temp = element.getChildren();

        if (temp == null) {
            return;
        }

        for (int i = 0; i < temp.size(); i++) {
            xmllist.add(((org.jdom.Element) temp.get(i)).detach());
        }
    }

    /**
     * This method create a XML stream for all data in this class, defined by
     * the MyCoRe XML MCRMetaLangText definition for the given subtag.
     * 
     * @exception MCRException
     *                if the content of this class is not valid
     * @return a JDOM Element with the XML MCRMetaLangText part
     */
    public org.jdom.Element createXML() throws MCRException {
        if (!isValid()) {
            debug();
            throw new MCRException("The content of MCRMetaXML is not valid.");
        }

        org.jdom.Element elm = new org.jdom.Element(subtag);
        elm.setAttribute("lang", lang, Namespace.XML_NAMESPACE);
        elm.setAttribute("inherited", (new Integer(inherited)).toString());

        if ((type != null) && ((type = type.trim()).length() != 0)) {
            elm.setAttribute("type", type);
        }

        for (int i = 0; i < xmllist.size(); i++) {
            elm.addContent(((org.jdom.Element) xmllist.get(i)).detach());
        }

        return elm;
    }

    /**
     * This methode create an empty String for all cases
     * 
     * @param textsearch
     *            true if the data should text searchable
     * @exception MCRException
     *                if the content of this class is not valid
     * @return a String with the text value
     */
    public String createTextSearch(boolean textsearch) throws MCRException {
        return "";
    }

    /**
     * This method check the validation of the content of this class. The method
     * returns <em>true</em> if
     * <ul>
     * <li>the subtag is not null or empty
     * <li>the text is not null or empty
     * </ul>
     * otherwise the method return <em>false</em>
     * 
     * @return a boolean value
     */
    public boolean isValid() {
        if (!super.isValid()) {
            return false;
        }

        if (xmllist == null) {
            return false;
        }

        return true;
    }

    /**
     * This method make a clone of this class.
     */
    public Object clone() {
        MCRMetaXML out = null;

        try {
            out = (MCRMetaXML) super.clone();
        } catch (CloneNotSupportedException e) {
            LOGGER.warn(new StringBuffer(MCRMetaXML.class.getName()).append(" could not be cloned."), e);

            return null;
        }

        out.xmllist = (ArrayList) xmllist.clone();

        return out;
    }

    /**
     * This method put debug data to the logger (for the debug mode).
     */
    public void debug() {
        LOGGER.debug("Start Class : MCRMetaXML");
        super.debugDefault();
        LOGGER.debug("ArrayList size()   = \n" + xmllist.size());
    }
}
