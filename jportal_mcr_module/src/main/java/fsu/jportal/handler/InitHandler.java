package fsu.jportal.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.servlet.ServletContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jdom2.Document;
import org.jdom2.Element;
import org.mycore.access.MCRAccessInterface;
import org.mycore.access.MCRAccessManager;
import org.mycore.backend.hibernate.MCRHIBConnection;
import org.mycore.backend.hibernate.tables.MCRACCESSRULE;
import org.mycore.common.MCRException;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.common.content.MCRStreamContent;
import org.mycore.common.events.MCRStartupHandler.AutoExecutable;
import org.mycore.common.xml.MCRXMLParserFactory;
import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryDAO;
import org.mycore.datamodel.classifications2.MCRCategoryDAOFactory;
import org.mycore.datamodel.classifications2.impl.MCRCategoryImpl;
import org.mycore.datamodel.classifications2.utils.MCRXMLTransformer;
import org.mycore.frontend.cli.MCRAccessCommands;
import org.mycore.frontend.cli.annotation.MCRCommand;
import org.mycore.solr.MCRSolrCore;
import org.mycore.solr.classification.MCRSolrClassificationUtil;
import org.mycore.user2.MCRUserCommands;
import org.mycore.user2.MCRUserManager;
import org.xml.sax.SAXParseException;

import fsu.jportal.nio.JarResource;

public class InitHandler implements AutoExecutable {

    private static final Logger LOGGER = LogManager.getLogger(InitHandler.class);

    private Session session;

    private Transaction transaction;

