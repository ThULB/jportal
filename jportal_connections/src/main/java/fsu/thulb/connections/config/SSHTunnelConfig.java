package fsu.thulb.connections.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by chi on 2020-02-25
 *
 * @author Huu Chi Vu
 */
@XmlRootElement
public class SSHTunnelConfig {
    @XmlElement
    private String username;
    @XmlElement
    private String password;
    @XmlElement
    private String remoteHost;
    @XmlElement
    private int remotePort;
    @XmlElement
    private String localHost;
    @XmlElement
    private int localPort;
    @XmlElement
    private int sshPort;

    public SSHTunnelConfig() {
    }

    public SSHTunnelConfig(String username, String password, String remoteHost, int remotePort, String localHost,
                           int localPort, int sshPort) {
        this.username = username;
        this.password = password;
        this.remoteHost = remoteHost;
        this.localHost = localHost;
        this.remotePort = remotePort;
        this.localPort = localPort;
        this.sshPort = sshPort;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public String getLocalHost() {
        return localHost;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public int getLocalPort() {
        return localPort;
    }

    public int getSshPort() {
        return sshPort;
    }

    public String info(){
        return getRemoteHost() + ":" + getRemotePort() + " -> " + getLocalHost() + ":" + getLocalPort();
    }
}
