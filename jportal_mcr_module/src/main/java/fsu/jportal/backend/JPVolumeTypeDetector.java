package fsu.jportal.backend;

import java.util.List;

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
     * @return types of the volume or an empty list if its not specified, unknown or cannot be detected
     */
    default List<String> detect(MCRObject object) {
        return detect(new JPVolume(object));
    }

    /**
     * Detects the type of the given volume.
     *
     * @param id mycore object id of the volume
     * @return types of the volume or an empty list if its not specified, unknown or cannot be detected
     */
    default List<String> detect(MCRObjectID id) {
        return detect(new JPVolume(id));
    }

    /**
     * Detects the type of the given volume.
     * 
     * @param volume the volume to check
     * @return types of the volume or an empty list if its not specified, unknown or cannot be detected
     */
    List<String> detect(JPVolume volume);

}
