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
package org.cougaar.tools.csmart.experiment;

import org.apache.xerces.dom.CoreDocumentImpl;
import org.apache.xerces.parsers.DOMParser;
import org.cougaar.tools.csmart.core.cdata.AgentComponentData;
import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.cdata.GenericComponentData;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.util.ConfigFinder;
import org.cougaar.util.log.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import java.io.IOException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.File;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


/**
 * ExperimentDump.java
 *
 *
 * Created: Fri May 24 10:23:33 2002
 *
 * @author <a href="mailto:bkrisler@bbn.com">Brian Krisler</a>
 * @version 1.0
 */

public class ExperimentDump {
  public static final String PARENT_NODE = "Experiment";
  public static final String HOST_NODE = "Host";
  public static final String NODE_NODE = "Node";
  public static final String AGENT_NODE = "Agent";
  public static final String NAME_ATTR = "name";

  private Logger log;

  public ExperimentDump (){
    log = CSMART.createLogger("org.cougaar.tools.csmart.experiment.ExperimentDump");
  }
  
  public ComponentData parseXMLExperiment(String filename) {
    ComponentData society = null; 
    try {
      DOMParser parser = new DOMParser();
      parser.parse(new InputSource(ConfigFinder.getInstance().open(filename)));
      Element root = parser.getDocument().getDocumentElement();
      society = new GenericComponentData();
      society.setType(ComponentData.SOCIETY);
      society.setName(root.getAttribute(NAME_ATTR));
      society.setClassName("java.lang.Object");
      society.setParent(null);
      society.setOwner(null);
      if(log.isDebugEnabled()) {
        log.debug("Creating society: " + society.getName());
      }
      parseElement(root, society);
    } catch (org.xml.sax.SAXParseException spe) {
      if (log.isErrorEnabled()) {
	log.error("Parse exception Parsing file: " + filename, spe);
      }
    } catch (org.xml.sax.SAXException se) {
      if (log.isErrorEnabled()) {
	log.error("SAX exception Parsing file: " + filename, se);
      }
    } catch (Exception e) {
      if (log.isErrorEnabled()) {
	log.error("Exception Parsing file: " + filename, e);
      }
    }

    return society;
  }

  private ComponentData parseElement(Element element, ComponentData parent) {
    NodeList children = element.getChildNodes();
    for(int i=0; i < children.getLength(); i++) {
      Node child = children.item(i);
      if(child.getNodeType() == Node.ELEMENT_NODE) {
        if(child.getNodeName().equals(HOST_NODE)) {
          parseChild((Element)child, parent, ComponentData.HOST);
        } else if(child.getNodeName().equals(NODE_NODE)) {
          parseChild((Element)child, parent, ComponentData.NODE);
        } else if(child.getNodeName().equals(AGENT_NODE)) {
          parseAgent((Element)child, parent);
        }
      }
    }
    return parent;
  }

  private ComponentData parseChild(Element child, ComponentData parent, String type) {
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

    parseElement(child, cd);
    parent.addChild(cd);
    return parent;
  }

  private ComponentData parseAgent(Element element, ComponentData parent) {
    ComponentData agent = new AgentComponentData();
    agent.setName(element.getAttribute(NAME_ATTR));
    agent.setClassName("");
    agent.setOwner(null);
    agent.setParent(parent);           
    parent.addChild(agent);
    if(log.isDebugEnabled()) {
      log.debug("Creating Agent: " + agent.getName() + " with parent " + agent.getParent().getName());
    }
    return parent;
  }


  public void createXMLExperiment(ComponentData data, File configDir) {
    if(data == null) return;

    String experimentName = data.getName();
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    try {
      DocumentBuilder db = dbf.newDocumentBuilder();
      
      Document doc = db.newDocument();
      
      if(data.getType().equals(ComponentData.SOCIETY)) {
        
        Element root = doc.createElement(PARENT_NODE);    
        root.setAttribute(NAME_ATTR, data.getName());
        doc.appendChild(root);
        ComponentData[] children = data.getChildren();
        for(int i=0; i < children.length; i++) {
          addChildToDocument(doc, children[i], root);
        }
      }

      printXMLFile(configDir, doc, experimentName);
        

    } catch(ParserConfigurationException pce) {
      if(log.isErrorEnabled()) {
        log.error("Exception creating DocumentBuilder.", pce);
      }
    } catch(IOException ioe) {
      if(log.isErrorEnabled()) {
        log.error("Exception creating file", ioe);
      }
    }

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

    element.setAttribute(NAME_ATTR, data.getName());
    ComponentData[] children = data.getChildren();
    for(int i=0; i < children.length; i++) {
      addChildToDocument(doc, children[i], element);
    }
    parent.appendChild(element);
  }

