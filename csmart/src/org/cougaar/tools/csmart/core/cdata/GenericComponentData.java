/* 
 * <copyright>
 *  Copyright 2001-2002 BBNT Solutions, LLC
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

package org.cougaar.tools.csmart.core.cdata;

import java.io.Serializable;
import java.util.ArrayList;

import org.cougaar.tools.csmart.core.property.ConfigurableComponent;
import org.cougaar.util.log.Logger;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.core.component.ComponentDescription;
import java.io.ObjectInputStream;
import java.io.IOException;

/**
 * Generic Component Data.
 * All components, except for Agent components can use
 * the generic component data structure.
 *
 * @see ComponentData for docs.
 */
public class GenericComponentData implements ComponentData, Serializable {
  protected String type = null;
  private String name = null;
  protected String className = null;
  private String priority = 
    ComponentDescription.priorityToString(ComponentDescription.PRIORITY_COMPONENT);
  private ArrayList children = null;
  private ArrayList parameters = null;
  private ComponentData parent = null;
  private transient ConfigurableComponent owner = null;
  private ArrayList leafComponents = null;
  private ArrayList timePhasedData = null;
  private AgentAssetData assetData = null;
  private String aLibID = null;
  private String libID = null;

  private transient Logger log;


  public GenericComponentData() {
    createLogger();
    children = new ArrayList();
    parameters = new ArrayList();
    leafComponents = new ArrayList();
    timePhasedData = new ArrayList();
  }
  
  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  public String getType() {
    return this.type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getClassName() {
    return this.className;
  }

  public void setClassName(String className) {
    this.className = className;
  }

  public String getPriority() {
    return this.priority;
  }

  public void setPriority(String priority) {
    try {
      ComponentDescription.parsePriority(priority);
      this.priority = priority;
    } catch(IllegalArgumentException e) {
      if(log.isErrorEnabled()) {
        log.error("IllegalArgumentException setting component priority", e);
      }
    }
  }

  public void setPriority(int priority) {
    try {
      this.priority = ComponentDescription.priorityToString(priority);
    } catch(IllegalArgumentException e) {
      if(log.isErrorEnabled()) {
        log.error("IllegalArgumentException setting component priority", e);
      }
    }
  }
  
  public ComponentData[] getChildren() {
    return (ComponentData[])children.toArray(new ComponentData[children.size()]);
  }

  public void setChildren(ComponentData[] child) {
    if (child == null || child.length == 0 && log.isDebugEnabled()) {
      log.debug(getName() + ".setChildren called with empty list - will clear children.");
    }

    this.children.clear();
    for(int i=0; i < child.length; i++) {
      if (child[i] != null)
	this.children.add(child[i]);
      else if (log.isDebugEnabled()) {
	log.debug(getName() + ".setChildren got a null child at index " + i);
      }
    }
  }

  public void addChild(ComponentData child) {
    if (child != null)
      this.children.add(child);
    else {
      if (log.isDebugEnabled()) 
	log.debug(getName() + ".addChild got a null child");
      if (log.isErrorEnabled())
	log.error("addChild", new Throwable());
    }
  }

  public void addChild(int index, ComponentData child) {
    if (child != null)
      this.children.add(index, child);
      else {
	if (log.isDebugEnabled())
	  log.debug(getName() + ".addChild got a null child to add at index " + index);
	if (log.isErrorEnabled())
	  log.error("addChild index, child", new Throwable());
      }
  }

  public void setChild(int index, ComponentData child) {
    if (child != null)
      this.children.set(index, child);
    else {
      if (log.isDebugEnabled())
	log.debug(getName() + ".setChild got a null child to put at index " + index);
      if (log.isErrorEnabled())
	log.error("setChild index, child", new Throwable());
    }
  }

  public int childCount() {
    return children.size();
  }

  public int getChildIndex(ComponentData child) {
    return this.children.indexOf(child);
  }

  public void addChildDefaultLoc(ComponentData comp) {
    if (comp == null) {
      if (log.isDebugEnabled()) {
	log.debug(getName() + ".addChildDefaultLoc got null component");
      }
      if (log.isErrorEnabled())
	log.error("addChildDefaultLoc", new Throwable());
      return;
    }

    // Binders will be inserted before other items
    // at the same level.
    // Components that are .equals with other items already
    // there will replace the existing versions
    if(log.isDebugEnabled()) {
      log.debug("Adding comp: " + comp + " to " + this.getName());
    }
    ComponentData[] dkids = this.getChildren();
    int lastbinder = -1; // last binder
    int firstother = -1; // -1 if no kids or first is after
    // last binder
    // else index of first other
    for (int i = 0; i < dkids.length; i++) {
      ComponentData kid = dkids[i];
      if (this.getType().equals(ComponentData.NODE)) {
	if (kid.getType().equals(ComponentData.AGENT)) {
//            if(log.isDebugEnabled()) {
//  	    log.debug("Got an agent at index: " + i);
//            }
	  if (firstother < 0)
	    firstother = i;
	} else if (kid.getType().equals(ComponentData.NODEBINDER)) {
//            if(log.isDebugEnabled()) {
//              log.debug("Got a nodebinder at index: " + i);
//            }
	  lastbinder = i;
	} else {
	  // Plugins, AgentBinders, misc components
	  // FIXME!!!
	}
      } else if (this.getType().equals(ComponentData.AGENT)) {
	if (kid.getType().equals(ComponentData.PLUGIN)) {
//            if(log.isDebugEnabled()) {
//              log.debug("Got a plugin at index: " + i);
//            }
	  if (firstother < 0)
	    firstother = i;	      
	} else if (kid.getType().equals(ComponentData.AGENTBINDER)) {
//            if(log.isDebugEnabled()) {
//              log.debug("Got an agentbinder at index: " + i);
//            }
	  lastbinder = i;
	} else {
	  // Misc components
	  // FIXME!!!
	}
      }
    } // end of loop over kids of this data item
    // Now lastbinder points to the last binder.
    // I use lastbinder+1
    
    // If this is a binder
    if (comp.getType().equals(ComponentData.NODEBINDER) || comp.getType().equals(ComponentData.AGENTBINDER)) {
//        if(log.isDebugEnabled()) {
//          log.debug("Comp being added is a binder");
//        }
      // see if it is in node/agent at all
      if (this.getChildIndex(comp) >= 0) {
	// if it is there
	// see if there are any agents/plugins before it
	if (firstother < this.getChildIndex(comp)) {
	  // if there are, must remove it and later add it
	  // Its easiest to do a complete fix
	  // FIXME: This shuffles all the other binders,
	  // when maybe they're intentionally placed where they are?
	  ArrayList dkidsnew = new ArrayList();
	  for (int i = 0; i < dkids.length; i++) {
	    // First add all the binders
	    if (dkids[i].getType().equals(comp.getType())) {
	      if (i == this.getChildIndex(comp)) {
		dkidsnew.add(comp);
	      } else {
		dkidsnew.add(dkids[i]);
	      }
	      dkids[i] = null;
	    }
	  } // end of loop to add binders
	  for (int i = 0; i < dkids.length; i++) {
	    if (dkids[i] != null)
	      dkidsnew.add(dkids[i]);
	  } // end of loop to add others
	  this.setChildren((ComponentData [])dkidsnew.toArray(new ComponentData[dkidsnew.size()]));
	} else {
//            if(log.isDebugEnabled()) {
//              log.debug(".. replacing old version with new");
//            }
	  // else all agents/plugins are after it. Replace old with new
	  this.setChild(this.getChildIndex(comp), comp);
	}
      } else {
//            if(log.isDebugEnabled()) {
//              log.debug("Adding at index: " + (lastbinder + 1));
//            }
	// else if it is not there at all, insert it after last binder
	this.addChild(lastbinder + 1, comp);
      }
    } else {
//        if(log.isDebugEnabled()) {
//          log.debug("Adding a non binder");
//        }
      // else if its an agent or a plugin
      // see if it is there at all
      if (this.getChildIndex(comp) >= 0) {
	// if it is, do this.setChild(its index>, comp)
	this.setChild(this.getChildIndex(comp), comp);
      } else {
	// else (it is not there), do this.addChild(comp)
	// FIXME: Maybe this should add it after the last item not its type?
	this.addChild(comp);
      }
    } // finished handling non binders
  }
  
  public Object[] getParameters() {
    return parameters.toArray();
  }

  public void setParameters(Object[] params) {
    this.parameters.clear();
    for(int i=0; i < params.length; i++) {
      this.parameters.add(params[i]);
    }    
  }

  public void addParameter(Object param) {
    this.parameters.add(param);
  }

  public void setParameter(int index, Object param) {
    this.parameters.set(index, param);
  }

  public Object getParameter(int index) {
    return parameters.get(index);
  }

  public int parameterCount() {
    return parameters.size();
  }

  public ComponentData getParent() {
    return parent;
  }

  public void setParent(ComponentData parent) {
    this.parent = parent;
  }

  public ConfigurableComponent getOwner() {
    return owner;
  }

  public void setOwner(ConfigurableComponent owner) {
    this.owner = owner;
  }

  public LeafComponentData[] getLeafComponents() {
    return (LeafComponentData[]) leafComponents.toArray(new LeafComponentData[leafComponents.size()]);
  }

  public void setLeafComponents(LeafComponentData[] leaves) {
    leafComponents.clear();
    for(int i = 0 ; i < leaves.length; i++) {
      leafComponents.add(leaves[i]);
    }
  }

  public void addLeafComponent(LeafComponentData leaf) {
    leafComponents.add(leaf);
  }

  public void setLeafComponent(int index, LeafComponentData leaf) {
    leafComponents.set(index, leaf);
  }

  public int leafCount() {
    return leafComponents.size();
  }

  public AgentAssetData getAgentAssetData() {
    return assetData;
  }

  public void addAgentAssetData(AgentAssetData data) {
    this.assetData = data;
  }

  // For testing, dump out the tree from here down
  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append(this.getClass().toString() + ": ");
    buf.append("Name: " + getName());
    buf.append(", Type: " + getType());
    buf.append(", Class: " + getClassName());
    buf.append(", AlibID: " + getAlibID());
    buf.append(", libID: " + getLibID());
    buf.append(", Priority: " + getPriority());
    if (owner != null) {
      buf.append(", Owner: " + getOwner());
    }
    buf.append(", LeafCount: " + leafCount());
    // FIXME print out the arguments
    buf.append(", ChildCount: " + childCount());
    ComponentData[] children = getChildren();
    for (int i = 0; i < childCount(); i++) {
      buf.append(", Child[" + i + "]: \n" + children[i].getName() + "\n");
    }
    return buf.toString();
  }

