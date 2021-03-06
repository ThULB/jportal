package fsu.jportal.backend;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.mycore.access.MCRAccessException;
import org.mycore.common.MCRPersistenceException;
import org.mycore.datamodel.metadata.MCRBase;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.metadata.MCRObjectService;

/**
 * Base jportal interface of components. This includes persons, institutions, articles,
 * volumes and journals. Internally a {@link MCRObject} is used.
 * 
 * @author Matthias Eichner
 */
public interface JPComponent {

    /**
     * Describes what to store.
     */
    enum StoreOption {
        /**
         * Store the metadata of an mcrobject or mcrderivate.
         */
        metadata,

        /**
         * Store added children.
         */
        children,

        /**
         * Update all the derivates of an mycore object.
         */
        derivate,

        /**
         * Store the content of a derivate. Only relevant if you update
         * a {@link MCRDerivate}.
         */
        content
    }

    /**
     * The corresponding mycore object.
     * 
     * @return the base <code>MCRObject</code>
     */
    MCRBase getObject();

    /**
     * Returns the mycore object id for this component.
     * 
     * @return the mycore object id
     */
    default MCRObjectID getId() {
        return getObject().getId();
    }

    /**
     * Returns the creation date and time of this component. Can return
     * null if the mycore object does not have such a createdate field.
     * 
     * @return date and time of creation
     */
    default LocalDateTime getCreateDate() {
        MCRObjectService service = getObject().getService();
        Date date = service.getDate(MCRObjectService.DATE_TYPE_CREATEDATE);
        if (date == null) {
            return null;
        }
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * Returns the creation date and time of this component. Can return
     * null if the mycore object does not have such a modifydate field.
     * 
     * @return date and time of creation
     */
    default LocalDateTime getModifyDate() {
        MCRObjectService service = getObject().getService();
        Date date = service.getDate(MCRObjectService.DATE_TYPE_MODIFYDATE);
        if (date == null) {
            return null;
        }
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * Returns the title of the component.
     * 
     * @return the title or null
     */
    String getTitle();

    /**
     * Stores the component metadata, all its children and all derivates with content.
     *
     * @throws MCRPersistenceException if a persistence problem occur
     * @throws MCRAccessException if write permission is missing
     */
    default void store() throws MCRPersistenceException, MCRAccessException {
        this.store(StoreOption.metadata, StoreOption.children, StoreOption.derivate, StoreOption.content);
    }

    /**
     * Stores the component to the mycore system. With the option flags you can
     * handle what to store.
     * 
     * @param options what to store
     *
     * @throws MCRPersistenceException if a persistence problem occur
     * @throws MCRAccessException if write permission is missing
     */
    void store(StoreOption... options) throws MCRPersistenceException, MCRAccessException;

    /**
     * Returns the type of the component. One of person, jpinst, jpjournal, jpvolume,
     * jparticle or derivate is returned here.
     * 
     * @return the tpye of the component
     */
    String getType();

}