  private void printXMLFile(File configDir, Node node, String name) 
    throws IOException {
    String configFileName = name + ".xml";
    PrintWriter writer = new PrintWriter(new FileWriter(new File(configDir, configFileName)));
    try {
      internalPrint(writer, node, 0);
    } catch (Exception e) {
      if(log.isErrorEnabled()) {
        log.error("Error writing config file: " + e);
      }
    }
    finally {
      writer.close();
    }    

  }
    
  private void internalPrint(PrintWriter writer, Node node, int indent) {
    StringBuffer ibuff = new StringBuffer();
    for(int i=0; i < indent; i++) {
      ibuff.append(" ");
    }
    int type = node.getNodeType();
    switch(type) {
    case Node.DOCUMENT_NODE:
        writer.println("<?xml version=\"1.0\" encoding=\""+
                           "UTF-8" + "\"?>");  
        indent = -2;
        break;                  
    case Node.ELEMENT_NODE:
      writer.print(ibuff.substring(0) + '<' + node.getNodeName() );
      NamedNodeMap nnm = node.getAttributes();
      if(nnm != null )
        {
          int len = nnm.getLength() ;
          Attr attr;
          for ( int i = 0; i < len; i++ )
            {
              attr = (Attr)nnm.item(i);
              writer.print(' ' 
                               + attr.getNodeName()
                               + "=\""
                               + attr.getNodeValue()
                               +  '"' );
            }
        }
      writer.println('>');
      break;
      
      case Node.ENTITY_REFERENCE_NODE:
        writer.print('&' + node.getNodeName() + ';' );
        break;
      case Node.CDATA_SECTION_NODE:
        writer.print( "<![CDATA[" 
                          + node.getNodeValue()
                          + "]]>" );
        break;       
      case Node.TEXT_NODE:
        writer.print(ibuff.substring(0) + node.getNodeValue());
        break;
      case Node.PROCESSING_INSTRUCTION_NODE:
        writer.print(ibuff.substring(0) + "<?" 
                         + node.getNodeName() ) ;
        String data = node.getNodeValue();
        if ( data != null && data.length() > 0 ) {
          writer.print(' ');
          writer.print(data);
        }
        writer.println("?>");
        break;
      
    }//end of switch
    
    
    //recurse
    for(Node child = node.getFirstChild(); child != null; 
        child = child.getNextSibling()) {
      internalPrint(writer, child, indent+2);
      }
    
    //without this the ending tags will miss
    if ( type == Node.ELEMENT_NODE )
      {
        writer.println(ibuff.substring(0) + "</" + node.getNodeName() + ">");
      }
  }

  private void dumpData(ComponentData data) {
    if(log.isDebugEnabled()) {
      StringBuffer buff = new StringBuffer();
      buff.append(data.getType() + " with name " + data.getName() + "\n");
      buff.append(dumpChildren(data, 2));
      log.debug(buff.substring(0));
    }
  }

  private String dumpChildren(ComponentData data, int indent) {
    ComponentData[] children = data.getChildren();
    StringBuffer buf = new StringBuffer();
    for(int i=0; i < children.length; i++) {
      ComponentData child = children[i];
      for(int j=0; j < indent; j++) {
        buf.append("  ");
      }
      buf.append("Child of type: ");
      buf.append(child.getType());
      buf.append(" with name: ");
      buf.append(child.getName());
      buf.append(" and parent: ");
      buf.append(child.getParent().getName());
      buf.append("\n");
      if(child.getChildren().length > 0) {
        buf.append(dumpChildren(child, indent+2));
      }
    }
    return buf.substring(0);
  }

  public static void main(String[] args) {
    ExperimentDump dump = new ExperimentDump();
    ComponentData data = dump.parseXMLExperiment(args[0]);
    dump.dumpData(data);

    File file = new File("/tmp/");
    dump.createXMLExperiment(data, file);
  }

}// ExperimentDump
