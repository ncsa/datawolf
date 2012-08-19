//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.0.5-b02-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.08.22 at 12:56:39 PM CDT 
//


package edu.illinois.ncsa.gondola.types.service;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for access-type complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="access-type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="jdbc-connection" type="{http://edu.illinois.ncsa.gondola/service}connection-type"/>
 *         &lt;element name="jdbc-dbcp" type="{http://edu.illinois.ncsa.gondola/service}dbcp-type" minOccurs="0"/>
 *         &lt;element name="initialize" type="{http://edu.illinois.ncsa.gondola/service}initialize-type" minOccurs="0"/>
 *         &lt;element name="drop" type="{http://edu.illinois.ncsa.gondola/service}drop-type" minOccurs="0"/>
 *         &lt;element name="max-batch-insert" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="supports-relational-constraints" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="type" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "access-type", propOrder = {
    "jdbcConnection",
    "jdbcDbcp",
    "initialize",
    "drop",
    "maxBatchInsert",
    "supportsRelationalConstraints"
})
public class AccessType {

    @XmlElement(name = "jdbc-connection", required = true)
    protected ConnectionType jdbcConnection;
    @XmlElement(name = "jdbc-dbcp")
    protected DbcpType jdbcDbcp;
    protected InitializeType initialize;
    protected DropType drop;
    @XmlElement(name = "max-batch-insert", defaultValue = "128")
    protected Integer maxBatchInsert;
    @XmlElement(name = "supports-relational-constraints", defaultValue = "false")
    protected Boolean supportsRelationalConstraints;
    @XmlAttribute
    protected String type;

    /**
     * Gets the value of the jdbcConnection property.
     * 
     * @return
     *     possible object is
     *     {@link ConnectionType }
     *     
     */
    public ConnectionType getJdbcConnection() {
        return jdbcConnection;
    }

    /**
     * Sets the value of the jdbcConnection property.
     * 
     * @param value
     *     allowed object is
     *     {@link ConnectionType }
     *     
     */
    public void setJdbcConnection(ConnectionType value) {
        this.jdbcConnection = value;
    }

    /**
     * Gets the value of the jdbcDbcp property.
     * 
     * @return
     *     possible object is
     *     {@link DbcpType }
     *     
     */
    public DbcpType getJdbcDbcp() {
        return jdbcDbcp;
    }

    /**
     * Sets the value of the jdbcDbcp property.
     * 
     * @param value
     *     allowed object is
     *     {@link DbcpType }
     *     
     */
    public void setJdbcDbcp(DbcpType value) {
        this.jdbcDbcp = value;
    }

    /**
     * Gets the value of the initialize property.
     * 
     * @return
     *     possible object is
     *     {@link InitializeType }
     *     
     */
    public InitializeType getInitialize() {
        return initialize;
    }

    /**
     * Sets the value of the initialize property.
     * 
     * @param value
     *     allowed object is
     *     {@link InitializeType }
     *     
     */
    public void setInitialize(InitializeType value) {
        this.initialize = value;
    }

    /**
     * Gets the value of the drop property.
     * 
     * @return
     *     possible object is
     *     {@link DropType }
     *     
     */
    public DropType getDrop() {
        return drop;
    }

    /**
     * Sets the value of the drop property.
     * 
     * @param value
     *     allowed object is
     *     {@link DropType }
     *     
     */
    public void setDrop(DropType value) {
        this.drop = value;
    }

    /**
     * Gets the value of the maxBatchInsert property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMaxBatchInsert() {
        return maxBatchInsert;
    }

    /**
     * Sets the value of the maxBatchInsert property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMaxBatchInsert(Integer value) {
        this.maxBatchInsert = value;
    }

    /**
     * Gets the value of the supportsRelationalConstraints property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isSupportsRelationalConstraints() {
        return supportsRelationalConstraints;
    }

    /**
     * Sets the value of the supportsRelationalConstraints property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSupportsRelationalConstraints(Boolean value) {
        this.supportsRelationalConstraints = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType(String value) {
        this.type = value;
    }

}
