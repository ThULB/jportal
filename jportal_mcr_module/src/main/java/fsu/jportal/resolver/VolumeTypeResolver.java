package fsu.jportal.resolver;

import java.util.Optional;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.jdom2.Element;
import org.jdom2.transform.JDOMSource;
import org.mycore.datamodel.metadata.MCRObjectID;

import fsu.jportal.backend.JPVolume;
import fsu.jportal.util.JPComponentUtil;

/**
 * Resolves the volume types of a given jpvolume.
 * 
 * @author Matthias Eichner
 */
@URIResolverSchema(schema = "volumeType")
public class VolumeTypeResolver implements URIResolver {

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        String id = href.substring(href.indexOf(":") + 1);
        if (!MCRObjectID.isValid(id)) {
            throw new TransformerException(
                "Unable to get volume type of " + id + ". Its not a valid mycore object id.");
        }
        MCRObjectID mcrObjectID = MCRObjectID.getInstance(id);
        Optional<JPVolume> volumeOptional = JPComponentUtil.get(mcrObjectID, JPVolume.class);
        if (!volumeOptional.isPresent()) {
            throw new TransformerException(
                "Unable to get volume type of " + id + ". The object is not a volume.");
        }
        JPVolume volume = volumeOptional.get();
        Element volumeTypes = new Element("types");
        volume.getVolumeTypes().stream()
            .map(value -> new Element("type").setText(value))
            .forEach(volumeTypes::addContent);
        return new JDOMSource(volumeTypes);
    }

}
