package fsu.jportal.handler.it;

import fsu.jportal.handler.MCRInitHandler;

/**
 * Created by chi on 09.11.15.
 * @author Huu Chi Vu
 */
public class TestDataHandler extends MCRInitHandler {
    @Override
    public String getName() {
        return "IT - Init test data";
    }

    @Override
    public int getPriority() {
        return -10;
    }

    @Override
    protected void run() {

    }

    @Override
    protected void runWithSession() {
        createACLRules("Creating IT ACL rules...", "/config/jportal_IT-TestData/acl/IT-ACLRules-commands");
    }
}
