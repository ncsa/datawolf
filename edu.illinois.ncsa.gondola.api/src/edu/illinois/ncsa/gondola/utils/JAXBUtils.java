/******************************************************************************
 * Copyright 2004-2011 The Board of Trustees of the University of Illinois. 
 * All rights reserved.
 * 
 * Contributors:
 *    Albert L. Rossi:  original design and implementation
 ******************************************************************************/
package edu.illinois.ncsa.gondola.utils;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import edu.illinois.ncsa.gondola.GondolaApiPlugin;
import edu.illinois.ncsa.gondola.messages.Messages;
import edu.illinois.ncsa.gondola.types.service.AccessType;
import edu.illinois.ncsa.gondola.types.service.ServiceType;
import edu.illinois.ncsa.gondola.types.submission.ErrorHandlerType;
import edu.illinois.ncsa.gondola.types.submission.IdListType;
import edu.illinois.ncsa.gondola.types.submission.JobStatusListType;
import edu.illinois.ncsa.gondola.types.submission.JobStatusType;
import edu.illinois.ncsa.gondola.types.submission.JobSubmissionType;
import edu.illinois.ncsa.gondola.types.submission.ParserType;
import edu.illinois.ncsa.gondola.types.submission.ScriptType;

import ncsa.NonNLSConstants;

/**
 * Convenience methods for handling XML using JAXB.
 * 
 * @author arossi
 * 
 */
public class JAXBUtils
{

    private static final Logger logger = LoggerFactory.getLogger( JAXBUtils.class );

    private static edu.illinois.ncsa.gondola.types.submission.ObjectFactory submissionFactory;
    private static edu.illinois.ncsa.gondola.types.service.ObjectFactory serviceFactory;

    private JAXBUtils()
    {
    }

    /**
     * @param data
     * @return XML document string.
     * @throws IOException
     * @throws SAXException
     * @throws URISyntaxException
     * @throws JAXBException
     * @throws ParserConfigurationException
     */
    public static String asXML( AccessType data )
            throws IOException, SAXException, URISyntaxException, JAXBException, ParserConfigurationException
    {
        StringWriter sw = new StringWriter();
        getServiceMarshaller().marshal( getServiceFactory().createJdbcAccess( data ), sw );
        return sw.toString();
    }

    /**
     * @param data
     * @return XML document string.
     * @throws JAXBException
     */
    public static String asXML( ErrorHandlerType data ) throws JAXBException
    {
        StringWriter sw = new StringWriter();
        getSubmissionMarshaller().marshal( getSubmissionFactory().createJobErrorHandler( data ), sw );
        return sw.toString();
    }

    /**
     * @param data
     * @return XML document string.
     * @throws IOException
     * @throws SAXException
     * @throws URISyntaxException
     * @throws JAXBException
     * @throws ParserConfigurationException
     */
    public static String asXML( IdListType data )
            throws IOException, SAXException, URISyntaxException, JAXBException, ParserConfigurationException
    {
        StringWriter sw = new StringWriter();
        getSubmissionMarshaller().marshal( getSubmissionFactory().createIdList( data ), sw );
        return sw.toString();
    }

    /**
     * @param data
     * @return XML document string.
     * @throws IOException
     * @throws SAXException
     * @throws URISyntaxException
     * @throws JAXBException
     * @throws ParserConfigurationException
     */
    public static String asXML( JobStatusListType data )
            throws IOException, SAXException, URISyntaxException, JAXBException, ParserConfigurationException
    {
        StringWriter sw = new StringWriter();
        getSubmissionMarshaller().marshal( getSubmissionFactory().createJobStatusList( data ), sw );
        return sw.toString();
    }

    /**
     * @param data
     * @return XML document string.
     * @throws IOException
     * @throws SAXException
     * @throws URISyntaxException
     * @throws JAXBException
     * @throws ParserConfigurationException
     */
    public static String asXML( JobStatusType data )
            throws IOException, SAXException, URISyntaxException, JAXBException, ParserConfigurationException
    {
        StringWriter sw = new StringWriter();
        getSubmissionMarshaller().marshal( getSubmissionFactory().createJobStatus( data ), sw );
        return sw.toString();
    }

