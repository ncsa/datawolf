/******************************************************************************
 * Copyright 2004-2011 The Board of Trustees of the University of Illinois. 
 * All rights reserved.
 * 
 * Contributors:
 *    Albert L. Rossi:  original design and implementation
 ******************************************************************************/
package edu.illinois.ncsa.cyberintegrator.executor.hpc.ssh;

import java.io.File;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import edu.illinois.ncsa.cyberintegrator.executor.hpc.util.NonNLSConstants;

/**
 * Manages the JSch Session object by checking for connection and trying to
 * reconnect whenever a channel is retrieved.
 * 
 * @author Albert L. Rossi.
 */
public class SSHSession {
    private static final Logger logger   = LoggerFactory.getLogger(SSHSession.class);

    private Session             session  = null;
    private URI                 endpoint = null;
    private SSHInfo             info     = null;

    /**
     * @param session
     *            existing jcraft Session object
     */
    public SSHSession(Session session) {
        this.session = session;
    }

    /**
     * @param endpoint
     *            to which to open connection.
     */
    public SSHSession(URI endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * @param info
     *            to open connection.
     */
    public SSHSession(URI endpoint, SSHInfo info) {
        this.endpoint = endpoint;
        this.info = info;
    }

    /**
     * Disconnects.
     */
    public void close() {
        if (session != null) {
            logger.debug("closing session " + this);
            try {
                session.disconnect();
            } catch (Throwable t) {
                logger.warn("SSHSession.close()", t);
            }
        }
    }

    /**
     * The main purpose of the SSH connection: to do an exec. If the the session
     * is down or not connected, an attempt will be made to re-establish the
     * connection.
     */
    public Channel getExecChannel() throws Exception {
        Channel channel = null;
        Throwable t = null;

        try {
            if (session.isConnected())
                channel = session.openChannel("exec");
        } catch (JSchException firstAttempt) {
            t = firstAttempt;
        } catch (NullPointerException npe) {
            t = npe;
        }

        if (t != null || channel == null) {
            initializeSSHSession(); // second attempt or no jcraft session
            try {
                channel = session.openChannel("exec");
            } catch (JSchException lastAttempt) {
                throw new Exception("could not get exec channel for " + endpoint, lastAttempt);
            }
        }
        return channel;
    }

    public SSHInfo getInfo() {
        return info;
    }

    public void setInfo(SSHInfo info) {
        this.info = info;
    }

    private void initializeSSHSession() throws Exception {
        if (endpoint == null)
            throw new Exception("cannot open ssh session: no endpoint");

        if (info == null)
            info = new SSHInfo(endpoint);

        String host = endpoint.getHost();
        if (host == null)
            host = "127.0.0.1";
        int port = endpoint.getPort();
        if (port < 0)
            port = NonNLSConstants.DEFAULT_SSH_PORT;

        File sshHome = info.getSshHome();
        File rsa = null;
        File dsa = null;

        if (sshHome != null) {
            rsa = new File(sshHome, "id_rsa");
            dsa = new File(sshHome, "id_dsa");
        }

        try {
            JSch jsch = new JSch();
            if (rsa != null && rsa.exists())
                jsch.addIdentity(rsa.getAbsolutePath());
            if (dsa != null && dsa.exists())
                jsch.addIdentity(dsa.getAbsolutePath());
            session = jsch.getSession(info.getUser(), host, port);
            session.setUserInfo(info);
            session.connect();
        } catch (JSchException jsche) {
            throw new Exception("ssh utils, getSession", jsche);
        }
    }
}
