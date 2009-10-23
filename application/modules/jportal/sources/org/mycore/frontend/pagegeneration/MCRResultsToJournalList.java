package org.mycore.frontend.pagegeneration;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Text;
import org.jdom.xpath.XPath;
import org.mycore.common.MCRException;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.services.fieldquery.MCRHit;
import org.mycore.services.fieldquery.MCRResults;

public class MCRResultsToJournalList extends JournalList {
    Logger LOGGER = Logger.getLogger(MCRResultsToJournalList.class);

    public MCRResultsToJournalList(MCRResults results) throws JDOMException {
        XPath xPath = XPath.newInstance("maintitles/maintitle/text()");
        for (MCRHit mcrHit : results) {
            MCRObject mcrObj = new MCRObject();
            mcrObj.receiveFromDatastore(mcrHit.getID());
            add(makeEntry(mcrObj, xPath));
        }
    }

    private Entry makeEntry(MCRObject mcrObj, XPath xPath) throws MCRException, JDOMException {
        Element xml = mcrObj.getMetadata().createXML();
        String title = ((Text) xPath.selectSingleNode(xml)).getText();
        return new Entry(title, mcrObj.getId().getId());

    }
}
