package fsu.jportal.iview2;

import javax.servlet.http.HttpServletRequest;

import org.mycore.common.MCRSessionMgr;
import org.mycore.common.MCRSystemUserInformation;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.iview2.frontend.configuration.MCRIViewClientConfiguration;
import org.mycore.iview2.frontend.configuration.MCRIViewClientDefaultConfigurationStrategy;

public class JPortalIViewConfigurationStrategy extends MCRIViewClientDefaultConfigurationStrategy {

    @Override
    public MCRIViewClientConfiguration get(HttpServletRequest request) {
        MCRIViewClientConfiguration config = super.get(request);

        String baseURL = MCRFrontendUtil.getBaseURL();
        // css
        config.addCSS(baseURL + "css/jp-iview.css");

        if (!isPDF(request)) {
            // derivate link
            if (!MCRSessionMgr.getCurrentSession().getUserInformation()
                .equals(MCRSystemUserInformation.getGuestInstance())) {
                config.addLocalScript("derivate-link-module.js");
            }
        }

        return config;
    }

}
