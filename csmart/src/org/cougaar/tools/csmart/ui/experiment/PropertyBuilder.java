/* 
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 * 
 *       © Copyright 2001 by BBNT Solutions LLC.
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
  private static final String[] variationSchemes = {
    "Univariate", "Bivariate", "Multivariate", "Random" };
  public static final int VARY_ONE_DIMENSION = 0;
  public static final int VARY_TWO_DIMENSION = 1;
  public static final int VARY_SEQUENTIAL = 2;
  public static final int VARY_RANDOM = 3;
  private int variationScheme;
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
	updateTrialCount(experiment, variationScheme);
      }
    }
  };

  /**
   * Define table model listener to update minimum trial count
   * whenever values in table are changed.
   */

  private TableModelListener myTableModelListener = new TableModelListener() {
    public void tableChanged(TableModelEvent e) {
      System.out.println("Updating trial count...");
      updateTrialCount(experiment, variationScheme);
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
    JComboBox cb = new JComboBox(variationSchemes);
    cb.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	variationSchemeCB_actionPerformed(e);
      }
    });
    variationScheme = 0; // vary one dimension 
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
    variationScheme = ((JComboBox)e.getSource()).getSelectedIndex();
    updateTrialCount(experiment, variationScheme);
  }

  /**
   * Update the trial count and display it; called when user
   * changes trial variation scheme, edits any property, or
   * changes the experiment tree.
   */

  public static int getTrialCount(Experiment experiment, int variationScheme) {
    int numberOfTrials = 0;
    ArrayList experimentValueCounts = new ArrayList(100);
    int n = experiment.getSocietyComponentCount();
    for (int i = 0; i < n; i++) {
      SocietyComponent society = experiment.getSocietyComponent(i);
      Iterator names = society.getPropertyNames();
      while (names.hasNext()) {
	Property property = society.getProperty((CompositeName)names.next());
	List values = property.getExperimentValues();
	if (values != null) 
	  experimentValueCounts.add(new Integer(values.size()));
      }
    }
    // one dimension: sum the counts of experiment values
    // but only count nominal value the first time
    if (variationScheme == VARY_ONE_DIMENSION) {
      for (int i = 0; i < experimentValueCounts.size(); i++)
	numberOfTrials = numberOfTrials + 
	  ((Integer)experimentValueCounts.get(i)).intValue() - 1;
      numberOfTrials++; // add one to use nominal value for first time
    }
    // sequential or random: (all combinations): multiply counts
    else if (variationScheme == VARY_RANDOM ||
	     variationScheme == VARY_SEQUENTIAL) {
      numberOfTrials = 1;
      for (int i = 0; i < experimentValueCounts.size(); i++)
	numberOfTrials = numberOfTrials *
	  ((Integer)experimentValueCounts.get(i)).intValue();
    }
    // two dimension: ???
    if (numberOfTrials == 0)
      numberOfTrials = 1;  // always assume at least one trial
    return numberOfTrials;
  }

  private void updateTrialCount(Experiment experiment,
				int variationScheme) {
    int n = getTrialCount(experiment, variationScheme);
    trialCountField.setText(Integer.toString(n));
  }

  /**
   * Get scheme for auto-generating trial values; one of
   * PropertyBuilder.ONE_DIMENSION, TWO_DIMENSION, VARY_SEQUENTIAL, VARY_RANDOM
   */

  public int getVariationScheme() {
    return variationScheme;
  }
}
