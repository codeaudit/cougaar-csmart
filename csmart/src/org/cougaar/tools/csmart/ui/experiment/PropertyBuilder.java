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
package org.cougaar.tools.csmart.ui.experiment;

import java.awt.event.*;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import org.cougaar.tools.csmart.ui.component.CompositeName;
import org.cougaar.tools.csmart.ui.component.ConfigurableComponent;
import org.cougaar.tools.csmart.ui.component.Property;
import org.cougaar.tools.csmart.ui.component.SocietyComponent;

public class PropertyBuilder extends JPanel {
  private static final String REMOVE_MENU_ITEM = "Remove";
  private String variationScheme;
  private DefaultTreeModel model;
  private ExperimentTree tree;
  private Experiment experiment;
  private DefaultMutableTreeNode root;
  private DefaultMutableTreeNode societies;
  private DefaultMutableTreeNode impacts;
  private DefaultMutableTreeNode metrics;
  private PropTableModel propModel = new PropTableModel();
  private JTable propTable = new JTable(propModel);
  private JScrollPane propScrollPane = new JScrollPane(propTable);
  private JPanel rightPanel = new JPanel(new BorderLayout());
  private JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
  private JPopupMenu popupMenu = new JPopupMenu();
  private JPopupMenu societiesMenu = new JPopupMenu();
  private JPopupMenu impactsMenu = new JPopupMenu();
  private JPopupMenu metricsMenu = new JPopupMenu();
  private JTextField trialCountField;

  /**
   * Define actions for pop-up menus on societies, impacts, and metrics.
   */

  private Action removeAction = new AbstractAction(REMOVE_MENU_ITEM) {
    public void actionPerformed(ActionEvent e) {
      removeSelectedItems();
    }
  };
  private Action[] popupActions = {
    removeAction
  };
  private Action[] societiesActions = {
    new AbstractAction(REMOVE_MENU_ITEM + " All Societies") {
      public void actionPerformed(ActionEvent e) {
        removeAllChildren(societies);
      }
    }
  };
  private Action[] impactsActions = {
    new AbstractAction(REMOVE_MENU_ITEM + " All Impacts") {
      public void actionPerformed(ActionEvent e) {
        removeAllChildren(impacts);
      }
    }
  };
  private Action[] metricsActions = {
    new AbstractAction(REMOVE_MENU_ITEM + " All Metrics") {
      public void actionPerformed(ActionEvent e) {
        removeAllChildren(metrics);
      }
    }
  };

  /**
   * Define mouse listener to pop-up menus.
   */

  private MouseListener mouseListener = new MouseAdapter() {
    public void mouseClicked(MouseEvent e) {
      if (e.isPopupTrigger()) doPopup(e);
    }
    public void mousePressed(MouseEvent e) {
      if (e.isPopupTrigger()) doPopup(e);
    }
    public void mouseReleased(MouseEvent e) {
      if (e.isPopupTrigger()) doPopup(e);
    }
  };

  /**
   * Define tree selection listener to display unbound properties
   * for selected component in tree.
   */

  private TreeSelectionListener myTreeSelectionListener = new TreeSelectionListener() {
    public void valueChanged(TreeSelectionEvent e) {
      TreePath path = tree.getSelectionPath();
      if (path != null) {
        DefaultMutableTreeNode node =
        (DefaultMutableTreeNode) path.getLastPathComponent();
        displayEditorForNode(node);
      }
    }
  };

  /**
   * Define table model listener to update minimum trial count
   * whenever values in table are changed.
   */

  private TableModelListener myTableModelListener = new TableModelListener() {
    public void tableChanged(TableModelEvent e) {
      // experimental property values changed; need to update trials
      experiment.invalidateTrials();
      updateTrialCount();
    }
  };

  /**
   * Define tree model listener to update the societies, metrics, and impacts
   * in the experiment when the user modifies the tree.
   */

  private TreeModelListener myTreeModelListener = new TreeModelListener() {
    public void treeNodesChanged(TreeModelEvent e) {
      reconcileExperimentNodes();
    }
    public void treeNodesInserted(TreeModelEvent e) {
      reconcileExperimentNodes();
    }
    public void treeNodesRemoved(TreeModelEvent e) {
      reconcileExperimentNodes();
    }
    public void treeStructureChanged(TreeModelEvent e) {
      reconcileExperimentNodes();
    }
  };

  /**
   * User interface for specifying values of properties of configurable
   * components to use in trials.
   */

