package fsu.jportal.access;

import org.mycore.common.MCRConfiguration;
import org.mycore.common.MCRSessionMgr;

public class SuperUserStrategy extends StrategieChain {

    @Override
    protected boolean isReponsibleFor(String id, String permission) {
        return isSuperUser();
    }

    @Override
    protected boolean permissionStrategyFor(String id, String permission) {
        return true;
    }

    private final boolean isSuperUser() {
        String currentUserID = MCRSessionMgr.getCurrentSession().getUserInformation().getCurrentUserID();
        return currentUserID.equals(MCRConfiguration.instance().getString("MCR.Users.Superuser.UserName"));
    }
}