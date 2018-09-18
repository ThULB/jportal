package fsu.jportal.backend;

import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;

/**
 * There are different types of volume in jportal. The object type jpvolume is way to general. You can use this
 * interface to specify the type exactly.
 *
 * @author Matthias Eichner
 */
public interface JPVolumeTypeDetector {

    /**
     * Detects the type of the given volume.
     *
     * @param object the mycore object
     * @return type of the volume or null if its not specified, unknown or cannot be detected
     */
    default String detect(MCRObject object) {
        return detect(new JPVolume(object));
    }

    /**
     * Detects the type of the given volume.
     *
     * @param id mycore object id of the volume
     * @return type of the volume or null if its not specified, unknown or cannot be detected
     */
    default String detect(MCRObjectID id) {
        return detect(new JPVolume(id));
    }

    /**
     * Detects the type of the given volume.
     * 
     * @param volume the volume to check
     * @return type of the volume or null if its not specified, unknown or cannot be detected
     */
    String detect(JPVolume volume);

}
