/*
 * <copyright>
 *  
 *  Copyright 2000-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
 */
package org.cougaar.tools.csmart.society.cdata;

import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.society.AgentComponent;
import org.cougaar.tools.csmart.society.SocietyBase;

import java.util.ArrayList;

/**
 * The class for creating a configurable component that represents
 * a society from component data.
 */
public class SocietyCDataComponent extends SocietyBase {
  ComponentData cdata;

  protected static final String DESCRIPTION_RESOURCE_NAME = "/org/cougaar/tools/csmart/society/society-base-description.html";
  protected static final String BACKUP_DESCRIPTION =
    "A Society: Agents, Binders, Plugins, etc.";

  public SocietyCDataComponent(ComponentData cdata, String assemblyId) {
    super(cdata.getName());
    this.cdata = cdata;
    this.oldAssemblyId = assemblyId;
  }

  public void initProperties() {
    // create society properties from cdata
    // create agents from cdata
    ArrayList agentData = new ArrayList();
    ArrayList alldata = new ArrayList();
    if (cdata != null)
      alldata.add(cdata);
    
    // FIXME: It'd be nice to deal with binders of Agents in here!!!

    // Find all the agents
    for (int i = 0; i < alldata.size(); i++) {
      ComponentData someData = (ComponentData)alldata.get(i);
//       if (log.isDebugEnabled())
// 	log.debug("initProps: alldata(" + i + ")= " + someData);
      String type = someData.getType();
//       if (log.isDebugEnabled())
// 	log.debug("type = " + type);
      if (type.equals(ComponentData.AGENT)) {
// 	if (log.isDebugEnabled())
// 	  log.debug("adding to list of agents if not already there");

	// Do not add the same Agent to this society twice.
	// This should not be necessary if the incoming
	// CDATA is correctly created - usu from 
	// Experiment.getSocietyComponentData()
	// If we dont do this, ConfigurableComponent.addChild, when
	// we create an AgentCDataComponent for this and
	// add it to this society, will rename the Agent
	// for us quietly
	if (! agentData.contains(someData))
	  agentData.add(someData);

      } else if (type.equals(ComponentData.NODE) || type.equals(ComponentData.HOST) || type.equals(ComponentData.SOCIETY)) {
        ComponentData[] moreData = someData.getChildren();
        for (int j = 0; j < moreData.length; j++) 
          alldata.add(moreData[j]);
      }
    }

    if (log.isDebugEnabled())
      log.debug("initProps found " + agentData.size() + " agents to add");

    // For each agent, create a component and add it as a child
    for (int i = 0; i < agentData.size(); i++) {
//       if (log.isDebugEnabled())
// 	log.debug("initProps had CDATA(" + i + "): " + (ComponentData)agentData.get(i));
      AgentComponent agentComponent = 
        new AgentCDataComponent((ComponentData)agentData.get(i));
      agentComponent.initProperties();
//       if (log.isDebugEnabled())
// 	log.debug("... which after initProps produced AgentCDataComponent " + agentComponent.getFullName().toString());
      addChild(agentComponent);
//       if (log.isDebugEnabled())
// 	log.debug("... and which after addChild is " + agentComponent.getFullName().toString());
    }
  }

}
