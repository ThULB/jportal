package fsu.jportal.iview2;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.annotation.XmlRootElement;

import org.mycore.common.MCRSessionMgr;
import org.mycore.common.MCRSystemUserInformation;
import org.mycore.iview2.frontend.MCRIViewMetsClientConfiguration;

@XmlRootElement(name = "iviewClientConfiguration")
public class JPortalIViewConfiguration extends MCRIViewMetsClientConfiguration {

    @Override
    public void setup(HttpServletRequest request) {
        super.setup(request);

        // TODO: check if user is guest -> this should be secured by MCRAccess
        if (!MCRSessionMgr.getCurrentSession().getUserInformation().equals(MCRSystemUserInformation.getGuestInstance())) {
            this.addScript("modules/iview2/js/derivate-link-module.js");
        }

    }

}
