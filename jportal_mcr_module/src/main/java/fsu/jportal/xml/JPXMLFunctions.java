package fsu.jportal.xml;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xpath.NodeSet;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.MCRTextResolver;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.common.xml.MCRXMLFunctions;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetaISO8601Date;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.MCRURL;
import org.mycore.mets.validator.validators.ValidationException;
import org.mycore.services.i18n.MCRTranslation;
import org.mycore.user2.MCRUserManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import fsu.jportal.mets.LLZMetsUtils;
import fsu.jportal.util.ResolverUtil;

public class JPXMLFunctions {

    private static final Logger LOGGER = LogManager.getLogger(JPXMLFunctions.class);

    private static final ThreadLocal<DocumentBuilder> BUILDER_LOCAL = new ThreadLocal<DocumentBuilder>() {
        @Override
        protected DocumentBuilder initialValue() {
            try {
                return DocumentBuilderFactory.newInstance().newDocumentBuilder();
            } catch (ParserConfigurationException pce) {
                LOGGER.error("Unable to create document builder", pce);
                return null;
            }
        }
    };

    public static String formatISODate(String isoDate, String iso639Language) throws ParseException {
        if (LOGGER.isDebugEnabled()) {
            StringBuffer sb = new StringBuffer("isoDate=");
            sb.append(isoDate).append(", iso649Language=").append(iso639Language);
            LOGGER.debug(sb.toString());
        }
        Locale locale = new Locale(iso639Language);
        SimpleDateFormat df = new SimpleDateFormat(getFormat(isoDate), locale);
        MCRMetaISO8601Date mcrdate = new MCRMetaISO8601Date();
        mcrdate.setDate(isoDate);
        Date date = mcrdate.getDate();
        return (date == null) ? "?" + isoDate + "?" : df.format(date);
    }

    public static String getUserID() {
        return MCRUserManager.getCurrentUser().getUserID();
    }

    public static String getFormat(String date) {
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
    }

    public static Document getLanguages() {
        String languagesString = MCRConfiguration.instance().getString("MCR.Metadata.Languages");
        String[] languagesArray = languagesString.split(",");
        LOGGER.debug(languagesArray);
        Document document = BUILDER_LOCAL.get().newDocument();
        Element languages = document.createElement("languages");
        document.appendChild(languages);
        for (String lang : languagesArray) {
            Element langElement = document.createElement("lang");
            langElement.setTextContent(lang);
            languages.appendChild(langElement);
        }
        return document;
    }

