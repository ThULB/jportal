/*
 * 
 * $Revision: 14994 $ $Date: 2009-03-24 13:01:57 +0100 (Di, 24. MÃ€r 2009) $
 *
 * This file is part of ***  M y C o R e  ***
 * See http://www.mycore.de/ for details.
 *
 * This program is free software; you can use it, redistribute it
 * and / or modify it under the terms of the GNU General Public License
 * (GPL) as published by the Free Software Foundation; either version 2
 * of the License or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program, in a file called gpl.txt or license.txt.
 * If not, write to the Free Software Foundation Inc.,
 * 59 Temple Place - Suite 330, Boston, MA  02111-1307 USA
 */

package org.mycore.common.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.DOMBuilder;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.jdom.transform.JDOMSource;
import org.mycore.access.MCRAccessManager;
import org.mycore.common.MCRCache;
import org.mycore.common.MCRConfiguration;
import org.mycore.common.MCRException;
import org.mycore.common.MCRPersistenceException;
import org.mycore.common.MCRSession;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.MCRUsageException;
import org.mycore.common.MCRUtils;
import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryDAO;
import org.mycore.datamodel.classifications2.MCRCategoryDAOFactory;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.classifications2.utils.MCRCategoryTransformer;
import org.mycore.datamodel.common.MCRXMLTableManager;
import org.mycore.datamodel.ifs.MCRDirectoryXML;
import org.mycore.datamodel.ifs.MCRFile;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.services.fieldquery.MCRQuery;
import org.mycore.services.fieldquery.MCRQueryClient;
import org.mycore.services.fieldquery.MCRQueryManager;
import org.mycore.services.fieldquery.MCRResults;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * Reads XML documents from various URI types. This resolver is used to read
 * DTDs, XML Schema files, XSL document() usages, xsl:include usages and MyCoRe
 * Editor include declarations. DTDs and Schema files are read from the
 * CLASSPATH of the application when XML is parsed. XML document() calls and
 * xsl:include calls within XSL stylesheets can be read from URIs of type
 * resource, webapp, file, session, query or mcrobject. MyCoRe editor include
 * declarations can read XML files from resource, webapp, file, session, http or
 * https, query, or mcrobject URIs.
 * 
 * @author Frank Luetzenkirchen
 * @author Thomas Scheffler (yagee)
 */
public final class MCRURIResolver implements javax.xml.transform.URIResolver, EntityResolver {
    private static final Logger LOGGER = Logger.getLogger(MCRURIResolver.class);

    private static Map<String, MCRResolver> SUPPORTED_SCHEMES;

    private static final String CONFIG_PREFIX = "MCR.URIResolver.";

    private static final MCRResolverProvider EXT_RESOLVER = getExternalResolverProvider();

    private static final MCRURIResolver singleton = new MCRURIResolver();

    private static ServletContext context;

    final static String SESSION_OBJECT_NAME = "URI_RESOLVER_DEBUG";

    private MCRCache bytesCache;

    /**
     * Creates a new MCRURIResolver
     */
    private MCRURIResolver() {
        MCRConfiguration config = MCRConfiguration.instance();
        int cacheSize = config.getInt(CONFIG_PREFIX + "StaticFiles.CacheSize", 100);
        bytesCache = new MCRCache(cacheSize, "URIResolver Resources");
        SUPPORTED_SCHEMES = Collections.unmodifiableMap(getResolverMapping());
    }

    private static final MCRResolverProvider getExternalResolverProvider() {
        String externalClassName = MCRConfiguration.instance().getString(CONFIG_PREFIX + "ExternalResolver.Class", null);
        final MCRResolverProvider emptyResolver = new MCRResolverProvider() {
            public Map<String, MCRResolver> getResolverMapping() {
                return new HashMap<String, MCRResolver>();
            }
        };
        if (externalClassName == null) {
            return emptyResolver;
        }
        try {
            Class<?> cl = Class.forName(externalClassName);
            final MCRResolverProvider resolverProvider = (MCRResolverProvider) cl.newInstance();
            return resolverProvider;
        } catch (ClassNotFoundException e) {
            LOGGER.warn("Could not find external Resolver class", e);
            return emptyResolver;
        } catch (InstantiationException e) {
            LOGGER.warn("Could not instantiate external Resolver class", e);
            return emptyResolver;
        } catch (IllegalAccessException e) {
            LOGGER.warn("Could not instantiate external Resolver class", e);
            return emptyResolver;
        }
    }

