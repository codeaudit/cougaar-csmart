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
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import org.cougaar.tools.csmart.ui.component.CompositeName;
import org.cougaar.tools.csmart.ui.component.ConfigurableComponent;
import org.cougaar.tools.csmart.ui.component.Property;
import org.cougaar.tools.csmart.ui.component.SocietyComponent;
import org.cougaar.tools.csmart.ui.component.ImpactComponent;
import org.cougaar.tools.csmart.ui.component.ModifiableConfigurableComponent;

public class UnboundPropertyBuilder extends JPanel {
  private static final String REMOVE_MENU_ITEM = "Remove";
  private String variationScheme;
  private DefaultTreeModel model;
  private ExperimentTree tree;
  private Experiment experiment;
  private boolean isEditable;
  private boolean isRunnable;
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

  public UnboundPropertyBuilder(Experiment experiment) {
    this.experiment = experiment;
    isEditable = experiment.isEditable();
    isRunnable = experiment.isRunnable();
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
    initDisplay();
  }

  /**
   * Display information about the experiment.  Called to 
   * re-use the display.
   */

  public void reinit(Experiment newExperiment) {
    // restore editable flag on previous experiment
    if (isEditable) 
      experiment.setEditable(isEditable);
    if (isRunnable)
      experiment.setRunnable(isRunnable);
    experiment = newExperiment;
    isEditable = newExperiment.isEditable();
    isRunnable = newExperiment.isRunnable();
    initDisplay();
  }

  private void initDisplay() {
    model.removeTreeModelListener(myTreeModelListener);
    societies.removeAllChildren();
    impacts.removeAllChildren();
    metrics.removeAllChildren();
    model.nodeStructureChanged(root);
    DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
    if (!isEditable) {
      renderer.setTextNonSelectionColor(Color.gray);
      renderer.setTextSelectionColor(Color.gray);
    }
    tree.setCellRenderer(renderer);
    tree.setEditable(isEditable);
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
    // FIXME!!!! Must I add in the unbound properties from the Metrics & Impacts here??
    // Or is this a one-by-one kind of thing?
    if (o instanceof ModifiableConfigurableComponent) {
      propModel.clear(); // clear out any previous table entries
      propModel.setComponentProperties((ModifiableConfigurableComponent) o);
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
      SocietyComponent society = (SocietyComponent) ((DefaultMutableTreeNode) societies.getChildAt(i)).getUserObject();
      societyComponentsAry[i] = society;
      society.setEditable(false); // so society editability tracks experiment editability
    }
    experiment.setSocietyComponents(societyComponentsAry);
    int nImpacts = impacts.getChildCount();
    ImpactComponent[] impactAry = new ImpactComponent[nImpacts];
    for (int i = 0; i < nImpacts; i++) {
      impactAry[i] =
        (ImpactComponent) ((DefaultMutableTreeNode) impacts.getChildAt(i)).getUserObject();
      impactAry[i].setEditable(false);
    }
    experiment.setImpacts(impactAry);
    int nMetrics = metrics.getChildCount();
    //    MetricComponent[] metricAry = new MetricComponent[nMetrics];
    Metric[] metricAry = new Metric[nMetrics];
    for (int i = 0; i < nMetrics; i++) {
      metricAry[i] =
        (Metric) ((DefaultMutableTreeNode) metrics.getChildAt(i)).getUserObject();
      //        (MetricComponent) ((DefaultMutableTreeNode) metrics.getChildAt(i)).getUserObject();
      //metricAry[i].setEditable(false);
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
    } else if (o instanceof ImpactComponent) {
      popupMenu.show(tree, e.getX(), e.getY());
      //    } else if (o instanceof MetricComponent) {
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
    for (int i = 0; i < parent.getChildCount(); i++) {
      DefaultMutableTreeNode node = 
	(DefaultMutableTreeNode)parent.getChildAt(i);
      // make removed society component editable again
      Object userObject = node.getUserObject();
      if (userObject != null &&
	  userObject instanceof ModifiableConfigurableComponent)
	((ModifiableConfigurableComponent)userObject).setEditable(true);
    }
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
      // make removed society component editable again
      Object userObject = nodes[i].getUserObject();
      if (userObject != null &&
	  userObject instanceof ModifiableConfigurableComponent)
	((ModifiableConfigurableComponent)userObject).setEditable(true);
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

  private void addImpact(ImpactComponent impact) {
    DefaultMutableTreeNode node = new DefaultMutableTreeNode(impact, false);
    model.insertNodeInto(node, impacts, impacts.getChildCount());
  }

  /**
   * Add metric component to tree.
   */

  //  private void addMetric(MetricComponent metric) {
  private void addMetric(Metric metric) {
    DefaultMutableTreeNode node = new DefaultMutableTreeNode(metric, false);
    model.insertNodeInto(node, metrics, metrics.getChildCount());
  }

  /**
   * User modified variation scheme; update minimum number of trials.
   */

  public void variationSchemeCB_actionPerformed(ActionEvent e) {
    variationScheme = (String)((JComboBox)e.getSource()).getSelectedItem();
    if (variationScheme.equals(Experiment.VARY_TWO_DIMENSION)) {
      JOptionPane.showMessageDialog(this,
				    "Variation scheme not implemented",
				    "Variation Scheme",
				    JOptionPane.WARNING_MESSAGE);
      return;
    }
    updateTrialCount();
  }

  private void updateTrialCount() {
    experiment.setVariationScheme(variationScheme);
    trialCountField.setText(Integer.toString(experiment.getTrialCount()));
  }

}
