/*
 * 
 * $Revision: 13491 $ $Date: 2008-05-07 14:44:55 +0200 (Mi, 07 Mai 2008) $
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

import org.mycore.common.MCRException;

/**
 * This class implements all method for handling with the MCRMetaClassification
 * part of a metadata object. The MCRMetaClassification class present a link to
 * a category of a classification.
 * <p>
 * &lt;tag class="MCRMetaClassification" heritable="..."&gt; <br>
 * &lt;subtag classid="..." categid="..." /&gt; <br>
 * &lt;/tag&gt; <br>
 * 
 * @author Jens Kupferschmidt
 * @version $Revision: 13491 $ $Date: 2008-03-18 22:53:44 +0000 (Di, 18 Mrz
 *          2008) $
 */
public class MCRMetaClassification extends MCRMetaDefault {
    /** The length of the classification ID * */
    public static final int MAX_CLASSID_LENGTH = MCRObjectID.MAX_LENGTH;

    public static final int MAX_CATEGID_LENGTH = 128;

    // MCRMetaClassification data
    protected String classid;

    protected String categid;

    /**
     * This is the constructor. <br>
     * The language element was set to <b>en </b>. The classid and categid value
     * was set to an empty string.
     */
    public MCRMetaClassification() {
        super();
        classid = "";
        categid = "";
    }

    /**
     * This is the constructor. <br>
     * The language element was set to <b>en </b>. The subtag element was set to
     * the value of <em>set_subtag<em>. If the
     * value of <em>set_subtag</em> is null or empty an exception was throwed.
     * The type element was set to an empty string.
     * the <em>set_classid</em> and the <em>categid</em> must be not null
     * or empty!
     *
     * @param set_datapart     the global part of the elements like 'metadata'
     *                         or 'service'
     * @param set_subtag       the name of the subtag
     * @param set_inherted     a value >= 0
     * @param set_type         the type attribute
     * @param set_classid      the classification ID
     * @param set_categid      the category ID
     * @exception MCRException if the set_subtag value, the set_classid value or
     * the set_categid are null, empty, too long or not a MCRObjectID
     */
    public MCRMetaClassification(String set_datapart, String set_subtag, int set_inherted, String set_type, String set_classid, String set_categid)
            throws MCRException {
        super(set_datapart, set_subtag, "en", set_type, set_inherted);
        setValue(set_classid, set_categid);
    }

    /**
     * The method return the classification ID.
     * 
     * @return the classId
     */
    public final String getClassId() {
        return classid;
    }

    /**
     * The method return the category ID.
     * 
     * @return the categId
     */
    public final String getCategId() {
        return categid;
    }

    /**
     * This method set values of classid and categid.
     * 
     * @param set_classid
     *            the classification ID
     * @param set_categid
     *            the category ID
     * @exception MCRException
     *                if the set_classid value or the set_categid are null,
     *                empty, too long or not a MCRObjectID
     */
    public final void setValue(String set_classid, String set_categid) throws MCRException {
        if ((set_classid == null) || ((set_classid = set_classid.trim()).length() == 0)) {
            throw new MCRException("The classid is empty.");
        }

        if ((set_categid == null) || ((set_categid = set_categid.trim()).length() == 0)) {
            throw new MCRException("The categid is empty.");
        }

        if (set_classid.length() > MAX_CLASSID_LENGTH) {
            throw new MCRException("The classid is too long.");
        }

        try {
            MCRObjectID mid = new MCRObjectID(set_classid);
            classid = mid.getId();
        } catch (Exception e) {
            throw new MCRException("The classid is not MCRObjectID.");
        }

        if (set_categid.length() > MAX_CATEGID_LENGTH) {
            throw new MCRException("The categid is too long.");
        }

        categid = set_categid;
    }

    /**
     * This method read the XML input stream part from a DOM part for the
     * metadata of the document.
     * 
     * @param element
     *            a relevant JDOM element for the metadata
     * @exception MCRException
     *                if the set_classid value or the set_categid are null,
     *                empty, too long or not a MCRObjectID
     */
    public void setFromDOM(org.jdom.Element element) throws MCRException {
        super.setFromDOM(element);

        String set_classid = element.getAttributeValue("classid");
        String set_categid = element.getAttributeValue("categid");
        setValue(set_classid, set_categid);
    }

    /**
     * This method create a XML stream for all data in this class, defined by
     * the MyCoRe XML MCRMetaClassification definition for the given subtag.
     * 
     * @exception MCRException
     *                if the content of this class is not valid
     * @return a JDOM Element with the XML MCRClassification part
     */
    public org.jdom.Element createXML() throws MCRException {
        if (!isValid()) {
            throw new MCRException("The content of MCRMetaClassification in subtag " + subtag + " is not valid.");
        }

        org.jdom.Element elm = new org.jdom.Element(subtag);

        if ((type != null) && ((type = type.trim()).length() != 0)) {
            elm.setAttribute("type", type);
        }

        elm.setAttribute("inherited", Integer.toString(inherited));
        elm.setAttribute("classid", classid);
        elm.setAttribute("categid", categid);

        return elm;
    }

    /**
     * This method check the validation of the content of this class. The method
     * returns <em>true</em> if
     * <ul>
     * <li>the subtag is not null or empty
     * </ul>
     * otherwise the method return <em>false</em>
     * 
     * @return a boolean value
     */
    public boolean isValid() {
        if (!super.isValid()) {
            return false;
        }
        return classid != null && classid.length() > 0 && categid != null && categid.length() > 0;
    }

    /**
     * This method make a clone of this class.
     */
    public Object clone() {
        return new MCRMetaClassification(datapart, subtag, inherited, type, classid, categid);
    }

    /**
     * This method put debug data to the logger (for the debug mode).
     */
    public void debug() {
        LOGGER.debug("Start Class : MCRMetaClassification");
        super.debugDefault();
        LOGGER.debug("ClassID            = " + classid);
        LOGGER.debug("CategID            = " + categid);
    }
}