    /**
     * @param data
     * @return XML document string.
     * @throws IOException
     * @throws SAXException
     * @throws URISyntaxException
     * @throws JAXBException
     * @throws ParserConfigurationException
     */
    public static String asXML( JobSubmissionType data )
            throws IOException, SAXException, URISyntaxException, JAXBException, ParserConfigurationException
    {
        StringWriter sw = new StringWriter();
        getSubmissionMarshaller().marshal( getSubmissionFactory().createJobSubmission( data ), sw );
        return sw.toString();
    }

    /**
     * @param data
     * @return XML document string.
     * @throws JAXBException
     */
    public static String asXML( ParserType data ) throws JAXBException
    {
        StringWriter sw = new StringWriter();
        getSubmissionMarshaller().marshal( getSubmissionFactory().createJobStatusParser( data ), sw );
        return sw.toString();
    }

    /**
     * @param data
     * @return XML document string.
     * @throws JAXBException
     */
    public static String asXML( ScriptType data ) throws JAXBException
    {
        StringWriter sw = new StringWriter();
        getSubmissionMarshaller().marshal( getSubmissionFactory().createJobScript( data ), sw );
        return sw.toString();
    }

    /**
     * @param data
     * @return XML document string.
     * @throws JAXBException
     */
    public static String asXML( ServiceType data ) throws JAXBException
    {
        StringWriter sw = new StringWriter();
        getServiceMarshaller().marshal( getServiceFactory().createServiceConfig( data ), sw );
        return sw.toString();
    }

    /**
     * 
     * @param xml
     * @return ErrorHandlerType
     * @throws SAXException
     * @throws IOException
     * @throws URISyntaxException
     * @throws JAXBException
     */
    public static ErrorHandlerType unmarshalErrorHandler( String xml ) throws SAXException, IOException, URISyntaxException, JAXBException
    {
        JAXBElement<?> o = getJAXBElement( xml, getSubmissionUnmarshaller(), getSubmissionValidator() );
        return (ErrorHandlerType) o.getValue();
    }

    /**
     * Delegates to {@link #getJAXBElement(String)}.
     * 
     * @param xml
     * @return IdListType object
     * @throws JAXBException
     *             problem encountered during unmarshaling
     * @throws IOException
     * @throws SAXException
     *             validation error
     * @throws URISyntaxException
     */
    public static IdListType unmarshalIdList( String xml ) throws JAXBException, IOException, SAXException, URISyntaxException
    {
        JAXBElement<?> o = getJAXBElement( xml, getSubmissionUnmarshaller(), getSubmissionValidator() );
        return (IdListType) o.getValue();
    }

    /**
     * Delegates to {@link #unmarshalIdList(String)}, {@link #getXML(url)}
     * 
     * @param url
     *            of the XML
     * @return IdListType object
     * @throws IOException
     * @throws SAXException
     * @throws URISyntaxException
     * @throws JAXBException
     */
    public static IdListType unmarshalIdList( URL url ) throws IOException, SAXException, URISyntaxException, JAXBException
    {
        return unmarshalIdList( getXML( url ) );
    }

    /**
     * Delegates to {@link #getJAXBElement(String)}.
     * 
     * @param xml
     * @return AccessType object
     * @throws JAXBException
     *             problem encountered during unmarshaling
     * @throws IOException
     * @throws SAXException
     *             validation error
     * @throws URISyntaxException
     */
    public static AccessType unmarshalJdbcAccess( String xml ) throws JAXBException, IOException, SAXException, URISyntaxException
    {
        JAXBElement<?> o = getJAXBElement( xml, getServiceUnmarshaller(), getServiceValidator() );
        return (AccessType) o.getValue();
    }

