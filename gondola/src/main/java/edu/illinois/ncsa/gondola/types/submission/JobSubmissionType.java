//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.0.5-b02-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.08.21 at 08:05:26 AM CDT 
//


package edu.illinois.ncsa.gondola.types.submission;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for job-submission-type complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="job-submission-type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="instance-id" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="myproxy-user" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="target-user" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="target-uri" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="service-user-home" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="target-user-home" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="submit-path" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="terminate-path" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="submit-error-handler" type="{http://edu.illinois.ncsa.gondola/submission}error-handler-type" minOccurs="0"/>
 *         &lt;element name="job-id-parser" type="{http://edu.illinois.ncsa.gondola/submission}parser-type" minOccurs="0"/>
 *         &lt;element name="status-handler" type="{http://edu.illinois.ncsa.gondola/submission}status-handler-type" minOccurs="0"/>
 *         &lt;element name="script" type="{http://edu.illinois.ncsa.gondola/submission}script-type" minOccurs="0"/>
 *         &lt;element name="x509-certificate" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "job-submission-type", propOrder = {
    "instanceId",
    "myproxyUser",
    "targetUser",
    "targetUri",
    "serviceUserHome",
    "targetUserHome",
    "submitPath",
    "terminatePath",
    "submitErrorHandler",
    "jobIdParser",
    "statusHandler",
    "script",
    "x509Certificate"
})
public class JobSubmissionType {

    @XmlElement(name = "instance-id")
    protected String instanceId;
    @XmlElement(name = "myproxy-user")
    protected String myproxyUser;
    @XmlElement(name = "target-user")
    protected String targetUser;
    @XmlElement(name = "target-uri")
    protected String targetUri;
    @XmlElement(name = "service-user-home")
    protected String serviceUserHome;
    @XmlElement(name = "target-user-home")
    protected String targetUserHome;
    @XmlElement(name = "submit-path")
    protected String submitPath;
    @XmlElement(name = "terminate-path")
    protected String terminatePath;
    @XmlElement(name = "submit-error-handler")
    protected ErrorHandlerType submitErrorHandler;
    @XmlElement(name = "job-id-parser")
    protected ParserType jobIdParser;
    @XmlElement(name = "status-handler")
    protected StatusHandlerType statusHandler;
    protected ScriptType script;
    @XmlElement(name = "x509-certificate")
    protected String x509Certificate;
    @XmlAttribute
    protected String id;

    /**
     * Gets the value of the instanceId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInstanceId() {
        return instanceId;
    }

    /**
     * Sets the value of the instanceId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInstanceId(String value) {
        this.instanceId = value;
    }

    /**
     * Gets the value of the myproxyUser property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMyproxyUser() {
        return myproxyUser;
    }

    /**
     * Sets the value of the myproxyUser property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMyproxyUser(String value) {
        this.myproxyUser = value;
    }

    /**
     * Gets the value of the targetUser property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTargetUser() {
        return targetUser;
    }

    /**
     * Sets the value of the targetUser property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTargetUser(String value) {
        this.targetUser = value;
    }

    /**
     * Gets the value of the targetUri property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTargetUri() {
        return targetUri;
    }

    /**
     * Sets the value of the targetUri property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTargetUri(String value) {
        this.targetUri = value;
    }

    /**
     * Gets the value of the serviceUserHome property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getServiceUserHome() {
        return serviceUserHome;
    }

    /**
     * Sets the value of the serviceUserHome property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setServiceUserHome(String value) {
        this.serviceUserHome = value;
    }

    /**
     * Gets the value of the targetUserHome property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTargetUserHome() {
        return targetUserHome;
    }

    /**
     * Sets the value of the targetUserHome property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTargetUserHome(String value) {
        this.targetUserHome = value;
    }

    /**
     * Gets the value of the submitPath property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSubmitPath() {
        return submitPath;
    }

    /**
     * Sets the value of the submitPath property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSubmitPath(String value) {
        this.submitPath = value;
    }

    /**
     * Gets the value of the terminatePath property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTerminatePath() {
        return terminatePath;
    }

    /**
     * Sets the value of the terminatePath property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTerminatePath(String value) {
        this.terminatePath = value;
    }

    /**
     * Gets the value of the submitErrorHandler property.
     * 
     * @return
     *     possible object is
     *     {@link ErrorHandlerType }
     *     
     */
    public ErrorHandlerType getSubmitErrorHandler() {
        return submitErrorHandler;
    }

    /**
     * Sets the value of the submitErrorHandler property.
     * 
     * @param value
     *     allowed object is
     *     {@link ErrorHandlerType }
     *     
     */
    public void setSubmitErrorHandler(ErrorHandlerType value) {
        this.submitErrorHandler = value;
    }

    /**
     * Gets the value of the jobIdParser property.
     * 
     * @return
     *     possible object is
     *     {@link ParserType }
     *     
     */
    public ParserType getJobIdParser() {
        return jobIdParser;
    }

    /**
     * Sets the value of the jobIdParser property.
     * 
     * @param value
     *     allowed object is
     *     {@link ParserType }
     *     
     */
    public void setJobIdParser(ParserType value) {
        this.jobIdParser = value;
    }

    /**
     * Gets the value of the statusHandler property.
     * 
     * @return
     *     possible object is
     *     {@link StatusHandlerType }
     *     
     */
    public StatusHandlerType getStatusHandler() {
        return statusHandler;
    }

    /**
     * Sets the value of the statusHandler property.
     * 
     * @param value
     *     allowed object is
     *     {@link StatusHandlerType }
     *     
     */
    public void setStatusHandler(StatusHandlerType value) {
        this.statusHandler = value;
    }

    /**
     * Gets the value of the script property.
     * 
     * @return
     *     possible object is
     *     {@link ScriptType }
     *     
     */
    public ScriptType getScript() {
        return script;
    }

    /**
     * Sets the value of the script property.
     * 
     * @param value
     *     allowed object is
     *     {@link ScriptType }
     *     
     */
    public void setScript(ScriptType value) {
        this.script = value;
    }

    /**
     * Gets the value of the x509Certificate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getX509Certificate() {
        return x509Certificate;
    }

    /**
     * Sets the value of the x509Certificate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setX509Certificate(String value) {
        this.x509Certificate = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

}