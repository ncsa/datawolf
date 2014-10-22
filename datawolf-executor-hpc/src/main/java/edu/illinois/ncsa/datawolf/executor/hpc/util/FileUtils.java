/******************************************************************************
 * Copyright 2004-2010 The Board of Trustees of the University of Illinois. 
 * All rights reserved.
 * 
 * Contributors:
 *    Albert L. Rossi:  original design and implementation
 ******************************************************************************/
package edu.illinois.ncsa.datawolf.executor.hpc.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

/**
 * Wrapper static methods for doing reads and writes using RandomAccessFile
 * object.
 * 
 * @author Albert L. Rossi
 */
public class FileUtils {
    /**
     * Static utility class; cannot be constructed.
     */
    private FileUtils() {}

    /**
     * Counts the number of lines in the file.
     * 
     * @param file
     *            to read.
     * @return number of lines in file.
     */
    public static long countLines(File file) throws Exception {
        long lineCount = 0;
        String line = null;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file), NonNLSConstants.READER_BUFFER_SIZE);
            while (true) {
                try {
                    line = br.readLine();
                } catch (IOException ioe) {
                    throw new Exception("Read.readLines", ioe);
                }
                if (line == null)
                    break;
                lineCount++;
            }
        } catch (Throwable t) {
            throw new Exception("problem reading lines for " + file, t);
        } finally {
            if (br != null)
                try {
                    br.close();
                } catch (IOException ignore) {}
        }
        return lineCount;
    }

    /**
     * Checksum using java.util.zip.
     */
    public static long getChecksumValue(String path) throws IOException {
        Checksum checksum = new CRC32();
        BufferedInputStream is = null;
        try {
            is = new BufferedInputStream(new FileInputStream(path));
            byte[] bytes = new byte[NonNLSConstants.READER_BUFFER_SIZE];
            int len = 0;
            while ((len = is.read(bytes)) >= 0)
                checksum.update(bytes, 0, len);
        } catch (IOException e) {
            throw e;
        } finally {
            if (is != null)
                try {
                    is.close();
                } catch (IOException ignore) {}
        }
        return checksum.getValue();
    }

    public static String read(File file, long startOffset, long numBytes) throws Exception {
        if (numBytes == 0)
            return "";

        if (startOffset > file.length())
            throw new Exception("startOffset " + startOffset + " is past the end of the file: " + file.length());

        if (numBytes < 0 || file.length() - startOffset < numBytes)
            numBytes = file.length() - startOffset;

        int read = 0;
        long left = 0;
        byte[] bytes = new byte[NonNLSConstants.READER_BUFFER_SIZE];
        StringBuffer sb = new StringBuffer();
        FileInputStream stream = null;

        try {
            stream = new FileInputStream(file);
            left = startOffset;
            int toRead = bytes.length;

            // read up to startOffset
            while (left > 0) {
                if (left < toRead)
                    toRead = (int) left;
                read = stream.read(bytes, 0, toRead);
                left -= read;
            }

            left = numBytes;

            while (left > 0) {
                try {
                    read = stream.read(bytes, 0, bytes.length);
                } catch (EOFException eof) {
                    break;
                }
                if (read == 0)
                    continue;
                if (read > left) {
                    // if left is less than read, it is an integer
                    sb.append(new String(bytes, 0, (int) left));
                    break;
                }
                sb.append(new String(bytes, 0, read));
                left -= read;
            }
        } catch (Throwable t) {
            throw new Exception("could not read " + file, t);
        } finally {
            if (stream != null)
                try {
                    stream.close();
                } catch (IOException t) {
                    throw new Exception(t);
                }
        }
        return sb.toString();
    }

    public static byte[] readBytes(File file, int start, int numBytes) throws Exception {
        String byteString = read(file, start, numBytes);
        return byteString.getBytes();
    }

    public static List readLines(File file, long firstLine, long numLines, boolean includeEmptyLines) throws Exception {
        List temp = new ArrayList();
        String line = null;
        long lineCount = countLines(file);

        // TAIL
        if (firstLine == NonNLSConstants.UNDEFINED) {
            if (numLines == NonNLSConstants.UNDEFINED)
                numLines = lineCount;
            else if (numLines < 0)
                throw new Exception("bad number of lines: " + numLines);
            firstLine = numLines == 0 ? 0 : lineCount - numLines;
        } else if (numLines == NonNLSConstants.UNDEFINED)
            numLines = lineCount - firstLine;
        else if (numLines < 0)
            throw new Exception("bad number of lines: " + numLines);

        if (firstLine < 0 || firstLine > lineCount)
            throw new Exception("bad first line " + firstLine + ", number of lines: " + lineCount);

        if (firstLine + numLines > lineCount)
            throw new Exception("bad number of lines: last line number " + (firstLine + numLines - 1) + ", number of lines: " + lineCount);

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file), NonNLSConstants.READER_BUFFER_SIZE);
            for (int i = 0; i < firstLine; i++) {
                line = br.readLine();
                if (line == null)
                    break;
            }

            for (int i = 0; i < numLines; i++) {
                line = br.readLine();
                if (line == null)
                    break;
                if (!line.trim().equals("") || includeEmptyLines)
                    temp.add(line);
            }
        } catch (OutOfMemoryError error) {
            throw new Exception(file + " too big to read into memory", error);
        } catch (IOException ioe) {
            throw new Exception("Read.readLines", ioe);
        } finally {
            if (br != null)
                try {
                    br.close();
                } catch (IOException ignore) {}
        }

        return temp;
    }

    public static List readLinesFromEnd(File file, long numLines, boolean includeEmptyLines) throws Exception {
        return readLines(file, NonNLSConstants.UNDEFINED, numLines, includeEmptyLines);
    }

    public static List readLinesFromStart(File file, long numLines, boolean includeEmptyLines) throws Exception {
        return readLines(file, 0, numLines, includeEmptyLines);
    }

    public static void sync(File file) throws Throwable {
        if (file.isDirectory())
            return;
        FileInputStream fis = new FileInputStream(file);
        fis.getFD().sync();
        fis.close();
    }

    public static void writeBinary(File file, List content, boolean append, String mode) throws Exception {
        RandomAccessFile raf = null;

        try {
            raf = new RandomAccessFile(file, mode);
        } catch (FileNotFoundException t1) {
            throw new Exception(t1);
        }

        try {
            if (append)
                raf.seek(raf.length());
            else {
                raf.setLength(0);
                raf.seek(0);
            }
        } catch (IOException ioe) {
            throw new Exception("FileUtils.write, could not call seek on " + raf, ioe);
        }

        try {
            for (ListIterator it = content.listIterator(); it.hasNext();) {
                Object o = it.next();
                if (o == null)
                    raf.writeBytes(null);
                else if (o instanceof Boolean)
                    raf.writeBoolean(((Boolean) o).booleanValue());
                else if (o instanceof Byte)
                    raf.writeByte(((Byte) o).intValue());
                else if (o instanceof Character)
                    raf.writeChar(((Character) o).charValue());
                else if (o instanceof Double)
                    raf.writeDouble(((Double) o).doubleValue());
                else if (o instanceof Float)
                    raf.writeFloat(((Float) o).floatValue());
                else if (o instanceof Integer)
                    raf.writeInt(((Integer) o).intValue());
                else if (o instanceof Long)
                    raf.writeLong(((Long) o).longValue());
                else if (o instanceof Short)
                    raf.writeShort(((Short) o).shortValue());
                else
                    raf.writeBytes(o.toString());
            }
        } catch (IOException ioe) {
            throw new Exception("FileUtils.write", ioe);
        } finally {
            try {
                raf.getFD().sync();
                raf.close();
            } catch (IOException t) {}
        }
    }

    public static void writeBytes(File file, byte[] bytes, boolean append) throws Exception {
        try {
            FileOutputStream stream = new FileOutputStream(file, append);
            stream.write(bytes);
            stream.flush();
            stream.close();
        } catch (IOException t) {
            throw new Exception("could not write to " + file + ": " + new String(bytes), t);
        }
    }

    public static void writeLines(File file, List lines, boolean append) throws Exception {
        writeLines(file, (String[]) lines.toArray(new String[0]), append, true);
    }

    public static void writeLines(File file, List lines, boolean append, boolean includeEmpty) throws Exception {
        writeLines(file, (String[]) lines.toArray(new String[0]), append, includeEmpty);
    }

    public static void writeLines(File file, String[] lines, boolean append, boolean includeEmpty) throws Exception {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, append));
            for (int i = 0; i < lines.length; i++)
                if (includeEmpty || (lines[i] != null && !"".equals(lines[i].trim()))) {
                    writer.write(lines[i]);
                    writer.write(NonNLSConstants.LINE_SEP);
                }
            writer.flush();
            writer.close();
        } catch (IOException t) {
            throw new Exception("could not write lines to " + file, t);
        }
    }

    /**
     * If wspc is true, skips whitespace until it finds the first matching
     * character; else, skips past all matching characters until it finds the
     * first non-match.
     * 
     * @param file
     *            as initialized by execute method.
     * @param match
     *            array of characters which are valid matches.
     * @param wspc
     *            true = skip whitespace to first match; else skip to first
     *            non-matching.
     * @return the file pointer value at return.
     * @throws NCSAException
     *             if an IOException is thrown getting the current file pointer.
     */
    private static long skip(RandomAccessFile file, char[] match, boolean wspc) throws IOException {
        long pos = file.getFilePointer();

        while (true) {
            char c = 0;
            try {
                c = file.readChar();
                boolean matches = false;
                for (int i = 0; i < match.length; i++)
                    if (c == match[i]) {
                        matches = true;
                        break;
                    }
                if (wspc) {
                    if (!matches)
                        break;
                    pos = file.getFilePointer();
                } else {
                    pos = file.getFilePointer();
                    if (matches)
                        break;
                }
            } catch (IOException ieo) {
                // either end of file or next byte is not a char
                break;
            }
        }
        return pos;
    }
}
