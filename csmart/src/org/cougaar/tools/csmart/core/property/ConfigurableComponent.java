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

package org.cougaar.tools.csmart.core.property;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.net.URL;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.event.EventListenerList;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;

import org.cougaar.util.FilteredIterator;
import org.cougaar.util.UnaryPredicate;
import org.cougaar.util.log.Logger;

import org.cougaar.tools.csmart.core.property.name.ComponentName;
import org.cougaar.tools.csmart.core.property.name.SimpleName;
import org.cougaar.tools.csmart.core.property.name.CompositeName;
import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.db.PopulateDb;
import org.cougaar.tools.csmart.ui.viewer.CSMART;

/**
 * This is the default configurable component implementation.
 * It implements all methods that define, name, label, etc. Properties.
 *
 * This class should be overridden by specific components.
 */
public abstract class ConfigurableComponent
  implements BaseComponent, ConfigurableComponentListener
{
  private static final long serialVersionUID = -1294225527645517794L;

  private transient Map myProperties = new HashMap();
  private transient boolean myPropertiesInvalid = false; // flag to invalidate the local cache
  private transient ComposableComponent parent; // Our parent, if any
  private transient List children = null; // Our children, if any
  private static int nameCount = 0;
  private ComponentName myName;
  private boolean nameChangeMark = false;

  private transient EventListenerList listeners = null;

  private PropertiesListener myPropertiesListener = new MyPropertiesListener();

  /** Handle to the Logger **/
  protected transient Logger log;

  private class MyPropertiesListener
    implements PropertiesListener, ConfigurableComponentListener
  {
    public void propertyAdded(PropertyEvent e) {
      // Note the assumption that getProperty returns both Visible and inVisible properties
      if(e.getProperty().isVisible()) {
        firePropertyAdded(e);
      }
    }

    public void propertyRemoved(PropertyEvent e) {
      // Note the assumption that getProperty returns both Visible and inVisible properties
      // Bug 1743: Is the following version better?
      //      if (e.getProperty().isVisible()) {
      if (isPropertyVisible(e.getProperty())) {
        firePropertyRemoved(e);
      }
    }
  }

  /**
   * Gets an <code>EventListenerList</code>.
   *
   * @return an <code>EventListenerList</code> object
   */
  protected EventListenerList getEventListenerList() {
    if (listeners == null) listeners = new EventListenerList();
    return listeners;
  }

  /**
   * Add a PropertiesListener. PropertiesListeners are invoked
   * whenever properties are added or removed.
   *
   * @param l <code>PropertiesListener</code> to add
   */
  public void addPropertiesListener(PropertiesListener l) {
    getEventListenerList().add(PropertiesListener.class, l);
  }

  /**
   * Removes a PropertiesListener.
   *
   * @param l <code>PropertiesListener</code> to remove
   */
  public void removePropertiesListener(PropertiesListener l) {
    getEventListenerList().remove(PropertiesListener.class, l);
  }

  /**
   * Fires an <code>PropertyEvent</code> when a new property
   * is added to the component.
   *
   * @param ev <code>PropertyEvent </code>
   */
  protected void firePropertyAdded(PropertyEvent ev) {
    PropertiesListener[] ls =
      (PropertiesListener[]) getEventListenerList().getListeners(PropertiesListener.class);
    for (int i = 0; i < ls.length; i++) {
      ls[i].propertyAdded(ev);
    }
  }

  private void firePropertyAdded(Property p) {
    if (listeners != null) {
      firePropertyAdded(new PropertyEvent(p, PropertyEvent.PROPERTY_ADDED));
    }
  }

  private void firePropertyRemoved(Property p) {
    firePropertyRemoved(new PropertyEvent(p, PropertyEvent.PROPERTY_REMOVED));
  }

  private void firePropertyRemoved(PropertyEvent ev) {
    PropertiesListener[] ls =
      (PropertiesListener[]) getEventListenerList().getListeners(PropertiesListener.class);
    for (int i = 0; i < ls.length; i++) {
      ls[i].propertyRemoved(ev);
    }
  }

  /**
   * Construct a ConfigurableComponent with a given name. Nota bene:
   * the constructors of ConfigurableComponents must _not_ perform any
   * initialization of properties because the final name of the
   * component is not known until it has been incorporated in a
   * component hierarchy. The abstract initProperties() method should
   * be used to initialize properties. The initialization of any child
   * components should be deferred until the initialization of the
   * parent.
   **/
  protected ConfigurableComponent(String name) {
    if (name == null) {
      name = getClass().getName();
      int ix = name.lastIndexOf('.');
      if (ix >= 0) name = name.substring(ix + 1);
      name += "-" + ++nameCount;
    }
    myName = new ComponentName(null, name);
    createLogger();
  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  public abstract void initProperties();

  /**
   * Set a bunch of Properties at once. Used when creating a component
   * from the database. Default implementation just iterates through
   * the map. Note that the properties <em>must</em> have been previously
   * created in <code>initProperties()</code>
   *
   * @param props a <code>Map</code> of <code>String</code> property names and <code>Object</code> values
   */
  public void setProperties(Map props) {
    // Set the properties in Alphabetical Order.
    // This allows any required listeners to fire, setting up
    // dynamic properties.
    ArrayList names = new ArrayList();
    for (Iterator i = props.keySet().iterator(); i.hasNext(); ) {
      names.add(i.next());
    }

    // Note therefore that for recipes, Arg Order doesnt matter - just
    // the alphabetical order of the argument names
    Collections.sort(names);

    Iterator i = names.iterator();
    
    // For each property to add
    while(i.hasNext()) {
      try {
        String propName = (String) i.next();
        String propValue = (String) props.get(propName);
        Property prop = getProperty(propName);
        if (prop == null) {
          if(log.isErrorEnabled()) {
            log.error("Unknown property: " + propName + "=" + propValue + " in component " + this.toString() + ". Will add it.");
          }
	  // FIXME: This wont get the class right potentially
	  prop = addProperty(propName, propValue);
        } else {
          Class propClass = prop.getPropertyClass();
	  if (log.isDebugEnabled() && propClass == null) {
	    log.debug("null prop class for Prop name: " + propName + ", value " + propValue + " property: " + prop.toString());
	  }
          Constructor constructor = 
            propClass.getConstructor(new Class[] {String.class});
          Object value = constructor.newInstance(new Object[] {propValue});
	  if (log.isDebugEnabled()) {
	    log.debug("Setting value for property " + prop.getName().toString() + " with label " + prop.getLabel() + " and old value: " + prop.getValue() + " to new val " + value);
	  }
          prop.setValue(value);
        }
      } catch (Exception e) {
	if (log.isErrorEnabled()) {
	  log.error("Exception setting component properties", e);
	}
      }
    }
  } // end of setProperties

  /**
   * Get a <code>URL</code> for a description of the component. May return <code>null</code>.
   * <br>
   * This default implementation returns <code>null</code>.
   *
   * @return an <code>URL</code> describing this component.
   */
  public URL getDescription() {
    return null;
  }
  
  /**
   * Gets the Ancestor of this component.  
   * The ancestor is a specific class type.
   *
   * @param cls <code>Class</code> of Ancestor 
   * @return <code>ComposableComponent</code> for the ancestor.
   */
  protected ComposableComponent getAncestorOfClass(Class cls) {
    ComposableComponent parent = getParent();
    if (parent == null) return null;
    if (cls.isInstance(parent)) return parent;
    return ((ConfigurableComponent)parent).getAncestorOfClass(cls);
  }

  /**
   * Gets a <code>Collection</code> of all Descendents of this component 
   * that are a specific class type.
   * Note: The collection (parameter) must be initialized!
   *
   * @param cls The <code>Class</code> of the Descendents
   * @param c An Empty <code>Collection</code> for the result.
   * @return a <code>Collection</code> of a descendents requested.
   */
  public Collection getDescendentsOfClass(Class cls, Collection c) {
    for (int i = 0, n = getChildCount(); i < n; i++) {
      ComposableComponent child = getChild(i);
      child.getDescendentsOfClass(cls, c);
      if (cls.isInstance(child)) c.add(child);
    }
    return c;
  }
        
  /**
   * Gets a <code>Collection</code> of all Descendents of this component 
   * that are a specific class type.
   *
   * @param cls The <code>Class</code> of the Descendents
   * @return a <code>Collection</code> of a descendents requested.
   */
  public Collection getDescendentsOfClass(Class cls) {
    return getDescendentsOfClass(cls, new ArrayList());
  }

  /**
   * Set the name of this component. The name is relative to the
   * parent and must be distinct in that context.
   *
   * @param newName the new name for this component.
   **/
  public void setName(String newName) {
    startNameChange();
    myName.setName(SimpleName.getSimpleName(newName));
    finishNameChange();
  }

  /**
   * Gets the short name of this component.  All component names
   * are made up of a chain based on the component hierarchy.
   * This chain is: grandparent.parent.child
   * Short name is just 'child'.
   *
   * @return a <code>String</code> value of the short name.
   */
  public String getShortName() {
    return getFullName().last().toString();
  }

  /**
   * Gets the full name of this component. All component names
   * are made up of a chain based on the component hierarchy.
   * This chain is: grandparent.parent.child
   *
   * Full name is the complete chain.
   *
   * @return a <code>CompositeName</code> value of the full component name.
   */
  public CompositeName getFullName() {
    return myName;
  }

  /**
   * Add a child to this component. Childs are always of type
   * <code>ComposableComponent</code>.
   *
   * @param c the child to add
   * @return Count of all Children in this component.
   */
  public int addChild(ComposableComponent c) {
    if (children == null) children = new ArrayList();
    ((ConfigurableComponent)c).addPropertiesListener(myPropertiesListener);
    if (getChild(((ConfigurableComponent)c).getFullName()) != null) {
      // Name is not unqiue, make it so.
      ((ConfigurableComponent)c).setName(((ConfigurableComponent)c).getFullName().toString() + String.valueOf(getChildCount()));
    }
    children.add(c);
    c.setParent(this);
    for (Iterator i = ((ConfigurableComponent)c).getProperties(); i.hasNext(); ) {
      Property prop = (Property) i.next();
      if (prop != null) firePropertyAdded(prop);
    }
    return children.size() - 1;
  }

  /**
   * Removes a child component at a specific index.
   *
   * @param childIndex Index of child to remove.
   */
  public void removeChild(int childIndex) {
    removeChild(getChild(childIndex));
  }

  /**
   * Removes a specific child component.
   *
   * @param c <code>ComposableComponent</code> of child to remove.
   */
  public void removeChild(ComposableComponent c) {
    if (children == null || c.getParent() != this) return;
    ((ConfigurableComponent)c).removePropertiesListener(myPropertiesListener);
    for (Iterator i = ((ConfigurableComponent)c).getProperties(); i.hasNext(); ) {
      Property prop = (Property) i.next();
      if (prop != null) firePropertyRemoved(prop);
    }
    children.remove(c);
    c.setParent(null);
  }

  /**
   * Removes all children of this component.
   *
   */
  public void removeAllChildren() {
    while(getChildCount() != 0) {
      removeChild(getChildCount() - 1);
    }
  }

  /**
   * Gets a count of all children within this component.
   *
   * @return Child Count.
   */
  public int getChildCount() {
    if (children == null) return 0;
    return children.size();
  }

  /**
   * Gets a child component from a specific index.
   *
   * @param n index of child component to retreive.
   * @return a <code>ComposableComponent</code> object for that child.
   */
  public ComposableComponent getChild(int n) {
    return (ComposableComponent) children.get(n);
  }

  /**
   * Gets a child component based on the childs Full Name.
   *
   * @param childName <code>CompositeName</code> of the child.
   * @return a <code>ComposableComponent</code> object of the child.
   */
  public ComposableComponent getChild(CompositeName childName) {
    ComposableComponent cc = null;

    for(int i=0; i < getChildCount(); i++) {
      cc = getChild(i);
      if(((ConfigurableComponent)cc).getShortName().equals(childName.toString())){
	return cc;
      }
    }

    return null;
  }

  /**
   * Changing our parent changes all our property names so we have to
   * rehash them and flush their caches.
   *
   * @param newParent New Parent Component
   */
  public void setParent(ComposableComponent newParent) {
    ComposableComponent oldParent = parent;
    startNameChange();
    parent = newParent;
    myName.setComponent((BaseComponent)parent); // flushes myName's cache as a side-effect
    finishNameChange();
    if (oldParent != null) 
      ((ConfigurableComponent)oldParent).fireChildConfigurationChanged();
    if (newParent != null) 
      ((ConfigurableComponent)newParent).fireChildConfigurationChanged();
  }

  /**
   * Add a ChildConfigurationListener. ChildConfigurationListener are
   * invoked whenever children are added or removed.
   *
   * @param l <code>ChildConfigurationListener</code> to add to this component.
   */
  public void addChildConfigurationListener(ChildConfigurationListener l) {
    getEventListenerList().add(ChildConfigurationListener.class, l);
  }

  /**
   * Removes a <code>ChildConfigurationListener</code> from this component.
   *
   * @param l <code>ChildCondigurationListener</code> to remove.
   */
  public void removeChildConfigurationListener(ChildConfigurationListener l) {
    getEventListenerList().remove(ChildConfigurationListener.class, l);
  }

  /**
   * Fires an event when a child configuration has changed.
   *
   */
  protected void fireChildConfigurationChanged() {
    ChildConfigurationListener[] ls =
      (ChildConfigurationListener[])
      getEventListenerList().getListeners(ChildConfigurationListener.class);
    for (int i = 0; i < ls.length; i++) {
      ls[i].childConfigurationChanged();
    }
  }
      
  /**
   * Move the contents of the myProperties Map into the
   * myPropertyEntries Collection since the keys in the Map are about
   * to become invalid. The myProperties Map will be recreated later.
   **/
  public void startNameChange() {
    if (nameChangeMark) return;
    nameChangeMark = true;
    for (int i = 0, n = getChildCount(); i < n; i++) {
      ((ConfigurableComponent)getChild(i)).startNameChange();
    }

    // MIK: WTF why are we trashing the entire CC hierarchy's maps?!?
    /*
    if (parent != null) 
      ((ConfigurableComponent)parent).startNameChange();
    */

    nameChangeMark = false;

    getFullName().decache();    // decache this component name.

    if (myProperties != null) {
      // invalidate the property hashmap
      myPropertiesInvalid = true;
      
      // while we're here, we'll decache the property names
      // MIK: we could probably wait till we do getMyProperties
      for (Iterator it = myProperties.entrySet().iterator(); it.hasNext(); ) {
        Map.Entry entry = (Map.Entry) it.next();
        CompositeName n = (CompositeName) entry.getKey();
        n.decache();
      }
    }
  } // end of startNameChange

  /**
   * Called after a change in our name is completed. We do nothing
   * with the myProperties Map because it is recreated on demand. If
   * there was a former parent, we inform the old parent that one of
   * its children has been abducted.
   **/
  public void finishNameChange()
  {
    if (nameChangeMark) return;
    nameChangeMark = true;
    for (int i = 0, n = getChildCount(); i < n; i++) {
      ((ConfigurableComponent)getChild(i)).finishNameChange();
    }
    if (parent != null) 
      ((ConfigurableComponent)parent).finishNameChange();
    nameChangeMark = false;
  }

  /**
   * The actual Map of our properties is private so we can do a late
   * bind of it. The properties are sometimes in the myProperties Map
   * and sometimes in myPropertyEntries collection. This is done
   * because the names can change as the component is reparented and
   * therefore become invalid as keys in the map. After reparenting,
   * this procedure recreates the Map the first time it is needed.
   * @return the Map of this component's properties.
   **/
  protected Map getMyProperties() {
    if (myProperties != null) {
      if (myPropertiesInvalid) {
        // copy the contents because we're going to trash the original
        ArrayList tmp = new ArrayList(myProperties.values());
        // clear the original
        myProperties.clear();
        for (int i = 0, l = tmp.size(); i<l; i++) {
          Property p = (Property) tmp.get(i);
          myProperties.put(p.getName(), p);
        }
        myPropertiesInvalid = false;
      } 
    }
    // Note assumption that myProperties is never null. Otherwise, getMyProperties could
    // now return null, which is an API change!
    return myProperties;
  }

  /**
   * Gets the parent of this component.  If there are no
   * parents, NULL is returned.
   *
   * @return a <code>ComposableComponent</code> object for the parent, or null if no parent.
   */
  public ComposableComponent getParent() {
    return parent;
  }

  /**
   * Get an invisible property which is not returned by <code>getVisibleProperty</code>. 
   * Use this method only when absolutely necessary.
   * Returns null if no invisible property exists with the specified name. 
   *
   * @param name full name of property to look for
   * @return a <code>Property</code> value, null if the named property is Visible
   */
  public Property getInvisibleProperty(CompositeName name) {
    return getPropertyWorker(name, false, false);
  }

  /**
   * Get an invisible property which is not returned by <code>getVisibleProperty</code>. 
   * Use this method only when absolutely necessary.
   * Returns null if no invisible property exists with the specified name. 
   *
   * @param localName short name of property to look for
   * @return a <code>Property</code> value
   */
  public Property getInvisibleProperty(String localName){
    return getInvisibleProperty(new ComponentName(this, localName));
  }
  
  /**
   * Get a visible property which is not returned by <code>getInvisibleProperty</code>. 
   * Use this method only when absolutely necessary.
   * Returns null if no visible property exists with the specified name. 
   *
   * @param name full Name of property to look for
   * @return a <code>Property</code> value, null if the named property is Invisible
   */
  public Property getVisibleProperty(CompositeName name) {
    return getPropertyWorker(name, false, true);
  }

  /**
   * Get a visible property which is not returned by <code>getVisibleProperty</code>. 
   * Use this method only when absolutely necessary.
   * Returns null if no visible property exists with the specified name. 
   *
   * @param localName short name of Property to find
   * @return a <code>Property</code> value
   */
  public Property getVisibleProperty(String localName){
    return getVisibleProperty(new ComponentName(this, localName));
  }
  
  /**
   * Get a property by name. Current functionality returns it regardless
   * of whether it is in fact invisible.
   *
   * @param name the name of the property.
   * @return the property or null if there is no property with the
   * specified name
   **/
  public Property getProperty(CompositeName name) {
    return getPropertyWorker(name, true, true);
  }

  // Only print the stacktrace once for this error
  private transient boolean hadNullChildNameError = false;

  // Worker method that gets properties from a component or its children.
  // If you ask for all, it gets the property whether visible or not.
  // Otherwise, it looks at the third argument, and only returns the property
  // if its visibility matches your request
  // Note that this method is called recursively
  protected Property getPropertyWorker(CompositeName name, boolean all, boolean visible) {
    // Note: This now assumes it is _always_ true that a Property is located off a Component
    // based on its name: <full name of component>.foo, for example

    // walk from root to see if we're at a reasonable spot
    if (name.startsWith(myName)) { // is this component a prefix?
      // Invisible Properties are still properties -- they just
      // say they are not visible. So we must test them
      // and return null if they say they are not visible
      Property p = (Property)getMyProperties().get(name);
      if (p != null) {
	// Are we getting all properties
	if (all) {
	  // If so, just return it
	  return p;
	  // Otherwise, we only want to return the actual property
	  // if its visibility matches that requested
	} else if (p.isVisible() == visible) {
	  return p;
	} else {
	  // User did not want all properties, and this property's visibility
	  // is not the variant the user wanted
	  // Don't return it.
	  // Note however that on recursion into children, this null return
	  // is indistinguishable from not finding the property
	  return null;
	}
      } // end of block on found the property locally

      // Didn't find the property locally. Check the children.
      // check the children
      int myl = myName.size();
      int nl = name.size();
      if (nl>myl) {             // any chance a child has it?
        CompositeName nn = name.get(myl); // get the next name
	if (nn == null) {
	  if (log.isErrorEnabled())
	    log.error("getProperty: null CompositeName. myName: " + myName + ", name: " + name + " myName.size(): " + myl + " name.size() " + nl, new Throwable());
	  return null;
	}

	// Now loop over the children
        for (int i = 0, n = getChildCount(); i < n; i++) {
          ConfigurableComponent child = (ConfigurableComponent) getChild(i);

	  // First, some error checks
	  if (child == null) {
	    if (log.isErrorEnabled())
	      log.error("getProperty[" + getFullName() + "]: null child at index " + i + " when getChildCount reported " + n, new Throwable());
	    continue;
	  }
	  if (child.getFullName() == null) {
	    // This seems common at de-serialization, and apparently harmless. Is it somehow expected?
	    if (hadNullChildNameError) {
	      // Warning: Can't do stacktrace here cause it happens so often
	      if (log.isInfoEnabled())
		log.info("getProperty[" + getFullName() + "]: null child name for child at index " + i);
	    } else {
	      hadNullChildNameError = true;
	      if (log.isInfoEnabled())
		log.info("getProperty[" + getFullName() + "]: null child name for child at index " + i, new Throwable());
	    }
	    continue;
	  }

	  // Now see if the childs name would allow it to have the property
          if (nn.equals(child.getFullName().get(myl))) {
            Property result = child.getPropertyWorker(name, all, visible);
            if (result != null) {
	      // Found it!
              if (nl == myl+1) {
		if (log.isErrorEnabled())
		  log.error("getProperty: Bogon alert: n+l child match!\n"+
                                   getFullName()+"\n"+
                                   name, new Throwable());
              }                
              return result;
            }
	    // We fall in here also when the matching child had the property
	    // but it was invisible and we wanted visible or vice versa
	    if (log.isErrorEnabled())
	      log.error("getProperty Error: matching child didn't have " + (all==true ? "" : (visible==true ? "visible " : "invisible ")) + "property\n"+
                               getFullName()+"\n"+
                               name, new Throwable());
            return null;
          }
        } // end of loop over children

        // no child has it
        if (nl != myl+1) {
          // only warn if the prop is not 1 name longer than component.
	  if (log.isErrorEnabled())
	    log.error("getProperty Error: didn't have a child who could have match\n"+
                             getFullName()+"\n"+
                             name, new Throwable());
        }
        return null;
      } else {
        // no child could have it (even if we have children)
        // We'll see this whenever someone asks component "foo.bar" for "foo.bar.baz" property
        // which doesn't exist.
	//if (log.isErrorEnabled())
        //log.error("getProperty Error: No child could have match ("+nl+"<="+myl+")\n"+
        //                   getFullName()+"\n"+
        //                   name, new Throwable());
        return null;
      }
    } else {
      // inappropriate prefix - bail
      if (log.isErrorEnabled())
	log.error("getProperty Error: Inappropriate prefix\n"+
                         getFullName()+"\n"+
                         name, new Throwable());
      //Thread.dumpStack();
      return null;
    }
  } // end of getPropertyWorker

  /**
   * Gets a local property based on a <code>String</code> name.
   *
   * @param localName of the property
   * @return a <code>Property</code> object.
   */
  public Property getProperty(String localName) {
    return getProperty(new ComponentName(this, localName));
  }

  /**
   * Add a property with a given value. A new Property is created
   * having the given name and value. In addition the other fields of
   * the property set to default values consistent with the class of
   * the value.
   * @param name the name of the property
   * @param value must be one of the supported value types
   * @return the index of the new property
   **/
  public Property addProperty(String name, Object value) {
    if (value == null) throw new IllegalArgumentException("null value not allowed");
    return addProperty(name, value, value.getClass(), true);
  }

  /**
   * Add an invisible property to the component.  Invisible properties
   * are property that act and behave just like a normal property but
   * are not visible to the GUI.
   *
   * @param name of the property
   * @param value of the propery
   * @return a <code>Property</code> object for the new Property
   */
  public Property addInvisibleProperty(String name, Object value) {
    if (value == null) throw new IllegalArgumentException("null value not allowed");
    return addProperty(name, value, value.getClass(), false);
  }

  /**
   * Adds a property to the component with a given name, value and class.
   *
   * @param name Name of the property
   * @param value Value of the property
   * @param cls Class of the property
   * @return a <code>Property</code> object for the new Property.
   */
  public Property addProperty(String name, Object value, Class cls) {
    return addProperty(name, value, cls, true);
  }

  /**
   * Adds an invisible property to the component with a given name, value and class.
   *
   * @param name Name of the property
   * @param value Value of the property
   * @param cls Class of the property
   * @return a <code>Property</code> object for the new Property.
   */
  public Property addInvisibleProperty(String name, Object value, Class cls) {
    return addProperty(name, value, cls, false);
  }

  /**
   * Adds a new Property to the Component.
   *
   * @param p <code>Property</code> to add.
   * @return a <code>Property</code> value
   */
  public Property addProperty(Property p) {
    return addProperty(p, true);
  }

  /**
   * Adds a new Invisible Property to the component.
   *
   * @param p <code/>Property</code> to add.
   * @return a <code/>Property</code> value
   */
  public Property addInvisibleProperty(Property p) {
    return addProperty(p, false);
  }

  /**
   * Adds a new Property with an attached PropertyListener
   *
   * @param name Name of the new Property
   * @param value Value of the new Property
   * @param l PropertyListener for this Property
   * @return a <code>Property</code> value
   */
  public Property addProperty(String name, Object value, PropertyListener l) {
    Property p = addProperty(name, value, value.getClass(), false);
    p.addPropertyListener(l);
    return p;
  }


  /**
   * Removes a Property from this component.  The property must be
   * local to the component.
   *
   * @param prop Property to remove.
   */
  public void removeProperty(Property prop) {
    boolean wasVisible = isPropertyVisible(prop);
    // FIXME!!
    // Huh? So how do you remove an invisible property? Or is that not possible?
    if (!wasVisible) return; // if it's not visible, then it's not my property
    Object oldValue = getMyProperties().remove(prop.getName());
    if (oldValue == null) { // was someone else's property, ignore
      if (log.isErrorEnabled())
        log.error("", new Throwable("Attempting to remove non-local property: " + prop.getName() + " from " + getFullName().toString()));
      return;
    }
    firePropertyRemoved(prop);
  }

  private Property addProperty(Property p, boolean visible) {
    if (p == null || p.getName() == null)
      return p;

    // Put in some debug stuff to see if this is ever called with a Property whose
    // name does not make sense in this component
    CompositeName name = p.getName();
    if (! name.startsWith(myName)) {
      if (log.isWarnEnabled())
	log.warn("Adding property whose name makes no sense locally: " + getFullName() + ".addProperty(" + name + ", visible=" + visible, new Throwable());
    }
    
    // remove old property if it exists
    Property oldProperty = (Property)getMyProperties().get(p.getName());
    if (oldProperty != null)
      removeProperty(oldProperty);
    getMyProperties().put(p.getName(), p);
    // if the new property is visible, tell the listeners
    if (visible) firePropertyAdded(p);
    return p;
  }

  private Property addProperty(String name, Object value, Class cls, boolean visible) {
    Property result = addProperty(new ConfigurableComponentProperty(this, name, value), visible);
    result.setPropertyClass(cls);
    return result;
  }


  /**
   * Gets an <code/>Iterator</code> of all visible properties Local to this component.
   *
   * @return an <code/>Iterator</code> of all local properties
   */
  public Iterator getLocalPropertyNames() {
    return new FilteredIterator(getPropertyNames(), localPropertyNamePredicate);
  }

  /**
   * Gets an <code/>Iterator</code> of all visible properties Local to this component, sorted.
   *
   * @return an <code/>Iterator</code> of all sorted local properties
   */
  public Iterator getSortedLocalPropertyNames() {
    ArrayList names = new ArrayList();
    Iterator iter = new FilteredIterator(getPropertyNames(), localPropertyNamePredicate);
    
    while(iter.hasNext()) {
      names.add(iter.next());
    }

    Collections.sort(names);

    return names.iterator();

  }

  /**
   * Test if a Property is local to this component. Local properties
   * have names beginning with the name of this component and have one
   * additional component.
   **/
  private UnaryPredicate localPropertyNamePredicate = new UnaryPredicate() {
    public boolean execute(Object o) {
      CompositeName pName = (CompositeName) o;
      CompositeName cName = getFullName();
      return pName.size() == cName.size() + 1 && pName.startsWith(cName);
    }
  };
    
  // FIXME: Maybe add a version that does all properties, not just visible?
  // Follow the getPropertyWorker model above...
  /**
   * Gets an <code>Iterator</code> of all visible Property Names in (or under) this component.
   *
   * @return an <code>Iterator</code> value
   */
  public Iterator getPropertyNames() {
    return new Iterator() {
	// Note: getMyProperties _will_ return invisible properties
	Iterator currentIterator = getMyProperties().keySet().iterator();
	int nextChildIndex = 0;
	CompositeName pendingName = null;

	// Track which component's properties we are looking at. Always starts with this
	ConfigurableComponent child = ConfigurableComponent.this;

	public boolean hasNext() {
	  while (pendingName == null) {
	    // If done searching a given component
	    while (!currentIterator.hasNext()) {
	      // If don't have any more children to search
	      if (nextChildIndex >= getChildCount()) {
		currentIterator = null; // drop ref to keyset, even if client keeps iterator around.
		return false;
	      }
	      // Get next child
	      child = (ConfigurableComponent)getChild(nextChildIndex++);
	      currentIterator = child.getPropertyNames();
	    }

	    // Try next property name in list
	    pendingName = (CompositeName) currentIterator.next();
	    try {
	      if (child != null) {
		if (pendingName != null) {
		  // Find the property in the child, regardless of its visibility
		  if (child.getProperty(pendingName) == null) {
		    // Since we're looking at the child from which we just got the name,
		    // this is bad. Particulary since getProperty returns all properties,
		    // not just visible ones
		    if (log.isErrorEnabled())
		      log.error(getFullName() + ".getPropertyNames: got null property for name " + pendingName + " off of child " + child.getFullName(), new Throwable());

		    // Don't treat this as a valid property name to return
		    pendingName = null;
		  } else if (!((Property)child.getProperty(pendingName)).isVisible()) {
		    // getProperty now returns all properties, so this is legitimate

// 		    if (log.isInfoEnabled())
// 		      log.info(getFullName() + ".getPropertyNames got invisible but not null property " + pendingName + " off of child " + child.getFullName());

		    // Right here we decide: Does this method return both visible and 
		    // invisible property names?
		    // To ignore invisible properties, we set pendingName to null
		    // so the hasNext will return false if only invisible properties left
		    // and next will never give it to you
		    pendingName = null;
		  }
		}
	      } else {
		if (log.isErrorEnabled())
		  log.error(getFullName() + ".getPropertyNames got null child at nextChildIndex " + nextChildIndex + " while looking for prop=" + pendingName);
	      } // end of block to ensure non-null child
	    } catch (Exception e) {
	      if (log.isErrorEnabled())
		log.error("getPropertyNames: Exception while getting "+ getFullName()+" prop="+pendingName+" and nextChildIndex: " + nextChildIndex, e);
	    }
	  } // end of while loop getting a non-null pendingName
	  return true;
	}
	
	public Object next() {
	  if (pendingName == null && !hasNext())
	    throw new NoSuchElementException();
	  Object result = pendingName;
	  pendingName = null;
	  return result;
	}
	public void remove() {
	  throw new UnsupportedOperationException();
	}
      };
  }
  
  /**
   * Get iterator of all local properties (regardless of visibility)
   **/
  public Iterator getLocalProperties() {
    return getMyProperties().values().iterator();
  }

  // FIXME: Need a version for all properties? For invisible properties?
  /**
   * Return the properties in this component tree. Ignore invisible properties
   **/
  public Iterator getProperties() {
    return new Iterator() {
        Iterator currentIterator = getMyProperties().values().iterator();
        int nextChildIndex = 0;
        Property pendingProp = null;
        public boolean hasNext() {
          while (pendingProp == null) {
            while (!currentIterator.hasNext()) {
              if (nextChildIndex >= getChildCount()) {
                return false;
              }
              currentIterator = 
                ((ConfigurableComponent)getChild(nextChildIndex++)).getProperties();
            }
	    
	    Property nprop = (Property)currentIterator.next();
	    // Right here is where we decide whether this iterator covers just visible
	    // -- as in this case -- all, or just invisible properties
            if (!nprop.isVisible()) {
              pendingProp = null;
            } else {
	      pendingProp = nprop;
	    }
          }
          return true;
        }
        
        public Object next() {
          if (pendingProp == null && !hasNext())
            throw new NoSuchElementException();
          Object result = pendingProp;
          pendingProp = null;
          return result;
        }
        public void remove() {
          throw new UnsupportedOperationException();
        }
      };
  }

  /**
   * Return all properties in this tree, regardless of visibility
   **/
  public Iterator getAllProperties() {
    return new Iterator() {
        Iterator currentIterator = getMyProperties().values().iterator();
        int nextChildIndex = 0;
        Property pendingProp = null;
        public boolean hasNext() {
          while (pendingProp == null) {
            while (!currentIterator.hasNext()) {
              if (nextChildIndex >= getChildCount()) {
                return false;
              }
              currentIterator = 
                ((ConfigurableComponent)getChild(nextChildIndex++)).getProperties();
            }
	    
	    pendingProp = (Property)currentIterator.next();
          }
          return true;
        }
        
        public Object next() {
          if (pendingProp == null && !hasNext())
            throw new NoSuchElementException();
          Object result = pendingProp;
          pendingProp = null;
          return result;
        }
        public void remove() {
          throw new UnsupportedOperationException();
        }
      };
  }

  /**
   * Block the propagation of a child property. Such properties are
   * typically set by the parent component and should not be visible
   * from above.
   **/
  public void setPropertyVisible(Property prop, boolean newVisible) {
    CompositeName name = prop.getName();
    Property p = null;
    if (newVisible) {
      p = (Property) getMyProperties().get(name);
      // Property may not exist in map yet.
      if(p != null) {
        if (!p.isVisible()) firePropertyAdded(prop);
      }
    } else {
      p = (Property)getMyProperties().get(name);
      if(p != null) {
        if (p.isVisible()) firePropertyRemoved(prop);
      }
    }
    if(p != null) {
      p.setVisible(newVisible);
      getMyProperties().put(name, p);
    }
  }

  /**
   * Determines if the specified property is visible as a local property.
   * If a property is not visible, it cannot be seen in the GUI.
   *
   * @param prop Property to check visiblity
   * @return a <code>boolean</code> value
   */
  public boolean isPropertyVisible(Property prop) {
    Property p = (Property)getMyProperties().get(prop.getName());
    // If the society is fresh from the db, props don't exist in map yet.
    if(p == null) return false;
    return p.isVisible();
  }

  /**
   * Gets a <code/>List</code> of all visible Properties in this component.
   *
   * @return a <code/>List</code> value
   */
  public List getPropertyNamesList() {
    List props = new ArrayList();
    for (Iterator i = getPropertyNames(); i.hasNext(); ) {
      CompositeName name = (CompositeName) i.next();
      props.add(name);
    }
    return props;
  }

  /**
   * Copies a BaseComponent Object
   *
   * @param result Object to copy
   * @return a <code/>BaseComponent</code> value
   */
  public BaseComponent copy(BaseComponent result) {
    if (result == null) {
      log.error("Null target component copying from " + this.getFullName().toString(), new Throwable());
      return null;
    }
    // Make sure we're copying apples into apples
    // The result can be a sub-class, but we want it
    // to at least have the same set of properties
    if (! (this.getClass().isAssignableFrom(result.getClass())))
      return null;
    Iterator iter = getSortedLocalPropertyNames();
    while(iter.hasNext()) {
      CompositeName name = (CompositeName)iter.next();
      Property myProp = getProperty(name);
      if (myProp == null) {
        // Try to get invisible Property. -- but note that
	// invisible properties are also returned by getProperty
	// so we should never get in here
        myProp = getInvisibleProperty(name);
        if (myProp == null) {
          if (log.isErrorEnabled()) {
            log.error("Null property " + name.toString() + " copying from " + this.getFullName().toString(), new Throwable());
          }
          continue;
        }
      }
      // compose the correct name for the property
      // name must be prepended by new society name
      String s = name.last().toString();
      ComponentName hisPropName = new ComponentName(result, s);
      Property hisProp = result.getProperty(hisPropName);

      // Note that this InvisibleProperty call should not help, since getProperty should
      // return both visible and invisible properties
      if (hisProp == null && ((hisProp = result.getInvisibleProperty(hisPropName)) == null )) {
        if (log.isErrorEnabled()) {
          log.error("Report bug 1377: Using " + CSMART.writeDebug() + " couldn't find " + hisPropName.toString() + " in " + result.getFullName().toString() + " copying from " + name.toString() + " in " + this.getFullName().toString(), new Throwable());
        }
      } else {
	try {
	  // if have experimental values, then copy those
	  // else copy the property value
	  if (myProp.getExperimentValues() != null) {
	    List experimentValues = myProp.getExperimentValues();
	    Object newValue = Array.newInstance(myProp.getPropertyClass(),
						experimentValues.size());
	    for (int j = 0; j < experimentValues.size(); j++)
	      Array.set(newValue, j,
			PropertyHelper.validateValue(myProp, 
						     experimentValues.get(j)));
	    hisProp.setExperimentValues(Arrays.asList((Object[])newValue));
	    hisProp.setValue(null); // no specific value
	  } else {
	    Object o = PropertyHelper.validateValue(myProp, myProp.getValue());
	    if (o != null) {
	      hisProp.setValue(o);
	    }
	  }
	} catch (InvalidPropertyValueException e) {
	  if(log.isErrorEnabled()) {
	    log.error("Caught InvalidPropertyValueException: " + getClass().getName(), e);
	  }
	}
      } // end of else block
    } // end of loop over properties

    // Now, clone my children
    for(int i=0; i < getChildCount(); i++) {
      ConfigurableComponent cc = (ConfigurableComponent)getChild(i);
      BaseComponent co = 
        (BaseComponent)result.getChild(cc.getFullName().last());
      if(co != null) {
	cc.copy(co);
      }
    }

    return result;
  }

  private void writeObject(ObjectOutputStream stream)
     throws IOException
  {
    stream.defaultWriteObject();
    stream.writeObject(parent);
    stream.writeObject(children);
    Map properties = getMyProperties();
    
    // write in new format - old format is below in comment
    {
      int n = (-1)-properties.size(); // signal new format
      stream.writeInt(n);
      for (Iterator i = properties.values().iterator(); i.hasNext(); ) {
        stream.writeObject(i.next());
      }
    }

    /*
    // old format
    stream.writeInt(properties.size());
    for (Iterator i = properties.entrySet().iterator(); i.hasNext(); ) {
      PropertyEntry entry = new PropertyEntry((Map.Entry)i.next());
      stream.writeObject(entry);
    }
    */

    stream.writeObject(getSerializableListeners());
  }

  private List getSerializableListeners() {
    List result = new ArrayList();
    Object[] lll = getEventListenerList().getListenerList();
    for (int i = 0; i < lll.length; i += 2) {
      Class cls = (Class) lll[i];
      EventListener l = (EventListener) lll[i + 1];
      if (l instanceof ConfigurableComponentListener) {
        result.add(cls);
        result.add(l);
      }
    }
    return result;
  }

  private void readObject(ObjectInputStream stream)
    throws IOException, ClassNotFoundException
  {
    myProperties = new HashMap(1);
    stream.defaultReadObject();
    parent = (ComposableComponent) stream.readObject();
    children = (List) stream.readObject();
    int n = stream.readInt();
    if (n >= 0) {
      // old, big format - this should be dumped.
      myProperties = new HashMap(1+n*4);
      for (int i = 0; i < n; i++) {
        PropertyEntry entry = (PropertyEntry)stream.readObject();
        Property p = (Property) entry.getValue();
        myProperties.put(p.getName(), p);
      }
    } else {
      // new format - we should cut over and simplify
      n = (-1)-n;           
      myProperties = new HashMap(1+n*4);
      for (int i = 0; i < n; i++) {
        Property p = (Property) stream.readObject();
        myProperties.put(p.getName(), p);
      }
    }
    setSerializableListeners((List) stream.readObject());
    createLogger();
  }

  public ComponentData addComponentData(ComponentData data) {
    return data;
  }

  public ComponentData modifyComponentData(ComponentData data) {
    return data;
  }

  /**
   * Default implementation of new form of modifyComponentData invokes
   * the old form without the pdb argument
   **/
  public ComponentData modifyComponentData(ComponentData data, PopulateDb pdb) {
    return modifyComponentData(data);
  }

  public boolean componentWasRemoved() {
    return false;
  }

  // Note this only checks visible properties
  public boolean hasUnboundProperties() {
    for (Iterator i = getPropertyNames(); i.hasNext(); ) {
      Property prop = (Property) getProperty((CompositeName) i.next());
      if(prop != null) {
        if (!prop.isValueSet()) return true;
      }
    }
    return false;
  }

  private void setSerializableListeners(List l) {
    EventListenerList ll = getEventListenerList();
    for (Iterator i = l.iterator(); i.hasNext(); ) {
      Class cls = (Class) i.next();
      EventListener listener = (EventListener) i.next();
      ll.add(cls, listener);
    }
  }

  /**
   * Dumps a list of the Local Properties
   *
   * @param out Stream to dump properties to.
   */
  public void printLocalProperties(PrintStream out) {
    printLocalProperties(out, "");
  }

  /**
   * Prints a list of local properties.
   *
   * @param out Stream to print properties to.
   * @param indent Indentation before each output string.
   */
  public void printLocalProperties(PrintStream out, String indent) {
    List props = new ArrayList();
    for (Iterator iterator = getLocalPropertyNames(); iterator.hasNext(); )
      props.add(iterator.next());
    printProperties(out, indent, props);
  }

  /**
   * Prints all visible Properties for this component.
   *
   * @param out Stream to print to.
   */
  public void printAllProperties(PrintStream out) {
    printAllProperties(out, "");
  }

  /**
   * Prints all visible Properties for this component.
   *
   * @param out Stream to print to.
   * @param indent Indentation amount before each output line.
   */
  public void printAllProperties(PrintStream out, String indent) {
    printProperties(out, "", getPropertyNamesList());
  }

  /**
   * Prints a specific list of Properties.
   *
   * @param out Stream to output to.
   * @param indent About to indent output.
   * @param props List of Properties to list.
   */
  private void printProperties(PrintStream out, String indent, List props) {
    int n = props.size();
    out.println(indent + "Number of properties: " + n);
    out.println(indent + "=======================================");
    for (int i = 0; i < n; i++) {
      CompositeName name = (CompositeName) props.get(i);
      out.println(indent + "Property name: " + name);
      getProperty(name).printProperty(out, indent);
      out.println(indent + "=======================================");
    }
  }

  static class PropertyEntry implements Map.Entry, Serializable {
    Object key;
    Object value;

    public PropertyEntry(Map.Entry me) {
      key = me.getKey();
      value = me.getValue();
    }

    public Object getKey() {
      return key;
    }

    public Object getValue() {
      return value;
    }

    public Object setValue(Object o) {
      Object result = value;
      value = o;
      return result;
    }
  }

  // Note: ConfigurableComponent has no .equals method. Should it?

  public String toString() {
    return getShortName();
  }
} // end of ConfigurableComponent
