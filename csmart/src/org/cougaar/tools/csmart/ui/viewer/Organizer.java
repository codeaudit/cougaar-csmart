/*
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 *
 * © Copyright 2000, 2001 BBNT Solutions LLC
 * </copyright>
 */

package org.cougaar.tools.csmart.ui.viewer;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.*;
import java.awt.event.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.*;
import java.lang.reflect.Constructor;
import org.cougaar.tools.csmart.ui.builder.TreeBuilder;
import org.cougaar.tools.csmart.ui.component.*;
import org.cougaar.tools.csmart.ui.experiment.Impact;
import org.cougaar.tools.csmart.ui.experiment.Metric;
import org.cougaar.tools.csmart.scalability.ScalabilityXSociety;
import org.cougaar.tools.csmart.configgen.abcsociety.ABCSociety;
import org.cougaar.tools.csmart.ui.experiment.*;

public class Organizer extends JScrollPane {
    private static final String DEFAULT_FILE_NAME = "Default Workspace.bin";

    private static final String FRAME_TITLE = "CSMART Launcher";

    private static final long UPDATE_DELAY = 5000L;

    private boolean updateNeeded = false;

    private long nextUpdate = 0L;

    private String workspaceFileName;
    private CSMART csmart;
    private DefaultMutableTreeNode root;
    DefaultTreeModel model;
    private OrganizerTree workspace;
    // to guarantee unique names
    private static class UniqueNameSet extends HashSet {
        private String prefix;
        private int nameCounter = 0;
        public UniqueNameSet(String prefix) {
            this.prefix = prefix;
        }
        public void init(Object[] things, Method getNameMethod) {
            Object[] noArgs = new Object[0];
            for (int i = 0; i < things.length; i++) {
                try {
                    String name = (String) getNameMethod.invoke(things[i], noArgs);
                    add(name);
                } catch (Exception e) {
		  System.out.println("Reading: " + things[i]);
                    e.printStackTrace();
                }
            }
        }
                
        public String generateName() {
            return generateName(prefix);
        }
        public String generateName(String name) {
            if (contains(name)) {
                String base = name;
                do {
                    name = base + ++nameCounter;
                } while (contains(name));
            }
            return name;
        }
    }
                         
    private UniqueNameSet societyNames = new UniqueNameSet("Society");
    private UniqueNameSet experimentNames = new UniqueNameSet("Experiment");
    private UniqueNameSet impactNames = new UniqueNameSet("Impact");
    private UniqueNameSet metricNames = new UniqueNameSet("Metric");