    /**
     * Checks if the given path exists.
     * 
     * @param path
     * @return
     */
    public static boolean resourceExist(String webResource) {
        InputStream resource = JPXMLFunctions.class.getResourceAsStream("/META-INF/resources/" + webResource);

        if (resource == null) {
            return false;
        } else {
            try {
                resource.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return true;
        }
    }

    public String resolveText(String text, String varList) {
        String[] vars = varList.split(",");
        Map<String, String> varMap = new HashMap<String, String>();
        for (String var : vars) {
            String[] split = var.split("=");
            if (split.length == 2) {
                varMap.put(split[0], split[1]);
            }
        }
        MCRTextResolver r = new MCRTextResolver(varMap);
        return r.resolve(text);
    }

    public static String getLastValidPageID() {
        String page = (String) MCRSessionMgr.getCurrentSession().get("lastPageID");
        return page == null ? "" : page;
    }

    public static String setLastValidPageID(String pageID) {
        MCRSessionMgr.getCurrentSession().put("lastPageID", pageID);
        return "";
    }

    public static int getCentury(String date) {
        try {
            return (Integer.valueOf(date.substring(0, 2)) + 1);
        } catch (Exception exc) {
            LOGGER.warn("unable to format date " + date + " to century.");
            // return default 18 century
            return 18;
        }
    }

    /**
     * Tries to format the date string to a valid solr date string. If the given
     * date could not be formatted an empty string is returned.
     * 
     * @param date date to format
     * @return
     */
    public static String formatDate(String date) {
        try {
            SimpleDateFormat format = null;
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
            return null;
        }
    }

    /**
     * Checks if the given derivate contains an uibk mets file
     * and the corresponding mycore object has no children. 
     * 
     * @return
     */
    public static boolean isMetsImportable(String derivateId) {
        boolean uibkMets = isUIBKMets(derivateId);
        boolean children = hasChildren(derivateId);
        return uibkMets && !children;
    }

    /**
     * Checks if the given derivate contains an uibk mets file
     * and the corresponding mycore object has children.
     * 
     * @param derivateId
     * @return
     */
    public static boolean isMetsConvertable(String derivateId) {
        boolean uibkMets = isUIBKMets(derivateId);
        boolean children = hasChildren(derivateId);
        return uibkMets && children;
    }

    /**
     * Checks if the given derivate contains a uibk mets file.
     * 
     * @param derivateId
     * @return
     */
    private static boolean isUIBKMets(String derivateId) {
        try {
            org.jdom2.Document mets = LLZMetsUtils.getMetsXMLasDocument(derivateId);
            LLZMetsUtils.fastCheck(mets);
            return true;
        } catch (ValidationException ve) {
            return false;
        } catch (Exception e) {
            LOGGER.error(e);
            return false;
        }
    }

    /**
     * Checks if the corresponding mycore object has children.
     * 
     * @param derivateId
     * @return true if there are children
     */
    private static boolean hasChildren(String derivateId) {
        MCRObjectID mcrDerivateId = MCRObjectID.getInstance(derivateId);
        MCRDerivate derivate = MCRMetadataManager.retrieveMCRDerivate(mcrDerivateId);
        MCRObjectID ownerID = derivate.getOwnerID();
        MCRObject mcrObject = MCRMetadataManager.retrieveMCRObject(ownerID);
        return mcrObject.getStructure().getChildren().size() > 0;
    }

    /**
     * Checks if a fq request parameter for the specified facet exist.
     * 
     * @param requestURL
     * @param facet
     * @param value
     * @return
     */
    public static boolean isFacetSelected(String requestURL, String facet, String value) {
        String query = facet + ":" + value;
        try {
            return new MCRURL(requestURL).getParameterValues("fq").contains(query);
        } catch (Exception exc) {
            LOGGER.error("Unable to parse request url " + requestURL, exc);
            return false;
        }
    }

    public static String removeFacet(String requestURL, String facet, String value) {
        String query = facet + ":" + value;
        try {
            return new MCRURL(requestURL).removeParameterValue("fq", query).toString();
        } catch (Exception exc) {
            LOGGER.error("Unable to parse request url " + requestURL, exc);
            return "";
        }
    }

    /**
     * This method encodes the url. After the encoding the url is redirectable.
     * taken from MCRServlet
     *
     * @param url
     *            the source URL
     */
    public static String encodeURL(String url) throws URISyntaxException {
        try {
            return MCRXMLFunctions.normalizeAbsoluteURL(url);
        } catch (MalformedURLException | URISyntaxException e) {
            try {
                return MCRXMLFunctions.encodeURIPath(url);
            } catch (URISyntaxException e2) {
                throw e2;
            }
        }
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
     * Returns the best published date of a dates container. The type of the date has
     * to be equals 'published' or 'published_from' and has the lowest inherited value.
     * 
     * @param dates element of dates
     * @return the published date or null
     */
    public static String getPublishedDate(Object o) {
        Element dates = (Element) ((NodeSet) o).getCurrentNode();
        List<Element> publishedList = new ArrayList<>();
        NodeList nodeList = dates.getChildNodes();
        // get all elements which are published or published_from
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (!(node instanceof Element)) {
                continue;
            }
            Element child = (Element) node;
            String type = child.getAttribute("type");
            if (!(type.equals("published") || type.equals("published_from"))) {
                continue;
            }
            publishedList.add(child);
        }
        if(publishedList.isEmpty()) {
            return null;
        }
        // sort by inherited
        Collections.sort(publishedList, new Comparator<Element>() {
            @Override
            public int compare(Element e1, Element e2) {
                String i1 = e1.getAttribute("inherited");
                String i2 = e2.getAttribute("inherited");
                if (i1.equals("") || i2.equals("")) {
                    return 0;
                }
                return Integer.compare(Integer.valueOf(i1), Integer.valueOf(i2));
            }
        });
        // get the lowest date and format it
        return formatDate(publishedList.get(0).getTextContent());
    }

}
