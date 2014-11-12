package fsu.jportal.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.text.ParseException;

import javax.servlet.ServletContext;

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
import org.mycore.user2.MCRUserCommands;
import org.mycore.user2.MCRUserManager;
import org.xml.sax.SAXParseException;

import fsu.jportal.nio.JarResource;

public class InitHandler implements AutoExecutable{

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
        
        if(isTableEmpty(MCRACCESSRULE.class)){
            createDefaultRules();
        }
        
        if(isTableEmpty(MCRCategoryImpl.class)){
            createClass();
        }
        
        initSuperUser();
        
        closeSession();
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
            JarResource jarResource = new JarResource("/classifications");
            MCRCategoryDAO DAO = MCRCategoryDAOFactory.getInstance();
            for (Path child : jarResource.listFiles()) {
                InputStream classiXMLIS = Files.newInputStream(child);
                
                Document xml = MCRXMLParserFactory.getParser().parseXML(new MCRStreamContent(classiXMLIS));
                MCRCategory category = MCRXMLTransformer.getCategory(xml);
                DAO.addCategory(null, category);
            }
            jarResource.close();
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
        if(!MCRUserManager.exists(superuser)){
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
            for(String cmdLine; (cmdLine = cmdFileReader.readLine()) != null; ) {
                if(cmdLine != null && !cmdLine.trim().equals("")) {
                    info(cmdLine);
                    createRule(cmdLine);
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    private void createRule(String cmdLine){
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
        Method method = MCRAccessCommands.class.getMethod("permissionUpdateForID", String.class, String.class, String.class, String.class);
        String pattern = method.getAnnotation(MCRCommand.class).syntax();
        MessageFormat mf = new MessageFormat(pattern);
        Object[] params = mf.parse(cmdLine);
        return params;
    }
    
    private void addRule(String id, String permission, Element rule, String description){
        MCRAccessInterface AI = MCRAccessManager.getAccessImpl();
        
        AI.addRule(id, permission, rule, description);
    }

}