package fsu.jportal.startup;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.hsqldb.server.Server;

public class WebappConf {

    private static WebappConf instance;

    private File confDir;

    private File solrHomeDir;

    private String projectName;
    
    private Path confDirFromResource = Paths.get("/config/jportal_jetty_tools");

    private WebappConf() {
        projectName = System.getProperty("MCR.AppName", "jportal");
        initConfDir();
        checkConf(Paths.get("mycore.properties"));
        checkConf(Paths.get("hibernate/hibernate.cfg.xml"));
//        startHsqlDB();
        checkSolrHome();
    }

    private void checkSolrHome() {
        solrHomeDir = ConfigurationDir.getConfigFile("solr-home");
        Path sorHomePath = Paths.get(solrHomeDir.getAbsolutePath());

        if (!solrHomeDir.exists()) {
            info("Creating SOLR home ...");
            URL solrHomeJarURL = getClass().getResource(
                    "/config/jportal_jetty_tools/solr/solr-home");
            String[] solrHomeURLSplit = solrHomeJarURL.toString().split("!");
            Map<String, String> env = new HashMap<>();
            info("Solr from Jar " + solrHomeJarURL.toString());
            try {
                FileSystem zipFS = FileSystems.newFileSystem(URI.create(solrHomeURLSplit[0]), env);
                Path[] hibConfPath = new Path[]{zipFS.getPath(solrHomeURLSplit[1])};
                FileTools.copy(hibConfPath, sorHomePath, "r");
                info("Created SOLR home ... (" + solrHomeDir.getAbsolutePath() + ").");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        info("Using SOLR home (" + solrHomeDir.getAbsolutePath() + ").");
    }

    private void checkConf(Path confFilePath) {
        String fileName = confFilePath.getFileName().toString();
        File hibConFile = ConfigurationDir.getConfigFile(fileName);

        if (!hibConFile.exists()) {
            info("Creating config file " + fileName);
            Path hibConfPath = Paths.get(hibConFile.getAbsolutePath());
            String confFile = confDirFromResource.resolve(confFilePath).toString();
            InputStream hibConfIS = getClass().getResourceAsStream(confFile);
            try {
                Files.copy(hibConfIS, hibConfPath);
                info("Created config file ... (" + hibConFile.getAbsolutePath() + ").");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        info("Using config file (" + hibConFile.getAbsolutePath() + ").");
    }

    private void initConfDir() {
        confDir = ConfigurationDir.getConfigurationDirectory();

        if (!confDir.exists()) {
            info("Creating webapp home ...");
            confDir.mkdirs();
            info("Created webapp home (" + confDir.getAbsolutePath() + ").");
        } else {
            info("Using webapp home (" + confDir.getAbsolutePath() + ").");
        }
    }

    private void info(String msg) {
        System.out.println("WebappConf: " + msg);
    }

    public static WebappConf instance() {
        if (instance == null) {
            instance = new WebappConf();
        }

        return instance;
    }

    public void startHsqlDB() {
        Path dbPath = Paths.get(confDir.getAbsolutePath(), "hsqldb-data", projectName);
        String[] args = { "-database", dbPath.toString(), "-port", "8298" };
        info("HSQLDB use directory " + dbPath.toString());

        Server.main(args);
    }
    
    public String getSolrHome(){
        return solrHomeDir.getAbsolutePath();
    }

}