    /**
     * Delegates to {@link #unmarshalJdbcAccess(String)}, {@link #getXML(url)}
     * 
     * @param url
     *            of the XML
     * @return AccessType object
     * @throws IOException
     * @throws SAXException
     * @throws URISyntaxException
     * @throws JAXBException
     */
    public static AccessType unmarshalJdbcAccess( URL url ) throws IOException, SAXException, URISyntaxException, JAXBException
    {
        return unmarshalJdbcAccess( getXML( url ) );
    }

    /**
     * 
     * @param xml
     * @return ScriptType
     * @throws SAXException
     * @throws IOException
     * @throws URISyntaxException
     * @throws JAXBException
     */
    public static ScriptType unmarshalJobScript( String xml ) throws SAXException, IOException, URISyntaxException, JAXBException
    {
        JAXBElement<?> o = getJAXBElement( xml, getSubmissionUnmarshaller(), getSubmissionValidator() );
        return (ScriptType) o.getValue();
    }

    /**
     * Delegates to {@link #getJAXBElement(String)}.
     * 
     * @param xml
     * @return JobStatusType object
     * @throws JAXBException
     *             problem encountered during unmarshaling
     * @throws IOException
     * @throws SAXException
     *             validation error
     * @throws URISyntaxException
     */
    public static JobStatusType unmarshalJobStatus( String xml ) throws IOException, SAXException, URISyntaxException, JAXBException
    {
        JAXBElement<?> o = getJAXBElement( xml, getSubmissionUnmarshaller(), getSubmissionValidator() );
        return (JobStatusType) o.getValue();
    }

    /**
     * Delegates to {@link #unmarshalJobStatus(String)}, {@link #getXML(url)}
     * 
     * @param url
     *            of the XML
     * @return JobStatusType object
     * @throws IOException
     * @throws SAXException
     * @throws URISyntaxException
     * @throws JAXBException
     */
    public static JobStatusType unmarshalJobStatus( URL url ) throws IOException, SAXException, URISyntaxException, JAXBException
    {
        return unmarshalJobStatus( getXML( url ) );
    }

    /**
     * Delegates to {@link #getJAXBElement(String)}.
     * 
     * @param xml
     * @return JobStatusListType object
     * @throws JAXBException
     *             problem encountered during unmarshaling
     * @throws IOException
     * @throws SAXException
     *             validation error
     * @throws URISyntaxException
     */
    public static JobStatusListType unmarshalJobStatusList( String xml )
            throws IOException, SAXException, URISyntaxException, JAXBException
    {
        JAXBElement<?> o = getJAXBElement( xml, getSubmissionUnmarshaller(), getSubmissionValidator() );
        return (JobStatusListType) o.getValue();
    }

    /**
     * Delegates to {@link #unmarshalJobStatusList(String)},
     * {@link #getXML(url)}
     * 
     * @param url
     *            of the XML
     * @return JobStatusListType object
     * @throws IOException
     * @throws SAXException
     * @throws URISyntaxException
     * @throws JAXBException
     */
    public static JobStatusListType unmarshalJobStatusList( URL url ) throws IOException, SAXException, URISyntaxException, JAXBException
    {
        return unmarshalJobStatusList( getXML( url ) );
    }

    /**
     * Delegates to {@link #getJAXBElement(String)}.
     * 
     * @param xml
     * @return JobSubmissionType object
     * @throws JAXBException
     *             problem encountered during unmarshaling
     * @throws IOException
     * @throws SAXException
     *             validation error
     * @throws URISyntaxException
     */
    public static JobSubmissionType unmarshalJobSubmission( String xml )
            throws JAXBException, IOException, SAXException, URISyntaxException
    {
        JAXBElement<?> o = getJAXBElement( xml, getSubmissionUnmarshaller(), getSubmissionValidator() );
        return (JobSubmissionType) o.getValue();
    }

