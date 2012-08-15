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

import com.jcraft.jsch.UserInfo;

/**
 * @author Albert L. Rossi;
 */
public class SSHInfo implements UserInfo {
    private String        user;
    private File          sshHome;
    private String        password;
    private final boolean isGsi;

    public SSHInfo() {
        isGsi = false;
    }

    public SSHInfo(URI endpoint) throws IllegalArgumentException {
        isGsi = endpoint.getScheme().startsWith("gsi");
        String prefix = endpoint.getUserInfo();
        if (prefix != null) {
            String[] userinfo = endpoint.getUserInfo().split(":");
            if (userinfo.length > 0)
                user = userinfo[0];
            if (userinfo.length > 1)
                password = userinfo[1];
        }
    }

    public String getPassphrase() {
        return null;
    }

    public String getPassword() {
        return password;
    }

    public File getSshHome() {
        if (!isGsi && sshHome == null)
            sshHome = new File(System.getProperty("user.home"), ".ssh");
        return sshHome;
    }

    public String getUser() {
        if (user == null)
            user = System.getProperty("user.name");
        return user;
    }

    public boolean promptPassphrase(String p1) {
        return true;
    }

    public boolean promptPassword(String p1) {
        return true;
    }

    public boolean promptYesNo(String p1) {
        return true;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setSshHome(File sshHome) {
        this.sshHome = sshHome;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void showMessage(String p1) {}
}
