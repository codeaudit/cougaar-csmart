/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
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

package org.cougaar.tools.csmart.experiment;

import org.cougaar.core.component.ComponentDescription;
import org.cougaar.tools.csmart.core.cdata.AgentComponentData;
import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.cdata.GenericComponentData;
import org.cougaar.tools.csmart.core.db.DBConflictHandler;
import org.cougaar.tools.csmart.society.AgentComponent;
import org.cougaar.tools.csmart.society.SocietyComponent;
import org.cougaar.tools.csmart.society.cdata.AgentCDataComponent;
import org.cougaar.tools.csmart.society.cdata.SocietyCDataComponent;
import org.cougaar.tools.csmart.ui.console.CSMARTConsoleModel;
import org.cougaar.tools.csmart.util.ReadOnlyProperties;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.swing.*;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.awt.*;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;

/**
 * org.cougaar.tools.csmart.experiment
 *
 */
public class XMLExperiment extends ExperimentBase {
  private File file;

  private ComponentData experiment;
  private String societyFileName;
  private Component parent;
  private ProgressMonitorInputStream monitor = null;

  public XMLExperiment(File file, Component parent) {
    super(file.getName());
    this.file = file;
    this.parent = parent;
    createLogger();
    setDefaultNodeArguments();
  }

  public ComponentData getSocietyComponentData() {
    return experiment;
  }

  public String getSocietyFileName() {
    return societyFileName;
  }

  public void dumpINIFiles() {
  }

  public void dumpHNA() {
  }

  public void save(DBConflictHandler ch) {
  }

  public ComponentData getExperiment() {
    return experiment;
  }

  public void doParse(boolean validate) throws Exception {
    MyHandler handler = new MyHandler();
    SAXParserFactory factory = SAXParserFactory.newInstance();
    factory.setValidating(validate);
    factory.setNamespaceAware(true);
    SAXParser saxParser = factory.newSAXParser();

    String text = ((factory.isValidating()) ? "Parsing and Validating " : "Parsing ");
    monitor = new ProgressMonitorInputStream(parent, text + file.getName(), new FileInputStream(file));
    InputStream in = new BufferedInputStream(monitor);
    if (in != null) {
      saxParser.parse(in, handler);
    } else {
      log.error("Unable to open " + file.getName() + " for XML initialization");
      throw new IOException("Upable to open stream for " + file.getName());
    }

    SocietyComponent soc = new SocietyCDataComponent(experiment, null);
    soc.initProperties();
    setSocietyComponent(soc);
  }

  protected void setDefaultNodeArguments() {
    defaultNodeArguments = new ReadOnlyProperties();
    createObserver();
    defaultNodeArguments.put(PERSISTENCE_ENABLE, PERSISTENCE_DFLT);
    // By default we clear any existing persistence deltas when we
    // start a run.
    defaultNodeArguments.put(PERSIST_CLEAR, PERSIST_CLEAR_DFLT);
    defaultNodeArguments.put(TIMEZONE, TIMEZONE_DFLT);
    defaultNodeArguments.put(AGENT_STARTTIME, AGENT_STARTTIME_DFLT);
    defaultNodeArguments.put(COMPLAININGLP_LEVEL, COMPLAININGLP_LEVEL_DFLT);
    defaultNodeArguments.put(CONTROL_PORT, Integer.toString(APP_SERVER_DEFAULT_PORT));

    // By default, we tell the AppServer to ignore connection errors
    // if CSMART dies, so that the society does _not_ die.
    defaultNodeArguments.put(AS_SWALLOW_ERRORS, AS_SWALLOW_ERRORS_DFLT);

    // Class of Node to run. This is the first argument to the BOOTSTRAP_CLASS
    // below.
    defaultNodeArguments.put(CSMARTConsoleModel.COMMAND_ARGUMENTS, DEFAULT_NODE_CLASS);

    // Class of bootstrapper to use. The actual class being executed
    defaultNodeArguments.put(BOOTSTRAP_CLASS, DEFAULT_BOOTSTRAP_CLASS);

    try {
      defaultNodeArguments.put(ENV_DISPLAY, InetAddress.getLocalHost().getHostName() + ":0.0");
    } catch (UnknownHostException uhe) {
      if (log.isErrorEnabled()) {
        log.error("UnknownHost Exception", uhe);
      }
    }
  }

//  private static String insertionPointContainer(String insertionPoint) {
//    return insertionPoint.substring(0, insertionPoint.lastIndexOf('.'));
//  }

  private class MyHandler extends DefaultHandler {

    boolean thisNode = false;
    CharArrayWriter chars;
    ComponentData host;
    ComponentData node;
    ComponentData agent;
    ComponentData component;
    ComponentData parent;
    ComponentData current;
    ComponentData childless;
    HostComponent hostComp = null;
    NodeComponent nodeComp = null;

    private final String stdPriority = ComponentDescription.priorityToString(ComponentDescription.PRIORITY_STANDARD);


