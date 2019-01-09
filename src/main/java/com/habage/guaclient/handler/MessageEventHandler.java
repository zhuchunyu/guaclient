package com.habage.guaclient.handler;

import com.alibaba.fastjson.JSON;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.habage.guaclient.entity.TermSize;
import com.habage.guaclient.entity.WebsocketSession;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ChannelShell;
import org.apache.sshd.client.channel.ClientChannel;
import org.apache.sshd.client.config.hosts.HostConfigEntryResolver;
import org.apache.sshd.client.keyverifier.AcceptAllServerKeyVerifier;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.channel.Channel;
import org.apache.sshd.common.channel.ChannelListener;
import org.apache.sshd.common.future.CloseFuture;
import org.apache.sshd.common.future.SshFutureListener;
import org.apache.sshd.common.io.IoInputStream;
import org.apache.sshd.common.io.IoOutputStream;
import org.apache.sshd.common.io.IoReadFuture;
import org.apache.sshd.common.keyprovider.KeyPairProvider;
import org.apache.sshd.common.util.buffer.Buffer;
import org.apache.sshd.common.util.buffer.ByteArrayBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * socket.io消息事件处理器
 *
 * @author yuz
 */
@Component
public class MessageEventHandler {

    private static final String USER = "root";
    private static final String PASSWORD = "1q2w3e4r";
    private static final String HOST = "10.69.0.155";
    private static final int DEFAULT_SSH_PORT = 22;
    private static final int SSH_BUUFER_SIZE = 4096;

    private static SshClient sshClient = SshClient.setUpDefaultClient();

    static {
        sshClient.setServerKeyVerifier(AcceptAllServerKeyVerifier.INSTANCE);
        sshClient.setHostConfigEntryResolver(HostConfigEntryResolver.EMPTY);
        sshClient.setKeyPairProvider(KeyPairProvider.EMPTY_KEYPAIR_PROVIDER);
        sshClient.start();
    }

    volatile static Map<String, WebsocketSession> sessions = new ConcurrentHashMap<>(16);

    private final SocketIOServer server;

    private static final Logger logger = LoggerFactory.getLogger(MessageEventHandler.class);

    @Autowired
    public MessageEventHandler(SocketIOServer server) {
        this.server = server;
    }

