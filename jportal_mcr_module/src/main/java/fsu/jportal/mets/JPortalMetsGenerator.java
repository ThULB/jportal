package fsu.jportal.mets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.mycore.datamodel.metadata.MCRMetaElement;
import org.mycore.datamodel.metadata.MCRMetaInterface;
import org.mycore.datamodel.metadata.MCRMetaLangText;
import org.mycore.datamodel.metadata.MCRMetaLinkID;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.mets.model.MCRMETSHierarchyGenerator;
import org.mycore.solr.MCRSolrClientFactory;

public class JPortalMetsGenerator extends MCRMETSHierarchyGenerator {
    private static Logger LOGGER = LogManager.getLogger(JPortalMetsGenerator.class);

    protected String getType(MCRObject obj) {
        return obj.getId().getTypeId().substring(2);
    }

    protected String getLabel(MCRObject obj) {
        MCRMetaElement me = obj.getMetadata().getMetadataElement("maintitles");
        if (me != null) {
            Iterator<MCRMetaInterface> it = me.iterator();
            while (it.hasNext()) {
                MCRMetaInterface mi = it.next();
                if (mi.getInherited() == 0 && mi.getSubTag().equals("maintitle") && mi instanceof MCRMetaLangText) {
                    return ((MCRMetaLangText) mi).getText();
                }
            }
        }
        return "no title for " + obj.getId().toString();
    }

    protected List<MCRMetaLinkID> getChildren(MCRObject parentObject) {
        String sorlQuery = "+parent:" + parentObject.getId().toString();
        ModifiableSolrParams solrParams = new ModifiableSolrParams();
        solrParams.set("q", sorlQuery);
        solrParams.set("sort", "size asc");
        ArrayList<MCRMetaLinkID> linkList = new ArrayList<>();
        try {
            QueryResponse response = MCRSolrClientFactory.getSolrClient().query(solrParams);
            for (SolrDocument solrDoc : response.getResults()) {
                String id = (String) solrDoc.getFieldValue("id");
                MCRMetaLinkID metaLinkID = new MCRMetaLinkID("child", MCRObjectID.getInstance(id), parentObject.getLabel(), id);
                linkList.add(metaLinkID);
            }
        } catch (SolrServerException | IOException e) {
            e.printStackTrace();
        }

        return linkList;
    }

    protected String getEnclosingDerivateLinkName() {
        return "derivateLinks";
    }

    protected String getDerivateLinkName() {
        return "derivateLink";
    }
}
