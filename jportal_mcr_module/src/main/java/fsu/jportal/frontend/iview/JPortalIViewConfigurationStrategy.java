package fsu.jportal.frontend.iview;

import javax.servlet.http.HttpServletRequest;

import org.mycore.common.MCRSessionMgr;
import org.mycore.common.MCRSystemUserInformation;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.iview2.frontend.configuration.MCRViewerConfiguration;
import org.mycore.iview2.frontend.configuration.MCRViewerDefaultConfigurationStrategy;

public class JPortalIViewConfigurationStrategy extends MCRViewerDefaultConfigurationStrategy {

    @Override
    public MCRViewerConfiguration get(HttpServletRequest request) {
        MCRViewerConfiguration config = super.get(request);

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