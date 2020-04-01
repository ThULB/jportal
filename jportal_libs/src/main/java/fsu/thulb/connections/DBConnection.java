package fsu.thulb.connections;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import fsu.thulb.connections.config.DBConnectionConfig;
import fsu.thulb.connections.config.SSHTunnelConfig;

public class DBConnection {
    private static final Logger LOGGER = LogManager.getLogger();
    private final DBConnectionConfig dbConnectionConfig;
    private Connection connection;
    private Statement currentStatement;
    private Session sshSession;

    public DBConnection(DBConnectionConfig dbConnectionConfig) {
        this.dbConnectionConfig = dbConnectionConfig;
    }


    public Connection getConnection() {
        if (getDbConnectionConfig().useSSHTunnel()) {
            openSSHTunnel(getDbConnectionConfig().getSshTunnelConfig());
        }

        if (this.connection != null) {
            LOGGER.info("Reuse connection " + getUrl() + " .");
            return this.connection;
        }

        LOGGER.info("-------- Connect to " + getUrl() + " ------------");

        try {
            Class.forName(getDriver());
        } catch (ClassNotFoundException e) {
            LOGGER.error("Where is your JDBC Driver " + getDriver() + "? Include in your library path!");
            e.printStackTrace();
            return null;

        }

        LOGGER.info(getDriver() + " JDBC Driver Registered!");

        try {
            LOGGER.info("Connection to " + getUrl() + " established.");
            connection = DriverManager.getConnection(getUrl(), getUsername(), getPassword());
        } catch (SQLException e) {
            LOGGER.error("Failed to make connection!");
            e.printStackTrace();
        }

        return connection;
    }

    private void openSSHTunnel(SSHTunnelConfig sshTunnelConfig) {
        if (this.sshSession == null) {
            try {
                this.sshSession = SSHTunnel.connect(sshTunnelConfig);
            } catch (JSchException e) {
                LOGGER.info("SSH tunnel " + sshTunnelConfig.info() + " failed.");
                e.printStackTrace();
            }

            LOGGER.info("SSH tunnel " + sshTunnelConfig.info() + " established.");
        }
    }

    public void closeConnection() {
        if (this.connection != null) {
            releaseCurrentStatement();
            try {
                this.connection.close();
            } catch (SQLException e) {
                LOGGER.error("Could not close connection " + getUrl() + " !");
                e.printStackTrace();
            }

            LOGGER.info("Connection to " + getUrl() + " closed!");
        }

        SSHTunnel.close(this.sshSession);
    }

    public void execUpdate(String updateQuery) {
        Integer rowCount = execStatement(stmt -> stmt.executeUpdate(updateQuery));
        LOGGER.info("Update " + rowCount + " rows.");
    }

    public ResultSet execQuery(String query) {
        return execStatement(stmt -> stmt.executeQuery(query));
    }

    public String getUrl() {
        return getDbConnectionConfig().getUrl();
    }

    public String getUsername() {
        return getDbConnectionConfig().getUsername();
    }

    public String getPassword() {
        return getDbConnectionConfig().getPassword();
    }

    public String getDriver() {
        return getDbConnectionConfig().getDriver();
    }

    public DBConnectionConfig getDbConnectionConfig() {
        return dbConnectionConfig;
    }


    interface StamentExecutor<T> {
        T exec(Statement stmt) throws SQLException;
    }

    private <V> V execStatement(StamentExecutor<V> executor) {
        Connection connection = getConnection();
        V result = null;

        if (connection != null) {
            releaseCurrentStatement();
            try {
                currentStatement = connection.createStatement();
                result = executor.exec(currentStatement);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    public void releaseCurrentStatement() {
        if (currentStatement != null) {
            try {
                currentStatement.close();
                currentStatement = null;
            } catch (SQLException e) {
                LOGGER.error("Could not release current statement!");
                e.printStackTrace();
            }
        }
    }
}
