/* 
 * <copyright>
 *  Copyright 2001-2002 BBNT Solutions, LLC
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
package org.cougaar.tools.csmart.runtime.plugin;

import java.util.*;
import java.io.*;

import org.apache.xerces.parsers.DOMParser;
import org.xml.sax.InputSource;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;

import org.cougaar.core.agent.ClusterIdentifier;
import org.cougaar.core.util.UID;
import org.cougaar.util.UnaryPredicate;

import org.cougaar.tools.csmart.util.LatLonPoint;

import org.cougaar.tools.csmart.runtime.ldm.event.RealWorldEvent;
import org.cougaar.tools.csmart.runtime.ldm.event.CyberAttackEvent;
import org.cougaar.tools.csmart.runtime.ldm.event.NewCyberAttackEvent;
import org.cougaar.tools.csmart.runtime.ldm.event.KineticEvent;
import org.cougaar.tools.csmart.runtime.ldm.event.NewSimpleKEvent;
import org.cougaar.util.log.Logger;
import org.cougaar.tools.csmart.ui.viewer.CSMART;

/**
 * The ScriptedEventPlugIn allows real world events to enter the society
 * via a script.  Theses events can be AttackEvents or Kinetic events.<br>
 * This plugin will read in an script that gives the list of events and
 * will publish those events with visibility times equal to the time indicated
 * in the events file plus the current time.<br>
 * The XML file has one main Node - an EVENTLIST. This list contains many Nodes,
 * of type CYBER or KINETIC. Either way, there are two possible parameters:
 * Intensity and Duration (in milliseconds).<br>
 * There are also 3 or 4 inline attributes. This includes Type, Time (in milliseconds
 * from the start of the PlugIn), and either the ClusterID of the target Agent for a Cyber attack,
 * or the latitude and longitude center of the kinetic attack.<br>
 *
 * @author <a href="mailto:wfarrell@bbn.com">Wilson Farrell</a>
 * @see CSMARTPlugIn
 */
