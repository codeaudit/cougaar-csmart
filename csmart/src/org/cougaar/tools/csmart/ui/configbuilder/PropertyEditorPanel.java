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
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
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
import org.cougaar.tools.csmart.core.property.name.CompositeName;
import org.cougaar.tools.csmart.core.property.BaseComponent;
import org.cougaar.tools.csmart.core.property.ConfigurableComponent;
import org.cougaar.tools.csmart.core.property.InvalidPropertyValueException;
import org.cougaar.tools.csmart.core.property.ModifiableConfigurableComponent;
import org.cougaar.tools.csmart.core.property.ModifiableComponent;
import org.cougaar.tools.csmart.core.property.PropertiesListener;
import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.tools.csmart.core.property.PropertyEvent;
import org.cougaar.tools.csmart.core.property.PropertyHelper;
import org.cougaar.tools.csmart.core.property.PropertyListener;
import org.cougaar.tools.csmart.society.AgentBase;
import org.cougaar.tools.csmart.society.AgentComponent;
import org.cougaar.tools.csmart.society.SocietyComponent;
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
  JPopupMenu defaultMenu;
  DefaultMutableTreeNode root;
  PropertyTable propertyTable = null;
  JScrollPane tableScrollPane;
  // correspondence between configurable component and tree node
  Hashtable componentToNode = new Hashtable();
  Hashtable nodeToComponent = new Hashtable();
  Hashtable propertyToLabel = new Hashtable();
  boolean isEditable;
  ModifiableComponent[] compsToConfig = null; // support an array of things to edit/view
  ModifiableComponent componentToConfigure = null;
  TableModelListener myTableModelListener;
  PropertyEditorPanel propertyEditorPanelListener;
  private transient Logger log;

  public PropertyEditorPanel(ModifiableComponent configComp, 
                             boolean isEditable) {
    // caller decides if this panel should allow editing
    this.isEditable = isEditable;
    // create the configComp panel
    configCompPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    setLayout(new BorderLayout());
    add(configCompPanel, BorderLayout.CENTER);
    componentToConfigure = configComp;
    compsToConfig = new ModifiableConfigurableComponent[1];
    compsToConfig[0] = configComp;
    setModifiableConfigurableComponent();
    createLogger();
  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  /**
   * Set configComp component to edit; used to re-use a running editor
   * to edit a different configComp.
   */
  public void reinit(ModifiableComponent newModifiableConfigurableComponent) {
    componentToConfigure = newModifiableConfigurableComponent;
    compsToConfig = new ModifiableConfigurableComponent[1];
    compsToConfig[0] = componentToConfigure;
    setModifiableConfigurableComponent();
  }

  public void hyperlinkUpdate(HyperlinkEvent e) {
    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
      Browser.setPage(e.getURL());
    }
  }

  private void setModifiableConfigurableComponent() {
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
    societyMenu = new JPopupMenu(); // add/remove agents
    agentMenu = new JPopupMenu();   // add/remove plugins
    defaultMenu = new JPopupMenu(); // add/remove parameters
    JMenuItem addAgentMenuItem = new JMenuItem("Add Agent");
    addAgentMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          addAgent();
        }
      });
    societyMenu.add(addAgentMenuItem);
    JMenuItem removeAgentMenuItem = new JMenuItem("Remove Agent");
    removeAgentMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          removeAgent();
        }
      });
    societyMenu.add(removeAgentMenuItem);
    JMenuItem addPluginMenuItem = new JMenuItem("Add Plugin");
    addPluginMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          addPlugin();
        }
      });
    agentMenu.add(addPluginMenuItem);
    JMenuItem removePluginMenuItem = new JMenuItem("Remove Plugin");
    removePluginMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          removePlugin();
        }
      });
    agentMenu.add(removePluginMenuItem);
    JMenuItem addParameterMenuItem = new JMenuItem("Add Parameter");
    addParameterMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          addParameter();
        }
      });
    defaultMenu.add(addParameterMenuItem);
    configCompPanel.setLeftComponent(new JScrollPane(tree));
    propertyTable = new PropertyTable(isEditable);
    // don't allow user to reorder columns
    propertyTable.getTableHeader().setReorderingAllowed(false);
    if (!isEditable) {
      propertyTable.setForeground(Color.gray);
    }
    // when the table is changed, this listener is notified
    // if a property was deleted, then remove the property from the
    // configurable component selected in the tree
    myTableModelListener = new TableModelListener() {
        public void tableChanged(TableModelEvent e) {
          if (e.getType() == TableModelEvent.DELETE) {
            PropertyTreeNode nodeSelected =
              (PropertyTreeNode)tree.getSelectionPath().getLastPathComponent();
            Object o = nodeToComponent.get(nodeSelected);
            if (o instanceof ConfigurableComponent) {
              // delete the local property corresponding to the row deleted
              int rowDeleted = e.getFirstRow();
              int thisIndex = 0;
              ConfigurableComponent c = (ConfigurableComponent)o;
              for (Iterator i = c.getLocalPropertyNames(); i.hasNext(); ) {
                if (thisIndex == rowDeleted) {
                  // remove our listener on the component's properties
                  c.removePropertiesListener(propertyEditorPanelListener);
                  // remove the property
                  c.removeProperty(c.getProperty((CompositeName)i.next()));
                  // add listener back in so we catch any other changes
                  c.addPropertiesListener(propertyEditorPanelListener);
                  break;
                } else {
                  thisIndex++;
                  i.next();
                }
              }
            } 
          }
        }
      };
    propertyTable.getModel().addTableModelListener(myTableModelListener);
    tableScrollPane = new JScrollPane(propertyTable);
    JPanel rightPanel = new JPanel(new BorderLayout());
    rightPanel.add(tableScrollPane);
    // only add description if user can edit the properties
    if (isEditable) {
      // Just show the description for the first component...
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
    for (int i = 0; i < compsToConfig.length; i++) {
      compsToConfig[i].addPropertiesListener(this);
    }
    propertyEditorPanelListener = this;
    configCompPanel.validate();
    configCompPanel.setDividerLocation(200);
  }

  /**
   * Make a tree from the property names in the selected configurable
   * component.
   */

  private DefaultMutableTreeNode makeTree() {
    List components = new ArrayList();
    DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
    for (int i = 0; i < compsToConfig.length; i++) {
      BaseComponent component = compsToConfig[i];
      PropertyTreeNode node = new PropertyTreeNode(component.getFullName());
      model.insertNodeInto(node, root, root.getChildCount());
      componentToNode.put(component, node);
      nodeToComponent.put(node, component);
      components.add(component);
    }
    int i = 0;
    while (i < components.size()) {
      BaseComponent component = (BaseComponent)components.get(i);
      PropertyTreeNode parentNode = 
        (PropertyTreeNode)componentToNode.get(component);
      int nChildren = component.getChildCount();
      for (int j = 0; j < nChildren; j++) {
        BaseComponent childComponent = component.getChild(j);
        PropertyTreeNode node = 
          new PropertyTreeNode(childComponent.getFullName());
        model.insertNodeInto(node, parentNode, parentNode.getChildCount());
        componentToNode.put(childComponent, node);
        nodeToComponent.put(node, childComponent);
        components.add(childComponent);
      }
      i++;
    }
    return root;
  }

  /**
   * Display user interface components for property names.
   * Called when user makes a selection in the tree.
   */

  private void displayComponents(PropertyTreeNode node) {
    BaseComponent component = (BaseComponent)nodeToComponent.get(node);
    for (Iterator i = component.getLocalPropertyNames(); i.hasNext(); ) {
      CompositeName propName = (CompositeName) i.next();
      Property property = component.getProperty(propName);
      if (property != null) 
        addComponentForProperty(property);
    }
  }

  /**
   * Add user interface component for a property.
   */
  private void addComponentForProperty(Property property) {
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
    CompositeName name = prop.getName();
    if (addPropertyName(name)) {
      TreePath path = tree.getSelectionPath();
      if (path == null) return;
      PropertyTreeNode node = (PropertyTreeNode)path.getLastPathComponent();
      if (name.getPrefix().equals(node.getName())) {
        if(log.isDebugEnabled()) {
          log.debug("PropertyBuilder: Property added: " + name);
        }
        addComponentForProperty(prop);
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
    CompositeName name = prop.getName();
    if (removePropertyName(name)) {
      TreePath path = tree.getSelectionPath();
      if (path == null) return;
      PropertyTreeNode node = (PropertyTreeNode)path.getLastPathComponent();
      if (name.getPrefix().equals(node.getName())) {
        if(log.isDebugEnabled()) {
          log.debug("PropertyBuilder: Property removed: " + prop.getName());
        }
        removeComponentForProperty(prop);
      }
    }
  }

  /**
   * End PropertiesListener interface.
   */

  /**
   * Add a property to a tree, first adding its ancestors if necessary.
   */

  private boolean addPropertyName(CompositeName name) {
    PropertyTreeNode node = findPropertyNode(name.getPrefix(), true);
    return node.addPropertyName(name);
  }

  private PropertyTreeNode findPropertyNode(CompositeName name, boolean create) {
    MutableTreeNode parentNode;
    if (name.size() > 1) {
      parentNode = findPropertyNode(name.getPrefix(), create);
      if (parentNode == null) return null;
    } else {
      parentNode = root;
    }
    for (int i = 0, n = parentNode.getChildCount(); i < n; i++) {
      PropertyTreeNode node = (PropertyTreeNode) parentNode.getChildAt(i);
      if (name.equals(node.getName())) {
        return node;
      }
    }
    if (!create) return null;

    // TODO: need to update the node<->component hashtables?
    // this occurs only when you're adding a property to the tree
    // and you haven't previously added its parent; does this ever happen?
    // if not, remove the code

    PropertyTreeNode node = new PropertyTreeNode(name);
    if (log.isErrorEnabled()) {
      log.error("PropertyEditorPanel: WARNING: adding new component to tree: " + name);
    }
    DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
    model.insertNodeInto(node, parentNode, parentNode.getChildCount());
    return node;
  }

  private boolean removePropertyName(CompositeName name) {
    PropertyTreeNode node = findPropertyNode(name.getPrefix(), false);
    if (node != null) {
      if (node.removePropertyName(name)) {
        checkRemoveNode(node);
        return true;
      }
    }
    return false;
  }

  private void checkRemoveNode(PropertyTreeNode node) {
    if (node.getPropertyCount() + node.getChildCount() == 0) {
      DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
      TreeNode parentNode = node.getParent();
      model.removeNodeFromParent(node);
      if (parentNode instanceof PropertyTreeNode) {
        checkRemoveNode((PropertyTreeNode) parentNode);
      }
    }
  }

  /**
   * Remove user interface component for a property, if it exists.
   */
  private void removeComponentForProperty(Property property) {
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
      propertyTable.getModel().removeTableModelListener(myTableModelListener);
      propertyTable.removeAll(); // clear what was displayed
      propertyTable.getModel().addTableModelListener(myTableModelListener);
      PropertyTreeNode node = (PropertyTreeNode) o;
      //      displayComponents(node.getPropertyNames());
      displayComponents(node);
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
    // determine what type of node is selected
    Object o = nodeToComponent.get(selPath.getLastPathComponent());
    if (o instanceof SocietyComponent)
      // add/remove agents
      societyMenu.show(tree, e.getX(), e.getY());
    else if (o instanceof AgentComponent)
      // add/remove plugins
      agentMenu.show(tree, e.getX(), e.getY());
    else
      // add/remove parameters
      defaultMenu.show(tree, e.getX(), e.getY());
  }

  /**
   * Actions invoked from popup menu.
   */

  /**
   * Add a node representing the agent to the society node of the tree.
   * Initialize the agents properties.
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
    DefaultTreeModel model = (DefaultTreeModel)tree.getModel();

    AgentComponent agentComponent = new AgentBase(name);
    PropertyTreeNode agentNode = 
      new PropertyTreeNode(agentComponent.getFullName());
    agentNode.setLeaf(false);
    model.insertNodeInto(agentNode, selNode, selNode.getChildCount());
    componentToNode.put(agentComponent, agentNode);
    nodeToComponent.put(agentNode, agentComponent);

//      INIContainer pluginContainer = new INIContainer("Plugins");
//      PropertyTreeNode pluginNode = new PropertyTreeNode("Plugins");
//      node.setLeaf(false);
//      model.insertNodeInto(pluginNode, agentNode, 0);
//      componentToNode.put(pluginContainer, pluginNode);
//      nodeToComponent.put(pluginNode, pluginContainer);

//      INIContainer assetContainer = new INIContainer("AssetData");
//      PropertyTreeNode assetNode = new PropertyTreeNode("AssetData");
//      node.setLeaf(false);
//      model.insertNodeInto(assetNode, agentNode, 1);
//      componentToNode.put(agentContainer, agentNode);
//      nodeToComponent.put(agentNode, agentContainer);
  }

  public void removeAgent() {
  }

  public void addPlugin() {
  }

  public void removePlugin() {
  }

  public void addParameter() {
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

}
