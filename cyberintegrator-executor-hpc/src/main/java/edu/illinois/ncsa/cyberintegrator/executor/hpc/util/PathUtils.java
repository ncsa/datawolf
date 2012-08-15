/******************************************************************************
 * Copyright 2004-2010 The Board of Trustees of the University of Illinois. 
 * All rights reserved.
 * 
 * Contributors:
 *    Albert L. Rossi:  original design and implementation
 ******************************************************************************/
package edu.illinois.ncsa.cyberintegrator.executor.hpc.util;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Management of file paths. These methods attempt to guarantee consistency by
 * going through the URI and File (for local paths) objects.
 * 
 * @author Albert L. Rossi
 */
public class PathUtils {
    /**
     * Static utility class cannot be constructed.
     */
    private PathUtils() {}

    /**
     * Changes the path in base to newPath.
     * 
     * @param base
     *            uri to which to concatenate the subpath.
     * @param newPath
     *            to replace the base path.
     * @return URI with changed path.
     */
    public static URI changePath(URI base, String newPath) throws URISyntaxException {
        if (base == null)
            throw new URISyntaxException("cannot replace " + newPath + " in " + base, null);

        return UriUtils.getUri(base.getScheme(), base.getUserInfo(), base.getHost(), base.getPort(), newPath);
    }

    /**
     * If the sub path is <code>null</code>, the base is returned; else a new
     * URI with the concatenated path is returned.
     * <p>
     * 
     * All backslashes (\) will be replaced by forward slashes (/) in the
     * subpath before the path is given to the URI constructor.
     * 
     * @param base
     *            uri to which to concatenate the subpath.
     * @param subPath
     *            to affix to the base.
     * @return URI with concatenated path.
     */
    public static URI concatenate(URI base, String subPath) throws URISyntaxException {
        if (base == null)
            throw new URISyntaxException("cannot concatenate " + subPath + " to " + base, PathUtils.class.getName());

        String basePath = base.getPath();

        if (basePath.endsWith("/"))
            if (subPath != null)
                basePath = basePath.substring(0, basePath.length() - 1);

        StringBuffer sb = new StringBuffer();
        sb.append(basePath);

        if (subPath != null) {
            sb.append("/");
            subPath.replace('\\', '/');
            if (subPath.startsWith("/"))
                subPath = subPath.substring(1);
            sb.append(subPath);
        }

        return UriUtils.getUri(base.getScheme(), base.getUserInfo(), base.getHost(), base.getPort(), sb.toString());
    }

    /**
     * Constructs a duplicate file name from original plus given number tag.
     * 
     * @param path
     *            for which to find the tag.
     * @param dupNum
     *            tag to add.
     * @return modified name with numbered tag.
     * @throws URISyntaxException
     */
    public static String duplicateName(String path, int dupNum) throws URISyntaxException {
        if (dupNum == 0)
            return path;

        URI uri = new File(path).toURI();
        String name = getName(uri);
        URI parent = getParent(uri);
        int index = name.lastIndexOf(".");
        if (index == 0)
            index = name.substring(1).lastIndexOf(".");
        String prefix = name;
        String suffix = "";
        if (index >= 0) {
            prefix = name.substring(0, index);
            suffix = name.substring(index);
        }
        if (suffix.indexOf("/") >= 0) {
            prefix = name;
            suffix = "";
        }

        uri = concatenate(parent, prefix + "_" + dupNum + suffix);
        return getNormalizedPath(uri);
    }

    public static URI[] enumerateAncestorsExcludeSelf(URI uri) throws URISyntaxException {
        List l = new ArrayList();
        URI parent = PathUtils.getParent(uri);
        while (true) {
            if (parent == null || "/".equals(parent.getPath()))
                break;
            l.add(0, parent);
            parent = PathUtils.getParent(parent);
        }
        return (URI[]) l.toArray(new URI[0]);
    }

    /**
     * Presumes listLine follows standard UNIX formatting, with 'l' as first
     * character on line, and symlink indicated by '->'. Composes relative
     * symlink into the apparent parent.
     */
    public static String extractSymlink(String listLine) {
        if (listLine == null)
            return null;
        if (!listLine.startsWith("l"))
            return null;
        int i = listLine.indexOf("->");
        if (i < 0)
            return null;
        String link = listLine.substring(i + 2).trim();
        if (!link.startsWith("/")) {
            int j = i - 2;
            for (; j >= 0 && listLine.charAt(j) != ' '; j--)
                ;
            link = listLine.substring(j, i - 2).trim();
        }
        return link;
    }

