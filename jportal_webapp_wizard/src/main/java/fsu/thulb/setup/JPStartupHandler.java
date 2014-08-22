package fsu.thulb.setup;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.common.config.MCRConfigurationDir;
import org.mycore.common.events.MCRShutdownHandler.Closeable;
import org.mycore.common.events.MCRStartupHandler;
import org.mycore.common.events.MCRStartupHandler.AutoExecutable;

public class JPStartupHandler implements AutoExecutable, Closeable {
    private static final String WIZARD_FILTER_NAME = WizardRequestFilter.class.getName();

    private static final Class<WizardRequestFilter> WIZARD_FILTER_CLASS = WizardRequestFilter.class;

    static Logger LOGGER = Logger.getLogger(JPStartupHandler.class);

    @Override
    public void prepareClose() {
        System.out.println("####### JP Startup Handler prepare close!!");
        LOGGER.info("JP Startup Handler prepare close!!");
    }

    @Override
    public void close() {
        System.out.println("####### JP Startup Handler close!!");
        LOGGER.info("JP Startup Handler close!!");
    }

    @Override
    public String getName() {
        return getClass().getName();
    }

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void startUp(ServletContext servletContext) {
        if (servletContext != null) {
            File baseDir = MCRConfigurationDir.getConfigurationDirectory();

            MCRConfiguration config = MCRConfiguration.instance();

            if (!baseDir.exists()) {
                LOGGER.info("Create missing MCR.basedir (" + baseDir.getAbsolutePath() + ")...");
                baseDir.mkdirs();
                config.set("MCR.basedir", baseDir.getAbsolutePath());
            } else {
                File mcrProps = MCRConfigurationDir.getConfigFile("mycore.properties");
                File hibCfg = MCRConfigurationDir.getConfigFile("hibernate.cfg.xml");

                if ((mcrProps != null && mcrProps.canRead()) || (hibCfg != null && hibCfg.canRead())) {
                    return;
                }
            }

            servletContext.setAttribute(MCRStartupHandler.HALT_ON_ERROR, Boolean.toString(false));
            addWizardRequestFilterTo(servletContext);

        }
    }

    public static class WizardRequestFilter implements Filter {

        @Override
        public void init(FilterConfig filterConfig) throws ServletException {
            LOGGER.info("Init Filter");

        }

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
                ServletException {
            HttpServletRequest req = (HttpServletRequest) request;
            HttpServletResponse res = (HttpServletResponse) response;
            String contextPath = req.getContextPath();
            
            if (!req.getRequestURI().contains("wizard")) {
                String uri = req.getRequestURI();

                LOGGER.info("Requested Resource " + uri + " for context path " + contextPath + " # "
                        + req.getRequestURI());
                res.sendRedirect(contextPath + "/gui/wizard.html");
                return;
            }

            LOGGER.info("Chain filter!");
            chain.doFilter(request, response);
        }

        @Override
        public void destroy() {
            // TODO Auto-generated method stub

        }

    }

    private void addWizardRequestFilterTo(ServletContext servletContext) {
        LOGGER.info("Register " + WIZARD_FILTER_NAME + "...");

        Dynamic ft = servletContext.addFilter(WIZARD_FILTER_NAME, WIZARD_FILTER_CLASS);
        if (ft != null) {
            ft.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/*");
        } else {
            LOGGER.info("Couldn't map " + WIZARD_FILTER_NAME + "!");
        }

        //            LOGGER.info("Register " + WIZARD_SERVLET_NAME + "...");
        //            
        //            ServletRegistration sr = servletContext.addServlet(WIZARD_SERVLET_NAME, WIZARD_SERVLET_CLASS);
        //            if (sr != null) {
        //                sr.setInitParameter("keyname", WIZARD_SERVLET_NAME);
        //                sr.addMapping("/servlets/" + WIZARD_SERVLET_NAME + "/*");
        //                sr.addMapping("/wizard/config/*");
        //            } else {
        //                LOGGER.info("Couldn't map " + WIZARD_SERVLET_NAME + "!");
        //            }
        //            
        //            String wizStylesheet = config.getString("MIR.Wizard.LayoutStylesheet", "xsl/mir-wizard-layout.xsl");
        //            config.set("MCR.LayoutTransformerFactory.Default.Stylesheets", wizStylesheet);
    }

}