    private HashMap<String, MCRResolver> getResolverMapping() {
        final Map<String, MCRResolver> extResolverMapping = EXT_RESOLVER.getResolverMapping();
        extResolverMapping.putAll(new MCRModuleResolverProvider().getResolverMapping());
        // set Map to final size with loadfactor: full
        HashMap<String, MCRResolver> supportedSchemes = new HashMap<String, MCRResolver>(10 + extResolverMapping.size(), 1);
        // don't let interal mapping be overwritten
        supportedSchemes.putAll(extResolverMapping);
        supportedSchemes.put("webapp", new MCRWebAppResolver());
        supportedSchemes.put("file", new MCRFileResolver());
        supportedSchemes.put("ifs", new MCRIFSResolver());
        supportedSchemes.put("mcrfile", new MCRMCRFileResolver());
        supportedSchemes.put("mcrobject", new MCRObjectResolver());
        supportedSchemes.put("mcrws", new MCRWSResolver());
        supportedSchemes.put("http", new MCRHttpResolver());
        supportedSchemes.put("request", new MCRRequestResolver());
        supportedSchemes.put("session", new MCRSessionResolver());
        supportedSchemes.put("access", new MCRACLResolver());
        supportedSchemes.put("resource", new MCRResourceResolver());
        supportedSchemes.put("localclass", new MCRLocalClassResolver());
        supportedSchemes.put("classification", new MCRClassificationResolver());
        supportedSchemes.put("query", new MCRQueryResolver());
        supportedSchemes.put("buildxml", new MCRBuildXMLResolver());
        supportedSchemes.put("notnull", new MCRNotNullResolver());
        supportedSchemes.put("xslStyle", new MCRXslStyleResolver());
        supportedSchemes.put("xslInclude", new MCRXslIncludeResolver());
        return supportedSchemes;
    }

    /**
     * Returns the MCRURIResolver singleton
     */
    public static MCRURIResolver instance() {
        return singleton;
    }

    /**
     * Initializes the MCRURIResolver for servlet applications.
     * 
     * @param ctx
     *            the servlet context of this web application
     * @param webAppBase
     *            the base URL of this web application
     */
    public static synchronized void init(ServletContext ctx, String webAppBase) {
        context = ctx;
        LOGGER.debug("parameter webAppBase (" + webAppBase + ") will not be used");
        // FIXME: use webAppBase or remove it
    }

    /**
     * URI Resolver that resolves XSL document() or xsl:include calls.
     * 
     * @see javax.xml.transform.URIResolver
     */
    public Source resolve(String href, String base) throws TransformerException {
        if (LOGGER.isDebugEnabled()) {
            if (base != null) {
                final String baseFileName = getFileName(base);
                LOGGER.debug("Including " + href + " from " + baseFileName);
                addDebugInfo(href, baseFileName);
            } else {
                LOGGER.debug("Including " + href);
                addDebugInfo(href, null);
            }
        }
        if (href.indexOf(":") == -1) {
            return tryResolveXSL(href);
        }

        String scheme = getScheme(href);

        if (SUPPORTED_SCHEMES.containsKey(scheme)) {
            try {
                return new JDOMSource(resolveURI(href));
            } catch (Exception e) {
                LOGGER.error("Error while resolving: " + href, e);
                throw new TransformerException("Error while resolving: " + href, e);
            }
        }
        return null;
    }

