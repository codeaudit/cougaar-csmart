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
import org.cougaar.tools.csmart.core.property.BaseComponent;
import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.tools.csmart.society.AgentBase;
import org.cougaar.tools.csmart.society.AgentComponent;
import org.cougaar.tools.csmart.society.BinderBase;
import org.cougaar.tools.csmart.society.ComponentBase;
import org.cougaar.tools.csmart.society.ContainerBase;
import org.cougaar.tools.csmart.society.PluginBase;

/**
 * Create a ConfigurableComponent which represents an Agent
 * from ComponentData.
 */
public class AgentCDataComponent
    extends AgentBase
    implements AgentComponent {

  ComponentData cdata;

  /**
   * Creates a new <code>AgentCDataComponent</code> instance.
   *
   * @param cdata <code>ComponentData</code> to use as base.
   */
  public AgentCDataComponent(ComponentData cdata) {
    super(cdata.getName());
    this.cdata = cdata;
  }

  /**
   * Initializes all <code>Property</code> values for this class.
   *
   */
  public void initProperties() {
    super.initProperties();
    Property p = addProperty(PROP_CLASSNAME, new String(cdata.getClassName()));
    p.setToolTip(PROP_CLASSNAME_DESC);

    addComponent(ComponentData.AGENTBINDER, "Binders");
    addComponent(ComponentData.PLUGIN, "Plugins");
    addComponents();
    addAssetData();
  }

  protected void addComponent(String typeid, String containerName) {
    ContainerBase container = new ContainerBase(containerName);
    container.initProperties();
    addChild(container);

    ComponentData[] childCData = cdata.getChildren();
    ComponentBase component = null;
    for (int i = 0; i < childCData.length; i++) {
      if (childCData[i].getType().equals(typeid)) {

        if (typeid.equals(ComponentData.PLUGIN)) {
          component = new PluginBase(childCData[i].getName(),
                                     childCData[i].getClassName(), childCData[i].getPriority());
        } else if (typeid.equals(ComponentData.AGENTBINDER)) {
          component = new BinderBase(childCData[i].getName(),
                                     childCData[i].getClassName(), childCData[i].getPriority());
        }
        component.initProperties();
        component.setComponentType(childCData[i].getType());

        Object[] parameters = childCData[i].getParameters();
        for (int j = 0; j < parameters.length; j++)
            //          component.addProperty(ComponentBase.PROP_PARAM + j, parameters[j]);
          component.addParameter(parameters[j]);
        container.addChild(component);
      }
    }
  }

  // FIXME: Add misc components -- how do I find the type though?
  protected void addComponents() {
    // add plugins
    ContainerBase container = new ContainerBase("Other Components");
    container.initProperties();
    addChild(container);

    ComponentData[] childCData = cdata.getChildren();
    for (int i = 0; i < childCData.length; i++) {
      if (childCData[i].getType().equals(ComponentData.PLUGIN) ||
          childCData[i].getType().equals(ComponentData.AGENTBINDER) ||
          !childCData[i].getParent().getType().equals(ComponentData.AGENT)) {
// not added here
      } else {
        ComponentBase plugin = new ComponentBase(childCData[i].getName(),
                                                 childCData[i].getClassName(), childCData[i].getPriority(), childCData[i].getType());
        plugin.initProperties();
//	plugin.setComponentType(childCData[i].getType());
        Object[] parameters = childCData[i].getParameters();
        for (int j = 0; j < parameters.length; j++)
            //          plugin.addProperty(ComponentBase.PROP_PARAM + j, parameters[j]);
          plugin.addParameter(parameters[j]);
        container.addChild(plugin);
      }
    }

  }


  protected void addAssetData() {
    // add asset data components
    if (cdata.getAgentAssetData() != null) {
      BaseComponent asset =
          (BaseComponent) new AssetCDataComponent(cdata.getAgentAssetData());
      asset.initProperties();
      addChild(asset);
    }
  }
} // End of AgentCDataComponent