    /**
     * Delegates to {@link #unmarshalJobSubmission(String)},
     * {@link #getXML(url)}
     * 
     * @param url
     *            of the XML
     * @return JobSubmissionType object
     * @throws IOException
     * @throws SAXException
     * @throws URISyntaxException
     * @throws JAXBException
     */
    public static JobSubmissionType unmarshalJobSubmission( URL url ) throws IOException, SAXException, URISyntaxException, JAXBException
    {
        return unmarshalJobSubmission( getXML( url ) );
    }

    /**
     * 
     * @param xml
     * @return ParserType
     * @throws SAXException
     * @throws IOException
     * @throws URISyntaxException
     * @throws JAXBException
     */
    public static ParserType unmarshalParser( String xml ) throws SAXException, IOException, URISyntaxException, JAXBException
    {
        JAXBElement<?> o = getJAXBElement( xml, getSubmissionUnmarshaller(), getSubmissionValidator() );
        return (ParserType) o.getValue();
    }

    /**
     * Delegates to {@link #getJAXBElement(String)}.
     * 
     * @param xml
     * @return ServiceType object
     * @throws JAXBException
     *             problem encountered during unmarshaling
     * @throws IOException
     * @throws SAXException
     *             validation error
     * @throws URISyntaxException
     */
    public static ServiceType unmarshalService( String xml ) throws JAXBException, IOException, SAXException, URISyntaxException
    {
        JAXBElement<?> o = getJAXBElement( xml, getServiceUnmarshaller(), getServiceValidator() );
        return (ServiceType) o.getValue();
    }

    /**
     * Delegates to {@link #unmarshalIdList(String)}, {@link #getXML(url)}
     * 
     * @param url
     *            of the XML
     * @return ServiceType object
     * @throws IOException
     * @throws SAXException
     * @throws URISyntaxException
     * @throws JAXBException
     */
    public static ServiceType unmarshalService( URL url ) throws IOException, SAXException, URISyntaxException, JAXBException
    {
        return unmarshalService( getXML( url ) );
    }

    /**
     * First validates the xml, then gets the JAXB context and calls the JAXB
     * unmarshaller from it.
     * 
     * @param xml
     * @param unmarshaller
     *            for the schema
     * @param validator
     *            for the schema
     * @return the JAXBElement
     * @throws JAXBException
     *             problem encountered during unmarshaling
     * @throws IOException
     * @throws SAXException
     *             validation error
     * @throws URISyntaxException
     */
    private static JAXBElement<?> getJAXBElement( String xml, Unmarshaller unmarshaller, Validator validator )
            throws SAXException, IOException, URISyntaxException, JAXBException
    {
        Source source = new StreamSource( new StringReader( xml ) );
        validate( source, validator );
        source = new StreamSource( new StringReader( xml ) );
        return (JAXBElement<?>) unmarshaller.unmarshal( source );
    }

    /**
     * 
     * @param context
     * @return marshaller
     * @throws JAXBException
     */
    private static Marshaller getMarshaller( String context ) throws JAXBException
    {
        JAXBContext jc = JAXBContext.newInstance( context, JAXBUtils.class.getClassLoader() );
        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
        return marshaller;
    }

    /**
     * @return object factory
     */
    private static edu.illinois.ncsa.gondola.types.service.ObjectFactory getServiceFactory()
    {
        if ( serviceFactory == null )
            serviceFactory = new edu.illinois.ncsa.gondola.types.service.ObjectFactory();
        return serviceFactory;
    }

    /**
     * @return static singleton
     * @throws JAXBException
     */
    private synchronized static Marshaller getServiceMarshaller() throws JAXBException
    {
        return getMarshaller( NonNLSConstants.JAXB_SERV_CONTEXT );
    }

    /**
     * @return static singleton
     * @throws JAXBException
     */
    private synchronized static Unmarshaller getServiceUnmarshaller() throws JAXBException
    {
        return getUnmarshaller( NonNLSConstants.JAXB_SERV_CONTEXT );
    }

    /**
     * @return static singleton
     * @throws IOException
     * @throws SAXException
     */
    private synchronized static Validator getServiceValidator() throws IOException, SAXException
    {
        return getValidator( NonNLSConstants.SERV_XSD );
    }

