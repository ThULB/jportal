package fsu.jportal.backend;

import java.io.IOException;

import org.mycore.access.MCRAccessException;
import org.mycore.common.MCRPersistenceException;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.metadata.MCRBase;
import org.mycore.datamodel.metadata.MCRObject;

/**
 * Base jportal interface of components. This includes persons, institutions, articles,
 * volumes and journals. Internally a {@link MCRObject} is used.
 * 
 * @author Matthias Eichner
 */
public interface JPComponent {

    /**
     * The corresponding mycore object.
     * 
     * @return the base <code>MCRObject</code>
     */
    public MCRBase getObject();

    /**
     * Returns the title of the component.
     * 
     * @return the title or null
     */
    public String getTitle();

    /**
     * Stores the component and all its children (when its a {@link JPContainer}) 
     * to the mycore system. This method checks if the component is already added,
     * if so an update is done.
     */
    public void store() throws MCRPersistenceException, MCRActiveLinkException, MCRAccessException, IOException;

    /**
     * Returns the type of the component. One of person, jpinst, jpjournal, jpvolume,
     * jparticle or derivate is returned here.
     * 
     * @return the tpye of the component
     */
    public abstract String getType();
   
}
