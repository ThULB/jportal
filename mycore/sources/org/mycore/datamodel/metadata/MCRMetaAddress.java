/*
 * 
 * $Revision: 13085 $ $Date: 2008-02-06 18:27:24 +0100 (Mi, 06 Feb 2008) $
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

import org.jdom.Namespace;
import org.mycore.common.MCRException;

/**
 * This class implements all methods for handling with the MCRMetaAddress part
 * of a metadata object. The MCRMetaAddress class represents a natural address
 * specified by a list of names.
 * 
 * @author J. Vogler
 * @version $Revision: 13085 $ $Date: 2008-02-06 18:27:24 +0100 (Mi, 06 Feb 2008) $
 */
final public class MCRMetaAddress extends MCRMetaDefault {
    // MetaAddress data
    private String country;

    private String state;

    private String zipcode;

    private String city;

    private String street;

    private String number;

    /**
     * This is the constructor. <br>
     * The language element was set to <b>en </b>. All other elemnts are set to
     * an empty string.
     */
    public MCRMetaAddress() {
        super();
        country = "";
        state = "";
        zipcode = "";
        city = "";
        street = "";
        number = "";
    }

    /**
     * This is the constructor. <br>
     * The language element was set. If the value of <em>default_lang</em> is
     * null, empty or false <b>en </b> was set. The subtag element was set to
     * the value of <em>set_subtag<em>. If the value of <em>set_subtag</em>
     * is null or empty an exception was throwed. The type element was set to
     * the value of <em>set_type<em>, if it is null, an empty string was set
     * to the type element. The country, state, zipcode, city, street and
     * number element was set to the value of <em>set_...<em>, if they are null,
     * an empty string was set to this element.
     *
     * @param set_datapart    the global part of the elements like 'metadata'
     *                        or 'service'
     * @param set_subtag      the name of the subtag
     * @param default_lang    the default language
     * @param set_type        the optional type string
     * @param set_inherted    a value >= 0
     * @param set_country     the country name
     * @param set_state       the state name
     * @param set_zipcode     the zipcode string
     * @param set_city        the city name
     * @param set_street      the street name
     * @param set_number      the number string
     * @exception MCRException if the parameter values are invalid
     */
    public MCRMetaAddress(String set_datapart, String set_subtag, String default_lang, String set_type, int set_inherted, String set_country, String set_state, String set_zipcode, String set_city, String set_street, String set_number) throws MCRException {
        super(set_datapart, set_subtag, default_lang, set_type, set_inherted);
        country = "";
        state = "";
        zipcode = "";
        city = "";
        street = "";
        number = "";

        if (set_country != null) {
            country = set_country;
        }

        if (set_state != null) {
            state = set_state;
        }

        if (set_zipcode != null) {
            zipcode = set_zipcode;
        }

        if (set_city != null) {
            city = set_city;
        }

        if (set_street != null) {
            street = set_street;
        }

        if (set_number != null) {
            number = set_number;
        }
    }

    /**
     * This methode set all address componets.
     * 
     * @param set_country
     *            the country name
     * @param set_state
     *            the state name
     * @param set_zipcode
     *            the zipcode string
     * @param set_city
     *            the city name
     * @param set_street
     *            the street name
     * @param set_number
     *            the number string
     */
    public final void set(String set_country, String set_state, String set_zipcode, String set_city, String set_street, String set_number) {
        if ((set_country == null) || (set_state == null) || (set_zipcode == null) || (set_city == null) || (set_street == null) || (set_number == null)) {
            throw new MCRException("One parameter is null.");
        }

        country = set_country;
        state = set_state;
        zipcode = set_zipcode;
        city = set_city;
        street = set_street;
        number = set_number;
    }

    /**
     * This method get the country text element.
     * 
     * @return the country
     */
    public final String getCountry() {
        return country;
    }

    /**
     * This method get the state text element.
     * 
     * @return the state
     */
    public final String getState() {
        return state;
    }

