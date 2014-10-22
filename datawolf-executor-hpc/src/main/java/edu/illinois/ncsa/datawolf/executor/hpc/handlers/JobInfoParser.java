/******************************************************************************
 * Copyright 2004-2011 The Board of Trustees of the University of Illinois. 
 * All rights reserved.
 * 
 * Contributors:
 *    Albert L. Rossi:  original design and implementation
 ******************************************************************************/
package edu.illinois.ncsa.datawolf.executor.hpc.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.ncsa.gondola.NonNLSConstants;
import edu.illinois.ncsa.gondola.types.submission.ExpressionGroupTranslatorType;
import edu.illinois.ncsa.gondola.types.submission.JobStateType;
import edu.illinois.ncsa.gondola.types.submission.ParserType;
import edu.illinois.ncsa.gondola.types.submission.RegexType;

public class JobInfoParser {

    private class GroupTranslator {
        private String       from;
        private Integer      group;
        private TranslatorOp op;
        private JobStateType to;

        private boolean matches(List<String> groups) {
            if (group >= groups.size())
                return false;
            String value = groups.get(group);
            switch (op) {
            case EQUALS:
                return value.equals(from);
            case EQUALS_IGNORE_CASE:
                return value.equalsIgnoreCase(from);
            case STARTS_WITH:
                return value.startsWith(from);
            case ENDS_WITH:
                return value.endsWith(from);
            case CONTAINS:
                return value.contains(from);
            }
            return false;
        }
    }

    private enum TranslatorOp {
        EQUALS, EQUALS_IGNORE_CASE, STARTS_WITH, ENDS_WITH, CONTAINS;

        public static TranslatorOp fromValue(String v) {
            return valueOf(v);
        }

    }

    private final Logger          logger = LoggerFactory.getLogger(this.getClass());
    private final RegexType       regex;
    private final int             jobIdGroup;
    private List<GroupTranslator> translator;
    private final boolean         stderr;

    private final Pattern         pattern;

    private final List<String>    groups;

    public JobInfoParser(ParserType jaxbType) {
        regex = jaxbType.getExpression();
        jobIdGroup = jaxbType.getJobIdGroup();
        stderr = jaxbType.isStderr();
        List<ExpressionGroupTranslatorType> egtTypes = jaxbType.getTranslator();
        if (!egtTypes.isEmpty()) {
            translator = new ArrayList<GroupTranslator>();
            for (ExpressionGroupTranslatorType egt : egtTypes) {
                GroupTranslator gt = new GroupTranslator();
                gt.from = egt.getFrom();
                gt.to = egt.getTo();
                gt.group = egt.getGroup();
                gt.op = TranslatorOp.fromValue(egt.getOp());
                translator.add(gt);
            }
        }
        pattern = Pattern.compile(regex.getContent(), getFlags(regex.getFlags()));
        groups = new ArrayList<String>();
    }

    public boolean getStderr() {
        return stderr;
    }

    public String parseJobId(String[] lines) {
        String jobId = null;
        for (String line : lines) {
            logger.debug("parseJobId: trying to match '" + line + "' against '" + regex.getContent() + "'(flags: " + regex.getFlags() + ")");
            if (NonNLSConstants.UNDEFINED != getMatched(line, groups)) {
                logger.debug("parseJobId: matched, groups " + groups);
                jobId = groups.get(jobIdGroup);
                logger.debug("parseJobId: jobId is:  " + jobId);
                groups.clear();
                break;
            }
            groups.clear();
        }
        return jobId;
    }

    public String[] parseJobState(String line) {
        logger.debug("parseJobState: trying to match " + line + " against " + regex.getContent());
        String[] jobInfo = null;
        if (NonNLSConstants.UNDEFINED != getMatched(line, groups)) {
            logger.debug("parseJobState: matched, groups " + groups);
            String id = null;
            String state = null;
            id = groups.get(jobIdGroup);
            logger.debug("parseJobState: jobId translates to " + id);
            for (GroupTranslator grpTrans : translator)
                if (grpTrans.matches(groups)) {
                    state = grpTrans.to.name();
                    logger.debug("parseJobState: state translates to " + state);
                    break;
                }
            if (id != null) {
                jobInfo = new String[2];
                jobInfo[0] = id;
                if (state == null)
                    state = JobStateType.JOB_COMPLETED.name();
                jobInfo[1] = state;
            }
        }
        groups.clear();
        logger.debug("parseJobState: returning " + (jobInfo == null ? null : jobInfo[0] + ", " + jobInfo[1]));
        return jobInfo;
    }

    /**
     * @param sequence
     *            the segment to match
     * @return array of substrings either from a split using the regex, or the
     *         regex groups (including group 0).
     */
    private int getMatched(String sequence, List<String> groups) {
        int lastChar = NonNLSConstants.UNDEFINED;
        Matcher m = pattern.matcher(sequence);
        if (m.matches()) {
            int count = m.groupCount();
            for (int i = 0; i <= count; i++)
                groups.add(m.group(i));
            lastChar = m.end(count);
        }
        return lastChar;
    }

    /**
     * Translates the string representation of the flags into the corresponding
     * Java int value. String can represent an or'd set, e.g.,
     * "CASE_INSENTIVE | DOTALL".
     * 
     * @param flags
     *            string representing the or'd flags.
     * @return corresponding internal value
     */
    public static int getFlags(String flags) {
        if (flags == null)
            return 0;
        int f = 0;
        String[] split = flags.split(NonNLSConstants.REGPIP);
        for (String s : split)
            if (NonNLSConstants.CASE_INSENSITIVE.equals(s.trim()))
                f |= Pattern.CASE_INSENSITIVE;
            else if (NonNLSConstants.MULTILINE.equals(s.trim()))
                f |= Pattern.MULTILINE;
            else if (NonNLSConstants.DOTALL.equals(s.trim()))
                f |= Pattern.DOTALL;
            else if (NonNLSConstants.UNICODE_CASE.equals(s.trim()))
                f |= Pattern.UNICODE_CASE;
            else if (NonNLSConstants.CANON_EQ.equals(s.trim()))
                f |= Pattern.CANON_EQ;
            else if (NonNLSConstants.LITERAL.equals(s.trim()))
                f |= Pattern.LITERAL;
            else if (NonNLSConstants.COMMENTS.equals(s.trim()))
                f |= Pattern.COMMENTS;
            else if (NonNLSConstants.UNIX_LINES.equals(s.trim()))
                f |= Pattern.UNIX_LINES;
        return f;
    }
}
