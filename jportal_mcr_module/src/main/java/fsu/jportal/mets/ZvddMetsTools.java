package fsu.jportal.mets;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.jdom2.Element;
import org.mycore.common.MCRConstants;
import org.mycore.datamodel.metadata.JPMetaDate;
import org.mycore.datamodel.metadata.MCRMetaLink;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectStructure;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.mets.model.MCRMETSHierarchyGenerator;
import org.mycore.mets.model.Mets;
import org.mycore.mets.model.files.FLocat;
import org.mycore.mets.model.files.File;
import org.mycore.mets.model.files.FileGrp;
import org.mycore.mets.model.files.FileSec;
import org.mycore.mets.model.sections.AmdSec;
import org.mycore.mets.model.sections.DigiprovMD;
import org.mycore.mets.model.sections.DmdSec;
import org.mycore.mets.model.sections.RightsMD;
import org.mycore.mets.model.struct.Fptr;
import org.mycore.mets.model.struct.LOCTYPE;
import org.mycore.mets.model.struct.MDTYPE;
import org.mycore.mets.model.struct.MdWrap;
import org.mycore.mets.model.struct.PhysicalDiv;
import org.mycore.mets.model.struct.PhysicalStructMap;
import org.mycore.mets.model.struct.PhysicalSubDiv;

import com.neovisionaries.i18n.LanguageAlpha3Code;
import com.neovisionaries.i18n.LanguageCode;

import fsu.jportal.backend.JPArticle;
import fsu.jportal.backend.JPContainer;
import fsu.jportal.backend.JPDerivateComponent;
import fsu.jportal.backend.JPInstitution;
import fsu.jportal.backend.JPJournal;
import fsu.jportal.backend.JPLegalEntity;
import fsu.jportal.backend.JPObjectType;
import fsu.jportal.backend.JPPeriodicalComponent;
import fsu.jportal.backend.JPPerson;
import fsu.jportal.backend.JPVolume;
import fsu.jportal.backend.mcr.JPConfig;
import fsu.jportal.backend.mcr.MetadataManager;
import fsu.jportal.util.JPComponentUtil;
import fsu.jportal.xml.JPXMLFunctions;

/**
 * Contains some useful static methods for zvdd mets generation.
 *
 * @author Matthias Eichner
 */
public abstract class ZvddMetsTools {

    public static String getPublishedDate(JPVolume volume) {
        Integer order = calculateOrder(volume);
        return volume
                .getPublishedDate()
                .map(JPMetaDate::toString)
                .orElse(String.valueOf(order));
    }

    public interface Institution {
            String getTitle();
            Optional<String> getLogoURL();
            Optional<String> getURL();
            String getID();
    }

    public static Institution wrap(JPInstitution institution){
        return new Institution() {
            @Override
            public String getTitle() {
                return institution.getTitle();
            }

            @Override
            public Optional<String> getLogoURL() {
                return institution.getLogo()
                                  .map(JPXMLFunctions::getLogoURL);
            }

            @Override
            public Optional<String> getURL() {
                return institution.getURL()
                                  .map(MCRMetaLink::getXLinkHref);
            }

            @Override
            public String getID() {
                return "RIGHTS_" + institution.getId().toString();
            }
        };
    }

    /**
     * Creates the default Institution using the mycore properties JP.Site.Owner.*. This method should only be used if
     * the journals does not specify owners.
     *
     * @return a new rightsMD object using mycore properties
     */
    public static Institution defaultInst(){
        return new Institution() {
            @Override
            public String getTitle() {
                return JPConfig.getStringOrThrow("JP.Site.Owner.label");
            }

            @Override
            public Optional<String> getLogoURL() {
                return JPConfig.getString("JP.Site.Footer.Logo.url");
            }

            @Override
            public Optional<String> getURL() {
                return JPConfig.getString("JP.Site.Owner.url")
                               .map(MCRFrontendUtil.getBaseURL()::concat);
            }

            @Override
            public String getID() {
                return "RIGHTS";
            }
        };
    }

