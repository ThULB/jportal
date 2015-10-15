package fsu.jportal.backend;

import java.text.DecimalFormat;

import org.mycore.common.MCRPersistenceException;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.metadata.MCRObject;

public interface JPComponent {

    public static DecimalFormat EIGHT_DIGIT_FORMAT =  new DecimalFormat("00000000");
    public static DecimalFormat FOUR_DIGIT_FORMAT =  new DecimalFormat("0000");

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