    /**
     * Tries to determine the path from root to the nearest common ancestor of
     * the two uri paths, if any. URIs are assumed to be absolute. Returned
     * paths are consistent with URI.getPath().
     * 
     * @param u0
     *            file or directory uri.
     * @param u1
     *            file or directory uri.
     * 
     * @return an array of three strings representing: 0: the common path from
     *         root to nearest common ancestor; 1: the remainder of the path in
     *         u0 from ancestor to end; 2: the remainder of the path in u1 from
     *         ancestor to end; if there is no common ancestor, ["", path0,
     *         path1] is returned; if the shared path coincides with either u0
     *         or u1, that value will be "" in the returned array.
     */
    public static String[] findCommonPrefix(URI u0, URI u1) {
        return commonPrefix(u0, u1);

    }

    /**
     * Derives name from uri. If the uri is local, this is done through the File
     * interface; else calls getURIName.
     * 
     * @param uri
     *            whose path to get name of.
     * @return name of file or directory.
     */
    public static String getName(URI uri) {
        if (uri == null)
            return null;
        if ("file".equals(uri.getScheme()))
            return new File(uri).getName();
        return getURIName(uri);
    }

    /**
     * @return File.getAbsolutePath(), if the uri is local; else uri.getPath().
     */
    public static String getNormalizedPath(URI uri) {
        if (uri == null)
            return null;
        if (uri.getScheme().equals("file"))
            return new File(uri).getAbsolutePath();
        return uri.getPath();
    }