public class ScriptedEventPlugIn
    extends CSMARTPlugIn {

  /** ClusterID of Agent containing the Transducer PlugIn **/
  private ClusterIdentifier transducer;

  private transient Logger log;

  /**
   * Lets face it, this plugin just blindly publishes events, it doesn't
   * even care if they succeed.  No subscriptions for now.<br>
   * It parses the given XML events file, and sends one event per entry.<br>
   */
  public void setupSubscriptions() {
    log = CSMART.createLogger(this.getClass().getName());

    if (log.isDebugEnabled()) {
      log.debug("setupSubscriptions:" + this + ":Entering");
    }
    
    // Load in the config file.
    Vector pv = getParameters() != null ? new Vector(getParameters()) : null;
    if(pv == null ) {
      throw new RuntimeException("ScriptedEventPlugIn expects parameters, got none");
    }
    
    String eventFileName = (String)pv.elementAt(0);
    if(eventFileName == null) {
      throw new RuntimeException("ScriptedEventPlugIn expected param[0] to be eventFile name");
    }
    String transducerName = (String)pv.elementAt(1);
    if (transducerName == null || transducerName.equals("")) {
      throw new RuntimeException("ScriptedEventPlugIn expected param[1] to be the name of the Transducer Agent");
    }
    transducer = new ClusterIdentifier(transducerName);
    
    publishEvents(eventFileName);
  }
  
  /**
   * Parses the xml file containing the real world events , builds events and
   * publishes them.
   * <br>
   *
   * @param Filename of the Tasks ini File.
   */
  private void publishEvents(String fileName) {
    
    try {
      DOMParser parser = new DOMParser();
      parser.parse(new InputSource(getConfigFinder().open(fileName)));
      
      // Get a list of all Nodes in the document. "*" Returns all Elements.
      NodeList nodes = parser.getDocument().getElementsByTagName("*");
      if (log.isDebugEnabled()) {
	log.debug("publishEvents: " + this + ": got " + nodes.getLength() + " nodes");
      }
      for (int i = 0; i < nodes.getLength(); i++) {
	Node node = (Node)nodes.item(i);
	String nodeName = node.getNodeName();
	if (nodeName.equals("EVENTLIST")) {
	  // this is the parent node
	  // keep going
	} else if (nodeName.equals("CYBER")) {
	  RealWorldEvent cyber = createCyber(node);
	  if (log.isDebugEnabled()) {
	    log.debug("publishEvents:" + this + ": Publishing cyber event "+cyber.getUID());
	  }
	  // Note: The Transducer is responsible for delaying the InfEvent as
	  // necessary.
	  publishAdd(cyber);
	} else if (nodeName.equals("KINETIC")) {
	  // Have a Kinetic Event Node
	  RealWorldEvent kinetic = createKinetic(node);
	  if (log.isDebugEnabled()) {
	    log.debug("publishEvents:" + this + ": Publishing kinetic event "+kinetic.getUID());
	  }
	  // Note: The Transducer is responsible for delaying the InfEvent as
	  // necessary.
	  publishAdd(kinetic);
	} else {
	  if (log.isDebugEnabled()) {
	    log.debug("publishEvents:" + this + ": got unknown node type");
	  }
	  if (nodeName.equals("EVENTLIST")) {
	    // Have an EventList Node
	    if (log.isDebugEnabled()) {
	      log.debug("publishEvents:" + this + ": got node type EVENTLIST");
	    }
	  }
	  if (nodeName.equals("PARAM")) {
	    // Have a Param Node
	    if (log.isDebugEnabled()) {
	      log.debug("publishEvents:" + this + ": got node type PARAM");
	    }
	  }
	}
      }
    } catch (org.xml.sax.SAXParseException spe) {
      if (log.isErrorEnabled()) {
	log.error("publishEvents: Parse exception Parsing file: " + fileName, spe);
      }
    } catch (org.xml.sax.SAXException se) {
      if (log.isErrorEnabled()) {
	log.error("publishEvents: SAX exception parsing file: " + fileName, se);
      }
    } catch (Exception e) {
      if (log.isErrorEnabled()) {
	log.error("publishEvents: exception parsing Attack Tree File: " + fileName, e);
      }
    }
  }
  
  /**
   * Create a CyberAttackEvent using the config data in the given XML Node
   *
   * @param node a <code>Node</code> of XML data
   * @return a <code>CyberAttackEvent</code> as specified by that Node
   */
  private CyberAttackEvent createCyber(Node node) {
    NamedNodeMap nnm = node.getAttributes();
    String target = nnm.getNamedItem("TARGET").getNodeValue();
    String type = nnm.getNamedItem("TYPE").getNodeValue();
    long time = 0l;
    try {
      time = Long.parseLong(nnm.getNamedItem("TIME").getNodeValue());
    } catch (NumberFormatException nfe) {
      throw new IllegalArgumentException("time must be a long in " +
					 type +
					 " against " +
					 target);
    }
    Hashtable params = getParams(node);
    
    NewCyberAttackEvent cae = theCSMARTF.newCyberAttackEvent();
    cae.setTarget(new ClusterIdentifier(target));
    cae.setType(type);
    // Time is delta from now, in millis
    cae.setTime(time + currentTimeMillis());
    Object intensityString = params.get("Intensity");
    if (intensityString == null) {
      cae.setIntensity(0.0);
    } else {
      try {
	cae.setIntensity(
			 Double.parseDouble((String) intensityString));
      } catch (NumberFormatException nfe) {
	throw new IllegalArgumentException(
					   "intensity must be a double in " +
					   type +
					   " against " +
					   target);
      }
    }
    Object durationString = params.get("Duration");
    if (durationString == null) {
      cae.setDuration(0);
    } else {
      try {
	cae.setDuration(
                        Long.parseLong((String) durationString));
      } catch (NumberFormatException nfe) {
	throw new IllegalArgumentException(
					   "duration must be a long in " +
					   type +
					   " against " +
					   target);
      }
    }
    // other fields to set???
    cae.setSource(getAgentIdentifier());
    cae.setDestination(transducer);
    cae.setPublisher(this.toString());
    return cae;
  }

  /**
   * Create a Kinetic event from the given XML data
   *
   * @param node a <code>Node</code> of XML data
   * @return a <code>KineticEvent</code> as specified by the XML
   */
  private KineticEvent createKinetic(Node node) {
    NamedNodeMap nnm = node.getAttributes();
    String latString = nnm.getNamedItem("LATITUDE").getNodeValue();
    String longString = nnm.getNamedItem("LONGITUDE").getNodeValue();
    LatLonPoint pos = null;
    try {
      pos = new LatLonPoint(Float.parseFloat(latString),
			    Float.parseFloat(longString));
    } catch (NumberFormatException nfe) {
      throw new IllegalArgumentException("LATITUDE and LONGITUDE must both be floats");
    }
    String type = nnm.getNamedItem("TYPE").getNodeValue();
    long time = 0l;
    try {
      time = Long.parseLong(nnm.getNamedItem("TIME").getNodeValue());
    } catch (NumberFormatException nfe) {
      throw new IllegalArgumentException("time must be a long in " +
					 type +
					 " at " +
					 pos.getLatitude() + "," +
					 pos.getLongitude());
    }
    Hashtable params = getParams(node);
    
    NewSimpleKEvent ske = theCSMARTF.newSimpleKEvent();
    ske.setLocation(pos);
    ske.setType(type);
    // Time is millis into the future
    ske.setTime(time + currentTimeMillis());
    Object intensityString = params.get("Intensity");
    if (intensityString == null) {
      ske.setIntensity(0.0);
    } else {
      try {
	ske.setIntensity(
			 Double.parseDouble((String) intensityString));
      } catch (NumberFormatException nfe) {
	throw new IllegalArgumentException(
					   "intensity must be a double in " +
					   type +
					   " at " +
					   pos.getLatitude() + "," + pos.getLongitude());
      }
    }
    Object durationString = params.get("Duration");
    if (durationString == null) {
      ske.setDuration(0);
    } else {
      try {
	ske.setDuration(
                        Long.parseLong((String) durationString));
      } catch (NumberFormatException nfe) {
	throw new IllegalArgumentException(
					   "duration must be a long in " +
					   type +
					   " at " +
					   pos.getLatitude() + "," + pos.getLongitude());
      }
    }
    // other things to set???
    ske.setSource(getAgentIdentifier());
    ske.setDestination(transducer);
    ske.setPublisher(this.toString());
    return ske;
  }
  
  private Hashtable getParams(Node node) {    
    NodeList nl = node.getChildNodes();
    Hashtable ht = new Hashtable();
    for (int i = 0; i < nl.getLength(); i++) {
      Node n = nl.item(i);
      if(n.getNodeName().equals("PARAM")) {
	NamedNodeMap nnm = n.getAttributes();   
	ht.put(nnm.getNamedItem("NAME").getNodeValue(), nnm.getNamedItem("VALUE").getNodeValue());
      }
    }
    return ht;
  }
  
  /**
   * Execute does nothing.  This plugin doesn't subscribe for any events.
   */
  public void execute() { }
} // end of ScriptedEventPlugIn.java
