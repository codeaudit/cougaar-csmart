/*
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 *
 * © Copyright 2000, 2001 BBNT Solutions LLC
 * </copyright>
 */

package org.cougaar.tools.csmart.ui.builder;

import java.awt.BorderLayout;
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
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.cougaar.tools.csmart.ui.Browser;
import org.cougaar.tools.csmart.ui.component.*;
import org.cougaar.tools.csmart.ui.experiment.PropTableModelBase;
import org.cougaar.tools.csmart.ui.util.NamedFrame;
import org.cougaar.tools.csmart.ui.util.Renderer;
import org.cougaar.tools.csmart.ui.viewer.CSMART;

/**
 * User interface that supports building a society.
 */

public class TreeBuilder
  extends JFrame
  implements ActionListener, PropertiesListener, PropertyListener,
  TreeSelectionListener, HyperlinkListener
{
  private static final String FILE_MENU = "File";
  private static final String EXIT_MENU_ITEM = "Exit";

  private static final String HELP_MENU = "Help";

  protected static final String HELP_DOC = "help.html";
  protected static final String ABOUT_CSMART_ITEM = "About CSMART";
  protected static final String ABOUT_DOC = "../help/about-csmart.html";
  protected static final String HELP_MENU_ITEM = "Help";

  private String[] helpMenuItems = {
    HELP_MENU_ITEM, ABOUT_CSMART_ITEM
  };

  private static final String SOCIETY_TAB_LABEL = "Society Specification";
  private static final String AGENT_TAB_LABEL = "Agent Specification";
  private static final String PLUGIN_TAB_LABEL = "Plugin Specification";
  private static final String LOAD_TAB_LABEL = "Load Specification";

  CSMART csmart; // top level viewer, gives access to save method, etc.
  JComboBox selectionCB;
  JSplitPane societyPanel;
  JTree tree;
  DefaultMutableTreeNode root;
  GridPanel2 componentPanel = null;
  JScrollPane componentScrollPane;
  SocietyComponent societyCfg;
  Hashtable componentToProperty = new Hashtable();
  Hashtable propertyToComponent = new Hashtable();
  Hashtable propertyToLabel = new Hashtable();

  // for the window listener
  private static TreeBuilder builder = null;

  String[] societyNames = { "<selection>", "ABC Society", "Scalable Society" };

  String[] societyClassNames = {
    "foo",
    "org.cougaar.tools.csmart.configgen.abcsociety.ABCSociety",
    "org.cougaar.tools.csmart.scalability.ScalabilityXSociety"
  };

  public TreeBuilder(CSMART csmart, SocietyComponent society) {
    this.csmart = csmart;

    // initialize menus and gui panels
    JMenuBar menuBar = new JMenuBar();
    getRootPane().setJMenuBar(menuBar);
    JMenu fileMenu = new JMenu(FILE_MENU);

    JMenuItem exitMenuItem = new JMenuItem(EXIT_MENU_ITEM);
    exitMenuItem.addActionListener(this);
    fileMenu.add(exitMenuItem);

    menuBar.add(fileMenu);

    // placeholder help menu
    JMenu helpMenu = new JMenu(HELP_MENU);
    for (int i = 0; i < helpMenuItems.length; i++) {
      JMenuItem mItem = new JMenuItem(helpMenuItems[i]);
      mItem.addActionListener(this);
      helpMenu.add(mItem);
    }
    menuBar.add(helpMenu);

    // create the society panel
    societyPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(societyPanel, BorderLayout.CENTER);

    getContentPane().add(societyPanel);

    if (society != null)
      setSocietyComponent(society);

    setSize(600,500);
    setVisible(true);
  }

  /**
   * Set society component to edit; used to re-use a running editor
   * to edit a different society.
   */

  public void reinit(SocietyComponent societyComponent) {
    setSocietyComponent(societyComponent);
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

    // handle our own menu items
    String s = ((AbstractButton)source).getActionCommand();
    if (s.equals(EXIT_MENU_ITEM)) {
      // notify top-level viewer that user quit the builder
      NamedFrame.getNamedFrame().removeFrame(this);
      dispose();
    } else if (s.equals(HELP_MENU_ITEM)) {
      URL help = (URL)getClass().getResource(HELP_DOC);
      if (help != null)
	Browser.setPage(help);
    } else if (s.equals(ABOUT_CSMART_ITEM)) {
      URL about = (URL)getClass().getResource(ABOUT_DOC);
      if (about != null)
	Browser.setPage(about);
    }	       

  }// end action listener

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
      System.err.println("TreeBuilder: can't set value in property: " + e);
      e.printStackTrace();
    }
  }

  /**
   * Return property value or default value as a string
   * using the Renderer utility.
   */

  private String renderValue(Property property) {
    Class cls = property.getPropertyClass();
    Object value = null;
    value = property.getValue();
    if (cls == null) {
      System.err.println("getPropertyClass is null for " + property.getName());
      if (value == null) {
	cls = Object.class;
      } else {
	cls = value.getClass();
      }
    }
    return Renderer.renderValue(cls, value);
  }

  public void hyperlinkUpdate(HyperlinkEvent e) {
    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
      Browser.setPage(e.getURL());
    }
  }

  private void setSocietyComponent(SocietyComponent societyCfg) {
    this.societyCfg = societyCfg;
    Iterator names = societyCfg.getPropertyNames();
    if (tree != null)
      societyPanel.remove(tree);
    // create tree and model before adding to it
    // as the add methods reference the tree and model
    root = new DefaultMutableTreeNode("Properties");
    tree = new JTree(new DefaultTreeModel(root));
    makeTree();
    tree.expandPath(new TreePath(root));
    tree.setEditable(true);
    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    tree.addTreeSelectionListener(this);
    societyPanel.setLeftComponent(new JScrollPane(tree));
    componentPanel = new GridPanel2();
    componentScrollPane = new JScrollPane(componentPanel);
    JPanel rightPanel = new JPanel(new BorderLayout());
    rightPanel.add(componentScrollPane);
    URL url = societyCfg.getDescription();
    if (url != null) {
      JTextPane pane = new JTextPane();
      try {
        pane.setEditable(false);
        pane.addHyperlinkListener(this);
        pane.setPage(url);
        rightPanel.add(pane, BorderLayout.NORTH);
      } catch (IOException ioe) {
        ioe.printStackTrace();
      }
    } else {
      JLabel pane = new JLabel("No description available");
      rightPanel.add(pane, BorderLayout.NORTH);
    }
    societyPanel.setRightComponent(rightPanel);
    societyCfg.addPropertiesListener(this);
    societyPanel.validate();
  }

  /**
   * Make a tree from the property names in the selected configurable
   * component.
   */

  private DefaultMutableTreeNode makeTree() {
    SortedSet names = new TreeSet(societyCfg.getPropertyNamesList());
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
      Property property = societyCfg.getProperty(propName);
      addComponentForProperty(property);
    }
  }


  /**
   * Add user interface component for a property.
   */

  private void addComponentForProperty(Property property) {
    componentPanel.addProperty(property);
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
        System.out.println("TreeBuilder: Property added: " + name);
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
        System.out.println("TreeBuilder: Property removed: " + prop.getName());
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
    componentPanel.removeProperty(property);
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
      ((PropTableModelBase)(componentPanel.getModel())).getRowForProperty(p);
    if (row != -1) {
      componentPanel.setValueAt(p.getValue(), row, 1);
      componentPanel.revalidate();
      componentPanel.repaint();
    }
    p.addPropertyListener(this);
  }

  public void propertyOtherChanged(PropertyEvent e) {
    //    System.out.println("TreeBuilder: propertyOtherChanged: " +
    //		       e.getWhatChanged());
    //    e.getProperty().printProperty(System.out);
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
      componentPanel.removeAll(); // clear what was displayed
      PropertyTreeNode node = (PropertyTreeNode) o;
      displayComponents(node.getPropertyNames());
    }
  }

  /**
   * End TreeSelectionListener interface.
   */

  public static void main(String[] args) {
    TreeBuilder builder = new TreeBuilder(null, null);
    final ABCSocietyComponent abc = new ABCSocietyComponent();
    builder.setSocietyComponent(abc);
    abc.initProperties();
    JMenu testMenu = new JMenu("Test");
    JMenuItem testMenuItem = new JMenuItem("Test Change");
    testMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	abc.test();
      }
    });
    testMenu.add(testMenuItem);
    builder.getRootPane().getJMenuBar().add(testMenu);
    builder.pack();
    builder.setDefaultCloseOperation(EXIT_ON_CLOSE);
  }

}