  public TimePhasedData[] getTimePhasedData() {
    return (TimePhasedData[]) timePhasedData.toArray(new TimePhasedData[timePhasedData.size()]);
  }

  public void setTimePhasedData(TimePhasedData[] data) {
    timePhasedData.clear();
    for(int i = 0 ; i < data.length; i++) {
      timePhasedData.add(data[i]);
    }
  }

  public void setTimePhasedData(int index, TimePhasedData data) {
    timePhasedData.set(index, data);
  }

  public void addTimePhasedData(TimePhasedData data) {
    timePhasedData.add(data);
  }

  public int timePhasedCount() {
    return timePhasedData.size();
  }

  public void setAlibID(String alibID) {
    this.aLibID = alibID;
  }

  public String getAlibID() {
    return aLibID;
  }

  public void setLibID(String libID) {
    this.libID = libID;
  }

  public String getLibID() {
    return libID;
  }

  private void readObject(ObjectInputStream ois)
    throws IOException, ClassNotFoundException
  {
    ois.defaultReadObject();
    createLogger();
  }

  public boolean equals(Object o) {
    if (o instanceof GenericComponentData) {
      GenericComponentData that = (GenericComponentData)o;
      // FIXME: Compare AlibIDs if they're both not null first?
//        if (this.getAlibID() != null && that.getAlibID() != null) {
//  	return (this.getAlibID().equals(that.getAlibID()));
//        } else
      if (this.getName() == null) {
	if (this.getClassName() != null && this.getClassName().equals(that.getClassName()) && this.getType() != null && this.getType().equals(that.getType()) && this.parameterCount() == that.parameterCount() && this.getPriority() != null && this.getPriority().equals(that.getPriority())) {
	  for (int j = 0; j < that.parameterCount(); j++) {
	    if (! that.getParameter(j).equals(this.getParameter(j))) {
	      return false;
	    }
	  }
	  // FIXME: Compare the children?
	  return true;
	}
      } else if (this.getName().equals(that.getName())) {
	return true;
      }
    }
    return false;
  }

