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

package org.cougaar.tools.csmart.ui.configbuilder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import java.io.IOException;
import java.net.URL;
import javax.swing.event.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.cougaar.tools.csmart.ui.Browser;
import org.cougaar.tools.csmart.core.cdata.PropGroupData;
import org.cougaar.tools.csmart.core.property.name.CompositeName;
import org.cougaar.tools.csmart.core.property.BaseComponent;
import org.cougaar.tools.csmart.core.property.ModifiableComponent;
import org.cougaar.tools.csmart.core.property.PropertiesListener;
import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.tools.csmart.core.property.PropertyEvent;
import org.cougaar.tools.csmart.core.property.PropertyHelper;
import org.cougaar.tools.csmart.core.property.PropertyListener;
import org.cougaar.tools.csmart.society.AgentComponent;
import org.cougaar.tools.csmart.society.AssetComponent;
import org.cougaar.tools.csmart.society.ContainerBase;
import org.cougaar.tools.csmart.society.ContainerComponent;
import org.cougaar.tools.csmart.society.PluginBase;
import org.cougaar.tools.csmart.society.PluginComponent;
import org.cougaar.tools.csmart.society.PropGroupBase;
import org.cougaar.tools.csmart.society.PropGroupComponent;
import org.cougaar.tools.csmart.society.SocietyComponent;
import org.cougaar.tools.csmart.society.ui.AgentUIComponent;
import org.cougaar.tools.csmart.society.ui.AssetUIComponent;
import org.cougaar.tools.csmart.ui.experiment.PropTableModelBase;
import org.cougaar.tools.csmart.ui.util.NamedFrame;
import org.cougaar.util.log.Logger;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import java.io.ObjectInputStream;

/**
 * Panel that holds the PropertyEditor for editing the properties of a <code>ModifiableConfigurableComponent</code>. <br>
 * It will both view a non-editable components, and edit an editable one.<br>
 * Note that you may in fact view/edit multiple components at once.
 */