    /**
     * Helper method to build the AmdSec for the given component.
     * 
     * @param component the component
     * @return a new AmdSec containing digiprovMD and rightsMD section
     */
    public static AmdSec createAmdSec(JPPeriodicalComponent component) {
        String id = component.getId().toString();
        AmdSec amd = new AmdSec("amd_" + id);
        amd.setDigiprovMD(createDigiprovMD(component));
        // the FIRST owner is the main owner, because there is only one rightsMD section allowed
        Institution mainOwner = component
                .getJournal()
                .getParticipants("owner")
                .stream()
                .findFirst()
                .map(JPInstitution::new)
                .map(ZvddMetsTools::wrap)
                .orElse(defaultInst());

        String licence = JPXMLFunctions.getLicence(id);
        amd.listRightsMD().add(createRightsMD(mainOwner, licence));

        return amd;
    }

    /**
     * Creates a rightsMD object for the given institution.
     *
     * @param institution the institution to convert
     * @return a new rightsMD object
     */
    public static RightsMD createRightsMD(Institution institution, String licence) {
        Element dvRights = dv("rights");
        dvRights.addContent(dv("owner").setText(institution.getTitle()));
        institution.getLogoURL()
                   .map(logoURL -> dv("ownerLogo").setText(logoURL))
                   .ifPresent(dvRights::addContent);

        institution.getURL()
                   .map(url -> dv("ownerSiteURL").setText(url))
                   .ifPresent(dvRights::addContent);

        dvRights.addContent(dv("licence").setText(licence));
        return buildRightsMD(institution.getID(), dvRights);
    }

    public static RightsMD buildRightsMD(String id, Element dvRights) {
        RightsMD rightsMD = new RightsMD(id);
        MdWrap mdWrap = new MdWrap(MDTYPE.OTHER, dvRights);
        mdWrap.setOtherMdType("DVRIGHTS");
        rightsMD.setMdWrap(mdWrap);
        return rightsMD;
    }

    public static DigiprovMD createDigiprovMD(JPPeriodicalComponent component) {
        DigiprovMD digiprovMD = new DigiprovMD("DIGIPROV");
        Element presentation = dv("presentation")
            .setText(MCRFrontendUtil.getBaseURL() + "receive/" + component.getId().toString());
        Element links = dv("links");
        links.addContent(presentation);
        MdWrap mdWrap = new MdWrap(MDTYPE.OTHER, links);
        mdWrap.setOtherMdType("DVLINKS");
        digiprovMD.setMdWrap(mdWrap);
        return digiprovMD;
    }

    /**
     * Creates a mets:dmdSec for the given article.
     *
     * @param article the article
     * @return a newly generated mets:dmdSec
     */
    public static DmdSec createDmdSec(JPArticle article) {
        DmdSec dmd = new DmdSec("dmd_" + article.getId().toString());
        Element mods = modsDefault(article, article.getTitle());
        MdWrap mdWrap = new MdWrap(MDTYPE.MODS, mods);
        dmd.setMdWrap(mdWrap);
        return dmd;
    }

    /**
     * Creates a mets:dmdSec for the given volume.
     *
     * @param volume the volume
     * @param type the type either issue or volume
     * @return a newly generated mets:dmdSec
     */
    public static DmdSec createDmdSec(JPVolume volume, String type) {
        DmdSec dmd = new DmdSec("dmd_" + volume.getId().toString());
        Element mods = modsYearOrIssue(volume, null, type);
        MdWrap mdWrap = new MdWrap(MDTYPE.MODS, mods);
        dmd.setMdWrap(mdWrap);
        return dmd;
    }

    /**
     * Helper method to create a mets:fileSec by the given files and groups.
     * 
     * @param files the files to add to the groups
     * @param groups the available file groups
     * @return a newly generated mets FileSec
     */
    public static FileSec createFileSection(List<MCRMETSHierarchyGenerator.FileRef> files, List<FileGrp> groups) {
        final FileSec fileSec = new FileSec();
        // get paths
        groups.forEach(fileSec::addFileGrp);
        // add to file sec
        ZvddMetsTools.addFilesToGroups(files, groups);
        return fileSec;
    }

