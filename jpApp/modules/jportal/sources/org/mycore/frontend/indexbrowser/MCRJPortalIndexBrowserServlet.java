package org.mycore.frontend.indexbrowser;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.jdom.Document;
import org.mycore.frontend.indexbrowser.lucene.MCRIndexBrowserCache;
import org.mycore.frontend.indexbrowser.lucene.MCRIndexBrowserEntry;
import org.mycore.frontend.indexbrowser.lucene.MCRIndexBrowserIncomingData;
import org.mycore.frontend.indexbrowser.lucene.MCRIndexBrowserServlet;
import org.mycore.frontend.indexbrowser.lucene.MCRIndexBrowserXmlGenerator;

public class MCRJPortalIndexBrowserServlet extends MCRIndexBrowserServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected Document createResultListDocument() {
        List<MCRIndexBrowserEntry> resultList = null;
        String index = config.getIndex();
        if(MCRIndexBrowserCache.isCached(index, incomingBrowserData)) {
            resultList = MCRIndexBrowserCache.getFromCache(index, incomingBrowserData);
        } else {
            MCRJPortalIndexBrowserSearcher searcher = new MCRJPortalIndexBrowserSearcher(incomingBrowserData, config);
            resultList = searcher.doSearch();
            MCRIndexBrowserCache.addToCache(incomingBrowserData, index, resultList);
        }
        MCRIndexBrowserXmlGenerator xmlGen = new MCRIndexBrowserXmlGenerator(resultList, incomingBrowserData, config);
        return xmlGen.getXMLContent();
    }

    protected MCRIndexBrowserIncomingData getIncomingBrowserData(HttpServletRequest request) {
        String search = "*" + request.getParameter("search") + "*";
        String mode = getMode(request);
        String searchclass = request.getParameter("searchclass");
        String fromTo = request.getParameter("fromTo");
        String init = request.getParameter("init");

        return new MCRIndexBrowserIncomingData(search, mode, searchclass, fromTo, init);
    }
    
    protected String getMode(HttpServletRequest request) {
        if (request.getParameter("mode") != null && !request.getParameter("mode").trim().equals("")) {
            return request.getParameter("mode").toLowerCase().trim();
        } else
            return "prefix";
    }
}