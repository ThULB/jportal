package fsu.jportal.mets;

import java.util.Map;

import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.mets.model.Mets;
import org.mycore.mets.model.struct.LogicalDiv;

import fsu.jportal.backend.JPComponent;

public class JVBMetsImporter implements MetsImporter {

    @Override
    public Map<LogicalDiv, JPComponent> importMets(Mets mets, MCRObjectID derivateId) throws MetsImportException {
        // TODO Auto-generated method stub
        return null;
    }

}
