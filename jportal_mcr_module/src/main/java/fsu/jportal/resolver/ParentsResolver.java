package fsu.jportal.resolver;

import java.util.List;
import java.util.Optional;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.transform.JDOMSource;
import org.mycore.common.MCRConstants;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.metadata.MCRObjectUtils;

import fsu.jportal.backend.JPComponent;
import fsu.jportal.backend.mcr.MetadataManager;
import fsu.jportal.util.JPComponentUtil;

/**
 * This resolver returns the parents of an mycore object. The returned element looks like:
 * <pre>
 * {@code
 * <parents>
 *   <parent title="title of parent" inherited="1" href="jportal_jpvolume_00000001" referer="jportal_jparticle_00000001" />
 *   <parent title="title of second parent" inherited="2" href="jportal_jpvolume_00000002" referer="jportal_jpvolume_00000001" />
 *   <parent title="journal" inherited="3" href="jportal_jpjournal_00000001" referer="jportal_jpvolume_00000002" />
 * </parents>
 * }
 * </pre>
 * 
 * @author Matthias Eichner
 */
@URIResolverSchema(schema = "parents")
public class ParentsResolver implements URIResolver {

    private static final Logger LOGGER = LogManager.getLogger(ParentsResolver.class);

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        String childId = href.replaceAll("parents:", "");
        try {
            if (!MCRMetadataManager.exists(MCRObjectID.getInstance(childId))) {
                error("Error: the object with the id " + childId + " does not exists.");
            }
            return new JDOMSource(getParents(childId));
        } catch (Exception exc) {
            LOGGER.error("unable to retrieve parents of mcr object " + childId, exc);
            return error("Error: unable to load parents of object " + childId);
        }
    }

    private Source error(String message) {
        Element parent = new Element("parent").setAttribute("error", message).setAttribute("href", "unknown",
            MCRConstants.XLINK_NAMESPACE);
        return new JDOMSource(new Element("parents").addContent(parent));
    }

    public static Element getParents(String childId) {
        Element parents = new Element("parents");
        MCRObjectID objectId = MCRObjectID.getInstance(childId);
        if (objectId.getTypeId().equals("derivate")) {
            objectId = MCRMetadataManager.retrieveMCRDerivate(objectId).getDerivate().getMetaLink().getXLinkHrefID();
        }
        MCRObject child = MetadataManager.retrieveMCRObject(objectId);
        List<MCRObject> parentList = MCRObjectUtils.getAncestors(child);
        String referer = objectId.toString();
        int inherited = 1;
        for (MCRObject parent : parentList) {
            parents.addContent(0, buildParent(parent, referer, inherited++));
            referer = parent.getId().toString();
        }
        return parents;
    }

    public static Element buildParent(MCRObject parent, String referer, int inherited) {
        Optional<? extends JPComponent> parentComponent = JPComponentUtil.get(parent.getId());
        Element parentElement = new Element("parent");
        parentElement.setAttribute("inherited", String.valueOf(inherited));
        parentElement.setAttribute("title", parentComponent.get().getTitle(), MCRConstants.XLINK_NAMESPACE);
        parentElement.setAttribute("href", parent.getId().toString(), MCRConstants.XLINK_NAMESPACE);
        if (referer != null) {
            parentElement.setAttribute("referer", referer);
        }
        return parentElement;
    }

}
