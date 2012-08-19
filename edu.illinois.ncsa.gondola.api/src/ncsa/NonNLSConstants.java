/******************************************************************************
 * Copyright 2004-2011 The Board of Trustees of the University of Illinois. 
 * All rights reserved.
 * 
 * Contributors:
 *    Albert L. Rossi:  original design and implementation
 ******************************************************************************/
package ncsa;

/**
 * Gathers all internal, unmodifiable string constants into a single place for
 * convenience and in the interest of uncluttered code.
 */
public class NonNLSConstants {

	public static final int UNDEFINED = -1;
	public static final int EOF = -1; // DO NOT ALTER!

	/**
	 * Default value for bit-wise & and |.
	 */
	public static final int BIT_DEFAULT = 0; // DO NOT ALTER!

	public static final int DEFAULT_SSH_PORT = 22;
	public static final int STREAM_BUFFER_SIZE = 1024;
	public static final int READER_BUFFER_SIZE = 512 * 1024;
	public static final int COPY_BUFFER_SIZE = 64 * 1024;
	public static final int MAX_PATTERN_LENGTH = 128 * 1024;
	public static final int MAX_TAG_LENGTH = 128;

	public static final int INAME = 10150;
	public static final int IVALUE = 10151;

	/**
	 * Type markers for file read.
	 */
	public static final int BOOL = 10130; // Read
	public static final int BYTE = 10131;
	public static final int CHAR = 10132;
	public static final int CRET = 10133;
	public static final int DOUB = 10134;
	public static final int FLOT = 10135;
	public static final int INTG = 10136;
	public static final int LONG = 10137;
	public static final int NEWL = 10138;
	public static final int SHRT = 10139;
	public static final int UBYT = 10140;
	public static final int USHT = 10141;
	public static final int UTFC = 10142;
	public static final int WSPC = 10143;
	public static final int TEXT = 10144;

	/**
	 * Logical operations & comparators
	 */
	public static final int iEQ = 10010;
	public static final int iEQUALS = 10011;
	public static final int iGE = 10012;
	public static final int iGT = 10013;
	public static final int iLE = 10014;
	public static final int iLT = 10015;
	public static final int iNE = 10016;
	public static final int iNEQUALS = 10017;

	/**
	 * Regex filter settings
	 */
	public static final int MATCHES = 10028;
	public static final int NMATCHES = 10029;
	public static final int MANY_TO_ONE = 10100;
	public static final int ONE_TO_ONE = 10101;
	public static final int PRIORITY = 10102;
	public static final int LOOKING = 10110;
	public static final int MATCHED = 10111;
	public static final int UNMATCHED = 10112;

	/**
	 * Regex flags
	 */
	public static final String CASE_INSENSITIVE = "CASE_INSENSITIVE";//$NON-NLS-1$
	public static final String MULTILINE = "MULTILINE";//$NON-NLS-1$
	public static final String DOTALL = "DOTALL";//$NON-NLS-1$
	public static final String UNICODE_CASE = "UNICODE_CASE";//$NON-NLS-1$
	public static final String CANON_EQ = "CANON_EQ";//$NON-NLS-1$
	public static final String LITERAL = "LITERAL";//$NON-NLS-1$
	public static final String COMMENTS = "COMMENTS";//$NON-NLS-1$
	public static final String UNIX_LINES = "UNIX_LINES";//$NON-NLS-1$

