/*
 * <copyright>
 *  Copyright 2000-2002 BBNT Solutions, LLC
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
package org.cougaar.tools.csmart.society.file;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import org.cougaar.core.component.ComponentDescription;
import org.cougaar.tools.csmart.core.cdata.ComponentConnector;
import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.property.ModifiableComponent;
import org.cougaar.tools.csmart.society.AgentComponent;
import org.cougaar.tools.csmart.society.SocietyBase;
import org.cougaar.tools.csmart.society.file.AgentFileComponent;

public class SocietyFileComponent
  extends SocietyBase {

  private String name;
  private ComponentDescription[] desc;
  private String[] filenames = null;
  private String singleFilename = null;
  private boolean filesParsed = false;

  /**
   * Construct a society from a single file which
   * enumerates the agents in the society.
   * The names of files that define individual agents are assumed
   * to be of the form: <agentname>.ini
   * @param filename the name of the file that enumerates the agents
   */
  public SocietyFileComponent(String filename) {
    super(filename);
    this.name = filename;
    this.singleFilename = filename;
  }

  /**
   * Creates a new <code>SocietyFileComponent</code> from the given 
   * Node file, with the given (different) name.
   *
   * @param filename a <code>String</code> name of a Node file on the ConfigPath
   * @param socname a <code>String</code> name for the new society
   */
  public SocietyFileComponent(String filename, String socname) {
    super(socname);
    this.name = socname;

    // Must store the filename for use in initproperties
    this.singleFilename = filename;
  }

  /**
   * Construct a <code>SocietyFileComponent</code> from files, 
   * each of which defines an agent in the society.
   * @param name Name of the society
   * @param filenames Names of the files that define the agents
   */
  public SocietyFileComponent(String name, String[] filenames) {
    super(name);
    this.filenames = filenames;
  }

  /**
   * Initialize all local properties
   *
   */
  public void initProperties() {
    if(!filesParsed) {
      filesParsed = true;
      initFromFiles();
    }
  }

  private void initFromFiles() {
    if (filenames == null)
      initFromSingleFile();
    else
      initFromMultiFiles();
  }

  private void initFromSingleFile() {
    String filename = singleFilename;
    if(log.isDebugEnabled()) {
      log.debug("Parse File: " + filename);
    }

    // FIXME: Must handle getting non-Agents in the file

    desc = ComponentConnector.parseFile(filename);
    if (desc == null)
      return;
    for(int i=0; i < desc.length; i++) {
      String agentName = ComponentConnector.getAgentName(desc[i]);
      if(log.isDebugEnabled()) {
        log.debug("Name: " + agentName + " Class: " + desc[i].getClassname());
      }
      // construct agent filename as:
      // path of singleFilename + agentName + ".ini"
      String agentFilename = filename;
      int index = agentFilename.lastIndexOf(File.separatorChar);
      if (index != -1)
        agentFilename = agentFilename.substring(0, index+1);
      agentFilename = agentFilename + agentName + ".ini";
      AgentComponent agent = 
        (AgentComponent)new AgentFileComponent(agentName, agentFilename,
                                               desc[i].getClassname());
      agent.initProperties();
      addChild(agent);
    }
  }

  private void initFromMultiFiles() {
    for (int i = 0; i < filenames.length; i++) {
      if(log.isDebugEnabled()) {
        log.debug("Parse File: " + filenames[i]);
      }
      // derive agent name from filename
      String agentName = filenames[i];
      if (agentName.endsWith(".ini"))
        agentName = agentName.substring(0, agentName.length()-4);
      int index = agentName.lastIndexOf(File.separatorChar);
      if (index != -1)
        agentName = agentName.substring(index+1);

      // FIXME: I'd like to handle non binders of Agents, etc

      AgentComponent agent = 
        (AgentComponent)new AgentFileComponent(agentName,
                                               filenames[i],
                                               "org.cougaar.core.agent.ClusterImpl");
      agent.initProperties();
      addChild(agent);
    }
  }

//   /**
//    * Copies this component.
//    * @param name Name to use in the copy
//    * @return a <code>ModifiableComponent</code> which is a copy of this object
//    */
//   public ModifiableComponent copy(String name) {
//     if (log.isDebugEnabled()) {
//       log.debug("Copying society " + this.getSocietyName() + " with assembly " + getAssemblyId() + " into new name " + name);
//     }
//     ModifiableComponent societyCopy; 

//     // FIXME: I'd like to just call the parent, But I'm not
//     // sure it would work. When the super calls initProperties
//     // it will try to re-parse from files
//     // The only way to stop that would be between the constructor
//     // and the initProperties, to set that flag
//     // But even then, the children agents wouldnt exist to
//     // be copied...
//     //societyCopy = super.copy(name);
//     if (filenames != null)
//       societyCopy = new SocietyFileComponent(name, filenames);
//     else 
//       societyCopy = new SocietyFileComponent(singleFilename, name);

//     societyCopy.initProperties();

//     // If I re-init from files, it is not modified, per se.
//     // But we're putting it under a new assembly ID, and not saving it
//     // so to that extent, it is modified.
//     // Otherwise, set to the old value (= this.modified)
//     ((SocietyBase)societyCopy).modified = true;

//     // copy the assembly ID - the one under which this societies'
//     // data is currently in the DB, but must be copied
//     ((SocietyBase)societyCopy).oldAssemblyId = getAssemblyId();

//     return societyCopy;
//   }

}
