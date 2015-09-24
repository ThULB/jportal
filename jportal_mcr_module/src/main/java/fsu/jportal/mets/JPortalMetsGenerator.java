package fsu.jportal.mets;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import fsu.jportal.frontend.SolrToc;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.mycore.datamodel.metadata.*;
import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.mets.model.MCRMETSHierarchyGenerator;
import org.mycore.mets.model.files.FileSec;
import org.mycore.mets.model.struct.*;
import org.mycore.solr.MCRSolrClientFactory;
import org.mycore.solr.search.MCRSolrSearchUtils;
import org.mycore.solr.search.MCRSolrURL;

import static fsu.jportal.frontend.SolrToc.buildQuery;
import static fsu.jportal.frontend.SolrToc.getSort;

public class JPortalMetsGenerator extends MCRMETSHierarchyGenerator {
    private static Logger LOGGER = Logger.getLogger(JPortalMetsGenerator.class);

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
        List<MCRMetaLinkID> metaLinkIDs = getMcrMetaLinkIDs(parentObject, "jpvolume");
        metaLinkIDs.addAll(getMcrMetaLinkIDs(parentObject, "jparticle"));
        return metaLinkIDs;
    }

    private List<MCRMetaLinkID> getMcrMetaLinkIDs(MCRObject parentObject, String objectType) {
        String parentID = parentObject.getId().toString();
        String sort = getSort(parentID, objectType);
        ModifiableSolrParams solrParams = buildQuery(parentID, objectType, sort);
        SolrClient solrClient = MCRSolrClientFactory.getSolrClient();

        SolrTocHandler tocHandler = new SolrTocHandler(parentObject);
        try {
            return MCRSolrSearchUtils.list(solrClient, solrParams, tocHandler);
        } catch (SolrServerException | IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private class SolrTocHandler implements MCRSolrSearchUtils.DocumentHandler<MCRMetaLinkID>{
        private final MCRObject parentObject;

        public SolrTocHandler(MCRObject parentObject) {
            this.parentObject = parentObject;
        }

        @Override
        public MCRMetaLinkID getResult(SolrDocument document) {
            String id = (String) document.getFieldValue("id");
            String label = parentObject.getLabel();
            if(label == null || label.equals("")){
                label = id;
            }

            return new MCRMetaLinkID("child", MCRObjectID.getInstance(id), label, id);
        }

        @Override
        public String fl() {
            return "id objectType";
        }
    }

    protected String getEnclosingDerivateLinkName() {
        return "derivateLinks";
    }

    protected String getDerivateLinkName() {
        return "derivateLink";
    }
}
