/* 
 * <copyright>
 *  
 *  Copyright 2001-2004 BBNT Solutions, LLC
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
package org.cougaar.tools.csmart.core.cdata;

import java.util.ArrayList;

/**
 * <code>AgentComponentData</code> extends <code>GenericComponentData</code>
 * and sets the Component Type to {@link ComponentData#AGENT} and the
 * class name to <code>org.cougaar.core.agent.SimpleAgent</code>.
 * <br><br>
 * This class also provides access to the agents plugins and their arguments.
 *
 * @see GenericComponentData
 */
public class AgentComponentData extends GenericComponentData {

  private static final String defClassName = "org.cougaar.core.agent.SimpleAgent";

  /** Default Constructor **/
  public AgentComponentData() {
    type = ComponentData.AGENT;
    className = defClassName;
  }

  // FIXME: Do we need equivalent methods for Binders
  // and other Misc components?

  /**
   * Returns a list of all plugin names for this agent.
   *
   * @return a <code>String[]</code> array of all plugin names
   */
  public String[] getPluginNames() {
    ArrayList names = new ArrayList();
    ComponentData[] plugins = getChildren();
    
    for(int i=0; i < plugins.length; i++) {
      ComponentData plugin = plugins[i];
      if(plugin.getType().equals(ComponentData.PLUGIN)) {
        names.add(plugin.getName());
      }
    }
    
    return (String[])names.toArray(new String[names.size()]);
  }
  
  /**
   * Returns all arguments for a specific plugin
   *
   * @param name Name of the plugin
   * @return an <code>Object[]</code> array of all plugin arguments
   */
  public Object[] getPluginArgs(String name) {
    ComponentData plugin = findPlugin(name);
    if(plugin != null) {
      return plugin.getParameters();
    }
    return null;
  }

  /**
   * Finds a specific plugin, for this agent, based
   * on a name and returns the <code>ComponentData</code> 
   * for that plugin.
   *
   * @param name Name of the plugin to search for.
   * @return a <code>ComponentData</code> for the specified plugin
   */
  private ComponentData findPlugin(String name) {
    ComponentData[] plugins = getChildren();
    for(int i=0; i < plugins.length; i++) {
      if(plugins[i].getName().equals(name)) {
        return plugins[i];
      }
    }
    return null;
  }

}
