/**
 * <copyright>
 *  Copyright 2002-2003 BBNT Solutions, LLC
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
package org.cougaar.tools.csmart.core.cdata;

import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.tools.csmart.util.XMLUtils;
import org.cougaar.util.log.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Iterator;

/**
 * ComponentDataXML.java
 *
 *
 * Created: Wed Jun  5 11:20:07 2002
 *
 * @author <a href="mailto:bkrisler@bbn.com">Brian Krisler</a>
 * @version 1.0
 */

public class ComponentDataXML extends XMLUtils {

  public static final String PARENT_NODE = "society";
  public static final String HOST_NODE = "host";
  public static final String NODE_NODE = "node";
  public static final String AGENT_NODE = "agent";
  public static final String ATTR_NODE = "argument";
  public static final String FACET_NODE = "facet";
  public static final String CLASS_NODE = "class";
  public static final String VMARG_NODE = "vm_parameter";
  public static final String PARG_NODE = "prog_parameter";
  public static final String COMPONENT_NODE = "component";
  public static final String NAME_ATTR = "name";
  public static final String CLASS_ATTR = "class";
  public static final String PRIORITY_ATTR = "priority";
  public static final String INSERTION_ATTR = "insertionpoint";

  private static Logger log = CSMART.createLogger("org.cougaar.tools.csmart.core.cdata.ComponentDataXML");

  public static Document createXMLDocument(ComponentData data) {
    if(data == null) return null;

    Document doc = null;

    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    try {
      DocumentBuilder db = dbf.newDocumentBuilder();

      doc = db.newDocument();

      if(data.getType().equals(ComponentData.SOCIETY)) {

        Element root = doc.createElement(PARENT_NODE);
        root.setAttribute(NAME_ATTR, data.getName());
        root.setAttribute("xmlns:xsi","http://www.w3.org/2001/XMLSchema-instance" );
        root.setAttribute("xsi:noNamespaceSchemaLocation", "http://www.cougaar.org/2003/society.xsd");
        doc.appendChild(root);
        ComponentData[] children = data.getChildren();
        for(int i=0; i < children.length; i++) {
          addChildToDocument(doc, children[i], root);
        }
      }
    } catch(ParserConfigurationException pce) {
      if(log.isErrorEnabled()) {
        log.error("Exception creating DocumentBuilder.", pce);
      }
    }
    return doc;
  }

  public static ComponentData createComponentData(File file) {
    Document doc = null;
    try {
      doc = loadXMLFile(file);
    } catch(FileNotFoundException e) {
      if (log.isErrorEnabled()) {
        log.error("File: " + file.getName() + " could not be found.");
      }
    }
    if(doc == null) return null;

    ComponentData society = new GenericComponentData();
    society.setType(ComponentData.SOCIETY);
    society.setName(doc.getDocumentElement().getAttribute(NAME_ATTR));
    society.setClassName("java.lang.Object");
    society.setParent(null);
    society.setOwner(null);
    if(log.isDebugEnabled()) {
      log.debug("Creating society: " + society.getName());
    }
    parse(doc.getDocumentElement(), society);

    return society;
  }

  public static ComponentData createComponentData(String filename) {
    Document doc = loadXMLFile(filename);
    if(doc == null) return null;

    ComponentData society = new GenericComponentData();
    society.setType(ComponentData.SOCIETY);
    society.setName(doc.getDocumentElement().getAttribute(NAME_ATTR));
    society.setClassName("java.lang.Object");
    society.setParent(null);
    society.setOwner(null);
    if(log.isDebugEnabled()) {
      log.debug("Creating society: " + society.getName());
    }
    parse(doc.getDocumentElement(), society);

    return society;
  }

