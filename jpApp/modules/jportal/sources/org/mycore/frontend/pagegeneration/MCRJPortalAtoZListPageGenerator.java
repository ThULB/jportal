package org.mycore.frontend.pagegeneration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Text;
import org.jdom.filter.ElementFilter;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;
import org.mycore.common.MCRConfiguration;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.parsers.bool.MCRAndCondition;
import org.mycore.parsers.bool.MCRCondition;
import org.mycore.services.fieldquery.MCRFieldDef;
import org.mycore.services.fieldquery.MCRHit;
import org.mycore.services.fieldquery.MCRQuery;
import org.mycore.services.fieldquery.MCRQueryCondition;
import org.mycore.services.fieldquery.MCRQueryManager;
import org.mycore.services.fieldquery.MCRResults;
import org.mycore.services.fieldquery.MCRSortBy;

/**
 * Main class to create, load, save or manipulate the AtoZ list.
 * @author Matthias Eichner
 */
public class MCRJPortalAtoZListPageGenerator {

    private static final Logger LOGGER = Logger.getLogger(MCRJPortalAtoZListPageGenerator.class);
    private static final String FS = System.getProperty("file.seperator", "/");
    private static final String WEBAPPS_DIR = MCRConfiguration.instance().getString("MCR.webappsDir", "build"+FS+"webapps");
    private static final String JOURNAL_XML = WEBAPPS_DIR + FS + "content" + FS + "main" + FS + "journalList.xml";

    private Document doc;
    private Element journalListElement;

    public Document getJournalList() {
        return doc;
    }

    public boolean journalListExists() {
        return new File(JOURNAL_XML).exists();
    }

    /**
     * Creates the AtoZ list as an element. The structure of the element is:
     * <p>
     * &lt;journalList&gt;<br/>
     *   &lt;section name="A"&gt;<br/>
     *      &lt;journal&gt;id&lt;/journal&gt;<br/>
     *      ...<br/>
     *   &lt;/section&gt;<br/>
     *   ...
     * </p>
     */
    public void createJournalList() {
        // create the new root node
        journalListElement = new Element("journalList");
        journalListElement.setAttribute("mode", "alphabetical");
        doc = new Document(journalListElement);
        // search qry
        MCRFieldDef def1 = MCRFieldDef.getDef("objectType");
        MCRCondition cond1 = new MCRQueryCondition(def1, "=", "jpjournal");
        MCRFieldDef def2 = MCRFieldDef.getDef("deletedFlag");
        MCRCondition cond2 = new MCRQueryCondition(def2, "=", "false");
        // sortBy the maintitle of the journal
        MCRSortBy sortBy = new MCRSortBy(MCRFieldDef.getDef("maintitles"), MCRSortBy.ASCENDING);
        List<MCRSortBy> sortByList = new ArrayList<MCRSortBy>();
        sortByList.add(sortBy);
        MCRQuery qry = new MCRQuery(new MCRAndCondition(cond1, cond2), sortByList, 0);
        // do search
        MCRResults results = MCRQueryManager.search(qry);

        // go through all journals
        for(int i = 0; i < results.getNumHits(); i++) {
            MCRHit hit = results.getHit(i);
            MCRObject mcrObj = new MCRObject();
            try {
                mcrObj.receiveFromDatastore(hit.getID());
                // add the journal to the tree
                addJournalToEnd(mcrObj);
            } catch(Exception exc) {
                LOGGER.error(exc);
            }
        }
    }

    /**
     * Loads the journalList from the build/webapps folder.
     * @throws FileNotFoundException
     * @throws IOException
     * @throws JDOMException
     */
    public Document loadJournalList() throws FileNotFoundException, IOException, JDOMException {
        File file = new File(JOURNAL_XML);
        if (!file.exists()) {
            LOGGER.error("Couldnt find journalList.xml.");
            throw new FileNotFoundException(file.getAbsolutePath());
        }
        SAXBuilder builder = new SAXBuilder();
        doc = builder.build(file);
        journalListElement = doc.getRootElement();
        return doc;
    }

    /**
     * Saves the journalList as an xml file to the build/webapps folder.
     * @throws FileNotFoundException
     * @throws IOException
     */
    public synchronized void saveJournalList() throws FileNotFoundException, IOException {
        // save document
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        File journalListFile = new File(JOURNAL_XML);
        FileOutputStream output = new FileOutputStream(journalListFile, false);
        outputter.output(doc, output);
    }

