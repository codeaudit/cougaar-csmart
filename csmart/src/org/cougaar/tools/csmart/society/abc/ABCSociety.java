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
package org.cougaar.tools.csmart.society.abc;

import org.cougaar.tools.csmart.Constants;
import org.cougaar.tools.csmart.core.property.ModifiableConfigurableComponent;
import org.cougaar.tools.csmart.core.property.ConfigurableComponentPropertyAdapter;
import org.cougaar.tools.csmart.core.property.BaseComponent;
import org.cougaar.tools.csmart.core.property.PropertiesListener;
import org.cougaar.tools.csmart.core.property.PropertyEvent;
import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.tools.csmart.core.property.ModificationListener;
import org.cougaar.tools.csmart.core.property.ModificationEvent;
import org.cougaar.tools.csmart.society.AgentComponent;
import org.cougaar.tools.csmart.experiment.NodeComponent;
import org.cougaar.tools.csmart.experiment.HostComponent;
import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.society.SocietyBase;

import java.io.FileFilter;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JOptionPane;

public class ABCSociety
  extends SocietyBase
  implements Serializable
{

  /** Property Definition for Community Count **/
  public static final String PROP_COMMUNITYCOUNT = "Community Count";
  public static final Integer PROP_COMMUNITYCOUNT_DFLT = new Integer(5);
  public static final String PROP_COMMUNITYCOUNT_DESC = "Number of Communities in the Society";

  /** Property Definition for Level Count **/
  public static final String PROP_LEVELCOUNT = "Level Count";
  public static final Integer PROP_LEVELCOUNT_DFLT = new Integer(3);
  public static final String PROP_LEVELCOUNT_DESC = "Number of Levels in the Society";

  public static final String[] PROP_TASKVERB_DFLT = {"Supply500MREs"};
  
  /** Properties defined in other files **/
  public static final String PROP_TASKVERB = ABCTask.PROP_TASKVERB;
  public static final String PROP_STARTTIME = ABCAgent.PROP_STARTTIME;
  public static final String PROP_STOPTIME = ABCAgent.PROP_STOPTIME;

  // Initialize a default society of 8 communities.
  private static final String[] DEMAND_INIT 
            = {"5", "3", "4", "2", "2", "2", "2", "2"};
  private static final String[] PRODUCTION_INIT 
            = {"20", "20", "20", "20", "20", "20", "60", "400"};
  private static final String[] LATITUDE_INIT 
            = {"25", "27", "29", "26", "28", "25", "27", "29"};
  private static final String[] LONGITUDE_INIT 
            = {"52", "52", "52", "75", "75", "100", "100", "100"};

  private Property propCommunityCount;
  private Property propLevelCount;
  private Property propStartTime;
  private Property propStopTime;
  private Property propTaskVerb;

  private HashMap  commPerLevel = new HashMap(25);

  /**
   * Creates a new <code>ABCSociety</code> instance.
   *
   */
  public ABCSociety() {
    this("ABC Society");
  }

  /**
   * Creates a new <code>ABCSociety</code> instance.
   *
   * @param name Name of this component
   */
  public ABCSociety(String name) {
    super(name);

    // Init the CSMART Constants file to get Roles
    Constants.Role.init();
  }

  /**
   * Initializes all properties
   */
  public void initProperties() {

    propLevelCount = addProperty(PROP_LEVELCOUNT, PROP_LEVELCOUNT_DFLT,
                                 new ConfigurableComponentPropertyAdapter() {
	public void propertyValueChanged(PropertyEvent e) {
	  if(((Integer)e.getProperty().getValue()).intValue() < 1) {
	    propLevelCount.setValue(e.getPreviousValue());
	  } else {
	    changeSociety();
	  }
	}
    });
    propLevelCount.setToolTip(PROP_LEVELCOUNT_DESC);

    propCommunityCount = addProperty(PROP_COMMUNITYCOUNT, PROP_COMMUNITYCOUNT_DFLT,
                                     new ConfigurableComponentPropertyAdapter() {
      	public void propertyValueChanged(PropertyEvent e) {
	  if(((Integer)e.getProperty().getValue()).intValue() < 1) {
	    propCommunityCount.setValue(e.getPreviousValue());
	  } else if( ((Integer)e.getProperty().getValue()).intValue() < ((Integer)propLevelCount.getValue()).intValue()) {
	    // Community count should be equal to or greater than level count.
	    propCommunityCount.setValue(e.getPreviousValue());
	  } else {
	    changeSociety();
	  }
	}
    });
    propCommunityCount.setToolTip(PROP_COMMUNITYCOUNT_DESC);

    propStartTime = addProperty(PROP_STARTTIME, ABCAgent.PROP_STARTTIME_DFLT,
                                new ConfigurableComponentPropertyAdapter() {
	public void propertyValueChanged(PropertyEvent e) {
	}
    });
    propStartTime.setToolTip(ABCAgent.PROP_STARTTIME_DESC);

    propStopTime = addProperty(PROP_STOPTIME, ABCAgent.PROP_STOPTIME_DFLT,
                                new ConfigurableComponentPropertyAdapter() {
	public void propertyValueChanged(PropertyEvent e) {
	}
    });
    propStopTime.setToolTip(ABCAgent.PROP_STOPTIME_DESC);

    propTaskVerb = addProperty(PROP_TASKVERB, PROP_TASKVERB_DFLT);
    propTaskVerb.setToolTip(ABCTask.PROP_TASKVERB_DESC);

    buildDefaultCommunity();
  }
  
  private Property addPropertyAlias(ABCCommunity c, Property prop, String name) {
    Property childProp = c.addAliasProperty(prop, name);
    setPropertyVisible(childProp, false);
    return childProp;
  }

  /**
   * Returns the number of levels in the society
   *
   * @param int The number of levels
   */
  private int getLevelCount() {
    return ((Integer) getProperty(PROP_LEVELCOUNT).getValue()).intValue();
  }

  /**
   * Returns the number of communities in the society
   *
   * @param int The number of communities
   */
  private int getCommunityCount() {
    return ((Integer) getProperty(PROP_COMMUNITYCOUNT).getValue()).intValue();
  }

  /** 
   * calcCommunitiesPerLevel
   * 
   * The formula used to calculate the number of communities per layer is: <br>
   *  middle = (comm - factor) / levels                                     <br>
   * Where:                                                                <br>
   *  comm = Number of Communities.                                        <br>
   *  levels = Number of Levels.                                             <br>
   *  factor = <b>levels - 1</b> if levels is odd, <b>levels - 2</b> if levels is even.       <br>
   *  middle = Number of Middle Nodes per level                            <br>
   * <br>
   * If middle is < 0, it is rounded up to 1.
   * <br>
   * If there are any extra nodes, they are added to the bottom.
   * This function will create an approximate hourglass society.
   */
  private void calcCommunitiesPerLevel() {

    // Calculate the Number of middle nodes.
    int comm = getCommunityCount();
    int levels = getLevelCount();
    int factor = (levels % 2 == 0) ? (levels - 2) : (levels - 1);

    // Handle boundry case.
    if(comm == 1 || levels == 1) {
      commPerLevel.put(new Integer(1), new Integer(comm));
    } else if( levels < 3 ) {
      // For this case, put half on each level.
      commPerLevel.put(new Integer(0), new Integer(comm / 2));
      commPerLevel.put(new Integer(1), new Integer(comm - (comm / 2)));
    } else if( comm < levels ) {
      // This should be caught at the property level.
    } else {

      float value = (float)(comm - factor) / (float)levels;
      int middle = (int)Math.ceil(value);

      int top;
      int bottom;

      if(levels > 3) {
	top = (comm - ((levels - 2) * middle)) / 2;
      } else {
	top = (comm - middle) / 2;
      }

      // If there is a node left over, add it to the bottom level.
      if((top * 2) + (middle * (levels - 2)) == comm) {
	bottom = top;
      } else {
	bottom = top + 1;
      }

      for(int i=1; i <= levels; i++) {
	if(i == 1) {
	  commPerLevel.put(new Integer(i), new Integer(top));
	} else if( i == levels ) {
	  commPerLevel.put(new Integer(i), new Integer(bottom));
	} else {
	  commPerLevel.put(new Integer(i), new Integer(middle));
	}
      }
    }
  }

  /**
   * Redefines a society when the community or level count changes
   */
  private void changeSociety() {

    int newLevels = getLevelCount();
    int newCommunities = getCommunityCount();
    int crntCommunity = 0;
    String[] externalProviders = null;

    // For now, when a value is changed, we redefine the entire society
    // because of that we need to start fresh every time.  Not the most
    // efficient, but it works.
    while(getChildCount() != 0) {
      removeChild(getChildCount() - 1);
    }

    calcCommunitiesPerLevel();

    for(int i=1; i <= newLevels; i++) {
      int count = 0;
      int communitiesForLevel = ((Integer) commPerLevel.get(new Integer(i))).intValue();
      while(count < communitiesForLevel) {
	ABCCommunity child = new ABCCommunity(i, crntCommunity);
	addChild(child);
	child.addPropertiesListener(this);
	addPropertyAlias(child, propTaskVerb, PROP_TASKVERB);
	addPropertyAlias(child, propStartTime, PROP_STARTTIME);
	addPropertyAlias(child, propStopTime, PROP_STOPTIME);

	child.initProperties();
	child.addAgents();

	externalProviders = new String[getChildCount()];
	int index = 0;
	Iterator iter = ((Collection)getDescendentsOfClass(ABCCommunity.class)).iterator();
	while(iter.hasNext()) {
	  ABCCommunity comm = (ABCCommunity)iter.next();
	  String provider = comm.getExternalProviderName();
	  int level = comm.getLevel();
	  if(level == (child.getLevel() - 1)) {
	    externalProviders[index++] = provider;
	  } else {
            if(log.isDebugEnabled()) {
              log.debug("Skipping: " + provider + 
                        ": My Level = " + child.getLevel() + ": " + level);
            }
	  } 
	}

	if(externalProviders != null) {
	  setSupplies(child, externalProviders);
	}
	count++;
	crntCommunity++;
      }
    }
  }

  /**
   * Constructs a default ABC Society
   */
  private void buildDefaultCommunity() {
    int crntCommunity = 0;
    String[] externalProviders = null;

    calcCommunitiesPerLevel();

    for(int i=1; i <= getLevelCount(); i++) {
      int count = 0;
      int communitiesForLevel = ((Integer) commPerLevel.get(new Integer(i))).intValue();
      while(count < communitiesForLevel) {
	ABCCommunity child = new ABCCommunity(i, crntCommunity);
	addChild(child);
	child.addPropertiesListener(this);
	addPropertyAlias(child, propTaskVerb, PROP_TASKVERB);
	addPropertyAlias(child, propStartTime, PROP_STARTTIME);
	addPropertyAlias(child, propStopTime, PROP_STOPTIME);
	child.initProperties();
        child.getProperty(ABCCommunity.PROP_DEMAND).setValue(
                                 new Integer(DEMAND_INIT[crntCommunity]));
        child.getProperty(ABCCommunity.PROP_PRODUCTION).setValue(
                                 new Integer(PRODUCTION_INIT[crntCommunity]));
        child.getProperty(ABCCommunity.PROP_LATITUDE).setValue(
                                 new Float(LATITUDE_INIT[crntCommunity]));
        child.getProperty(ABCCommunity.PROP_LONGITUDE).setValue(
                                 new Float(LONGITUDE_INIT[crntCommunity]));
	child.addAgents();

	externalProviders = new String[getChildCount()];
	int index = 0;
	Iterator iter = ((Collection)getDescendentsOfClass(ABCCommunity.class)).iterator();
	while(iter.hasNext()) {
	  ABCCommunity comm = (ABCCommunity)iter.next();
	  String provider = comm.getExternalProviderName();
	  int level = comm.getLevel();
	  if(level == (child.getLevel() - 1)) {
	    externalProviders[index++] = provider;
	  }
	}

	if(externalProviders != null) {
	  setSupplies(child, externalProviders);
	}
	count++;
	crntCommunity++;
      }
    }    
  }

  /**
   * Adds all supplies to a community
   */
  private void setSupplies(ABCCommunity c, String[] supp) {
    int count = 0;
    // Need the size supp.  The array is larger than # elements.
    for(int i=0; i < supp.length; i++) {
      if(supp[i] != null) {
	count++;
      }
    }

    Iterator iter = ((Collection)c.getDescendentsOfClass(ABCAgent.class)).iterator();
    while(iter.hasNext()) {
      ABCAgent agent = (ABCAgent)iter.next();
      if(agent.getFullName().get(2).toString().equals("Provider2")) {
	String[] supplies = (String[])agent.getProperty(ABCAgent.PROP_SUPPLIES).getValue();
	String[] newSupplies = new String[supplies.length + count];
	for(int x=0; x < supplies.length; x++) {
	  newSupplies[x] = supplies[x];
	}
	for(int x=0, y=supplies.length; x < supp.length; x++, y++) {	  
	  if(supp[x] != null) {
	    newSupplies[y] = supp[x];
	  }
	}
	agent.getProperty(ABCAgent.PROP_SUPPLIES).setValue(newSupplies);
	break;
      }
    }
  }

  /**
   * Returns all the agents in this society.
   *
   * @return an <code>AgentComponent[]</code> value
   */
  public AgentComponent[] getAgents() {
    Collection communities = getDescendentsOfClass(ABCCommunity.class);
    Iterator iter = communities.iterator();
    ArrayList agents = new ArrayList();
    while(iter.hasNext()) {
      ABCCommunity c = (ABCCommunity)iter.next();
      agents.addAll(c.getDescendentsOfClass(ABCAgent.class));
    }

    return (AgentComponent[]) agents.toArray(new AgentComponent[agents.size()]);
  }

  /**
   * Copies the society component.
   *
   * @param result society component to copy
   * @return a <code>ComponentProperties</code> value
   */
  public BaseComponent copy(BaseComponent result) {
    result = super.copy(result);
    // Try to get the PROP_SUPPLIES and PROP_INITIALIZER properties
    // reset to have the new society name in them
    if (result instanceof ABCSociety)
      ((ABCSociety)result).changeSociety();

    return result;
  }

  /**
   * Adds the all the ComponentData for this society.
   *
   * @param data 
   * @return a <code>ComponentData</code> value
   */
  public ComponentData addComponentData(ComponentData data) {
    ComponentData[] children = data.getChildren();

    for(int i=0; i < children.length; i++) {
      ComponentData child = children[i];
      if(child.getType() == ComponentData.AGENT) {

	Iterator iter = ((Collection)getDescendentsOfClass(ABCAgent.class)).iterator();

	while(iter.hasNext()) {
	  ABCAgent agent = (ABCAgent)iter.next();
	  if(child.getName().equals(agent.getFullName().toString())) {
	    child.setOwner(this);
	    agent.addComponentData(child);
	  }
	}		
      } else {
	// Process it's children.
	addComponentData(child);
      }      
    }

    return data;
  }
}
