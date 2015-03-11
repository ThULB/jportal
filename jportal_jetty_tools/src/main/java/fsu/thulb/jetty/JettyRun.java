package fsu.thulb.jetty;

import java.io.File;

import org.eclipse.jetty.plus.jndi.EnvEntry;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.security.Credential;
import org.eclipse.jetty.webapp.WebAppContext;

import fsu.jportal.startup.WebappConf;

public class JettyRun {
    public static void main(String[] args) throws Exception {
        System.out.println("Hello World!");
        Server server = new Server();
        HashLoginService hashLoginService = new HashLoginService();
        hashLoginService.setName("Restricted");
        hashLoginService.putUser("test", Credential.getCredential("OBF:1z0f1vu91vv11z0f"), new String[] { "users" });
        hashLoginService.start();


        ServerConnector jportalConnector = new ServerConnector(server);
        jportalConnector.setPort(8291);
        server.addConnector(jportalConnector);
        
        for (String arg : args) {
            if (arg.startsWith("--path")) {
                String[] pathParts = arg.split(" ");
                if (pathParts.length == 3) {
                    WebAppContext webapp = new WebAppContext();
                    webapp.setContextPath(pathParts[1]);
                    File warFile = new File(pathParts[2]);
                    webapp.setWar(warFile.getAbsolutePath());
                    server.setHandler(webapp);
                }
            }
        }

        //        ServerConnector solrConnector = new ServerConnector (server);
        //        solrConnector.setPort(8294);
        //        server.addConnector(solrConnector);

        EnvEntry envEntry = new EnvEntry("/solr/home", WebappConf.instance().getSolrHome());

        server.start();
        server.dumpStdErr();
        server.join();
    }
}