  /**
   * Generate the standard unique name for a sub-component (Binder, Plugin). 
   * The rule is that the name should be parent|<classname>. 
   * However, if the ComponentData has a non-null name when passed in,
   * then use that as the base.
   * But if that is taken within this parent, and you have a first parameter, add |<param 0>. 
   * Otherwise, or if that too is taken, add a number: The index at which
   * this component will be added to the parent. 
   * Note that the self component should <i>not</i> be currently a child of the parent - at least,
   * not to the best of the caller's knowledge. 
   * Also note that this method does not <i>set</i> the name, just calculate it.
   *
   * @param parent a <code>ComponentData</code> to which this Component has <i>not yet</i> been added
   * @param self a <code>ComponentData</code> which will be added to the parent and whose name should be calculated
   * @return a <code>String</code> name for the self ComponentData, null if the self ComponentData was null
   */
  public static String getSubComponentUniqueName(ComponentData parent, ComponentData self) {
    // FIXME: This needs to match up with PopulateDb's ideas of Alib IDs and stuff
    if (self == null)
      return null;

    String cname = self.getName();
    if (parent == null) 
      if (cname != null)
	return cname;
      else 
	return self.getClassName();
     
    String type = self.getType();

    // Create a default component name if necessary
    if (cname == null) 
      cname = self.getClassName();
    
    // Either way, ensure the name starts with the parent name
    // for things that arent Nodes or Agents,
    // cause PopulateDb will assume so in creating Alib IDs
    if (type != null && ! type.equals(ComponentData.NODE) && ! type.equals(ComponentData.AGENT) && ! type.equals(ComponentData.HOST) && ! type.equals(ComponentData.SOCIETY) && ! cname.startsWith(parent.getName() + "|"))
      cname = parent.getName() + "|" + cname;
    
    ComponentData[] children = parent.getChildren();
    if (children == null)
      return cname;
    boolean addedparam = false;
    for (int i = 0; i < children.length; i++) {
      ComponentData kid = children[i];
      if (kid != null && kid.getName().equals(cname)) {
	// OK, must at least add a paramter if it has one
	if (self.parameterCount() > 0 && ! addedparam) {
	    cname = cname + "|" + self.getParameter(0);
	    addedparam = true;
	} else {
	  // OK, no params. Add a number? Maybe the others do have a param?
	  cname = cname + children.length;
	}
      }
    }
    return cname;
  }

