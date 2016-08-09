package fsu.jportal.mets;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectUtils;
import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.mets.model.Mets;
import org.mycore.mets.model.sections.AmdSec;
import org.mycore.mets.model.sections.DmdSec;
import org.mycore.mets.model.struct.LogicalDiv;
import org.mycore.mets.model.struct.LogicalStructMap;

/**
 * Created by chi on 28.07.16.
 * @author Huu Chi Vu
 */
public class DFGOAIMetsGenerator extends JPortalMetsGenerator {
    protected final static Logger LOGGER = LogManager.getLogger();

    @Override
    protected AmdSec createAmdSection() {
        String amdId = "amd_" + this.rootObj.getId().toString();
        return new AmdSec(amdId);
    }

    @Override
    protected DmdSec createDmdSection() {
        String dmdSec = "dmd_" + this.rootObj.getId().toString();
        return new DmdSec(dmdSec);
    }

    @Override
    protected Mets createMets(MCRPath dir, Set<MCRPath> ignoreNodes) throws IOException {
        Mets mets = super.createMets(dir, ignoreNodes);
        visitMCRObjTree(this.rootObj)
                .forEach(id -> {
                             mets.addAmdSec(new AmdSec("amd_" + id.toString()));
                             mets.addDmdSec(new DmdSec("dmd_" + id.toString()));
                         }
                );

        LogicalDiv divContainer = ((LogicalStructMap) mets.getStructMap(LogicalStructMap.TYPE))
                .getDivContainer();

        visitStructMapTree(divContainer).forEach(structMap -> LOGGER.info("Map: " + structMap.getId()));
        return mets;
    }

    private Stream<LogicalDiv> visitStructMapTree(LogicalDiv divContainer) {
        return Stream.concat(Stream.of(divContainer), divContainer.getChildren().stream()
                                                                  .flatMap(this::visitStructMapTree));
    }

    private Stream<MCRObject> visitMCRObjTree(MCRObject obj) {
        List<MCRObject> children = MCRObjectUtils.getChildren(obj);
        return Stream.concat(children.stream(), children.stream().flatMap(this::visitMCRObjTree));
    }
}
