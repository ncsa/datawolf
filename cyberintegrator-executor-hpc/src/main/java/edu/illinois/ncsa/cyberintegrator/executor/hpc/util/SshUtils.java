/******************************************************************************
 * Copyright 2004-2010 The Board of Trustees of the University of Illinois. 
 * All rights reserved.
 * 
 * Contributors:
 *    Albert L. Rossi:  original design and implementation
 ******************************************************************************/
package edu.illinois.ncsa.cyberintegrator.executor.hpc.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;

import edu.illinois.ncsa.cyberintegrator.executor.hpc.ssh.SSHInfo;
import edu.illinois.ncsa.cyberintegrator.executor.hpc.ssh.SSHSession;

/**
 * Methods associated with Ssh (= jcraft.JSch implementation).
 * 
 * @author Albert L. Rossi
 */
public class SshUtils {
    private static class SCPFrom extends SCPTransfer {

        private OutputStream targetStream;

        public void run() {
            for (int c = 0; c < 5; c++) {
                try {
                    // connect
                    connect();

                    // send '\0'
                    logger.debug("sending: " + 0);
                    buf[0] = 0;
                    out.write(buf, 0, 1);
                    out.flush();
                    error = new StringBuffer(0);

                    while (true) {
                        logger.debug("checking ACK for C");
                        if (SshUtils.checkAck(in, error) != 'C') {
                            if (error.length() > 0)
                                throw new IOException("scpFrom " + source + ", " + target + ": " + error.toString());
                            break;
                        }

                        // read '0644 '
                        logger.debug("reading 0644");
                        in.read(buf, 0, 5);

                        size = 0;
                        logger.debug("reading size");
                        while (true) {
                            in.read(buf, 0, 1);
                            if (buf[0] == ' ')
                                break;
                            size = size * 10 + (buf[0] - '0');
                        }

                        logger.debug("looking for 0x0a");
                        for (int i = 0;; i++) {
                            in.read(buf, i, 1);
                            if (buf[i] == (byte) 0x0a)
                                break;
                        }

                        // send '\0'
                        logger.debug("sending: " + 0);
                        buf[0] = 0;
                        out.write(buf, 0, 1);
                        out.flush();

                        logger.debug("doing byte stream transfer: " + in + ", " + targetStream + ", " + size);
                        byteStreamTransfer(in, targetStream, size);

                        // send '\0'
                        logger.debug("sending: " + 0);
                        buf[0] = 0;
                        out.write(buf, 0, 1);
                        out.flush();
                    }

                    return;
                } catch (Throwable t) {
                    operationError = t;
                    logger.warn("run", t);
                } finally {
                    if (channel != null) {
                        logger.debug(session + ", disconnecting channel after " + getCommand());
                        channel.disconnect();
                    }
                    try {
                        if (targetStream instanceof FileOutputStream)
                            targetStream.close();
                    } catch (Throwable e) {
                        logger.error("problem closing target stream", e);
                    }
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    logger.info("interrupted", e);
                }
            }
        }

        @Override
        protected String getCommand() {
            return "scp -f " + source;
        }
    }

    private static class SCPTo extends SCPTransfer {

        private InputStream sourceStream;

        public void run() {
            for (int c = 0; c < 5; c++) {
                error = new StringBuffer(0);
                try {
                    // connect
                    connect();

                    logger.debug("checking ACK for: " + 0);
                    if (SshUtils.checkAck(in, error) != 0)
                        if (error.length() > 0)
                            throw new IOException("scpTo " + source + ", " + target + ": " + error.toString());

                    String name = PathUtils.getURIName(source);

                    // send "C0644 filesize source"
                    logger.debug("sending: C0644 filesize source");
                    String command = "C0644 " + size + " " + name + NonNLSConstants.LINE_SEP;
                    out.write(command.getBytes());
                    out.flush();

                    logger.debug("checking ACK for: " + 0);
                    if (SshUtils.checkAck(in, error) != 0)
                        if (error.length() > 0)
                            throw new IOException("scpTo " + source + ", " + target + ": " + error.toString());

                    logger.debug("doing byte stream transfer: " + sourceStream + ", " + out + ", " + size);
                    byteStreamTransfer(sourceStream, out, size);

                    // send '\0'
                    logger.debug("sending: " + 0);
                    buf[0] = 0;
                    out.write(buf, 0, 1);
                    out.flush();

                    logger.debug("checking ACK for: " + 0);
                    if (SshUtils.checkAck(in, error) != 0)
                        if (error.length() > 0)
                            throw new IOException("scpTo " + source + ", " + target + ": " + error.toString());
                    return;
                } catch (Throwable t) {
                    operationError = t;
                    logger.warn("run", t);
                } finally {
                    if (channel != null) {
                        logger.debug(session + ", disconnecting channel after " + getCommand());
                        channel.disconnect();
                    }
                    try {
                        if (sourceStream instanceof FileInputStream)
                            sourceStream.close();
                    } catch (Throwable e) {
                        logger.error("problem closing source stream", e);
                    }
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    logger.info("interrupted", e);
                }
            }
        }