    /**
     * 添加connect事件，当客户端发起连接时调用
     */
    @OnConnect
    public void onConnect(SocketIOClient client) {
        if (client == null) {
            logger.error("客户端为空");
            return;
        }

        String username = client.getHandshakeData().getSingleUrlParam("username");
        String password = client.getHandshakeData().getSingleUrlParam("password");
        String cols = client.getHandshakeData().getSingleUrlParam("cols");
        String rows = client.getHandshakeData().getSingleUrlParam("rows");
        String sessionId = client.getSessionId().toString();
        logger.info("连接成功, username=" + username + ", password=" + password + ", sessionId=" + sessionId);
        System.out.println("cols:" + cols);
        System.out.println("rows:" + rows);
        System.out.println("sessionId:" + client.getSessionId().toString());
        System.out.println(sessions);

        if (sessions.get(client.getSessionId().toString()) != null) {
            return;
        }

        // 获取认证权限

        try {
            ClientSession session = sshClient.connect(USER,
                    new InetSocketAddress(HOST, DEFAULT_SSH_PORT))
                    .verify(7L, TimeUnit.SECONDS).getSession();
            session.addPasswordIdentity(PASSWORD);
            session.auth().verify(7L, TimeUnit.SECONDS);

            ChannelShell channel = session.createShellChannel();
            channel.setStreaming(ClientChannel.Streaming.Async);
            // channel.setPtyType("vt100");
            channel.setPtyType("xterm-color");
            channel.setUsePty(true);
            channel.setPtyColumns(Integer.parseInt(cols));
            channel.setPtyLines(Integer.parseInt(rows));

            channel.addChannelListener(new ChannelListener() {
                @Override
                public void channelOpenSuccess(Channel channel) {
                    // Mismatched opened channel instances
                    System.out.println("channelOpenSuccess....");
                }

                @Override
                public void channelOpenFailure(Channel channel, Throwable reason) {
                    // Mismatched failed open channel instances
                }

                @Override
                public void channelInitialized(Channel channel) {
                    // Multiple channel initialization notifications
                    System.out.println("channelInitialized....");
                }

                @Override
                public void channelStateChanged(Channel channel, String hint) {
                    // channelStateChanged(%s): %s
                    System.out.println("channelStateChanged:");
                    System.out.println(channel.isOpen());
                    System.out.println(hint);
                }

                @Override
                public void channelClosed(Channel channel, Throwable reason) {
                    // Mismatched closed channel instances
                    System.out.println("Channel...exit");

                    WebsocketSession websocketSession = sessions.get(client.getSessionId().toString());
                    if (websocketSession != null) {
                        //websocketSession.removeIoReadListener();

                        /*if (websocketSession.getChannel().isOpen()) {
                            try {
                                websocketSession.getChannel().close(true);
                            } catch (Exception e) {
                                logger.warn(e.getMessage());
                            }
                        }
                        if (websocketSession.getSession().isOpen()) {
                            try {
                                websocketSession.getSession().close(true);
                            } catch (Exception e) {
                                logger.warn(e.getMessage());
                            }
                        }*/

                        // sessions.remove(client.getSessionId().toString());
                    }

                    client.disconnect();
                }
            });
            channel.addCloseFutureListener(new SshFutureListener<CloseFuture>() {
                @Override
                public void operationComplete(CloseFuture future) {
                    System.out.println("closed:::" + future.isClosed());
                }
            });

            channel.open().verify(5L, TimeUnit.SECONDS);

            IoOutputStream asyncIn = channel.getAsyncIn();

            IoInputStream asyncOut = channel.getAsyncOut();

            SshFutureListener<IoReadFuture> sshFutureListener = new SshFutureListener<IoReadFuture>() {
                @Override
                public void operationComplete(IoReadFuture future) {
                    Buffer buffer = future.getBuffer();
                    client.sendEvent("data", new String(buffer.array(),
                            buffer.rpos(), buffer.available(), StandardCharsets.UTF_8));
                    buffer.rpos(buffer.rpos() + buffer.available());
                    buffer.compact();

                    IoReadFuture read = asyncOut.read(buffer);
                    WebsocketSession websocketSession = sessions.get(client.getSessionId().toString());
                    if (websocketSession != null) {
                        websocketSession.setIoReadFuture(read);
                        websocketSession.setSshFutureListener(this);
                    }

                    read.addListener(this);
                }
            };

            IoReadFuture ioReadFuture = asyncOut.read(new ByteArrayBuffer(SSH_BUUFER_SIZE));
            ioReadFuture.addListener(sshFutureListener);

            IoInputStream asyncErr = channel.getAsyncErr();
            SshFutureListener<IoReadFuture> sshErrListener = new SshFutureListener<IoReadFuture>() {
                @Override
                public void operationComplete(IoReadFuture future) {
                    if (future.getException() != null) {
                        System.out.println("#################################exp::");
                        // future.getException().printStackTrace();
                        logger.warn(future.getException().getMessage());

                        /*if (channel.isOpen()) {
                            try {
                                channel.close();
                            } catch (Exception e) {
                                logger.warn(e.getMessage());
                            }
                        }
                        if (session.isOpen()) {
                            try {
                                session.close(false);
                            } catch (Exception e) {
                                logger.warn(e.getMessage());
                            }
                        }*/
                    } else {
                        Buffer buffer = future.getBuffer();
                        String errMessage = new String(buffer.array(), buffer.rpos(),
                                buffer.available(), StandardCharsets.UTF_8);
                        System.out.println("error::::");
                        System.out.println(errMessage);
                        client.sendEvent("data", errMessage);
                        buffer.rpos(buffer.rpos() + buffer.available());
                        buffer.compact();

                        IoReadFuture read = asyncErr.read(buffer);
                        WebsocketSession websocketSession = sessions.get(client.getSessionId().toString());
                        if (websocketSession != null) {
                            websocketSession.setIoReadFuture(read);
                            websocketSession.setSshFutureListener(this);
                        }

                        read.addListener(this);
                    }
                }
            };

            IoReadFuture ioErrFuture = asyncErr.read(new ByteArrayBuffer());
            ioErrFuture.addListener(sshErrListener);

            WebsocketSession websocketSession = new WebsocketSession(
                    channel, session, asyncIn, ioReadFuture, sshFutureListener, ioErrFuture, sshErrListener);
            sessions.put(client.getSessionId().toString(), websocketSession);

            Map<String, Object> setTerminalOpts = new HashMap<>(8);
            setTerminalOpts.put("cursorBlink", true);
            setTerminalOpts.put("scrollback", 10000);
            setTerminalOpts.put("tabStopWidth", 8);
            setTerminalOpts.put("bellStyle", "sound");
            client.sendEvent("setTerminalOpts", setTerminalOpts);

            client.sendEvent("menu", "menu");
            client.sendEvent("status", "SSH 连接成功！");
            client.sendEvent("statusBackground", "green");
            client.sendEvent("allowreplay", true);
            client.sendEvent("allowreauth", true);

        } catch (Exception e) {
            e.printStackTrace();
            client.disconnect();
        }
    }