	/* CHARACTERS */
	public static final String ZEROSTR = "";//$NON-NLS-1$
	public static final String SP = " ";//$NON-NLS-1$
	public static final String REGPIP = "[|]";//$NON-NLS-1$
	public static final String REGDOT = "[.]";//$NON-NLS-1$
	public static final String LINE_SEP = System.getProperty("line.separator"); //$NON-NLS-1$
	public static final char LINE_SEP_CHAR = LINE_SEP.charAt(0);
	public static final String REMOTE_LINE_SEP = "\n"; //$NON-NLS-1$
	public static final String REMOTE_PATH_SEP = "/"; //$NON-NLS-1$
	public static final String PATH_SEP = System.getProperty("file.separator"); //$NON-NLS-1$
	public static final String LEN = "N";//$NON-NLS-1$
	public static final String TAB = "\t"; //$NON-NLS-1$
	public static final String EQ = "=";//$NON-NLS-1$
	public static final String QT = "\"";//$NON-NLS-1$
	public static final String APOS = "'";//$NON-NLS-1$
	public static final String QM = "?";//$NON-NLS-1$
	public static final String PD = "#";//$NON-NLS-1$
	public static final String PDRX = "[#]";//$NON-NLS-1$
	public static final String CM = ",";//$NON-NLS-1$
	public static final String CO = ":";//$NON-NLS-1$
	public static final String SC = ";";//$NON-NLS-1$
	public static final String LT = "<"; //$NON-NLS-1$
	public static final String LTS = "</";//$NON-NLS-1$
	public static final String GT = ">";//$NON-NLS-1$
	public static final String GTGT = " >> ";//$NON-NLS-1$
	public static final String GTLT = "><";//$NON-NLS-1$
	public static final String HYPH = "-";//$NON-NLS-1$
	public static final String AT = "@";//$NON-NLS-1$
	public static final String DOL = "$";//$NON-NLS-1$
	public static final String PIP = "|";//$NON-NLS-1$
	public static final String DOT = ".";//$NON-NLS-1$
	public static final String OPENP = "(";//$NON-NLS-1$
	public static final String OPENSQ = "[";//$NON-NLS-1$
	public static final String OPENV = "${";//$NON-NLS-1$
	public static final String CLOSP = ")";//$NON-NLS-1$
	public static final String CLOSSQ = "]";//$NON-NLS-1$
	public static final String CLOSV = "}";//$NON-NLS-1$
	public static final String BKESC = "\\\\";//$NON-NLS-1$
	public static final String BKBKESC = "\\\\\\\\";//$NON-NLS-1$
	public static final String DLESC = "\\$";//$NON-NLS-1$
	public static final String DLESCESC = "\\\\\\$";//$NON-NLS-1$
	public static final String SPESC = "\\\\s";//$NON-NLS-1$
	public static final String LNSEPESC = "\\\\n";//$NON-NLS-1$
	public static final String TBESC = "\\t";//$NON-NLS-1$
	public static final String TBESCESC = "\\\\t";//$NON-NLS-1$
	public static final String LNESC = "\\n";//$NON-NLS-1$
	public static final String RTESC = "\\r";//$NON-NLS-1$
	public static final String LN = "\n";//$NON-NLS-1$
	public static final String RT = "\r";//$NON-NLS-1$

	/* TYPES */
	public static final String sCHAR = "char";//$NON-NLS-1$
	public static final String sFLOAT = "float";//$NON-NLS-1$
	public static final String sINT = "int";//$NON-NLS-1$
	public static final String sLONG = "long";//$NON-NLS-1$
	public static final String sDOUBLE = "double";//$NON-NLS-1$
	public static final String sSHORT = "short";//$NON-NLS-1$

	/* JAXB */
	public static final String XMLSchema = "http://www.w3.org/2001/XMLSchema"; //$NON-NLS-1$
	public static final String DATA = "data/"; //$NON-NLS-1$
	public static final String SUBM_XSD = DATA + "job_submission.xsd";//$NON-NLS-1$
	public static final String SERV_XSD = DATA + "service_configuration.xsd";//$NON-NLS-1$
	public static final String JAXB = "JAXB";//$NON-NLS-1$
	public static final String JAXB_SUBM_CONTEXT = "edu.illinois.ncsa.gondola.types.submission";//$NON-NLS-1$
	public static final String JAXB_SERV_CONTEXT = "edu.illinois.ncsa.gondola.types.service";//$NON-NLS-1$
	public static final String DOT_XML = ".xml";//$NON-NLS-1$

	/* COMMON KEY WORDS OR PROPERTIES */
	public static final String NAME = "name"; //$NON-NLS-1$
	public static final String VALUE = "value"; //$NON-NLS-1$
	public static final String TYPE = "type"; //$NON-NLS-1$
	public static final String DEFAULT_DATE_FORMAT = "yyyy/MM/dd HH:mm:ss"; //$NON-NLS-1$
	public static final String TMP_DIR = System.getProperty("java.io.tmpdir"); //$NON-NLS-1$

	/* JDBC */
	public static final String TAG_SUPPORTS_RC = "supports-relational-constraints"; //$NON-NLS-1$
	public static final String SP_EQ_SP = " = "; //$NON-NLS-1$

	/* STAGING & STATUS HANDLING */
	public static final String JOB_SCRIPTS = ".job_scripts"; //$NON-NLS-1$
	public static final String LOG = "log"; //$NON-NLS-1$
	public static final String SCRIPT = "script"; //$NON-NLS-1$
	public static final String SSH = "ssh"; //$NON-NLS-1$
	public static final String DOT_SSH = ".ssh"; //$NON-NLS-1$
	public static final String CAT = "cat"; //$NON-NLS-1$;
	public static final String DONE = "DONE";//$NON-NLS-1$;
	public static final String UUID = "uuid";//$NON-NLS-1$;
}
