package edu.illinois.ncsa.datawolf.executor.hpc.util;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemUtils {
    private static Logger      logger = LoggerFactory.getLogger(SystemUtils.class);
    public static final Random random = new Random(System.currentTimeMillis());

    /**
     * Execs command directly.
     * 
     * @param command
     * @param env
     * @param dir
     * @param wait
     * @throws IOException
     */
    public static void doExec(List<String> command, String[] env, File dir, boolean wait, StringBuffer stdout, StringBuffer stderr) throws IOException {
        int exit = 0;
        Process p = null;
        Thread[] streams = new Thread[2];
        StringBuffer exception = null;
        if (stderr == null) {
            exception = new StringBuffer();
            stderr = exception;
        }
        try {
            Runtime r = Runtime.getRuntime();
            logger.debug("executing: " + Arrays.asList(command));
            if (env == null && dir == null)
                p = r.exec(command.toArray(new String[0]));
            else if (dir == null)
                p = r.exec(command.toArray(new String[0]), env);
            else
                p = r.exec(command.toArray(new String[0]), env, dir);
            streams[0] = streamConsumer(new InputStreamReader(p.getInputStream()), false, stdout);
            streams[1] = streamConsumer(new InputStreamReader(p.getErrorStream()), true, stderr);
            streams[0].start();
            streams[1].start();
            if (wait) {
                logger.debug("waiting for exit of " + p);
                exit = p.waitFor();
            }
        } catch (InterruptedException ignored) {} catch (Throwable io) {
            throw new IOException(command + " failed to execute:\n" + ExceptionUtils.getStackTrace(io));
        } finally {
            if (wait)
                destroy(p, streams);
        }
        if (wait && (0 != exit || (exception != null && exception.length() > 0)))
            throw new IOException(command + " exited with " + exit + ": " + exception);
    }

    /**
     * Safety feature: checks to make sure the calling thread is not the thread
     * on which the join is called, to avoid deadlock.
     * 
     * @param thread
     *            to join on.
     */
    public static void join(Thread thread) {
        if (thread == null || Thread.currentThread() == thread)
            return;
        try {
            thread.join();
        } catch (InterruptedException ignored) {}
    }

    /**
     * Calls destroy on the process in order to guarantee release of resources
     * in underlying platform.
     * 
     * @param pd
     *            process
     * @param streamThreads
     *            of the stream consumers
     */
    public static void destroy(final Process pd, final Thread[] streamThread) {
        if (streamThread != null)
            for (int i = 0; i < streamThread.length; i++)
                join(streamThread[i]);

        if (pd != null)
            pd.destroy();
    }

    /**
     * Creates thread which consumes the stream by reading and appending to
     * buffer
     * 
     * @param reader
     *            for the input stream
     * @param err
     *            if true, stream is an error stream.
     * @param output
     *            of stream
     */
    public static Thread streamConsumer(final InputStreamReader reader, final boolean err, final StringBuffer output) {
        return new Thread(reader + (err ? "err-thread" : "out-thread")) {

            @Override
            public void run() {
                char[] buffer = new char[NonNLSConstants.STREAM_BUFFER_SIZE];
                int numBytes;

                while (true) {
                    numBytes = 0;
                    try {
                        numBytes = reader.read(buffer, 0, NonNLSConstants.STREAM_BUFFER_SIZE);
                    } catch (EOFException eofe) {
                        break;
                    } catch (IOException ioe) {
                        logger.error("streamConsumer", ioe);
                        return;
                    }
                    if (numBytes == NonNLSConstants.EOF)
                        break;
                    if (output != null)
                        output.append(buffer, 0, numBytes);
                }

                try {
                    reader.close();
                } catch (IOException ignored) {}
            }
        };
    }
}
