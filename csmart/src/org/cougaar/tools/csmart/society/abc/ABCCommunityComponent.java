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

package org.cougaar.tools.csmart.society.abc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.tools.csmart.core.property.ConfigurableComponent;
import org.cougaar.tools.csmart.core.property.range.StringRange;
import org.cougaar.tools.csmart.core.property.range.IntegerRange;
import org.cougaar.tools.csmart.core.property.name.CompositeName;

/**
 * <PRE/>
 * Define a community with the following properties:
 * TaskType: type (verb) of task that this community processes
 * NumberOfAgents: number of agents in this community
 * </PRE>
 */
public class ABCCommunityComponent extends ConfigurableComponent {
  static final long serialVersionUID = 3635872801642632859L;

  private static final String TASK_TYPE = "TaskType";
  private static final String NUMBER_OF_AGENTS = "NumberOfAgents";
  private static final Set taskChoices = new HashSet();
  private static final Set agentChoices = new HashSet();
  static {
    taskChoices.add(new StringRange("transport"));
    taskChoices.add(new StringRange("supply"));
    taskChoices.add(new StringRange("feed"));
    agentChoices.add(new IntegerRange(1, 4));
  }
  private Object[][] propertyInfo = {
    {TASK_TYPE,        String.class,  "Type of Tasks",    "supply",       taskChoices},
    {NUMBER_OF_AGENTS, Integer.class, "Number of Agents", new Integer(4), agentChoices}
  };

  /**
   * Define properties and set default and allowed values.
   */
  public ABCCommunityComponent(String name) {
    super(name);
  }

  public void initProperties() {
    for (int i = 0; i < propertyInfo.length; i++) {
      Object[] info = propertyInfo[i];
      //      Property p = addProperty((String) info[0], null);
      Property p = addProperty((String) info[0], info[3]);
      p.setPropertyClass((Class) info[1]);
      p.setLabel((String) info[2]);
      p.setDefaultValue(info[3]);
      p.setAllowedValues((Set) info[4]);
    }
  }

  public static void main(String[] args) {
    ABCCommunityComponent abc = new ABCCommunityComponent("Community");
    abc.initProperties();
    List props = abc.getPropertyNamesList();
    int n = props.size();
    System.out.println("Number of properties: " + n);
    for (int i = 0; i < n; i++) {
      CompositeName name = (CompositeName) props.get(i);
      System.out.println("Key: " + name);
      Property prop = abc.getProperty(name);
      System.out.println("Name: " + prop.getName());
      System.out.println("Label: " + prop.getLabel());
      System.out.println("Class: " + prop.getClass());
      System.out.println("Value: " + prop.getValue());
      System.out.println("Default: " + prop.getDefaultValue());
      System.out.println("Allowed Values: " + prop.getAllowedValues());
    }
  }
}