  public PropertyBuilder(Experiment experiment) {
    root = new DefaultMutableTreeNode();
    model = new DefaultTreeModel(root);
    societies = new DefaultMutableTreeNode(ExperimentTree.SOCIETIES);
    impacts = new DefaultMutableTreeNode(ExperimentTree.IMPACTS);
    metrics = new DefaultMutableTreeNode(ExperimentTree.METRICS);
    societies.setAllowsChildren(true);
    impacts.setAllowsChildren(true);
    metrics.setAllowsChildren(true);
    model.insertNodeInto(societies, root, 0);
    model.insertNodeInto(impacts, root, 1);
    model.insertNodeInto(metrics, root, 2);
    model.setAsksAllowsChildren(true);
    tree = new ExperimentTree(model);
    tree.setExpandsSelectedPaths(true);
    tree.setRootVisible(false);
    tree.expandNode(societies);
    tree.expandNode(impacts);
    tree.expandNode(metrics);
    tree.addMouseListener(mouseListener);
    tree.setPreferredSize(new Dimension(250, 200));
    model.addTreeModelListener(myTreeModelListener);
    tree.addTreeSelectionListener(myTreeSelectionListener);

    propTable.setCellSelectionEnabled(true);
    propTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    propModel.addTableModelListener(myTableModelListener);
    rightPanel.add(propScrollPane, BorderLayout.CENTER);
    rightPanel.add(new JLabel("Unbound Properties", SwingConstants.CENTER), 
		   BorderLayout.NORTH);
    splitPane.setRightComponent(rightPanel);
    splitPane.setLeftComponent(tree);
    setLayout(new BorderLayout());
    JPanel trialPanel = new JPanel();
    JLabel variationLabel = new JLabel("Variation Scheme:");
    String[] variationSchemes = Experiment.getVariationSchemes();
    JComboBox cb = new JComboBox(variationSchemes);
    variationScheme = variationSchemes[0];
    cb.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	variationSchemeCB_actionPerformed(e);
      }
    });
    trialPanel.add(variationLabel);
    trialPanel.add(cb);
    JLabel trialLabel = new JLabel("Minimum number of Trials:");
    trialCountField = new JTextField("1", 4);
    trialCountField.setEditable(false);
    trialPanel.add(trialLabel);
    trialPanel.add(trialCountField);
    add(trialPanel, BorderLayout.NORTH);
    add(splitPane, BorderLayout.CENTER);
    splitPane.setDividerLocation(100);
    for (int i = 0; i < popupActions.length; i++) {
      popupMenu.add(popupActions[i]);
    }
    for (int i = 0; i < societiesActions.length; i++) {
      societiesMenu.add(societiesActions[i]);
    }
    for (int i = 0; i < impactsActions.length; i++) {
      impactsMenu.add(impactsActions[i]);
    }
    for (int i = 0; i < metricsActions.length; i++) {
      metricsMenu.add(metricsActions[i]);
    }
    setExperiment(experiment);
  }

  /**
   * Display information about the experiment.  Called to 
   * re-use the display.
   */

  public void setExperiment(Experiment experiment) {
    model.removeTreeModelListener(myTreeModelListener);
    this.experiment = experiment;
    societies.removeAllChildren();
    impacts.removeAllChildren();
    metrics.removeAllChildren();
    model.nodeStructureChanged(root);
    for (int i = 0, n = experiment.getSocietyComponentCount(); i < n; i++) {
      addSocietyComponent(experiment.getSocietyComponent(i));
    }
    for (int i = 0, n = experiment.getImpactCount(); i < n; i++) {
      addImpact(experiment.getImpact(i));
    }
    for (int i = 0, n = experiment.getMetricCount(); i < n; i++) {
      addMetric(experiment.getMetric(i));
    }
    tree.expandNode(societies);
    tree.expandNode(impacts);
    tree.expandNode(metrics);
    model.addTreeModelListener(myTreeModelListener);
    updateTrialCount();
  }

  /**
   * Clear out old property name/value pairs in table and display new ones.
   */

  private void displayEditorForNode(DefaultMutableTreeNode node) {
    if (node == null) return;
    Object o = (node.getUserObject());
    if (o instanceof SocietyComponent) {
      propModel.clear(); // clear out any previous table entries
      propModel.setComponentProperties((SocietyComponent) o);
      rightPanel.add(propScrollPane, BorderLayout.CENTER);
    }
  }

  /**
   * Update societies, impacts and metrics in the experiment when the user
   * modifies the tree.
   */

  private void reconcileExperimentNodes() {
    int nSocieties = societies.getChildCount();
    SocietyComponent[] societyComponentsAry = new SocietyComponent[nSocieties];
    for (int i = 0; i < nSocieties; i++) {
      societyComponentsAry[i] =
        (SocietyComponent) ((DefaultMutableTreeNode) societies.getChildAt(i)).getUserObject();
    }
    experiment.setSocietyComponents(societyComponentsAry);
    int nImpacts = impacts.getChildCount();
    Impact[] impactAry = new Impact[nImpacts];
    for (int i = 0; i < nImpacts; i++) {
      impactAry[i] =
        (Impact) ((DefaultMutableTreeNode) impacts.getChildAt(i)).getUserObject();
    }
    experiment.setImpacts(impactAry);
    int nMetrics = metrics.getChildCount();
    Metric[] metricAry = new Metric[nMetrics];
    for (int i = 0; i < nMetrics; i++) {
      metricAry[i] =
        (Metric) ((DefaultMutableTreeNode) metrics.getChildAt(i)).getUserObject();
    }
    experiment.setMetrics(metricAry);
    experiment.invalidateTrials(); // and force experiment to recreate trials
  }

  /**
   * Display the correct popup menu.
   */

  private void doPopup(MouseEvent e) {
    TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
    if (selPath == null) return;
    // set the selected node to be the node the mouse is pointing at
    tree.setSelectionPath(selPath);
    DefaultMutableTreeNode popupNode =
      (DefaultMutableTreeNode) selPath.getLastPathComponent();
    Object o = popupNode.getUserObject();
    if (o instanceof SocietyComponent) {
      popupMenu.show(tree, e.getX(), e.getY());
    } else if (o instanceof Impact) {
      popupMenu.show(tree, e.getX(), e.getY());
    } else if (o instanceof Metric) {
      popupMenu.show(tree, e.getX(), e.getY());
    } else if (o instanceof Experiment) {
      popupMenu.show(tree, e.getX(), e.getY());
    } else if (popupNode == societies) {
      societiesMenu.show(tree, e.getX(), e.getY());
    } else if (popupNode == impacts) {
      impactsMenu.show(tree, e.getX(), e.getY());
    } else if (popupNode == metrics) {
      metricsMenu.show(tree, e.getX(), e.getY());
    }
  }

  /**
   * Remove all children of the specified parent from the tree.
   */

  private void removeAllChildren(DefaultMutableTreeNode parent) {
    parent.removeAllChildren();
    model.nodeStructureChanged(parent);
  }

  /**
   * Remove selected items from the tree.
   */

  private void removeSelectedItems() {
    TreePath[] selectionPaths = tree.getSelectionPaths();
    if (selectionPaths == null)
      return;
    DefaultMutableTreeNode[] nodes = 
      new DefaultMutableTreeNode[selectionPaths.length];
    for (int i = 0; i < nodes.length; i++) {
      nodes[i] =
	(DefaultMutableTreeNode) selectionPaths[i].getLastPathComponent();
    }
    for (int i = 0; i < nodes.length; i++) {
      model.removeNodeFromParent(nodes[i]);
    }
    propModel.clear(); // selected items were removed, so clear prop table
  }

  /**
   * Add society component to tree.
   */

  private void addSocietyComponent(SocietyComponent sc) {
    DefaultMutableTreeNode node = new DefaultMutableTreeNode(sc, false);
    model.insertNodeInto(node, societies, societies.getChildCount());
  }

  /**
   * Add impact component to tree.
   */

  private void addImpact(Impact impact) {
    DefaultMutableTreeNode node = new DefaultMutableTreeNode(impact, false);
    model.insertNodeInto(node, impacts, impacts.getChildCount());
  }

  /**
   * Add metric component to tree.
   */

  private void addMetric(Metric metric) {
    DefaultMutableTreeNode node = new DefaultMutableTreeNode(metric, false);
    model.insertNodeInto(node, metrics, metrics.getChildCount());
  }

  /**
   * User modified variation scheme; update minimum number of trials.
   */

  public void variationSchemeCB_actionPerformed(ActionEvent e) {
    variationScheme = (String)((JComboBox)e.getSource()).getSelectedItem();
    updateTrialCount();
  }

  private void updateTrialCount() {
    experiment.setVariationScheme(variationScheme);
    trialCountField.setText(Integer.toString(experiment.getTrialCount()));
  }

}
