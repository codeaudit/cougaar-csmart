/* 
 * <copyright>
 *  Copyright 2001-2003 BBNT Solutions, LLC
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

import org.cougaar.tools.csmart.core.cdata.AgentComponentData;
import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.cdata.GenericComponentData;
import org.cougaar.tools.csmart.core.cdata.LeafComponentData;
import org.cougaar.tools.csmart.core.property.BaseComponent;
import org.cougaar.tools.csmart.core.property.name.ComponentName;
import org.cougaar.tools.csmart.society.AgentComponent;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.util.log.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Config writer that writes out only the LeafComponentData - that is, only writes files
 * that are parameters to Plugins and are not stored in the ConfigDB
 * Note this should be fixed - it constructs the _complete_ ComponentData
 * and then inefficiently finds the Leaf files to write them.
 **/
public class LeafOnlyConfigWriter implements ConfigurationWriter {
  transient NodeComponent[] nodesToWrite;
  transient List components;
  ComponentData theSoc;
  private transient Logger log;

  public LeafOnlyConfigWriter(ComponentData theSoc) {
    createLogger();
    this.theSoc = theSoc;
  }

  public LeafOnlyConfigWriter(List components, NodeComponent[] nodesToWrite, Experiment exp) {
    createLogger();
    this.nodesToWrite = nodesToWrite;
    this.components = components;
    theSoc = new GenericComponentData();
    theSoc.setType(ComponentData.SOCIETY);
    theSoc.setName(exp.getExperimentName()); // this should be experiment: trial FIXME
    theSoc.setClassName(""); // leave this out? FIXME
    theSoc.setOwner(exp); // the experiment
    theSoc.setParent(null);
    // For each node, create a GenericComponentData, and add it to the society
    addNodes(exp);

    // Some components will want access to the complete set of Nodes in the society, etc.
    // To get that, they must get back to the root soc object,
    // and do a getOwner and go from there. Ugly.
    
    // Now ask each component in turn to add its stuff
    for (int i = 0; i < components.size(); i++) {
      BaseComponent soc = (BaseComponent) components.get(i);
      soc.addComponentData(theSoc);
    }
    // Then give everyone a chance to modify what they've collectively produced
    for (int i = components.size() - 1; i >= 0; i--) {
      BaseComponent soc = (BaseComponent) components.get(i);
      soc.modifyComponentData(theSoc);
    }    
  }

  /**
   * Obtains a list of all file names that need to be written
   * out by the server.  If there are no extra (non-db) files
   * for this society, null is returned.
   *
   * @return an <code>Iterator</code> of all names or null if none.
   */
  public Iterator getFileNames() {
    ArrayList files = new ArrayList();
    Collection c;

    if(log.isDebugEnabled() && theSoc == null) {
      log.debug("theSoc is null");
    }
    ComponentData[] nodes = theSoc.getChildren();
    for(int i=0; i < theSoc.childCount(); i++) {
      if(( c = getNodeFiles(nodes[i])) != null ) {
        files.addAll(c);
      }
    }

    return files.iterator();
  }

  private Collection getNodeFiles(ComponentData nc) {
    ArrayList files = new ArrayList();
    Collection c;

    // loop over children
    // if there are binder or such, do those
    ComponentData[] children = nc.getChildren();
    for (int i = 0; i < nc.childCount(); i++) {
      // write out any leaf components
      if(( c = getLeafNames(children[i])) != null) {
        files.addAll(c);
      }
    }
    for (int i = 0; i < nc.childCount(); i++) {
      if (children[i] instanceof AgentComponentData) {
        if((c = getAgentFiles((AgentComponentData)children[i])) != null) {
          files.addAll(c);
        }
      }
    }

    return files;
  }

  private Collection getLeafNames(ComponentData me) {
    ArrayList files = new ArrayList();

    if (me.leafCount() < 1)
      return null;
    LeafComponentData[] leaves = me.getLeafComponents();
    for (int i = 0; i < me.leafCount(); i++) {
      LeafComponentData leaf = leaves[i];
      if (leaf == null)
	continue;
      if (!leaf.getType().equals(LeafComponentData.FILE)) {
        if(log.isErrorEnabled()) {
          log.error("Got unknown LeafComponent type: " + leaf.getType());
        }
	continue;
      }

      files.add(leaf.getName());
    } // end of loop over leaves
    
    return files;
  }

  private Collection getAgentFiles(AgentComponentData ac) {
    ArrayList files = new ArrayList();
    Collection c, d;

    if(( c = getChildrenOfComp((ComponentData)ac)) != null) {
      files.addAll(c);
    }

    if(( d = getLeafNames((ComponentData)ac)) != null) {
      files.addAll(d);
    }

    return files;
  }

  private Collection getChildrenOfComp(ComponentData comp) {
    ArrayList files = new ArrayList();
    Collection c;

    if (comp == null || comp.childCount() == 0)
      return null;
    ComponentData[] children = comp.getChildren();
    for (int i = 0; i < children.length; i++) {
      // write out any leaf components
      if((c = getLeafNames(children[i])) != null) {
        files.addAll(c);
      }
    }

    return files;
  }

  /**
   * Describe <code>writeFile</code> method here.
   *
   * @param filename 
   * @param out 
   * @exception Exception if an error occurs
   */
  public void writeFile(String filename, OutputStream out) throws Exception {
    // find the file, the write it.
    ComponentData[] nodes = theSoc.getChildren();
    for(int i=0; i < theSoc.childCount(); i++) {
      String contents = findFile(nodes[i], filename);
      if(contents != null) {
        out.write(contents.getBytes());
      } else {
        if(log.isWarnEnabled()) {
          log.warn("Warning: Could not locate: " + filename + " + in ComponentData");
        }
      }
    }
  }

