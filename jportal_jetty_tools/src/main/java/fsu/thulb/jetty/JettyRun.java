package fsu.thulb.jetty;

import org.eclipse.jetty.plus.jndi.EnvEntry;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.security.Credential;

import fsu.jportal.startup.WebappConf;

public class JettyRun {
    public static void main(String[] args) throws Exception
    {
        System.out.println("Hello World!");
        for (String arg : args) {
            System.out.println("Args: " + arg);
        }
        Server server = new Server();
        HashLoginService hashLoginService = new HashLoginService();
        hashLoginService.setName("Restricted");
        hashLoginService.putUser("test", Credential.getCredential("OBF:1z0f1vu91vv11z0f"), new String[] {"users"});
        hashLoginService.start();
        
        ServerConnector jportalConnector = new ServerConnector (server);
        jportalConnector.setPort(8291);
        server.addConnector(jportalConnector);
        
//        ServerConnector solrConnector = new ServerConnector (server);
//        solrConnector.setPort(8294);
//        server.addConnector(solrConnector);
        
        EnvEntry envEntry = new EnvEntry("/solr/home", WebappConf.instance().getSolrHome());
        
        server.start();
        server.dumpStdErr();
        server.join();
    }
}
