package org.mycore.frontend.indexbrowser;

import java.util.List;

import org.jdom.Document;

public class MCRJPortalIndexBrowserServlet extends MCRIndexBrowserServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected Document createResultListDocument() {
        List<MCRIndexBrowserEntry> resultList = null;
        if(MCRIndexBrowserCache.isCached(incomingBrowserData)) {
            resultList = MCRIndexBrowserCache.getFromCache(incomingBrowserData);
        } else {
            MCRJPortalIndexBrowserSearcher searcher = new MCRJPortalIndexBrowserSearcher(incomingBrowserData, config);
            resultList = searcher.doSearch();
            MCRIndexBrowserCache.addToCache(incomingBrowserData, resultList);
        }
        MCRIndexBrowserXmlGenerator xmlGen = new MCRIndexBrowserXmlGenerator(resultList, incomingBrowserData, config);
        return xmlGen.getXMLContent();
    }

}