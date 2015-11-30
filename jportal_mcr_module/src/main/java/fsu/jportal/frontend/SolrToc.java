package fsu.jportal.frontend;

import fsu.jportal.util.JPComponentUtil;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.common.content.MCRURLContent;
import org.mycore.solr.MCRSolrClientFactory;
import org.mycore.solr.search.MCRSolrURL;
import org.xml.sax.SAXException;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.List;

/**
 * Created by chi on 03.09.15.
 * @author Huu Chi Vu
 */
public class SolrToc {
    public static ModifiableSolrParams buildQuery(String parentID, String objectType, String sort){
        String sorlQuery = "+parent:" + parentID;
        if (objectType != null) {
            sorlQuery += " +objectType:" + objectType;
        }

        ModifiableSolrParams solrParams = new ModifiableSolrParams();
        solrParams.set("q", sorlQuery);
        solrParams.set("sort", sort);
        return solrParams;
    }

    public static ModifiableSolrParams buildQuery(String parentID, String objectType, String sort, int rows,
            int start) {

        ModifiableSolrParams solrParams = buildQuery(parentID, objectType, sort);
        solrParams.set("rows", rows);
        solrParams.set("start", start);
        return solrParams;
    }

    public static Source getToc(String parentID, String objectType, int start, int rows) {
        String sort = getSort(parentID, objectType);
        ModifiableSolrParams solrParams = buildQuery(parentID, objectType, sort, rows, start);
        SolrClient solrClient = MCRSolrClientFactory.getSolrClient();
        MCRSolrURL solrURL = new MCRSolrURL((HttpSolrClient) solrClient, solrParams.toString());
        try {
            MCRURLContent result = new MCRURLContent(solrURL.getUrl());

            return result.getSource();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String getSort(String parentID, String objectType) {
        if(objectType.equals("jparticle")){
            return "size asc,maintitle asc";
        }

        String datePublOrder = isPartOfOnlineJournal(parentID) ? "desc" : "asc";
        return "indexPosition asc,date.published " + datePublOrder + ",maintitle asc";
    }

    public static boolean isPartOfOnlineJournal(String parentID) {
        String journalID = JPComponentUtil.getJournalID(parentID).orElse("");
        String listType = JPComponentUtil.getListType(journalID).orElse("");
        if (listType.equals("online")) {
            return true;
        }
        return false;
    }

    public static int getRefererStart(String parentID, String objectType, String referer, int rows) throws TransformerException {
        String sort = getSort(parentID, objectType);
        ModifiableSolrParams solrParams = buildQuery(parentID, objectType, sort, 99999, 0).set("fl", "id");

        SolrClient solrClient = MCRSolrClientFactory.getSolrClient();
        MCRSolrURL solrURL = new MCRSolrURL((HttpSolrClient) solrClient, solrParams.toString());
        try {
            MCRURLContent result = new MCRURLContent(solrURL.getUrl());
            Document resultXML = result.asXML();
            XPathExpression<Element> precedingSiblings = XPathFactory.instance()
                    .compile("/response/result/doc[str[@name='id'] = '" + referer + "']/preceding-sibling::*",
                            Filters.element());
            List<Element> precedingSiblingList = precedingSiblings.evaluate(resultXML);
            int positionInParent = precedingSiblingList.size();
            return (int)Math.floor(positionInParent / rows) * rows;
        } catch (IOException e) {
            throw new TransformerException("Unable to get input stream from solr: " + solrURL.getUrl(), e);
        } catch (JDOMException | SAXException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
