package fsu.jportal.xml;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.MCRUserInformation;
import org.mycore.datamodel.metadata.JPMetaDate;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.pi.MCRPIService;
import org.mycore.pi.MCRPersistentIdentifier;

import fsu.jportal.backend.JPLegalEntity;
import fsu.jportal.backend.JPPeriodicalComponent;
import fsu.jportal.urn.URNTools;
import fsu.jportal.util.JPComponentUtil;
import fsu.jportal.util.JPComponentUtil.JPInfoProvider;
import fsu.jportal.util.JPComponentUtil.JPObjectInfo;

public abstract class LayoutTools {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final Map<String, String> JOURNAL_ICON_MAP;

    static {
        JOURNAL_ICON_MAP = new HashMap<>();
        JOURNAL_ICON_MAP.put("newspapers", "Zeitungen.svg");
        JOURNAL_ICON_MAP.put("magazines", "Zeitschriften.svg");
        JOURNAL_ICON_MAP.put("addressBooks", "Adressbuecher.svg");
        JOURNAL_ICON_MAP.put("parliamentDocuments", "Parlamentsschriften.svg");
        JOURNAL_ICON_MAP.put("calendars", "Kalendarien.svg");
    }

    private static class DerivateDisplay implements JPObjectInfo<Boolean> {
        @Override
        public Boolean getInfo(List<Object> node) {
            return node.size() != 0;
        }
    }

    public static String getJournalID(String mcrID) {
        String journalID = JPComponentUtil.getJournalID(MCRObjectID.getInstance(mcrID));
        if (journalID == null) {
            LOGGER.warn("Unable to get journal id of {}", mcrID);
            return "";
        }
        return journalID;
    }

    public static String getMaintitle(String mcrID) {
        return JPComponentUtil.getMaintitle(mcrID).orElse("");
    }

    public static String getListType(String mcrID) {
        return JPComponentUtil.getListType(mcrID).orElse("");
    }

    public static String getJournalPublished(String journalID) {
        return JPComponentUtil
            .getPeriodical(MCRObjectID.getInstance(journalID))
            .flatMap(journal -> journal.getDate(JPPeriodicalComponent.DateType.published))
            .map(JPMetaDate::toString)
            .orElse("");
    }

    public static String getJournalIconURL(String mcrID) {
        return JPComponentUtil.getPeriodical(MCRObjectID.getInstance(mcrID))
            .map(JPPeriodicalComponent::getJournal)
            .flatMap(journal -> journal.getJournalTypeCategoryId("jportal_class_00000200"))
            .map(JOURNAL_ICON_MAP::get)
            .map(imageName -> MCRFrontendUtil.getBaseURL() + "logo/journal/" + imageName)
            .orElse("");
    }

    public static String getDerivateDisplay(String derivateID) {
        JPInfoProvider infoProvider = new JPInfoProvider(derivateID,
            "/mycorederivate/derivate[not(@display) or @display!='false']");
        return infoProvider.get(new DerivateDisplay()).toString();
    }

    public static String getUserName() {
        MCRUserInformation userInformation = MCRSessionMgr.getCurrentSession().getUserInformation();
        String realname = userInformation.getUserAttribute(MCRUserInformation.ATT_REAL_NAME);
        if (realname != null && !"".equals(realname.trim())) {
            return realname;
        } else {
            return userInformation.getUserID();
        }
    }

    public static String getIdentifier(String mcrID, String type) {
        Optional<JPLegalEntity> legalEntity = JPComponentUtil.getLegalEntity(MCRObjectID.getInstance(mcrID));
        return legalEntity.map(le -> le.getId(type).orElse(null)).orElse(null);
    }

    public static boolean hasURNAssigned(String derivID) {
        MCRPIService<MCRPersistentIdentifier> urnServiceManager = URNTools.getURNServiceManager();
        MCRObjectID derivObjID = MCRObjectID.getInstance(derivID);
        return urnServiceManager.isCreated(derivObjID, "");
    }

}