  /**
   * Look for the given ComponentData in the given parent, 
   * using its class, type, and parameters.
   * Do not consider the name of the child, or any 
   * AssetData or children it might have.
   * That is, if any existing child of the given parent has the same type, 
   * class, and parameter list
   * of the given candidate child, return true. Also return true 
   * if the parent or child is null.
   * Return false if the parent has no children.
   *
   * @param parent <code>ComponentData</code> that may already contain the component
   * @param self a <code>ComponentData</code> a component to look for
   * @return a <code>boolean</code>, true if a component with the same type, 
   *         class, and parameters is already present
   */
  public static boolean alreadyAdded(final ComponentData parent, final ComponentData self) {
    Logger log = CSMART.createLogger(GenericComponentData.class.getName());
    if (self == null || parent == null)
      return true;
    ComponentData[] children = parent.getChildren();
    if (children == null)
      return false;
    if (log.isDebugEnabled() && children.length != parent.childCount()) {
      log.debug(parent + " says childCount is " + parent.childCount() + " but returned array of children was of length " + children.length);
    }
    for (int i = 0; i < children.length; i++) {
      boolean isdiff = false;
      ComponentData kid = children[i];
      if (kid == null) {
	if (log.isErrorEnabled()) {
	  log.error("Please report seeing Bug 1279: Using CSMART " + CSMART.writeDebug() + " Child " + i + " out of " + parent.childCount() + " is null in " + parent.getName() + " while considering adding " + self.getName() + " with class " + self.getClassName(), new Throwable());
	}
	// FIXME: Maybe do a parent.setChildren with a new list that doesn't include
	// the null?
      } else if (kid.getClassName().equals(self.getClassName())) {
	if (kid.getType().equals(self.getType())) {
	  if (kid.parameterCount() == self.parameterCount()) {
	    // Then we better compare the parameters in turn.
	    // As soon as we find one that differs, were OK.
	    for (int j = 0; j < kid.parameterCount(); j++) {
	      if (! kid.getParameter(j).equals(self.getParameter(j))) {
		isdiff = true;
		break;
	      }
	    } // loop over params
	    // If we get here, we finished comparing the parameters
            // Either cause we broke out, and isdiff is true
            // Or we completely compared the child, and it is
            // identical.
            // If we did not mark this child as different,
            // then return true - it is the same
	    if (! isdiff)
	      return true;
	  } // check param count
	} // check comp type
      } // check comp class
    } // loop over children

    // If we get here, we did not find any component that is identical
    return false;
  }

}
