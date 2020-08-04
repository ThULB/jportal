package fsu.jportal.handler;

import java.nio.file.Paths;

import javax.servlet.ServletContext;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.mycore.backend.hibernate.MCRHIBConnection;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.events.MCRStartupHandler;
import org.mycore.user2.MCRUserCommands;
import org.mycore.user2.MCRUserManager;

import fsu.jportal.backend.ACLTools;
import fsu.jportal.backend.mcr.JPConfig;

/**
 * Created by chi on 11.11.15.
 * @author Huu Chi Vu
 */
public abstract class MCRInitHandler implements MCRStartupHandler.AutoExecutable {
    private final fsu.jportal.backend.ACLTools ACLTools = new ACLTools();

    protected Session session;

    private Transaction transaction;

    @Override
    public void startUp(ServletContext servletContext) {
        try {
            startSession();
            runWithSession();
            initSuperUser();
            closeSession();
        } catch(Exception exc) {
            if(transaction != null) {
                transaction.rollback();
            }
            exc.printStackTrace();
        }

        run();
    }

    protected abstract void run();

    protected abstract void runWithSession();

    private void closeSession() {
        transaction.commit();
        session.close();
        MCRSessionMgr.lock();
    }

    private void startSession() {
        session = MCRHIBConnection.instance().getSession();
        MCRSessionMgr.unlock();
        transaction = session.beginTransaction();
    }

    private void initSuperUser() {
        info("superuser ...");
        String superuser = JPConfig.getString("MCR.Users.Superuser.UserName", "administrator");
        if (!MCRUserManager.exists(superuser)) {
            MCRUserCommands.initSuperuser();
        }
        info("superuser initialized");
    }

    protected void info(String msg) {
        System.out.println("Init: " + msg);
    }

    protected void createACLRules(String msg, String pathToRuleCMDs) {
        info(msg);
        ACLTools.createRules(Paths.get(pathToRuleCMDs));
    }
}