    /**
     * This method get the zipcode text element.
     * 
     * @return the zipcode
     */
    public final String getZipcode() {
        return zipcode;
    }

    /**
     * This method get the city text element.
     * 
     * @return the city
     */
    public final String getCity() {
        return city;
    }

    /**
     * This method get the street text element.
     * 
     * @return the street
     */
    public final String getStreet() {
        return street;
    }

    /**
     * This method get the number text element.
     * 
     * @return the number
     */
    public final String getNumber() {
        return number;
    }

    /**
     * This method reads the XML input stream part from a DOM part for the
     * metadata of the document.
     * 
     * @param element
     *            a relevant JDOM element for the metadata
     */
    public final void setFromDOM(org.jdom.Element element) {
        super.setFromDOM(element);
        country = element.getChildTextTrim("country");

        if (country == null) {
            country = "";
        }

        state = element.getChildTextTrim("state");

        if (state == null) {
            state = "";
        }

        zipcode = element.getChildTextTrim("zipcode");

        if (zipcode == null) {
            zipcode = "";
        }

        city = element.getChildTextTrim("city");

        if (city == null) {
            city = "";
        }

        street = element.getChildTextTrim("street");

        if (street == null) {
            street = "";
        }

        number = element.getChildTextTrim("number");

        if (number == null) {
            number = "";
        }
    }

    /**
     * This method creates a XML stream for all data in this class, defined by
     * the MyCoRe XML MCRMetaAddress definition for the given subtag.
     * 
     * @exception MCRException
     *                if the content of this class is not valid
     * @return a JDOM Element with the XML MCRMetaAddress part
     */
    public final org.jdom.Element createXML() throws MCRException {
        if (!isValid()) {
            throw new MCRException("The content of MCRMetaAddress is not valid.");
        }

        org.jdom.Element elm = new org.jdom.Element(subtag);
        elm.setAttribute("lang", lang, Namespace.XML_NAMESPACE);
        elm.setAttribute("inherited", Integer.toString(inherited));

        if ((type != null) && ((type = type.trim()).length() != 0)) {
            elm.setAttribute("type", type);
        }

        if ((country = country.trim()).length() != 0) {
            elm.addContent(new org.jdom.Element("country").addContent(country));
        }

        if ((state = state.trim()).length() != 0) {
            elm.addContent(new org.jdom.Element("state").addContent(state));
        }

        if ((zipcode = zipcode.trim()).length() != 0) {
            elm.addContent(new org.jdom.Element("zipcode").addContent(zipcode));
        }

        if ((city = city.trim()).length() != 0) {
            elm.addContent(new org.jdom.Element("city").addContent(city));
        }

        if ((street = street.trim()).length() != 0) {
            elm.addContent(new org.jdom.Element("street").addContent(street));
        }

        if ((number = number.trim()).length() != 0) {
            elm.addContent(new org.jdom.Element("number").addContent(number));
        }

        return elm;
    }

    /**
     * This method checks the validation of the content of this class. The
     * method returns <em>false</em> if
     * <ul>
     * <li>the country is empty and
     * <li>the state is empty and
     * <li>the zipcode is empty and
     * <li>the city is empty and
     * <li>the street is empty and
     * <li>the number is empty
     * </ul>
     * otherwise the method returns <em>true</em>.
     * 
     * @return a boolean value
     */
    public final boolean isValid() {
        if (((country = country.trim()).length() == 0) && ((state = state.trim()).length() == 0) && ((zipcode = zipcode.trim()).length() == 0) && ((city = city.trim()).length() == 0) && ((street = street.trim()).length() == 0) && ((number = number.trim()).length() == 0)) {
            return false;
        }

        return true;
    }

    /**
     * This method make a clone of this class.
     */
    public Object clone() {
        return new MCRMetaAddress(datapart, subtag, DEFAULT_LANGUAGE, type, inherited, country, state, zipcode, city, street, number);
    }
}
