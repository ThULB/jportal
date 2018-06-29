package fsu.jportal.mets;

import org.mycore.datamodel.metadata.MCRObjectID;

/**
 * Base interface for auto generating mets.xml.
 * 
 * @author Matthias Eichner
 */
public interface MetsAutoGenerator {

    /**
     * Adds a new derivate to the auto generator.
     * 
     * @param derivateId the derivate identifier
     */
    void add(MCRObjectID derivateId);

    /**
     * Removes a derivate from the auto generator.
     * 
     * @param derivateId the derivate identifier
     */
    void remove(MCRObjectID derivateId);

}
