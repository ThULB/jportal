package fsu.thulb.connections;

import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import fsu.thulb.connections.config.SSHTunnelConfig;

/**
 * Created by chi on 2020-02-05
 *
 * @author Huu Chi Vu
 */
public class SSHTunnel {
    private static final Logger LOGGER = LogManager.getLogger();

    public static Session connect(String username, String passwort, String remoteHost, int remotePort,
                                  String localHost, int localPort, int sshPort) throws JSchException {
        final JSch jsch = new JSch();
        Session session = jsch.getSession(username, remoteHost, sshPort);
        session.setPassword(passwort);

        final Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);

        session.connect();
        int assignedPort = session.setPortForwardingL(localPort, localHost, remotePort);

        LOGGER.info("Open Tunnel " + remoteHost + ":" + assignedPort + " -> " + localHost + ":" + remotePort);
        return session;
    }

    public static Session connect(SSHTunnelConfig sshTunnelConfig) throws JSchException {
        String username = sshTunnelConfig.getUsername();
        String password = sshTunnelConfig.getPassword();
        String remoteHost = sshTunnelConfig.getRemoteHost();
        int remotePort = sshTunnelConfig.getRemotePort();
        String localHost = sshTunnelConfig.getLocalHost();
        int localPort = sshTunnelConfig.getLocalPort();
        int sshPort = sshTunnelConfig.getSshPort();
        return connect(username, password, remoteHost, remotePort, localHost, localPort, sshPort);
    }

    public static void close(Session session) {
        if(session == null){
            LOGGER.warn("Can not close session with null value.");
            return;
        }

        try {
            String[] portForwardingL = session.getPortForwardingL();
            String remoteHost = session.getHost();

            if (session.isConnected()) {
                session.disconnect();
                LOGGER.info("Close Tunnel " + String.join(":",portForwardingL).concat(" " + remoteHost));
            }
        } catch (JSchException e) {
            e.printStackTrace();
        }
    }
}
