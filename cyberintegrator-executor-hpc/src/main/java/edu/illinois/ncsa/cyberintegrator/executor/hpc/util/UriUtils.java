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
import java.net.URL;

/**
 * Static auxiliary methods for dealing with java.net.URI.
 * 
 * @author Albert L. Rossi
 */
public class UriUtils {
    /**
     * Static utility class cannot be constructed.
     */
    private UriUtils() {}

    /**
     * Constructs new uri from old, updating userInfo.
     * 
     * @param uri
     *            from which to construct updated uri.
     * @param userInfo
     *            new user information.
     * @return new URI.
     */
    public static URI addUserInfo(URI uri, String userInfo) throws URISyntaxException {
        return getUri(uri.getScheme(), userInfo, uri.getHost(), uri.getPort(), uri.getPath());
    }

    /**
     * SDH 01/06/09: In following with the revelations of isUri, this method
     * also needs to be check out. So I'll deprecate it to make nasty yellow
     * marks in any code that uses it.
     * 
     * @param path
     *            which may or may not be a URI
     * @return a valid URI
     * @throws URISyntaxException
     *             ALR 01/07/09: The fix of isUri allows us to remove the
     *             deprecation here.
     */
    public static URI ensureUri(String path) throws URISyntaxException {
        if (isHierarchicalUri(path))
            // assume URI
            return new URI(path);
        return new File(path).toURI();
    }

    /**
     * For local file conversion. FIXME is this correct on Windows?
     * 
     * @param url
     *            to convert
     * @return file from url
     */
    public static File getFile(URL url) {
        File f = null;
        try {
            f = new File(url.toURI());
        } catch (URISyntaxException e) {
            f = new File(url.getPath());
        }
        return f;
    }

    /**
     * @return scheme+host+port.
     */
    public static String getKey(URI uri) {
        String s = uri.getScheme();
        String h = uri.getHost();
        int p = uri.getPort();
        if (s == null)
            s = "file";
        if (h == null)
            h = "";
        if (p == -1)
            return s + h;
        return s + h + p;
    }

    /**
     * Constructs the uri string and creates a URI object. Encodes the path
     * (calls the 7-parameter URI constructor).
     * 
     * @param s
     *            scheme.
     * @param h
     *            host name.
     * @param p
     *            port number.
     * @param path
     *            of resource.
     * @return URI object.
     */
    public static URI getUri(String s, String h, int p, String path) throws URISyntaxException {
        return getUri(s, null, h, p, path);
    }

    /**
     * Constructs the uri string and creates a URI object. Encodes the path
     * (calls the 7-parameter URI constructor).
     * 
     * @param s
     *            scheme.
     * @param u
     *            user info.
     * @param h
     *            host name.
     * @param p
     *            port number.
     * @param path
     *            of resource.
     * @return URI object.
     */
    public static URI getUri(String s, String u, String h, int p, String path) throws URISyntaxException {
        if (s == null)
            s = "file";
        if ("file".equals(s)) {
            h = null;
            p = -1;
        }
        if (path == null || path.equals(""))
            path = "/";
        return new URI(s, u, h, p, path, null, null);
    }

    /**
     * Constructs new uri from old, swapping out the old scheme for the new.
     * 
     * @param s
     *            scheme.
     * @param old
     *            URI object.
     * @return new URI object.
     */
    public static URI getUri(String s, URI old) throws URISyntaxException {
        String h = old.getHost();
        if (s == null || s.equals(""))
            s = "file";
        if (s.equals("file"))
            h = null;
        return new URI(s, old.getUserInfo(), h, old.getPort(), old.getPath(), old.getQuery(), old.getFragment());
    }

    /**
     * Constructs new uri from old, updating path.
     * 
     * @param uri
     *            from which to construct updated uri.
     * @param path
     *            new URI-consistent path.
     * @return new URI.
     */
    public static URI getUri(URI uri, String path) throws URISyntaxException {
        return getUri(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), path);
    }

    /**
     * Constructs the uri from the url using protocol, host, port and path.
     * 
     * @param url
     *            to convert.
     * @return uri from url
     */
    public static URI getUri(URL url) throws URISyntaxException {
        return getUri(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath());
    }

    public static boolean haveSameConnection(URI uri0, URI uri1) {
        if (uri0 == null)
            return uri1 == null;
        if (uri1 == null)
            return false;
        String s0 = uri0.getScheme();
        String s1 = uri1.getScheme();
        String h0 = uri0.getHost();
        String h1 = uri1.getHost();
        return (s0 != null && s0.equals(s1)) && ((h0 == null && h1 == null) || (h0 != null && h0.equals(h1))) && (uri0.getPort() == uri1.getPort());
    }

    /**
     * SDH 01/06/09: This method shouldn't be used. It just plain doesn't work
     * on Windows based files as c:/foo/bar.xml will show up as a URI but it
     * isn't. ALR 01/07/09: Undeprecated method after correcting it. To capture
     * Windows paths, we use the following logic: We search for the first index
     * of ":/". If the index is -1, obviously there is no schema, so we do not
     * have a URI. The index cannot be 0, because a string like ":/u/ncsa/foo"
     * will throw a URISyntaxException; If the index is 1, this is likely a
     * Windows path, so not a uri (note we do not try to validate single char
     * schemas, and particularly whether the schema itself is just a slash...).
     * If the prefix up to index contains "/", this is not a uri. Else, we have
     * a hierarchical uri.
     */
    public static boolean isHierarchicalUri(String path) {
        if (path == null)
            return false;
        int firstColonSlash = path.indexOf(":/");
        return firstColonSlash > 1 && path.substring(0, firstColonSlash).indexOf("/") < 0;
    }

    /**
     * @return whether this URI represents a resource.
     */
    public static boolean isResource(URI uri) {
        String scheme = uri.getScheme();
        return scheme.equals("resource") || scheme.equals("classpath") || scheme.startsWith("jar");
    }

    /**
     * Constructs new uri from old, removing the userInfo.
     * 
     * @param uri
     *            from which to construct updated uri.
     * @return new URI.
     */
    public static URI removeUserInfo(URI uri) throws URISyntaxException {
        return getUri(uri.getScheme(), uri.getHost(), uri.getPort(), uri.getPath());
    }
}
