package org.mycore.frontend.cli;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.mycore.backend.hibernate.MCRHIBConnection;

public class MCRHibernateTools extends MCRAbstractCommands {
    private static Logger LOGGER = Logger.getLogger(MCRHibernateTools.class.getName());

    public MCRHibernateTools() {
        super();
        MCRCommand com = null;

        com = new MCRCommand("exec sql query {0}", "org.mycore.frontend.cli.MCRHibernateTools.execSqlQuery String", "");
        command.add(com);
    }

    public static void execSqlQuery(String query) {
        LOGGER.info("Query: " + query);
        Session session = MCRHIBConnection.instance().getSession();
        List list = session.createSQLQuery(query).list();

        for (Iterator iterator = list.iterator(); iterator.hasNext();) {
            Object object = (Object) iterator.next();

            if (!object.getClass().isArray()) {
                LOGGER.info(object);
            } else {
                Object[] arrayOfObjects = (Object[]) object;
                StringBuffer queryResponse = new StringBuffer();
                for (int i = 0; i < arrayOfObjects.length; i++) {
                    queryResponse.append(arrayOfObjects[i]);
                    
                    if (i != (arrayOfObjects.length -1))
                        queryResponse.append(" - ");
                }
                
                LOGGER.info(queryResponse.toString());
            }
        }
    }
}
