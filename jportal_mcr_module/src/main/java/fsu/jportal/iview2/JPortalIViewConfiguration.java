package fsu.jportal.iview2;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.annotation.XmlRootElement;

import org.mycore.common.MCRSessionMgr;
import org.mycore.common.MCRSystemUserInformation;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.iview2.frontend.MCRIViewMetsClientConfiguration;

@XmlRootElement(name = "iviewClientConfiguration")
public class JPortalIViewConfiguration extends MCRIViewMetsClientConfiguration {

    @Override
    public void setup(HttpServletRequest request) {
        super.setup(request);
        String baseURL = MCRServlet.getBaseURL();

        // css
        this.addCSS(baseURL + "css/jp-iview.css");

        // piwik
        if (MCRConfiguration.instance().getBoolean("MCR.Piwik.enable", false)) {
            this.addScript(baseURL + "modules/iview2/js/iview-client-piwik.js");
            this.setProperty("MCR.Piwik.baseurl", MCRConfiguration.instance().getString("MCR.Piwik.baseurl"));
            this.setProperty("MCR.Piwik.id", MCRConfiguration.instance().getString("MCR.Piwik.id"));
        }

        // derivate link
        if (!MCRSessionMgr.getCurrentSession().getUserInformation().equals(MCRSystemUserInformation.getGuestInstance())) {
            this.addScript(baseURL + "modules/iview2/js/derivate-link-module.js");
        }

    }

}
