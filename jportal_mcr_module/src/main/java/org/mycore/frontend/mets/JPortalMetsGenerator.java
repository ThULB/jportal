package org.mycore.frontend.mets;

import java.util.Iterator;

import org.mycore.datamodel.metadata.MCRMetaElement;
import org.mycore.datamodel.metadata.MCRMetaInterface;
import org.mycore.datamodel.metadata.MCRMetaLangText;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.mets.model.MCRMETSHierarchyGenerator;

public class JPortalMetsGenerator extends MCRMETSHierarchyGenerator {

    protected String getType(MCRObject obj) {
        return obj.getId().getTypeId().substring(2);
    }

    protected String getLabel(MCRObject obj) {
        MCRMetaElement me = obj.getMetadata().getMetadataElement("maintitles");
        if(me != null) {
            Iterator<MCRMetaInterface> it = me.iterator();
            while(it.hasNext()) {
                MCRMetaInterface mi = it.next();
                if(mi.getInherited() == 0 && mi.getSubTag().equals("maintitle") && mi instanceof MCRMetaLangText) {
                    return ((MCRMetaLangText)mi).getText();
                }
            }
        }
        return "no title for " + obj.getId().toString();
    }

    protected String getEnclosingDerivateLinkName() {
        return "derivateLinks";
    }
    protected String getDerivateLinkName() {
        return "derivateLink";
    }
}
