package fsu.jportal.mets;

import static fsu.jportal.frontend.SolrToc.buildQuery;
import static fsu.jportal.frontend.SolrToc.getSort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.mycore.datamodel.metadata.MCRMetaElement;
import org.mycore.datamodel.metadata.MCRMetaInterface;
import org.mycore.datamodel.metadata.MCRMetaLangText;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.mets.model.MCRMETSHierarchyGenerator;
import org.mycore.solr.MCRSolrClientFactory;
import org.mycore.solr.search.MCRSolrSearchUtils;

public class JPortalMetsGenerator extends MCRMETSHierarchyGenerator {

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

    protected List<MCRObjectID> getChildren(MCRObject parentObject) {
        if (parentObject.getId().getTypeId().equals("jparticle")) {
            return Collections.emptyList();
        }
        List<MCRObjectID> childrenIds = new ArrayList<MCRObjectID>();
        childrenIds.addAll(getChildren(parentObject, "jpvolume"));
        childrenIds.addAll(getChildren(parentObject, "jparticle"));
        return childrenIds;
    }

    private List<MCRObjectID> getChildren(MCRObject parentObject, String objectType) {
        String parentID = parentObject.getId().toString();
        String sort = getSort(parentID, objectType);
        ModifiableSolrParams solrParams = buildQuery(parentID, objectType, sort);
        solrParams.set("fl", "id objectType");
        SolrClient solrClient = MCRSolrClientFactory.getSolrClient();
        return MCRSolrSearchUtils.stream(solrClient, solrParams).map(doc -> {
            String id = (String) doc.getFieldValue("id");
            return MCRObjectID.getInstance(id);
        }).collect(Collectors.toList());
    }

    protected String getEnclosingDerivateLinkName() {
        return "derivateLinks";
    }

    protected String getDerivateLinkName() {
        return "derivateLink";
    }
}
