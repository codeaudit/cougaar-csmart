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
import org.cougaar.tools.csmart.core.property.InvalidPropertyValueException;
import org.cougaar.tools.csmart.core.property.ModifiableConfigurableComponent;
import org.cougaar.tools.csmart.core.property.ModifiableComponent;
import org.cougaar.tools.csmart.core.property.PropertiesListener;
import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.tools.csmart.core.property.PropertyEvent;
import org.cougaar.tools.csmart.core.property.PropertyHelper;
import org.cougaar.tools.csmart.core.property.PropertyListener;
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
  implements ActionListener, PropertiesListener, PropertyListener,
  TreeSelectionListener, HyperlinkListener {
  JComboBox selectionCB;
  JSplitPane configCompPanel;
  JTree tree;
  DefaultMutableTreeNode root;
  PropertyTable propertyTable = null;
  JScrollPane tableScrollPane;
  Hashtable componentToProperty = new Hashtable();
  Hashtable propertyToComponent = new Hashtable();
  Hashtable propertyToLabel = new Hashtable();
  boolean isEditable;
  ModifiableComponent[] compsToConfig = null; // support an array of things to edit/view
  ModifiableComponent componentToConfigure = null;
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
   * Creates a new <code>PropertyEditorPanel</code> instance for a set of components to edit
   *
   * @param configComps a <code>ModifiableConfigurableComponent[]</code> array of components to edit - usually really just to vie
   */
  public PropertyEditorPanel(ModifiableComponent[] configComps,
                             boolean isEditable) {
    // caller decides if this panel should allow editing
    this.isEditable = isEditable;
    // create the configComp panel
    configCompPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    setLayout(new BorderLayout());
    add(configCompPanel, BorderLayout.CENTER);
    compsToConfig = configComps;
    componentToConfigure = configComps[0];
    setModifiableConfigurableComponent();
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

  /**
   * ActionListener interface.
   */
  public void actionPerformed(ActionEvent e) {
    Object source = e.getSource();
    Property p = (Property)componentToProperty.get(source);
    // handle user interface components for properties
    if (p != null) {
      Object value = null;
      if (source instanceof JTextField) {
	value = ((JTextField)source).getText();
      } else if (source instanceof JComboBox) {
	value = ((JComboBox)source).getSelectedItem();
      }
      if (value != null) { 
	setValueInProperty(p, value);
	return;
      }
    }
  }

  /**
   * Handle conversions here; if possible make the value be an object
   * of the class specified in property.getClass() and use that object
   * to set the property value.
   */
  private void setValueInProperty(Property p, Object value) {
    try {
      if (value instanceof String && value.toString().equals("")) {
        value = null;           // Use default
      } else {
        value = PropertyHelper.validateValue(p, value);
      }
      p.setValue(value);
    } catch (InvalidPropertyValueException e) {
      if(log.isErrorEnabled()) {
        log.error("PropertyBuilder: can't set value in property: ", e);
      }
    }
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
      //      componentToConfigure.addPropertiesListener(this);
      compsToConfig[i].addPropertiesListener(this);
    }
    configCompPanel.validate();
    configCompPanel.setDividerLocation(200);
  }

  /**
   * Make a tree from the property names in the selected configurable
   * component.
   */
  private DefaultMutableTreeNode makeTree() {
    List props = new ArrayList();
    // FIXME: This will have trouble if there are more than one property
    // with the same name
    for (int i = 0; i < compsToConfig.length; i++) {
      props.addAll(compsToConfig[i].getPropertyNamesList());
    }
    //    SortedSet names = new TreeSet(componentToConfigure.getPropertyNamesList());
    SortedSet names = new TreeSet(props);
    for (Iterator i = names.iterator(); i.hasNext(); ) {
      CompositeName name = (CompositeName) i.next();
      addPropertyName(name);
    }
    return root;
  }

  /**
   * Display user interface components for property names.
   * Called when user makes a selection in the tree.
   */
  private void displayComponents(Collection propertyNames) {
    for (Iterator i = propertyNames.iterator(); i.hasNext(); ) {
      CompositeName propName = (CompositeName) i.next();
      for (int j = 0; j < compsToConfig.length; j++) {
	Property property = compsToConfig[j].getProperty(propName);
	// FIXME: If there are 2 properties of the same name
	// this finds only the first instance of it
	if (property != null) {
	  addComponentForProperty(property);
	  break;
	}
      }
    }
  }

  /**
   * Add user interface component for a property.
   */
  private void addComponentForProperty(Property property) {
    propertyTable.addProperty(property);
    // don't add yourself as listener until problem with changing propery
    // values is fixed: see propertyValueChanged
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
   * property from the tree.
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
    PropertyTreeNode node = new PropertyTreeNode(name);
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
      // stop any editing in progress, and accept the value
      int row = propertyTable.getEditingRow();
      int column = propertyTable.getEditingColumn();
      if (row != -1 && column != -1) 
        propertyTable.getCellEditor(row, column).stopCellEditing();
      propertyTable.removeAll(); // clear what was displayed
      PropertyTreeNode node = (PropertyTreeNode) o;
      displayComponents(node.getPropertyNames());
    }
  }

  /**
   * End TreeSelectionListener interface.
   */

  private void readObject(ObjectInputStream ois)
    throws IOException, ClassNotFoundException
  {
    ois.defaultReadObject();
    createLogger();
  }

}
