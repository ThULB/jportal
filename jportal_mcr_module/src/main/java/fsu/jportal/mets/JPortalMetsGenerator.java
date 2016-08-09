package fsu.jportal.mets;

import static fsu.jportal.frontend.SolrToc.buildQuery;
import static fsu.jportal.frontend.SolrToc.getSort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.mycore.datamodel.metadata.MCRMetaLangText;
import org.mycore.datamodel.metadata.MCRMetadataManager;
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
        Optional<MCRMetaLangText> maintitle = obj.getMetadata().findFirst("maintitles");
        return maintitle.map(MCRMetaLangText::getText).orElse("no title for " + obj.getId());
    }

    protected List<MCRObject> getChildren(MCRObject parentObject) {
        if (parentObject.getId().getTypeId().equals("jparticle")) {
            return Collections.emptyList();
        }
        List<MCRObject> children = new ArrayList<MCRObject>();
        getChildren(parentObject, "jpvolume").stream()
                                             .map(MCRMetadataManager::retrieveMCRObject)
                                             .forEach(children::add);
        getChildren(parentObject, "jparticle").stream()
                                              .map(MCRMetadataManager::retrieveMCRObject)
                                              .forEach(children::add);
        return children;
    }

    protected List<MCRObjectID> getChildren(MCRObject parentObject, String objectType) {
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
