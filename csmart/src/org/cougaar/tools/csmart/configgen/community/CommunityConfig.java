/*
 * <copyright>
 *  Copyright 2001 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
 * 
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the Cougaar Open Source License as published by
 *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 *  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *  PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */

package org.cougaar.tools.csmart.configgen.community;

import java.util.*;

import org.w3c.dom.*;

import org.cougaar.tools.csmart.util.LatLonPoint;

import org.cougaar.tools.csmart.configgen.*;

/**
 * Reads and parses a community intermediate configuration file.
 * The Configure file which is a representation of an Agent Community
 * is an XML file representing necessary data required for running the
 * society.  Creating this file requires knowledge about the template.
 * <br>
 * The format of the XML file, in xsd tags (view source for details) is:   <br>
 * <code>
 * <xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema">
 *   <xsd:annotation>
 *     <xsd:documentation xml:lang="en">
 *       Agent Community Definition file used by the CSMART
 *       Configuration Generator.  File consists of 1 or more communities.
 *       Each community contains information used to create that community.
 *       This information consists of: When the customer will start and
 *       stop submitting tasks; StartMillis, StopMillis. How often
 *       the attacker sniffer will run; SniffInterval.  What the demand of
 *       the community is, how often it will submit tasks; Demand.  What the
 *       production of this community is, how much local asset inventory, Production.
 *       Other communities this community can allocate to; Suppliers. And, the
 *       geographic center of this community; Geography.
 *     </xsd:documentation>
 *   </xsd:annotation>
 *
 *   <xsd:element name="CommunityConfig" type="CommunityConfigType"/>
 *
 *   <xsd:complexType name="CommunityConfigType">
 *     <xsd:sequence>
 *       <xsd:element name="Community" type="Community"/>
 *     </xsd:sequence>
 *   </xsd:complexType>
 *
 *   <xsd:complexType name="Community">
 *     <xsd:sequence>
 *       <xsd:element name="StartMillis"      type="xsd:decimal"/>
 *       <xsd:element name="StopMillis"       type="xsd:decimal"/>
 *       <xsd:element name="SniffInterval" type="xsd:decimal"/>
 *       <xsd:element name="Demand"        type="xsd:decimal"/>
 *       <xsd:element name="Production"    type="xsd:decmial"/>
 *       <xsd:element name="Suppliers"     type="Suppliers"/>
 *       <xsd:element name="Geography"     type="Geography"/>
 *     </xsd:sequence>
 *     <xsd:attribute name="name"     type="xsd:string"  use="required"/>
 *     <!-- Template name MUST be the EXACT name of a Java Template file -->
 *     <xsd:attribute name="template" type="xsd:string"  use="required"/>
 *   </xsd:complexType>
 *
 *   <xsd:complexType name="Suppliers">
 *     <xsd:sequence>
 *       <!-- Each SupplyCommunity must be a valid Community name -->
 *       <xsd:element name="SupplyCommunity" type="xsd:string" minOccurs="0" maxOccurs="unbounded"/>
 *     </xsd:sequence>
 *   </xsd:complexType>
 *
 *   <xsd:complexType name="Geogrpahy">
 *     <xsd:sequence>
 *       <xsd:element name="Latitude" type="xsd:decimal"/>
 *       <xsd:element name="Longitude" type="xsd:decimal"/> 
 *     </xsd:sequence>
 *   </xsd:complexType>
 * </xsd:schema>
 * </code>
 */
public class CommunityConfig  {

  /** Name of the Community **/
  private String name = null;

  /** Name of the Agent Template **/
  private String tmplate = null;

  /** Start time **/
  private long startMillis = 0;
  
  /** Stop time **/
  private long stopMillis  = 0;

  /** Attacker Sniff Interval **/
  private long sniffInterval = 0;

  /** Amount of Demand this Community has **/
  private long demand = 0;

  /** Amount of Production this Community has **/
  private long production = 0;

  /** List of all OrgAssets for this Community **/
  private List supportedCommunityNames = null;

