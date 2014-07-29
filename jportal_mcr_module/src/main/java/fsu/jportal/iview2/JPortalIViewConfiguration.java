package fsu.jportal.iview2;

import javax.servlet.http.HttpServletRequest;

import org.mycore.common.MCRSessionMgr;
import org.mycore.common.MCRSystemUserInformation;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.iview2.frontend.configuration.MCRIViewClientConfiguration;
import org.mycore.iview2.frontend.configuration.MCRIViewClientConfigurationBuilder;

public class JPortalIViewConfiguration extends MCRIViewClientConfiguration {

    @Override
    public MCRIViewClientConfiguration setup(HttpServletRequest request) {
        // mets, logo, metadata, piwik
        MCRIViewClientConfigurationBuilder builder = MCRIViewClientConfigurationBuilder.metsAndPlugins(request);
        // get base configuration
        MCRIViewClientConfiguration baseConfiguration = builder.get();
        // mixin with configuration
        MCRIViewClientConfigurationBuilder.mixin(this, baseConfiguration);
        // add jportal required stuff
        super.setup(request);

        String baseURL = MCRServlet.getBaseURL();
        // css
        this.addCSS(baseURL + "css/jp-iview.css");

        // derivate link
        if (!MCRSessionMgr.getCurrentSession().getUserInformation().equals(MCRSystemUserInformation.getGuestInstance())) {
            this.addLocalScript("derivate-link-module.js");
        }
        return this;
    }

}
