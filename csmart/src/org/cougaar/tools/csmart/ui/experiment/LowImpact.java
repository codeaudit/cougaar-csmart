/*
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 *
 * © Copyright 2000, 2001 BBNT Solutions LLC
 * </copyright>
 */

package org.cougaar.tools.csmart.ui.experiment;

import org.cougaar.tools.csmart.ui.viewer.Organizer;
import org.cougaar.tools.csmart.ui.component.AgentComponent;
import org.cougaar.tools.server.ConfigurationWriter;
import org.cougaar.tools.csmart.ui.component.NodeComponent;

public class LowImpact implements Impact {
    private String name;

    public LowImpact(String name) {
        this.name = name;
    }
    public void setName(String newName) {
        name = newName;
    }
    public String getName() {
        return name;
    }
    public String toString() {
        return name;
    }
    public Impact copy(Organizer organizer, Object context) {
      //      return organizer.copyImpact(new LowImpact(organizer.generateImpactName(name)), context);
      return new LowImpact(organizer.generateImpactName(name));
    }

  public AgentComponent[] getAgents() {
    return null;
  }

  public ConfigurationWriter getConfigurationWriter(NodeComponent[] nodes) {
    return null;
  }

  /**
   * This is the opportunity for an impact to specify additional
   * components to load into non-Impact Agents
   *
   * @return a <code>String</code> Node file addition, possibly null
   */
  public String getNodeFileAddition() {
    return null;
  }
}
