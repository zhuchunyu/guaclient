package com.habage.guaclient.servlet;

import com.google.inject.Singleton;

import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.net.GuacamoleSocket;
import org.apache.guacamole.net.GuacamoleTunnel;
import org.apache.guacamole.net.InetGuacamoleSocket;
import org.apache.guacamole.net.SimpleGuacamoleTunnel;
import org.apache.guacamole.protocol.ConfiguredGuacamoleSocket;
import org.apache.guacamole.protocol.GuacamoleClientInformation;
import org.apache.guacamole.protocol.GuacamoleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 *
 * @author yuz
 * @date 2019/1/7
 */
@Singleton
public class TunnelRequestService {

    private final Logger logger = LoggerFactory.getLogger(TunnelRequestService.class);

    protected GuacamoleClientInformation getClientInformation(TunnelRequest request)
            throws GuacamoleException {

        // Get client information
        GuacamoleClientInformation info = new GuacamoleClientInformation();

        // Set width if provided
        Integer width = request.getWidth();
        if (width != null) {
            info.setOptimalScreenWidth(width);
        }

        // Set height if provided
        Integer height = request.getHeight();
        if (height != null) {
            info.setOptimalScreenHeight(height);
        }

        // Set resolution if provided
        Integer dpi = request.getDPI();
        if (dpi != null) {
            info.setOptimalResolution(dpi);
        }

        // Add audio mimetypes
        List<String> audioMimetypes = request.getAudioMimetypes();
        if (audioMimetypes != null) {
            info.getAudioMimetypes().addAll(audioMimetypes);
        }

        // Add video mimetypes
        List<String> videoMimetypes = request.getVideoMimetypes();
        if (videoMimetypes != null) {
            info.getVideoMimetypes().addAll(videoMimetypes);
        }

        // Add image mimetypes
        List<String> imageMimetypes = request.getImageMimetypes();
        if (imageMimetypes != null) {
            info.getImageMimetypes().addAll(imageMimetypes);
        }

        return info;
    }

    public GuacamoleTunnel createTunnel(TunnelRequest request)
            throws GuacamoleException {
        GuacamoleClientInformation info = getClientInformation(request);
        System.out.println("info:::");
        System.out.println(info);
        System.out.println(info.getOptimalScreenWidth());
        System.out.println(info.getOptimalScreenHeight());
        System.out.println(info.getOptimalResolution());
        System.out.println(info.getAudioMimetypes());
        System.out.println(info.getVideoMimetypes());

        System.out.println(info.getImageMimetypes());

        // guacd connection information
        String hostname = "10.69.0.155";
        int port = 4822;

        // RDP connection information
        GuacamoleConfiguration guacamoleConfig = new GuacamoleConfiguration();
        guacamoleConfig.setProtocol("rdp");
        guacamoleConfig.setParameter("hostname", "10.69.0.254");
        guacamoleConfig.setParameter("port", "3389");
        guacamoleConfig.setParameter("username", "administrator");
        guacamoleConfig.setParameter("password", "Pioneer@2018");

        guacamoleConfig.setParameter("resize-method", "display-update");
        guacamoleConfig.setParameter("dpi", "100");
        guacamoleConfig.setParameter("console", "true");
        guacamoleConfig.setParameter("disable-audio", "true");
        guacamoleConfig.setParameter("enable-drive", "true");
        guacamoleConfig.setParameter("drive-path", "/usr/guacamole/upload/");
        guacamoleConfig.setParameter("create-drive-path", "true");
        guacamoleConfig.setParameter("ignore-cert", "true");
        guacamoleConfig.setParameter("security", "any");

        // Connect to guacd, proxying a connection to the RDP server above
        /*GuacamoleClientInformation clientInfo = new GuacamoleClientInformation();*/
        info.setOptimalScreenWidth(1280);
        info.setOptimalScreenHeight(700);
        info.setOptimalResolution(96);
        GuacamoleSocket socket = new ConfiguredGuacamoleSocket(
                new InetGuacamoleSocket(hostname, port),
                guacamoleConfig, info
        );

        // Create tunnel from now-configured socket
        GuacamoleTunnel tunnel = new SimpleGuacamoleTunnel(socket);

        return tunnel;
    }
}