  private String findFile(ComponentData nc, String filename) {
    // loop over children
    ComponentData[] children = nc.getChildren();
    for (int i = 0; i < nc.childCount(); i++) {
      // write out any leaf components
      String contents = findInLeaf(children[i], filename);
      if(contents != null) {
        return contents;
      }
    }
    for (int i = 0; i < nc.childCount(); i++) {
      if (children[i] instanceof AgentComponentData) {
        String contents = findInAgent((AgentComponentData)children[i], filename);
        if(contents != null) {
          return contents;
        }
      }
    }

    return null;
  }

  private String findInAgent(AgentComponentData ac, String filename) {
    String contents = null;

    contents = findInChildren((ComponentData)ac, filename);
    if(contents == null) {
      contents = findInLeaf((ComponentData)ac, filename);
    }

    return contents;
  }

  private String findInChildren(ComponentData comp, String filename) {
    if (comp == null || comp.childCount() == 0)
      return null;
    ComponentData[] children = comp.getChildren();
    for (int i = 0; i < children.length; i++) {
      // write out any leaf components
      String contents = findInLeaf(children[i], filename);
      if(contents != null) {
        return contents;
      }
    }

    return null;
  }

  private String findInLeaf(ComponentData me, String filename) {
    if (me.leafCount() < 1)
      return null;
    LeafComponentData[] leaves = me.getLeafComponents();
    for (int i = 0; i < me.leafCount(); i++) {
      LeafComponentData leaf = leaves[i];
      if (leaf == null)
	continue;
      if (!leaf.getType().equals(LeafComponentData.FILE)) {
        if(log.isErrorEnabled()) {
          log.error("Got unknown LeafComponent type: " + leaf.getType());
        }
	continue;
      }

      if(leaf.getName().equals(filename)) {
        return (String)leaf.getValue();
      }
    } // end of loop over leaves

    return null;
  }



  /**
   * @deprecated
   */
  public void writeConfigFiles(File configDir) throws IOException {
    // Call writeNodeFile for each of the nodes in theSoc.
    ComponentData[] nodes = theSoc.getChildren();
    for (int i = 0; i < theSoc.childCount(); i++) {
      writeNodeFile(configDir, nodes[i]);
    }
  }
  
  /**
   * @deprecated
   */
  private void writeNodeFile(File configDir, ComponentData nc) throws IOException {
    // loop over children
    // if there are binder or such, do those
    ComponentData[] children = nc.getChildren();
    for (int i = 0; i < nc.childCount(); i++) {
      // write out any leaf components
      writeLeafData(configDir, children[i]);
    }
    for (int i = 0; i < nc.childCount(); i++) {
      if (children[i] instanceof AgentComponentData) {
	writeAgentFile(configDir, (AgentComponentData)children[i]);
      }
    }
  }
  
  /**
   * @deprecated
   */
  private void writeChildrenOfComp(File configDir, ComponentData comp) throws IOException {
    if (comp == null || comp.childCount() == 0)
      return;
    ComponentData[] children = comp.getChildren();
    for (int i = 0; i < children.length; i++) {
      // write out any leaf components
      writeLeafData(configDir, children[i]);
    }
  }
  
  /**
   * @deprecated
   */
  private void writeAgentFile(File configDir, AgentComponentData ac) throws IOException {
    writeChildrenOfComp(configDir, (ComponentData)ac);
    // write any other leaf component data files
    writeLeafData(configDir, (ComponentData)ac);
  }
  
  /**
   * @deprecated
   */
  private void addNodes(Experiment exp) {
    for (int i = 0; i < nodesToWrite.length; i++) {
      ComponentData nc = new GenericComponentData();
      nc.setType(ComponentData.NODE);
      nc.setName(nodesToWrite[i].getShortName());
      nc.setClassName(""); // leave this out?? FIXME
      nc.setOwner(exp); // the experiment? FIXME
      nc.setParent(theSoc);
      ComponentName name = 
	  new ComponentName((BaseComponent)nodesToWrite[i], 
                            "ConfigurationFileName");
      nc.addParameter(nodesToWrite[i].getProperty(name).getValue().toString());
      theSoc.addChild(nc);
      addAgents(nodesToWrite[i], nc);
    }
  }

  /**
   * @deprecated
   */
  private void addAgents(NodeComponent node, ComponentData nc) {
    AgentComponent[] agents = node.getAgents();
    if (agents == null || agents.length == 0)
      return;
    for (int i = 0; i < agents.length; i++) {
      AgentComponentData ac = new AgentComponentData();
      ac.setName(agents[i].getFullName().toString());
      // FIXME!!
      ac.setOwner(null); // the society that contains this agent FIXME!!!
      ac.setParent(nc);
      nc.addChild((ComponentData)ac);
    }
  }


  /**
   * @deprecated
   */
  private void writeLeafData(File configDir, ComponentData me) throws IOException {
    if (me.leafCount() < 1)
      return;
    LeafComponentData[] leaves = me.getLeafComponents();
    for (int i = 0; i < me.leafCount(); i++) {
      LeafComponentData leaf = leaves[i];
      if (leaf == null)
	continue;
      if (!leaf.getType().equals(LeafComponentData.FILE)) {
        if(log.isErrorEnabled()) {
          log.error("Got unknown LeafComponent type: " + leaf.getType());
        }
	continue;
      }
      PrintWriter writer = new PrintWriter(new FileWriter(new File(configDir, leaf.getName())));
      try {
	writer.println(leaf.getValue().toString());
      } catch (Exception e) {
        if(log.isErrorEnabled()) {
          log.error("Error writing config file: " + e);
        }
      }
      finally {
	writer.close();
      }
    } // end of loop over leaves
  } // end of writeLeafData  

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }  
}
