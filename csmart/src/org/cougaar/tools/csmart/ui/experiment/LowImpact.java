/*
 * <copyright>
 *  Copyright 2000-2001 BBNT Solutions, LLC
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
