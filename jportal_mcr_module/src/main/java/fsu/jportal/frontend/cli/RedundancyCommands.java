package fsu.jportal.frontend.cli;

import static org.mycore.access.MCRAccessManager.PERMISSION_WRITE;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.ElementFilter;
import org.mycore.access.MCRAccessManager;
import org.mycore.common.xml.MCRAttributeValueFilter;
import org.mycore.common.xsl.MCRParameterCollector;
import org.mycore.datamodel.common.MCRLinkTableManager;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.cli.annotation.MCRCommand;
import org.mycore.frontend.cli.annotation.MCRCommandGroup;
import org.mycore.solr.MCRSolrClientFactory;

import fsu.jportal.backend.MetaDataTools;

@MCRCommandGroup(name = "JP doubletOf Commands")
public class RedundancyCommands{

    private static final Logger LOGGER = Logger.getLogger(RedundancyCommands.class);

    @MCRCommand(help = "Deletes and relinks all doublets for a specific type. Doublets signed with doubletOf", syntax = "fix title of {0} for link {1}")
    public static void removeDoublets(String objId, String linkID){
        MCRParameterCollector parameter = new MCRParameterCollector();
        parameter.setParameter("linkId", linkID);
        
        MetaDataTools.updateWithXslt(objId, "/xsl/fixTitleOfLink.xsl", parameter);
    }

    
    
    @MCRCommand(help = "Deletes and relinks all doublets for a specific type. Doublets signed with doubletOf", syntax = "jp clean up {0}")
    public static List<String> cleanUp(String type) {
        // get all objects of specific type where doubletOf is not empty
        SolrDocumentList results = getDoubletObjsOfType(type);
        ArrayList<String> commandList = new ArrayList<String>();
        
        for (SolrDocument hit : results) {
            String doublet = (String) hit.getFieldValue("id");
            String doubletOf = (String) hit.getFieldValue("doubletOf");
            if (!doublet.equals(doubletOf)) {
                StringBuffer replaceCommand = new StringBuffer("internal replace links and remove ");
                replaceCommand.append(doublet).append(" ").append(doubletOf);
                commandList.add(replaceCommand.toString());
            }
        }
        
        return commandList;
    }

    public static SolrDocumentList getDoubletObjsOfType(String type) {
        try {
            SolrQuery q = new SolrQuery("+doubletOf:* +objectType:"+type);
            SolrDocumentList solrResultList = MCRSolrClientFactory.getSolrClient().query(q).getResults();
            return solrResultList;
        } catch (SolrServerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @MCRCommand(help = "internal command for replacing links and removing the doublet", syntax = "internal replace links and remove {0} {1}")
    public static List<String> replaceAndRemove(String doublet, String doubletOf) throws Exception {
        ArrayList<String> commandList = new ArrayList<String>();
        if (!MCRMetadataManager.exists(MCRObjectID.getInstance(doubletOf))) {
            String errorMsg = "'" + doublet + "' is defined as a doublet of the nonexistent object '" + doubletOf + "'!"
                    + " The doublet is not removed!";
            // print to console
            LOGGER.error(errorMsg);
            // write to file
            BufferedWriter out = new BufferedWriter(new FileWriter("invalidDoublets.txt", true));
            out.write(errorMsg + "\n");
            out.close();
            return commandList;
        }
        Collection<String> list = MCRLinkTableManager.instance().getSourceOf(doublet, "reference");
        for (String source : list) {
            // add replace command
            StringBuffer command = new StringBuffer("internal replace links ");
            command.append(source).append(" ");
            command.append(doublet).append(" ");
            command.append(doubletOf);
            commandList.add(command.toString());
            commandList.add("fix title of " + source + " for link " + doubletOf);
            
        }
        // add delete command
        commandList.add(new StringBuffer("delete object ").append(doublet).toString());
        return commandList;
    }
    
    /**
     * Replaces all links which are found in the source mcrobject xml-tree.
     * @param sourceId The source Id as String.
     * @param oldLink The link which to replaced.
     * @param newLink The new link.
     */
    @MCRCommand(help = "internal command for replacing links and removing the doublet", syntax = "internal replace links {0} {1} {2}")
    public static void replaceLinks(String sourceId, String oldLink, String newLink) throws Exception {
        if (!MCRAccessManager.checkPermission(sourceId, PERMISSION_WRITE)) {
            LOGGER.error("The current user has not the permission to modify " + sourceId);
            return;
        }

        MCRObject sourceMCRObject = MCRMetadataManager.retrieveMCRObject(MCRObjectID.getInstance(sourceId));

        // ArrayList for equal elements
        ArrayList<Element> equalElements = new ArrayList<Element>();

        Namespace ns = Namespace.getNamespace("xlink", "http://www.w3.org/1999/xlink");
        MCRAttributeValueFilter oldLinkFilter = new MCRAttributeValueFilter("href", ns, oldLink);
        Document doc = sourceMCRObject.createXML();
        Iterator<Element> i = doc.getDescendants(oldLinkFilter);
        while (i.hasNext()) {
            Element e = i.next();
            e.setAttribute("href", newLink, ns);
            /*  It is possible, that an updated element is equal with an existing element.
                In that case it is necessary to delete the new element. */
            if (isElementAlreadyExists(e)) {
                equalElements.add(e);
            }
        }
        // delete equal elements
        for (Element e : equalElements) {
            Element parent = e.getParentElement();
            parent.removeContent(e);
        }
        sourceMCRObject = new MCRObject(doc);
        MCRMetadataManager.update(sourceMCRObject);
        LOGGER.info("Links replaced of source " + sourceId + ": " + oldLink + " -> " + newLink);
    }
    
    /**
     * Checks if the element is equal to an element from the same parent.
     * @param element The element to check.
     * @return If the element in the parent already exists.
     */
    protected static boolean isElementAlreadyExists(Element element) {
        Element parent = element.getParentElement();
        ElementFilter filter = new ElementFilter(element.getName());
        for (Element child : parent.getContent(filter)) {
            // only different instances
            if (element == child)
                continue;

            // bad compare, but jdom doesnt support a better solution
            if (element.getName().equals(child.getName()) && element.getAttributes().toString().equals(child.getAttributes().toString())) {
                return true;
            }
        }
        return false;
    }
}