  /** Geographical Location of this Community **/
  private float latitude = 0.0f;
  private float longitude = 0.0f;
  private LatLonPoint llp;

  /**
   * Creates a new <code>CommunityConfig</code> instance.
   */
  public CommunityConfig() {
    llp = new LatLonPoint();
  }

  /**
   *  Sets the name of the Community.
   * <br>
   * @param name of the community
   */
  public void setName(String name) {
    if(this.name != null) {
      throw new IllegalArgumentException(
          "Name already Set: " + this.name + ":");
    }
    this.name = name;
  }

  /**
   * Gets the name of the Community.
   * <br>
   * @return The Community name
   */
  public String getName() {
    return this.name;
  }

  /**
   * Sets the name of the Agent Template that
   * this community will use.
   * <br>
   * @param tmplate name
   */
  public void setTemplate(String tmplate) {
    if(this.tmplate != null) {
      throw new IllegalArgumentException("Template already Set");
    }
    this.tmplate = tmplate;
  }

  /**
   * Gets the name of the Agent Template that this
   * Community will use.
   * <br>
   * @return The Agent Template name
   */
  public String getTemplate() {
    return this.tmplate;
  }

  /**
   * Sets the Scenario Start time for this community.
   * <br>
   * @param startMillis The scenario start time.
   */
  public void setStartMillis(long startMillis) {
    this.startMillis = startMillis;
  }

  /**
   * Gets the Scenario start time for this community.
   * <br>
   * @return The scenario start time.
   */
  public long getStartMillis() {
    return this.startMillis;
  }

  /**
   * Sets the Scenario Stop time for this community.
   * <br>
   * @param startMillis The scenario stop time.
   */
  public void setStopMillis(long stopMillis) {
    this.stopMillis = stopMillis;
  }

  /**
   * Gets the Scenario stop time for this community.
   * <br>
   * @return The scenario stop time.
   */
  public long getStopMillis() {
    return this.stopMillis;
  }

  /**
   * Sets the attacker sniff interval.
   * <br>
   * @param sniff The interval to sniff
   */
  public void setSniffInterval(long sniff) {
    this.sniffInterval = sniff;
  }

  /**
   * Gets the Attacker Sniff Interval 
   * <br>
   * @return The sniff interval
   */
  public long getSniffInterval() {
    return this.sniffInterval;
  }

  /** 
   * Sets the demand that this Community has.  Demand
   * is used to calculate the rate that Customers within
   * the Community submit tasks.
   * <br>
   * @param demand for this community
   */
  public void setDemand(long demand) {
    if(this.demand != 0) {
      throw new IllegalArgumentException("Demand already Set");
    }
    this.demand = demand;
  }

  /**
   * Gets the demand of this Community.  The demand
   * is used to calculate the rate that customers within
   * the community submit tasks.
   * <br>
   * @return demand rate
   */
  public long getDemand() {
    return this.demand;
  }

  /**
   * Sets the production of this Community.  Production
   * is used to calculate the production rate of all providers
   * in the community.
   * <br>
   * @param production rate
   */
  public void setProduction(long production) {
    if(this.production != 0) {
      throw new IllegalArgumentException("production already Set");
    }
    this.production = production;
  }

  /**
   * Gets the production rate of this community.  Production
   * is used to calculate the production rate of all providers
   * in the community.
   * <br>
   * @return production rate
   */
  public long getProduction() {
    return this.production;
  }

  /**
   * Sets the name of all Communities that are supported by this 
   * Community.  
   * <p>
   * These external communities may make requests to this community
   * when they cannot satisfy requests locally.
   * <br>
   * @param list of all supported community names.
   */
  public void setSupportedCommunityNames(List list) {
    if (this.supportedCommunityNames != null) {
      throw new IllegalArgumentException(
          "SupportedCommunityNames already set");
    }

    this.supportedCommunityNames = list;
  }

