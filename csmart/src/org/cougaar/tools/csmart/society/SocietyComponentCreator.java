/**
 * <copyright>
 *  
 *  Copyright 2002-2004 BBNT Solutions, LLC
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
package org.cougaar.tools.csmart.society;

import org.cougaar.core.agent.SimpleAgent;
import org.cougaar.tools.csmart.core.cdata.AgentAssetData;
import org.cougaar.tools.csmart.core.cdata.AgentComponentData;
import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.cdata.GenericComponentData;
import org.cougaar.tools.csmart.core.property.BaseComponent;
import org.cougaar.tools.csmart.core.property.ConfigurableComponent;
import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.util.log.Logger;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Iterator;

/**
 * Get a ComponentData tree for a Society, plus helper methods.
 *
 * Created: Thu Mar 28 11:35:40 2002
 *
 */
public class SocietyComponentCreator {

  public static final String PROP_PREFIX = "PROP$";

  public SocietyComponentCreator (){
  }

  /**
   * Given a SocietyComponent, this method will
   * return a complete ComponentData tree.
   *
   * @param society
   * @return a <code>ComponentData</code> value
   */
  public static final ComponentData getComponentData(SocietyComponent society) {
    //    Logger log = CSMART.createLogger("org.cougaar.tools.csmart.society.SocietyComponentCreator");

    ComponentData cData = new GenericComponentData();

    cData.setType(ComponentData.SOCIETY);
    cData.setClassName("java.lang.Object");
    cData.setName(society.getSocietyName());
    cData.setOwner(null);
    cData.setParent(null);

    AgentComponent[] agents = society.getAgents();
    for (int i = 0; i < agents.length; i++) {
      generateAgentComponentData(agents[i], cData, null);
    }

    // Now let all components add their data.
    society.addComponentData(cData);

    society.modifyComponentData(cData);

    return cData;
  }


  private static final void generateAgentComponentData(AgentComponent agent,
                             ComponentData parent,
                             ConfigurableComponent owner) {

    AgentComponentData ac = new AgentComponentData();
    ac.setName(agent.getShortName());
    ac.setClassName(SimpleAgent.class.getName());
    ac.addParameter(agent.getShortName()); // Agents have one parameter, the agent name
    ac.setOwner(owner);
    ac.setParent(parent);
    parent.addChild((ComponentData)ac);
  }

  private static final void addContainer(ComponentData cd, BaseComponent cp, String name) {
    //    Logger log = CSMART.createLogger("org.cougaar.tools.csmart.society.SocietyComponentCreator");

    Iterator iter = ((Collection)cp.getDescendentsOfClass(ContainerBase.class)).iterator();
    while(iter.hasNext()) {
      ContainerBase container = (ContainerBase)iter.next();
      if(container.getShortName().equals(name)) {
        for(int i=0; i < container.getChildCount(); i++) {
          BaseComponent base = (BaseComponent) container.getChild(i);
          base.addComponentData(cd);
        }
      }
    }
  }

  private static final void addPropertiesAsParameters(ComponentData cd,
                                                      BaseComponent cp)
  {
    Logger log = CSMART.createLogger("org.cougaar.tools.csmart.society.SocietyComponentCreator");
    for (Iterator it = cp.getProperties(); it.hasNext(); ) {
      Property prop = (Property) it.next();
      if (prop != null) {
        Object pvalue = prop.getValue();
        if (pvalue instanceof String)
          if(log.isWarnEnabled()) {
            log.warn("Adding Parameter.");
          }
          cd.addParameter(PROP_PREFIX + prop.getName().last() + "=" + pvalue);
      }
    }
  }

  public static final void dumpComponentData(ComponentData data, PrintStream stream, int sp) {
    ComponentData[] children = data.getChildren();
    for(int i=0; i < children.length; i++) {
      ComponentData child = children[i];
      dumpChild(child, stream, sp);
      if(child.childCount() > 0) {
        dumpComponentData(child, stream, sp+2);
      }
    }

  }

  private static final void dumpChild(ComponentData child,
                                      PrintStream stream,
                                      int sp) {
    stream.println(repeat(sp, " ") + "Name: " + child.getName());
    stream.println(repeat(sp, " ") + "Type: " + child.getType());
    stream.println(repeat(sp, " ") + "Classname: " + child.getClassName());

    if(child instanceof AgentComponentData) {
      AgentAssetData aad = child.getAgentAssetData();

      if(aad != null) {
        stream.println(repeat(sp+2, " ") + "UIC: "+ aad.getUIC());
        stream.println(repeat(sp+2, " ") + "UniqueName: " + aad.getUniqueID());
        stream.println(repeat(sp+2, " ") + "Unit Name: " + aad.getUnitName());
      }
    }

  }

  private static final String repeat(int count, String str) {
    StringBuffer buf = new StringBuffer(count);
    for(int i=0; i < count; i++) { buf.append(str); }
    return buf.toString();
  }

}// SocietyComponentCreator