  private static ComponentData parse(Element element, ComponentData parent) {
    NodeList children = element.getChildNodes();
    for(int i=0; i < children.getLength(); i++) {
      Node child = children.item(i);
      if(child.getNodeType() == Node.ELEMENT_NODE) {
        if(child.getNodeName().equals(HOST_NODE)) {
          parseGenericChild((Element)child, parent, ComponentData.HOST);
        } else if(child.getNodeName().equals(NODE_NODE)) {
          parseGenericChild((Element)child, parent, ComponentData.NODE);
        } else if(child.getNodeName().equals(AGENT_NODE)) {
          parseAgentChild((Element)child, parent);
        }
      }
    }
    return parent;
  }

  private static ComponentData parseGenericChild(Element child, ComponentData parent, String type) {
    ComponentData cd = new GenericComponentData();
    cd.setType(type);
    cd.setName(child.getAttribute(NAME_ATTR));
    cd.setClassName("");
    cd.setOwner(null);
    cd.setParent(parent);
    if(log.isDebugEnabled()) {
      log.debug("Creating " + cd.getType() + " : " + cd.getName() +
                " with parent: " + cd.getParent().getName());
    }

    parse(child, cd);
    parent.addChild(cd);
    return parent;
  }

  private static ComponentData parseAgentChild(Element element, ComponentData parent) {
    ComponentData agent = new AgentComponentData();
    agent.setName(element.getAttribute(NAME_ATTR));
    agent.setOwner(null);
    agent.setParent(parent);
    parent.addChild(agent);
    if(log.isDebugEnabled()) {
      log.debug("Creating Agent: " + agent.getName() + " with parent " + agent.getParent().getName());
    }
    return parent;
  }

  private static void addChildToDocument(Document doc, ComponentData data, Element parent) {
    Element element = null;
    if(data.getType().equals(ComponentData.HOST)) {
      element = doc.createElement(HOST_NODE);
      addAttribute(element, NAME_ATTR, data.getName());
    } else if(data.getType().equals(ComponentData.NODE)) {
      element = doc.createElement(NODE_NODE);
      addAttribute(element, NAME_ATTR, data.getName());
    } else if(data.getType().equals(ComponentData.AGENT)) {
      element = doc.createElement(AGENT_NODE);
      addAttribute(element, CLASS_ATTR, data.getClassName());
      addAttribute(element, NAME_ATTR, data.getName());
    } else if(data.getType().equals(ComponentData.PLUGIN)) {
      element = doc.createElement(COMPONENT_NODE);
      addAttribute(element, NAME_ATTR, data.getName());
      addAttribute(element, CLASS_ATTR, data.getClassName());
      addAttribute(element, PRIORITY_ATTR, data.getPriority());
      addAttribute(element, INSERTION_ATTR, GenericComponentData.getLongType(data.getType()));
      Object[] params = data.getParameters();
      for (int i = 0; i < params.length; i++) {
        Element sub = doc.createElement(ATTR_NODE);
        Text txt = doc.createTextNode(((String)params[i]).trim());
        sub.appendChild(txt);
        element.appendChild(sub);
      }
    } else if(data.getType().equals("facet")) {
      element = doc.createElement(FACET_NODE);
      HashMap map = (HashMap)data.getParameter(0);
      Iterator keys = (map.keySet()).iterator();
      while (keys.hasNext()) {
        String str = (String) keys.next();
        addAttribute(element, str, (String)map.get(str));
      }
    } else if(data.getType().equals("class") || data.getType().equals("vm_parameter")
        || data.getType().equals("prog_parameter") || data.getType().equals("env_parameter")) {
      element = doc.createElement(data.getType());
      Text txt = doc.createTextNode(data.getName());
      element.appendChild(txt);
    }

    // For now, don't walk below Agent.  This will
    // need to change when we implement ability to
    // Generate a complete XML file from ComponentData.
    ComponentData[] children = data.getChildren();
    for(int i=0; i < children.length; i++) {
      addChildToDocument(doc, children[i], element);
    }

    if(element != null) {
      parent.appendChild(element);
    }
  }

  private static void addAttribute(Element element, String attr, String txt) {
    element.setAttribute(attr, ((txt == null) ? "" : txt));
  }


}// ComponentDataXML
