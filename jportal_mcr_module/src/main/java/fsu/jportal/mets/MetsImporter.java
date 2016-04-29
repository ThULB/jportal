package fsu.jportal.mets;

import java.util.Map;

import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.mets.model.Mets;
import org.mycore.mets.model.struct.LogicalDiv;

import fsu.jportal.backend.JPComponent;

/**
 * Base interface for mets importing classes.
 * 
 * @author Matthias Eichner
 */
public interface MetsImporter {

    /**
     * Does the mets import.
     * 
     * @param mets METS Object
     * @param derivateId MCR derivate ID
     * @throws MetsImportException something went so wrong that the import process has to be stopped
     * @return a map where each logical div is assigned to its imported <code>JPComponent</code>
     */
    public Map<LogicalDiv, JPComponent> importMets(Mets mets, MCRObjectID derivateId) throws MetsImportException;

}
