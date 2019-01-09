package com.habage.guaclient.entity;

import org.apache.sshd.client.channel.ChannelShell;
import org.apache.sshd.common.future.SshFutureListener;
import org.apache.sshd.common.io.IoOutputStream;
import org.apache.sshd.common.io.IoReadFuture;
import org.apache.sshd.common.session.Session;

/**
 *
 * @author yuz
 * @date 2019/1/2
 */
public class WebsocketSession {
    private volatile ChannelShell channel;
    private volatile Session session;
    private volatile IoOutputStream asyncIn;
    private volatile IoReadFuture ioReadFuture;
    private volatile SshFutureListener<IoReadFuture> sshFutureListener;
    private volatile IoReadFuture ioErrFuture;
    private volatile SshFutureListener<IoReadFuture> sshErrListener;

    public WebsocketSession(ChannelShell channel, Session session, IoOutputStream asyncIn,
                            IoReadFuture ioReadFuture, SshFutureListener<IoReadFuture> sshFutureListener,
                            IoReadFuture ioErrFuture, SshFutureListener<IoReadFuture> sshErrListener) {
        this.channel = channel;
        this.session = session;
        this.asyncIn = asyncIn;
        this.ioReadFuture = ioReadFuture;
        this.sshFutureListener = sshFutureListener;
        this.ioErrFuture = ioErrFuture;
        this.sshErrListener = sshErrListener;
    }

    public ChannelShell getChannel() {
        return channel;
    }

    public void setChannel(ChannelShell channel) {
        this.channel = channel;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public IoOutputStream getAsyncIn() {
        return asyncIn;
    }

    public void setAsyncIn(IoOutputStream asyncIn) {
        this.asyncIn = asyncIn;
    }

    public IoReadFuture getIoReadFuture() {
        return ioReadFuture;
    }

    public void setIoReadFuture(IoReadFuture ioReadFuture) {
        this.ioReadFuture = ioReadFuture;
    }

    public SshFutureListener<IoReadFuture> getSshFutureListener() {
        return sshFutureListener;
    }

    public void setSshFutureListener(SshFutureListener<IoReadFuture> sshFutureListener) {
        this.sshFutureListener = sshFutureListener;
    }

    public IoReadFuture getIoErrFuture() {
        return ioErrFuture;
    }

    public void setIoErrFuture(IoReadFuture ioErrFuture) {
        this.ioErrFuture = ioErrFuture;
    }

    public SshFutureListener<IoReadFuture> getSshErrListener() {
        return sshErrListener;
    }

    public void setSshErrListener(SshFutureListener<IoReadFuture> sshErrListener) {
        this.sshErrListener = sshErrListener;
    }

    public void removeIoReadListener() {
        ioReadFuture.removeListener(sshFutureListener);
        ioErrFuture.removeListener(sshErrListener);
    }
}