    private DefaultMutableTreeNode popupNode;
    private JPopupMenu societyMenu = new JPopupMenu();
    private JPopupMenu impactMenu = new JPopupMenu();
    private JPopupMenu metricMenu = new JPopupMenu();
    private JPopupMenu experimentMenu = new JPopupMenu();
    private JPopupMenu treeMenu = new JPopupMenu();
    private JPopupMenu rootMenu = new JPopupMenu();
    private Action[] rootAction = {
        new AbstractAction("Rename") {
            public void actionPerformed(ActionEvent e) {
                renameWorkspace();
            }
        },
        new AbstractAction("New Folder") {
            public void actionPerformed(ActionEvent e) {
                newFolder(popupNode);
            }
        },
        new AbstractAction("New Experiment") {
            public void actionPerformed(ActionEvent e) {
                newExperiment(popupNode);
            }
        },
        new AbstractAction("New Society") {
            public void actionPerformed(ActionEvent e) {
                newSociety(popupNode);
            }
        },
        new AbstractAction("New Impact") {
            public void actionPerformed(ActionEvent e) {
                newImpact(popupNode);
            }
        },
        new AbstractAction("New Metric") {
            public void actionPerformed(ActionEvent e) {
                newMetric(popupNode);
            }
        }
    };
  private Action[] experimentAction = {
        new AbstractAction("Edit", new ImageIcon(getClass().getResource("EB16.gif"))) {
            public void actionPerformed(ActionEvent e) {
                startExperimentBuilder(popupNode);
            }
        },
        new AbstractAction("Run", new ImageIcon(getClass().getResource("EC16.gif"))) {
            public void actionPerformed(ActionEvent e) {
                startConsole(popupNode);
            }
        },
        new AbstractAction("Rename") {
            public void actionPerformed(ActionEvent e) {
                renameExperiment(popupNode);
            }
        },
        new AbstractAction("Delete") {
            public void actionPerformed(ActionEvent e) {
                deleteExperiment(popupNode);
            }
        },
	new AbstractAction("Copy") {
	  public void actionPerformed(ActionEvent e) {
	    copyExperimentInNode(popupNode);
	  }
	}
  };
    private Action[] societyAction = {
        new AbstractAction("Edit", new ImageIcon(getClass().getResource("SB16.gif"))) {
            public void actionPerformed(ActionEvent e) {
                startBuilder(popupNode);
            }
        },
        new AbstractAction("New Experiment",
                           new ImageIcon(getClass().getResource("EB16.gif"))) {
            public void actionPerformed(ActionEvent e) {
                startExperimentBuilder(popupNode);
            }
        },
        new AbstractAction("Run", new ImageIcon(getClass().getResource("EC16.gif"))) {
            public void actionPerformed(ActionEvent e) {
                startConsole(popupNode);
            }
        },
        new AbstractAction("Rename") {
            public void actionPerformed(ActionEvent e) {
                renameSociety(popupNode);
            }
        },
        new AbstractAction("Delete") {
            public void actionPerformed(ActionEvent e) {
                deleteSociety(popupNode);
            }
        },
        new AbstractAction("Copy") {
            public void actionPerformed(ActionEvent e) {
                copySocietyInNode(popupNode);
            }
        }
    };
    private Action[] impactAction = {
//          new AbstractAction("Edit", new ImageIcon(getClass().getResource("SB16.gif"))) {
//              public void actionPerformed(ActionEvent e) {
//                  startBuilder(popupNode);
//              }
//          },
//          new AbstractAction("Run", new ImageIcon(getClass().getResource("EC16.gif"))) {
//              public void actionPerformed(ActionEvent e) {
//                  startConsole(popupNode);
//              }
//          },
        new AbstractAction("New Experiment",
                           new ImageIcon(getClass().getResource("EB16.gif"))) {
            public void actionPerformed(ActionEvent e) {
                startExperimentBuilder(popupNode);
            }
        },
        new AbstractAction("Rename") {
            public void actionPerformed(ActionEvent e) {
                renameImpact(popupNode);
            }
        },
        new AbstractAction("Delete") {
            public void actionPerformed(ActionEvent e) {
                deleteImpact(popupNode);
            }
        },
	new AbstractAction("Copy") {
	  public void actionPerformed(ActionEvent e) {
	    copyImpactInNode(popupNode);
	  }
	}
    };
    private Action[] metricAction = {
//          new AbstractAction("Edit", new ImageIcon(getClass().getResource("SB16.gif"))) {
//              public void actionPerformed(ActionEvent e) {
//                  startBuilder(popupNode);
//              }
//          },
//          new AbstractAction("Run", new ImageIcon(getClass().getResource("EC16.gif"))) {
//              public void actionPerformed(ActionEvent e) {
//                  startConsole(popupNode);
//              }
//          },
        new AbstractAction("New Experiment",
                           new ImageIcon(getClass().getResource("EB16.gif"))) {
            public void actionPerformed(ActionEvent e) {
                startExperimentBuilder(popupNode);
            }
        },
        new AbstractAction("Rename") {
            public void actionPerformed(ActionEvent e) {
                renameMetric(popupNode);
            }
        },
        new AbstractAction("Delete") {
            public void actionPerformed(ActionEvent e) {
                deleteMetric(popupNode);
            }
        }
    };
    private Action[] treeAction = {
        new AbstractAction("New Folder") {
            public void actionPerformed(ActionEvent e) {
	      while (! popupNode.getAllowsChildren())
		popupNode = (DefaultMutableTreeNode)popupNode.getParent();
	      newFolder(popupNode);
            }
        },
        new AbstractAction("New Society") {
            public void actionPerformed(ActionEvent e) {
	      while (! popupNode.getAllowsChildren())
		popupNode = (DefaultMutableTreeNode)popupNode.getParent();
	      newSociety(popupNode);
            }
        },
        new AbstractAction("New Impact") {
            public void actionPerformed(ActionEvent e) {
	      while (! popupNode.getAllowsChildren())
		popupNode = (DefaultMutableTreeNode)popupNode.getParent();
	      newImpact(popupNode);
            }
        },
        new AbstractAction("New Metric") {
            public void actionPerformed(ActionEvent e) {
	      while (! popupNode.getAllowsChildren())
		popupNode = (DefaultMutableTreeNode)popupNode.getParent();
	      newMetric(popupNode);
            }
        },
        new AbstractAction("New Experiment") {
            public void actionPerformed(ActionEvent e) {
	      while (! popupNode.getAllowsChildren())
		popupNode = (DefaultMutableTreeNode)popupNode.getParent();
	      newExperiment(popupNode);
            }
        },
        new AbstractAction("Rename") {
            public void actionPerformed(ActionEvent e) {
                renameFolder(popupNode);
            }
        },
        new AbstractAction("Delete") {
            public void actionPerformed(ActionEvent e) {
                deleteFolder(popupNode);
            }
        }
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

    private DefaultMutableTreeNode getSelectedNode() {
        TreePath selPath = workspace.getSelectionPath();
        if (selPath == null) return null;
        return (DefaultMutableTreeNode) selPath.getLastPathComponent();
    }

    public SocietyComponent[] getSelectedSocieties() {
        return (SocietyComponent[]) getSelectedLeaves(SocietyComponent.class);
    }

    public Impact[] getSelectedImpacts() {
        return (Impact[]) getSelectedLeaves(Impact.class);
    }

    public Metric[] getSelectedMetrics() {
        return (Metric[]) getSelectedLeaves(Metric.class);
    }

    public Experiment[] getSelectedExperiments() {
        return (Experiment[]) getSelectedLeaves(Experiment.class);
    }

    private Object[] getSelectedLeaves(Class leafClass) {
        TreePath[] paths = workspace.getSelectionPaths();
	if (paths == null)
	  return null;
        return getLeaves(leafClass, paths);
    }

    private Object[] getLeaves(Class leafClass, TreePath[] paths) {
        List result = new ArrayList();
        return fill(result, leafClass, paths);
    }

    private Object[] getLeaves(Class leafClass, TreeNode node) {
        TreeNode[] nodes = model.getPathToRoot(node);
        return getLeaves(leafClass,
                         new TreePath[] {new TreePath(nodes)});
    }

    private SocietyComponent[] getSocietyComponents(TreeNode node) {
        return (SocietyComponent[]) getLeaves(SocietyComponent.class, node);
    }

    private Impact[] getImpacts(TreeNode node) {
        return (Impact[]) getLeaves(Impact.class, node);
    }

    private Metric[] getMetrics(TreeNode node) {
        return (Metric[]) getLeaves(Metric.class, node);
    }

    private Experiment[] getExperiment(TreeNode node) {
        return (Experiment[]) getLeaves(Experiment.class, node);
    }

    private Object[] fill(List result, Class leafClass, TreePath[] paths) {
        for (int i = 0; i < paths.length; i++) {
            DefaultMutableTreeNode selNode =
                (DefaultMutableTreeNode) paths[i].getLastPathComponent();
            Object o = selNode.getUserObject();
            if (leafClass.isInstance(o)) {
                result.add(o);
            } else if (o instanceof String) {
                for (Enumeration e = selNode.depthFirstEnumeration(); e.hasMoreElements(); ) {
                    DefaultMutableTreeNode node =
                        (DefaultMutableTreeNode) e.nextElement();
                    o = node.getUserObject();
                    if (leafClass.isInstance(o)) {
                        result.add(o);
                    }
                }
            }
        }
        return result.toArray((Object[]) Array.newInstance(leafClass, result.size()));
    }

    public Object getSelectedObject() {
        DefaultMutableTreeNode selNode = getSelectedNode();
        if (selNode == null) return null;
        return selNode.getUserObject();
    }

    private void doPopup(MouseEvent e) {
        TreePath selPath = workspace.getPathForLocation(e.getX(), e.getY());
        if (selPath == null) return;
        // set the selected node to be the node the mouse is pointing at
        workspace.setSelectionPath(selPath);
        popupNode = (DefaultMutableTreeNode) selPath.getLastPathComponent();
        Object o = popupNode.getUserObject();
	if (popupNode.isRoot()) {
	    rootMenu.show(workspace, e.getX(), e.getY());
        } else if (o instanceof SocietyComponent) {
            societyMenu.show(workspace, e.getX(), e.getY());
        } else if (o instanceof Impact) {
            impactMenu.show(workspace, e.getX(), e.getY());
        } else if (o instanceof Metric) {
            metricMenu.show(workspace, e.getX(), e.getY());
        } else if (o instanceof Experiment) {
            experimentMenu.show(workspace, e.getX(), e.getY());
        } else if (o instanceof String) {
            treeMenu.show(workspace, e.getX(), e.getY());
        }
    }

    private void startBuilder(DefaultMutableTreeNode node) {
        csmart.runBuilder((SocietyComponent) node.getUserObject(), false);
    }
        
    private void startExperimentBuilder(DefaultMutableTreeNode node) {
        Object o = node.getUserObject();
        Experiment experiment;
        if (o instanceof SocietyComponent) {
            SocietyComponent sc = (SocietyComponent) o;
            String name = "Experiment for " + sc.getSocietyName();
            experiment = new Experiment(experimentNames.generateName(name),
                                        new SocietyComponent[] {sc},
                                        new Impact[0],
                                        new Metric[0]);
        } else if (o instanceof Impact) {
            Impact impact = (Impact) o;
            String name = "Experiment for " + impact.getName();
            experiment = new Experiment(experimentNames.generateName(name),
                                        new SocietyComponent[0],
                                        new Impact[] {impact},
                                        new Metric[0]);
        } else if (o instanceof Metric) {
            Metric metric = (Metric) o;
            String name = "Experiment for " + metric.getName();
            experiment = new Experiment(experimentNames.generateName(name),
                                        new SocietyComponent[0],
                                        new Impact[0],
                                        new Metric[] {metric});
        } else if (o instanceof Experiment) {
            experiment = (Experiment) o;
        } else if (o instanceof String) {
            experiment = new Experiment(experimentNames.generateName(),
                                        getSocietyComponents(node),
                                        getImpacts(node),
                                        getMetrics(node));
        } else {
            return;
        }
        if (o instanceof Experiment) {
            //Experiment already in tree
        } else {
            if (o instanceof String) {
                // Add experiment under the given node
                DefaultMutableTreeNode newNode = 
		  new DefaultMutableTreeNode(experiment);
                model.insertNodeInto(newNode, node, 0);
		// make the new node be the selected node
		workspace.setSelection(newNode);
            } else {
                // Add experiment as sibling of given node
                DefaultMutableTreeNode newNode =
                    new DefaultMutableTreeNode(experiment, false);
                DefaultMutableTreeNode parentNode =
                    (DefaultMutableTreeNode) node.getParent();
                model.insertNodeInto(newNode, parentNode, parentNode.getIndex(node) + 1);
		// make the new node be the selected node
		workspace.setSelection(newNode);
            }
            experimentNames.add(experiment.getExperimentName());
	    experiment.addModificationListener(myModificationListener);
        }
        csmart.runExperimentBuilder(experiment, false);
    }

    private void startConsole(DefaultMutableTreeNode node) {
        Object o = node.getUserObject();
        Experiment experiment;
        if (o instanceof SocietyComponent) {
            SocietyComponent sc = (SocietyComponent) o;
            String name = "Temp Experiment for  " + sc.getSocietyName();
            name = experimentNames.generateName(name);
            experiment = new Experiment(name,
                                        new SocietyComponent[] {sc},
                                        new Impact[0],
                                        new Metric[0]);
            DefaultMutableTreeNode parent = 
	      (DefaultMutableTreeNode) node.getParent();
	    DefaultMutableTreeNode newNode =
	      new DefaultMutableTreeNode(experiment, false);
            model.insertNodeInto(newNode, parent, parent.getChildCount());
            experimentNames.add(name);
	    experiment.addModificationListener(myModificationListener);
	    workspace.setSelection(newNode);
        } else if (o instanceof Experiment) {
            experiment = (Experiment) o;
        } else {
            return;
        }
        csmart.runConsole(experiment);
    }

    private static class ComboItem {
        public String name;
        public Class cls;
        public ComboItem(String name, Class cls) {
            this.cls = cls;
            this.name = name;
        }
        public String toString() {
            return name;
        }
    }
    private int nameCounter = 0;

    private static Class[] constructorArgTypes = {String.class};

    PropertyListener myPropertyListener = new PropertyListener() {
        public void propertyValueChanged(PropertyEvent e) {
            update();
        }
        
        public void propertyOtherChanged(PropertyEvent e) {
            update();
        }
    };

    PropertiesListener myPropertiesListener = new PropertiesListener() {
        public void propertyAdded(PropertyEvent e) {
            e.getProperty().addPropertyListener(myPropertyListener);
            update();
        }

        public void propertyRemoved(PropertyEvent e) {
            e.getProperty().removePropertyListener(myPropertyListener);
            update();
        }
    };

    ModificationListener myModificationListener = new ModificationListener() {
        public void modified(ModificationEvent event) {
            update();
        }
    };

    public boolean exitAllowed() {
        synchronized(root) {
            if (updateNeeded) {
                nextUpdate = System.currentTimeMillis();
                root.notify();
                while (updateNeeded) {
                    try {
                        root.wait();
                    } catch (InterruptedException ie) {
                    }
                }
            }
        }
        return true;
    }

    private DefaultMutableTreeNode newSociety(DefaultMutableTreeNode node) {
        Object[] values = {
            new ComboItem("Scalability", ScalabilityXSociety.class),
	    new ComboItem("ABC", ABCSociety.class)
        };
        Object answer =
            JOptionPane.showInputDialog(this, "Select Society Type",
                                        "Select Society",
                                        JOptionPane.QUESTION_MESSAGE,
                                        null,
                                        values,
                                        "ScalabilityX");
        if (answer instanceof ComboItem) {
            ComboItem item = (ComboItem) answer;
            String name = societyNames.generateName(item.name);
            while (true) {
                name = (String) JOptionPane.showInputDialog(this, "Enter Society Name",
                                                            "Name Society",
                                                            JOptionPane.QUESTION_MESSAGE,
                                                            null, null,
                                                            name);
                if (name == null) return null;
                if (!societyNames.contains(name)) break;
                int ok = JOptionPane.showConfirmDialog(this,
                                                       "Use an unique name",
                                                       "Society Name Not Unique",
                                                       JOptionPane.OK_CANCEL_OPTION,
                                                       JOptionPane.ERROR_MESSAGE);
                if (ok != JOptionPane.OK_OPTION) return null;
            }
            try {
                Constructor constructor = item.cls.getConstructor(constructorArgTypes);
                SocietyComponent sc = (SocietyComponent) constructor.newInstance(new String[] {name});
                sc.initProperties();
		DefaultMutableTreeNode newNode =
		  addSocietyToWorkspace(sc, node);
		workspace.setSelection(newNode);
		return newNode;
            } catch (Exception e) {
                e.printStackTrace();
		return null;
            }
        }
	return null;
    }

  private DefaultMutableTreeNode addSocietyToWorkspace(SocietyComponent sc,
						       DefaultMutableTreeNode node) {
    DefaultMutableTreeNode newNode = 
      new DefaultMutableTreeNode(sc, false);
    addNode(node, newNode);
    societyNames.add(sc.getSocietyName());
    installListeners(sc);
    return newNode;
  }

    private void installListeners(SocietyComponent sc) {
        sc.addPropertiesListener(myPropertiesListener);
        for (Iterator i = sc.getPropertyNames(); i.hasNext(); ) {
            Property p = sc.getProperty((CompositeName) i.next());
            PropertyEvent event = new PropertyEvent(p, PropertyEvent.PROPERTY_ADDED);
            myPropertiesListener.propertyAdded(event);
        }
        sc.addModificationListener(myModificationListener);
    }

    private void renameWorkspace() {
        String name = JOptionPane.showInputDialog("New workspace name");
	renameWorkspace(name);
    }

  private void renameWorkspace(String name) {
        if (name == null || name.equals(""))
            return;
        workspaceFileName = name;
        String rootName = this.workspaceFileName;
        int extpos = this.workspaceFileName.lastIndexOf('.');
        if (extpos >= 0) {
            rootName = rootName.substring(0, extpos);
        }
        root.setUserObject(rootName);
	model.nodeChanged(root);
        update();
    }

  private void renameSociety(DefaultMutableTreeNode node) {
    SocietyComponent societyComponent =
      (SocietyComponent) node.getUserObject();
    String name = JOptionPane.showInputDialog("New society name");
    renameSociety(node, name);
  }
  
  private void renameSociety(DefaultMutableTreeNode node, String name) {
    SocietyComponent societyComponent =
      (SocietyComponent) node.getUserObject();
    if (name == null || name.equals(societyComponent.getSocietyName()) || name.equals("")) return;
    while (true) {
      if (!societyNames.contains(name)) break;
      int ok = JOptionPane.showConfirmDialog(this,
					     "Use an unique name",
					     "Society Name Not Unique",
					     JOptionPane.OK_CANCEL_OPTION,
					     JOptionPane.ERROR_MESSAGE);
      if (ok != JOptionPane.OK_OPTION) return;
      name = JOptionPane.showInputDialog("New society name");
      if (name == null || name.equals("")) return;
    }
    societyNames.remove(societyComponent.getSocietyName());
    societyNames.add(name);
    societyComponent.setName(name);
    model.nodeChanged(node);
  }
  
    private void deleteSociety(DefaultMutableTreeNode node) {
        model.removeNodeFromParent(node);
        societyNames.remove(((SocietyComponent) node.getUserObject()).getSocietyName());
    }

    private void newImpact(DefaultMutableTreeNode node) {
        Object[] values = {
            new ComboItem("ABCImpact", ABCImpact.class),
            new ComboItem("High Impact", HighImpact.class),
            new ComboItem("Low Impact", LowImpact.class)
        };
        Object answer =
            JOptionPane.showInputDialog(this, "Select Impact Type",
                                        "Select Impact",
                                        JOptionPane.QUESTION_MESSAGE,
                                        null,
                                        values,
                                        "Low Impact");
        if (answer instanceof ComboItem) {
            ComboItem item = (ComboItem) answer;
            String name = impactNames.generateName(item.name);

	    // Special case code for ABC Impacts:
	    File xmlfile = null; // to read in
	    if (item.name.equals("ABCImpact")) {
	      // Use a fileChooser to get the name of the file
	      // if we had some other directory to start from,
	      // we could pass in that parameter
	      xmlfile = ABCImpact.getImpactFile(null, this);
	      if (xmlfile == null)
		return;
	    }
	    while (true) {
	      name = (String) JOptionPane.showInputDialog(this, "Enter Impact Name",
							  "Name Impact",
							  JOptionPane.QUESTION_MESSAGE,
							  null, null,
							  name);
	      if (name == null) return;
	      if (!impactNames.contains(name)) break;
	      int ok = JOptionPane.showConfirmDialog(this,
						     "Use an unique name",
						     "Impact Name Not Unique",
						     JOptionPane.OK_CANCEL_OPTION,
                                                       JOptionPane.ERROR_MESSAGE);
	      if (ok != JOptionPane.OK_OPTION) return;
	    }
            try {
                Constructor constructor = item.cls.getConstructor(constructorArgTypes);
                Impact impact =
                    (Impact) constructor.newInstance(new String[] {name});
		DefaultMutableTreeNode newNode =
		  addImpactToWorkspace(impact, node);
		workspace.setSelection(newNode);
		// If this is an ABCImpact, set the XML File as specified above
		if (xmlfile != null && impact instanceof ABCImpact)
		  ((ABCImpact)impact).setFile(xmlfile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private DefaultMutableTreeNode addImpactToWorkspace(Impact impact,
							DefaultMutableTreeNode node) {
      DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(impact, false);
      addNode(node, newNode);
      impactNames.add(impact.getName());
      //   installListeners(sc);
      return newNode;
    }

  private void renameImpact(DefaultMutableTreeNode node) {
    String name = JOptionPane.showInputDialog("New impact name");
    renameImpact(node, name);
  }
  
  private void renameImpact(DefaultMutableTreeNode node, String name) {
    Impact impact =
      (Impact) node.getUserObject();
    if (name == null || name.equals(impact.getName()) || name.equals("")) return;
    while (true) {
      if (!impactNames.contains(name)) break;
      int ok = JOptionPane.showConfirmDialog(this,
					     "Use an unique name",
					     "Impact Name Not Unique",
					     JOptionPane.OK_CANCEL_OPTION,
					     JOptionPane.ERROR_MESSAGE);
      if (ok != JOptionPane.OK_OPTION) return;
      name = JOptionPane.showInputDialog("New impact name");
      if (name == null || name.equals("")) return;
    }
    if (name != null) {
      impactNames.remove(impact.getName());
      impact.setName(name);
      impactNames.add(impact.getName());
      model.nodeChanged(node);
    }
  }

    private void deleteImpact(DefaultMutableTreeNode node) {
        if (node == null) return;
        model.removeNodeFromParent(node);
        impactNames.remove(((Impact) node.getUserObject()).getName());
    }

    private void newMetric(DefaultMutableTreeNode node) {
        Object[] values = {
            new ComboItem("Some Metric", SomeMetric.class),
            new ComboItem("Mo Metric", MoMetric.class)
        };
        Object answer =
            JOptionPane.showInputDialog(this, "Select Metric Type",
                                        "Select Metric",
                                        JOptionPane.QUESTION_MESSAGE,
                                        null,
                                        values,
                                        "Mo Metric");
        if (answer instanceof ComboItem) {
            ComboItem item = (ComboItem) answer;
            String name = metricNames.generateName(item.name);
            while (true) {
                name = (String) JOptionPane.showInputDialog(this, "Enter Metric Name",
                                                            "Name Metric",
                                                            JOptionPane.QUESTION_MESSAGE,
                                                            null, null,
                                                            name);
                if (name == null) return;
                if (!metricNames.contains(name)) break;
                int ok = JOptionPane.showConfirmDialog(this,
                                                       "Use an unique name",
                                                       "Metric Name Not Unique",
                                                       JOptionPane.OK_CANCEL_OPTION,
                                                       JOptionPane.ERROR_MESSAGE);
                if (ok != JOptionPane.OK_OPTION) return;
            }
            try {
                Constructor constructor = item.cls.getConstructor(constructorArgTypes);
                Metric metric =
                    (Metric) constructor.newInstance(new String[] {name});
		DefaultMutableTreeNode newNode =
		  addMetricToWorkspace(metric, node);
		workspace.setSelection(newNode);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private DefaultMutableTreeNode addMetricToWorkspace(Metric metric,
						      DefaultMutableTreeNode node) {
      DefaultMutableTreeNode newNode = 
	new DefaultMutableTreeNode(metric, false);
      addNode(node, newNode);
      metricNames.add(metric.getName());
      //      installListeners(sc);
      return newNode;
    }

  private void renameMetric(DefaultMutableTreeNode node) {
    String name = JOptionPane.showInputDialog("New metric name");
    renameMetric(node, name);
  }

  // Separate method for use from model...
  private void renameMetric(DefaultMutableTreeNode node, String name) {
    Metric metric =
      (Metric) node.getUserObject();
    if (name == null || name.equals(metric.getName()) || name.equals("")) return;
    while (true) {
      if (!metricNames.contains(name)) break;
      int ok = JOptionPane.showConfirmDialog(this,
					     "Use an unique name",
					     "Metric Name Not Unique",
					     JOptionPane.OK_CANCEL_OPTION,
					     JOptionPane.ERROR_MESSAGE);
      if (ok != JOptionPane.OK_OPTION) return;
      name = JOptionPane.showInputDialog("New metric name");
      if (name == null || name.equals("")) return;
    }
    if (name != null) {
      metricNames.remove(metric.getName());
      metric.setName(name);
      metricNames.add(metric.getName());
      model.nodeChanged(node); // update the model...
    }
  }

    private void deleteMetric(DefaultMutableTreeNode node) {
        if (node == null) return;
        model.removeNodeFromParent(node);
        metricNames.remove(((Metric) node.getUserObject()).getName());
    }

    private DefaultMutableTreeNode newExperiment(DefaultMutableTreeNode node) {
        String name = experimentNames.generateName();
        while (true) {
            name = (String) JOptionPane.showInputDialog(this, "Enter Experiment Name",
                                                        "Experiment Name",
                                                        JOptionPane.QUESTION_MESSAGE,
                                                        null, null,
                                                        name);
            if (name == null) return null;
            if (!experimentNames.contains(name)) break;
            int answer = JOptionPane.showConfirmDialog(this,
                                                       "Use an unique name",
                                                       "Experiment Name Not Unique",
                                                       JOptionPane.OK_CANCEL_OPTION,
                                                       JOptionPane.ERROR_MESSAGE);
            if (answer != JOptionPane.OK_OPTION) return null;
        }
        try {
            Experiment experiment = new Experiment(name);
	    DefaultMutableTreeNode newNode =
	      addExperimentToWorkspace(experiment, node);
	    workspace.setSelection(newNode);
	    return newNode;
        } catch (Exception e) {
            e.printStackTrace();
	    return null;
        }
    }

  private DefaultMutableTreeNode addExperimentToWorkspace(Experiment experiment, 
					       DefaultMutableTreeNode node) {
    DefaultMutableTreeNode newNode = 
      new DefaultMutableTreeNode(experiment, false);
    addNode(node, newNode);
    experimentNames.add(experiment.getExperimentName());
    // TODO: if add experiment properties,
    // then need to install listeners
    experiment.addModificationListener(myModificationListener);
    return newNode;
  }

    private void renameExperiment(DefaultMutableTreeNode node) {
        String name = JOptionPane.showInputDialog("New experiment name");
	renameExperiment(node, name);
    }

  // Separate method that takes a new name, for use by model.valueChanged...
  private void renameExperiment(DefaultMutableTreeNode node, String name) {
    Experiment experiment =
      (Experiment) node.getUserObject();
    if (name == null || name.equals(experiment.getName()) || name.equals("")) return;
    while (true) {
      if (!experimentNames.contains(name)) break;
      int ok = JOptionPane.showConfirmDialog(this,
					     "Use an unique name",
					     "Experiment Name Not Unique",
					     JOptionPane.OK_CANCEL_OPTION,
					     JOptionPane.ERROR_MESSAGE);
      if (ok != JOptionPane.OK_OPTION) return;
      name = JOptionPane.showInputDialog("New experiment name");
      if (name == null || name.equals("")) return;
    }
    if (name != null) {
      experimentNames.remove(experiment.getExperimentName());
      experiment.setName(name);
      experimentNames.add(name);
      // This next line is the key line, that updates things
      model.nodeChanged(node);
    }
  }

    private void deleteExperiment(DefaultMutableTreeNode node) {
        if (node == null) return;
        model.removeNodeFromParent(node);
        experimentNames.remove(((Experiment) node.getUserObject()).getName());
    }

    private void newFolder(DefaultMutableTreeNode node) {
        String name = JOptionPane.showInputDialog("New folder name");
        if (name != null) {
            DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(name, true);
            addNode(node, newNode);
            workspace.setSelection(newNode);
        }
    }

    private void addNode(DefaultMutableTreeNode node, DefaultMutableTreeNode newNode) {
        model.insertNodeInto(newNode, node, node.getChildCount());
	workspace.scrollPathToVisible(new TreePath(newNode.getPath()));
//          TreePath path = new TreePath(newNode.getPath());
//          workspace.expandPath(path);
//          workspace.setSelectionPath(path);
//          System.out.println(path);
    }

  private void renameFolder(DefaultMutableTreeNode node) {
    String name = JOptionPane.showInputDialog("New folder name");
    // FIXME Unique?
    renameFolder(node, name);
  }
  
  private void renameFolder(DefaultMutableTreeNode node, String name) {    
    // FIXME Unique?
    if (name != null && ! name.equals("")) {
      node.setUserObject(name);
      model.nodeChanged(node);
    }
  }

    private void deleteFolder(DefaultMutableTreeNode node) {
        if (node == root) return;
        if (node == null) return;
        model.removeNodeFromParent(node);
    }

    private void restore(String fileName) {
        PropertyListener l = null;
        try {
            File f = new File(fileName);
	    this.workspaceFileName = f.getPath();
            if (f.canRead()) {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
                try {
                    root = (DefaultMutableTreeNode) ois.readObject();
                    for (Enumeration e = root.depthFirstEnumeration();
                         e.hasMoreElements(); ) {
                        DefaultMutableTreeNode node =
                            (DefaultMutableTreeNode) e.nextElement();
                        Object o = node.getUserObject();
                        if (o instanceof SocietyComponent) {
                            installListeners((SocietyComponent) o);
                        } else if (o instanceof Experiment)
			  ((Experiment)o).addModificationListener(myModificationListener);
                    }
		} catch (Exception e) {
		  System.out.println("Organizer: can't read file: " + f);
                } finally {
                    ois.close();
		} 	  
	    }
	    if (root == null)
	      root = new DefaultMutableTreeNode();
            String rootName = f.getName();
            int extpos = rootName.lastIndexOf('.');
            if (extpos >= 0) 
                rootName = rootName.substring(0, extpos);
            root.setUserObject(rootName);
	    //            model = new DefaultTreeModel(root);
            model = createModel(this, root);
            Class[] noTypes = new Class[0];
            try {
                societyNames.init(getLeaves(SocietyComponent.class, root),
                                  SocietyComponent.class.getMethod("getSocietyName", noTypes));
                experimentNames.init(getLeaves(Experiment.class, root),
                                     Experiment.class.getMethod("getExperimentName", noTypes));
                impactNames.init(getLeaves(Impact.class, root),
                                 Impact.class.getMethod("getName", noTypes));
                metricNames.init(getLeaves(Metric.class, root),
                                 Metric.class.getMethod("getName", noTypes));
                return;
            } catch (NoSuchMethodException nsme) {
                nsme.printStackTrace();
            } catch (SecurityException se) {
                se.printStackTrace();
            }
        } catch (Exception ioe) {
            ioe.printStackTrace();
            return;
        }
    }

    private void initSocietyNames() {
      // initialize known society names
      Enumeration nodes = root.depthFirstEnumeration();
      while (nodes.hasMoreElements()) {
	DefaultMutableTreeNode node = 
	  (DefaultMutableTreeNode)nodes.nextElement();
	if (node.isLeaf()) {
	  Object o = node.getUserObject();
	  if (o instanceof SocietyComponent)
	    societyNames.add(((SocietyComponent)o).getSocietyName());
	}
      }
    }

  /** 
   * Force an update, which saves the current workspace.
   */

    public void save() {
        update();
    }

    private void save(String fileName) {
      //      System.out.println("Saving to: " + fileName);
        if (!fileName.endsWith(".bin"))
            fileName = fileName + ".bin";
        try {
            File f = new File(fileName);
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
            try {
                oos.writeObject(root);
            } finally {
                oos.close();
            }
        } catch (Exception ioe) {
            ioe.printStackTrace();
        }
    }

    private Thread updater = new Thread() {
        public void start() {
            updateNeeded = false;
            super.start();
        }
        public void run() {
            synchronized (root) {
                while (true) {
                    try {
                        long now = System.currentTimeMillis();
                        if (updateNeeded && now > nextUpdate) {
                            save(workspaceFileName);
                            updateNeeded = false;
                            root.notifyAll(); // In case anyone's waiting for the update to finish
                        } else if (updateNeeded) {
                            long delay = nextUpdate - now;
                            if (delay > 0) {
                                root.wait(delay);
                            }
                        } else {
                            root.wait();
                        }
                    } catch (InterruptedException ie) {
                    }
                }
            }
        }
    };

    private void update() {
        synchronized (root) {
            nextUpdate = System.currentTimeMillis() + UPDATE_DELAY;
            updateNeeded = true;
            root.notify();
        }
    }

    private TreeModelListener myModelListener = new TreeModelListener() {
        public void treeNodesChanged(TreeModelEvent e) {
            update();
        }
        public void treeNodesInserted(TreeModelEvent e) {
            update();
        }
        public void treeNodesRemoved(TreeModelEvent e) {
            update();
        }
        public void treeStructureChanged(TreeModelEvent e) {
            update();
        }
    };

    private void setFrameTitle() {
        setFrameTitle(workspace.getSelectionPath());
    }

    private void setFrameTitle(TreePath newSelection) {
        String doc;
        if (newSelection != null) {
            doc = newSelection.toString();
        } else {
            doc = null;
        }
        setFrameTitleDocument(doc);
    }

    private void setFrameTitleDocument(String doc) {
        JFrame frame =
            (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, workspace);
        if (frame == null) return;
        String oldTitle = frame.getTitle();
        if (oldTitle == null) {
            oldTitle = FRAME_TITLE;
        } else {
            int colon = oldTitle.indexOf(':');
            if (colon >= 0) oldTitle = oldTitle.substring(0, colon).trim();
        }
        if (doc == null)
            frame.setTitle(oldTitle);
        else
            frame.setTitle(oldTitle + ":" + doc);
    }

    private TreeSelectionListener mySelectionListener =
        new TreeSelectionListener()
    {
        public void valueChanged(TreeSelectionEvent e) {
            setFrameTitle(e.getNewLeadSelectionPath());
        }
    };

    private AncestorListener myAncestorListener =
        new AncestorListener()
    {
        public void ancestorAdded(AncestorEvent e) {
            setFrameTitle();
        }
        public void ancestorRemoved(AncestorEvent e) {}
        public void ancestorMoved(AncestorEvent e) {}
    };
        
    public Organizer(CSMART csmart) {
        this(csmart, null);
    }

    public Organizer(CSMART csmart, String workspaceFileName) {
        setPreferredSize(new Dimension(400, 100));
	JPanel panel = new JPanel(new BorderLayout());
	setViewportView(panel);
        this.csmart = csmart;
        if (workspaceFileName == null) {
            this.workspaceFileName = DEFAULT_FILE_NAME;
        } else {
            this.workspaceFileName = workspaceFileName;
        }
        restore(this.workspaceFileName);
        if (root == null) {
            root = new DefaultMutableTreeNode(null, true);
	    //            model = new DefaultTreeModel(root);
            model = createModel(this, root);
        }
// 	System.out.println("Organizer: setting root name to: " +
// 			   this.workspaceFileName);
//         String rootName = this.workspaceFileName;
//         int extpos = this.workspaceFileName.lastIndexOf('.');
//         if (extpos >= 0) {
//             rootName = rootName.substring(0, extpos);
//         }
//         root.setUserObject(rootName);
        model.setAsksAllowsChildren(true);
        model.addTreeModelListener(myModelListener);
        workspace = new OrganizerTree(model);
	workspace.setExpandsSelectedPaths(true);
	for (int i = 0; i < rootAction.length; i++) {
	  rootMenu.add(rootAction[i]);
	}
        for (int i = 0; i < societyAction.length; i++) {
            societyMenu.add(societyAction[i]);
        }
        for (int i = 0; i < impactAction.length; i++) {
            impactMenu.add(impactAction[i]);
        }
        for (int i = 0; i < metricAction.length; i++) {
            metricMenu.add(metricAction[i]);
        }
        for (int i = 0; i < experimentAction.length; i++) {
            experimentMenu.add(experimentAction[i]);
        }
        for (int i = 0; i < treeAction.length; i++) {
            treeMenu.add(treeAction[i]);
        }
        workspace.addTreeSelectionListener(mySelectionListener);
        workspace.addAncestorListener(myAncestorListener);
        workspace.addMouseListener(mouseListener);
        workspace.setSelection(root);
	expandTree(); // fully expand workspace tree
        panel.add(workspace);
        setViewportView(panel);
        updater.start();
    }

//      public void setRoot(ObjectInputStream init) {
//          root = (DefaultMutableTreeNode) init.readObject();
//      }

  /**
   * Create a new experiment and make it be the selected experiment.
   * Allows tools that need an experiment to create one
   * if it does not exist when the tool is invoked.
   */

    public void addExperiment() {
      DefaultMutableTreeNode newNode = newExperiment(root);
    }

    private DefaultMutableTreeNode findNode(Object userObject) {
      Enumeration nodes = root.depthFirstEnumeration();
      while (nodes.hasMoreElements()) {
	DefaultMutableTreeNode node = 
	  (DefaultMutableTreeNode)nodes.nextElement();
	if (node.isLeaf() &&
	    node.getUserObject().equals(userObject))
	  return node;
      }
      return null;
    }

    /**
     * Fully expand the tree; called in initialization
     * so that the initial view of the tree is fully expanded.
     */

    private void expandTree() {
      Enumeration nodes = root.depthFirstEnumeration();
      while (nodes.hasMoreElements()) {
	DefaultMutableTreeNode node = 
	  (DefaultMutableTreeNode)nodes.nextElement();
	workspace.expandPath(new TreePath(node.getPath()));
      }
    }

    /**
     * Create a new society and make it be the selected society.
     * Allows tools that need a society to create one,
     * if none exists when the tool is invoked.
     */

    public void addSociety() {
      DefaultMutableTreeNode newNode = newSociety(root);
    }

    public void addTreeSelectionListener(TreeSelectionListener listener) {
        workspace.addTreeSelectionListener(listener);
    }

  /**
   * Copy an experiment.
   */

  private void copyExperimentInNode(DefaultMutableTreeNode node) {
    copyExperiment((Experiment)node.getUserObject(), node.getParent());
  }

  public void copyExperiment(Experiment experiment, Object context) {
    // context is the tree node of the folder containing the experiment
    Experiment experimentCopy = experiment.copy(this, context);
    addExperimentToWorkspace(experimentCopy, (DefaultMutableTreeNode)context);
  }

  private void copySocietyInNode(DefaultMutableTreeNode node) {
    copySociety((SocietyComponent)node.getUserObject(), node.getParent());
  }

  public SocietyComponent copySociety(SocietyComponent society, 
				      Object context) {
    SocietyComponent societyCopy = society.copy(this, context);
    addSocietyToWorkspace(societyCopy, (DefaultMutableTreeNode)context);
    return societyCopy;
  }

  private void copyImpactInNode(DefaultMutableTreeNode node) {
    copyImpact((Impact)node.getUserObject(), node.getParent());
  }

  public Impact copyImpact(Impact impact, Object context) {
    Impact impactCopy = impact.copy(this, context);
    addImpactToWorkspace(impactCopy, (DefaultMutableTreeNode)context);
    return impactCopy;
  }

  public Metric copyMetric(Metric metric, Object context) {
    Metric metricCopy = metric.copy(this, context);
    addMetricToWorkspace(metricCopy, (DefaultMutableTreeNode)context);
    return metricCopy;
  }

  public String generateExperimentName(String name) {
    return experimentNames.generateName(name);
  }

  public String generateSocietyName(String name) {
    return societyNames.generateName(name);
  }

  public String generateImpactName(String name) {
    return impactNames.generateName(name);
  }

  public String generateMetricName(String name) {
    return metricNames.generateName(name);
  }

  // Override the default tree model
  // This is necessary because when edits happen on Nodes in a
  // DefaultTreeModel, the objects are treated as Strings.
  // The result is that whenever a user edits
  // an entry in the model, it becomes a string.
  // Instead, use the main rename functions from above
  // Note that the key thing those above methods do is call
  // model.nodeChanged(node) if the node in fact changed.
  // This causes the model to tell the tree the new size of the label,
  // for example
  private DefaultTreeModel createModel(final Organizer myorg, DefaultMutableTreeNode node) {
    return new DefaultTreeModel(node) {
	public void valueForPathChanged(TreePath path, Object newValue) {
	  if (newValue == null) return;
	  DefaultMutableTreeNode aNode = (DefaultMutableTreeNode)path.getLastPathComponent();
	  
	  if (aNode.getUserObject() instanceof SocietyComponent) {
	    //	    System.err.println("Resetting node type to SocietyComponent");
	    myorg.renameSociety(aNode, newValue.toString());
	    
	  } else if (aNode.getUserObject() instanceof Impact) {
	    myorg.renameImpact(aNode, newValue.toString());
	  } else if (aNode.getUserObject() instanceof Metric) {
	    myorg.renameMetric(aNode, newValue.toString());
	  } else if (aNode.getUserObject() instanceof Experiment) {
	    myorg.renameExperiment(aNode, newValue.toString());
	  } else if (aNode == myorg.root) {
	    // trying to rename the workspace
	    myorg.renameWorkspace(newValue.toString());
	  } else {
	    // This must be a folder?
	    aNode.setUserObject(newValue);
	    nodeChanged(aNode);
	  }	  
	}
      };
  }
}