    /**
     * Creates the physical struct map based on the given files and groups. The old mets is optional and can be null.
     * If there are two equal physical mets:div/@ID attributes and the oldMets is not null, then the @ORDERLABEL can be
     * reused.
     * 
     * @param files the file references
     * @param groups the file groups
     * @param oldMets the old mets, usually the derivate/mets.xml
     * @return a newly generated physical struct map
     */
    public static PhysicalStructMap createPhysicalStructMap(List<MCRMETSHierarchyGenerator.FileRef> files,
        List<FileGrp> groups, Mets oldMets) {
        String oldPhysicalDivId = oldMets.getPhysicalStructMap().getDivContainer().getId();
        final PhysicalStructMap struct = new PhysicalStructMap();
        final PhysicalDiv physicalDiv = new PhysicalDiv();
        physicalDiv.setId(oldPhysicalDivId);
        struct.setDivContainer(physicalDiv);

        // add file refs
        AtomicInteger order = new AtomicInteger(1);
        files.forEach(file -> {
            final String orderLabel = getOrderLabel(oldMets, file.toPhysId());
            final PhysicalSubDiv div = new PhysicalSubDiv(file.toPhysId(), PhysicalSubDiv.TYPE_PAGE, orderLabel);
            div.setOrder(order.getAndIncrement());
            groups.forEach(group -> {
                Fptr fptr = new Fptr(file.toFileId(group));
                div.add(fptr);
            });
            physicalDiv.add(div);
        });
        return struct;
    }

    /**
     * Returns the @ORDERLABEL of the physical div in the mets. Returns null if there is no physical div with the given
     * identifier.
     * 
     * @param mets the mets to search in
     * @param physId the physical div identifier
     * @return the order label of the physical div or null
     */
    public static String getOrderLabel(Mets mets, String physId) {
        if (mets == null || physId == null || mets.getPhysicalStructMap() == null
            || mets.getPhysicalStructMap().getDivContainer() == null) {
            return null;
        }
        PhysicalDiv divContainer = mets.getPhysicalStructMap().getDivContainer();
        PhysicalSubDiv physicalSubDiv = divContainer.get(physId);
        return physicalSubDiv != null ? physicalSubDiv.getOrderLabel() : null;
    }

