/* 
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 * 
 *       © Copyright 2001 by BBNT Solutions LLC.
 * </copyright>
 */
package org.cougaar.tools.csmart.ui.component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Iterator;
import java.util.Collections;
import java.net.URL;
import java.io.*;

public abstract class PropertyBase implements Property {
    private transient List listeners = null;
    ConfigurableComponent component;
    private String tooltip;
    private URL help;

    protected PropertyBase(ConfigurableComponent c) {
        component = c;
    }

    public final ConfigurableComponent getConfigurableComponent() {
        return component;
    }
    public abstract CompositeName getName();
    public abstract Class getPropertyClass();
    public abstract void setPropertyClass(Class c);
    public abstract String getLabel();
    public abstract void setLabel(String label);
    public abstract Object getDefaultValue();
    public abstract void setDefaultValue(Object defaultValue);
    public abstract Object getValue();
    public abstract void setValue(Object value);
    public abstract Set getExperimentValues();
    public abstract void setExperimentValues(Set experimentValues);
    public abstract Set getAllowedValues();
    public abstract void setAllowedValues(Set allowedValues);
    public abstract boolean isValueSet();
    public String getToolTip() {
        return tooltip;
    }

    public Property setToolTip(String tt) {
        tooltip = tt;
        return this;
    }

    public URL getHelp() {
        return help;
    }

    public Property setHelp(URL url) {
        help = url;
        return this;
    }

    public void addPropertyListener(PropertyListener l) {
        if (listeners == null) listeners = new ArrayList();
        listeners.add(l);
    }

    public void removePropertyListener(PropertyListener l) {
        if (listeners == null) return;
        listeners.remove(l);
    }

    public Iterator getPropertyListeners() {
        if (listeners == null) return Collections.EMPTY_SET.iterator();
        return listeners.iterator();
    }

    protected boolean haveListeners() {
        return listeners != null && listeners.size() > 0;
    }

    protected void fireValueChanged(Object oldValue) {
        if (listeners != null) {
            PropertyEvent ev = new PropertyEvent(this, PropertyEvent.VALUE_CHANGED, oldValue);
            // Use array in case listeners remove themselves
            PropertyListener[] ls =
                (PropertyListener[]) listeners.toArray(new PropertyListener[listeners.size()]);
            for (int i = 0; i < ls.length; i++) {
                ls[i].propertyValueChanged(ev);
            }
        }
    }

    protected void fireOtherChanged(Object old, int whatChanged) {
        if (listeners != null) {
            PropertyEvent ev = new PropertyEvent(this, whatChanged, old);
            // Use array in case listeners remove themselves
            PropertyListener[] ls =
                (PropertyListener[]) listeners.toArray(new PropertyListener[listeners.size()]);
            for (int i = 0; i < ls.length; i++) {
                ls[i].propertyOtherChanged(ev);
            }
        }
    }

    private void writeObject(ObjectOutputStream stream)
        throws IOException
    {
        stream.defaultWriteObject();
        stream.writeObject(getSerializableListeners(listeners));
    }

    private List getSerializableListeners(List listeners) {
        List result = null;
        if (listeners != null) {
            result = new ArrayList(listeners.size());
            for (int i = 0, n = listeners.size(); i < n; i++) {
                Object o = listeners.get(i);
                if (o instanceof ConfigurableComponentListener) result.add(o);
            }
        }
        return result;
    }

    private void readObject(ObjectInputStream stream)
        throws IOException, ClassNotFoundException
    {
        stream.defaultReadObject();
        listeners = (List) stream.readObject();
    }

    public void printProperty(PrintStream out) {
        printProperty(out, "");
    }

    public void printProperty(PrintStream out, String indent) {
        out.println(indent + "Name: " + getName()); 
        out.println(indent + "Label: " + getLabel());
        out.println(indent + "Class: " + getPropertyClass());
        out.println(indent + "Value: " + getValue());
        out.println(indent + "Default: " + getDefaultValue());
        out.println(indent + "Allowed Values: " + getAllowedValues());
        out.println(indent + "Experiment Values: " + getExperimentValues());
    }
}