public class PropertyEditorPanel extends JPanel 
  implements PropertiesListener, PropertyListener,
  TreeSelectionListener, HyperlinkListener {
  JComboBox selectionCB;
  JSplitPane configCompPanel;
  JTree tree;
  JPopupMenu societyMenu;
  JPopupMenu agentMenu;
  JPopupMenu pluginsMenu;
  JPopupMenu pluginMenu;
  JPopupMenu assetMenu;
  JPopupMenu pgMenu;
  JPopupMenu relationshipsMenu;
  JPopupMenu defaultMenu;
  DefaultMutableTreeNode root;
  PropertyTable propertyTable = null;
  JScrollPane tableScrollPane;
  // correspondence between configurable component and tree node
  Hashtable componentToNode = new Hashtable();
  Hashtable nodeToComponent = new Hashtable();
  Hashtable propertyToLabel = new Hashtable();
  boolean isEditable;
  ModifiableComponent componentToConfigure = null;
  PropertyEditorPanel propertyEditorPanelListener;
  private transient Logger log;
  // all property groups, including those created by user
  Vector propertyGroups = null;
  // well known property groups
  Object [] wellKnownPropertyGroups = { 
    PropGroupData.ITEM_IDENTIFICATION,
    PropGroupData.TYPE_IDENTIFICATION,
    PropGroupData.CLUSTER,
    PropGroupData.ENTITY,
    PropGroupData.COMMUNITY,
    PropGroupData.MILITARYORG,
    PropGroupData.ASSIGNMENT,
    PropGroupData.ORGANIZATION,
    PropGroupData.MAINTENANCE,
    PropGroupData.CSSCAPABILITY
  };
  // indices for above
  static final int ITEM_IDENTIFICATION = 0;
  static final int TYPE_IDENTIFICATION = 1;
  static final int CLUSTER = 2;
  static final int ENTITY = 3;
  static final int COMMUNITY = 4;
  static final int MILITARYORG = 5;
  static final int ASSIGNMENT = 6;
  static final int ORGANIZATION = 7;
  static final int MAINTENANCE = 8;
  static final int CSSCAPABILITY = 9;

  // actions for use on popup menus
  private AbstractAction addAgentAction =
    new AbstractAction("Add Agent") {
        public void actionPerformed(ActionEvent e) {
          addAgent();
        }
      };
  private AbstractAction addPluginAction =
    new AbstractAction("Add Plugin") {
        public void actionPerformed(ActionEvent e) {
          addPlugin();
        }
      };
  private AbstractAction addPropertyGroupAction =
    new AbstractAction("Add Property Group") {
        public void actionPerformed(ActionEvent e) {
          addPropertyGroup();
        }
      };
  private AbstractAction addParameterAction =
    new AbstractAction("Add Parameter") {
        public void actionPerformed(ActionEvent e) {
          addParameter();
        }
      };
  private AbstractAction removeAgentAction =
    new AbstractAction("Delete") {
        public void actionPerformed(ActionEvent e) {
          removeAgent();
        }
      };
  private AbstractAction removePluginAction =
    new AbstractAction("Delete") {
        public void actionPerformed(ActionEvent e) {
          removePlugin();
        }
      };
  private AbstractAction removePropertyGroupAction =
    new AbstractAction("Delete") {
        public void actionPerformed(ActionEvent e) {
          removePropertyGroup();
        }
      };

  private Object[] societyMenuItems = {
    addAgentAction
  };

  private Object[] agentMenuItems = {
    addParameterAction,
    removeAgentAction
  };

  private Object[] pluginsMenuItems = {
    addPluginAction
  };

  private Object[] pluginMenuItems = {
    addParameterAction,
    removePluginAction
  };

  private Object[] assetMenuItems = {
    addPropertyGroupAction,
    addParameterAction
  };

  private Object[] pgMenuItems = {
    addParameterAction,
    removePropertyGroupAction
  };

  private Object[] relationshipsMenuItems = {
  };

  private Object[] defaultMenuItems = {
    addParameterAction
  };



  public PropertyEditorPanel(ModifiableComponent c, boolean isEditable) {
    // caller decides if this panel should allow editing
    this.isEditable = isEditable;
    // create the panel
    configCompPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    setLayout(new BorderLayout());
    add(configCompPanel, BorderLayout.CENTER);
    componentToConfigure = c;
    init();
    createLogger();
  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  /**
   * Set component to edit; used to re-use a running editor
   * to edit a different component.
   */
  public void reinit(ModifiableComponent c) {
    componentToConfigure = c;
    init();
  }

  public void hyperlinkUpdate(HyperlinkEvent e) {
    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
      Browser.setPage(e.getURL());
    }
  }

  private void init() {
    if (tree != null)
      configCompPanel.remove(tree);
    // create tree and model before adding to it
    // as the add methods reference the tree and model
    root = new DefaultMutableTreeNode("Properties");
    tree = new JTree(new DefaultTreeModel(root));
    if (!isEditable) {
      DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
      renderer.setTextNonSelectionColor(Color.gray);
      renderer.setTextSelectionColor(Color.gray);
      tree.setCellRenderer(renderer);
    }
    makeTree();
    tree.expandPath(new TreePath(root));
    tree.setEditable(isEditable);
    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    tree.addTreeSelectionListener(this);
    MouseListener myMouseListener = new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (e.isPopupTrigger()) displayMenu(e);
      }
      public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) displayMenu(e);
      }
      public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) displayMenu(e);
      }
    };
    tree.addMouseListener(myMouseListener);
    // create popup menus to be displayed in tree
    societyMenu = new JPopupMenu(); // add agent
    agentMenu = new JPopupMenu();   // delete (agent)
    pluginsMenu = new JPopupMenu(); // add plugin
    pluginMenu = new JPopupMenu();  // delete (plugin)
    assetMenu = new JPopupMenu();   // add property group, add parameter
    pgMenu = new JPopupMenu();      // delete (property group)
    defaultMenu = new JPopupMenu(); // add parameter
    relationshipsMenu = new JPopupMenu(); // add relationship
    for (int i = 0; i < societyMenuItems.length; i++)
      societyMenu.add((Action)societyMenuItems[i]);
    for (int i = 0; i < agentMenuItems.length; i++)
      agentMenu.add((Action)agentMenuItems[i]);
    for (int i = 0; i < pluginsMenuItems.length; i++)
      pluginsMenu.add((Action)pluginsMenuItems[i]);
    for (int i = 0; i < pluginMenuItems.length; i++)
      pluginMenu.add((Action)pluginMenuItems[i]);
    for (int i = 0; i < assetMenuItems.length; i++)
      assetMenu.add((Action)assetMenuItems[i]);
    for (int i = 0; i < pgMenuItems.length; i++)
      pgMenu.add((Action)pgMenuItems[i]);
    for (int i = 0; i < relationshipsMenuItems.length; i++)
      relationshipsMenu.add((Action)relationshipsMenuItems[i]);
    for (int i = 0; i < defaultMenuItems.length; i++)
      defaultMenu.add((Action)defaultMenuItems[i]);
    configCompPanel.setLeftComponent(new JScrollPane(tree));
    propertyTable = new PropertyTable(isEditable);
    // don't allow user to reorder columns
    propertyTable.getTableHeader().setReorderingAllowed(false);
    if (!isEditable) {
      propertyTable.setForeground(Color.gray);
    }
    tableScrollPane = new JScrollPane(propertyTable);
    JPanel rightPanel = new JPanel(new BorderLayout());
    rightPanel.add(tableScrollPane);
    // only add description if user can edit the properties
    if (isEditable) {
      URL url = componentToConfigure.getDescription();
      if (url != null) {
	JTextPane pane = new JTextPane();
	try {
	  pane.setEditable(false);
	  pane.addHyperlinkListener(this);
	  pane.setPage(url);
	  rightPanel.add(pane, BorderLayout.NORTH);
	} catch (IOException ioe) {
          if(log.isErrorEnabled()) {
            log.error("Exception", ioe);
          }
	}
      } else {
	JLabel pane = new JLabel("No description available");
	rightPanel.add(pane, BorderLayout.NORTH);
      }
    }
    configCompPanel.setRightComponent(rightPanel);
    componentToConfigure.addPropertiesListener(this);
    propertyEditorPanelListener = this;
    configCompPanel.validate();
    configCompPanel.setDividerLocation(200);
  }

  /**
   * Make a tree from the property names in the selected configurable
   * component.
   */

  private void makeTree() {
    // get the list of configurable components to represent in the tree
    List components = new ArrayList();
    components.add(componentToConfigure);
    // for each configurable component
    for (int i = 0; i < components.size(); i++) {
      ModifiableComponent component = 
        (ModifiableComponent)components.get(i);
      // determine where to put component in tree
      ModifiableComponent parentComponent = 
        (ModifiableComponent)component.getParent();
      DefaultMutableTreeNode parentNode = root;
      if (parentComponent != null) {
        parentNode = 
          (DefaultMutableTreeNode)componentToNode.get(parentComponent);
        if (parentNode == null)
          parentNode = root;
      }
      // add the component to the tree
      createTreeNode(component, parentNode);
      // add its children to the list of components to put in the tree
      int nChildren = component.getChildCount();
      for (int j = 0; j < nChildren; j++) 
        components.add(component.getChild(j));
    }
  }

  /**
   * Display user interface components for property names in a table.
   * Called when user makes a selection in the tree.
   */

  private void displayComponents(PropertyTreeNode node) {
    ModifiableComponent component = 
      (ModifiableComponent)nodeToComponent.get(node);
    for (Iterator i = component.getLocalPropertyNames(); i.hasNext(); ) {
      CompositeName propName = (CompositeName) i.next();
      Property property = component.getProperty(propName);
      if (property != null) 
        addTableEntryForProperty(property);
    }
  }

  /**
   * Add table entry for a property.
   */
  private void addTableEntryForProperty(Property property) {
    propertyTable.addProperty(property);
    property.addPropertyListener(this);
  }

  /**
   * PropertiesListener interface.
   */

  /**
   * A property was added by the configurable component.
   * Add the property name to the tree.
   * If the parent property name is selected in the tree,
   * then add an user interface component for this property
   * to the current display, otherwise the user interface component
   * is created whent the parent property name is selected from the tree.
   */
  public void propertyAdded(PropertyEvent e) {
    Property prop = e.getProperty();
    // get the tree node for this property's configurable component
    ModifiableComponent cc = 
      (ModifiableComponent)prop.getConfigurableComponent();
    PropertyTreeNode node = (PropertyTreeNode)componentToNode.get(cc);
    CompositeName name = prop.getName();
    if (node.addPropertyName(name)) {
      TreePath path = tree.getSelectionPath();
      if (path == null) return;
      // if it's a property of the selected tree node, then display it now
      PropertyTreeNode selectedNode = 
        (PropertyTreeNode)path.getLastPathComponent();
      if (selectedNode.equals(node)) {
        if(log.isDebugEnabled()) {
          log.debug("PropertyBuilder: Property added: " + name);
        }
        addTableEntryForProperty(prop);
      }
    }
  }

  /**
   * A property was removed by the configurable component.
   * Remove the user interface component for the property and remove the
   * property from the tree, only if the property is found.
   * If we get notified because the property of a child node was removed,
   * this does the right thing -- i.e. it ignores the remove notification
   * because it doesn't find the specified property in the properties
   * for the tree node.
   */
  public void propertyRemoved(PropertyEvent e) {
    Property prop = e.getProperty();
    ModifiableComponent cc = 
      (ModifiableComponent)prop.getConfigurableComponent();
    PropertyTreeNode node = (PropertyTreeNode)componentToNode.get(cc);
    CompositeName name = prop.getName();
    if (node.removePropertyName(name)) {
      TreePath path = tree.getSelectionPath();
      if (path == null) return;
      PropertyTreeNode selectedNode = 
        (PropertyTreeNode)path.getLastPathComponent();
      if (selectedNode.equals(node)) {
        if(log.isDebugEnabled()) {
          log.debug("PropertyBuilder: Property removed: " + prop.getName());
        }
        removeTableEntryForProperty(prop);
      }
    }
  }

  /**
   * End PropertiesListener interface.
   */

  /**
   * Remove table entry for a property, if it exists.
   */
  private void removeTableEntryForProperty(Property property) {
    propertyTable.removeProperty(property);
    property.removePropertyListener(this);
  }


  /**
   * PropertyListener interface.
   * If the configurable component (as opposed to the UI) initiates a change
   * then set the new value in the user inteface (table)
   * or (for aspects of the property other than the value), handle
   * the change (i.e. new label, default value, class or allowed values).
   */
  public void propertyValueChanged(PropertyEvent e) {
    Property p = e.getProperty();
    p.removePropertyListener(this);
    int row = 
      ((PropTableModelBase)(propertyTable.getModel())).getRowForProperty(p);
    if (row != -1) {

      Object value = null;
      if (p.isValueSet())
	value = p.getValue();
      else
	value = "<not set>";
      Object valueInTable = propertyTable.getValueAt(row, 1);
      if (valueInTable.equals(value)) {
	p.addPropertyListener(this);
	return; // don't need to update
      }
      propertyTable.setValueAt(p.getValue(), row, 1);
      propertyTable.revalidate();
      propertyTable.repaint();
    }
    p.addPropertyListener(this);
  }

  public void propertyOtherChanged(PropertyEvent e) {
    // TODO: handle DEFAULTVALUE_CHANGED, LABEL_CHANGED,
    // CLASS_CHANGED, ALLOWEDVALUES_CHANGED
  }

  /**
   * End PropertyListener interface.
   */

  /**
   * TreeSelectionListener interface.
   */

  /**
   * If tree selection changes, update list of user interface components
   * displayed.
   * @param event the tree selection event
   */
  public void valueChanged(TreeSelectionEvent event) {
    TreePath path = event.getPath();
    if (path == null) return;
    Object o = path.getLastPathComponent();
    if (o instanceof PropertyTreeNode) {
      stopEditing();
      propertyTable.removeAll(); // clear what was displayed
      if (event.isAddedPath())   // display parameters for new selection
        displayComponents((PropertyTreeNode)o);
    }
  }

  /**
   * End TreeSelectionListener interface.
   */

  /**
   * Display popup menu with actions appropriate for the selected tree node.
   */

  private void displayMenu(MouseEvent e) {
    // the path to the node the mouse is pointing at
    TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
    if (selPath == null)
      return;
    tree.setSelectionPath(selPath);
    int x = e.getX();
    int y = e.getY();
    // show menu for type of node selected
    Object o = nodeToComponent.get(selPath.getLastPathComponent());
    if (o instanceof SocietyComponent)
      societyMenu.show(tree, x, y);
    else if (o instanceof AgentComponent)
      agentMenu.show(tree, x, y);
    else if (o instanceof ContainerComponent &&
             ((ModifiableComponent)o).getShortName().equals("Plugins"))
      pluginsMenu.show(tree, x, y);
    else if (o instanceof AssetComponent)
      assetMenu.show(tree, x, y);
    else if (o instanceof PluginComponent)
      pluginMenu.show(tree, x, y);
    else if (o instanceof PropGroupComponent)
      pgMenu.show(tree, x, y);
    else
      defaultMenu.show(tree, x, y); // add parameter
  }

  /**
   * Actions invoked from popup menu.
   */

  /**
   * Query the user for the name of the agent.
   * Create a component for the agent, and a tree node
   * representing the agent, and add it to the society node in the tree.
   * Create components and add tree nodes for Plugins and AssetData.
   */

  public void addAgent() {
    String name = 
      (String)JOptionPane.showInputDialog(this, "Enter Agent Name", 
                                          "Agent Name",
                                          JOptionPane.QUESTION_MESSAGE,
                                          null, null, "");
    if (name == null) return;

    DefaultMutableTreeNode selNode =
      (DefaultMutableTreeNode)tree.getSelectionPath().getLastPathComponent();
    ModifiableComponent society = 
      (ModifiableComponent)nodeToComponent.get(selNode);
    AgentComponent agentComponent = 
      (AgentComponent)new AgentUIComponent(name);
    // put the node in the tree before adding properties to it
    PropertyTreeNode agentNode = createTreeNode(agentComponent, selNode);
    agentComponent.initProperties();
    society.addChild(agentComponent);

    ContainerComponent pluginContainer = 
      (ContainerComponent)new ContainerBase("Plugins");
    createTreeNode(pluginContainer, agentNode);
    pluginContainer.initProperties();
    agentComponent.addChild(pluginContainer);

    AssetComponent asset = (AssetComponent)new AssetUIComponent();
    createTreeNode(asset, agentNode);
    Property p = asset.addProperty(AssetComponent.PROP_TYPE, "");
    p.setToolTip(AssetComponent.PROP_TYPE_DESC);
    p = asset.addProperty(AssetComponent.PROP_CLASS, "");
    p.setToolTip(AssetComponent.PROP_CLASS_DESC);
    p = asset.addProperty(AssetComponent.PROP_UID, "");
    p.setToolTip(AssetComponent.PROP_UID_DESC);
    p = asset.addProperty(AssetComponent.PROP_UNITNAME, "");
    p.setToolTip(AssetComponent.PROP_UNITNAME_DESC);
    p = asset.addProperty(AssetComponent.PROP_UIC, "");
    p.setToolTip(AssetComponent.PROP_UIC_DESC);
    agentComponent.addChild(asset);
  }

  public void removeAgent() {
    DefaultMutableTreeNode selNode =
      (DefaultMutableTreeNode)tree.getSelectionPath().getLastPathComponent();
    ModifiableComponent cc = 
      (ModifiableComponent)nodeToComponent.get(selNode);
    Object parent = cc.getParent();
    ((ModifiableComponent)parent).removeChild(cc);
    // remove agent and subnodes from the tree
    removeTreeNodes(selNode);
  }

  public void addPlugin() {
    // get plugins from all existing agents
    Enumeration components = componentToNode.keys();
    ArrayList pluginNames = new ArrayList();
    ArrayList pluginClasses = new ArrayList();
    while (components.hasMoreElements()) {
      Object o = components.nextElement();
      if (o instanceof PluginComponent) {
        PluginComponent plugin = (PluginComponent)o;
        String name = ((BaseComponent)plugin).getShortName();
        if (!pluginNames.contains(name)) {
          pluginNames.add(name);
          pluginClasses.add(plugin.getPluginClassName());
        }
      }
    }
    JPanel panel = new JPanel(new GridBagLayout());
    ArrayList sortedNames = (ArrayList)pluginNames.clone();
    Collections.sort(sortedNames);
    JComboBox cb = new JComboBox(sortedNames.toArray());
    cb.setEditable(true);
    cb.setPreferredSize(new Dimension(200, 
                                      (int)cb.getPreferredSize().getHeight()));
    int x = 0;
    int y = 0;
    panel.add(cb,
              new GridBagConstraints(x, y++, 1, 1, 0.0, 0.0,
                                     GridBagConstraints.WEST,
                                     GridBagConstraints.HORIZONTAL,
                                     new Insets(0, 0, 0, 5), 0, 0));
    int result = JOptionPane.showConfirmDialog(this, panel, "Plugin",
                                               JOptionPane.OK_CANCEL_OPTION,
                                               JOptionPane.PLAIN_MESSAGE);
    if (result != JOptionPane.OK_OPTION)
      return;
    String name = (String)cb.getSelectedItem();
    name = name.trim();
    if (name.length() == 0)
      return;
    String className = null;
    int index = pluginNames.indexOf(name);
    if (index > -1 && index < pluginNames.size())
      className = (String)pluginClasses.get(index);
    else {
      className =
        (String)JOptionPane.showInputDialog(this, "Enter Plugin Class Name",
                                            "Plugin Class Name",
                                            JOptionPane.QUESTION_MESSAGE,
                                            null, null, "");
      if (className == null) return;
      className = className.trim(); // trim white space
      if (className.length() == 0) return;
    }
    PluginComponent plugin = 
      (PluginComponent)new PluginBase(name, className);
    DefaultMutableTreeNode selNode =
      (DefaultMutableTreeNode)tree.getSelectionPath().getLastPathComponent();
    createTreeNode(plugin, selNode);
    plugin.initProperties();
    ModifiableComponent cc = 
      (ModifiableComponent)nodeToComponent.get(selNode);
    cc.addChild(plugin);
  }

  public void removePlugin() {
    DefaultMutableTreeNode selNode =
      (DefaultMutableTreeNode)tree.getSelectionPath().getLastPathComponent();
    ModifiableComponent cc = 
      (ModifiableComponent)nodeToComponent.get(selNode);
    Object parent = cc.getParent();
    ((ModifiableComponent)parent).removeChild(cc);
    removeTreeNodes(selNode);
  }

  /**
   * Create component and tree node for new property group.
   */
  // TODO: need to determine how to organize classes so that
  // classes created from INI files, and 
  // classes created from database, and
  // classes created from user edits
  // share a common base
  public void addPropertyGroup() {
    JPanel panel = new JPanel(new GridBagLayout());
    int x = 0;
    int y = 0;
    panel.add(new JLabel("Property Group"),
              new GridBagConstraints(x++, y, 1, 1, 0.0, 0.0,
                                     GridBagConstraints.WEST,
                                     GridBagConstraints.NONE,
                                     new Insets(0, 5, 0, 5), 0, 0));
    if (propertyGroups == null) {
      propertyGroups = new Vector(wellKnownPropertyGroups.length);
      for (int i = 0; i < wellKnownPropertyGroups.length; i++)
        propertyGroups.add(wellKnownPropertyGroups[i]);
    }
    JComboBox cb = new JComboBox(propertyGroups);
    cb.setEditable(true);
    cb.setPreferredSize(new Dimension(200, 
                                      (int)cb.getPreferredSize().getHeight()));
    panel.add(cb,
              new GridBagConstraints(x, y++, 1, 1, 0.0, 0.0,
                                     GridBagConstraints.WEST,
                                     GridBagConstraints.HORIZONTAL,
                                     new Insets(0, 0, 0, 5), 0, 0));
    int result = JOptionPane.showConfirmDialog(this, panel, "Property Group",
                                               JOptionPane.OK_CANCEL_OPTION,
                                               JOptionPane.PLAIN_MESSAGE);
    if (result != JOptionPane.OK_OPTION)
      return;
    String pgName = (String)cb.getSelectedItem();
    int pgIndex = cb.getSelectedIndex();
    pgName = pgName.trim();
    if (pgName.length() == 0)
      return;
    DefaultMutableTreeNode selNode =
      (DefaultMutableTreeNode)tree.getSelectionPath().getLastPathComponent();
    AssetComponent assetComponent = 
      (AssetComponent)nodeToComponent.get(selNode);
    PropGroupData pgData = new PropGroupData(pgName);
    PropGroupComponent pg = (PropGroupComponent)new PropGroupBase(pgData);
    DefaultMutableTreeNode pgNode = createTreeNode(pg, selNode);
    pg.initProperties();
    // for each of the well defined properties
    // add the appropriate parameters
    if (pgIndex >= 0 && pgIndex < wellKnownPropertyGroups.length)
      setParameters(pg, pgIndex);
    else
      propertyGroups.add(pgName); // user defined property group
    assetComponent.addChild(pg);
    // select the new node
    tree.setSelectionPath(tree.getSelectionPath().pathByAddingChild(pgNode));
  }

  /**
   * TODO: will this work, i.e. can you add properties to the property group
   * or must this use the PropGroupData object
   */

  private void setParameters(ModifiableComponent pg, int pgIndex) {
    switch (pgIndex) {
    case ITEM_IDENTIFICATION:
      pg.addProperty("ItemIdentification", "");
      pg.addProperty("Nomenclature", "");
      pg.addProperty("AlternateItemIdentification", "");
      break;
    case TYPE_IDENTIFICATION:
      pg.addProperty("TypeIdentification", "UTC/RTOrg");
      break;
    case CLUSTER:
      // TODO: type should be ClusterIdentifier???
      pg.addProperty("ClusterIdentifier", "");
      break;
    case ENTITY:
      break;
    case COMMUNITY:
      // TODO: type should be TimeSpan
      pg.addProperty("TimeSpan", "");
      // how do you actually set these as properties?
      pg.addProperty("Communities", "");
      break;
    case MILITARYORG:
      break;
    case ASSIGNMENT:
      break;
    case ORGANIZATION:
      break;
    case MAINTENANCE:
      break;
    case CSSCAPABILITY:
      break;
    default:
      break;
    }
  }

  public void removePropertyGroup() {
    DefaultMutableTreeNode selNode =
      (DefaultMutableTreeNode)tree.getSelectionPath().getLastPathComponent();
    ModifiableComponent cc = 
      (ModifiableComponent)nodeToComponent.get(selNode);
    Object parent = cc.getParent();
    ((ModifiableComponent)parent).removeChild(cc);
    removeTreeNodes(selNode);
  }

  /**
   * Add a parameter to a configurable component.  Invoked when the
   * user selects a tree node and invokes the "Add Parameter" option from
   * the pop-up menu.  Note that this simply adds the parameter to the
   * configurable component corresponding to the tree node.  It relies
   * on listeners that were previously set up on the configurable components
   * to update the user interface.
   */

  public void addParameter() {
    String name =
      (String)JOptionPane.showInputDialog(this, "Enter Parameter Name",
                                          "Parameter Name",
                                          JOptionPane.QUESTION_MESSAGE,
                                          null, null, "");
    if (name == null) return;
    name = name.trim(); // trim white space
    if (name.length() == 0) return;
    DefaultMutableTreeNode selNode =
      (DefaultMutableTreeNode)tree.getSelectionPath().getLastPathComponent();
    ModifiableComponent cc = 
      (ModifiableComponent)nodeToComponent.get(selNode);
    Property p = cc.addProperty(name, "");
  }

  /**
   * Create a tree node for the configurable component,
   * and add it to the parent tree node.
   * Also, put the component and the new node in hashtables to facilitate
   * matching tree nodes to components.
   * Return the new tree node.
   */

  private PropertyTreeNode createTreeNode(ModifiableComponent component,
                                          DefaultMutableTreeNode parentNode) {
    PropertyTreeNode newNode = new PropertyTreeNode(component.getFullName());
    if (component instanceof AssetComponent || 
        component instanceof ContainerComponent)
      newNode.setLeaf(false);
    DefaultTreeModel model = (DefaultTreeModel)tree.getModel();        
    model.insertNodeInto(newNode, parentNode, parentNode.getChildCount());
    componentToNode.put(component, newNode);
    nodeToComponent.put(newNode, component);
    return newNode;
  }

  /**
   * Remove node and its children from tree and
   * remove node and its children from the hashtables.
   */

  private void removeTreeNodes(DefaultMutableTreeNode nodeToRemove) {
    Enumeration nodesToRemove = nodeToRemove.depthFirstEnumeration();
    while (nodesToRemove.hasMoreElements()) {
      DefaultMutableTreeNode node = 
        (DefaultMutableTreeNode)nodesToRemove.nextElement();
      ModifiableComponent cc = 
        (ModifiableComponent)nodeToComponent.get(node);
      componentToNode.remove(cc);
      nodeToComponent.remove(node);
    }
    DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
    model.removeNodeFromParent(nodeToRemove);
  }

  /**
   * Stop any editing in progress, and accept the value.
   */

  public void stopEditing() {
    int row = propertyTable.getEditingRow();
    int column = propertyTable.getEditingColumn();
    if (row != -1 && column != -1) 
      propertyTable.getCellEditor(row, column).stopCellEditing();
  }

  private void readObject(ObjectInputStream ois)
    throws IOException, ClassNotFoundException
  {
    ois.defaultReadObject();
    createLogger();
  }