    /**
     * Adds all the given files to groups.
     * 
     * @param files the files to add
     * @param groups the available groups
     */
    public static void addFilesToGroups(List<MCRMETSHierarchyGenerator.FileRef> files, List<FileGrp> groups) {
        files.forEach(fileRef -> groups.forEach(group -> {
            try {
                File file = new File(fileRef.toFileId(group), fileRef.getContentType());
                String href = fileRef.toFileHref(group);
                FLocat fLocat = new FLocat(LOCTYPE.URL, href);
                file.setFLocat(fLocat);
                group.addFile(file);
            } catch (Exception uriExc) {
                LogManager.getLogger().error("Unable to resolve path " + fileRef.getPath(), uriExc);
            }
        }));
    }

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
        List<String> titles = MetadataManager.getAncestorsAndSelf(volume.getObject()).stream()
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
        order += MetadataManager.getAncestors(volume.getObject()).stream()
            .filter(o -> !o.getId().equals(parent.getId()))
            .map(MCRObject::getStructure)
            .map(MCRObjectStructure::getChildren)
            .map(List::size).mapToInt(Integer::intValue).sum();
        return order;
    }

    /**
     * Creates a mods:mods element for the dmd section for the given periodical. This method adds:
     * 
     * <ul>
     *     <li>mods:titleInfo: using the given title string</li>
     *     <li>mods:identifier: using the identis element in the periodical (can be multiple like URN, DOI...)</li>
     *     <li>mods:name: using participants</li>
     * </ul>
     *
     * @param periodical the periodical to convert to mods
     * @param title the title of the mods
     * @return a new mods element
     */
    public static Element modsDefault(JPPeriodicalComponent periodical, String title) {
        Element mods = mods("mods");

        // identifier
        periodical.listIdenti().stream().map(text -> modsIdentifier("identifier", text.getType(), text.getText()))
            .forEach(mods::addContent);

        // title
        if (title != null) {
            mods.addContent(modsTitleInfo(title, null));
        }

        // participants
        periodical.getParticipants(JPObjectType.person).stream()
            .map(personPair -> modsName(new JPPerson(personPair.getKey()), personPair.getValue()))
            .forEach(mods::addContent);
        periodical.getParticipants(JPObjectType.jpinst).stream()
            .map(jpinstPair -> modsName(new JPInstitution(jpinstPair.getKey()), jpinstPair.getValue()))
            .forEach(mods::addContent);

        // add content
        return mods;
    }

    /**
     * Enhances the {@link #modsDefault(JPPeriodicalComponent, String)} method. Use this for year or issue based volumes.
     * 
     * <ul>
     *     <li>mods:identifer: if a derivate is given the URN will be added</li>
     *     <li>mods:recordIdentifier: for the journal as relatedItem 'host' with an optional zdb-id.</li>
     *     <li>mods:part: also for the journal 'host' to add the order of the given volume</li>
     * </ul>
     * 
     * @param volume the year volume
     * @param derivate an optional derivate, can be null
     * @param type the type of the volume, either volume or issue
     * @return a new mods element
     */
    public static Element modsYearOrIssue(JPVolume volume, JPDerivateComponent derivate, String type) {
        String title = type.equals("volume") ? null : volume.getTitle();
        Element mods = modsDefault(volume, title);
        if (derivate != null) {
            derivate.getURN().ifPresent(urn -> {
                mods.addContent(0, modsIdentifier("identifier", "urn", "urn"));
            });
        }
        JPJournal journal = volume.getJournal();
        // related item host
        Element relatedItem = mods("relatedItem").setAttribute("type", "host");
        String isil = JPConfig.getString("JP.Site.ISIL", null);
        journal.getIdenti("zdb-id")
            .map(zdbId -> modsIdentifier("identifier", "zdb", zdbId))
            .ifPresent(relatedItem::addContent);
        Element relatedRecordInfo = getModsRedordInfo(journal.getId().toString(), isil);
        relatedItem.addContent(relatedRecordInfo);
        mods.addContent(relatedItem);
        // mods:part
        Integer order = ZvddMetsTools.calculateOrder(volume);
        String number = volume.getPublishedDate().map(JPMetaDate::toString).orElse(String.valueOf(order));
        Element dateIssued = getModsDateIssued(number);
        mods.addContent(dateIssued);
        Element location = getLocation(isil);
        mods.addContent(location);
        Element part = getModsPart(number, type, number);
        mods.addContent(part);
        Element redordInfo = getModsRedordInfo(volume.getId().toString(), isil);
        mods.addContent(redordInfo);
        return mods;
    }

    private static Element getModsRedordInfo(String id, String isil) {
        Element recordIdentifier = modsIdentifier("recordIdentifier", null, id, isil);
        return mods("recordInfo").addContent(recordIdentifier);
    }

    private static Element getLocation(String isil) {
        Element location = mods("location");
        location.setAttribute("authority", "ISIL");
        Element physicalLocation = mods("physicalLocation");
        physicalLocation.addContent(isil);
        location.addContent(physicalLocation);
        return location;
    }

    private static Element getModsDateIssued(String number) {
        Element originInfo = mods("originInfo");
        originInfo.setAttribute("eventType", "publication");
        Element dateIssued = mods("dateIssued");
        dateIssued.setAttribute("encoding","iso8601");
        dateIssued.addContent(number);
        originInfo.addContent(dateIssued);
        return originInfo;
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
     * @param type the type attribute (invalid for recordIdentfier -> should be null)
     * @param value the value of the identifier
     * @param source the source attribute
     * @return an mods identifier element
     */
    public static Element modsIdentifier(String name, String type, String value, String source) {
        Element identifier = mods(name);
        if (type != null) {
            identifier.setAttribute("type", type);
        }
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
    public static Element getModsPart(String order, String detailType, String detailNumber) {
        Element part = mods("part")
            .setAttribute("order", order)
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
