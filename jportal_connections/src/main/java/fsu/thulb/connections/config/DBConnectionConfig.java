package fsu.thulb.connections.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by chi on 2020-02-24
 *
 * @author Huu Chi Vu
 */
@XmlRootElement
public class DBConnectionConfig {
    @XmlElement
    private String driver;
    @XmlElement
    private String url;
    @XmlElement
    private String username;
    @XmlElement
    private String password;
    @XmlElement
    private SSHTunnelConfig sshTunnelConfig;

    private DBConnectionConfig() {
    }

    public DBConnectionConfig(String driver, String url, String username, String password) {
        this.driver = driver;
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public String getUrl() {
        return url;
    }

    public String getDriver() {
        return driver;
    }

    public void useSSHTunnel(SSHTunnelConfig sshTunnelConfig) {
        this.sshTunnelConfig = sshTunnelConfig;
    }

    public SSHTunnelConfig getSshTunnelConfig() {
        return sshTunnelConfig;
    }

    public boolean useSSHTunnel(){
        return getSshTunnelConfig() != null;
    }
}
