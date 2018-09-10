package fsu.jportal.mets;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jdom2.Element;
import org.mycore.common.MCRConstants;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectStructure;
import org.mycore.datamodel.metadata.MCRObjectUtils;

import com.neovisionaries.i18n.LanguageAlpha3Code;
import com.neovisionaries.i18n.LanguageCode;

import fsu.jportal.backend.JPContainer;
import fsu.jportal.backend.JPLegalEntity;
import fsu.jportal.backend.JPPeriodicalComponent;
import fsu.jportal.backend.JPPerson;
import fsu.jportal.backend.JPVolume;
import fsu.jportal.util.JPComponentUtil;
import fsu.jportal.xml.JPXMLFunctions;

/**
 * Contains some useful static methods for zvdd mets generation.
 *
 * @author Matthias Eichner
 */
public abstract class ZvddMetsTools {

    /**
     * Returns the title for a volume (year). The full title of a volume is a combination of the titles of all ancestor
     * volumes and its own title separated by ":".
     *
     * <p>E.g. journal (LLZ) : volume (Intelligenzblaetter) : volume (1788) -> the title would be 
     * "Intelligenzblaetter : 1788"</p>
     *
     * @param volume the volume
     * @return the title of the volume
     */
    public static String getTitle(JPVolume volume) {
        List<String> titles = MCRObjectUtils.getAncestorsAndSelf(volume.getObject()).stream()
            .map(JPComponentUtil::getContainer)
            .flatMap(o -> o.map(Stream::of).orElseGet(Stream::empty))
            .filter(container -> container.getType().equals(JPVolume.TYPE))
            .map(JPVolume.class::cast)
            .map(JPVolume::getTitle)
            .collect(Collectors.toList());
        Collections.reverse(titles);
        return String.join(" : ", titles);
    }

    /**
     * The zvdd requires an @order attribute at the mods:part element. This has to be an integer value. Due to the
     * complex hierarchy of our volumes (years/issues can appear on different levels in the hierarchy) its hard to
     * determine a sequence of n+1. For faster calculation we summarize the number of children for each ancestor
     * and add the position of the given volume in its parent.
     *
     * @param volume the volume to get the position
     * @return the order value
     */
    public static Integer calculateOrder(JPVolume volume) {
        final JPContainer parent = volume.getParent().orElse(null);
        if (parent == null) {
            return 0;
        }
        int order = parent.getChildren().indexOf(volume.getId());
        order += MCRObjectUtils.getAncestors(volume.getObject()).stream()
            .filter(o -> !o.getId().equals(parent.getId()))
            .map(MCRObject::getStructure)
            .map(MCRObjectStructure::getChildren)
            .map(List::size).mapToInt(Integer::intValue).sum();
        return order;
    }

    /**
     * Create a new mods:* element in http://www.loc.gov/mods/v3 namespace.
     *
     * @param name the name of the element
     * @return a new jdom element
     */
    public static Element mods(String name) {
        return new Element(name, MCRConstants.MODS_NAMESPACE);
    }

    /**
     * Creates a new mods:titleInfo element with the given title and subtitle.
     *
     * @param title the title
     * @param subtitle the subtitle
     * @return a new jdom mods:titleInfo element
     */
    public static Element modsTitleInfo(String title, String subtitle) {
        Element titleInfo = mods("titleInfo");
        if (title != null) {
            Element titleElement = mods("title").setText(title);
            titleInfo.addContent(titleElement);
        }
        if (subtitle != null) {
            Element subtitleElement = mods("subtitle").setText(subtitle);
            titleInfo.addContent(subtitleElement);
        }
        return titleInfo;
    }

    /**
     * Creates a new mods:languageTerm element. The passed iso639-1 language code is converted to a iso-639-2b code as
     * required by the zvdd profile.
     *
     * @param iso6391Code the iso-639-1 language code
     * @return a new mods:languageTerm element
     */
    public static Element modsLanguage(String iso6391Code) {
        LanguageCode languageCode = LanguageCode.getByCode(iso6391Code);
        LanguageAlpha3Code alpha3B = languageCode.getAlpha3().getAlpha3B();
        Element languageTerm = mods("languageTerm")
            .setAttribute("authority", "iso639-2b")
            .setAttribute("type", "code")
            .setText(alpha3B.name());
        return mods("language").addContent(languageTerm);
    }

    /**
     * Creates a new mods identifier element with the given name, type and value. The source attribute will not be set.
     *
     * @param name the name of the element, usually "identifer" or "recordIdentfier"
     * @param type the type attribute
     * @param value the value of the identifier
     * @return an mods identifier element
     */
    public static Element modsIdentifier(String name, String type, String value) {
        return modsIdentifier(name, type, value, null);
    }

    /**
     * Creates a new mods identifier element with the given name, type value and source.
     *
     * @param name the name of the element, usually "identifer" or "recordIdentfier"
     * @param type the type attribute
     * @param value the value of the identifier
     * @param source the source attribute
     * @return an mods identifier element
     */
    public static Element modsIdentifier(String name, String type, String value, String source) {
        Element identifier = mods(name);
        identifier.setAttribute("type", type);
        if (source != null) {
            identifier.setAttribute("source", source);
        }
        identifier.setText(value);
        return identifier;
    }

    /**
     * Creates a new mods:name element for the given legal entity.
     *
     * @param legalEntity the legal entity to convert to a mods:name
     * @param entityRole The journal role of the entity e.g. author. This will automatically be converted to a marc
     *                   relator role.
     * @return the mods:name element
     */
    public static Element modsName(JPLegalEntity legalEntity, String entityRole) {
        String marcRole = JPXMLFunctions.getMarcRelatorID(entityRole);
        String type = legalEntity.getType().equals(JPPerson.TYPE) ? "personal" : "corporate";
        // create
        Element name = mods("name")
            .setAttribute("type", type);
        Element role = mods("role");
        Element roleTerm = mods("roleTerm")
            .setAttribute("authority", "marcrelator")
            .setAttribute("type", "code")
            .setText(marcRole);
        Element displayForm = mods("displayForm")
            .setText(legalEntity.getTitle());
        // hierarchy
        role.addContent(roleTerm);
        name.addContent(role);
        name.addContent(displayForm);
        return name;
    }

    /**
     * Creates a new mods:part element.
     * 
     * @param order the order attribute
     * @param detailType the detail type e.g. volume or issue
     * @param detailNumber the detail number like the published date
     * @return a new mods part element
     */
    public static Element getModsPart(Integer order, String detailType, String detailNumber) {
        Element part = mods("part")
            .setAttribute("order", String.valueOf(order))
            .setAttribute("type", "host");
        Element detail = mods("detail").setAttribute("type", detailType);
        Element number = mods("number").setText(detailNumber);
        detail.addContent(number);
        part.addContent(detail);
        return part;
    }

    /**
     * Create a new dv:* element in http://dfg-viewer.de/ namespace.
     * 
     * @param name the name of the element
     * @return a new jdom element
     */
    public static Element dv(String name) {
        return new Element(name, MCRConstants.DV_NAMESPACE);
    }

}
