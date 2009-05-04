/*
 * $RCSfile: MCRClassificationBrowserData.java,v $
 * $Revision: 15054 $ $Date: 2009-03-31 15:24:46 +0200 (Di, 31. Mär 2009) $
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

package org.mycore.datamodel.classifications;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.xpath.XPath;
import org.mycore.access.MCRAccessManager;
import org.mycore.common.MCRConfiguration;
import org.mycore.common.MCRSession;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.events.MCRSessionEvent;
import org.mycore.common.events.MCRSessionListener;
import org.mycore.datamodel.classifications2.MCRCategLinkServiceFactory;
import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.classifications2.MCRLabel;

/**
 * Instances of MCRClassificationBrowser contain the data of the currently
 * displayed navigation tree. MCRClassificationBrowser uses one
 * MCRClassificationBrowserData instance per browser session to store and update
 * the category lines to be displayed.
 * 
 * @author Anja Schaar
 * 
 */
public class MCRClassificationBrowserData {

    protected String pageName;

    protected String xslStyle;

    protected String uri;

    private static MCRConfiguration config;

    private static final Logger LOGGER = Logger.getLogger(MCRClassificationBrowserData.class);

    private ArrayList<Element> lines;

    private MCRCategory classif;

    private String startPath = "";

    private String actItemID = "";

    private String lastItemID = "";

    private String[] categFields;

    private String emptyLeafs = null;

    private String view = null;

    private boolean comments = false;

    private String searchField = "";

    private boolean sort = false;

    private String objectType = null;

    private String[] objectTypeArray = null;

    private String restriction = null;

    int maxlevel = 0;

    int totalNumOfDocs = 0;

    public static Map<String, String> ClassUserTable = new Hashtable<String, String>();

    private static MCRSessionListener ClassUserTableCleaner = new MCRSessionListener() {

        public void sessionEvent(MCRSessionEvent event) {
            switch (event.getType()) {
            case destroyed:
                clearUserClassTable(event.getSession());
                break;
            default:
                LOGGER.debug("Skipping event: " + event.getType());
                break;
            }
        }

    };

    static {
        MCRSessionMgr.addSessionListener(ClassUserTableCleaner);
    }