  /**
   * Gets the name of all Communities that are supported by this
   * Community.
   * <p>
   * These external communities may make requests to this community
   * when they cannot satisfy requests locally.
   */
  public List getSupportedCommunityNames() {
    return this.supportedCommunityNames;
  }

  /**
   * Sets the Latitude of the center of this community. 
   * All agent placement within the community will be 
   * computed from the center of the community.
   * <br>
   * @param latitude of this community center
   */   
  public void setLatitude(float latitude) {
    if (this.latitude != 0.0) {
      throw new IllegalArgumentException("latitude already set");
    }
    this.latitude = latitude;

    this.llp.setLatitude(latitude);
  }
  
  /**
   * Gets the Latitude of the center of this community.
   * The community center is used to calculate the location
   * of all agents within the community.
   * <br>
   * @return latitude of the center of this community.
   */
  public float getLatitude() {
    return this.latitude;
  }

  /**
   * Sets the Longitude of the center of this community.
   * The community center is used to calculate the location
   * of all agents within the community.
   * <br>
   * @param longitude of the community center
   */
  public void setLongitude(float longitude) {
    if (this.longitude != 0.0) {
      throw new IllegalArgumentException("longitude already set");
    }
    this.longitude = longitude;

    this.llp.setLongitude(longitude);
  }

  /**
   * Gets the Longitude of the center of this community.
   * The community center is used to calculate the location
   * of all agents within the community.
   * <br>
   * @return longitude of the center of this community.
   */
  public float getLongitude() {
    return this.longitude;
  }

  /** 
   * Gets the <code>LatLonPoint</code> representing
   * the center of the Community.
   * <br>
   * @return Center of the Community as a <code>LatLonPoint</code>
   */
  public LatLonPoint getLatLonPoint() {
    return this.llp;
  }

  /**
   * Parses an XML "Community" Element and populates
   * all community values.
   * <br>
   * @param elem Community Element
   */
  public void parse(Element elem) {

    if ("Community".equals(elem.getTagName())) {

      setName(elem.getAttribute("name"));
      setTemplate(elem.getAttribute("template"));
	   
      NodeList nlist = elem.getChildNodes();
      List alist = new ArrayList(10);
      for (int i=0; i < nlist.getLength(); i++) {
	Node sub = (Node)nlist.item(i);
	if (sub.getNodeType() == Node.ELEMENT_NODE) {
          String tname = ((Element)sub).getTagName();
	  if ("StartMillis".equals(tname)) {
	    setStartMillis(Long.parseLong(((Text)sub.getFirstChild()).getData()));
	  } else if ("StopMillis".equals(tname)) {
	    setStopMillis(Long.parseLong(((Text)sub.getFirstChild()).getData()));
	  } else if ("SniffInterval".equals(tname)) {
	    setSniffInterval(Long.parseLong(((Text)sub.getFirstChild()).getData()));
	  } else if ("Demand".equals(tname)) {
	    setDemand(Long.parseLong(((Text)sub.getFirstChild()).getData()));
	  } else if ("Production".equals(tname)) {
	    setProduction(Long.parseLong(((Text)sub.getFirstChild()).getData()));
	  } else {
	    NodeList zlist = ((Element)sub).getChildNodes();
	  
	    for (int z=0; z < zlist.getLength(); z++) {
	      Node cSub = (Node)zlist.item(z);
	      if (cSub.getNodeType() == Node.ELEMENT_NODE) {
		if ("Supports".equals(((Element)sub).getTagName())) {
		  alist.add(((Text)cSub.getFirstChild()).getData());
		} else if ("Geography".equals(((Element)sub).getTagName())) {
		  if ("Latitude".equals(cSub.getNodeName())) {
		    setLatitude(Float.parseFloat(
			((Text)cSub.getFirstChild()).getData()));
		  } else if ("Longitude".equals(cSub.getNodeName())) {
		    setLongitude(Float.parseFloat(
			((Text)cSub.getFirstChild()).getData()));
		  }
		}
	      }
	    }
	  }
	}
      }
      setSupportedCommunityNames(alist);
    }
  }  
} // CommunityConfig