    /**
     * 添加@OnDisconnect事件，客户端断开连接时调用，刷新客户端信息
     */
    @OnDisconnect
    public void onDisconnect(SocketIOClient client) {
        logger.info("客户端断开连接, sessionId=" + client.getSessionId().toString());
        WebsocketSession session = sessions.get(client.getSessionId().toString());
        if (session != null) {
            /*if (session.getChannel().isOpen()) {
                try {
                    session.getChannel().close();
                } catch (Exception e) {
                    logger.warn(e.getMessage());
                }
            }
            if (session.getSession().isOpen()) {
                try {
                    session.getSession().close();
                } catch (Exception e) {
                    logger.warn(e.getMessage());
                }
            }*/

            sessions.remove(client.getSessionId().toString());
        }
        client.disconnect();
    }

    @OnEvent(value = "data")
    public void onDataEvent(SocketIOClient client, AckRequest ackRequest, String data) {
        WebsocketSession session = sessions.get(client.getSessionId().toString());
        if (session != null) {
            try {
                session.getAsyncIn().writePacket(new ByteArrayBuffer(data.getBytes(StandardCharsets.UTF_8)))
                        .addListener(future -> {
                            Throwable exception = future.getException();
                            if (exception != null) {
                                exception.printStackTrace();
                            }
                        });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @OnEvent(value = "resize")
    public void onResize(SocketIOClient client, AckRequest ackRequest, TermSize termSize) {
        WebsocketSession session = sessions.get(client.getSessionId().toString());
        if (session != null) {
            try {
                System.out.println(JSON.toJSON(termSize));
                session.getChannel().sendWindowChange(termSize.getCols(), termSize.getRows());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @OnEvent(value = "close")
    public void onClose(SocketIOClient client) {
        WebsocketSession session = sessions.get(client.getSessionId().toString());
        if (session != null) {
            session.removeIoReadListener();

            if (session.getChannel().isOpen()) {
                try {
                    session.getChannel().close(true);
                } catch (Exception e) {
                    logger.warn(e.getMessage());
                }
            }
            if (session.getSession().isOpen()) {
                try {
                    session.getSession().close(true);
                } catch (Exception e) {
                    logger.warn(e.getMessage());
                }
            }
        }

        client.disconnect();
    }
}
