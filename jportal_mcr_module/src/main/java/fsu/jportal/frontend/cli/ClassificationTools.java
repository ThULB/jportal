package fsu.jportal.frontend.cli;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.mycore.backend.hibernate.MCRHIBConnection;
import org.mycore.datamodel.classifications2.MCRCategoryDAOFactory;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.classifications2.MCRLabel;
import org.mycore.frontend.cli.annotation.MCRCommand;
import org.mycore.frontend.cli.annotation.MCRCommandGroup;

import fsu.jportal.backend.ClassificationHelper;

@MCRCommandGroup(name = "Classification Tools")
public class ClassificationTools{
    private static Logger LOGGER = LogManager.getLogger(ClassificationTools.class.getName());

    public ClassificationTools() {
        super();


    }

    // - get category without labels via SQL-query
    // - set category ID as category label
    @MCRCommand(help = "Repair category with empty labels.", syntax = "repair category with empty labels")
    public static void repairEmptyLabels() {
        Session session = MCRHIBConnection.instance().getSession();
        String sqlQuery = "select cat.classid,cat.categid from mcrcategory cat left outer join mcrcategorylabels label on cat.internalid = label.category where label.text is null";
        List list = session.createSQLQuery(sqlQuery).list();

        for (Object resultList : list) {
            Object[] arrayOfResults = (Object[]) resultList;
            String classIDString = (String) arrayOfResults[0];
            String categIDString = (String) arrayOfResults[1];

            MCRCategoryID mcrCategID = new MCRCategoryID(classIDString, categIDString);
            MCRLabel mcrCategLabel = new MCRLabel("de", categIDString, "");
            MCRCategoryDAOFactory.getInstance().setLabel(mcrCategID, mcrCategLabel);
            LOGGER.info("fixing category with class ID \"" + classIDString + "\" and category ID \"" + categIDString + "\"");
        }
        LOGGER.info("Fixing category labels completed!");
    }

    @MCRCommand(help = "Repair position in parent.", syntax = "repair position in parent")
    public static void repairPositionInParent() {
        Session session = MCRHIBConnection.instance().getSession();
        // this SQL-query find missing numbers in positioninparent
        String sqlQuery = "select parentid, min(cat1.positioninparent+1) from MCRCATEGORY cat1 " + "where cat1.parentid is not null and not exists"
                + "(select 1 from MCRCATEGORY cat2 " + "where cat2.parentid=cat1.parentid and cat2.positioninparent=(cat1.positioninparent+1))"
                + "and cat1.positioninparent not in " + "(select max(cat3.positioninparent) from MCRCATEGORY cat3 "
                + "where cat3.parentid=cat1.parentid) group by cat1.parentid";

        for (List<Object[]> parentWithErrorsList = session.createSQLQuery(sqlQuery).list(); !parentWithErrorsList.isEmpty(); parentWithErrorsList = session
                .createSQLQuery(sqlQuery).list()) {
            for (Object[] parentWithErrors : parentWithErrorsList) {
                Number parentID = (Number) parentWithErrors[0];
                Number firstErrorPositionInParent = (Number) parentWithErrors[1];
                LOGGER.info("Category " + parentID + " has the missing position " + firstErrorPositionInParent + " ...");
                repairCategoryWithGapInPos(parentID, firstErrorPositionInParent);
                LOGGER.info("Fixed position " + firstErrorPositionInParent + " for category " + parentID + ".");
            }
        }

        sqlQuery = "select parentid, min(cat1.positioninparent-1) from MCRCATEGORY cat1 " + "where cat1.parentid is not null and not exists"
                + "(select 1 from MCRCATEGORY cat2 " + "where cat2.parentid=cat1.parentid and cat2.positioninparent=(cat1.positioninparent-1))"
                + "and cat1.positioninparent not in " + "(select max(cat3.positioninparent) from MCRCATEGORY cat3 "
                + "where cat3.parentid=cat1.parentid) and cat1.positioninparent > 0 group by cat1.parentid";

        while (true) {
            List<Object[]> parentWithErrorsList = session.createSQLQuery(sqlQuery).list();

            if (parentWithErrorsList.isEmpty()) {
                break;
            }

            for (Object[] parentWithErrors : parentWithErrorsList) {
                Number parentID = (Number) parentWithErrors[0];
                Number wrongStartPositionInParent = (Number) parentWithErrors[1];
                LOGGER.info("Category " + parentID + " has the the starting position " + wrongStartPositionInParent + " ...");
                repairCategoryWithWrongStartPos(parentID, wrongStartPositionInParent);
                LOGGER.info("Fixed position " + wrongStartPositionInParent + " for category " + parentID + ".");
            }
        }
        LOGGER.info("Repair position in parent finished!");
    }

    private static void repairCategoryWithWrongStartPos(Number parentID, Number wrongStartPositionInParent) {
        Session session = MCRHIBConnection.instance().getSession();
        String sqlQuery = "update MCRCATEGORY set positioninparent= positioninparent -" + wrongStartPositionInParent + "-1 where parentid=" + parentID
                + " and positioninparent > " + wrongStartPositionInParent;

        session.createSQLQuery(sqlQuery).executeUpdate();
    }

    private static void repairCategoryWithGapInPos(Number parentID, Number firstErrorPositionInParent) {
        Session session = MCRHIBConnection.instance().getSession();
        // the query decrease the position in parent with a rate.
        // eg. posInParent: 0 1 2 5 6 7
        // at 3 the position get faulty, 5 is the min. of the position greather
        // 3
        // so the rate is 5-3 = 2
        String sqlQuery = "update MCRCATEGORY set positioninparent=(positioninparent - (select min(positioninparent) from MCRCATEGORY where parentid="
                + parentID + " and positioninparent > " + firstErrorPositionInParent + ")+" + firstErrorPositionInParent + ") where parentid=" + parentID
                + " and positioninparent > " + firstErrorPositionInParent;

        session.createSQLQuery(sqlQuery).executeUpdate();
    }
    
    @MCRCommand(syntax = "import export classification {0} {1}", help = "Reimport classification {id} and {keeplinks = 1}")
    public static void importExportClassification(String id, int keepLinks) {
        ClassificationHelper.importExportClassification(id, keepLinks);
    }
    
    @MCRCommand(syntax = "repair left right values for classification {0}", help = "fixes all left and right values in the given classification")
    public static void repairLeftRightValue(String classID) {
        ClassificationHelper.repairLeftRightValue(classID);
    }

}
