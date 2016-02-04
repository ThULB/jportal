package fsu.jportal.backend;

import org.mycore.access.MCRAccessException;
import org.mycore.common.MCRPersistenceException;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.metadata.MCRObject;

import java.text.DecimalFormat;

/**
 * Base jportal interface of components. This includes persons, institutions, articles,
 * volumes and journals. Internally a {@link MCRObject} is used.
 * 
 * @author Matthias Eichner
 */
public interface JPComponent {

    public static DecimalFormat EIGHT_DIGIT_FORMAT =  new DecimalFormat("00000000");
    public static DecimalFormat FOUR_DIGIT_FORMAT =  new DecimalFormat("0000");

    /**
     * The corresponding mycore object.
     * 
     * @return the base <code>MCRObject</code>
     */
    public MCRObject getObject();

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
     * 
     * @throws MCRActiveLinkException 
     * @throws MCRPersistenceException 
     */
    public void store() throws MCRPersistenceException, MCRActiveLinkException, MCRAccessException;

}
