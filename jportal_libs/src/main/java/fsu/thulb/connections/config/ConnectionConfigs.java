package fsu.thulb.connections.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by chi on 2020-02-25
 *
 * @author Huu Chi Vu
 */
@XmlRootElement
public class ConnectionConfigs {
    @XmlElement
    private DBConnectionConfig sourceDBConfig;
    @XmlElement
    private DBConnectionConfig targetDBConfig;

    private ConnectionConfigs() {
    }

    public ConnectionConfigs(DBConnectionConfig sourceDB, DBConnectionConfig targetDB) {
        this.sourceDBConfig = sourceDB;
        this.targetDBConfig = targetDB;
    }

    public DBConnectionConfig getSourceDBConfig() {
        return sourceDBConfig;
    }

    public DBConnectionConfig getTargetDBConfig() {
        return targetDBConfig;
    }
}
