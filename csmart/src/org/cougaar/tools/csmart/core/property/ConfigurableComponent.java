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
 * This method can be overridden by specific components.
 */
public abstract class ConfigurableComponent
  implements BaseComponent, ConfigurableComponentListener
{
  private static final long serialVersionUID = -1294225527645517794L;

  private transient Map myProperties = new HashMap();
  private transient Collection myPropertyEntries = null;
  private transient ComposableComponent parent; // Our parent, if any
  private transient List children = null; // Our children, if any
  private Set blockedProperties = new HashSet();
  private static int nameCount = 0;
  private ComponentName myName;
  private boolean nameChangeMark = false;
  private static Object nullProperty = new NullProperty();

  private transient EventListenerList listeners = null;

  private PropertiesListener myPropertiesListener = new MyPropertiesListener();

  /** Handle to the Logger **/
  protected transient Logger log;

  private class MyPropertiesListener
    implements PropertiesListener, ConfigurableComponentListener
  {
    public void propertyAdded(PropertyEvent e) {
      if (isPropertyVisible(e.getProperty())) {
        firePropertyAdded(e);
      }
    }

    public void propertyRemoved(PropertyEvent e) {
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
    ArrayList names = new ArrayList();
    for (Iterator i = props.keySet().iterator(); i.hasNext(); ) {
      names.add(i.next());
    }

    Collections.sort(names);

    Iterator i = names.iterator();
    
    while(i.hasNext()) {
      try {
        String propName = (String) i.next();
        String propValue = (String) props.get(propName);
        Property prop = getProperty(propName);
        if (prop == null) {
          if(log.isErrorEnabled()) {
            log.error("Unknown property: " + propName + "=" + propValue + " in component " + this.toString());
          }
        } else {
          Class propClass = prop.getPropertyClass();
	  if (log.isDebugEnabled() && propClass == null) {
	    log.debug("null prop class for Prop name: " + propName + ", value " + propValue + " property: " + prop.toString());
	  }
          Constructor constructor = 
            propClass.getConstructor(new Class[] {String.class});
          Object value = constructor.newInstance(new Object[] {propValue});
          prop.setValue(value);
	  if (log.isDebugEnabled()) {
	    log.debug("Setting value for property " + prop.getName().toString() + " with label " + prop.getLabel());
	  }
        }
      } catch (Exception e) {
	if (log.isErrorEnabled()) {
	  log.error("Exception setting component properties", e);
	}
      }
    }
  }

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
    myName.setName(new SimpleName(newName));
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
   * rehash them
   *
   * @param newParent New Parent Component
   */
  public void setParent(ComposableComponent newParent) {
    ComposableComponent oldParent = parent;
    startNameChange();
    parent = newParent;
    myName.setComponent((BaseComponent)parent);
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
    if (parent != null) 
      ((ConfigurableComponent)parent).startNameChange();
    nameChangeMark = false;
    if (myProperties != null) {
      // Must make a copy because the entrySet is backed by the Map
      // and the Map is about to become invalid. (The actual
      // implementation of HashMap does not require this copying, but
      // there are no guarantees.)
      myPropertyEntries = new ArrayList(myProperties.entrySet());
      myProperties = null;
    }
  }

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
    if (myProperties == null) {
      if (myPropertyEntries == null) {
        myProperties = new HashMap();
      } else {
        int sz = myPropertyEntries.size();
        sz += sz / 3;
        myProperties = new HashMap(sz + sz / 4 + 1);
        for (Iterator i = myPropertyEntries.iterator(); i.hasNext(); ) {
          Map.Entry entry = (Map.Entry) i.next();
          myProperties.put(entry.getKey(), entry.getValue());
        }
        myPropertyEntries = null;
      }
    }
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
   * Get a property by name.
   *
   * @param name the name of the property.
   * @return the property or null if there is no property with the
   * specified name
   **/
  public Property getProperty(CompositeName name) {
    Object o = getMyProperties().get(name);
    if (o != null) {
      if (o instanceof Property) return (Property) o;
      return null;              // Fetching an invisible property
    }
    for (int i = 0, n = getChildCount(); i < n; i++) {
      Property result = 
        ((ConfigurableComponent)getChild(i)).getProperty(name);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

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
   * Gets an <code/>Iterator</code> of all properties Local to this component.
   *
   * @return an <code/>Iterator</code> of all local properties
   */
  public Iterator getLocalPropertyNames() {
    return new FilteredIterator(getPropertyNames(), localPropertyNamePredicate);
  }

  /**
   * Gets an <code/>Iterator</code> of all properties Local to this component, sorted.
   * 
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
    
  /**
   * Gets an <code/>Iterator</code> of all Property Names in this component.
   *
   * @return an <code/>Iterator</code> value
   */
  public Iterator getPropertyNames() {
    return new Iterator() {
      Iterator currentIterator = getMyProperties().keySet().iterator();
      int nextChildIndex = 0;
      CompositeName pendingName = null;
      public boolean hasNext() {
        while (pendingName == null) {
          while (!currentIterator.hasNext()) {
            if (nextChildIndex >= getChildCount()) {
              return false;
            }
            currentIterator = 
              ((ConfigurableComponent)getChild(nextChildIndex++)).getPropertyNames();
          }
          pendingName = (CompositeName) currentIterator.next();
          if (pendingName != null && getProperty(pendingName) == nullProperty) {
            pendingName = null;
          }
        }
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

  public Iterator getLocalProperties() {
    return getMyProperties().values().iterator();
  }

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
	    
	    Object nprop = currentIterator.next();
            if (nprop == nullProperty) {
              pendingProp = null;
            } else {
	      pendingProp = (Property)nprop;
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
   * Block the propagation of a child property. Such properties are
   * typically set by the parent component and should not be visible
   * from above.
   **/
  public void setPropertyVisible(Property prop, boolean newVisible) {
    CompositeName name = prop.getName();
    if (newVisible) {
      Object np = getMyProperties().get(name);
      if (nullProperty.equals(np)) {
        getMyProperties().remove(name);
        firePropertyAdded(prop);
      }
    } else {
      Object np = getMyProperties().put(name, nullProperty);
      if (!nullProperty.equals(np))
        firePropertyRemoved(prop);
    }
  }

  /**
   * Determines if the specificed property is visible.
   * If a property is not visible, it cannot be seen in the GUI.
   *
   * @param prop Property to check visiblity
   * @return a <code/>boolean</code> value
   */

  public boolean isPropertyVisible(Property prop) {
    Object np = getMyProperties().get(prop.getName());
    return !nullProperty.equals(np);
  }

  /**
   * Gets a <code/>List</code> of all Properties in this component.
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
    // Make sure we're copying apples into apples
    // The result can be a sub-class, but we want it
    // to at least have the same set of properties
    if (! (this.getClass().isAssignableFrom(result.getClass())))
      return null;
    Iterator iter = getSortedLocalPropertyNames();
    while(iter.hasNext()) {
      CompositeName name = (CompositeName)iter.next();
      Property myProp = getProperty(name);
      // compose the correct name for the property
      // name must be prepended by new society name
      String s = name.last().toString();
      ComponentName hisPropName = new ComponentName(result, s);
      Property hisProp = result.getProperty(hisPropName);
      if (hisProp == null) {
	if (log.isErrorEnabled()) {
	  log.error("Report bug 1377: Using " + CSMART.writeDebug() + " couldn't find " + hisPropName.toString() + " in " + result.getFullName().toString() + " copying from " + name.toString() + " in " + this.getFullName().toString());
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
	    log.error("Caught InvalidPropertyValueException: " + getClass().getName() + e);
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
    stream.writeInt(properties.size());
    for (Iterator i = properties.entrySet().iterator(); i.hasNext(); ) {
      PropertyEntry entry = new PropertyEntry((Map.Entry)i.next());
      stream.writeObject(entry);
    }
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
    stream.defaultReadObject();
    parent = (ComposableComponent) stream.readObject();
    children = (List) stream.readObject();
    Map properties = getMyProperties();
    int n = stream.readInt();
    myPropertyEntries = new ArrayList(n);
    for (int i = 0; i < n; i++) {
      PropertyEntry entry = (PropertyEntry)stream.readObject();
      myPropertyEntries.add(entry);
    }
    myProperties = null;
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

  public boolean hasUnboundProperties() {
    for (Iterator i = getPropertyNames(); i.hasNext(); ) {
      Property prop = (Property) getProperty((CompositeName) i.next());
      if (!prop.isValueSet()) return true;
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
   * Prints all Properties for this component.
   *
   * @param out Stream to print to.
   */
  public void printAllProperties(PrintStream out) {
    printAllProperties(out, "");
  }

  /**
   * Prints all Properties for this component.
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

  static class NullProperty implements Serializable {

    public int hashCode() {
      return 0;
    }

    public boolean equals(Object o) {
      return o instanceof NullProperty;
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

  public String toString() {
    return getShortName();
  }
}