        @Override
        protected String getCommand() {
            return "scp -t " + target;
        }
    }

    private static abstract class SCPTransfer implements Runnable {

        protected SSHSession   session;
        protected Channel      channel;
        protected InputStream  in;
        protected OutputStream out;
        protected String       source;
        protected String       target;
        protected int          retry = 0;
        protected long         size  = NonNLSConstants.UNDEFINED;
        protected byte[]       buf   = new byte[NonNLSConstants.STREAM_BUFFER_SIZE];
        protected StringBuffer error;
        protected Throwable    operationError;

        protected void connect() throws Throwable {
            while (true)
                try {
                    channel = session.getExecChannel();
                    ((ChannelExec) channel).setCommand(getCommand());
                    out = channel.getOutputStream();
                    in = channel.getInputStream();
                    channel.connect();
                    logger.debug("connected " + getCommand());
                    break;
                } catch (Throwable t) {
                    if (retry++ < 2)
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ignored) {}
                    else
                        throw t;
                }
        }

        protected abstract String getCommand();
    }

    private static final Logger logger = LoggerFactory.getLogger(SshUtils.class);

    /**
     * Static utility class cannot be constructed.
     */
    private SshUtils() {}

    /**
     * Reads from the input stream; checks for status of ACK. Errors are written
     * to the string buffer.
     */
    public static int checkAck(InputStream in, StringBuffer err) throws IOException {
        int b = in.read();
        if (b == 1 || b == 2)
            if (err != null) {
                int c = 0;
                while (c != '\n') {
                    c = in.read();
                    err.append((char) c);
                }
            }
        return b;
    }

    /**
     * Copy remote to local. Not threaded.
     */
    public static void copyFrom(String remotePath, String localPath, SSHSession session) throws Throwable {
        logger.debug("copyFrom, " + remotePath + " " + localPath + ", session " + session);
        SCPFrom scpFrom = new SCPFrom();
        File targetFile = new File(localPath);
        scpFrom.targetStream = new FileOutputStream(targetFile);
        scpFrom.session = session;
        scpFrom.source = remotePath;
        scpFrom.target = localPath;
        scpFrom.run();
        if (scpFrom.operationError != null)
            throw scpFrom.operationError;
    }

    /**
     * Copy local to remote. Not threaded.
     */
    public static void copyTo(String localPath, String remotePath, SSHSession session) throws Throwable {
        logger.debug("copyTo, " + localPath + " " + remotePath + ", session " + session);
        SCPTo scpTo = new SCPTo();
        File sourceFile = new File(localPath);
        scpTo.sourceStream = new FileInputStream(sourceFile);
        scpTo.session = session;
        scpTo.source = localPath;
        scpTo.target = remotePath;
        scpTo.size = sourceFile.length();
        scpTo.run();
        if (scpTo.operationError != null)
            throw scpTo.operationError;
    }

    /**
     * Calls rm -rf.
     */
    public static void deleteAll(String path, boolean force, SSHSession session) throws IllegalArgumentException, Exception {
        if (path == null)
            return;
        String command = force ? "rm -rf " + path : "rm -r " + path;
        exec(session, command);
    }

    /**
     * Calls rm.
     */
    public static void deleteFile(String path, boolean force, SSHSession session) throws IllegalArgumentException, Exception {
        if (path == null)
            return;
        String command = force ? "rm -f " + path : "rm " + path;
        exec(session, command);
    }

    /**
     * Execs process without reading output stream, but throws exception if
     * error stream has been written to.
     */
    public static void exec(SSHSession session, String command) throws IllegalArgumentException, Exception {
        if (command == null)
            throw new IllegalArgumentException("SshUtils.exec: command was null");
        OutputPipe err = new OutputPipe();
        exec(session, command, null, err);
        String error = err.readReply().trim();
        if (error.length() > 0)
            throw new Exception("SshUtils.exec( " + command + " ): " + error);
    }

    /**
     * Executes a single command using the given session; does not disconnect
     * session. Writes to output and error streams.
     */
    public static void exec(SSHSession session, String command, OutputStream out, OutputStream err) throws IllegalArgumentException, Exception {
        if (session == null)
            throw new IllegalArgumentException("ssh utils, exec: session object was null");

        Channel channel = null;
        try {
            channel = session.getExecChannel();
            ((ChannelExec) channel).setCommand(command);
            if (out != null)
                channel.setOutputStream(out);
            if (err != null)
                ((ChannelExec) channel).setErrStream(err);
            channel.connect();

            while (!channel.isEOF())
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {}
            logger.debug(session + ", ssh exec " + command + ", EOF reached ...");
        } catch (Throwable jsche) {
            throw new Exception("ssh exec " + command, jsche);
        } finally {
            if (channel != null)
                channel.disconnect();
            logger.debug(session + ", disconnected channel after exec of " + command);
        }
    }

    /**
     * Execs process, reading output stream into the string buffer; throws
     * exception if error stream has been written to.
     */
    public static void exec(SSHSession session, String command, StringBuffer stdout, StringBuffer stderr) throws IllegalArgumentException, Exception {
        if (command == null)
            throw new IllegalArgumentException("SshUtils.exec: command was null");
        OutputPipe err = new OutputPipe();
        OutputPipe out = new OutputPipe();
        exec(session, command, out, err);
        StringBuffer error = stderr;
        if (error == null)
            error = new StringBuffer();
        String errreply = err.readReply();
        if (errreply.trim().length() > 0)
            if (stderr == null)
                throw new Exception("SshUtils.exec( " + command + " ): " + errreply.trim());
        error.append(errreply);
        stdout.append(out.readReply());
    }

    /**
     * Checks for existence of path.
     */
    public static boolean exists(String path, SSHSession session) throws NoSuchElementException, IllegalArgumentException, Exception {
        OutputPipe err = new OutputPipe();
        exec(session, "ls -la " + path, null, err);

        /*
         * Note that flush is called by the Jsch exec code, so it should not be
         * necessary here.
         */
        String error = err.readReply();

        if (error.length() > 0) {
            if (error.indexOf("o such file or directory") > 0 || error.indexOf("not exist") > 0 || error.indexOf("n't exist") > 0)
                return false;
            throw new NoSuchElementException("SshUtils.exists " + path + ": " + error);
        }
        return true;
    }

    /**
     * Checks for existence of directory.
     */
    public static boolean isDirectory(String path, SSHSession session) throws NoSuchElementException, IllegalArgumentException, Exception {
        OutputPipe out = new OutputPipe();
        exec(session, "ls -l " + path, out, null);
        String reply = out.readReply().trim();
        if (reply.startsWith("total"))
            return true;
        String symlink = PathUtils.extractSymlink(reply);
        if (symlink != null)
            return isDirectory(symlink, session);
        return false;
    }

    /**
     * Checks for existence of file.
     */
    public static boolean isFile(String path, SSHSession session) throws NoSuchElementException, IllegalArgumentException, Exception {
        OutputPipe out = new OutputPipe();
        exec(session, "ls -l " + path, out, null);
        String reply = out.readReply().trim();
        if (reply.startsWith("-"))
            return true;
        String symlink = PathUtils.extractSymlink(reply);
        if (symlink != null)
            return !isDirectory(symlink, session);
        return false;
    }

    /**
     * Makes directory and all its parents.
     */
    public static void mkdirs(String path, SSHSession session) throws IllegalArgumentException, Exception {
        if (path == null)
            return;
        synchronized (SshUtils.class) {
            exec(session, "mkdir -p " + path);
        }
    }

    /**
     * For use on same file system instead of copy + delete.
     */
    public static void mv(String from, String to, SSHSession session) throws IllegalArgumentException, Exception {
        if (from == null || to == null)
            return;
        exec(session, "mv " + from + " " + to);
    }

    /**
     * Calls rmdir.
     */
    public static void removeDir(String path, SSHSession session) throws IllegalArgumentException, Exception {
        if (path == null)
            return;
        exec(session, "rmdir " + path);
    }

    /**
     * Calls touch -m -t. Does not make non-existent parent directories.
     */
    public static void touch(String path, SSHSession session, long timestamp) throws IllegalArgumentException, Exception {
        if (path == null)
            return;
        String format = "yyyyMMddhhmm.ss";
        String date = DateUtils.getDateString(DateUtils.getDate(timestamp), format);
        String command = "touch -m -t " + date + " " + path;
        exec(session, command);
    }

    /**
     * Reads from input stream and writes to output stream. Does not close the
     * streams.
     * 
     * @param in
     *            stream from which to read.
     * @param out
     *            stream to which to write.
     * @param size
     *            of source.
     * @return size of target (actual bytes transferred).
     */
    protected static long byteStreamTransfer(InputStream in, OutputStream out, long size) throws Throwable {
        long total = 0;
        byte[] buffer = new byte[NonNLSConstants.COPY_BUFFER_SIZE];
        int recvd = 0;
        int toWrite = 0;

        try {
            while (size == NonNLSConstants.UNDEFINED || total < size) {
                recvd = in.read(buffer, 0, NonNLSConstants.COPY_BUFFER_SIZE);
                if (recvd == -1)
                    break;
                if (size == NonNLSConstants.UNDEFINED)
                    toWrite = recvd;
                else {
                    toWrite = (int) (size - total);
                    toWrite = recvd > toWrite ? toWrite : recvd;
                }
                out.write(buffer, 0, toWrite);
                total += toWrite;
            }
        } catch (IOException ioe) {
            throw new Throwable("byteStreamTransfer", ioe);
        } finally {
            try {
                out.flush();
            } catch (IOException ignore) {}
        }
        return total;
    }

    public static SSHSession maybeGetSession(URI contact, String user, String userHome) throws Exception {
        String scheme = contact.getScheme();
        if (scheme != null && scheme.indexOf(NonNLSConstants.SSH) >= 0) {
            SSHInfo info = new SSHInfo();
            info.setUser(user);
            info.setSshHome(new File(userHome, NonNLSConstants.DOT_SSH));
            return new SSHSession(contact, info);
        }
        return null;
    }
}