    /**
     * @return object factory
     */
    private static edu.illinois.ncsa.gondola.types.submission.ObjectFactory getSubmissionFactory()
    {
        if ( submissionFactory == null )
            submissionFactory = new edu.illinois.ncsa.gondola.types.submission.ObjectFactory();
        return submissionFactory;
    }

    /**
     * 
     * @return static singleton
     * @throws JAXBException
     */
    private synchronized static Marshaller getSubmissionMarshaller() throws JAXBException
    {
        return getMarshaller( NonNLSConstants.JAXB_SUBM_CONTEXT );
    }

    /**
     * 
     * @return static singleton
     * @throws JAXBException
     */
    private synchronized static Unmarshaller getSubmissionUnmarshaller() throws JAXBException
    {
        return getUnmarshaller( NonNLSConstants.JAXB_SUBM_CONTEXT );
    }

    /**
     * @return static singleton
     * @throws IOException
     * @throws SAXException
     */
    private synchronized static Validator getSubmissionValidator() throws IOException, SAXException
    {
        return getValidator( NonNLSConstants.SUBM_XSD );
    }

    /**
     * @param context
     * @return unmarshaller
     * @throws JAXBException
     */
    private static Unmarshaller getUnmarshaller( String context ) throws JAXBException
    {
        JAXBContext jc = JAXBContext.newInstance( context, JAXBUtils.class.getClassLoader() );
        return jc.createUnmarshaller();
    }

    /**
     * 
     * @param xsd
     * @return validator
     * @throws IOException
     * @throws SAXException
     */
    private static Validator getValidator( String xsd ) throws IOException, SAXException
    {
        URL url = GondolaApiPlugin.getResource( xsd );
        SchemaFactory factory = SchemaFactory.newInstance( NonNLSConstants.XMLSchema );
        Schema schema = factory.newSchema( url );
        return schema.newValidator();
    }

    /**
     * Streams the content from the url.
     * 
     * @param url
     *            location of XML resource
     * @return xml string
     */
    private static String getXML( URL url ) throws IOException
    {
        StringBuffer buffer = new StringBuffer();
        if ( url != null ) {
            InputStreamReader reader = new InputStreamReader( url.openStream() );
            char[] chars = new char[4096];
            int read = 0;
            while ( true ) {
                try {
                    read = reader.read( chars, 0, chars.length );
                } catch ( EOFException eof ) {
                    break;
                }
                if ( read <= 0 )
                    break;
                buffer.append( chars, 0, read );
            }
            return buffer.toString();
        }
        return null;
    }

    /**
     * Details from the parse exception.
     * 
     * @param e
     *            thrown parse exception
     * @return line, column and other info.
     */
    private static String printInfo( SAXParseException e )
    {
        StringBuffer sb = new StringBuffer();
        sb.append( Messages.PublicId + e.getPublicId() ).append( NonNLSConstants.LINE_SEP );
        sb.append( Messages.SystemId + e.getSystemId() ).append( NonNLSConstants.LINE_SEP );
        sb.append( Messages.LineNumber + e.getLineNumber() ).append( NonNLSConstants.LINE_SEP );
        sb.append( Messages.ColumnNumber + e.getColumnNumber() ).append( NonNLSConstants.LINE_SEP );
        sb.append( Messages.Message + e.getMessage() ).append( NonNLSConstants.LINE_SEP );
        return sb.toString();
    }

    /**
     * Validates the XML against the internal XSD.
     * 
     * @param source
     *            of the configuration xml.
     * @param validator
     *            for the schema against which to validate
     * @throws SAXException
     *             if invalid
     * @throws IOException
     */

    private static void validate( Source source, Validator validator ) throws SAXException, IOException
    {
        try {
            validator.validate( source );
        } catch ( SAXParseException sax ) {
            //			GondolaApiPlugin.log(printInfo(sax));
            logger.error( printInfo( sax ) );
            throw sax;
        }
    }
}
