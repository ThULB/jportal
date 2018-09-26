package fsu.jportal.xml;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import fsu.jportal.backend.JPComponent;
import fsu.jportal.backend.JPContainer;
import fsu.jportal.backend.JPDerivateComponent;
import fsu.jportal.backend.JPJournal;
import fsu.jportal.backend.JPLegalEntity;
import fsu.jportal.backend.JPObjectType;
import fsu.jportal.backend.JPPeriodicalComponent;
import fsu.jportal.backend.JPVolume;
import fsu.jportal.util.JPComponentUtil;
import fsu.jportal.util.JPDateUtil;
import fsu.jportal.util.MetsUtil;
import fsu.jportal.util.ResolverUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.input.DOMBuilder;
import org.mycore.common.MCRException;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.common.xml.MCRXMLFunctions;
import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryDAOFactory;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.classifications2.MCRLabel;
import org.mycore.datamodel.metadata.JPMetaDate;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.metadata.MCRObjectUtils;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.frontend.MCRURL;
import org.mycore.services.i18n.MCRTranslation;
import org.mycore.user2.MCRUserManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class JPXMLFunctions {

    private static final Logger LOGGER = LogManager.getLogger(JPXMLFunctions.class);

    private static final ThreadLocal<DocumentBuilder> BUILDER_LOCAL = ThreadLocal.withInitial(() -> {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException pce) {
            LOGGER.error("Unable to create document builder.", pce);
            return null;
        }
    });

    public static String getUserID() {
        try {
            return MCRUserManager.getCurrentUser().getUserID();
        } catch (Exception exc) {
            LOGGER.error("Unable to get user id of current session.", exc);
            return "";
        }
    }

    public static Document getLanguages() {
        Document document = BUILDER_LOCAL.get().newDocument();
        try {
            String languagesString = MCRConfiguration.instance().getString("MCR.Metadata.Languages");
            String[] languagesArray = languagesString.split(",");
            LOGGER.debug(languagesArray);
            Element languages = document.createElement("languages");
            document.appendChild(languages);
            for (String lang : languagesArray) {
                Element langElement = document.createElement("lang");
                langElement.setTextContent(lang);
                languages.appendChild(langElement);
            }
        } catch (Exception exc) {
            LOGGER.error("Unable to get languages. @check MCR.Metadata.Languages property.", exc);
        }
        return document;
    }

    /**
     * Checks if the given path exists.
     *
     * @param webResource the resource to check
     * @return true if the resource is found otherwise false
     */
    public static boolean resourceExist(String webResource) {
        try {
            InputStream resource = JPXMLFunctions.class.getResourceAsStream("/META-INF/resources/" + webResource);
            if (resource == null) {
                return false;
            } else {
                try {
                    resource.close();
                } catch (IOException ioExc) {
                    LOGGER.error("Unable to close resource {}.", webResource, ioExc);
                }
                return true;
            }
        } catch (Exception exc) {
            LOGGER.error("Unable to check if resource {} does exists.", webResource, exc);
        }
        return false;
    }

    public static int getCentury(String date) {
        try {
            return (Integer.valueOf(date.substring(0, 2)) + 1);
        } catch (Exception exc) {
            LOGGER.error("unable to format date {} to century.", date);
            return 18; // return default 18 century
        }
    }

    /**
     * Checks if the mets.xml can be generated.
     *
     * @param derivateId the derivate to check
     * @return true if the mets is generatable
     */
    public static boolean isMetsGeneratable(String derivateId) {
        try {
            return MetsUtil.isGeneratable(MCRObjectID.getInstance(derivateId));
        } catch (Exception exc) {
            LOGGER.error("Unable to check if mets.xml of {} is generatable.", derivateId, exc);
            return false;
        }
    }

    /**
     * Checks if the given derivate contains an uibk mets file
     * and the corresponding mycore object has no children. 
     *
     * @param derivateId the derivate to check
     * @return true if the mets is importable
     */
    public static boolean isMetsImportable(String derivateId) {
        boolean enmapMets = isENMAPMets(derivateId);
        boolean children = hasChildren(derivateId);
        return enmapMets && !children;
    }

    /**
     * Checks if the given derivate contains a ENMAP mets file.
     *
     * @param derivateId the derivate to check for the mets.xml
     * @return true if its a ENMAP mets.xml
     */
    private static boolean isENMAPMets(String derivateId) {
        try {
            org.jdom2.Document mets = MetsUtil.getMetsXMLasDocument(derivateId);
            return MetsUtil.isENMAP(mets);
        } catch (FileNotFoundException fnfe) {
            return false;
        } catch (Exception exc) {
            LOGGER.error("Unable to check if {} contains an ENMAP profile mets.xml.", derivateId, exc);
            return false;
        }
    }

    /**
     * Checks if the corresponding mycore object has children.
     *
     * @param derivateId the derivate to check
     * @return true if there are children
     */
    private static boolean hasChildren(String derivateId) {
        try {
            MCRObjectID mcrDerivateId = MCRObjectID.getInstance(derivateId);
            MCRDerivate derivate = MCRMetadataManager.retrieveMCRDerivate(mcrDerivateId);
            MCRObjectID ownerID = derivate.getOwnerID();
            MCRObject mcrObject = MCRMetadataManager.retrieveMCRObject(ownerID);
            return mcrObject.getStructure().getChildren().size() > 0;
        } catch (Exception exc) {
            LOGGER.error("Unable to check if the owner of the {} has any children.", derivateId, exc);
            return false;
        }
    }

    /**
     * Checks if a fq request parameter for the specified facet exist.
     *
     * @param requestURL the request url
     * @param facet the facet to check
     * @param value the value of the facet
     * @return if the facet with the specific value is selected
     */
    public static boolean isFacetSelected(String requestURL, String facet, String value) {
        String query = getFacetQuery(facet, value);
        try {
            return new MCRURL(requestURL).getParameterValues("fq").contains(query);
        } catch (Exception exc) {
            LOGGER.error("Unable to parse request url {}", requestURL, exc);
            return false;
        }
    }

    public static String rmStartParam(String requestURL) {
        try {
            URL url = new URL(requestURL);
            String query = url.getQuery();
            if (query.contains("&start=")) {
                String[] queryArray = query.split("&");
                String queryRmStartParam = Arrays.stream(queryArray)
                    .filter(q -> !q.startsWith("start="))
                    .collect(Collectors.joining("&"));

                return new URL(url.getProtocol(), url.getHost(), url.getPort(), url.getPath() + "?" + queryRmStartParam)
                    .toString();
            }
            return requestURL;
        } catch (MalformedURLException e) {
            return "Malformed URL: " + requestURL;
        }
    }

    private static String getFacetQuery(String facet, String value) {
        return facet + ":%22" + value + "%22";
    }

    public static String removeFacet(String requestURL, String facet, String value) {
        String query = getFacetQuery(facet, value);
        try {
            return new MCRURL(requestURL).removeParameterValue("fq", query).toString();
        } catch (Exception exc) {
            LOGGER.error("Unable to parse request url {}", requestURL, exc);
            return "";
        }
    }

    public static String getJournalTypeFacetLabel(String categoryId) {
        return ResolverUtil.getClassLabel(categoryId).orElse(categoryId + " - no Label!");
    }

    /**
     * Returns the label of the given classID.
     *
     * @param classID classification identifier
     * @return label of the classification or "undefined"
     */
    public static String getClassificationLabel(String classID) {
        return ResolverUtil.getClassLabel(classID).orElse("undefined");
    }

    /**
     * Returns the title for the given object.
     *
     * @param id mycore object identifier
     * @return the title or null
     */
    public static String getTitle(String id) {
        try {
            return JPComponentUtil.get(id).map(JPComponent::getTitle).orElse(null);
        } catch (Exception exc) {
            LOGGER.error("Unable to retrieve title of {}", id, exc);
            return null;
        }
    }

    /**
     * Returns the best published date of a dates container in ISO format. The type of the date has to be equals
     * 'published'.
     *
     * @param id mycore object identifier
     * @return a ISO 8601 conform string or null
     */
    public static String getPublishedISODate(String id) {
        try {
            MCRObjectID mcrId = MCRObjectID.getInstance(id);
            return JPComponentUtil.getPeriodical(mcrId)
                .flatMap(JPPeriodicalComponent::getPublishedDate)
                .map(JPMetaDate::getDateOrFrom)
                .map(JPDateUtil::format)
                .orElse(null);
        } catch (Exception exc) {
            LOGGER.error("Unable to retrieve published date of {}", id, exc);
        }
        return null;
    }

    /**
     * Returns the best published date for the given object. The type of the date has to be 'published'.
     * <p>
     * This date comes in UTC and is YYYY-MM-DDThh:mm:ssZ formatted. Its Solr compatible.
     * </p>
     * @param id mycore object identifier
     * @return the published date or null
     */
    public static String getPublishedSolrDate(String id) {
        try {
            MCRObjectID mcrId = MCRObjectID.getInstance(id);
            return JPComponentUtil.getPeriodical(mcrId).flatMap(JPPeriodicalComponent::getPublishedDate).map(jpDate -> {
                Temporal temporal = jpDate.getDateOrFrom();
                LocalDate localDate = JPDateUtil.startOf(temporal);
                if (localDate == null) {
                    throw new MCRException("Unable to retrieve published date of " + id + " cause there is no YEAR.");
                }
                ZonedDateTime zonedDateTime = localDate.atStartOfDay().atZone(ZoneId.systemDefault());
                return DateTimeFormatter.ISO_INSTANT.format(zonedDateTime);
            }).orElse(null);
        } catch (Throwable t) {
            LOGGER.error("Unable to retrieve published date of {}", id, t);
            return null;
        }
    }

    /**
     * Returns the best published date of an object in the solr.DateRangeField format. The type of the date has to
     * be equals 'published'.
     *
     * @param id mycore object identifier
     * @return a solr.DateRangeField format or null
     */
    public static String getPublishedSolrDateRange(String id) {
        try {
            MCRObjectID mcrId = MCRObjectID.getInstance(id);
            return JPComponentUtil.getPeriodical(mcrId).flatMap(JPPeriodicalComponent::getPublishedDate).map(jpDate -> {
                if (jpDate.getDate() != null) {
                    return JPDateUtil.format(jpDate.getDate());
                } else if (jpDate.getUntil() != null) {
                    return String.format("[%s TO %s]", JPDateUtil.format(jpDate.getFrom()),
                        JPDateUtil.format(jpDate.getUntil()));
                }
                return JPDateUtil.format(jpDate.getFrom());
            }).orElse(null);
        } catch (Throwable t) {
            LOGGER.error("Unable to retrieve published date of {}", id, t);
            return null;
        }
    }

    /**
     * Formats a solr.DateRangeField date to human readable format.
     *
     * @param solrDate the solr date
     * @param iso639Language the language
     * @return human readable formatted date
     */
    public static String formatSolrDate(String solrDate, String iso639Language) {
        try {
            if (!solrDate.contains("TO")) {
                return formatDate(solrDate, iso639Language);
            }
            String split[] = solrDate.split(" TO ");
            String from = split[0].substring(1);
            String until = split[1].substring(0, split[1].length() - 1);
            Locale locale = new Locale(iso639Language);
            String untilText = MCRTranslation.translate("metaData.date.until", locale);
            return String.format("%s %s %s", formatDate(from, iso639Language), untilText,
                formatDate(until, iso639Language));
        } catch (Throwable t) {
            LOGGER.error("Unable to format solr date {}", solrDate, t);
            return "";
        }
    }

    /**
     * Formats the given iso date with the given language.
     *
     * @param isoDate the date to format e.g. 2010-11-01
     * @param iso639Language the language
     * @return formatted date
     */
    public static String formatDate(String isoDate, String iso639Language) {
        try {
            String format = JPDateUtil.getSimpleFormat(isoDate);
            return MCRXMLFunctions.formatISODate(isoDate, format, iso639Language);
        } catch (Throwable t) {
            LOGGER.error("Unable to format date {} with language {}", isoDate, iso639Language, t);
            return "";
        }
    }

    /**
     * Formats the given jpmetadate node with the given language.
     *
     * @param node the jp meta date node
     * @param iso639Language the language
     * @return formatted date
     */
    public static String formatJPMetaDate(org.w3c.dom.Node node, String iso639Language) {
        try {
            DOMBuilder domBuilder = new DOMBuilder();
            org.jdom2.Element dateElement = domBuilder.build((Element) node);
            JPMetaDate metaDate = new JPMetaDate();
            metaDate.setFromDOM(dateElement);
            return JPDateUtil.prettify(metaDate, iso639Language);
        } catch (Throwable t) {
            LOGGER.error("Unable to format date", t);
            return "";
        }
    }

    /**
     * Returns the ISO 639-1 language of the given object. If the object itself does not have its own language metadata,
     * then the parents are checked.
     *
     * @param id the object identifier to get the language of
     * @return the language in ISO 639
     */
    public static String getLanguage(String id) {
        try {
            MCRObjectID mcrId = MCRObjectID.getInstance(id);
            return JPComponentUtil.getPeriodical(mcrId).flatMap(JPPeriodicalComponent::getLanguageCode).orElse(null);
        } catch (Exception exc) {
            LOGGER.error("Unable to retrieve language of {}", id, exc);
            return null;
        }
    }

    /**
     * Returns the name of the template for the given periodical object identifier.
     *
     * @param id the mycore object identifier
     * @return name of the template
     */
    public static String getNameOfTemplate(String id) {
        try {
            Optional<JPPeriodicalComponent> periodical = JPComponentUtil.getPeriodical(MCRObjectID.getInstance(id));
            if (periodical.isPresent()) {
                return periodical.get().getNameOfTemplate();
            }
        } catch (Exception exc) {
            LOGGER.error("Unable to get name of template for object {}. Return default template.", id, exc);
        }
        return "template_default";
    }

    /**
     * Returns the author or the published of the given periodical component.
     *
     * @param id the identifier of the object
     * @return the author or publisher as string
     */
    public static String getCreator(String id) {
        try {
            MCRObjectID mcrId = MCRObjectID.getInstance(id);
            Optional<JPLegalEntity> creator = JPComponentUtil.getPeriodical(mcrId)
                .flatMap(JPPeriodicalComponent::getCreator);
            return creator.map(JPLegalEntity::getTitle).orElse(null);
        } catch (Exception exc) {
            LOGGER.error("Unable to retrieve creator of {}", id, exc);
            return null;
        }
    }

    /**
     * Returns the logo URL for the given image.
     * 
     * @param image the image e.g. thulb.svg
     * @return the full url using the JP.Site.Logo.Proxy.url property
     */
    public static String getLogoURL(String image) {
        String baseURL = MCRFrontendUtil.getBaseURL();
        String logoPath = MCRConfiguration.instance().getString("JP.Site.Logo.Proxy.url");
        return baseURL + logoPath + image;
    }

    public static String getAccessRights(String id) {
        try {
            if (MCRXMLFunctions.isWorldReadableComplete(id)) {
                return "info:eu-repo/semantics/openAccess";
            }
            return "info:eu-repo/semantics/restrictedAccess";
        } catch (Exception exc) {
            LOGGER.error("Unable to retrieve access rights of {}", id, exc);
            return null;
        }
    }

    /**
     * Returns the publisher of the given component.
     *
     * @param id the identifier of the object
     * @return the publisher
     */
    public static String getPublisher(String id) {
        try {
            MCRObjectID mcrId = MCRObjectID.getInstance(id);
            Optional<JPLegalEntity> publisher = JPComponentUtil.getPeriodical(mcrId)
                .map(JPPeriodicalComponent::getJournal)
                .flatMap(JPJournal::getCreator);
            return publisher.map(JPLegalEntity::getTitle).orElse(null);
        } catch (Exception exc) {
            LOGGER.error("Unable to retrieve publisher of {}", id, exc);
            return null;
        }
    }

    /**
     * Returns a path to the ancestors of the mycore object.
     * <p>
     * Looks like "jportal_jpjournal_00000001/jportal_jpvolume_00000001/jportal_jpvolume_00000002/"
     * </p>
     *
     * @param id id of the object
     * @return path to the ancestor
     */
    public static String getAncestorPath(String id) {
        try {
            StringBuilder path = new StringBuilder();
            MCRObjectID mcrId = MCRObjectID.getInstance(id);
            MCRObject mcrObj = MCRMetadataManager.retrieveMCRObject(mcrId);
            List<MCRObject> ancestorsAndSelf = MCRObjectUtils.getAncestors(mcrObj);
            Collections.reverse(ancestorsAndSelf);
            ancestorsAndSelf.forEach(obj -> path.append(obj.getId()).append("/"));
            return path.toString();
        } catch (Exception exc) {
            LOGGER.error("Unable to retrieve anchestors of {}", id, exc);
            return null;
        }
    }

    /**
     * Return the position in parent for the given id.
     *
     * @param id the object identifier
     * @return the order
     */
    public static Integer getOrder(String id) {
        try {
            MCRObjectID mcrId = MCRObjectID.getInstance(id);
            Integer order = JPComponentUtil.getOrder(mcrId);
            if (order == null) {
                LOGGER.error("Unable to retrieve the order of {}", id);
                return 0;
            }
            return order;
        } catch (Exception exc) {
            LOGGER.error("Unable to retrieve the order of {}", id, exc);
            return 0;
        }
    }

    /**
     * Returns the page which should be selected when going back from an object to its parent view.
     *
     * @param parentID the parent object
     * @param objectType the object type e.g. jparticle or jpvolume
     * @param referer the referer mycore object identifier
     * @param rows the amount of rows
     * @return the page to select
     */
    public static int getRefererStart(String parentID, String objectType, String referer, int rows) {
        Optional<JPContainer> containerOptional = JPComponentUtil.getContainer(MCRObjectID.getInstance(parentID));
        if (containerOptional.isPresent()) {
            JPContainer container = containerOptional.get();
            List<MCRObjectID> children = container.getChildren(JPObjectType.valueOf(objectType));
            int positionInParent = children.indexOf(MCRObjectID.getInstance(referer));
            return (int) Math.floor(positionInParent / rows) * rows;
        }
        return 0;
    }

    /**
     * Returns the marc relator identifier for a given jportal role.
     *
     * @param role the jportal role, ('author' would return 'aut')
     * @return the closet marc relator id
     */
    public static String getMarcRelatorID(String role) {
        try {
            return getMarcRelatorCategoryLabel(role).map(MCRLabel::getText).orElse("oth");
        } catch (Throwable t) {
            LOGGER.error("Unable to retrieve the marc relator id of {}", role, t);
            return "oth";
        }
    }

    /**
     * Returns the marc relator text for a given jportal role.
     *
     * @param role the jportal role, e.g. ('author' would return 'Author')
     * @return the closet marc relator text
     */
    public static String getMarcRelatorText(String role) {
        try {
            return getMarcRelatorCategoryLabel(role).map(MCRLabel::getDescription).orElse("Other");
        } catch (Throwable t) {
            LOGGER.error("Unable to retrieve the marc relator text of {}", role, t);
            return "Other";
        }
    }

    /**
     * Heler method to parse the jportal_class_00000007 and returning the mrl label.
     *
     * @param role the jportal role
     * @return the label
     */
    private static Optional<MCRLabel> getMarcRelatorCategoryLabel(String role) {
        MCRCategoryID categoryID = MCRCategoryID.fromString("jportal_class_00000007:" + role);
        MCRCategory category = MCRCategoryDAOFactory.getInstance().getCategory(categoryID, 0);
        return category.getLabel("mrl");
    }

    /**
     * Checks if the given classID is excluded by the property JP.Exclude.Facet.
     *
     * @param classID the classification identifier to check
     * @return true if its excluded, otherwise false
     */
    public static boolean isExcludedFacet(String classID) {
        try {
            String[] classIDs = MCRConfiguration.instance().getString("JP.Exclude.Facet", "").split(",");
            return Stream.of(classIDs).anyMatch(classID::contains);
        } catch (Throwable t) {
            LOGGER.error("Unable to if facet is excluded {}", classID, t);
            return false;
        }
    }

    /**
     * Returns the image/pdf link for a given periodical object. This is either a derivate link or the mainDoc
     * of the derivate. The returned string is a combination between the derivate '/' maindoc e.g.
     * jportal_derivate_00000001/myimage.tif
     *
     * @param id the mycore object id.
     * @return the main derivate file
     */
    public static String getMainFile(String id) {
        try {
            Optional<JPPeriodicalComponent> periodicalOptional = JPComponentUtil.getPeriodical(
                MCRObjectID.getInstance(id));
            if (!periodicalOptional.isPresent()) {
                return "";
            }
            JPPeriodicalComponent periodical = periodicalOptional.get();
            String link = periodical.getDerivateLink();
            if (link == null) {
                link = periodical.getFirstDerivate().map(JPDerivateComponent::getMainDocAsLink).orElse(null);
            }
            return link;
        } catch (Throwable t) {
            LOGGER.error("Unable to get main file of {}", id, t);
        }
        return "";
    }

    /**
     * Returns a link to the MCRTileCombineServlet for the given object in the given resolution.
     *
     * @param id the mycore object identifier
     * @param resolution MIN | MID | MAX
     * @return link to the MCRTileCombineServlet with the preview picture
     */
    public static String getThumbnail(String id, String resolution) {
        try {
            String mainFile = getMainFile(id);
            String mimeType = getMimeTypeForThumbnail(mainFile);
            String baseURL = MCRFrontendUtil.getBaseURL();

            if (mimeType.startsWith("image/")) {
                return baseURL + "servlets/MCRTileCombineServlet/" + resolution + "/" + mainFile;
            }
            return baseURL + "servlets/MCRFileNodeServlet/" + mainFile;
        } catch (Throwable t) {
            LOGGER.error("Unable to get thumbnail of {}", id, t);
        }
        return "";
    }

    /**
     * Returns the appropriate mime type for the given file. For image's this will always return "image/jpeg"
     * because the MCRTileCombineServlet will serve them.
     *
     * @param file the file to check in the format of jportal_derivate_00000001/image.tif
     * @return the mime type
     */
    public static String getMimeTypeForThumbnail(String file) {
        String mimeType = MCRXMLFunctions.getMimeType(file);
        return mimeType.startsWith("image/") ? "image/jpeg" : mimeType;
    }

    /**
     * Returns the version of this jportal installation.
     *
     * @return "artifact id" "version" "timestamp"
     */
    public static String getJPortalVersion() {
        return getVersion(JPXMLFunctions.class, "MCR-Artifact-Id", "version", "timestamp");
    }

    /**
     * Returns the version of this mycore installation.
     *
     * @return "artifact id" "version" "timestamp"
     */
    public static String getMCRVersion() {
        return getVersion(MCRObject.class, "MCR-Artifact-Id", "Implementation-Version", null);
    }

    public static String getVersion(Class<?> clazz, String artifactKey, String versionKey, String timestampKey) {
        String errorMessage = "unable to determine version of " + clazz.getSimpleName();
        try {
            String className = clazz.getSimpleName() + ".class";
            String classPath = clazz.getResource(className).toString();
            if (!classPath.startsWith("jar")) {
                return errorMessage;
            }
            String manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1) + "/META-INF/MANIFEST.MF";
            Manifest manifest = new Manifest(new URL(manifestPath).openStream());
            Attributes attr = manifest.getMainAttributes();
            String artifact = artifactKey != null ? attr.getValue(artifactKey) : "";
            String version = versionKey != null ? attr.getValue(versionKey) : "";
            String timestamp = timestampKey != null ? attr.getValue(timestampKey) : "";
            return String.format("%s %s %s", artifact, version, timestamp);
        } catch (Exception exc) {
            LOGGER.error(errorMessage, exc);
        }
        return errorMessage;
    }

}
