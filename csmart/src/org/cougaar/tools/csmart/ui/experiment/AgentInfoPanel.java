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
import org.cougaar.tools.csmart.core.cdata.AgentComponentData;
import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.experiment.Experiment;
import org.cougaar.tools.csmart.ui.viewer.CSMART;

public class AgentInfoPanel extends JPanel {
  private transient Logger log;

  public AgentInfoPanel(Experiment experiment, String agentName) {
    super();
    log = CSMART.createLogger(this.getClass().getName());
    ComponentData societyComponentData = experiment.getSocietyComponentData();
    if (societyComponentData == null) {
      JOptionPane.showMessageDialog(this, "No information available",
                                    "No Information", 
                                    JOptionPane.PLAIN_MESSAGE);
      return;
    }
    ComponentData agentComponentData = null;
    ComponentData[] children = societyComponentData.getChildren();
    for (int i = 0; i < children.length; i++) {
      if (children[i].getType().equals(ComponentData.HOST)) {
        ComponentData[] nodes = children[i].getChildren();
        for (int j = 0; j < nodes.length; j++) {
          ComponentData[] agents = nodes[j].getChildren();
          for (int k = 0; k < agents.length; k++) {
            if (agents[k] instanceof AgentComponentData &&
                agents[k].getName().equals(agentName)) {
              agentComponentData = agents[k];
              break;
            }
          }
        }
      }
    }
    if (agentComponentData == null)
      return;
    ComponentData[] agentChildren = agentComponentData.getChildren();
    ArrayList entries = new ArrayList(agentChildren.length);
    for (int i = 0; i < agentChildren.length; i++) {
      StringBuffer sb = new StringBuffer();
      sb.append(agentChildren[i].getType());
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
    jsp.setPreferredSize(new Dimension(400, 100));
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
