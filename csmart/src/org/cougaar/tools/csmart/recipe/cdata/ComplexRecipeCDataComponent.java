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
package org.cougaar.tools.csmart.recipe.cdata;

import java.util.ArrayList;
import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.recipe.ComplexRecipeBase;
import org.cougaar.tools.csmart.society.AgentComponent;
import org.cougaar.tools.csmart.society.cdata.AgentCDataComponent;



/**
 * ComplexRecipeCDataComponent.java
 *
 * Creates a Recipe Component from a <code>ComponentData</code> object.
 *
 * Created: Tue Jun 25 14:14:45 2002
 *
 * @author <a href="mailto:bkrisler@bbn.com">Brian Krisler</a>
 * @version 1.0
 */

public class ComplexRecipeCDataComponent extends ComplexRecipeBase {
  ComponentData cdata;

  public ComplexRecipeCDataComponent (ComponentData cdata, String assemblyId) {
    super(cdata.getName());
    this.cdata = cdata;
    this.oldAssemblyId = assemblyId;
  }

  /**
   * Initialize the recipe component from a ComponentData Object.
   *
   */
  public void initProperties() {
    super.initProperties();

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
      if (someData.getType().equals(ComponentData.AGENT)) {
        agentData.add(someData);
      } else {
        ComponentData[] moreData = someData.getChildren();
        for (int j = 0; j < moreData.length; j++) 
          alldata.add(moreData[j]);
      }
    }

    // For each agent, create a component and add it as a child
    for (int i = 0; i < agentData.size(); i++) {
      AgentComponent agentComponent = 
        new AgentCDataComponent((ComponentData)agentData.get(i));
      agentComponent.initProperties();
      addChild(agentComponent);
    }
  }
  
}// ComplexRecipeCDataComponent
