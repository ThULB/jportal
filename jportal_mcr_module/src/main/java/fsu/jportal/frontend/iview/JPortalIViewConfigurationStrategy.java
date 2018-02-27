package fsu.jportal.frontend.iview;

import org.mycore.common.MCRSessionMgr;
import org.mycore.common.MCRSystemUserInformation;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.viewer.configuration.MCRViewerConfiguration;
import org.mycore.viewer.configuration.MCRViewerDefaultConfigurationStrategy;

import javax.servlet.http.HttpServletRequest;

public class JPortalIViewConfigurationStrategy extends MCRViewerDefaultConfigurationStrategy {

    @Override
    public MCRViewerConfiguration get(HttpServletRequest request) {
        MCRViewerConfiguration config = super.get(request);

        String baseURL = MCRFrontendUtil.getBaseURL();
        // css
        config.addCSS(baseURL + "css/jp-iview.css");

        if (!isPDF(request)) {
            // dfg viewer link
            config.addLocalScript("dfg-viewer-module.js");
            // derivate link
            if (!MCRSessionMgr.getCurrentSession().getUserInformation()
                .equals(MCRSystemUserInformation.getGuestInstance())) {
                config.addLocalScript("derivate-link-module.js");
            }
        }

        return config;
    }

}