    @Override
    public String getName() {
        return "Init JPortal";
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public void startUp(ServletContext servletContext) {
        startSession();

        if (isTableEmpty(MCRACCESSRULE.class)) {
            createDefaultRules();
        }

        if (isTableEmpty(MCRCategoryImpl.class)) {
            createClass();
        }

        initSuperUser();

        closeSession();

        initCLI();

        initSolr();
    }

    private void initCLI() {
        info("init CLI....");
        StringBuffer cliClassName = new StringBuffer();
        try {
            String packageName = "fsu.jportal.frontend.cli";
            Enumeration<URL> resources = getClass().getClassLoader().getResources(packageName.replace('.', '/'));
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                JarResource jarResource = new JarResource(url);
                DirectoryStream<Path> paths = jarResource.listFiles();
                for (Path path : paths) {
                    String className = path.getFileName().toString();
                    if (!className.contains("$")) {
                        info("found CLI: " + className);
                        cliClassName.append("," + packageName + "." + className.replace(".class", ""));
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (cliClassName.length() > 0) {
            cliClassName.insert(0, "%MCR.CLI.Classes.External%");
            HashMap<String, String> props = new HashMap<>();
            props.put("MCR.CLI.Classes.External", cliClassName.toString());
            MCRConfiguration.instance().initialize(props, false);
            //            info("found CLI classes " + cliClassName);
        }

    }

    public boolean isTableEmpty(Class clazz) {
        return session.createCriteria(clazz).setMaxResults(1).list().isEmpty();
    }

    private void closeSession() {
        transaction.commit();
        session.close();
    }

    private void startSession() {
        session = MCRHIBConnection.instance().getSession();
        transaction = session.beginTransaction();
    }

    private void createClass() {
        info("creating default classifications ...");

        try {
            Enumeration<URL> resources = getClass().getClassLoader().getResources("classifications");
            MCRCategoryDAO DAO = MCRCategoryDAOFactory.getInstance();
            while (resources.hasMoreElements()) {
                URL url = (URL) resources.nextElement();
                info("Classi location: " + url.toString());
                JarResource resource = new JarResource(url);

                for (Path child : resource.listFiles()) {
                    info("Found classi: " + child.toString());
                    InputStream classiXMLIS = Files.newInputStream(child);

                    Document xml = MCRXMLParserFactory.getParser().parseXML(new MCRStreamContent(classiXMLIS));
                    MCRCategory category = MCRXMLTransformer.getCategory(xml);
                    DAO.addCategory(null, category);
                }

                resource.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MCRException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void initSuperUser() {
        info("superuser ...");
        String superuser = MCRConfiguration.instance().getString("MCR.Users.Superuser.UserName", "administrator");
        if (!MCRUserManager.exists(superuser)) {
            MCRUserCommands.initSuperuser();
        }
        info("superuser initialized");
    }

    private void info(String msg) {
        System.out.println("Init: " + msg);
    }

    private void createDefaultRules() {
        info("creating default ACL rules ...");
        InputStream cmdFileIS = getClass().getResourceAsStream("/config/jportal_mcr/acl/defaultrules-commands");
        BufferedReader cmdFileReader = new BufferedReader(new InputStreamReader(cmdFileIS));

        try {
            for (String cmdLine; (cmdLine = cmdFileReader.readLine()) != null;) {
                if (cmdLine != null && !cmdLine.trim().equals("")) {
                    info(cmdLine);
                    createRule(cmdLine);
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void createRule(String cmdLine) {
        try {
            Object[] params = parseParams(cmdLine);

            String permission = (String) params[0];
            String id = (String) params[1];
            Element rule = getRuleXML((String) params[2]);
            String description = (String) params[3];

            addRule(id, permission, rule, description);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private Element getRuleXML(String source) {
        Path path = Paths.get("/config/jportal_mcr/acl", source);
        String ruleXML = path.toString();
        InputStream resourceIS = getClass().getResourceAsStream(ruleXML);
        try {
            Document ruleDom = MCRXMLParserFactory.getParser().parseXML(new MCRStreamContent(resourceIS));
            return ruleDom.getRootElement();
        } catch (MCRException | SAXParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }

    private Object[] parseParams(String cmdLine) throws NoSuchMethodException, ParseException {
        Method method = MCRAccessCommands.class.getMethod("permissionUpdateForID", String.class, String.class,
            String.class, String.class);
        String pattern = method.getAnnotation(MCRCommand.class).syntax();
        MessageFormat mf = new MessageFormat(pattern);
        Object[] params = mf.parse(cmdLine);
        return params;
    }

    private void addRule(String id, String permission, Element rule, String description) {
        MCRAccessInterface AI = MCRAccessManager.getAccessImpl();

        AI.addRule(id, permission, rule, description);
    }

    private void initSolr() {
        Runnable initTask = () -> {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<?> future = executor.submit(new SolrClassificationInitTask());
            try {
                future.get(5, TimeUnit.MINUTES);
            } catch (TimeoutException timeout) {
                LOGGER.error("Unable to find solr server after five minutes prior application start.", timeout);
            } catch (Exception exc) {
                LOGGER.error("Unable initialize solr.", exc);
            }
            executor.shutdownNow();
        };
        new Thread(initTask).start();
    }

    private static class SolrClassificationInitTask implements Runnable {

        @Override
        public void run() {
            try {
                MCRSolrCore core = MCRSolrClassificationUtil.getCore();
                HttpSolrClient client = core.getClient();
                awaitSolr(client);
                if (!isInitialized(client)) {
                    index(client);
                }
            } catch (Exception exc) {
                exc.printStackTrace();
            }
        }

        /**
         * Waits until the solr server is available.
         * 
         * @throws IOException
         * @throws InterruptedException
         */
        private void awaitSolr(SolrClient solrClient) throws IOException, InterruptedException {
            boolean solrRunning = false;
            do {
                try {
                    solrClient.ping();
                    solrRunning = true;
                } catch (SolrServerException sse) {
                    TimeUnit.MILLISECONDS.sleep(100);
                }
            } while (!solrRunning);
        }

        /**
         * Checks if the solr server has already indexed the classifications.
         * 
         * @return
         * @throws SolrServerException 
         */
        private boolean isInitialized(SolrClient solrClient) throws IOException, SolrServerException {
            ModifiableSolrParams params = new ModifiableSolrParams();
            params.set("q", "*:*");
            params.set("rows", 0);
            QueryResponse response = solrClient.query(params);
            return response.getResults().getNumFound() != 0;
        }

        /**
         * Rebuilds the classification index.
         * 
         * @param solrClient
         */
        private void index(SolrClient solrClient) {
            Session session = MCRHIBConnection.instance().getSession();
            Transaction transaction = session.beginTransaction();
            MCRSolrClassificationUtil.rebuildIndex();
            transaction.commit();
            session.close();
        }

    }

}
