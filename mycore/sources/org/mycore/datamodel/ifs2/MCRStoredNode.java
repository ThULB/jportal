/*
 * $Revision: 15002 $ 
 * $Date: 2009-03-25 09:36:28 +0100 (Mi, 25. Mär 2009) $
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

package org.mycore.datamodel.ifs2;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.Selectors;
import org.apache.commons.vfs.VFS;
import org.jdom.Element;
import org.jdom.Namespace;
import org.mycore.common.MCRConfiguration;
import org.mycore.common.MCRSessionMgr;

/**
 * A file or directory really stored by importing it from outside the system.
 * Can be modified, updated and deleted, in contrast to virtual nodes.
 * 
 * @author Frank L�tzenkirchen
 */
public abstract class MCRStoredNode extends MCRNode {

    /**
     * Any additional data of this node that is not stored in the file object
     */
    protected Element data;

    /**
     * Returns a stored node instance that already exists
     * 
     * @param parent
     *            the parent directory containing this node
     * @param fo
     *            the file object in local filesystem representing this node
     * @param data
     *            the additional data of this node that is not stored in the
     *            file object
     */
    protected MCRStoredNode(MCRDirectory parent, FileObject fo, Element data) throws Exception {
        super(parent, fo);
        this.data = data;
    }

    /**
     * Creates a new stored node
     * 
     * @param parent
     *            the parent directory
     * @param name
     *            the name of the node
     * @param type
     *            the node type, dir or file
     */
    protected MCRStoredNode(MCRDirectory parent, String name, String type) throws Exception {
        super(parent, VFS.getManager().resolveFile(parent.fo, name));
        data = new Element(type);
        data.setAttribute("name", name);
        parent.data.addContent(data);
    }

    /**
     * Deletes this node with all its data and children
     */
    public void delete() throws Exception {
        data.detach();
        fo.delete(Selectors.SELECT_ALL);
        getRoot().saveAdditionalData();
    }

    /**
     * Renames this node.
     * 
     * @param name
     *            the new file name
     */
    public void renameTo(String name) throws Exception {
        FileObject fNew = VFS.getManager().resolveFile(fo.getParent(), name);
        fo.moveTo(fNew);
        fo = fNew;
        fo.getContent().setLastModifiedTime(System.currentTimeMillis());
        data.setAttribute("name", name);
        getRoot().saveAdditionalData();
    }

    /**
     * Sets last modification time of this file to a custom value.
     * 
     * @param time
     *            the time to be stored as last modification time
     */
    public void setLastModified(Date time) throws Exception {
        fo.getContent().setLastModifiedTime(time.getTime());
    }

    /**
     * Sets a label for this node
     * 
     * @param lang
     *            the xml:lang language ID
     * @param label
     *            the label in this language
     */
    public void setLabel(String lang, String label) throws Exception {
        Element found = null;
        for (Element child : (List<Element>) (data.getChildren("label")))
            if (lang.equals(child.getAttributeValue("lang", Namespace.XML_NAMESPACE))) {
                found = child;
                break;
            }

        if (found == null) {
            found = new Element("label").setAttribute("lang", lang, Namespace.XML_NAMESPACE);
            data.addContent(found);
        }
        found.setText(label);
        getRoot().saveAdditionalData();
    }

    /**
     * Removes all labels set
     */
    public void clearLabels() throws Exception {
        data.removeChildren("label");
        getRoot().saveAdditionalData();
    }

    /**
     * Returns a map of all labels, sorted by xml:lang, Key is xml:lang, value
     * is the label for that language.
     */
    public Map<String, String> getLabels() {
        Map<String, String> labels = new TreeMap<String, String>();
        for (Element label : (List<Element>) (data.getChildren("label")))
            labels.put(label.getAttributeValue("lang", Namespace.XML_NAMESPACE), label.getText());
        return labels;
    }

    /**
     * Returns the label in the given language
     * 
     * @param lang
     *            the xml:lang language ID
     * @return the label, or null if there is no label for that language
     */
    public String getLabel(String lang) {
        for (Element label : (List<Element>) (data.getChildren("label")))
            if (lang.equals(label.getAttributeValue("lang", Namespace.XML_NAMESPACE)))
                return label.getText();
        return null;
    }

    /**
     * Returns the label in the current language, otherwise in default language,
     * otherwise the first label defined, if any at all.
     * 
     * @return the label
     */
    public String getCurrentLabel() {
        String currentLang = MCRSessionMgr.getCurrentSession().getCurrentLanguage();
        String label = getLabel(currentLang);
        if (label != null)
            return label;

        String defaultLang = MCRConfiguration.instance().getString("MCR.Metadata.DefaultLang", "en");
        label = getLabel(defaultLang);
        if (label != null)
            return label;

        return data.getChildText("label");
    }

    /**
     * Repairs additional metadata of this node
     */
    abstract void repairMetadata() throws Exception;
}
