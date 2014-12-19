package fsu.jportal.laws.frontend.mets;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetaElement;
import org.mycore.datamodel.metadata.MCRMetaInterface;
import org.mycore.datamodel.metadata.MCRMetaLangText;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.mets.model.MCRMETSDefaultGenerator;
import org.mycore.mets.model.MCRMETSGenerator;
import org.mycore.mets.model.Mets;

import fsu.jportal.mets.JPortalMetsGenerator;

public class LawsMETSGenerator extends MCRMETSGenerator {

    @Override
    public Mets getMETS(MCRPath dir, Set<MCRPath> ignoreNodes) throws IOException {
        // get derivate
        MCRObjectID derId = MCRObjectID.getInstance(dir.getOwner());
        MCRDerivate mcrDer = MCRMetadataManager.retrieveMCRDerivate(derId);
        // get mycore object
        MCRObjectID objId = mcrDer.getDerivate().getMetaLink().getXLinkHrefID();
        MCRObject mcrObj = MCRMetadataManager.retrieveMCRObject(objId);

        // has register entry
        if (hasRegisterEntry(mcrObj)) {
            return new LawCollectionMETSGenerator().getMETS(dir, ignoreNodes);
        }
        // has children -> use hierarchy mets generator
        if (mcrObj.getStructure().getChildren().size() > 0) {
            return new JPortalMetsGenerator().getMETS(dir, ignoreNodes);
        }
        return new MCRMETSDefaultGenerator().getMETS(dir, ignoreNodes);
    }

    /**
     * Checks if the given mcr object has a register entry:
     * &lt;identi type="register"&gt;1821_Register.xml&lt;/identi&gt;
     * 
     * @param obj
     * @return
     */
    private boolean hasRegisterEntry(MCRObject obj) {
        MCRMetaElement identis = obj.getMetadata().getMetadataElement("identis");
        if (identis == null)
            return false;
        Iterator<MCRMetaInterface> it = identis.iterator();
        while (it.hasNext()) {
            MCRMetaInterface mI = it.next();
            if (!(mI instanceof MCRMetaLangText))
                continue;
            if (((MCRMetaLangText) mI).getType().equals("register"))
                return true;
        }
        return false;
    }

}