    public void startElement(
        String namespaceURI,
        String localName,
        String qName,
        Attributes atts)
        throws SAXException {
      if (localName.equals("society")) {
        experiment = new GenericComponentData();
        experiment.setName(atts.getValue("name"));
        if (log.isDebugEnabled()) {
          log.debug("started element for society: " + experiment.getName());
        }
        experiment.setType(ComponentData.SOCIETY);
        experiment.setClassName("");
        experiment.setParent(null);
        experiment.setOwner(null);
        current = experiment;
        parent = experiment;
      }
      if (localName.equals("host")) {
        hostComp = XMLExperiment.this.addHost(atts.getValue("name"));
        host = new GenericComponentData();
        host.setName(atts.getValue("name"));
        if (log.isDebugEnabled()) {
          log.debug("started element for host: " + host.getName());
        }
        host.setType(ComponentData.HOST);
        host.setParent(experiment);
        host.setOwner(null);
        current = host;
        parent = host;
      }
      if (localName.equals("node")) {
        nodeComp = XMLExperiment.this.addNode(atts.getValue("name"));
        node = new GenericComponentData();
        node.setName(atts.getValue("name"));
        if (log.isDebugEnabled())
          log.debug("started element for node: " + node.getName());

        node.setType(ComponentData.NODE);
        node.setParent(host);
        node.setOwner(null);
        current = node;
        parent = node;
      }
      if (localName.equals("agent")) {
        agent = new AgentComponentData();
        agent.setName(atts.getValue("name"));
        if (log.isDebugEnabled())
          log.debug("started element for agent " + agent.getName());

        agent.setClassName(atts.getValue("class"));
        agent.setType(GenericComponentData.getCanonicalType(atts.getValue("insertionpoint")));
        agent.setPriority(stdPriority);
        agent.setParent(node);
        current = agent;
        parent = agent;
      } else if (localName.equals("component")) {
        component = new GenericComponentData();
        component.setName(atts.getValue("name"));
        if (log.isDebugEnabled()) {
          log.debug("Have component: " + atts.getValue("name"));
        }

        component.setClassName(atts.getValue("class"));
        component.setPriority(atts.getValue("priority"));
        component.setParent(parent);
        component.setType(GenericComponentData.getCanonicalType(atts.getValue("insertionpoint")));
        current = component;
      } else if (localName.equals("argument")) {
        if (log.isDebugEnabled()) {
          log.debug("Have an argument");
        }
        chars = new CharArrayWriter();
      } else if (localName.equals("facet")) {
        childless = new GenericComponentData();
        HashMap map = new HashMap(2);
        if(atts.getValue("superior_org_id") != null) {
          map.put("superior_org_id", atts.getValue("superior_org_id"));
        }
        if(atts.getValue("subordinate_org_id") != null) {
          map.put("subordinate_org_id", atts.getValue("subordinate_org_id"));
        }
        childless.addParameter(map);
        childless.setType("facet");
        childless.setParent(current);
        childless.setClassName("");
      } else if (localName.equals("class") || localName.equals("prog_parameter") ||
          localName.equals("vm_parameter") || localName.equals("env_parameter")) {
        childless = new GenericComponentData();
        childless.setType(localName);
        childless.setName(localName);
        chars = new CharArrayWriter();
      }
    }

    /**
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    public void characters(char[] ch, int start, int length)
        throws SAXException {
      if (chars != null)
        chars.write(ch, start, length);
    }

    /**
     * @see org.xml.sax.ContentHandler#endElement(String, String, String)
     */
    public void endElement(String namespaceURI, String localName, String qName)
        throws SAXException {
      if (localName.equals("argument")) {
        current.addParameter(chars.toString().trim());
        chars = null;
      } else if (localName.equals("class") || localName.equals("prog_parameter")||localName.equals("vm_parameter") ) {
        GenericComponentData gcd = new GenericComponentData();
        gcd.setName(chars.toString().trim());
        gcd.setType(localName);
        addToNode(localName, chars.toString().trim());
        current.addChild(gcd);
        current.setParent(agent);
        current = parent;
      } else if (localName.equals("facet")) {
        parent.addChild(childless);
      } else if (localName.equals("component")) {
        parent.addChild(component);
      } else if (localName.equals("agent")) {
        node.addChild(agent);
        AgentComponent ac = new AgentCDataComponent(agent);
        ac.initProperties();
        nodeComp.addAgent(ac);
        if (log.isDebugEnabled())
          log.debug("finished a agent");
      } else if (localName.equals("node")) {
        host.addChild(node);
        hostComp.addNode(nodeComp);
        if (log.isDebugEnabled())
          log.debug("finished a node");
        nodeComp = null;
      } else if (localName.equals("host")) {
        experiment.addChild(host);
        hostComp = null;
        if (log.isDebugEnabled()) {
          log.debug("finished a host");
        }
      } else if (localName.equals("society")) {
        if (log.isDebugEnabled()) {
          log.debug("finished a society");
        }
      }
    }

    private void addToNode(String type, String value) {
      if (type.equals("class")) {
        nodeComp.addProperty(CSMARTConsoleModel.COMMAND_ARGUMENTS, value);
      } else if(type.equals("prog_parameter")) {
        nodeComp.addProperty(CSMARTConsoleModel.COMMAND_ARGUMENTS, value);
      } else if(type.equals("vm_parameter")) {
        if(value.indexOf("=") != -1) {
          String name = value.substring(2, value.indexOf("="));
          String val = value.substring((value.indexOf("=") + 1));
          if(name.equals("org.cougaar.society.file")) {
            XMLExperiment.this.societyFileName = val;
          }
          nodeComp.addArgument(name, val);
        } else {
          nodeComp.addArgument(value, "");
        }
      } else if(type.equals("env_parameter")) {
        nodeComp.addProperty(CSMARTConsoleModel.COMMAND_ARGUMENTS, value);
      }
    }

    /**
     * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
     */
    public void error(SAXParseException exception) throws SAXException {
      log.error("Error parsing the file", exception);
      super.error(exception);
    }

    /**
     * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
     */
    public void warning(SAXParseException exception) throws SAXException {
      log.warn("Warning parsing the file", exception);
      super.warning(exception);
    }

  }
}