    /**
     * This method adds a journal to the journalList. The journal will append at the
     * end of its section.
     * @throws JDOMException
     */
    public void addJournalToEnd(MCRObject mcrObj) throws JDOMException {
        String title = getTitle(mcrObj);
        String firstChar = title.substring(0, 1).toUpperCase();
        Element section = getSection(firstChar);
        addJournalToEndOfSection(section, title, mcrObj.getId().getId());
    }

    /**
     * Inserts the journal in the journalList. Shifts the journals at that
     * position and any following to the right.
     * @throws JDOMException
     */
    public void insertJournal(MCRObject mcrObj) throws JDOMException {
        String title = getTitle(mcrObj);
        String firstChar = title.substring(0, 1).toUpperCase();
        Element section = getSection(firstChar);
        insertJournalToSection(section, title, mcrObj.getId().getId());
    }

    /**
     * Removes a journal from the journalList.
     * @throws JDOMException
     */
    public void removeJournal(MCRObject mcrObj) throws JDOMException {
        String title = getTitle(mcrObj);
        String firstChar = title.substring(0, 1).toUpperCase();
        Element section = getSection(firstChar);
        removeJournalFromSection(section, mcrObj.getId().getId());
    }

    /**
     * Returns the section by a name in a journalList.
     * @param sectionName the name of the section
     * @return the section with the specified section name. if no section was found
     * a new will be created.
     */
    private Element getSection(String sectionName) {
        List sections = journalListElement.getChildren("section");
        for(Object o : sections) {
            if(!(o instanceof Element))
                continue;
            Element section = (Element)o;
            if(section.getAttributeValue("name").equals(sectionName))
                return section;
        }
        return createSection(sectionName);
    }

    /**
     * Creates a new section in the journalList.
     * @param sectionName the name of the section
     * @return a new section as jdom element
     */
    private Element createSection(String sectionName) {
        Element section = new Element("section");
        section.setAttribute("name", sectionName);
        journalListElement.addContent(section);
        return section;
    }

    /**
     * Adds a journal to the end of a section.
     * @param section at which section the journal will be added
     * @param title the title of the journal
     * @param id the id of the journal
     */
    private void addJournalToEndOfSection(Element section, String title, String id) {
        if(isJournalInSection(section, id))
            return;
        // add the journal id to the xml file
        Element journal = new Element("journal");
        journal.setAttribute("title", title);
        journal.setText(id);
        section.addContent(journal);
    }

    /**
     * Inserts a journal in a section. Shifts the journals at that
     * position and any following to the right.
     * @param section at which section the journal will be added
     * @param title the title of the journal
     * @param id the id of the journal
     */
    private void insertJournalToSection(Element section, String title, String id) {
        if(isJournalInSection(section, id))
            return;
        // remove journals temporarily
        List journals = section.removeContent(new ElementFilter("journal"));
        boolean added = false;
        // go through all journals and add them
        for(Object o : journals) {
            if(!(o instanceof Element))
                continue;
            Element journal = (Element)o;
            if(added == false && journal.getAttributeValue("title").compareToIgnoreCase(title)  > 0) {
                addJournalToEndOfSection(section, title, id);
                added = true;
            }
            section.addContent(journal);
        }
        if(added == false) {
            addJournalToEndOfSection(section, title, id);
        }
    }

    /**
     * Removes the specified journal from the section.
     * @param section where the journal has to be removed
     * @param id the id of the journal
     * @return the removed journal. if no journal removed null will be returned.
     */
    private Element removeJournalFromSection(Element section, String id) {
        List journals = section.getChildren("journal");
        // go through all journals
        for(Object o : journals) {
            if(!(o instanceof Element))
                continue;
            Element journal = (Element)o;
            if(journal.getText().equals(id)) {
                section.removeContent(journal);
                return journal;
            }
        }
        return null;
    }

    /**
     * Checks if the journal id is already set in the specified section.
     * @param section the section to check
     * @param id the id to test
     * @return if the journal is in the section return true, otherwise false
     */
    private boolean isJournalInSection(Element section, String id) {
        List journals = section.getChildren("journal");
        for(Object o : journals) {
            if(!(o instanceof Element))
                continue;
            Element journal = (Element)o;
            if(journal.getText().equals(id)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the maintitle of an journal.
     * @param obj the journal as an mcr object
     * @return the title of the mcr object
     * @throws JDOMException
     */
    private String getTitle(MCRObject obj) throws JDOMException {
        Element elem = obj.getMetadata().createXML();
        String title = ((Text)XPath.selectSingleNode(elem, "maintitles/maintitle/text()")).getText();
        return title;
    }
}
