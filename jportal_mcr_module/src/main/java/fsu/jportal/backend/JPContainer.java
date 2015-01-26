package fsu.jportal.backend;

import java.util.Collection;

import org.mycore.datamodel.metadata.MCRObjectID;

/**
 * Component that can contain other components as children.
 * 
 * @author Matthias Eichner
 */
public interface JPContainer extends JPComponent {

    /**
     * Adds a new child.
     * 
     * @param child the child to add
     */
    public void addChild(JPComponent child);

    /**
     * Removes the child by the given id. If there is no such child
     * nothing happens.
     * 
     * @param id
     */
    public void removeChild(MCRObjectID id);

    /**
     * Returns a unmodifiable collection of all children.
     * 
     * @return collection of children
     */
    public Collection<JPComponent> getChildren();

}
