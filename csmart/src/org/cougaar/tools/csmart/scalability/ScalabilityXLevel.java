/* 
 * <copyright>
 *  Copyright 2001 BBNT Solutions, LLC
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
package org.cougaar.tools.csmart.scalability;

import org.cougaar.tools.csmart.ui.component.*;
import java.util.*;
import java.io.Serializable;

/**
 * ConfigurableComponent to manage the agents at a single level. Such
 * agents all have a set of properties in common plus certain unique
 * properties. Regardless, we cover all such properties with our own
 * and establish PropertyAliases for the agents to use.
 **/
public class ScalabilityXLevel
    extends ConfigurableComponent
    implements Serializable
{
    private static final long serialVersionUID = -1866337426260177284L;

    public static final String PROP_AGENTCOUNT = "Agent Count";
    public static final Object PROP_AGENTCOUNT_DFLT = new Integer(1);
    public static final String PROP_LEVEL = "Level";
    private Property propAgentCount;
    private Property propLevel;
    private ScalabilityXAgent superior;
    private int level;          // Pass level to initProperties
    private PropertiesListener childPropertiesListener = new MyPropertiesListener();
    private class PropertyRepeater implements PropertyListener {
        Property childProperty;
        public PropertyRepeater(Property childProperty) {
            this.childProperty= childProperty;
        }
        public void propertyValueChanged(PropertyEvent e) {
            Property myProp = e.getProperty();
            childProperty.setValue(myProp.getValue());
        }
        public void propertyOtherChanged(PropertyEvent e) {
            Property myProp = e.getProperty();
            switch (e.getWhatChanged()) {
            case PropertyEvent.VALUE_CHANGED:
                childProperty.setValue(myProp.getValue());
                break;
            case PropertyEvent.DEFAULTVALUE_CHANGED:
                childProperty.setDefaultValue(myProp.getDefaultValue());
                break;
            case PropertyEvent.LABEL_CHANGED:
                childProperty.setLabel(myProp.getLabel());
                break;
            case PropertyEvent.CLASS_CHANGED:
                childProperty.setPropertyClass(myProp.getPropertyClass());
                break;
            case PropertyEvent.ALLOWEDVALUES_CHANGED:
                childProperty.setAllowedValues(myProp.getAllowedValues());
                break;
            case PropertyEvent.EXPERIMENTVALUES_CHANGED:
                childProperty.setExperimentValues(myProp.getExperimentValues());
                break;
//              case PropertyEvent.TOOLTIP_CHANGED:
//                  childProperty.setToolTip(myProp.getToolTip());
//                  break;
//              case PropertyEvent.HELP_CHANGED:
//                  childProperty.setHelp(myProp.getHelp());
//                  break;
            default:
                break;
            }
        }
    }
        
    private class MyPropertiesListener
        implements PropertiesListener, ConfigurableComponentListener
    {
        public void propertyAdded(PropertyEvent e) {
            final Property p = e.getProperty();
            CompositeName pName = p.getName();
            ConfigurableComponent c = p.getConfigurableComponent();
            if (pName.size() == getFullName().size() + 2) {
                String name = pName.last().toString();
                if (!name.equals(ScalabilityXAgent.PROP_INDEX)) {
                    Property myProp = getProperty(name);
                    if (myProp == null) {
                        myProp = new PropertyAlias(ScalabilityXLevel.this, name, p);
                        addProperty(myProp);
                    } else {
                        myProp.addPropertyListener(new PropertyRepeater(p));
                        p.setValue(myProp.getValue());
                        p.setDefaultValue(myProp.getDefaultValue());
                        p.setLabel(myProp.getLabel());
                        p.setPropertyClass(myProp.getPropertyClass());
                        p.setAllowedValues(myProp.getAllowedValues());
                        p.setExperimentValues(myProp.getExperimentValues());
                        p.setToolTip(myProp.getToolTip());
                        p.setHelp(myProp.getHelp());
                    }
                }
		//                System.out.println("Hide " + p.getName());
                setPropertyVisible(p, false); // Hide this.
            }
        }
            
        public void propertyRemoved(PropertyEvent e) {}
    }

    public ScalabilityXLevel(int level, ScalabilityXAgent superior) {
        super("lvl" + level);
        this.level = level;
        this.superior = superior;
    }

    public void setCustomers(List customers) {
        for (int i = 0, n = getChildCount(); i < n; i++) {
            ScalabilityXAgent agent = (ScalabilityXAgent) getChild(i);
            agent.setSupporting(customers);
        }
    }

    public List getAgents() {
        return new ArrayList(getDescendentsOfClass(ScalabilityXAgent.class));
    }

    public void initProperties() {
        propAgentCount = addProperty(PROP_AGENTCOUNT, PROP_AGENTCOUNT_DFLT,
                                     Integer.class);
        propAgentCount.addPropertyListener(new PropertyListener() {
            public void propertyValueChanged(PropertyEvent e) {
                adjustAgentCount();
            }
            public void propertyOtherChanged(PropertyEvent e) {}
        });
        propLevel = addProperty(PROP_LEVEL, new Integer(level), Integer.class);
        adjustAgentCount();
        if (level == 0) {
            this.superior = (ScalabilityXAgent) getChild(0);
        }
    }

    private int getLevel() {
        return ((Integer) propLevel.getValue()).intValue();
    }

    public ScalabilityXAgent getSuperior() {
        return superior;
    }

    /**
     * Create or remove agents to match the new agent count property
     * value. We take over all the properties of the new agents
     * (except the PROP_INDEX property) and replace the with aliases
     * to our own properties (created as necessary). This has the
     * effect of making the agents at a level homogeneous.
     **/
    private void adjustAgentCount() {
        int newCount = ((Integer) propAgentCount.getValue()).intValue();
        if (level == 0 && newCount != 1) {
            newCount = 1;       // Force one agent at level 0
            propAgentCount.setValue(new Integer(newCount));
        }
        for (int i = getChildCount(); --i >= newCount; ) {
            removeChild(i);
        }
        for (int i = getChildCount(); i < newCount; i++) {
            ScalabilityXAgent c = new ScalabilityXAgent(getLevel(), i, superior);
            c.addPropertiesListener(childPropertiesListener);
            addChild(c);
            c.initProperties();
//              c.printAllProperties(System.out);
        }
    }
}
