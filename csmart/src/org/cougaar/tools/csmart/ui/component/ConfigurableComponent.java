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

package org.cougaar.tools.csmart.ui.component;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.event.EventListenerList;
import org.cougaar.util.FilteredIterator;
import org.cougaar.util.UnaryPredicate;

/**
 * For each property, define:
 * name, label, default value, current value, allowed values, experiment values
 * This is the default configurable component implementation,
 * it can be overridden by specific components.
 */

public abstract class ConfigurableComponent
  implements ComposableComponent, ComponentProperties, ConfigurableComponentListener
{
  private static final long serialVersionUID = -7727291298618568087L;

  private transient Map myProperties = new HashMap();
  private transient Collection myPropertyEntries = null;
  private List children = null; // Our children, if any
  private Set blockedProperties = new HashSet();
  private ConfigurableComponent parent; // Our parent, if any
  private static int nameCount = 0;
  private ComponentName myName;
  private boolean nameChangeMark = false;
  private static Object nullProperty = new NullProperty();

  private transient EventListenerList listeners = null;

  private PropertiesListener myPropertiesListener = new MyPropertiesListener();

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
  };

  protected EventListenerList getEventListenerList() {
    if (listeners == null) listeners = new EventListenerList();
    return listeners;
  }

  /**
   * Add a PropertiesListener. PropertiesListeners are invoked
   * whenever properties are added or removed.
   **/
  public void addPropertiesListener(PropertiesListener l) {
    getEventListenerList().add(PropertiesListener.class, l);
  }

  public void removePropertiesListener(PropertiesListener l) {
    getEventListenerList().remove(PropertiesListener.class, l);
  }

  private void firePropertyAdded(Property p) {
    if (listeners != null) {
      firePropertyAdded(new PropertyEvent(p, PropertyEvent.PROPERTY_ADDED));
    }
  }

  protected void firePropertyAdded(PropertyEvent ev) {
    PropertiesListener[] ls =
      (PropertiesListener[]) getEventListenerList().getListeners(PropertiesListener.class);
    for (int i = 0; i < ls.length; i++) {
      ls[i].propertyAdded(ev);
    }
  }

  private void firePropertyRemoved(Property p) {
    firePropertyRemoved(new PropertyEvent(p, PropertyEvent.PROPERTY_ADDED));
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
  }

  public abstract void initProperties();

  protected ConfigurableComponent getAncestorOfClass(Class cls) {
    ConfigurableComponent parent = getParent();
    if (parent == null) return null;
    if (cls.isInstance(parent)) return parent;
    return parent.getAncestorOfClass(cls);
  }

  public Collection getDescendentsOfClass(Class cls, Collection c) {
    for (int i = 0, n = getChildCount(); i < n; i++) {
      ConfigurableComponent child = getChild(i);
      child.getDescendentsOfClass(cls, c);
      if (cls.isInstance(child)) c.add(child);
    }
    return c;
  }
        
  public Collection getDescendentsOfClass(Class cls) {
    return getDescendentsOfClass(cls, new ArrayList());
  }

  /**
   * Set the name of this component. The name is relative to the
   * parent and must be distinct in that context.
   * @param newName the new name for this component.
   **/
  public void setName(String newName) {
    startNameChange();
    myName.setName(new SimpleName(newName));
    finishNameChange();
  }

  //  public CompositeName getName() {
  //    return myName;
  //  }

  public String getShortName() {
    return getFullName().last().toString();
  }

  public CompositeName getFullName() {
    return myName;
  }


  /**
   * Add a child to this component.
   * @param c the child to add
   * @param propertyMap maps child property names to prope
   **/
  public int addChild(ConfigurableComponent c) {
    if (children == null) children = new ArrayList();
    c.addPropertiesListener(myPropertiesListener);
    children.add(c);
    c.setParent(this);
    for (Iterator i = c.getPropertyNames(); i.hasNext(); ) {
      CompositeName name = (CompositeName) i.next();
      Property prop = getProperty(name);
      if (prop != null) firePropertyAdded(prop);
    }
    return children.size() - 1;
  }

  public void removeChild(int childIndex) {
    removeChild(getChild(childIndex));
  }

  public void removeChild(ConfigurableComponent c) {
    if (children == null || c.getParent() != this) return;
    c.removePropertiesListener(myPropertiesListener);
    for (Iterator i = c.getPropertyNames(); i.hasNext(); ) {
      CompositeName name = (CompositeName) i.next();
      Property prop = getProperty(name);
      if (prop != null) firePropertyRemoved(prop);
    }
    children.remove(c);
    c.setParent(null);
  }

  public void removeAllChildren() {
    while(getChildCount() != 0) {
      removeChild(getChildCount() - 1);
    }
  }

  public int getChildCount() {
    if (children == null) return 0;
    return children.size();
  }

  public ConfigurableComponent getChild(int n) {
    return (ConfigurableComponent) children.get(n);
  }

  public ConfigurableComponent getChild(CompositeName childName) {
    ConfigurableComponent cc = null;

    for(int i=0; i < getChildCount(); i++) {
      cc = getChild(i);
      if(cc.getFullName().equals(childName)){
	break;
      }
    }

    return cc;
  }

  /**
   * Changing our parent changes all our property names so we have to
   * rehash them
   **/
  public void setParent(ConfigurableComponent newParent) {
    ConfigurableComponent oldParent = parent;
    startNameChange();
    parent = newParent;
    myName.setComponent(parent);
    finishNameChange();
    if (oldParent != null) oldParent.fireChildConfigurationChanged();
    if (newParent != null) newParent.fireChildConfigurationChanged();
  }

  /**
   * Add a ChildConfigurationListener. ChildConfigurationListener are
   * invoked whenever children are added or removed.
   **/
  public void addChildConfigurationListener(ChildConfigurationListener l) {
    getEventListenerList().add(ChildConfigurationListener.class, l);
  }

  public void removeChildConfigurationListener(ChildConfigurationListener l) {
    getEventListenerList().remove(ChildConfigurationListener.class, l);
  }

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
      getChild(i).startNameChange();
    }
    if (parent != null) parent.startNameChange();
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
      getChild(i).finishNameChange();
    }
    if (parent != null) parent.finishNameChange();
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

  public ConfigurableComponent getParent() {
    return parent;
  }

  /**
   * Get a property by name.
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
      Property result = getChild(i).getProperty(name);
      if (result != null) return result;
    }
    return null;
  }

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
    return addProperty(name, value, value.getClass());
  }

  public Property addProperty(String name, Object value, Class cls) {
    Property result = addProperty(new ConfigurableComponentProperty(this, name, value));
    result.setPropertyClass(cls);
    return result;
  }

  public Property addProperty(Property p) {
    getMyProperties().put(p.getName(), p);
    firePropertyAdded(p);
    return p;
  }

  public Iterator getLocalPropertyNames() {
    return new FilteredIterator(getPropertyNames(), localPropertyNamePredicate);
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
    
  public Iterator getPropertyNames() {
    for (Iterator i = getMyProperties().keySet().iterator(); i.hasNext(); ) {
      Object key = i.next();
      Object val = getMyProperties().get(key);
    }
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
            currentIterator = getChild(nextChildIndex++).getPropertyNames();
          }
          pendingName = (CompositeName) currentIterator.next();
          if (pendingName != null && getProperty(pendingName) == null) {
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

  /**
   * Block the propagation of a child property. Such properties are
   * typically set by the parent component and should not be visible
   * from above.
   **/
  public void setPropertyVisible(Property prop, boolean newVisible) {
    CompositeName name = prop.getName();
    if (newVisible) {
      Object np = getMyProperties().remove(name);
      if (nullProperty.equals(np))
        firePropertyAdded(prop);
    } else {
      Object np = getMyProperties().put(name, nullProperty);
      if (!nullProperty.equals(np))
        firePropertyRemoved(prop);
    }
  }

  public boolean isPropertyVisible(Property prop) {
    Object np = getMyProperties().get(prop.getName());
    return !nullProperty.equals(np);
  }

  public List getPropertyNamesList() {
    List props = new ArrayList();
    for (Iterator i = getPropertyNames(); i.hasNext(); ) {
      CompositeName name = (CompositeName) i.next();
      props.add(name);
    }
    return props;
  }

  private void writeObject(ObjectOutputStream stream)
     throws IOException
  {
    stream.defaultWriteObject();
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
    Map properties = getMyProperties();
    int n = stream.readInt();
    myPropertyEntries = new ArrayList(n);
    for (int i = 0; i < n; i++) {
      PropertyEntry entry = (PropertyEntry)stream.readObject();
      myPropertyEntries.add(entry);
    }
    myProperties = null;
    setSerializableListeners((List) stream.readObject());
  }

  private void setSerializableListeners(List l) {
    EventListenerList ll = getEventListenerList();
    for (Iterator i = l.iterator(); i.hasNext(); ) {
      Class cls = (Class) i.next();
      EventListener listener = (EventListener) i.next();
      ll.add(cls, listener);
    }
  }

  public void printLocalProperties(PrintStream out) {
    printLocalProperties(out, "");
  }

  public void printLocalProperties(PrintStream out, String indent) {
    List props = new ArrayList();
    for (Iterator iterator = getLocalPropertyNames(); iterator.hasNext(); )
      props.add(iterator.next());
    printProperties(out, indent, props);
  }

  public void printAllProperties(PrintStream out) {
    printAllProperties(out, "");
  }

  public void printAllProperties(PrintStream out, String indent) {
    printProperties(out, "", getPropertyNamesList());
  }

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
