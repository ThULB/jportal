package fsu.jportal.mets;

import fsu.jportal.backend.MetadataManager;
import fsu.jportal.util.MetsUtil;
import org.apache.logging.log4j.LogManager;
import org.mycore.common.MCRException;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.mets.model.MCRMETSAbstractGenerator;
import org.mycore.mets.model.MCRMETSGenerator;
import org.mycore.mets.model.MCRMETSGeneratorFactory;
import org.mycore.mets.model.Mets;
import org.mycore.mets.tools.MCRMetsSave;

/**
 * Chooses which mets generator to pick depending if there is already an existing mets.xml and if the parent object
 * (probably a volume) has children or not.
 *
 * @author Matthias Eichner
 */
public class JPMETSGeneratorSelector implements MCRMETSGeneratorFactory.MCRMETSGeneratorSelector {

    @Override
    public MCRMETSGenerator get(MCRPath derivatePath) throws MCRException {
        // get derivate
        MCRObjectID derId = MCRObjectID.getInstance(derivatePath.getOwner());
        MCRDerivate mcrDer = MCRMetadataManager.retrieveMCRDerivate(derId);
        // get mycore object
        MCRObjectID objId = mcrDer.getDerivate().getMetaLink().getXLinkHrefID();
        MCRObject mcrObj = MetadataManager.retrieveMCRObject(objId);

        // there is an mets.xml and we don't have children -> just update the mets.xml
        if (MetsUtil.hasMets(derId.toString()) && mcrObj.getStructure().getChildren().isEmpty()) {
            return new JPMetsUpdateFileGenerator();
        }
        return new JPMetsHierarchyGenerator();
    }

    /**
     * Just updates the existing mets.xml with new files and removes old ones.
     */
    private class JPMetsUpdateFileGenerator extends MCRMETSAbstractGenerator {

        @Override
        public Mets generate() throws MCRException {
            return getOldMets().map(oldMets -> {
                try {
                    MCRMetsSave.updateFiles(oldMets, getDerivatePath());
                } catch (Exception exc) {
                    LogManager.getLogger().error("Unable to update mets.xml of derivate " + getOwner(), exc);
                }
                return oldMets;
            }).orElse(null);
        }
    }

}
