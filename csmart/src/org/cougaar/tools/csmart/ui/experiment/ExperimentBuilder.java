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

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.Dimension;
import java.awt.BorderLayout;
import org.cougaar.tools.csmart.ui.util.NamedFrame;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.tools.csmart.ui.component.SocietyComponent;
import java.net.URL;
import org.cougaar.tools.csmart.ui.Browser;

public class ExperimentBuilder extends JFrame {
  private static final String FILE_MENU = "File";
  private static final String EXIT_MENU_ITEM = "Exit";
  private static final String EDIT_MENU = "Edit";
  private static final String REMOVE_MENU_ITEM = "Remove";
  private static final String HELP_MENU = "Help";
  protected static final String HELP_DOC = "help.html";
  protected static final String ABOUT_CSMART_ITEM = "About CSMART";
  protected static final String ABOUT_DOC = "../help/about-csmart.html";
  protected static final String HELP_MENU_ITEM = "Help";
  private Action helpAction = new AbstractAction(HELP_MENU_ITEM) {
      public void actionPerformed(ActionEvent e) {
	URL help = (URL)this.getClass().getResource(HELP_DOC);
	if (help != null)
	  Browser.setPage(help);
      }
    };
  private Action aboutAction = new AbstractAction(ABOUT_CSMART_ITEM) {
      public void actionPerformed(ActionEvent e) {
	URL help = (URL)this.getClass().getResource(ABOUT_DOC);
	if (help != null)
	  Browser.setPage(help);
      }
    };

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

  private Action removeAction = new AbstractAction(REMOVE_MENU_ITEM) {
    public void actionPerformed(ActionEvent e) {
      removeSelectedItems();
    }
  };
  private JPopupMenu popupMenu = new JPopupMenu();
  private JPopupMenu societiesMenu = new JPopupMenu();
  private JPopupMenu impactsMenu = new JPopupMenu();
  private JPopupMenu metricsMenu = new JPopupMenu();
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
  private Action[] fileActions = {
    new AbstractAction(EXIT_MENU_ITEM) {
      public void actionPerformed(ActionEvent e) {
        NamedFrame.getNamedFrame().removeFrame(ExperimentBuilder.this);
	dispose();
      }
    }
  };

  private Action[] editActions = {
    removeAction
  };

  private Action[] helpActions = {
    helpAction,
    aboutAction
  };

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

  public ExperimentBuilder(CSMART csmart, Experiment experiment) {
    JMenuBar menuBar = new JMenuBar();
    getRootPane().setJMenuBar(menuBar);
    JMenu fileMenu = new JMenu(FILE_MENU);
    for (int i = 0; i < fileActions.length; i++) {
      fileMenu.add(fileActions[i]);
    }
    JMenu editMenu = new JMenu(EDIT_MENU);
    for (int i = 0; i < editActions.length; i++) {
      editMenu.add(editActions[i]);
    }
    JMenu helpMenu = new JMenu(HELP_MENU);
    for (int i = 0; i < helpActions.length; i++) {
      helpMenu.add(helpActions[i]);
    }
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
    menuBar.add(fileMenu);
    menuBar.add(editMenu);
    menuBar.add(helpMenu);
    setJMenuBar(menuBar);
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
    rightPanel.add(propScrollPane, BorderLayout.CENTER);
    rightPanel.add(new JLabel("Unbound Properties"), BorderLayout.NORTH);
    splitPane.setRightComponent(rightPanel);
    splitPane.setLeftComponent(tree);
    getContentPane().add(splitPane);
    setExperiment(experiment);
    pack();
    show();
  }

  /**
   * Set experiment to edit; used to re-use a running editor
   * to edit a different experiment.
   */

  public void reinit(Experiment experiment) {
    System.out.println("ExperimentBuilder: " + experiment.getName());
    setExperiment(experiment);
  }


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

  private void displayEditorForNode(DefaultMutableTreeNode node) {
    if (node == null) return;
    Object o = (node.getUserObject());
    if (o instanceof SocietyComponent) {
      propModel.clear(); // clear out any previous table entries
      propModel.setComponentProperties((SocietyComponent) o);
      rightPanel.add(propScrollPane, BorderLayout.CENTER);
    }
  }

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

  private void removeAllChildren(DefaultMutableTreeNode parent) {
    parent.removeAllChildren();
    model.nodeStructureChanged(parent);
  }

  private void removeSelectedItems() {
    TreePath[] selectionPaths = tree.getSelectionPaths();
    if (selectionPaths == null)
      return;
    DefaultMutableTreeNode[] nodes = new DefaultMutableTreeNode[selectionPaths.length];
    for (int i = 0; i < nodes.length; i++) {
      nodes[i] = (DefaultMutableTreeNode) selectionPaths[i].getLastPathComponent();
    }
    for (int i = 0; i < nodes.length; i++) {
      model.removeNodeFromParent(nodes[i]);
    }
  }

  private void setExperiment(Experiment experiment) {
    model.removeTreeModelListener(myTreeModelListener);
    this.experiment = experiment;
    societies.removeAllChildren();
    impacts.removeAllChildren();
    metrics.removeAllChildren();
    model.nodeStructureChanged(root);
    for (int i = 0, n = experiment.getSocietyComponentCount(); i < n; i++) {
      //      System.out.println("size = " + experiment.getSocietyComponentCount());
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

  public void addSocietyComponent(SocietyComponent sc) {
    DefaultMutableTreeNode node = new DefaultMutableTreeNode(sc, false);
    model.insertNodeInto(node, societies, societies.getChildCount());
  }
  public void addImpact(Impact impact) {
    DefaultMutableTreeNode node = new DefaultMutableTreeNode(impact, false);
    model.insertNodeInto(node, impacts, impacts.getChildCount());
  }
  public void addMetric(Metric metric) {
    DefaultMutableTreeNode node = new DefaultMutableTreeNode(metric, false);
    model.insertNodeInto(node, metrics, metrics.getChildCount());
  }
}
