/*
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 *
 * © Copyright 2000, 2001 BBNT Solutions LLC
 * </copyright>
 */
package org.cougaar.tools.csmart.ui.component;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.util.List;
import java.util.Collections;
import javax.swing.JOptionPane;

import org.cougaar.tools.server.ConfigurationWriter;

import org.cougaar.tools.csmart.ui.viewer.Organizer;
import org.cougaar.tools.csmart.scalability.ScalabilityMetricsFileFilter;

/**
 * <PRE>
 * Define a society with the following properties:
 * Levels: an integer, with value 1 to 3
 * Number of communities per level: an integer, with values 1 to 4
 * For each level,
 * the user can specify the ABCCommunityComponent TaskType and 
 * for each community, 
 * the user can specify the number of agents in that community
 * The subcomponents of this component are of class ABCCommunityComponent.
 * </PRE>
 */

public class ABCSocietyComponent
  extends ModifiableConfigurableComponent
  implements SocietyComponent, PropertyListener, ModificationListener
{
  static final long serialVersionUID = 5021216316615532269L;

  // used for creating subcomponent names
  private static final String DEFAULT_PREFIX = "Community";
  private static final String PROP_ = "Property ";
  private static final String PROP_LEVELS = "Levels";
  private static final String PROP_PREFIX = "NamePrefix";
  private static final String PROP_COMMUNITIES_PER_LEVEL = "CommunitiesPerLevel";
  // these two properties must be first

  private static final String LABEL_LEVELS = "Number of Levels";
  private static final String LABEL_COMMUNITIES_PER_LEVEL =
    "Number of Communities Per Level";

  private static final Integer ONE = new Integer(1);

  private Property propLevels;
  private Property propCPL;
  private Property propPrefix;

  private String name;
  private boolean isRunning;

  private static FileFilter metricsFileFilter = new ScalabilityMetricsFileFilter();

  /**
   * Define properties and set default and allowed values.
   */

  public ABCSocietyComponent() {
    super("abc");
  }

  public void initProperties() {
    int ix;
    propLevels = addProperty(PROP_LEVELS, ONE);
    propLevels.setLabel(LABEL_LEVELS);
    propLevels.setDefaultValue(ONE);
    propLevels.setAllowedValues(Collections.singleton(new IntegerRange(1, 3)));
    propLevels.setExperimentValues(Collections.singleton(new IntegerRange(1, 3)));
    propLevels.addPropertyListener(this);
    propCPL = addProperty(PROP_COMMUNITIES_PER_LEVEL, ONE);
    propCPL.setLabel(LABEL_COMMUNITIES_PER_LEVEL);
    propCPL.setDefaultValue(ONE);
    propCPL.setAllowedValues(Collections.singleton(new IntegerRange(1, 4)));
    propCPL.setExperimentValues(Collections.singleton(new IntegerRange(1, 4)));
    propCPL.addPropertyListener(this);
    propPrefix = addProperty(PROP_PREFIX, DEFAULT_PREFIX);
    propPrefix.addPropertyListener(this);
    setCommunities(1); // default to 1 level and 1 community
  }

  /**
   * Implement PropertyListener interface
   * Recompute the number of communities (LEVELS * COMMUNITIES_PER_LEVEL)
   * and ensure that the number of communities exists.
   **/
  public void propertyValueChanged(PropertyEvent e) {
    if (e.getProperty() == propPrefix) {
      renameCommunities();
    } else {
      int nLevels = ((Integer) propLevels.getValue()).intValue();
      int cpl = ((Integer) propCPL.getValue()).intValue();
      int nCommunities = nLevels * cpl;
      setCommunities(nCommunities);
    }
  }

  public void propertyOtherChanged(PropertyEvent e) {}

  /**
   * Set the number of communities in this society to the specified value,
   * by creating or deleting ABCCommunityComponents.
   * Recompute and set the property count for this component,
   * assuming that all communities have the same name number of properties.
   */

  private void setCommunities(int nCommunities) {
    System.out.println("Setting number of communities to: " + nCommunities);
    while (getChildCount() > nCommunities) {
      removeChild(getChild(getChildCount() - 1));
    }
    String namePrefix = (String) propPrefix.getValue();
    while (getChildCount() < nCommunities) {
      ConfigurableComponent community =
	new ABCCommunityComponent(namePrefix + getChildCount());
      community.initProperties();
      addChild(community);
    }
    System.out.println("Number of properties is now: " + 
		       getPropertyNamesList().size());
  }

  private void renameCommunities() {
    String namePrefix = (String) propPrefix.getValue();
    for (int i = 0, n = getChildCount(); i < n; i++) {
      ConfigurableComponent child = getChild(i);
      child.setName(namePrefix + i);
    }
  }

  /**
   * For debugging, print properties.
   */

  public void printProperties() {
    List props = getPropertyNamesList();
    int n = props.size();
    System.out.println("Number of properties: " + n);
    System.out.println("=======================================");
    for (int i = 0; i < n; i++) {
      CompositeName name = (CompositeName) props.get(i);
      System.out.println("Property name: " + name);
      printProperty(getProperty(name));
      System.out.println("=======================================");
    }
  }

  public void printProperty(Property prop) {
    System.out.println("Name: " + prop.getName()); 
    System.out.println("Label: " + prop.getLabel());
    System.out.println("Class: " + prop.getPropertyClass());
    System.out.println("Value: " + prop.getValue());
    System.out.println("Default: " + prop.getDefaultValue());
    System.out.println("Allowed Values: " + prop.getAllowedValues());
    System.out.println("Experiment Values: " + 
		       prop.getExperimentValues());
  }

  /**
   * Mostly empty versions of SocietyComponent interface
   * so we can use this class to test editing.
   */

  public void setName(String newName) {
    this.name = newName;
  }

  public String getSocietyName() {
    return name;
  }

  public AgentComponent[] getAgents() {
    return null;
  }

  public URL getDescription() {
    return null;
  }

  public boolean isEditable() {
    return true;
  }

  public void setRunning(boolean isRunning) {
    this.isRunning = isRunning;
  }

  public boolean isRunning() {
    return isRunning;
  }

  public SocietyComponent copy(Organizer organizer, Object context) {
    return null;
  }

  public FileFilter getMetricsFileFilter() {
    return metricsFileFilter;
  }

  public FileFilter getCleanupFileFilter() {
    return null;
  }

  public boolean isSelfTerminating() {
    return true;
  }

  public ConfigurationWriter getConfigurationWriter(NodeComponent[] nodes, 
						    String nodeFileAddition) {
    return null;
  }

  public void modified(ModificationEvent e) {
    fireModification();
  }

  public void test() {
    changeValue();
  }

  public void changeValue() {
    String s = JOptionPane.showInputDialog(null, "New value for levels:");
    propLevels.setValue(Integer.valueOf(s));
  }

  public static void main(String[] args) {
    ABCSocietyComponent abc = new ABCSocietyComponent();
    abc.initProperties();
    System.out.println("INITIAL PROPERTIES:");
    abc.printProperties();
    // user sets value of 2 for 0th property (which is the LEVELS property)
    abc.getProperty(PROP_LEVELS).setValue(new Integer(2)); // set number of levels
    System.out.println("PROPERTIES NOW ARE:");
    abc.printProperties();
    // user sets value of 3 for 1st property (which is the COMM/LEVEL property)
    abc.getProperty(PROP_COMMUNITIES_PER_LEVEL).setValue(new Integer(3)); // set number of levels
    System.out.println("PROPERTIES NOW ARE:");
    abc.printProperties();
    System.out.println("Setting properties 4 and 5 to transport and 3");
    abc.addProperty(PROP_ + 4, "transport"); // set number of levels
    abc.addProperty(PROP_ + 5, new Integer(3)); // set number of levels
    abc.printProperties();
    System.out.println("Changing community name prefix");
    abc.getProperty(PROP_PREFIX).setValue("Enclave");
    abc.printProperties();
    // now change the label for a property
    Property p = abc.getProperty(PROP_COMMUNITIES_PER_LEVEL);
    p.setLabel("New Label");
    abc.printProperties();
  }
}
