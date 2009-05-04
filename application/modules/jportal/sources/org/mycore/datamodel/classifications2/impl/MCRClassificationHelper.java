package org.mycore.datamodel.classifications2.impl;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.mycore.backend.hibernate.MCRHIBConnection;
import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryDAO;
import org.mycore.datamodel.classifications2.MCRCategoryDAOFactory;
import org.mycore.datamodel.classifications2.MCRCategoryID;

public class MCRClassificationHelper {
    private static Logger LOGGER = Logger.getLogger(MCRClassificationHelper.class.getName());

    public static void repairLeftRightValue(String classID) {
        final Session session = MCRHIBConnection.instance().getSession();
        final MCRCategoryID rootID = MCRCategoryID.rootID(classID);
        MCRCategoryImpl classification = MCRCategoryDAOImpl.getByNaturalID(session, rootID);
        classification.calculateLeftRightAndLevel(0, 0);
    }

    public static void importExportClassification(String id, int boolValue) {
        // retrieve the root category
        MCRCategoryDAO categoryDAO = MCRCategoryDAOFactory.getInstance();
        MCRCategory categ = categoryDAO.getCategory(MCRCategoryID.rootID(id), -1);

        List<MCRCategoryLink> linkList = null;
        // saving existing Link
        Session session = MCRHIBConnection.instance().getSession();
        
        if (boolValue == 1) {

            String sqlSaveQuery = "from MCRCategoryLink where category in (select internalID from MCRCategoryImpl where rootID='" + id
                    + "' and not(categID=''))";
            linkList = session.createQuery(sqlSaveQuery).list();
            for (MCRCategoryLink categLink : linkList) {
                session.delete(categLink);
            }
        }

        categoryDAO.replaceCategory(categ);

        if (boolValue == 1 && linkList != null) {
            for (MCRCategoryLink categLink : linkList) {
                session.save(categLink);
            }
        }
    }
}
