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

package org.cougaar.tools.csmart.ui.experiment;

import java.awt.*;
import java.util.*;
import javax.swing.*;

import org.cougaar.util.log.Logger;
import org.cougaar.core.component.ComponentDescription;

import org.cougaar.tools.csmart.core.cdata.AgentComponentData;
import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.experiment.Experiment;
import org.cougaar.tools.csmart.ui.viewer.CSMART;

/**
 * Window to show the detailed contents of an Agent
 **/
public class AgentInfoPanel extends JPanel {
  private transient Logger log;

  public AgentInfoPanel(Experiment experiment, String agentName) {
    super();
    log = CSMART.createLogger(this.getClass().getName());
    
    // Warning: This next line may save the whole experiment
    ComponentData societyComponentData = experiment.getSocietyComponentData();

    if (societyComponentData == null) {
      JOptionPane.showMessageDialog(this, "No information available",
                                    "No Information", 
                                    JOptionPane.PLAIN_MESSAGE);
      if (log.isDebugEnabled()) {
	log.debug("Experiment returned no component data: " + experiment.getExperimentName());
      }
      return;
    }
    ComponentData agentComponentData = null;
    ComponentData[] children = societyComponentData.getChildren();

    // Yuck this is ugly.....
    if (societyComponentData.getType().equals(ComponentData.NODE) || societyComponentData.getType().equals(ComponentData.AGENT)) {
      if (societyComponentData.getName().equals(agentName)) {
	agentComponentData = societyComponentData;
      }
    }

    if (agentComponentData == null) {

      // Loop to find the Agent we want
      for (int i = 0; i < children.length; i++) {
	if (agentComponentData != null)
	  break;
	if (children[i].getType().equals(ComponentData.NODE)) {
	  // Let it possibly be the Node itself
	  if (agentName.equals(children[i].getName())) {
	    agentComponentData = children[i];
	    break;
	  }
	  
	  ComponentData[] agents = children[i].getChildren();
	  for (int k = 0; k < agents.length; k++) {
	    if (agentComponentData != null)
	      break;
	    if (agents[k].getName().equals(agentName)) {
	      agentComponentData = agents[k];
	      break;
	    }
	  }
	} else if (children[i].getType().equals(ComponentData.AGENT)) {
	  // Let it possibly be the Node itself
	  if (agentName.equals(children[i].getName())) {
	    agentComponentData = children[i];
	    break;
	  }
	} else if (children[i].getType().equals(ComponentData.HOST)) {
	  ComponentData[] nodes = children[i].getChildren();

	  for (int j = 0; j < nodes.length; j++) {    
	    if (agentComponentData != null)
	      break;
	    // Let it possibly be the Node itself
	    if (agentName.equals(nodes[j].getName())) {
	      agentComponentData = nodes[j];
	      break;
	    }
	    
	    ComponentData[] agents = nodes[j].getChildren();
	    for (int k = 0; k < agents.length; k++) {
	      if (agentComponentData != null)
		break;
	      if (agents[k].getName().equals(agentName)) {
		agentComponentData = agents[k];
		break;
	      }
	    }
	  }
	}
      }
    }

    if (agentComponentData == null) {
      JOptionPane.showMessageDialog(this, "No information available",
                                    "No Information", 
                                    JOptionPane.PLAIN_MESSAGE);
      if (log.isDebugEnabled()) {
	log.debug("Got null agentComponentData after search?");
      }
      return;
    }

    ComponentData[] agentChildren = agentComponentData.getChildren();
    ArrayList entries = new ArrayList(agentChildren.length);
    for (int i = 0; i < agentChildren.length; i++) {
      StringBuffer sb = new StringBuffer();
      if (agentChildren[i].getType().equals(ComponentData.AGENTBINDER)) {
	sb.append("Node.AgentManager.Agent.PluginManager.Binder");
      } else if (agentChildren[i].getType().equals(ComponentData.NODEBINDER)) {
	sb.append("Node.AgentManager.Binder");
      } else {
	sb.append(agentChildren[i].getType());
      }
      if(ComponentDescription.parsePriority(agentChildren[i].getPriority()) != 
	 ComponentDescription.PRIORITY_COMPONENT) {
	sb.append("(" + agentChildren[i].getPriority() + ")");
      }
      sb.append(" = ");
      sb.append(agentChildren[i].getClassName());
      if (agentChildren[i].parameterCount() != 0) {
        sb.append("(");
        Object[] params = agentChildren[i].getParameters();
        sb.append(params[0].toString());
        for (int j = 1; j < agentChildren[i].parameterCount(); j++) {
          sb.append(",");
          sb.append(params[j].toString());
        }
        sb.append(")");
      }
      entries.add(sb.toString());
    }
    JList plugInsList = new JList(entries.toArray());
    JScrollPane jsp = new JScrollPane(plugInsList);
    jsp.setPreferredSize(new Dimension(550, 200));
    setLayout(new GridBagLayout());
    plugInsList.setBackground(getBackground());
    int x = 0;
    int y = 0;
    add(new JLabel("SubComponents:"),
        new GridBagConstraints(x++, y, 1, 1, 0.0, 0.0,
                               GridBagConstraints.WEST,
                               GridBagConstraints.NONE,
                               new Insets(10, 0, 5, 5),
                               0, 0));
    add(jsp,
        new GridBagConstraints(x, y++, 1, 1, 0.0, 0.0,
                               GridBagConstraints.WEST,
                               GridBagConstraints.NONE,
                               new Insets(0, 0, 5, 0),
                               0, 0));
    setSize(400, 400);
  }
}
