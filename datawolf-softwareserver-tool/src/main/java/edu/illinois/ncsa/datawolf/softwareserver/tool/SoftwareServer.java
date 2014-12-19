package edu.illinois.ncsa.datawolf.softwareserver.tool;

public interface SoftwareServer {
    public static final String TASK_CONVERT            = "/convert";
    public static final String SOFTWARE                = "software";
    public static final int    ATTEMPTS                = 10;
    public static final int    WAIT_TIME_MS            = 2000;
    public static final String HTML_A_TAG_PATTERN      = "(?i)<a([^>]+)>(.+?)</a>";
    public static final String HTML_A_HREF_TAG_PATTERN = "\\s*(?i)href\\s*=\\s*(\"([^\"]*\")|'[^']*'|([^'\">\\s]+))";
}
