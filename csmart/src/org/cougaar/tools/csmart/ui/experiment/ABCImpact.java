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
package org.cougaar.tools.csmart.ui.experiment;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JFileChooser;

import org.cougaar.tools.server.ConfigurationWriter;
import org.cougaar.util.EmptyIterator;

import org.cougaar.tools.csmart.ui.component.*;
import org.cougaar.tools.csmart.ui.viewer.Organizer;
import org.cougaar.tools.csmart.configgen.abcsociety.ABCAgent;

/**
 * Impact for ABC XML specified RealWorldEvents. 
 * Includes static method for getting an XML file.<br>
 * <br>
 * getAgents will then return a Generator and Transducer Agent.
 * And the ConfigurationWriter will write them out, and also
 * be able to modify the Node.ini files as necessary.
 * 
 * @see org.cougaar.tools.csmart.ldm.event.RealWorldEvent
 * @see org.cougaar.tools.csmart.plugin.ScriptedEventPlugIn
 */
public class ABCImpact 
  extends ModifiableConfigurableComponent
  implements PropertiesListener, Serializable, ImpactComponent {
  private String name;
  private transient File xmlfile;
  private String fileContents;

  private static final String DESCRIPTION_RESOURCE_NAME = "description.html";
  private static final String BACKUP_DESCRIPTION = 
    "Impact description not available";

  private static final String rweFileName = "RealWorldEvents.xml";
  private static final String tAgentName = "Transducer";
  private static final String gAgentName = "Generator";
  private static final String socFileName = "Society.dat";
  private ABCImpactAgentComponent[] agents;
  private boolean editable = true;

  /**
   * Filter to look for XML files: Return true for such files,
   * or Directories.
   */
  private static final javax.swing.filechooser.FileFilter xmlFilter = new javax.swing.filechooser.FileFilter() {
    public String getDescription() {
      return "XML Files";
    }
    public boolean accept(File f) {
      if (f == null || ! f.canRead())
	return false;
      if (f.isDirectory())
	return true;
      return f.getName().endsWith(".xml");
    }
    };

  /**
   * Search the given directory (or <code>org.cougaar.install.path</code>)
   * for readable XML Files. Return the users selection, or null.
   *
   * @param path a <code>String</code> directory to search, possibly <code>null</code>
   * @param parent a <code>Component</code> window for this window
   * @return a <code>File</code> to read, possibly null
   */
  public static File getImpactFile(String path, Component parent) {
    if (parent == null)
      return null;
    if (path == null)
      path = System.getProperty("org.cougaar.install.path");
    JFileChooser fileChooser = 
      new JFileChooser(path);
    fileChooser.setFileFilter(ABCImpact.xmlFilter);
    int result = fileChooser.showOpenDialog(parent);
    if (result != JFileChooser.APPROVE_OPTION) 
      return null;
    if (! fileChooser.getSelectedFile().isFile() || ! fileChooser.getSelectedFile().canRead())
      return null;
    return fileChooser.getSelectedFile();
  }
  
  public ABCImpact() {
    this("ABC Impact");
  }

  public ABCImpact (String name) {
    super(name);
    this.name = name;
    createGeneratorAgent();
    createTransducerAgent();
  }

  public void initProperties() {
    // Currently no properties to init.
  }

  public void setName(String newName) {
    this.name = newName;
  }

  public String getImpactName() {
    return name;
  }

  public String toString() {
    //    return name + " from file: " + xmlfile;
    return name;
  }
  
  public void setFile(File newFile) {
    this.xmlfile = newFile;
    readXMLFile();
    //    writeXMLFile(null);
  }

  public File getFile() {
    return xmlfile;
  }
  
  public ImpactComponent copy(Organizer organizer, Object context) {
     ImpactComponent newImpact = new ABCImpact(organizer.generateImpactName(name));
     if (xmlfile != null)
       ((ABCImpact)newImpact).setFile(new File(xmlfile.getPath()));
     return newImpact;
  }
  
  /**
   * Get the agents, both assigned and unassigned.
   * @return array of agent components
   */
  public AgentComponent[] getAgents() {
    // FIXME!!! Can I do this this late???
    if (agents == null) {
      agents = new ABCImpactAgentComponent[2];
      agents[0] = createGeneratorAgent();
      agents[1] = createTransducerAgent();
    }
    return agents;
  }

  class ABCImpactAgentComponent extends ConfigurableComponent implements AgentComponent, Serializable {
    private boolean isGenerator = false;

    ABCImpactAgentComponent(boolean generator) {
      super(null);
      isGenerator = generator;
    }

    public CompositeName getName() {
      return new ConcatenatedName(new SimpleName(name), (isGenerator ? "Generator" : "Transducer"));
    }
    
    public void initProperties() {
      // what do I do here? FIXMEEE!!!!
    }

    public Property getProperty(CompositeName name) {
      // FIXMEE!!!
      return null;
    }

    public Iterator getPropertyNames() {
      // FIXME!!!
      return EmptyIterator.iterator();
    }

    public List getPropertyNamesList() {
      return new ArrayList();
      // FIXME!!!
    }

    public void addPropertiesListener(PropertiesListener listener) {
      // FIXME!!!
    }

    public void removePropertiesListener(PropertiesListener listener) {
      // FIXME!!!!
    }
    
    public void setGenerator() {
      this.isGenerator = true;
    }
    
    public void setTransducer() {
      this.isGenerator = false;
    }
    
    public boolean isGenerator() {
      return isGenerator;
    }
    
    public boolean isTransducer() {
      return ! isGenerator;
    }
    
    public String getConfigLine() {
      return "cluster = " + getName().toString();
    }
    
    public void writeIniFile(File configDir) throws IOException {
      if (isGenerator())
	writeGeneratorAgent(configDir);
      else
	writeTransducerAgent(configDir);
      //System.err.println("Done with writeIniFile in my Agentcomponent");
    }

    public String toString() {
      return name + "." + (isGenerator ? "Generator" : "Transducer");
    }
  }
  
  /**
   * Get a configuration writer for this Impact.
   * Warning: This Impact assumes that it has been given
   * All of the Nodes in the Society
   */
  public ConfigurationWriter getConfigurationWriter(NodeComponent[] nodes) {
    return new ABCImpactCWriter(nodes);
  }

  class ABCImpactCWriter implements ConfigurationWriter {
    AgentComponent[] allagents;
    
    public ABCImpactCWriter(NodeComponent[] nodes) {
      ArrayList tmp = new ArrayList();
      for (int i = 0; i < nodes.length; i++) {
	NodeComponent node = nodes[i];
	AgentComponent[] ags = node.getAgents();
	for (int j = 0; j < ags.length; j++) {
	  tmp.add(ags[j]);
	}
      }
      this.allagents = (AgentComponent[])tmp.toArray(new AgentComponent[tmp.size()]);
    }

    /**
     * Just writes out the XML File and the Society File.
     * The Agent files get written out by the societies themselves,
     * as things are currently written.<br>
     * WARNING: This assumes it has gotten all of the Nodes
     * in the Society!
     *
     * @param configDir a <code>File</code> path
     * @exception IOException if an error occurs
     */
    public void writeConfigFiles(File configDir) throws IOException {
      writeXMLFile(configDir);
      writeSocietyFile(configDir, allagents);
	  // Already done by the Society as a whole
	  //mycomp.writeIniFile(configDir);
    }
  }
  
  /**
   * This is the opportunity for an impact to specify additional
   * components to load into non-Impact Agents
   *
   * @return a <code>String</code> Node file addition, possibly null
   */
  public String getNodeFileAddition() {
    // FIXME!!!
    return "#Node.AgentManager = org.cougaar.tools.csmart.binder.SlowMessageTransportServiceFilter(2,10,10)\n";
  }

  /**
   * Write out the saved file contents string into a file in the given
   * directory, with the constant impacts file name from above.
   *
   * @param configDir a <code>File</code> directory
   */
  private void writeXMLFile(File configDir) {
    //    System.err.println("in writeXMLFile");
    writeFile(configDir, fileContents, rweFileName);
  }

  private void writeFile(File configDir, String contents, String fname) {
     PrintWriter writer = null;
    if (configDir == null)
      configDir = new File(System.getProperty("org.cougaar.install.path"));
    try {
      writer = new PrintWriter(new FileWriter(new File(configDir, fname)));
      writer.print(contents);
    } catch (IOException e) {
    } finally {
      try {
	writer.close();
      } catch (NullPointerException e) {
      }
    }
  }

  private void writeGeneratorAgent(File configDir) {
    String gAgNReal = agents[0].getName().toString();
    String tAgNReal = agents[1].getName().toString();
    String gAgFileContents = "# $id$\n[ Cluster ]\nclass = org.cougaar.core.cluster.ClusterImpl\n" +
      "uic = " + gAgNReal + "\ncloned = false\n\n" + 
      "[ PlugIns ]\nplugin = org.cougaar.tools.csmart.plugin.ScriptedEventPlugIn(" + rweFileName + "," + tAgNReal + ")\nplugin = org.cougaar.lib.planserver.PlanServerPlugIn\n\n[ Policies ]\n\n[ Permission ]\n\n[ AuthorizedOperation ]\n";
    String gAgFileName = gAgNReal + ".ini";
    writeFile(configDir, gAgFileContents, gAgFileName);
    //System.err.println("Finishing writeGeneratorAgent");
  }

  private ABCImpactAgentComponent createGeneratorAgent() {
    // FIXME!!!
    return new ABCImpactAgentComponent(true);
  }

  private void writeTransducerAgent(File configDir) {
    String tAgNReal = agents[1].getName().toString();
    String tAgFileContents = "# $id$\n" + 
      "[ Cluster ]\n" + 
"class = org.cougaar.core.cluster.ClusterImpl\n" + 
"uic = " + tAgNReal + "\ncloned = false\n\n[ PlugIns ]\nplugin = org.cougaar.tools.csmart.plugin.TransducerPlugIn(" + socFileName + ")\nplugin = org.cougaar.lib.planserver.PlanServerPlugIn\n\n[ Policies ]\n\n[ Permission ]\n\n[ AuthorizedOperation ]\n";
    String tAgFileName = tAgNReal + ".ini";
    writeFile(configDir, tAgFileContents, tAgFileName);
  }

  private ABCImpactAgentComponent createTransducerAgent() {
    // FIXME!!
    return new ABCImpactAgentComponent(false);
  }

  private void writeSocietyFile(File configDir, AgentComponent[] agents) {
    String socContents = null;
    for (int i = 0; i < agents.length; i++) {
      // For each agent, get its name. If it has properties named lat & long, get
      // those as well. Create a string for each agent and cat that on
      AgentComponent curr = agents[i];
      if (curr == null || curr instanceof ABCImpactAgentComponent) {
	continue;
      }
      // First, get the name of the Agent.
      // get the property ABCAgent.PROP_NAME?
      // cast it to a ConfigurableComponent?
      String thisAg = curr.getFullName().toString();
      // Now get the lat & long.
      // Hard code to zeros for now.
      thisAg = thisAg + ",0.0,0.0";
      
      // get out the props
      // FIXME!!!!
      // FIXME!!!!!!!!!!!!!!
      if (thisAg != null)
	socContents = (socContents == null ? thisAg : socContents + '\n' + thisAg);
    }
    if (socContents == null)
      socContents = "";
    writeFile(configDir, socContents, socFileName);
  }

  /**
   * Suck in the contents of the File into a String.<br>
   * Eventually, this might actually parse the file.
   * In that case, the writer would similary be intelligent.
   * And the AgentComponents would have actual properties.
   *
   * @return a <code>boolean</code> whether we succeeded
   */
  private boolean readXMLFile() {
    boolean result = true;
    InputStream in = null;
    
    try {
      // open our configuration stream[i]
      in = new FileInputStream(xmlfile);
      
      BufferedReader bufIn =
	new BufferedReader(
			   new InputStreamReader(in));
      while (bufIn.ready()) {
	String line = bufIn.readLine();
	if (line == null) {
	  // end of file
	  break;
	}
	fileContents = (fileContents == null ? line : fileContents + '\n' + line);
      }
    } catch (IOException e) {
      result = false;
      e.printStackTrace(System.err);
    } finally {
      try {
	in.close();
      } catch (IOException e) {
      } catch (NullPointerException e) {
      }
    }
    return result;
  }

  public URL getDescription() {
    return getClass().getResource(DESCRIPTION_RESOURCE_NAME);
  }

  public boolean isEditable() {
    return this.editable;
  }

  public void setEditable(boolean editable) {
    this.editable = editable;
  }

  public void addModificationListener(ModificationListener l) {
    getEventListenerList().add(ModificationListener.class, l);
  }

  public void removeModificationListener(ModificationListener l) {
    getEventListenerList().remove(ModificationListener.class, l);
  }

  public void fireModification() {
    fireModification(new ModificationEvent(this, ModificationEvent.SOMETHING_CHANGED));
  }

  /**
   * Called when a new property has been added to the
   * society. 
   *
   * @param PropertyEvent Event for the new property
   */
  public void propertyAdded(PropertyEvent e) {
    Property addedProperty = e.getProperty();
    Property myProperty = getProperty(addedProperty.getName().last().toString());
    if (myProperty != null) {
      setPropertyVisible(addedProperty, true);
    }
  }

  /**
   * Called when a property has been removed from the society
   */
  public void propertyRemoved(PropertyEvent e) {}

}// ABCImpact
