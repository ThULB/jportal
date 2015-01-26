package fsu.jportal.backend;

import org.mycore.common.MCRPersistenceException;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.metadata.MCRObject;

public interface JPComponent {

    /**
     * The corresponding mycore object.
     * 
     * @return
     */
    public MCRObject getObject();

    /**
     * Returns the title of the component.
     * 
     * @return the title or null
     */
    public String getTitle();

    /**
     * Imports the component and all its children (when its a {@link JPContainer}) 
     * to the mycore system. This method checks if the component is already added,
     * if so an update is done.
     * @throws MCRActiveLinkException 
     * @throws MCRPersistenceException 
     */
    public void importComponent() throws MCRPersistenceException, MCRActiveLinkException;

}