    public MCRClassificationBrowserData(final String u, final String mode, final String actclid, final String actEditorCategid)
            throws Exception {
        uri = u;
        config = MCRConfiguration.instance();
        LOGGER.debug(" incomming Path " + uri);

        final String[] uriParts = uri.split("/"); // mySplit();
        LOGGER.info(" Start");
        String classifID = null;
        final String browserClass = (uriParts.length <= 1 ? "default" : uriParts[1]);
        LOGGER.debug(" PathParts - classification " + browserClass);
        LOGGER.debug(" Number of PathParts =" + uriParts.length);
        try {
            classifID = config.getString("MCR.ClassificationBrowser." + browserClass + ".Classification");
        } catch (final org.mycore.common.MCRConfigurationException noClass) {
            classifID = actclid;
        }
        try {
            pageName = config.getString("MCR.ClassificationBrowser." + browserClass + ".EmbeddingPage");
        } catch (final org.mycore.common.MCRConfigurationException noPagename) {
            pageName = config.getString("MCR.ClassificationBrowser.default.EmbeddingPage");
        }
        try {
            xslStyle = config.getString("MCR.ClassificationBrowser." + browserClass + ".Style");
        } catch (final org.mycore.common.MCRConfigurationException noStyle) {
            xslStyle = config.getString("MCR.ClassificationBrowser.default.Style");
        }
        try {
            searchField = config.getString("MCR.ClassificationBrowser." + browserClass + ".searchField");
        } catch (final org.mycore.common.MCRConfigurationException noSearchfield) {
            searchField = config.getString("MCR.ClassificationBrowser.default.searchField");
        }

        try {
            emptyLeafs = config.getString("MCR.ClassificationBrowser." + browserClass + ".EmptyLeafs");
        } catch (final org.mycore.common.MCRConfigurationException noEmptyLeafs) {
            emptyLeafs = config.getString("MCR.ClassificationBrowser.default.EmptyLeafs");
        }
        try {
            view = config.getString("MCR.ClassificationBrowser." + browserClass + ".View");
        } catch (final org.mycore.common.MCRConfigurationException noView) {
            view = config.getString("MCR.ClassificationBrowser.default.View");
        }
        setObjectTypes(browserClass);

        sort = config.getBoolean("MCR.ClassificationBrowser." + browserClass + ".Sort", false);
        comments = config.getBoolean("MCR.ClassificationBrowser." + browserClass + ".Comments", false);
        restriction = config.getString("MCR.ClassificationBrowser." + browserClass + ".Restriction", null);

        startPath = browserClass;

        if ("edit".equals(mode)) {
            pageName = config.getString("MCR.classeditor.EmbeddingPage");
            xslStyle = config.getString("MCR.classeditor.Style");
            sort = false;
            view = "tree";

            if (classifID.length() == 0) {
                return;
            }
        }

        if (emptyLeafs == null) {
            emptyLeafs = "yes";
        }
        if (view == null || !view.endsWith("flat")) {
            view = "tree";
        }
        LOGGER.info("uriParts length: " + uriParts.length);
        clearPath(uriParts);
        MCRCategoryID id = MCRCategoryID.rootID(classifID);
        setClassification(id);
        setActualPath(actEditorCategid);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(" SetClassification " + classifID);
            LOGGER.debug(" Empty nodes: " + emptyLeafs);
            LOGGER.debug(" View: " + view);
            LOGGER.debug(" Comment: " + comments);
            LOGGER.debug(" Doctypes: " + objectType);
            for (String element : objectTypeArray) {
                LOGGER.debug(" Type: " + element);
            }
            LOGGER.debug(" Restriction: " + restriction);
            LOGGER.debug(" Sort: " + sort);
        }
    }

    private void setObjectTypes(final String browserClass) {
        LOGGER.debug("setObjectTypes(" + browserClass + ")");
        try {
            // NOTE: read *.Doctype for compatiblity reasons
            objectType = config.getString("MCR.ClassificationBrowser." + browserClass + ".Objecttype", config.getString(
                    "MCR.ClassificationBrowser." + browserClass + ".Doctype", null));
        } catch (final org.mycore.common.MCRConfigurationException noDoctype) {
            objectType = config.getString("MCR.ClassificationBrowser.default.ObjectType", config
                    .getString("MCR.ClassificationBrowser.default.Doctype"));
        }

        if (objectType != null) {
            objectTypeArray = objectType.split(",");
        } else {
            objectTypeArray = new String[0];
        }
    }

    public String getUri() {
        return uri;
    }

    /**
     * Returns true if category comments for the classification currently
     * displayed should be shown.
     */
    public boolean showComments() {
        return comments;
    }

    /**
     * Returns the pageName for the classification
     */
    public String getPageName() {
        return pageName;
    }

    /**
     * Returns the xslStyle for the classification
     */
    public String getXslStyle() {
        return xslStyle;
    }

    public MCRCategory getClassification() {
        return classif;
    }

    @SuppressWarnings("unchecked")
    public org.jdom.Document loadTreeIntoSite(final org.jdom.Document cover, final org.jdom.Document browser) {
        final Element placeholder = cover.getRootElement().getChild("classificationBrowser");
        LOGGER.info(" Found Entry at " + placeholder);
        if (placeholder != null) {
            final List<Element> children = browser.getRootElement().getChildren();
            for (Element child : children) {
                placeholder.addContent((Element) child.clone());
            }
        }
        LOGGER.debug(cover);
        return cover;
    }

    public final void setClassification(final MCRCategoryID classifID) throws Exception {
        classif = getClassificationPool().getClassificationAsPojo(classifID, true);
        if (classif == null)
            return;
        lines = new ArrayList<Element>();
        totalNumOfDocs = 0;
        putCategoriesintoLines(-1, classif.getChildren());
        LOGGER.debug("Arraylist of CategItems initialized - Size " + lines.size());
    }

    private void clearPath(final String[] uriParts) throws Exception {
        final String[] cati = new String[uriParts.length];
        String path = "";
        if (uriParts.length == 1) {
            path = "/" + uriParts[0];
        } else {
            path = "/" + uriParts[1];
        }
        int len = 0;
        // pfad bereinigen
        for (int k = 2; k < uriParts.length; k++) {
            LOGGER.debug(" uriParts[k]=" + uriParts[k] + " k=" + k);
            if (uriParts[k].length() > 0) {
                if (uriParts[k].equalsIgnoreCase("..") && len > 0) {
                    len--;
                } else {
                    cati[len] = uriParts[k];
                    len++;
                }
            }
        }

        // remove double entries from path
        // (if an entry appears the 2nd time it will not be displayed -> so we
        // can remove it here)
        final ArrayList<String> result = new ArrayList<String>();
        for (int i = 0; i < len; i++) {
            final String x = cati[i];
            if (result.contains(x)) {
                result.remove(x);
            } else {
                result.add(x);
            }
        }

        // reinitialisieren
        categFields = new String[result.size()];
        int j = 0;
        for (String uriPart : result) {
            categFields[j] = uriPart;
            j++;
            path += "/" + uriPart;
        }
        this.uri = path;
    }

    private void setActualPath(final String actEditorCategid) throws Exception {
        actItemID = lastItemID = "";

        for (String categID : categFields) {
            update(categID);
            lastItemID = actItemID;
            actItemID = categID;
        }
        if (actEditorCategid != null) {
            actItemID = lastItemID = actEditorCategid;
        }
    }

    private Element setTreeline(MCRCategory categ) {
        Element categElement = MCRCategoryElementFactory.getCategoryElement(categ, false, 0);
        categElement.setAttribute("level", String.valueOf(categ.getLevel() + 1));

        if (categ.hasChildren()) {
            categElement.setAttribute("hasChildren", "T");
        } else {
            categElement.setAttribute("hasChildren", " ");
        }
        return categElement;
    }

    private Element getTreeline(final int i) {
        if (i >= lines.size()) {
            return null;
        }
        return lines.get(i);
    }

    private void putCategoriesintoLines(final int startpos, final List<MCRCategory> children) {
        LOGGER.debug("Start Explore Arraylist of CategItems  ");
        int i = startpos;
        List<MCRCategoryID> ids = new ArrayList<MCRCategoryID>();

        for (MCRCategory cat : children) {
            ids.add(cat.getId());
        }

        for (MCRCategory cat : children) {
            lines.add(++i, setTreeline(cat));
        }
        LOGGER.debug("End Explore - Arraylist of CategItems ");
    }

    public org.jdom.Document createXmlTreeforAllClassifications() throws Exception {
        LOGGER.debug("create XML tree for all classifications");
        final Element xDocument = new Element("classificationbrowse");
        final Element CreateClassButton = new Element("userCanCreate");
        if (MCRAccessManager.checkPermission("create-classification")) {
            CreateClassButton.addContent("true");
        } else {
            CreateClassButton.addContent("false");
        }

        xDocument.addContent(CreateClassButton);

        final Element xNavtree = new Element("classificationlist");
        xDocument.addContent(xNavtree);
        String browserClass = "";

        LOGGER.debug("query classification links");
        final Map<MCRCategoryID, Boolean> countMap = new HashMap<MCRCategoryID, Boolean>();
        for (MCRCategoryID classID : getClassificationPool().getAllIDs()) {
            MCRCategory classif = getClassificationPool().getClassificationAsPojo(classID, false);
            countMap.putAll(MCRCategLinkServiceFactory.getInstance().hasLinks(classif));
        }

        for (MCRCategoryID id : countMap.keySet()) {
            if (!id.isRootID())
                continue;
            LOGGER.debug("get classification " + id);
            MCRCategory classif = getClassificationPool().getClassificationAsPojo(id, false);
            LOGGER.debug("get browse element");
            Element cli = getBrowseElement(classif);
            LOGGER.debug("get browse element ... done");
            String sessionID = MCRSessionMgr.getCurrentSession().getID();
            // set browser type
            try {
                browserClass = config.getString("MCR.classeditor." + classif.getId().getRootID());
            } catch (final Exception ignore) {
                browserClass = "default";
            }
            // set permissions
            if (getClassificationPool().isEdited(id) == false) {
                if (MCRAccessManager.checkPermission(id.getRootID(), "writedb")) {
                    cli.setAttribute("userCanEdit", "true");
                } else {
                    cli.setAttribute("userCanEdit", "false");
                }
                if (MCRAccessManager.checkPermission(id.getRootID(), "deletedb")) {
                    cli.setAttribute("userCanDelete", "true");
                } else {
                    cli.setAttribute("userCanDelete", "false");
                }
            } else {
                cli.setAttribute("userCanEdit", "true");
                cli.setAttribute("userCanDelete", "true");
            }
            // set done flag
            if (ClassUserTable.containsKey(id.getRootID())) {
                if (ClassUserTable.get(id.getRootID()) != sessionID) {
                    MCRSession oldsession = MCRSessionMgr.getSession(ClassUserTable.get(id.getRootID()));
                    if (null != oldsession)
                        cli.setAttribute("userEdited", oldsession.getCurrentUserID());
                    else {
                        ClassUserTable.remove(id.getRootID());
                        cli.setAttribute("userEdited", "false");
                    }
                } else {
                    cli.setAttribute("userEdited", "false");
                }
            } else {
                cli.setAttribute("userEdited", "false");
            }

            if (getClassificationPool().isEdited(id)) {
                cli.setAttribute("edited", "true");
            } else {
                cli.setAttribute("edited", "false");
            }
            cli.setAttribute("browserClass", browserClass);
            setObjectTypes(browserClass);
            LOGGER.debug("counting linked objects");
            LOGGER.debug("counting linked objects ... done");
            cli.setAttribute("hasLinks", String.valueOf(countMap.get(id).booleanValue()));
            xNavtree.addContent(cli);
        }
        return new Document(xDocument);
    }

    private static Element getBrowseElement(MCRCategory classif) {
        Element ce = new Element("classification");
        ce.setAttribute("ID", classif.getId().getRootID());
        for (MCRLabel label : classif.getLabels()) {
            Element labelElement = new Element("label");
            if (label.getLang() != null) {
                labelElement.setAttribute("lang", label.getLang(), Namespace.XML_NAMESPACE);
            }
            if (label.getText() != null) {
                labelElement.setAttribute("text", label.getText());
            }
            if (label.getDescription() != null) {
                labelElement.setAttribute("description", label.getDescription());
            }
            ce.addContent(labelElement);
        }
        return ce;
    }

    /**
     * Creates an XML representation of MCRClassificationBrowserData
     * 
     * @author Anja Schaar
     * 
     */

    public org.jdom.Document createXmlTree(final String lang) throws Exception {

        LOGGER.debug("Show tree for classification:" + classif.getId());
        final MCRCategory cl = getClassificationPool().getClassificationAsPojo(classif.getId(), true);
        LOGGER.debug("Got classification");
        MCRClassificationPool cp = getClassificationPool();
        MCRLabel labels = getLabel(cl, lang);
        Element xDocument = new Element("classificationBrowse");

        final Element xID = new Element("classifID");
        xID.addContent(cl.getId().getRootID());
        xDocument.addContent(xID);

        final Element xUserEdited = new Element("userEdited");
        if (ClassUserTable.containsKey(cl.getId().getRootID())) {
            xUserEdited.addContent(MCRSessionMgr.getSession(ClassUserTable.get(cl.getId().getRootID())).getCurrentUserID());
        } else {
            xUserEdited.addContent("false");
        }
        xDocument.addContent(xUserEdited);

        final Element xSessionID = new Element("session");
        if (ClassUserTable.containsKey(cl.getId().getRootID())) {
            xSessionID.addContent(ClassUserTable.get(cl.getId().getRootID()));
        } else {
            xSessionID.addContent("");
        }

        xDocument.addContent(xSessionID);

        final Element xCurrentSessionID = new Element("currentSession");
        xCurrentSessionID.addContent(MCRSessionMgr.getCurrentSession().getID());
        xDocument.addContent(xCurrentSessionID);

        final Element xLabel = new Element("label");
        xLabel.addContent(labels.getText());
        xDocument.addContent(xLabel);

        final Element xDesc = new Element("description");
        xDesc.addContent(labels.getDescription());
        xDocument.addContent(xDesc);

        final Element xDocuments = new Element("cntDocuments");
        xDocuments.addContent(String.valueOf(totalNumOfDocs));
        xDocument.addContent(xDocuments);

        final Element xShowComments = new Element("showComments");
        xShowComments.addContent(String.valueOf(showComments()));
        xDocument.addContent(xShowComments);

        final Element xUri = new Element("uri");
        xUri.addContent(uri);
        xDocument.addContent(xUri);

        final Element xStartPath = new Element("startPath");
        xStartPath.addContent(startPath);
        xDocument.addContent(xStartPath);

        final Element xSearchField = new Element("searchField");
        xSearchField.addContent(searchField);
        xDocument.addContent(xSearchField);

        // add edit button if user has permission
        final Element CreateButton = new Element("userCanCreate");
        final Element EditButton = new Element("userCanEdit");
        final Element DeleteButton = new Element("userCanDelete");
        LOGGER.debug("now we check this right for the current user");
        // now we check this right for the current user
        if (cp.isEdited(getClassification().getId()) == false) {
            String permString = String.valueOf(MCRAccessManager.checkPermission("create-classification"));
            CreateButton.addContent(permString);
            xDocument.addContent(CreateButton);
            permString = String.valueOf(MCRAccessManager.checkPermission(cl.getId().getRootID(), "writedb"));
            EditButton.addContent(permString);
            xDocument.addContent(EditButton);
            permString = String.valueOf(MCRAccessManager.checkPermission(cl.getId().getRootID(), "deletedb"));
            DeleteButton.addContent(permString);
            xDocument.addContent(DeleteButton);
        } else {
            String permString = "true";
            CreateButton.addContent(permString);
            xDocument.addContent(CreateButton);
            EditButton.addContent(permString);
            xDocument.addContent(EditButton);
            DeleteButton.addContent(permString);
            xDocument.addContent(DeleteButton);
        }

        // data as XML from outputNavigationTree
        final Element xNavtree = new Element("navigationtree");
        xNavtree.setAttribute("classifID", cl.getId().getRootID());
        xNavtree.setAttribute("categID", actItemID);
        xNavtree.setAttribute("predecessor", lastItemID);
        xNavtree.setAttribute("emptyLeafs", emptyLeafs);
        xNavtree.setAttribute("view", view);
        final StringBuffer sb = new StringBuffer();
        if (objectTypeArray.length > 1) {
            sb.append("(");
        }
        for (int i = 0; i < objectTypeArray.length; i++) {
            sb.append("(objectType+=+").append(objectTypeArray[i]).append(")");
            if ((objectTypeArray.length > 1) && (i < objectTypeArray.length - 1)) {
                sb.append("+or+");
            }
        }
        if (objectTypeArray.length > 1) {
            sb.append(")");
        }
        xNavtree.setAttribute("doctype", sb.toString());
        xNavtree.setAttribute("restriction", restriction != null ? restriction : "");
        xNavtree.setAttribute("searchField", searchField);

        int i = 0;
        Element line;
        List<MCRCategoryID> ids = new ArrayList<MCRCategoryID>();
        LOGGER.debug("process tree lines: fetch ids");
        while ((line = getTreeline(i++)) != null) {
            final String catid = line.getAttributeValue("ID");
            ids.add(new MCRCategoryID(cl.getId().getRootID(), catid));
        }
        i = 0;
        LOGGER.debug("fetch Map<MCRCategoryID,Boolean>");
        Map<MCRCategoryID, Boolean> countMap = MCRCategLinkServiceFactory.getInstance().hasLinks(getClassification());
        LOGGER.debug("process tree lines: build xml tree");
        while ((line = getTreeline(i++)) != null) {

            final String catid = line.getAttributeValue("ID");
            boolean hasLinks = countMap.get(new MCRCategoryID(cl.getId().getRootID(), catid)).booleanValue();
            final String status = line.getAttributeValue("hasChildren");

            Element label = (Element) XPath.selectSingleNode(line, "label[lang('" + lang + "')]");
            if (label == null) {
                label = (Element) XPath.selectSingleNode(line, "label[lang('"
                        + MCRConfiguration.instance().getString("MCR.Metadata.DefaultLang", "en") + "')]");
            }
            if (label == null) {
                label = (Element) XPath.selectSingleNode(line, "label");
            }
            final String text = label.getAttributeValue("text");
            final String description = label.getAttributeValue("description");

            final int level = Integer.parseInt(line.getAttributeValue("level"));

            if (emptyLeafs.endsWith("no") && !hasLinks) {
                LOGGER.debug(" empty Leaf continue - " + emptyLeafs);
                continue;
            }
            final Element xRow = new Element("row");
            final Element xCol1 = new Element("col");
            final Element xCol2 = new Element("col");

            xRow.addContent(xCol1);
            xRow.addContent(xCol2);
            xNavtree.addContent(xRow);

            xCol1.setAttribute("lineLevel", String.valueOf(level - 1));
            xCol1.setAttribute("childpos", "middle");

            if (level > maxlevel) {
                xCol1.setAttribute("childpos", "first");
                maxlevel = level;
                if (getTreeline(i) == null) {
                    // Spezialfall nur genau ein Element
                    xCol1.setAttribute("childpos", "firstlast");
                }
            } else if (getTreeline(i) == null) {
                xCol1.setAttribute("childpos", "last");
            }

            xCol1.setAttribute("folder1", "folder_plain");
            xCol1.setAttribute("folder2", hasLinks ? "folder_closed_in_use" : "folder_closed_empty");

            if (status.equals("T")) {
                xCol1.setAttribute("plusminusbase", catid);
                xCol1.setAttribute("folder1", "folder_plus");
            } else if (status.equals("F")) {
                xCol1.setAttribute("plusminusbase", catid);
                xCol1.setAttribute("folder1", "folder_minus");
                xCol1.setAttribute("folder2", hasLinks ? "folder_open_in_use" : "folder_open_empty");
            }

            String search = uri;
            search += "/" + catid;

            if (search.indexOf("//") > 0)
                search = search.substring(0, search.indexOf("//")) + search.substring(search.indexOf("//") + 1);

            xCol2.setAttribute("searchbase", search);
            xCol2.setAttribute("lineID", catid);
            xCol2.setAttribute("hasLinks", String.valueOf(hasLinks));

            xCol2.addContent(text);

            if (showComments() && (description != null)) {
                final Element comment = new Element("comment");
                xCol2.addContent(comment);
                comment.setText(description);
            }
        }
        LOGGER.debug("Building XML document");

        xNavtree.setAttribute("rowcount", "" + i);
        xDocument.addContent(xNavtree);

        if ("true".equals(sort)) {
            xDocument = sortMyTree(xDocument);
        }
        Document doc = new org.jdom.Document(xDocument);
        //MCRUtils.writeJDOMToSysout(doc);
        return doc;
    }

    public void update(final String categID) throws Exception {
        int lastLevel = 0;
        boolean hideLevel = false;

        MCRCategory parent = MCRClassificationBrowserData.getClassificationPool().getClassificationAsPojo(classif.getId(), true);
        MCRCategory cat = findCategory(parent, categID);

        LOGGER.debug(" update CategoryTree for: " + categID);
        Element line;
        for (int i = 0; i < lines.size(); i++) {
            line = getTreeline(i);
            final String catid = line.getAttributeValue("ID");
            final String status = line.getAttributeValue("hasChildren");
            final int level = Integer.parseInt(line.getAttributeValue("level"));

            hideLevel = hideLevel && (level > lastLevel);
            LOGGER.debug(" compare CategoryTree on " + i + "_" + catid + " to " + categID);
            if (view.endsWith("tree")) {
                if (hideLevel) {
                    lines.remove(i--);
                } else if (categID.equals(catid)) {
                    if (status.equals("F")) // hide expanded category - //
                    // children
                    {
                        line.setAttribute("hasChildren", "T");
                        hideLevel = true;
                        lastLevel = level;
                    } else if (status.equals("T")) // expand category - //
                    // children
                    {
                        line.setAttribute("hasChildren", "F");
                        putCategoriesintoLines(i, cat.getChildren());
                    }
                }
            } else {
                if (categID.equalsIgnoreCase(catid)) {
                    line.setAttribute("level", "0");
                    LOGGER.info(" expand " + catid);
                    line.setAttribute("hasChildren", "F");
                    putCategoriesintoLines(i, cat.getChildren());
                } else {
                    LOGGER.debug(" remove lines " + i + "_" + catid);
                    lines.remove(i--);
                }
            }

        }
    }

    // don't use it works not really good

    private final Element sortMyTree(final Element xDocument) {
        Element xDoc = (Element) xDocument.clone();
        final Element navitree = ((Element) xDoc.getChild("navigationtree"));
        // separate
        ArrayList<String> itemname = new ArrayList<String>();
        ArrayList<Integer> itemlevel = new ArrayList<Integer>();
        ArrayList<Element> itemelm = new ArrayList<Element>();
        @SuppressWarnings("unchecked")
        List<Element> navitreelist = navitree.getChildren();
        for (int i = 0; i < navitreelist.size(); i++) {
            final Element child = navitreelist.get(i);
            final Element col1 = (Element) (child.getChildren().get(0));
            final Element col2 = (Element) (child.getChildren().get(1));
            final String sText = col2.getText();
            int level = 0;
            try {
                level = col1.getAttribute("lineLevel").getIntValue();
            } catch (final Exception ignored) {
            }
            itemname.add(sText);
            itemlevel.add(new Integer(level));
            itemelm.add((Element) child.clone());
            navitree.removeContent(child);
            i--;
        }
        int[] itemnum = new int[itemname.size()];
        for (int i = 0; i < itemname.size(); i++) {
            itemnum[i] = i;
        }
        // sort
        sortMyTreePerLevel(0, itemname.size(), 1, itemname, itemlevel, itemnum);
        // write back
        for (int i = 0; i < itemnum.length; i++) {
            navitree.addContent(itemelm.get(itemnum[i]));
        }
        return xDoc;
    }

    private final void sortMyTreePerLevel(int von, int bis, int level, ArrayList<String> itemname, ArrayList<Integer> itemlevel,
            int[] itemnum) {
        if (von == bis)
            return;
        // System.out.println("$$$>" + von + " " + bis);
        for (int i = von; i < bis - 1; i++) {
            // System.out.println("III>" + i + " " + itemname.get(itemnum[i]));
            if (itemlevel.get(itemnum[i]) != level) {
                // System.out.println("%%%> inner sort");
                int start = i;
                int stop = start;
                while (stop + 1 < bis && itemlevel.get(itemnum[stop + 1]).intValue() > level) {
                    stop++;
                }
                // System.out.println("--------------------");
                sortMyTreePerLevel(start, stop + 1, level + 2, itemname, itemlevel, itemnum);
                // System.out.println("--------------------");
                i = stop + 1;
                if (i >= bis)
                    continue;
            }
            String aktitem = itemname.get(itemnum[i]);
            for (int j = i + 1; j < bis; j++) {
                if (level != itemlevel.get(itemnum[j]).intValue())
                    continue;
                // System.out.println("%%%>" + i + " " + aktitem + " " + j + " "
                // + itemname.get(itemnum[j]) + " " + level + " " +
                // itemlevel.get(itemnum[j]).intValue());
                if (aktitem.compareTo(itemname.get(itemnum[j])) > 0) {
                    // must switch
                    // System.out.println("%%%> swap " + i + " with " + j);
                    int[] swap = itemnum.clone();
                    itemnum[i] = itemnum[j];
                    int ii = i;
                    for (int jj = j; jj < bis; jj++) {
                        // System.out.println("* " + ii + " " + jj);
                        itemnum[ii] = swap[jj];
                        ii++;
                    }
                    for (int jj = i; jj < j; jj++) {
                        // System.out.println("# " + ii + " " + jj);
                        itemnum[ii] = swap[jj];
                        ii++;
                    }
                    j = i;
                    aktitem = itemname.get(itemnum[i]);
                }
            }
        }
    }

    /**
     * @return Returns the pool.
     */
    public static MCRClassificationPool getClassificationPool() {
        MCRSession session = MCRSessionMgr.getCurrentSession();
        Object cp = session.get("MCRClassificationPool.instance");
        if (cp != null && cp instanceof MCRClassificationPool) {
            return (MCRClassificationPool) cp;
        }
        MCRClassificationPool classPool = new MCRClassificationPool();
        session.put("MCRClassificationPool.instance", classPool);
        return classPool;
    }

    private static MCRLabel getLabel(MCRCategory co, String lang) {
        for (MCRLabel label : co.getLabels()) {
            if (label.getLang().equals(lang)) {
                return label;
            }
        }
        return new MCRLabel();
    }

    public static void clearUserClassTable(MCRSession session) {
        final String curSessionID = session.getID();
        final Iterator<Map.Entry<String, String>> it = ClassUserTable.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> entry = it.next();
            if (entry.getValue().equals(curSessionID)) {
                LOGGER.info("Release classification " + entry.getKey() + " lock.");
                it.remove();
            }
        }
    }

    private MCRCategory findCategory(MCRCategory parent, String id) {
        MCRCategory found = null;
        for (MCRCategory cat : parent.getChildren()) {
            if (cat.getId().getID().equals(id)) {
                found = cat;
                LOGGER.debug("Found Category: " + found.getId().getID());
                break;
            }
            MCRCategory rFound = findCategory(cat, id);
            if (rFound != null) {
                found = rFound;
                break;
            }
        }

        return found;
    }

    private static class MCRCategoryElementFactory {
        static Element getCategoryElement(MCRCategory category, boolean withCounter, int numberObjects) {
            Element ce = new Element("category");
            Collection<MCRLabel> labels = category.getLabels();
            ce.setAttribute("ID", category.getId().getID());
            if (withCounter) {
                ce.setAttribute("counter", Integer.toString(numberObjects));
            }

            for (MCRLabel label : labels) {
                ce.addContent(getElement(label));
            }
            for (MCRCategory child : category.getChildren()) {
                ce.addContent(getCategoryElement(child, withCounter, numberObjects));
            }
            return ce;
        }

        private static Element getElement(MCRLabel label) {
            Element le = new Element("label");
            if (stringNotEmpty(label.getLang())) {
                le.setAttribute("lang", label.getLang(), Namespace.XML_NAMESPACE);
            }
            if (stringNotEmpty(label.getText())) {
                le.setAttribute("text", label.getText());
            }
            if (stringNotEmpty(label.getDescription())) {
                le.setAttribute("description", label.getDescription());
            }
            return le;
        }

        private static boolean stringNotEmpty(String test) {
            if (test != null && test.length() > 0) {
                return true;
            }
            return false;
        }
    }

}
