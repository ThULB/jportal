package fsu.thulb.persistence;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fsu.thulb.connections.DBConnection;
import fsu.thulb.connections.config.DBConnectionConfig;
import fsu.thulb.connections.config.SSHTunnelConfig;
import fsu.thulb.model.EpicurLite;
import fsu.thulb.xml.JAXBTools;

/**
 * Created by chi on 02.04.20
 *
 * @author Huu Chi Vu
 */
public class DNBUrnStorage {
    private static final Logger LOGGER = LogManager.getLogger();
    private static DNBUrnStorage instance;
    private DBConnectionConfig dbConnectionConfig;

    private DNBUrnStorage(DBConnectionConfig dbConnectionConfig) {
        this.dbConnectionConfig = dbConnectionConfig;
        initTable();
    }

    private static DBConnectionConfig initConfigs() throws Exception {
        String userHome = System.getProperty("user.home");
        String configDir = System.getProperty("DB.configDir");
        Path configPath = configDir != null ? Paths.get(configDir) : Paths.get(userHome, ".dnbDBConf");
        if (!Files.exists(configPath)) {

            String errMsg = "Please create directory $USERHOME" + File.separator + ".dnbDBConf.\n"
                    + "Or set system property DB.configDir";
            LOGGER.error(errMsg);
            throw new Exception();
        }

        LOGGER.info("Using DB config path " + configPath.toAbsolutePath().toString());
        Path configXMLPath = configPath.resolve("dbConnection.xml");
        if(!Files.exists(configXMLPath)) {
            LOGGER.error("Could not found config file " + configXMLPath.toAbsolutePath().toString());
            throw new Exception();
        }

        InputStream dbConnectionConfigIS = Files.newInputStream(configXMLPath);
        JAXBTools jaxbTools = JAXBTools.newInstance(SSHTunnelConfig.class, DBConnectionConfig.class);
        return (DBConnectionConfig) jaxbTools.getUnMarshaller().unmarshal(dbConnectionConfigIS);
    }

    public static DNBUrnStorage instance() throws Exception {
        if (instance == null) {
            DBConnectionConfig dbConnectionConfig = initConfigs();
            return instance(dbConnectionConfig);
        }

        return instance;
    }

    public static DNBUrnStorage instance(DBConnectionConfig dbConnectionConfig) {
        if (instance == null) {
            instance = new DNBUrnStorage(dbConnectionConfig);
        }

        return instance;
    }

    private DBConnection connectToDB() {
        return new DBConnection(getDbConnectionConfig());
    }

    private void initTable() {
        String createQuery = "create table if not exists urnstorage (urn VARCHAR, url VARCHAR)";
        DBConnection dbConnection = connectToDB();
        if (dbConnection != null) {
            dbConnection.execUpdate(createQuery);
            dbConnection.closeConnection();
        }
    }

    public Collection<EpicurLite> values() {
        DBConnection dbConnection = connectToDB();
        ResultSet resultSet = dbConnection.execQuery("select * from urnstorage");
        ArrayList<EpicurLite> values = new ArrayList<>();

        try {
            while (resultSet.next()) {
                String urn = resultSet.getString("urn");
                String url = resultSet.getString("url");
                UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("no", "login");
                EpicurLite epicurLite = EpicurLite.instance(credentials, urn, new URL(url));
                values.add(epicurLite);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        dbConnection.closeConnection();
        return values;
    }

    public boolean containsKey(String urn) {
        DBConnection dbConnection = connectToDB();
        ResultSet resultSet = dbConnection.execQuery("select count(*) from urnstorage where urn='" + urn + "'");
        boolean contains = false;
        try {
            long num = 0;
            while (resultSet.next()) {
                num = resultSet.getLong(1);
            }
            contains = (num == 1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        dbConnection.closeConnection();
        return contains;
    }

    public void put(String urn, EpicurLite epicurLite) {
        DBConnection dbConnection = connectToDB();
        String url = epicurLite.getURL();

        dbConnection.execUpdate("insert into URNSTORAGE (urn, url) values ('" + urn + "','" + url + "')");
        LOGGER.info("Saved: " + urn + " - " + epicurLite.getURL());
        dbConnection.closeConnection();
    }

    public void update(String urn, EpicurLite epicurLite) {
        DBConnection dbConnection = connectToDB();
        String url = epicurLite.getURL();

        dbConnection.execUpdate("update URNSTORAGE set url = '" + url + "' where urn = '" + urn + "'");
        LOGGER.info("Updated: " + urn + " - " + epicurLite.getURL());
        dbConnection.closeConnection();
    }

    public DBConnectionConfig getDbConnectionConfig() {
        return dbConnectionConfig;
    }
}