//    public void addParameter() {
//      DefaultMutableTreeNode selNode =
//        (DefaultMutableTreeNode)tree.getSelectionPath().getLastPathComponent();
//      ModifiableComponent cc = 
//        (ModifiableComponent)nodeToComponent.get(selNode);
//      JPanel panel = new JPanel(new GridBagLayout());
//      int x = 0;
//      int y = 0;
//      panel.add(new JLabel("Parameter Name:"),
//                new GridBagConstraints(x++, y, 1, 1, 0.0, 0.0,
//                                       GridBagConstraints.WEST,
//                                       GridBagConstraints.NONE,
//                                       new Insets(0, 5, 0, 5), 0, 0));
//      JTextField nameField = new JTextField(20);
//      panel.add(nameField,
//                new GridBagConstraints(x, y++, 1, 1, 0.0, 0.0,
//                                       GridBagConstraints.WEST,
//                                       GridBagConstraints.HORIZONTAL,
//                                       new Insets(0, 5, 0, 5), 0, 0));
//      x = 0;
//      panel.add(new JLabel("Parameter Type:"),
//                new GridBagConstraints(x++, y, 1, 1, 0.0, 0.0,
//                                       GridBagConstraints.WEST,
//                                       GridBagConstraints.NONE,
//                                       new Insets(0, 5, 0, 5), 0, 0));
//      Object[] types = { "Integer", "String" };
//      JComboBox cb = new JComboBox(types);
//      panel.add(cb,
//                new GridBagConstraints(x, y++, 1, 1, 0.0, 0.0,
//                                       GridBagConstraints.WEST,
//                                       GridBagConstraints.HORIZONTAL,
//                                       new Insets(0, 5, 0, 5), 0, 0));
//      int result = JOptionPane.showConfirmDialog(this, panel, "New Parameter",
//                                                 JOptionPane.OK_CANCEL_OPTION,
//                                                 JOptionPane.PLAIN_MESSAGE);
//      if (result != JOptionPane.OK_OPTION)
//        return;
//      String name = nameField.getText();
//      name = name.trim(); // trim white space
//      if (name == null || name.length() == 0) return;
//      Property p = cc.addProperty(name, "");
//      String type = (String)cb.getSelectedItem();
//      if (type.equals("String"))
//        p.setPropertyClass(String.class);
//      else if (type.equals("Integer"))
//        p.setPropertyClass(Integer.class);
//    }

}