    /**
     * Derives parent uri from uri. If the uri is local, this is done through
     * the File interface; else the uri path is parsed using '/' as separator.
     * 
     * @param uri
     *            to get parent of.
     * @return parent, if one exists; else null.
     */
    public static URI getParent(URI uri) throws URISyntaxException {
        if (uri == null)
            return null;

        if (uri.getScheme().equals("file")) {
            File f = new File(uri);
            File parent = f.getParentFile();
            if (parent == null)
                return null;
            return parent.toURI();
        }

        String parent = getURIParent(uri.getPath());

        return UriUtils.getUri(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), parent);
    }

    /**
     * Determines the relative path of the second URI with respect to the first.
     * This path is always consistent with URI.getPath().
     * 
     * @param base
     *            to which the returned path is relative.
     * @param full
     *            of which to find path relative to base.
     * 
     * @return path of file or directory relative to base.
     */
    public static String getRelativePath(URI base, URI full) throws URISyntaxException {
        return getRelativePath(base, full, false);
    }

    /**
     * Determines the relative path of the second URI with respect to the first.
     * This path is always consistent with URI.getPath().
     * 
     * @param base
     *            to which the returned path is relative.
     * @param full
     *            of which to find path relative to base.
     * @param strict
     *            if true, schema, host and ports of the two uris must match.
     * 
     * @return path of file or directory relative to base.
     */
    public static String getRelativePath(URI base, URI full, boolean strict) throws URISyntaxException {
        if (full == null)
            return base == null ? null : base.getPath();

        if (base == null) {
            if (full.toString().endsWith("/"))
                return PathUtils.getURIName(full) + "/";
            return PathUtils.getURIName(full);
        }

        if (strict && !UriUtils.haveSameConnection(full, base))
            throw new URISyntaxException(full.toString(), "connection does not match " + base.toString());
        // special case: relative to a .jar
        String relativeto = base.getPath();
        if (relativeto != null && relativeto.endsWith(".jar")) {
            String p = full.getPath();
            if (p.startsWith("/"))
                p = p.substring(1);
            return p;
        }

        String[] result = commonPrefix(base, full);
        if (!result[0].equals("") && (result[1] == null || !result[1].equals("")))
            throw new URISyntaxException(full.toString(), base.toString() + " is not the base directory");

        return result[2];
    }

    /**
     * Path is parsed using '/' as separator.
     * 
     * @param forwardSlashPath
     *            whose path to get name of.
     * @return name of file or directory.
     */
    public static String getURIName(String forwardSlashPath) {
        if (forwardSlashPath == null)
            return null;
        int end = forwardSlashPath.length();
        if (end == 0)
            return "";
        if (forwardSlashPath.charAt(end - 1) == '/')
            end = end - 1;
        if (end == 0)
            return "/";
        int index = forwardSlashPath.substring(0, end).lastIndexOf('/');
        return forwardSlashPath.substring(index + 1, end);
    }

    /**
     * Derives name from uri; calls overloaded method. the uri path is parsed
     * using '/' as separator.
     * 
     * @param uri
     *            whose path to get name of.
     * @return name of file or directory.
     */
    public static String getURIName(URI uri) {
        if (uri == null)
            return null;
        return getURIName(uri.getPath());
    }

    public static String getURIParent(String forwardSlashPath) {
        if (forwardSlashPath == null)
            return null;
        String parent;

        int end = forwardSlashPath.length();
        if (end == 0)
            return null;

        if (forwardSlashPath.charAt(end - 1) == '/')
            end = end - 1;

        if (end == 0)
            return null;

        int index = forwardSlashPath.substring(0, end).lastIndexOf('/');

        if (index == -1)
            return null;
        else if (index == 0)
            parent = "/";
        else
            parent = forwardSlashPath.substring(0, index);

        return parent;
    }

    /**
     * Retrieves the next duplicate id number from the map and modifies the name
     * accordingly.
     * 
     * @param name
     *            for which to find the tag.
     * @param duplicatePaths
     *            in which to search for duplicate paths.
     * @return modified name with next numbered tag.
     */
    public static String nextDuplicateName(String name, Map duplicatePaths) {
        int extension = name.lastIndexOf(".");
        String prefix = name;
        String suffix = "";
        if (extension >= 0) {
            prefix = name.substring(0, extension);
            suffix = name.substring(extension);
        }
        if (suffix.indexOf("/") >= 0) {
            prefix = name;
            suffix = "";
        }

        int next = 0;
        if (duplicatePaths.containsKey(prefix)) {
            Integer value = (Integer) duplicatePaths.get(prefix);
            next = value.intValue();
        }
        duplicatePaths.put(prefix, new Integer(next));
        return prefix + "_" + (next + 1) + suffix;
    }

    private static String[] commonPrefix(URI u0, URI u1) {
        if (u0 == null || u1 == null)
            return new String[] { "", null, null };

        String path0 = u0.getPath();
        String path1 = u1.getPath();

        // String[][] pathelements = { StringUtils.split(path0, "/"),
// StringUtils.split(path1, "/") };
        String[][] pathelements = { path0.split("/"), path1.split("/") };

        // check for null results
        int l0 = pathelements[0] == null ? 0 : pathelements[0].length;
        int l1 = pathelements[1] == null ? 0 : pathelements[1].length;

        // find the shorter of the two paths
        int longer = 1;
        int shorter = 0;
        if (l0 > l1) {
            longer = 0;
            shorter = 1;
        }

        // check for root
        String root;
        if (l0 == 0) {
            if (l1 > 0 && !pathelements[1][0].equals(""))
                return new String[] { "", path0, path1 };
            root = "";
        } else if (l1 == 0) {
            if (l0 > 0 && !pathelements[0][0].equals(""))
                return new String[] { "", path0, path1 };
            root = "";
        } else if (!pathelements[0][0].equals(pathelements[1][0]))
            return new String[] { "", path0, path1 };
        else
            root = pathelements[0][0];

        StringBuffer prefix = new StringBuffer(root);
        int i = 1;

        for (; i < pathelements[shorter].length; i++)
            if (pathelements[shorter][i].equals(pathelements[longer][i])) {
                prefix.append("/");
                prefix.append(pathelements[shorter][i]);
            } else
                break;

        if (prefix.toString().equals(""))
            prefix.append("/");

        // find the remaining path segments for the two paths
        StringBuffer[] suffix = { new StringBuffer(), new StringBuffer() };
        for (int j = i; j < pathelements[shorter].length; j++) {
            if (j != i)
                suffix[shorter].append("/");
            suffix[shorter].append(pathelements[shorter][j]);
        }

        for (int j = i; j < pathelements[longer].length; j++) {
            if (j != i)
                suffix[longer].append("/");
            suffix[longer].append(pathelements[longer][j]);
        }

        return new String[] { prefix.toString(), suffix[0].toString(), suffix[1].toString() };
    }
}
