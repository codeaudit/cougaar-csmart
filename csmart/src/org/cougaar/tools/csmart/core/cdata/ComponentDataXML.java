/**
 * <copyright>
 *  Copyright 2002 BBNT Solutions, LLC
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
 *  </copyright>
 */
package org.cougaar.tools.csmart.core.cdata;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.cougaar.tools.csmart.core.cdata.AgentComponentData;
import org.cougaar.tools.csmart.core.cdata.GenericComponentData;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.tools.csmart.util.XMLUtils;
import org.cougaar.util.log.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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

  public static final String PARENT_NODE = "Experiment";
  public static final String HOST_NODE = "Host";
  public static final String NODE_NODE = "Node";
  public static final String AGENT_NODE = "Agent";
  public static final String NAME_ATTR = "name";
  
  private Logger log;

  public ComponentDataXML (){
    log = CSMART.createLogger("org.cougaar.tools.csmart.core.cdata.ComponentDataXML");
  }

  public Document createXMLDocument(ComponentData data) {
    if(data == null) return null;

    Document doc = null;

    String experimentName = data.getName();
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    try {
      DocumentBuilder db = dbf.newDocumentBuilder();
      
      doc = db.newDocument();
      
      if(data.getType().equals(ComponentData.SOCIETY)) {
        
        Element root = doc.createElement(PARENT_NODE);    
        root.setAttribute(NAME_ATTR, data.getName());
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

  public ComponentData createComponentData(File file) {
    Document doc = loadXMLFile(file);
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

  public ComponentData createComponentData(String filename) {
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

  private ComponentData parse(Element element, ComponentData parent) {
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

  private ComponentData parseGenericChild(Element child, ComponentData parent, String type) {
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

  private ComponentData parseAgentChild(Element element, ComponentData parent) {
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

  private void addChildToDocument(Document doc, ComponentData data, Element parent) {
    Element element = null;
    if(data.getType().equals(ComponentData.HOST)) {
      element = doc.createElement(HOST_NODE);
    } else if(data.getType().equals(ComponentData.NODE)) {
      element = doc.createElement(NODE_NODE);
    } else if(data.getType().equals(ComponentData.AGENT)) {
      element = doc.createElement(AGENT_NODE);
    }


    if(element != null) {
      if(data != null && data.getName() != null) {
        element.setAttribute(NAME_ATTR, data.getName());
      } else {
        element.setAttribute(NAME_ATTR, "");
      }
    }

    // For now, don't walk below Agent.  This will
    // need to change when we implement ability to
    // Generate a complete XML file from ComponentData.
    if(!data.getType().equals(ComponentData.AGENT)) {
      ComponentData[] children = data.getChildren();
      for(int i=0; i < children.length; i++) {
        addChildToDocument(doc, children[i], element);
      }
    }
    if(element != null) {
      parent.appendChild(element);
    }
  }

}// ComponentDataXML
