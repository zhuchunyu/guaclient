package com.habage.guaclient.servlet;

import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.net.GuacamoleSocket;
import org.apache.guacamole.net.GuacamoleTunnel;
import org.apache.guacamole.net.InetGuacamoleSocket;
import org.apache.guacamole.net.SimpleGuacamoleTunnel;
import org.apache.guacamole.protocol.ConfiguredGuacamoleSocket;
import org.apache.guacamole.protocol.GuacamoleClientInformation;
import org.apache.guacamole.protocol.GuacamoleConfiguration;
import org.apache.guacamole.servlet.GuacamoleHTTPTunnelServlet;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;

/**
 * Simple tunnel example with hard-coded configuration parameters.
 * @author admin
 */
@WebServlet(value = "/tunnel",name = "guacdServlet")
public class GuacdServlet extends GuacamoleHTTPTunnelServlet {

    @Override
    protected GuacamoleTunnel doConnect(HttpServletRequest request) throws GuacamoleException {

        // guacd connection information
        String hostname = "10.69.0.155";
        int port = 4822;

        // RDP connection information
        GuacamoleConfiguration config = new GuacamoleConfiguration();
        config.setProtocol("rdp");
        config.setParameter("hostname", "10.69.0.254");
        config.setParameter("port", "3389");
        config.setParameter("username", "administrator");
        config.setParameter("password", "Pioneer@2018");

        config.setParameter("resize-method", "display-update");
        config.setParameter("dpi", "100");
        config.setParameter("console", "true");
        config.setParameter("disable-audio", "true");
        config.setParameter("enable-drive", "true");
        config.setParameter("drive-path", "/usr/guacamole/upload/");
        config.setParameter("create-drive-path", "true");
        config.setParameter("ignore-cert", "true");
        config.setParameter("security", "any");
        System.out.println("###########################");

        // Connect to guacd, proxying a connection to the RDP server above
        GuacamoleClientInformation info = new GuacamoleClientInformation();
        info.setOptimalScreenWidth(1200);
        info.setOptimalScreenHeight(680);
        GuacamoleSocket socket = new ConfiguredGuacamoleSocket(
                new InetGuacamoleSocket(hostname, port),
                config, info
        );

        // Create tunnel from now-configured socket
        GuacamoleTunnel tunnel = new SimpleGuacamoleTunnel(socket);
        return tunnel;

    }

}
