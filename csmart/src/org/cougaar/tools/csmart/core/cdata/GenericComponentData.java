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

import org.cougaar.core.component.ComponentDescription;
import org.cougaar.tools.csmart.core.property.ConfigurableComponent;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.util.log.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;

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

  // Flag: Recipe modified the component, so must compare it with DB on save
  private boolean modified = false;

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
    if (type == null || type.trim().equals("")) {
      if (log.isDebugEnabled())
	log.debug("setType got empty type in " + this, new Throwable());
      return;
    }
    if (type.equals(this.type))
      return;
    this.type = type;
    //    log.debug(this + " fireModified due to changed type");
    fireModified();
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    if (name == null && this.name == null)
      return;
    if (name != null && name.equals(this.name))
      return;
    this.name = name;
    //    log.debug(this + " fireModified due to changed name");
    fireModified();
  }

  public String getClassName() {
    return this.className;
  }

  public void setClassName(String className) {
    if (className == null && this.className == null)
      return;
    if (className != null && className.equals(this.className))
      return;
    this.className = className;
    //    log.debug(this + " fireModified due to changed classname");
    fireModified();
  }

  public String getPriority() {
    return this.priority;
  }

  public void setPriority(String priority) {
    try {
      ComponentDescription.parsePriority(priority);
      if (this.priority.equals(priority))
	return;
      this.priority = priority;
      //      log.debug(this + " fireModified due to changed priority");
      fireModified();
    } catch(IllegalArgumentException e) {
      if(log.isErrorEnabled()) {
        log.error("IllegalArgumentException setting component priority", e);
      }
    }
  }

  public void setPriority(int priority) {
    try {
      String old = this.priority;
      this.priority = ComponentDescription.priorityToString(priority);
      if (old.equals(this.priority))
	return;
      //      log.debug(this + " fireModified due to changed priority");
      fireModified();
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
      if (child[i] != null) {
	this.children.add(child[i]);
	//	log.debug(this + " fireModified due to setChildren");
	if (! child[i].isModified()) {
	  if (log.isDebugEnabled())
	    log.debug("where the child being added IS NOT MODIFIED!");
	  child[i].fireModified();
	} else {
	  // If the child is already modified then it will not propagate
	  // up, so call locally to be safe
	  if (log.isDebugEnabled()) {
	    if (! isModified())
	      log.debug("setChildren where child was modified but parent(this) not!");
	    else 
	      log.debug("setChildren where both child & parent modified");
	  }
	  fireModified();
	}
	// or should I do child.fireModified()?
      } else if (log.isDebugEnabled()) {
	log.debug(getName() + ".setChildren got a null child at index " + i);
      }
    }
  }

  public void addChild(ComponentData child) {
    if (child != null) {
      this.children.add(child);
      //      log.debug(this + " fireModified due to addChild");
      if (! child.isModified()) {
	  if (log.isDebugEnabled())
	    log.debug("addChild where the child being added IS NOT MODIFIED!");
	child.fireModified();
      } else {
	  if (log.isDebugEnabled()) {
	    if (! isModified())
	      log.debug("addChild where child was modified but parent(this) not!");
	    else 
	      log.debug("addChild where child was modified and so was parent.");
	  }
	fireModified();
      }
      // or should I do child.fireModified()?
    } else {
      if (log.isDebugEnabled()) 
	log.debug(getName() + ".addChild got a null child");
      if (log.isErrorEnabled())
	log.error("addChild", new Throwable());
    }
  }

  public void addChild(int index, ComponentData child) {
    if (child != null) {
      this.children.add(index, child);
      //      log.debug(this + " fireModified due to addChild");
      if (! child.isModified()) {
	if (log.isDebugEnabled())
	  log.debug("addChild where the child being added IS NOT MODIFIED!");
	child.fireModified();
      } else {
	if (log.isDebugEnabled()) {
	  if (! isModified())
	    log.debug("addChild where child was modified but parent(this) not!");
	  else 
	    log.debug("addChild where child was modified and so was parent.");
	}
	  
	fireModified();
      }
      // or should I do child.fireModified()?
    } else {
      if (log.isDebugEnabled())
	log.debug(getName() + ".addChild got a null child to add at index " + index);
      if (log.isErrorEnabled())
	log.error("addChild index, child", new Throwable());
    }
  }

  public void setChild(int index, ComponentData child) {
    if (child != null) {
      this.children.set(index, child);
      //      log.debug(this + " fireModified due to setChild");
      if (! child.isModified()) {
	if (log.isDebugEnabled())
	  log.debug("addChild where the child being added IS NOT MODIFIED!");
	child.fireModified();
      } else {
	if (log.isDebugEnabled()) {
	  if (! isModified())
	    log.debug("setChild where child was modified but parent(this) not!");
	  else 
	    log.debug("setChild where child was modified and so was parent.");
	}
	fireModified();
      }
      // or should I do child.fireModified()?
    } else {
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
      } else if (this.getType().equals(ComponentData.RECIPE)) {
        if (firstother < 0)
          firstother = i;
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
    //    log.debug(this + " fireModified due to setParams");
    fireModified();
  }

  public void addParameter(Object param) {
    this.parameters.add(param);
    //    log.debug(this + " fireModified due to addParam");
    fireModified();
  }

  public void setParameter(int index, Object param) {
    if (index < 0 || index >= parameterCount()) {
      if (log.isErrorEnabled())
	log.error(this + " got illegal new param index " + index + " when parameters size is " + parameterCount());
      return;
    }
    this.parameters.set(index, param);
    //    log.debug(this + " fireModified due to setParam");
    fireModified();
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
    if (parent == null && this.parent == null)
      return;
    // Is this next line expensive?
//     if (parent != null && parent.equals(this.parent))
//       return;
    this.parent = parent;
//     log.debug(this + " fireMod due to setParent, where before this.mod=" + isModified() + ", and parent.isMod=" + parent.isModified());
//     fireModified(); // really?
    // Perhaps dont do this cause some code sets the parent
    // on a component before deciding if it really needs to be
    // added to the parent. And since fireModified will go up
    // even if the chain is not complete going down, and saves go down,
    // this risks marking the parent modified even if the parent
    // will never get this child
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
    fireModified();
  }

  public void addLeafComponent(LeafComponentData leaf) {
    leafComponents.add(leaf);
    fireModified();
  }

  public void setLeafComponent(int index, LeafComponentData leaf) {
    leafComponents.set(index, leaf);
    fireModified();
  }

  public int leafCount() {
    return leafComponents.size();
  }

  public AgentAssetData getAgentAssetData() {
    return assetData;
  }

  public void addAgentAssetData(AgentAssetData data) {
    if (data == null && this.assetData == null)
      return;
    if (data != null && data.equals(this.assetData))
      return;
    this.assetData = data;
    if (log.isDebugEnabled())
      log.debug(this + " fireMod due to added assetData");
    fireModified();
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
    fireModified();
  }

  public void setTimePhasedData(int index, TimePhasedData data) {
    timePhasedData.set(index, data);
    fireModified();
  }

  public void addTimePhasedData(TimePhasedData data) {
    timePhasedData.add(data);
  }

  public int timePhasedCount() {
    return timePhasedData.size();
  }

  public void setAlibID(String alibID) {
    if (aLibID != null && aLibID.equals(this.aLibID))
      return;
    if (aLibID == null && this.aLibID == null)
      return;
    this.aLibID = alibID;
    //    log.debug(this + " fireModified due to changed alibid");
    fireModified();
  }

  public String getAlibID() {
    return aLibID;
  }

  public void setLibID(String libID) {
    if (libID != null && libID.equals(this.libID))
      return;
    if (libID == null && this.libID == null)
      return;
    this.libID = libID;
    //    log.debug(this + " fireModified due to changed libid");
    fireModified();
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


    if (parent.getType().equals(ComponentData.RECIPE)) {
      if (log.isDebugEnabled()) {
        log.debug("Ignore checking for duplicates, we are saving a recipe");
      }
      return false;
    }

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

  /**
   * Has this Component been modified by a recipe, requiring possible save?
   */
  public boolean isModified() {
    return modified;
  }

  /**
   * The component has been saved. Mark it and all children as saved.
   */
  public void resetModified() {
    // If this is not modified, neither are its children
    if (! modified) return;
//     if (log.isDebugEnabled())
//       log.debug(this + " resetmodified");
    modified = false;

    // reset any assetdata
    if (this.assetData != null)
      this.assetData.resetModified();

    // recurse down
    if (children != null) {
      for (int i = 0; i < children.size(); i++) {
	((ComponentData)children.get(i)).resetModified();
      }
    }
  }

  /**
   * The component has been modified from its initial state.
   * Mark it and all ancestors modified.
   **/
  public void fireModified() {
    // Problem: I only want to call this after the society generates
    // the CData, and before the recipes are applied....

    // make this private?

    // If this is already modified, so will the parents
    if (modified) return;
    //    if (log.isDebugEnabled())
    //      log.debug(this + " fireModified");
    modified = true;
    // recurse _up_
    if (parent != null)
      parent.fireModified();
  }

}
