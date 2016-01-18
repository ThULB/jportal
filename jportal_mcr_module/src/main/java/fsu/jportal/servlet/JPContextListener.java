package fsu.jportal.servlet;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.frontend.MCRFrontendUtil;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.net.URI;

/**
 * Created by chi on 18.01.16.
 * @author Huu Chi Vu
 */
public class JPContextListener implements ServletContextListener {
    static final Logger LOGGER = LogManager.getLogger(JPContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        String contextPath = sce.getServletContext().getContextPath();
        String baseURL = MCRFrontendUtil.getBaseURL();
        if (!baseURL.endsWith(contextPath)) {
            String uri = URI.create(baseURL).resolve(contextPath).toString();
            MCRConfiguration.instance().set("MCR.baseurl", uri);
            LOGGER.info("Using base URL " + uri);


        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }
}
