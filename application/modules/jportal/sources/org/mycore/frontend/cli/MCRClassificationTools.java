package org.mycore.frontend.cli;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.mycore.backend.hibernate.MCRHIBConnection;
import org.mycore.datamodel.classifications2.MCRCategoryDAOFactory;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.classifications2.MCRLabel;

public class MCRClassificationTools extends MCRAbstractCommands {
    private static Logger LOGGER = Logger.getLogger(MCRClassificationTools.class.getName());

    public MCRClassificationTools() {
        super();
        MCRCommand com = null;

        com = new MCRCommand("repair category with empty labels", "org.mycore.frontend.cli.MCRClassificationTools.repairEmptyLabels", "");
        command.add(com);
    }

    // - get category without labels via SQL-query
    // - set category ID as category label 
    public static void repairEmptyLabels() {
        Session session = MCRHIBConnection.instance().getSession();
        String sqlQuery = "select cat.classid,cat.categid from mcrcategory cat left outer join mcrcategorylabels label on cat.internalid = label.category where label.text is null";
        List list = session.createSQLQuery(sqlQuery).list();

        for (Object resultList : list) {
            Object[] arrayOfResults = (Object[]) resultList;
            String classIDString = (String) arrayOfResults[0];
            String categIDString = (String) arrayOfResults[1];

            MCRCategoryID mcrCategID = new MCRCategoryID(classIDString, categIDString);
            MCRLabel mcrCategLabel = new MCRLabel("de",categIDString,"");
            MCRCategoryDAOFactory.getInstance().setLabel(mcrCategID, mcrCategLabel);
            LOGGER.info("fixing category with class ID \"" + classIDString + "\" and category ID \"" + categIDString + "\"");
        }
        LOGGER.info("Fixing category labels completed!");
    }
}
