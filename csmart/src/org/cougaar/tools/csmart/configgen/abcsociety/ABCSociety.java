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
package org.cougaar.tools.csmart.configgen.abcsociety;

import org.cougaar.tools.csmart.Constants;
import org.cougaar.tools.csmart.scalability.ScalabilityMetricsFileFilter;
import org.cougaar.tools.csmart.ui.component.*;
import org.cougaar.tools.csmart.ui.viewer.Organizer;

import org.cougaar.tools.server.ConfigurationWriter;

import java.io.File;
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
  extends ModifiableConfigurableComponent
  implements PropertiesListener, Serializable, SocietyComponent
{
  private static final String DESCRIPTION_RESOURCE_NAME = "description.html";
  private static final String BACKUP_DESCRIPTION =
    "ABCSociety description not available";

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

  // Props for metrics:
  public static final String PROP_NUMBPROVIDERS = ABCAgent.PROP_NUMBPROVIDERS;
  public static final String PROP_SAMPLEINTERVAL = ABCAgent.PROP_SAMPLEINTERVAL;
  public static final String PROP_STARTDELAY = ABCAgent.PROP_STARTDELAY;
  public static final String PROP_MAXNUMBSAMPLES = ABCAgent.PROP_MAXNUMBSAMPLES;
  public static final String PROP_INITIALIZER = ABCAgent.PROP_INITIALIZER;
  // End of props for metrics

  
  private boolean isRunning = false;
  private boolean editable = true;

  private Property propCommunityCount;
  private Property propLevelCount;
  private Property propStartTime;
  private Property propStopTime;
  private Property propTaskVerb;

  // Props for metrics
  private Property propInitializer;
  private Property propSampleInterval;
  private Property propNumbProviders;
  private Property propStartDelay;
  private Property propMaxSamples;
  // End of props for metrics

  private HashMap  commPerLevel = new HashMap(25);

  private List hosts = new ArrayList();
  private List nodes = new ArrayList();

  // FileFilter for metrics:
  private static FileFilter metricsFileFilter = new ScalabilityMetricsFileFilter();

  private Property addProperty(String name, Object value, PropertyListener l) {
    Property p = addProperty(name, value, value.getClass());
    p.addPropertyListener(l);
    return p;
  }

  public ABCSociety() {
    this("ABC Society");
  }

  public ABCSociety(String name) {
    super(name);

    // Init the CSMART Constants file to get Roles
    Constants.Role.init();
  }

  /**
   * Returns the name of this Society
   *
   * @return Society Name
   */
  public String getSocietyName() {
    return getShortName();
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
	    //JOptionPane.showMessageDialog(this, "Value must be > 0; reseting to previous value.");
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
	    //JOptionPane.showMessageDialog(this, "Value must be > 0; reseting to previous value.");
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

    // Stuff for metrics:
    propInitializer = addProperty(PROP_INITIALIZER, ABCAgent.PROP_INITIALIZER_DFLT,
                                new ConfigurableComponentPropertyAdapter() {
	public void propertyValueChanged(PropertyEvent e) {
	}
    });
    propInitializer.setToolTip(ABCAgent.PROP_INITIALIZER_DESC);

    propSampleInterval = addProperty(PROP_SAMPLEINTERVAL, ABCAgent.PROP_SAMPLEINTERVAL_DFLT,
                                new ConfigurableComponentPropertyAdapter() {
	public void propertyValueChanged(PropertyEvent e) {
	}
    });
    propSampleInterval.setToolTip(ABCAgent.PROP_SAMPLEINTERVAL_DESC);

    propMaxSamples = addProperty(PROP_MAXNUMBSAMPLES, ABCAgent.PROP_MAXNUMBSAMPLES_DFLT,
                                new ConfigurableComponentPropertyAdapter() {
	public void propertyValueChanged(PropertyEvent e) {
	}
    });
    propMaxSamples.setToolTip(ABCAgent.PROP_MAXNUMBSAMPLES_DESC);

    propStartDelay = addProperty(PROP_STARTDELAY, ABCAgent.PROP_STARTDELAY_DFLT,
                                new ConfigurableComponentPropertyAdapter() {
	public void propertyValueChanged(PropertyEvent e) {
	}
    });
    propStartDelay.setToolTip(ABCAgent.PROP_STARTDELAY_DESC);

    propNumbProviders = addProperty(PROP_NUMBPROVIDERS, ABCAgent.PROP_NUMBPROVIDERS_DFLT,
                                new ConfigurableComponentPropertyAdapter() {
	public void propertyValueChanged(PropertyEvent e) {
	}
    });
    propNumbProviders.setToolTip(ABCAgent.PROP_NUMBPROVIDERS_DESC);
    // End of stuff for metrics

    propTaskVerb = addProperty(PROP_TASKVERB, PROP_TASKVERB_DFLT);
    propTaskVerb.setToolTip(ABCTask.PROP_TASKVERB_DESC);

    buildDefaultCommunity();

    // For metrics:
    addInitializer();
  }
  
  /**
   * Called when a new property has been added to the
   * society. 
   *
   * @param PropertyEvent Event for the new property
   */
  public void propertyAdded(PropertyEvent e) {
    Property addedProperty = e.getProperty();
    Property myProperty = getProperty(addedProperty.getName().last().toString());
    if (myProperty != null) {
      setPropertyVisible(addedProperty, true);
    }
  }

  /**
   * Called when a property has been removed from the society
   */
  public void propertyRemoved(PropertyEvent e) {}

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
      //      System.out.println("Count = " + count + " Communities for level: " + communitiesForLevel);
      while(count < communitiesForLevel) {
	//	System.out.println("Level == " + i);
	ABCCommunity child = new ABCCommunity(i, crntCommunity);
	addChild(child);
	child.addPropertiesListener(this);
	addPropertyAlias(child, propTaskVerb, PROP_TASKVERB);
	// Stuff for metrics:
	addPropertyAlias(child, propSampleInterval, PROP_SAMPLEINTERVAL);
	addPropertyAlias(child, propMaxSamples, PROP_MAXNUMBSAMPLES);
	addPropertyAlias(child, propStartDelay, PROP_STARTDELAY);
	addPropertyAlias(child, propNumbProviders, PROP_NUMBPROVIDERS);
	// End of stuff for metrics
	addPropertyAlias(child, propStartTime, PROP_STARTTIME);
	addPropertyAlias(child, propStopTime, PROP_STOPTIME);

	child.initProperties();
	child.addAgents();

	externalProviders = new String[getChildCount()];
	int index = 0;
	Iterator iter = ((Collection)getDescendentsOfClass(ABCCommunity.class)).iterator();
	//	System.out.println("Community: " + child.getName().toString());
	while(iter.hasNext()) {
	  ABCCommunity comm = (ABCCommunity)iter.next();
	  String provider = comm.getExternalProviderName();
	  int level = comm.getLevel();
	  if(level == (child.getLevel() - 1)) {
	    //	    System.out.println("Adding: " + provider);
	    externalProviders[index++] = provider;
	  } else {
	    //	    System.out.println("Skipping: " + provider + ": My Level = " + child.getLevel() + ": " + level);
	  } 
	}

	if(externalProviders != null) {
	  setSupplies(child, externalProviders);
	}

	// Stuff for metrics:
	if(i == 1 && count == 0 ) {
	  propInitializer.setValue(child.getProperty(ABCCommunity.PROP_FIRSTAGENT).getValue());
	}
	// Hide this value from the GUI, it should never been seen by the user.
	setPropertyVisible(child.getProperty(ABCCommunity.PROP_FIRSTAGENT), false);
	// End of stuff for metrics

	count++;
	crntCommunity++;
      }
    }
    // These are for metrics:
    propNumbProviders.setValue(new Long(getCommunityCount() * 4L));
    setPropertyVisible(propInitializer, false);
    addInitializer();
    // end of stuff for metrics

  }

  // This is just for metrics...
  /**
   * Adds an Initializer to all communities
   * The initializer is an agent name from a community
   * that all other agents within the society reference
   * for metrics collection.
   */
  private void addInitializer() {
    for(int i=0; i < getChildCount(); i++) {
      ABCCommunity comm = (ABCCommunity)getChild(i);
      comm.addAliasInitializer(propInitializer);
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
	// Stuff for metrics:
	addPropertyAlias(child, propSampleInterval, PROP_SAMPLEINTERVAL); 
	addPropertyAlias(child, propMaxSamples, PROP_MAXNUMBSAMPLES);
	addPropertyAlias(child, propStartDelay, PROP_STARTDELAY);
	addPropertyAlias(child, propNumbProviders, PROP_NUMBPROVIDERS);
	// end of stuff for metrics
	addPropertyAlias(child, propStartTime, PROP_STARTTIME);
	addPropertyAlias(child, propStopTime, PROP_STOPTIME);

	child.initProperties();

	if(child.getCommunity() == 0) {
	  child.getProperty(ABCCommunity.PROP_DEMAND).setValue(new Integer(5));
	  child.getProperty(ABCCommunity.PROP_PRODUCTION).setValue(new Integer(20));
	  child.getProperty(ABCCommunity.PROP_LATITUDE).setValue(new Float(25));
	  child.getProperty(ABCCommunity.PROP_LONGITUDE).setValue(new Float(52));
	} else if(child.getCommunity() == 1) {
	  child.getProperty(ABCCommunity.PROP_DEMAND).setValue(new Integer(3));
	  child.getProperty(ABCCommunity.PROP_PRODUCTION).setValue(new Integer(20));
	  child.getProperty(ABCCommunity.PROP_LATITUDE).setValue(new Float(27));
	  child.getProperty(ABCCommunity.PROP_LONGITUDE).setValue(new Float(52));
	} else if(child.getCommunity() == 2) {
	  child.getProperty(ABCCommunity.PROP_DEMAND).setValue(new Integer(4));
	  child.getProperty(ABCCommunity.PROP_PRODUCTION).setValue(new Integer(20));
	  child.getProperty(ABCCommunity.PROP_LATITUDE).setValue(new Float(29));
	  child.getProperty(ABCCommunity.PROP_LONGITUDE).setValue(new Float(52));
	} else if(child.getCommunity() == 3) {
	  child.getProperty(ABCCommunity.PROP_DEMAND).setValue(new Integer(2));
	  child.getProperty(ABCCommunity.PROP_PRODUCTION).setValue(new Integer(12));
	  child.getProperty(ABCCommunity.PROP_LATITUDE).setValue(new Float(26));
	  child.getProperty(ABCCommunity.PROP_LONGITUDE).setValue(new Float(75));
	} else if(child.getCommunity() == 4) {
	  child.getProperty(ABCCommunity.PROP_DEMAND).setValue(new Integer(2));
	  child.getProperty(ABCCommunity.PROP_PRODUCTION).setValue(new Integer(12));
	  child.getProperty(ABCCommunity.PROP_LATITUDE).setValue(new Float(28));
	  child.getProperty(ABCCommunity.PROP_LONGITUDE).setValue(new Float(75));
	} else if(child.getCommunity() == 5) {
	  child.getProperty(ABCCommunity.PROP_DEMAND).setValue(new Integer(2));
	  child.getProperty(ABCCommunity.PROP_PRODUCTION).setValue(new Integer(60));
	  child.getProperty(ABCCommunity.PROP_LATITUDE).setValue(new Float(25));
	  child.getProperty(ABCCommunity.PROP_LONGITUDE).setValue(new Float(100));
	} else if(child.getCommunity() == 6) {
	  child.getProperty(ABCCommunity.PROP_DEMAND).setValue(new Integer(2));
	  child.getProperty(ABCCommunity.PROP_PRODUCTION).setValue(new Integer(60));
	  child.getProperty(ABCCommunity.PROP_LATITUDE).setValue(new Float(27));
	  child.getProperty(ABCCommunity.PROP_LONGITUDE).setValue(new Float(100));
	} else if(child.getCommunity() == 7) {
	  child.getProperty(ABCCommunity.PROP_DEMAND).setValue(new Integer(2));
	  child.getProperty(ABCCommunity.PROP_PRODUCTION).setValue(new Integer(400));
	  child.getProperty(ABCCommunity.PROP_LATITUDE).setValue(new Float(29));
	  child.getProperty(ABCCommunity.PROP_LONGITUDE).setValue(new Float(100));
	}
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

	// This is for metrics:
	if(i == 1 && count == 0 ) {
	  propInitializer.setValue(child.getProperty(ABCCommunity.PROP_FIRSTAGENT).getValue());
	}
	// Hide this value from the GUI, it should never been seen by the user.
	setPropertyVisible(child.getProperty(ABCCommunity.PROP_FIRSTAGENT), false);
	// End of stuff for metrics

	count++;
	crntCommunity++;
      }
    }
    
    // These are for metrics:
    propNumbProviders.setValue(new Long(getCommunityCount() * 4L));
    setPropertyVisible(propInitializer, false);
    setPropertyVisible(propNumbProviders, false);
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
   * Generates all ini files for every community
   *
   * @param File directory to place ini files in.
   */
  public void generateIniFiles(File configDir) {
    for(int i=0; i < getChildCount(); i ++) {
      ABCCommunity c = (ABCCommunity)getChild(i);
      c.generateIniFiles(configDir);
    }
  }

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

    public NodeComponent[] getNodes() {
        return (NodeComponent[]) nodes.toArray(new NodeComponent[nodes.size()]);
    }

    public HostComponent[] getHosts() {
        return (HostComponent[]) hosts.toArray(new HostComponent[hosts.size()]);
    }

    public void addModificationListener(ModificationListener l) {
        getEventListenerList().add(ModificationListener.class, l);
    }

    public void removeModificationListener(ModificationListener l) {
      getEventListenerList().remove(ModificationListener.class, l);
    }

  protected void fireModification() {
    fireModification(new ModificationEvent(this, ModificationEvent.SOMETHING_CHANGED));
  }

  protected void fireModification(ModificationEvent event) {
    ModificationListener[] ls =
      (ModificationListener[]) getEventListenerList()
      .getListeners(ModificationListener.class);
    for (int i = 0; i < ls.length; i++) {
      try {
	ls[i].modified(event);
      } catch (RuntimeException re) {
	re.printStackTrace();
      }
    }
  }

  public URL getDescription() {
    return getClass().getResource(DESCRIPTION_RESOURCE_NAME);

  }

  /**
   * Returns whether or not the society can be edited.
   * @return true if society can be edited and false otherwise
   */

  public boolean isEditable() {
    //    return !isRunning;
    return editable;
  }

  /**
   * Set whether or not the society can be edited.
   * @param editable true if society is editable and false otherwise
   */

  public void setEditable(boolean editable) {
    this.editable = editable;
  }

  /**
   * Set by the experiment controller to indicate that the
   * society is running.
   * The society is running from the moment that any node
   * is successfully created 
   * until all nodes are terminated (aborted, self terminated, or
   * manually terminated).
   * @param flag indicating whether or not the society is running
   */
  public void setRunning(boolean isRunning) {
    this.isRunning = isRunning;
  }

  /**
   * Returns whether or not the society is running, 
   * i.e. can be dynamically monitored.
   * Running societies are not editable, but they can be copied,
   * and the copy can be edited.
   * @return true if society is running and false otherwise
   */
  public boolean isRunning() {
    return isRunning;
  }

  /**
   * Return a deep copy of the society.
   * @return society component created
   */
  public SocietyComponent copy(Organizer organizer, Object context) {

    String societyName = organizer.generateSocietyName(getSocietyName());
    ABCSociety result = new ABCSociety(societyName);
    result.initProperties();

//     // set the level & community count properties explicitly
//     Property propLevelCount = result.getProperty(PROP_LEVELCOUNT);
//     propLevelCount.setValue(getProperty(PROP_LEVELCOUNT).getValue());
//     Property propCommCount = result.getProperty(PROP_COMMUNITYCOUNT);
//     propCommCount.setValue(getProperty(PROP_COMMUNITYCOUNT).getValue());
//     result.changeSociety();

    this.copy(result);
    
    return (SocietyComponent)result;
  }
    

  // Change this to get getOutputFileFilter or something like that?
  /**
   * Return a file filter which can be used to fetch
   * the metrics files for this experiment.
   * @return file filter to get metrics files for this experiment
   */
  public FileFilter getResultFileFilter() {
    return metricsFileFilter;
  }

  /**
   * Return a file filter which can be used to delete
   * the files generated by this experiment.
   * @return file filter for cleanup
   */
  public FileFilter getCleanupFileFilter() {
    // TODO: return cleanup file filter
    return null;
  }


  /**
   * Returns whether the society is self terminating or must
   * be manually terminated.
   * Self terminating nodes cause a NODE_DESTROYED event
   * to be generated (see org.cougaar.tools.server.NodeEvent).
   * @return true if society is self terminating
   * @see org.cougaar.tools.server.NodeEvent
   */

  public boolean isSelfTerminating() {
    return false;
  }

  /**
   * Get a configuration writer for this society.
   */
  public ConfigurationWriter getConfigurationWriter(NodeComponent[] nodes, String nodeFileAddition) {
    return new ABCConfigurationWriter(this, nodes, nodeFileAddition);
  }

}
