package fsu.jportal.mets;

import org.jdom2.Element;
import org.mycore.common.MCRConstants;

import com.neovisionaries.i18n.LanguageAlpha3Code;
import com.neovisionaries.i18n.LanguageCode;

import fsu.jportal.backend.JPLegalEntity;
import fsu.jportal.xml.JPXMLFunctions;

public abstract class ZvddXMLTools {

    public static Element mods(String name) {
        return new Element(name, MCRConstants.MODS_NAMESPACE);
    }

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

    public static Element modsLanguage(String iso6391Code) {
        LanguageCode languageCode = LanguageCode.getByCode(iso6391Code);
        LanguageAlpha3Code alpha3B = languageCode.getAlpha3().getAlpha3B();
        Element languageTerm = mods("languageTerm")
            .setAttribute("authority", "iso639-2b")
            .setAttribute("type", "code")
            .setText(alpha3B.name());
        return mods("language").addContent(languageTerm);
    }

    public static Element modsIdentifier(String name, String type, String value) {
        Element identifier = mods(name);
        identifier.setAttribute("type", type);
        identifier.setText(value);
        return identifier;
    }

    public static Element modsName(JPLegalEntity legalEntity, String entityRole, String type) {
        String marcRole = JPXMLFunctions.getMarcRelatorID(entityRole);
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

    public static Element dv(String name) {
        return new Element(name, MCRConstants.DV_NAMESPACE);
    }

}