    private Source tryResolveXSL(String href) {
        if (href.endsWith(".xsl")) {
            final String resourceName = "xsl/" + href;
            try {
                LOGGER.debug("Trying to resolve " + href + " from resource " + resourceName);
                return new JDOMSource(SUPPORTED_SCHEMES.get("resource").resolveElement(resourceName));
            } catch (Exception e) {
                LOGGER.error("could not find Stylesheet " + href + " in resource " + resourceName);
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private void addDebugInfo(String href, String base) {
        MCRURIResolverFilter.uriList.get().add(href + " from " + base);
    }

    /**
     * Implements the SAX EntityResolver interface. This resolver type is used
     * to read DTDs and XML Schema files when parsing XML documents. This
     * resolver searches such files in the CLASSPATH of the current application.
     * 
     * @see org.xml.sax.EntityResolver
     */
    public InputSource resolveEntity(String publicId, String systemId) throws java.io.IOException {
        LOGGER.debug("Resolving " + publicId + " :: " + systemId);

        if (systemId == null) {
            return null; // Use default resolver
        }

        if (systemId.length() == 0) {
            // if you overwrite SYSTEM by empty String in XSL
            return new InputSource(new StringReader(""));
        }

        InputStream is = getCachedResource("/" + getFileName(systemId));

        if (is == null) {
            return null; // Use default resolver
        }

        LOGGER.debug("Reading " + getFileName(systemId));

        return new InputSource(is);
    }

    /**
     * Returns the filename part of a path.
     * 
     * @param path
     *            the path of a file
     * @return the part after the last / or \\
     */
    private String getFileName(String path) {
        int posA = path.lastIndexOf("/");
        int posB = path.lastIndexOf("\\");
        int pos = ((posA == -1) ? posB : posA);

        return ((pos == -1) ? path : path.substring(pos + 1));
    }

    private InputStream getCachedResource(String classResource) throws IOException {
        byte[] bytes = (byte[]) (bytesCache.get(classResource));

        if (bytes == null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            LOGGER.debug("Resolving resource " + classResource);
            InputStream in = this.getClass().getResourceAsStream(classResource);

            if (in == null) {
                LOGGER.debug(classResource + " not found");
                return null;
            }

            MCRUtils.copyStream(in, baos);
            baos.close();
            in.close();
            bytes = baos.toByteArray();
            bytesCache.put(classResource, bytes);
        }

        return new ByteArrayInputStream(bytes);
    }

    /**
     * Reads XML from URIs of various type.
     * 
     * @param uri
     *            the URI where to read the XML from
     * @return the root element of the XML document
     */
    public Element resolve(String uri) {
        if (LOGGER.isDebugEnabled()) {
            addDebugInfo(uri, "JAVA method invocation");
        }
        try {
            /**
             * rethrow Exception as RuntimException TODO: need to refactor this
             * and declare throw in method signature
             */
            return resolveURI(uri);
        } catch (Exception e) {
            throw new MCRException("Error while resolving: " + uri, e);
        }
    }

    private Element resolveURI(String uri) throws Exception {
        LOGGER.debug("Reading xml from uri " + uri);
        String scheme = getScheme(uri);
        return getResolver(scheme).resolveElement(uri);
    }

    /**
     * Returns the protocol or scheme for the given URI.
     * 
     * @param uri
     *            the URI to parse
     * @return the protocol/scheme part before the ":"
     */
    public String getScheme(String uri) {
        return new StringTokenizer(uri, ":").nextToken();
    }

    MCRResolver getResolver(String scheme) {
        if (SUPPORTED_SCHEMES.containsKey(scheme)) {
            return SUPPORTED_SCHEMES.get(scheme);
        }
        String msg = "Unsupported scheme type: " + scheme;
        throw new MCRUsageException(msg);
    }

    /**
     * Reads xml from an InputStream and returns the parsed root element.
     * 
     * @param in
     *            the InputStream that contains the XML document
     * @return the root element of the parsed input stream
     * @throws IOException
     * @throws JDOMException
     */
    protected Element parseStream(InputStream in) throws JDOMException, IOException {
        SAXBuilder builder = new SAXBuilder();
        builder.setValidation(false);
        builder.setEntityResolver(this);

        return builder.build(in).getRootElement();
    }

    /**
     * Resolver interface. use this to implement custom URI schemes.
     * 
     * @author Thomas Scheffler (yagee)
     */
    public static interface MCRResolver {
        /**
         * resolves an Element for XSLT process
         * 
         * @param URI
         *            String in URI Syntax
         * @return
         * @throws Exception
         */
        public Element resolveElement(String URI) throws Exception;
    }

    /**
     * provides a URI -- Resolver Mapping
     * 
     * One can implement this interface to provide additional URI schemes this
     * MCRURIResolver should handle, too. To add your mapping you have to set
     * the <code>MCR.URIResolver.ExternalResolver.Class</code> property to the
     * implementing class.
     * 
     * @author Thomas Scheffler
     */
    public static interface MCRResolverProvider {
        /**
         * provides a Map of Resolver mappings.
         * 
         * Key is the scheme, e.g. <code>http</code>, where value is an instance
         * of MCRResolver.
         * 
         * @see MCRResolver
         * @return a Map of Resolver mappings
         */
        public Map<String, MCRResolver> getResolverMapping();
    }

    private static class MCRModuleResolverProvider implements MCRResolverProvider {

        @SuppressWarnings("unchecked")
        public Map<String, MCRResolver> getResolverMapping() {
            Properties props = MCRConfiguration.instance().getProperties(CONFIG_PREFIX + "ModuleResolver.");
            if (props.isEmpty()) {
                return Collections.EMPTY_MAP;
            }
            Map<String, MCRResolver> map = new HashMap<String, MCRResolver>();
            for (Entry entry : props.entrySet()) {
                try {
                    String scheme = entry.getKey().toString();
                    scheme = scheme.substring(scheme.lastIndexOf('.') + 1);
                    LOGGER.info("Adding Resolver " + entry.getValue().toString() + " for URI scheme " + scheme);
                    Class cl = Class.forName(entry.getValue().toString());
                    map.put(scheme, (MCRResolver) cl.newInstance());
                } catch (Exception e) {
                    LOGGER.error("Cannot instantiate " + entry.getValue() + " for URI scheme " + entry.getKey());
                    throw new MCRException("Cannot instantiate " + entry.getValue() + " for URI scheme " + entry.getKey(), e);
                }
            }
            return map;
        }

    }

    private static class MCRObjectResolver implements MCRResolver {
        protected static final SAXBuilder SAX_BUILDER = new org.jdom.input.SAXBuilder();

        /**
         * Reads local MCRObject with a given ID from the store.
         * 
         * @param uri
         *            for example, "mcrobject:DocPortal_document_07910401"
         * @returns XML representation from MCRXMLContainer
         */
        public Element resolveElement(String uri) {
            String id = uri.substring(uri.indexOf(":") + 1);
            LOGGER.debug("Reading MCRObject with ID " + id);

            MCRObjectID mcrid = new MCRObjectID(id);
            Document doc = MCRXMLTableManager.instance().readDocument(mcrid);

            LOGGER.debug("end resolving " + uri);
            return doc.getRootElement();
        }

    }

    private static class MCRWSResolver implements MCRResolver {
        // TODO: add support for remote classifications

        private static final String HOST_KEY = "host";

        private static final String TYPE_KEY = "type";

        private static final String OPERATION_KEY = "operation";

        // parameter for MCRDoRetrieveObject
        private static final String OBJECT_KEY = "ID";

        // parameter for MCRDoRetrieveClassification
        private static final String LEVEL_KEY = "level";

        private static final String CLASS_KEY = "classid";

        private static final String CATEG_KEY = "categid";

        private static final String FORMAT_KEY = "format";

        // parameter for MCRDoRetrieveLinks
        private static final String FROM_KEY = "from";

        private static final String TO_KEY = "to";

        private static final DOMBuilder DOM_BUILDER = new DOMBuilder();

        public Element resolveElement(String uri) {
            String key = uri.substring(uri.indexOf(":") + 1);
            LOGGER.debug("Reading xml from WebService using key :" + key);

            HashMap<String, String> params = new HashMap<String, String>();
            String[] param;
            StringTokenizer tok = new StringTokenizer(key, "&");
            while (tok.hasMoreTokens()) {
                param = tok.nextToken().split("=");
                if (param.length == 1) {
                    params.put(param[0], "");
                } else {
                    params.put(param[0], param[1]);
                }
            }

            if (!params.containsKey(HOST_KEY) || !params.containsKey(OPERATION_KEY)) {
                LOGGER.warn("Either 'host' or 'operation' is not defined. Returning NULL.");
                return null;
            }
            if (params.get(OPERATION_KEY).equals("MCRDoRetrieveObject")) {
                org.w3c.dom.Document document = MCRQueryClient.doRetrieveObject(params.get(HOST_KEY), params.get(OBJECT_KEY));
                return DOM_BUILDER.build(document).detachRootElement();
            }
            if (params.get(OPERATION_KEY).equals("MCRDoRetrieveClassification")) {
                String hostAlias = params.get(HOST_KEY);
                String level = params.get(LEVEL_KEY);
                String type = params.get(TYPE_KEY);
                String classId = params.get(CLASS_KEY);
                String categId = params.get(CATEG_KEY);
                String format = params.get(FORMAT_KEY);
                org.w3c.dom.Document document = MCRQueryClient.doRetrieveClassification(hostAlias, level, type, classId, categId, format);
                return DOM_BUILDER.build(document).detachRootElement();
            }
            if (params.get(OPERATION_KEY).equals("MCRDoRetrieveLinks")) {
                String hostAlias = params.get(HOST_KEY);
                String from = params.get(FROM_KEY);
                String to = params.get(TO_KEY);
                String type = params.get(TYPE_KEY);
                org.w3c.dom.Document document = MCRQueryClient.doRetrieveLinks(hostAlias, from, to, type);
                return DOM_BUILDER.build(document).detachRootElement();
            }
            // only WS "MCRDoRetrieveObject" implemented yet
            LOGGER.warn("Unknown 'operation' requested. Returning NULL.");
            return null;
        }

    }

    private static class MCRHttpResolver implements MCRResolver {

        /**
         * Reads XML from a http or https URL.
         * 
         * @param url
         *            the URL of the xml document
         * @return the root element of the xml document
         * @throws IOException
         * @throws JDOMException
         * @throws MalformedURLException
         */
        public Element resolveElement(String url) throws MalformedURLException, JDOMException, IOException {
            LOGGER.debug("Reading xml from url " + url);

            return MCRURIResolver.instance().parseStream(new URL(url).openStream());
        }

    }

    private static class MCRRequestResolver implements MCRResolver {
        MCRResolver fallback;

        public MCRRequestResolver() {
            fallback = new MCRHttpResolver();
        }

        /**
         * Reads XML from a HTTP request to this web application.
         * 
         * @param uri
         *            the URI in the format request:path/to/servlet
         * @return the root element of the xml document
         * @throws Exception
         */
        public Element resolveElement(String uri) throws Exception {
            String path = uri.substring(uri.indexOf(":") + 1);
            LOGGER.debug("Reading xml from request " + path);

            StringBuffer url = new StringBuffer(MCRServlet.getBaseURL());
            url.append(path);

            final MCRSession currentSession = MCRSessionMgr.getCurrentSession();
            final Object httpSessionID = currentSession.get("http.session");
            final String finalURL;
            if (httpSessionID == null) {
                finalURL = url.toString();
            } else {
                finalURL = toEncoded(url.toString(), httpSessionID.toString());
            }

            return fallback.resolveElement(finalURL);
        }

        private String toEncoded(String url, String sessionId) {

            if ((url == null) || (sessionId == null)) {
                return (url);
            }
            String path = url;
            String query = "";
            int queryPos = url.indexOf('?');
            if (queryPos >= 0) {
                path = url.substring(0, queryPos);
                query = url.substring(queryPos);
            }
            StringBuffer sb = new StringBuffer(path);
            sb.append(";jsessionid=");
            sb.append(sessionId);
            sb.append(query);
            return (sb.toString());

        }
    }

    private static class MCRFileResolver implements MCRResolver {

        /**
         * A cache of parsed XML files *
         */
        final static int cacheSize = MCRConfiguration.instance().getInt(CONFIG_PREFIX + "StaticFiles.CacheSize", 100);

        private static MCRCache fileCache = new MCRCache(cacheSize, "URIResolver Files");

        /**
         * Reads XML from a file URL.
         * 
         * @param uri
         *            the URL of the file in the format file://path/to/file
         * @return the root element of the xml document
         * @throws IOException
         * @throws JDOMException
         * @throws FileNotFoundException
         * @throws URISyntaxException
         */
        public Element resolveElement(String uri) throws FileNotFoundException, JDOMException, IOException, URISyntaxException {
            URI fileURI = new URI(uri);
            File file = new File(fileURI);
            LOGGER.debug("Reading xml from file " + file.getAbsolutePath());
            Element fromCache = (Element) fileCache.getIfUpToDate(fileURI.toString(), file.lastModified());

            if (fromCache != null) {
                return (Element) (fromCache.clone());
            }

            Element parsed = MCRURIResolver.instance().parseStream(new FileInputStream(file));
            fileCache.put(fileURI.toString(), parsed);

            return (Element) (parsed.clone());
        }

    }

    private static class MCRWebAppResolver implements MCRResolver {
        MCRResolver fallback;

        public MCRWebAppResolver() {
            fallback = new MCRFileResolver();
        }

        /**
         * Reads XML from a static file within the web application.
         * 
         * @param uri
         *            the URI in the format request:path/to/servlet
         * @return the root element of the xml document
         * @throws Exception
         */
        public Element resolveElement(String uri) throws Exception {
            String path = uri.substring(uri.indexOf(":") + 1);
            LOGGER.debug("Reading xml from webapp " + path);
            File f = new File(context.getRealPath(path));
            uri = f.toURI().toString();

            return fallback.resolveElement(uri);
        }
    }

    private static class MCRResourceResolver implements MCRResolver {

        static MCRXMLResource CACHE = MCRXMLResource.instance();

        /**
         * Reads XML from the CLASSPATH of the application.
         * 
         * @param uri
         *            the location of the file in the format
         *            resource:path/to/file
         * @return the root element of the XML document
         * @throws IOException
         * @throws JDOMException
         */
        public Element resolveElement(String uri) throws JDOMException, IOException {
            String path = uri.substring(uri.indexOf(":") + 1);
            Element parsed = CACHE.getResource(path).getRootElement();
            return parsed;
        }

    }

    private static class MCRLocalClassResolver implements MCRResolver {

        /**
         * Delivers a jdom Element created by any local class that implements
         * MCRResolver
         * 
         * @param uri
         *            the class name of the file in the format
         *            localclass:org.mycore.ClassName?mode=getAll
         * 
         * @return the root element of the XML document
         * @throws Exception
         */
        public Element resolveElement(String uri) throws Exception {
            String classname = uri.substring(uri.indexOf(":") + 1, uri.indexOf("?"));
            Class<?> cl = null;
            Logger.getLogger(this.getClass()).debug("Loading Class: " + classname);
            cl = Class.forName(classname);
            Object o = cl.newInstance();
            MCRResolver resolver = (MCRResolver) o;
            return resolver.resolveElement(uri);
        }

    }

    private static class MCRSessionResolver implements MCRResolver {

        /**
         * Reads XML from URIs of type session:key. The method MCRSession.get(
         * key ) is called and must return a JDOM element.
         * 
         * @see org.mycore.common.MCRSession#get(java.lang.String )
         * 
         * @param uri
         *            the URI in the format session:key
         * @return the root element of the xml document
         */
        public Element resolveElement(String uri) {
            String key = uri.substring(uri.indexOf(":") + 1);

            LOGGER.debug("Reading xml from session using key " + key);

            Object value = MCRSessionMgr.getCurrentSession().get(key);

            return (Element) (((Element) value).clone());
        }

    }

    private static class MCRIFSResolver implements MCRResolver {

        /**
         * Reads XML from a http or https URL.
         * 
         * @param uri
         *            the URL of the xml document
         * @return the root element of the xml document
         */
        public Element resolveElement(String uri) {
            LOGGER.debug("Reading xml from url " + uri);

            String path = uri.substring(uri.indexOf(":") + 1);

            String hosts = null;
            int i = path.indexOf("?host");
            if (i > 0) {
                hosts = path.substring(i + 1 + 6);// "?host=".length()
                path = path.substring(0, i);
            }
            return MCRDirectoryXML.getInstance().getDirectory(path, hosts).getRootElement();
        }

    }

    private static class MCRMCRFileResolver implements MCRResolver {
        public Element resolveElement(String uri) throws IOException, JDOMException {
            LOGGER.debug("Reading xml from MCRFile " + uri);

            String id = uri.substring(uri.indexOf(":") + 1);

            Element ele = null;

            try {
                ele = MCRFile.getFile(id).getContentAsJDOM().detachRootElement();
            } catch (MCRPersistenceException e) {
                LOGGER.error("Cannot resolve MCRFile with id " + id);
            }

            return ele;
        }

    }

    private static class MCRACLResolver implements MCRResolver {

        private static final String ACTION_PARAM = "action";

        private static final String OBJECT_ID_PARAM = "object";

        /**
         * Returns access controll rules as XML
         */
        public Element resolveElement(String uri) {
            String key = uri.substring(uri.indexOf(":") + 1);
            LOGGER.debug("Reading xml from query result using key :" + key);

            String[] param;
            StringTokenizer tok = new StringTokenizer(key, "&");
            Hashtable<String, String> params = new Hashtable<String, String>();

            while (tok.hasMoreTokens()) {
                param = tok.nextToken().split("=");
                params.put(param[0], param[1]);
            }

            String action = params.get(ACTION_PARAM);
            String objId = params.get(OBJECT_ID_PARAM);

            if (action == null || objId == null) {
                return null;
            }

            Element container = new Element("servacls").setAttribute("class", "MCRMetaAccessRule");

            if (action.equals("all")) {
                for (String permission : MCRAccessManager.getPermissionsForID(objId)) {
                    // one pool Element under access per defined AccessRule in
                    // Pool
                    // for (Object-)ID
                    addRule(container, permission, MCRAccessManager.getAccessImpl().getRule(objId, permission));
                }
            } else {
                addRule(container, action, MCRAccessManager.getAccessImpl().getRule(objId, action));
            }

            return container;
        }

        private void addRule(Element root, String pool, Element rule) {
            if (rule != null && pool != null) {
                Element poolElement = new Element("servacl").setAttribute("permission", pool);
                poolElement.addContent(rule);
                root.addContent(poolElement);
            }
        }

    }

    private static class MCRClassificationResolver implements MCRResolver {

        private static final Pattern EDITORFORMAT_PATTERN = Pattern.compile("(\\[)([^\\]]*)(\\])");

        private static final MCRConfiguration CONFIG = MCRConfiguration.instance();

        private static final String FORMAT_CONFIG_PREFIX = CONFIG_PREFIX + "Classification.Format.";

        private static final String SORT_CONFIG_PREFIX = CONFIG_PREFIX + "Classification.Sort.";

        private static final MCRCache categoryCache = new MCRCache(CONFIG.getInt(CONFIG_PREFIX + "Classification.CacheSize", 1000),
                "URIResolver categories");

        private static final MCRCategoryDAO DAO = MCRCategoryDAOFactory.getInstance();

        public MCRClassificationResolver() {
        }

        /**
         * returns a classification in a specific format.
         * 
         * Syntax:
         * <code>classification:{editor['['formatAlias']']|metadata}:{Levels}[:noEmptyLeaves]:{parents|children}:{ClassID}[:CategID]
         * 
         * formatAlias: MCRConfiguration property MCR.UURResolver.Classification.Format.FormatAlias
         * 
         * @param uri
         *            URI in the syntax above
         * 
         * @return the root element of the XML document
         * @see ClassificationTransformer#getEditorDocument(Classification,
         *      String)
         */
        public Element resolveElement(String uri) {
            LOGGER.debug("start resolving " + uri);
            Element returns = (Element) categoryCache.getIfUpToDate(uri, getSystemLastModified());
            if (returns == null) {
                returns = getClassElement(uri);
                categoryCache.put(uri, returns);
            }
            return returns;
        }

        private Element getClassElement(String uri) {
            StringTokenizer pst = new StringTokenizer(uri, ":", true);
            if (pst.countTokens() < 9) {
                // sanity check
                throw new IllegalArgumentException("Invalid format of uri for retrieval of classification: " + uri);
            }

            pst.nextToken(); // "classification"
            pst.nextToken(); // :
            String format = pst.nextToken();
            pst.nextToken(); // :

            String levelS = pst.nextToken();
            pst.nextToken(); // :
            int levels = ("all".equals(levelS) ? -1 : Integer.parseInt(levelS));

            String axis;
            String token = pst.nextToken();
            pst.nextToken(); // :
            boolean emptyLeaves = (!("noEmptyLeaves".equals(token)));
            if (!emptyLeaves) {
                axis = pst.nextToken();
                pst.nextToken(); // :
            } else
                axis = token;

            String classID = pst.nextToken();
            StringBuffer categID = new StringBuffer();
            if (pst.hasMoreTokens()) {
                pst.nextToken(); // :
                while (pst.hasMoreTokens())
                    categID.append(pst.nextToken());
            }

            String categ;
            try {
                categ = URLDecoder.decode(categID.toString(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                categ = categID.toString();
            }
            MCRCategory cl = null;
            LOGGER.debug("categoryCache entry invalid or not found: start MCRClassificationQuery");
            if (axis.equals("children")) {
                if (categ.length() > 0) {
                    cl = DAO.getCategory(new MCRCategoryID(classID, categ), levels);
                } else {
                    cl = DAO.getCategory(MCRCategoryID.rootID(classID), levels);
                }
            } else if (axis.equals("parents")) {
                if (categ.length() == 0) {
                    LOGGER.error("Cannot resolve parent axis without a CategID. URI: " + uri);
                    throw new IllegalArgumentException(
                            "Invalid format (categID is required in mode 'parents') of uri for retrieval of classification: " + uri);
                }
                cl = DAO.getRootCategory(new MCRCategoryID(classID, categ), levels);
            }
            if (cl == null) {
                return null;
            }

            Element returns;
            LOGGER.debug("start transformation of ClassificationQuery");
            if (format.startsWith("editor")) {
                boolean sort = shouldSortCategories(classID);
                String labelFormat = getLabelFormat(format);
                if (labelFormat == null) {
                    returns = MCRCategoryTransformer.getEditorItems(cl, sort, emptyLeaves);
                } else {
                    returns = MCRCategoryTransformer.getEditorItems(cl, labelFormat, sort, emptyLeaves);
                }
            } else if (format.equals("metadata")) {
                returns = (Element) (MCRCategoryTransformer.getMetaDataDocument(cl, false).getRootElement().detach());
            } else {
                LOGGER.error("Unknown target format given. URI: " + uri);
                throw new IllegalArgumentException("Invalid target format (" + format + ") in uri for retrieval of classification: " + uri);
            }
            LOGGER.debug("end resolving " + uri);
            return returns;
        }

        private static String getLabelFormat(String editorString) {
            Matcher m = EDITORFORMAT_PATTERN.matcher(editorString);
            if ((m.find()) && (m.groupCount() == 3)) {
                String formatDef = m.group(2);
                return CONFIG.getString(FORMAT_CONFIG_PREFIX + formatDef);
            }
            return null;
        }

        private static boolean shouldSortCategories(String classId) {
            return CONFIG.getBoolean(SORT_CONFIG_PREFIX + classId, true);
        }

        private static long getSystemLastModified() {
            long xmlLastModified = MCRXMLTableManager.instance().getLastModified();
            long classLastModified = DAO.getLastModified();
            return Math.max(xmlLastModified, classLastModified);
        }

    }

    private static class MCRQueryResolver implements MCRResolver {

        private static final String QUERY_PARAM = "term";

        private static final String SORT_PARAM = "sortby";

        private static final String ORDER_PARAM = "order";

        private static final String MAXRESULTS_PARAM = "maxResults";

        /**
         * Returns query results for query in "term" parameter
         */
        public Element resolveElement(String uri) {
            String key = uri.substring(uri.indexOf(":") + 1);
            LOGGER.debug("Reading xml from query result using key :" + key);

            Hashtable<String, String> params = getParameterMap(key);

            String query;
            try {
                query = URLDecoder.decode(params.get(QUERY_PARAM), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return null;
            }
            String sortby = params.get(SORT_PARAM);
            String order = params.get(ORDER_PARAM);
            String maxResults = getMaxResults(params);

            if (query == null) {
                return null;
            }
            Document input = getQueryDocument(query, sortby, order, maxResults);
            // Execute query
            long start = System.currentTimeMillis();
            MCRResults result = MCRQueryManager.search(MCRQuery.parseXML(input));
            long qtime = System.currentTimeMillis() - start;
            LOGGER.debug("MCRSearchServlet total query time: " + qtime);
            return result.buildXML();
        }

        private static String getMaxResults(Hashtable<String, String> params) {
            String maxResults = params.get(MAXRESULTS_PARAM);
            if (maxResults != null && !maxResults.equals(""))
                return maxResults;
            return "0";
        }

        private static Document getQueryDocument(String query, String sortby, String order, String maxResults) {
            Element queryElement = new Element("query");
            queryElement.setAttribute("maxResults", maxResults);
            queryElement.setAttribute("numPerPage", "0");
            Document input = new Document(queryElement);

            Element conditions = new Element("conditions");
            queryElement.addContent(conditions);
            conditions.setAttribute("format", "text");
            conditions.addContent(query);
            org.jdom.Element root = input.getRootElement();
            if (sortby != null) {
                final Element fieldElement = new Element("field").setAttribute("name", sortby);
                if (order != null) {
                    fieldElement.setAttribute("order", order);
                }
                root.addContent(new Element("sortBy").addContent(fieldElement));
            }
            if (LOGGER.isDebugEnabled()) {
                XMLOutputter out = new XMLOutputter(org.jdom.output.Format.getPrettyFormat());
                LOGGER.debug(out.outputString(input));
            }
            return input;
        }

    }

    private static Hashtable<String, String> getParameterMap(String key) {
        String[] param;
        StringTokenizer tok = new StringTokenizer(key, "&");
        Hashtable<String, String> params = new Hashtable<String, String>();

        while (tok.hasMoreTokens()) {
            param = tok.nextToken().split("=");
            params.put(param[0], param[1]);
        }
        return params;
    }

    /**
     * Ensures that the return of the given uri is never null. When the return
     * is null, or the uri throws an exception, this resolver will return an
     * empty XML element instead.
     * 
     * Usage: notnull:<anyMyCoReURI>
     */
    private static class MCRNotNullResolver implements MCRResolver {

        public Element resolveElement(String uri) {
            String target = uri.substring(uri.indexOf(":") + 1);
            // fixes exceptions if suburi is empty like "mcrobject:"
            String subUri = target.substring(target.indexOf(":") + 1);
            if (subUri.length() == 0)
                return new Element("null");
            // end fix
            LOGGER.debug("Ensuring xml is not null: " + target);
            try {
                Element result = MCRURIResolver.instance().resolve(target);
                if (result != null)
                    return result;
                else {
                    LOGGER.debug("MCRNotNullResolver returning empty xml");
                    return new Element("null");
                }
            } catch (Exception ex) {
                LOGGER.info("MCRNotNullResolver caught exception: " + ex.getLocalizedMessage());
                LOGGER.debug(ex.getStackTrace());
                LOGGER.debug("MCRNotNullResolver returning empty xml");
                return new Element("null");
            }
        }
    }

    /**
     * Transform result of other resolver with stylesheet.
     * 
     * Usage:
     * xslStyle:<stylesheet><?param1=value1<&param2=value2>>:<anyMyCoReURI>
     * 
     * To <stylesheet> is extension .xsl added. File is searched in classpath.
     * 
     */
    private static class MCRXslStyleResolver implements MCRResolver {

        public Element resolveElement(String uri) {
            String help = uri.substring(uri.indexOf(":") + 1);
            String stylesheet = new StringTokenizer(help, ":").nextToken();
            String target = help.substring(help.indexOf(":") + 1);

            String subUri = target.substring(target.indexOf(":") + 1);
            if (subUri.length() == 0)
                return new Element("null");

            try {
                Hashtable<String, String> params = null;
                StringTokenizer tok = new StringTokenizer(stylesheet, "?");
                stylesheet = tok.nextToken();

                if (tok.hasMoreTokens()) {
                    params = getParameterMap(tok.nextToken());
                }

                Element result = MCRURIResolver.instance().resolve(target);
                if (result != null) {
                    Document doc = result.getDocument();
                    if (doc == null)
                        doc = new Document(result);
                    Document xx = MCRLayoutService.instance().doLayout(doc, "xsl/" + stylesheet + ".xsl", params);
                    if (!xx.hasRootElement()) {
                        LOGGER.info("MCRXslStyleResolver no root element after transformation ");
                        return new Element("null");
                    }
                    LOGGER.debug("MCRXslStyleResolver root element after transformation is " + xx.getRootElement().getName());
                    return xx.getRootElement();
                } else {
                    LOGGER.debug("MCRXslStyleResolver returning empty xml");
                    return new Element("null");
                }
            } catch (Exception ex) {
                LOGGER.info("MCRXslStyleResolver caught exception: " + ex.getLocalizedMessage());
                LOGGER.debug(ex.getStackTrace());
                LOGGER.debug("MCRXslStyleResolver returning empty xml");
                return new Element("null");
            }
        }
    }

    /**
     * <p>Includes xsl files which are set in the mycore.properties file.</p>
     * Example:
     *      MCR.URIResolver.xslIncludes.components=iview.xsl,wcms.xsl
     *      
     * @return
     *          A xsl file with the includes as href.
     */
    private static class MCRXslIncludeResolver implements MCRResolver {

        public Element resolveElement(String uri) throws Exception {
            String includePart = uri.substring(uri.indexOf(":") + 1);
            Namespace xslNamespace = Namespace.getNamespace("xsl", "http://www.w3.org/1999/XSL/Transform");

            Element root = new Element("stylesheet", xslNamespace);
            root.setAttribute("version", "1.0");

            // get the parameters from mycore.properties
            Properties props = MCRConfiguration.instance().getProperties("MCR.URIResolver.xslIncludes." + includePart);
            for(Object o : props.values()) {
                String propValue = (String)o;
                String[] includes = propValue.split(",");
                for(String include : includes) {
                    // create a new include element
                    Element includeElement = new Element("include", xslNamespace);
                    includeElement.setAttribute("href", include);
                    root.addContent(includeElement);
                }
            }
            return root;
        }
    }

    /**
     * Builds XML trees from a string representation. Multiple XPath expressions
     * can be separated by &amp;
     * 
     * Example:
     * 
     * buildxml:_rootName_=mycoreobject&metadata/parents/parent/@href=
     * 'FooBar_Document_4711'
     * 
     * This will return:
     * 
     * <mycoreobject> <metadata> <parents> <parent href="'FooBar_Document_4711'"
     * /> </parents> </metadata> </mycoreobject>
     */
    private static class MCRBuildXMLResolver implements MCRResolver {

        /**
         * Builds a simple xml node tree on basis of name value pair
         */
        public Element resolveElement(String uri) {
            String key = uri.substring(uri.indexOf(":") + 1);
            LOGGER.debug("Reading xml from query result using key :" + key);

            Hashtable<String, String> params = getParameterMap(key);
            String baseElement = params.get("_rootName_");
            if (baseElement == null) {
                baseElement = "uriTemp";
            } else {
                params.remove("_rootName_");
            }
            Element returns = new Element(baseElement);
            for (Map.Entry<String, String> entry : params.entrySet()) {
                constructElement(returns, entry.getKey(), entry.getValue());
            }
            if (returns.getName().equals("uriTemp")) {
                if (returns.getChildren().size() > 1) {
                    LOGGER.warn("More than 1 root node defined, returning one");
                    return (Element) ((Element) returns.getChildren().get(0)).detach();
                }
            }
            return returns;
        }

        private static Hashtable<String, String> getParameterMap(String key) {
            String[] param;
            StringTokenizer tok = new StringTokenizer(key, "&");
            Hashtable<String, String> params = new Hashtable<String, String>();

            while (tok.hasMoreTokens()) {
                param = tok.nextToken().split("=");
                try {
                    params.put(URLDecoder.decode(param[0], "UTF-8"), URLDecoder.decode(param[1], "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    // should never happen
                    LOGGER.error("UTF-8 is a unknown encoding.", e);
                }
            }
            return params;
        }

        private static void constructElement(Element current, String xpath, String value) {
            int i = xpath.indexOf('/');
            LOGGER.debug("Processing xpath: " + xpath);
            String subname = xpath;
            if (i > 0) {
                // construct new element name and xpath value
                subname = xpath.substring(0, i);
                xpath = xpath.substring(i + 1);
            }
            i = xpath.indexOf('/');
            if (subname.startsWith("@")) {
                if (i > 0) {
                    subname = subname.substring(0, i);// attribute should be
                    // the last
                }
                subname = subname.substring(1);// remove @
                LOGGER.debug("Setting attribute " + subname + "=" + value);
                current.setAttribute(subname, value);
                return;
            }
            Element newcurrent = current.getChild(subname);
            if (newcurrent == null) {
                newcurrent = new Element(subname);
                LOGGER.debug("Adding element " + newcurrent.getName() + " to " + current.getName());
                current.addContent(newcurrent);
            }
            if (subname == xpath) {
                // last element of xpath
                LOGGER.debug("Setting text of element " + newcurrent.getName() + " to " + value);
                newcurrent.setText(value);
                return;
            }
            constructElement(newcurrent, xpath, value); // recursive call
        }
    }
}
