package edu.illinois.ncsa.datawolf.softwareserver.tool;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Based on Tupelo MimeMap, provides common file extension -> mimetype mappings
 * 
 * @author Chris Navarro
 *
 */
public class MimeMap {

    private Map<String, String> extensions  = new HashMap<String, String>();
    public static final String  UNKNOWN_EXT = "unk";

    public MimeMap() {
        loadDefault();
    }

    public void loadDefault() {
        // add some common mime types
        addMimeTypeMapping("evy", "application/envoy", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("fif", "application/fractals", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("spl", "application/futuresplash", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("hta", "application/hta", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("acx", "application/internet-property-stream", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("hqx", "application/mac-binhex40", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("doc", "application/msword", true); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("dot", "application/msword", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("bin", "application/octet-stream", true); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("class", "application/octet-stream", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("dms", "application/octet-stream", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("exe", "application/octet-stream", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("lha", "application/octet-stream", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("lzh", "application/octet-stream", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("oda", "application/oda", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("axs", "application/olescript", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("pdf", "application/pdf", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("prf", "application/pics-rules", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("p10", "application/pkcs10", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("crl", "application/pkix-crl", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("ai", "application/postscript", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("eps", "application/postscript", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("ps", "application/postscript", true); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("rtf", "application/rtf", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("setpay", "application/set-payment-initiation", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("setreg", "application/set-registration-initiation", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("xla", "application/vnd.ms-excel", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("xlc", "application/vnd.ms-excel", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("xlm", "application/vnd.ms-excel", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("xls", "application/vnd.ms-excel", true); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("xlt", "application/vnd.ms-excel", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("xlw", "application/vnd.ms-excel", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("sst", "application/vnd.ms-pkicertstore", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("cat", "application/vnd.ms-pkiseccat", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("stl", "application/vnd.ms-pkistl", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("pot,", "application/vnd.ms-powerpoint", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("pps", "application/vnd.ms-powerpoint", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("ppt", "application/vnd.ms-powerpoint", true); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("mpp", "application/vnd.ms-project", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("wcm", "application/vnd.ms-works", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("wdb", "application/vnd.ms-works", false); //$NON-NLS-1$   //$NON-NLS-2$    
        addMimeTypeMapping("wks", "application/vnd.ms-works", true); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("wps", "application/vnd.ms-works", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("hlp", "application/winhlp", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("bcpio", "application/x-bcpio", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("cdf", "application/x-cdf", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("z", "application/x-compress", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("tgz", "application/x-compressed", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("cpio", "application/x-cpio", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("csh", "application/x-csh", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("dcr", "application/x-director", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("dir", "application/x-director", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("dxr", "application/x-director", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("dvi", "application/x-dvi", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("gtar", "application/x-gtar", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("gz", "application/x-gzip", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("hdf", "application/x-hdf", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("ins", "application/x-internet-signup", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("isp", "application/x-internet-signup", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("iii", "application/x-iphone", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("js", "application/x-javascript", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("latex", "application/x-latex", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("xml", "application/xml", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("mdb", "application/x-msaccess", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("crd", "application/x-mscardfile", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("clp", "application/x-msclip", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("dll", "application/x-msdownload", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("m13", "application/x-msmediaview", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("m14", "application/x-msmediaview", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("mvb", "application/x-msmediaview", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("wmf", "application/x-msmetafile", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("mny", "application/x-msmoney", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("pub", "application/x-mspublisher", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("scd", "application/x-msschedule", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("trm", "application/x-msterminal", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("wri", "application/x-mswrite", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("pma", "application/x-perfmon", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("pmc", "application/x-perfmon", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("pml", "application/x-perfmon", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("pmr", "application/x-perfmon", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("pmw", "application/x-perfmon", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("p12", "application/x-pkcs12", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("pfx", "application/x-pkcs12", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("p7b", "application/x-pkcs7-certificates", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("spc", "application/x-pkcs7-certificates", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("p7r", "application/x-pkcs7-certreqresp", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("p7c", "application/x-pkcs7-mime", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("p7m", "application/x-pkcs7-mime", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("p7s", "application/x-pkcs7-signature", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("sh", "application/x-sh", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("shar", "application/x-shar", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("swf", "application/x-shockwave-flash", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("sit", "application/x-stuffit", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("sv4cpio", "application/x-sv4cpio", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("sv4crc", "application/x-sv4crc", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("tar", "application/x-tar", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("tcl", "application/x-tcl", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("tex", "application/x-tex", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("texi", "application/x-texinfo", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("texinfo", "application/x-texinfo", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("obj", "application/x-tgif", true); //$NON-NLS-1$   //$NON-NLS-2$
        addMimeTypeMapping("roff", "application/x-troff", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("t", "application/x-troff", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("tr", "application/x-troff", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("man", "application/x-troff-man", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("me", "application/x-troff-me", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("ms", "application/x-troff-ms", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("avi", "application/x-troff-msvideo", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("ustar", "application/x-ustar", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("src", "application/x-wais-source", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("cer", "application/x-x509-ca-cert", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("crt", "application/x-x509-ca-cert", true); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("der", "application/x-x509-ca-cert", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("pko", "application/ynd.ms-pkipko", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("zip", "application/zip", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("au", "audio/basic", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("snd", "audio/basic", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("mid", "audio/mid", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("rmi", "audio/mid", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("mp3", "audio/mpeg", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("aif", "audio/x-aiff", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("aifc", "audio/x-aiff", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("aiff", "audio/x-aiff", true); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("m3u", "audio/x-mpegurl", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("ra", "audio/x-pn-realaudio", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("ram", "audio/x-pn-realaudio", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("wav", "audio/x-wav", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("wma", "audio/x-ms-wma", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("aac", "audio/aac", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("flac", "audio/x-flac", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("gsm", "audio/x-gsm", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("bmp", "image/bmp", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("cod", "image/cis-cod", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("gif", "image/gif", true); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("ief", "image/ief", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("jp2", "image/jp2", true); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("jpe", "image/jpeg", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("jpeg", "image/jpeg", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("jpg", "image/jpeg", true); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("jfif", "image/pipeg", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("svg", "image/svg+xml", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("tif", "image/tiff", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("tiff", "image/tiff", true); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("ras", "image/x-cmu-raster", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("cmx", "image/x-cmx", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("ico", "image/x-icon", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("pnm", "image/x-portable-anymap", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("pbm", "image/x-portable-bitmap", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("pgm", "image/x-portable-graymap", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("ppm", "image/x-portable-pixmap", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("rgb", "image/x-rgb", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("xbm", "image/x-xbitmap", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("xpm", "image/x-xpixmap", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("xwd", "image/x-xwindowdump", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("mht", "message/rfc822", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("mhtml", "message/rfc822", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("nws", "message/rfc822", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("css", "text/css", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("csv", "text/csv", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("323", "text/h323", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("htm", "text/html", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("html", "text/html", true); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("stm", "text/html", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("uls", "text/iuls", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("bas", "text/plain", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("c", "text/plain", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("h", "text/plain", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("txt", "text/plain", true); //$NON-NLS-1$   //$NON-NLS-2$
        addMimeTypeMapping("rtx", "text/richtext", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("sct", "text/scriptlet", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("tsv", "text/tab-separated-values", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("htt", "text/webviewhtml", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("htc", "text/x-component", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("xml", "text/xml", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("etx", "text/x-setext", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("vcf", "text/x-vcard", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("mp2", "video/mpeg", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("mpa", "video/mpeg", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("mpe", "video/mpeg", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("mpeg", "video/mpeg", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("mpg", "video/mpeg", true); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("mpv2", "video/mpeg", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("mov", "video/quicktime", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("qt", "video/quicktime", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("lsf", "video/x-la-asf", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("lsx", "video/x-la-asf", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("asf", "video/x-ms-asf", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("asr", "video/x-ms-asf", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("asx", "video/x-ms-asf", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("avi", "video/x-msvideo", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("movie", "video/x-sgi-movie", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("flr", "x-world/x-vrml", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("vrml", "x-world/x-vrml", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("wrl", "x-world/x-vrml", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("wrz", "x-world/x-vrml", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("xaf", "x-world/x-vrml", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMapping("xof", "x-world/x-vrml", false); //$NON-NLS-1$   //$NON-NLS-2$ 
        addMimeTypeMapping("png", "image/png", true); //$NON-NLS-1$   //$NON-NLS-2$
        addMimeTypeMapping("mts", "video/avchd", true); //$NON-NLS-1$   //$NON-NLS-2$
        addMimeTypeMapping("mod", "video/mpeg", false); //$NON-NLS-1$   //$NON-NLS-2$
        addMimeTypeMapping("ptm", "application/x-ptm", true);
        addMimeTypeMapping("oni", "application/x-openni", true);
        // Office xml formats
        addMimeTypeMapping("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation", true);
        addMimeTypeMapping("xslx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", true);
        addMimeTypeMapping("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", true);
    }

    public void addMimeTypeMapping(String extension, String mimeType, boolean isDefault) {
        if (isDefault) {
            extensions.put(extension, mimeType);
        }
    }

    public String getFileExtension(String mimeType) {
        for (Entry<String, String> entry : extensions.entrySet()) {
            if (entry.getValue().equals(mimeType)) {
                // return "." + entry.getKey();
                return entry.getKey();
            }
        }

        // return "." + UNKNOWN_EXT;
        return UNKNOWN_EXT;
    }
}
