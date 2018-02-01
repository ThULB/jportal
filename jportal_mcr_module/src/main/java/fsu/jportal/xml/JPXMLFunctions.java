package fsu.jportal.xml;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import fsu.jportal.backend.JPComponent;
import fsu.jportal.backend.JPJournal;
import fsu.jportal.backend.JPLegalEntity;
import fsu.jportal.backend.JPPeriodicalComponent;
import fsu.jportal.util.JPComponentUtil;
import fsu.jportal.util.MetsUtil;
import fsu.jportal.util.ResolverUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.common.xml.MCRXMLFunctions;
import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryDAOFactory;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.classifications2.MCRLabel;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetaISO8601Date;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.metadata.MCRObjectUtils;
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

    public static String formatISODate(String isoDate, String iso639Language) {
        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("isoDate=" + isoDate + ", iso649Language=" + iso639Language);
            }
            Locale locale = new Locale(iso639Language);
            SimpleDateFormat df = new SimpleDateFormat(getFormat(isoDate), locale);
            MCRMetaISO8601Date mcrdate = new MCRMetaISO8601Date();
            mcrdate.setDate(isoDate);
            Date date = mcrdate.getDate();
            return (date == null) ? "?" + isoDate + "?" : df.format(date);
        } catch (Exception exc) {
            LOGGER.error("While formating date " + isoDate + " with language " + iso639Language + ".", exc);
            return "?" + isoDate + "?";
        }
    }

    public static String getUserID() {
        try {
            return MCRUserManager.getCurrentUser().getUserID();
        } catch (Exception exc) {
            LOGGER.error("Unable to get user id of current session.", exc);
            return "";
        }
    }

    public static String getFormat(String date) {
        try {
            if (date != null && !date.equals("")) {
                String split[] = date.split("-");
                switch (split.length) {
                case 1:
                    return MCRTranslation.translate("metaData.dateYear");
                case 2:
                    return MCRTranslation.translate("metaData.dateYearMonth");
                case 3:
                    return MCRTranslation.translate("metaData.dateYearMonthDay");
                }
            }
            return MCRTranslation.translate("metaData.date");
        } catch (Exception exc) {
            LOGGER.error("Unable to format date " + date + ".", exc);
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
                    LOGGER.error("Unable to close resource " + webResource + ".", ioExc);
                }
                return true;
            }
        } catch (Exception exc) {
            LOGGER.error("Unable to check if resource " + webResource + " does exists.", exc);
        }
        return false;
    }

    public static int getCentury(String date) {
        try {
            return (Integer.valueOf(date.substring(0, 2)) + 1);
        } catch (Exception exc) {
            LOGGER.error("unable to format date " + date + " to century.");
            return 18; // return default 18 century
        }
    }

    /**
     * Tries to format the date string to a valid solr date string. If the given
     * date could not be formatted an empty string is returned.
     *
     * @param date date to format
     * @return date in solr format (YYYY-MM-DDThh:mm:ssZ)
     */
    public static String formatDate(String date) {
        if (date == null) {
            return "";
        }
        try {
            SimpleDateFormat format;
            if (date.length() == 4) {
                // 4 digit year
                format = new SimpleDateFormat("yyyy");
            } else if (date.length() == 7) {
                // 7 digit year-month
                format = new SimpleDateFormat("yyyy-MM");
            } else {
                format = new SimpleDateFormat("yyyy-MM-dd");
            }
            Date solrDate = format.parse(date);
            SimpleDateFormat solrDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            return solrDateFormat.format(solrDate);
        } catch (Exception exc) {
            LOGGER.warn("Unable to formate date " + date);
            return "";
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
            LOGGER.error("Unable to check if mets.xml of " + derivateId + " is generatable.", exc);
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
            LOGGER.error("Unable to check if " + derivateId + " contains an ENMAP profile mets.xml.", exc);
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
            LOGGER.error("Unable to check if the owner of the " + derivateId + " has any children.", exc);
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
            LOGGER.error("Unable to parse request url " + requestURL, exc);
            return false;
        }
    }

    public static String rmStartParam(String requestURL) {
        try {
            URL url = new URL(requestURL);
            String query = url.getQuery();
            if (query.contains("&start=")) {
                String[] queryArray = query.split("&");
                String queryRmStartParam = Arrays.stream(queryArray).filter(q -> !q.startsWith("start="))
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
            LOGGER.error("Unable to parse request url " + requestURL, exc);
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
            LOGGER.error("Unable to retrieve title of " + id, exc);
            return null;
        }
    }

    /**
     * Returns the best published date of a dates container. The type of the date has
     * to be equals 'published' or 'published_from' and has the lowest inherited value.
     * <p>
     * This date comes in UTC and is YYYY-MM-DDThh:mm:ssZ formated. Its Solr compatible.
     * </p>
     * @param id mycore object identifier
     * @return the published date or null
     */
    public static String getPublishedDate(String id) {
        return formatDate(getPublishedISODate(id));
    }

    /**
     * Returns the best published date of a dates container. The type of the date has
     * to be equals 'published' or 'published_from' and has the lowest inherited value.
     *
     * @param id mycore object identifier
     * @return a ISO 8601 conform String using the current set format.
     */
    public static String getPublishedISODate(String id) {
        try {
            MCRObjectID mcrId = MCRObjectID.getInstance(id);
            MCRObject mcrObj = MCRMetadataManager.retrieveMCRObject(mcrId);
            List<MCRObject> ancestorsAndSelf = MCRObjectUtils.getAncestorsAndSelf(mcrObj);
            for (MCRObject obj : ancestorsAndSelf) {
                Optional<JPPeriodicalComponent> periodical = JPComponentUtil.getPeriodical(obj.getId());
                if (!periodical.isPresent()) {
                    continue;
                }
                MCRMetaISO8601Date published = periodical.get().getDate("published")
                                                         .orElse(periodical.get().getDate("published_from")
                                                                           .orElse(null));
                if (published == null) {
                    continue;
                }
                return published.getISOString();
            }
        } catch (Exception exc) {
            LOGGER.error("Unable to retrieve published date of " + id, exc);
        }
        return null;
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
            return JPComponentUtil.getPeriodical(mcrId).map(JPPeriodicalComponent::getLanguageCode).orElse(null);
        } catch (Exception exc) {
            LOGGER.error("Unable to retrieve language of " + id, exc);
            return null;
        }
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
            LOGGER.error("Unable to retrieve creator of " + id, exc);
            return null;
        }
    }

    public static String getAccessRights(String id) {
        try {
            if (MCRXMLFunctions.isWorldReadableComplete(id)) {
                return "info:eu-repo/semantics/openAccess";
            }
            return "info:eu-repo/semantics/restrictedAccess";
        } catch (Exception exc) {
            LOGGER.error("Unable to retrieve access rights of " + id, exc);
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
            LOGGER.error("Unable to retrieve publisher of " + id, exc);
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
            LOGGER.error("Unable to retrieve anchestors of " + id, exc);
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
                LOGGER.error("Unable to retrieve the order of " + id);
                return 0;
            }
            return order;
        } catch (Exception exc) {
            LOGGER.error("Unable to retrieve the order of " + id, exc);
            return 0;
        }
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
            LOGGER.error("Unable to retrieve the marc relator id of " + role, t);
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
            LOGGER.error("Unable to retrieve the marc relator text of " + role, t);
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

}
