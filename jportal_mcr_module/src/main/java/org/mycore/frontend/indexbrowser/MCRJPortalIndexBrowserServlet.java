package org.mycore.frontend.indexbrowser;

import java.util.List;

import org.jdom.Document;
import org.mycore.frontend.indexbrowser.lucene.MCRIndexBrowserCache;
import org.mycore.frontend.indexbrowser.lucene.MCRIndexBrowserEntry;
import org.mycore.frontend.indexbrowser.lucene.MCRIndexBrowserServlet;
import org.mycore.frontend.indexbrowser.lucene.MCRIndexBrowserUtilsFoo;
import org.mycore.frontend.indexbrowser.lucene.MCRIndexBrowserXmlGenerator;

public class MCRJPortalIndexBrowserServlet extends MCRIndexBrowserServlet {

    private static final long serialVersionUID = 1L;
   
    @Override
    protected Document createResultListDocument() {
        List<MCRIndexBrowserEntry> resultList = null;
        String index = config.getIndex();
        String cacheKey = MCRIndexBrowserUtilsFoo.getCacheKey(index, incomingBrowserData);
        if(MCRIndexBrowserCache.isCached(index, cacheKey)) {
            resultList = MCRIndexBrowserCache.getFromCache(index, cacheKey);
        } else {
            MCRJPortalIndexBrowserSearcher searcher = new MCRJPortalIndexBrowserSearcher(incomingBrowserData, config);
            resultList = searcher.doSearch();
            MCRIndexBrowserCache.addToCache(cacheKey, index, resultList);
        }
        MCRIndexBrowserXmlGenerator xmlGen = new MCRIndexBrowserXmlGenerator(resultList, incomingBrowserData, config);
        return xmlGen.getXMLContent();
    }
}