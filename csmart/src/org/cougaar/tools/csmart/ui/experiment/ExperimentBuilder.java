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

import java.awt.Component;
import java.awt.event.*;
import java.net.URL;
import javax.swing.*;
import javax.swing.event.*;
import org.cougaar.tools.csmart.ui.Browser;
import org.cougaar.tools.csmart.ui.console.ExperimentDB;
import org.cougaar.tools.csmart.ui.util.NamedFrame;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.tools.csmart.ui.viewer.GUIUtils;
import org.cougaar.tools.csmart.ui.component.ModificationListener;
import org.cougaar.tools.csmart.ui.component.ModificationEvent;
import org.cougaar.tools.csmart.ui.component.PopulateDb;

public class ExperimentBuilder extends JFrame implements ModificationListener {
  private static final String FILE_MENU = "File";
  private static final String SAVE_MENU_ITEM = "Save";
  private static final String EXIT_MENU_ITEM = "Close";
  private static final String FIND_MENU = "Find";
  private static final String FIND_HOST_MENU_ITEM = "Find Host...";
  private static final String FIND_NODE_MENU_ITEM = "Find Node...";
  private static final String FIND_AGENT_MENU_ITEM = "Find Agent...";
  private static final String HELP_MENU = "Help";
  protected static final String HELP_DOC = "help.html";
  protected static final String ABOUT_CSMART_ITEM = "About CSMART";
  protected static final String ABOUT_DOC = "/org/cougaar/tools/csmart/ui/help/about-csmart.html";
  protected static final String HELP_MENU_ITEM = "About Experiment Builder";
  private Experiment experiment;
  private CSMART csmart;
  private JTabbedPane tabbedPane;
  private UnboundPropertyBuilder propertyBuilder;
  private HostConfigurationBuilder hostConfigurationBuilder;
  private TrialBuilder trialBuilder;
  private ThreadBuilder threadBuilder;
  private boolean modified = false;
  private JMenu findMenu;
  private PopulateDb.ConflictHandler saveToDbConflictHandler =
    GUIUtils.createSaveToDbConflictHandler(this);

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
  private Action[] fileActions = {
    new AbstractAction(SAVE_MENU_ITEM) {
      public void actionPerformed(ActionEvent e) {
        save();
      }
    },
    new AbstractAction(EXIT_MENU_ITEM) {
      public void actionPerformed(ActionEvent e) {
	exit();
        NamedFrame.getNamedFrame().removeFrame(ExperimentBuilder.this);
	dispose();
      }
    }
  };
  private Action[] findActions = {
    new AbstractAction(FIND_HOST_MENU_ITEM) {
      public void actionPerformed(ActionEvent e) {
        hostConfigurationBuilder.findHost();
      }
    },
    new AbstractAction(FIND_NODE_MENU_ITEM) {
      public void actionPerformed(ActionEvent e) {
        hostConfigurationBuilder.findNode();
      }
    },
    new AbstractAction(FIND_AGENT_MENU_ITEM) {
      public void actionPerformed(ActionEvent e) {
        hostConfigurationBuilder.findAgent();
      }
    }
  };

  private Action[] helpActions = {
    helpAction,
    aboutAction
  };

  public ExperimentBuilder(CSMART csmart, Experiment experiment) {
    this.csmart = csmart;
    setExperiment(experiment);
    JMenuBar menuBar = new JMenuBar();
    getRootPane().setJMenuBar(menuBar);
    JMenu fileMenu = new JMenu(FILE_MENU);
    for (int i = 0; i < fileActions.length; i++) {
      fileMenu.add(fileActions[i]);
    }
    findMenu = new JMenu(FIND_MENU);
    for (int i = 0; i < findActions.length; i++) {
      findMenu.add(findActions[i]);
    }
    JMenu helpMenu = new JMenu(HELP_MENU);
    for (int i = 0; i < helpActions.length; i++) {
      helpMenu.add(helpActions[i]);
    }
    menuBar.add(fileMenu);
    menuBar.add(findMenu);
    menuBar.add(helpMenu);
    setJMenuBar(menuBar);

    tabbedPane = new JTabbedPane();
    tabbedPane.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        if (tabbedPane.getSelectedComponent().equals(hostConfigurationBuilder))
          findMenu.setEnabled(true);
        else
          findMenu.setEnabled(false);
      }
    });
    propertyBuilder = new UnboundPropertyBuilder(experiment, this);
    tabbedPane.add("Properties", propertyBuilder);
    hostConfigurationBuilder = 
      new HostConfigurationBuilder(experiment, this);
    tabbedPane.add("Configurations", hostConfigurationBuilder);
    // only display trial builder for non-database experiments
    if (experiment.isInDatabase()) {
      threadBuilder = new ThreadBuilder(experiment);
      tabbedPane.add("Threads", threadBuilder);
    } else {
      trialBuilder = new TrialBuilder(experiment);
      tabbedPane.add("Trials", trialBuilder);
    }
    // after starting all the editors, set experiment editability to false
    experiment.setEditInProgress(true);
    getContentPane().add(tabbedPane);
    pack();
    setSize(650, 400);

    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
	exit();
      }
    });
    show();
  }

  private void setExperiment(Experiment newExperiment) {
    if (experiment != null) {
      experiment.removeModificationListener(this);
    }
    experiment = newExperiment;
    if (experiment != null) {
      experiment.addModificationListener(this);
    }
  }

  public void modified(ModificationEvent e) {
    setModified(true);
  }

  private void exit() {
    saveSilently(); // if experiment from database was modified, save it
    // before exiting, restore experiment's and society's editability
    // FIXME Restore on society!!!!
    experiment.setEditInProgress(false);
    // If the experiment now has a society and is otherwise runnable, say so
    if (experiment.getSocietyComponentCount() > 0)
      experiment.setRunnable(true);
  }

  /**
   * Set experiment to edit; used to re-use a running editor
   * to edit a different experiment.  Set the new experiment in all
   * the user interfaces (tabbed panes).
   * Silently save the previous experiment if necessary.
   */

  public void reinit(Experiment newExperiment) {
    saveSilently();
    // restore editable flag on previous experiment
    experiment.setEditInProgress(false);
    experiment = newExperiment;
    propertyBuilder.reinit(experiment);
    hostConfigurationBuilder.reinit(experiment);
    // only display trial builder for non-database experiments
    // only display thread builder for database experiments
    if (experiment.isInDatabase()) {
      if (trialBuilder != null) {
        tabbedPane.remove(trialBuilder);
        trialBuilder = null;
      }
      if (threadBuilder == null) {
        threadBuilder = new ThreadBuilder(experiment);
        tabbedPane.add("Threads", threadBuilder);
      } else
        threadBuilder.reinit(experiment);
    } else {
      if (trialBuilder == null) {
        trialBuilder = new TrialBuilder(experiment);
        tabbedPane.add("Trials", trialBuilder);
      } else
        trialBuilder.reinit(experiment);
      if (threadBuilder != null) {
        tabbedPane.remove(threadBuilder);
        threadBuilder = null;
      }
    }
    experiment.setEditInProgress(true);
  }

  /**
   * Called by HostConfigurationBuilder or UnboundPropertyBuilder
   * if they modify an experiment.
   */

  public void setModified(boolean modified) {
    this.modified = modified;
  }

  /**
   * If the experiment was from the database and 
   * components were either added or removed or
   * the host-node-agent mapping was modified, then save it,
   * otherwise display a dialog indicating that no modifications were made.
   */

  private void save() {
    if (!experiment.isInDatabase())
      return;
    if (!modified) {
      String[] msg = {
        "No modifications were made.",
        "Do you want to save this experiment anyway?"
      };
      int answer =
        JOptionPane.showConfirmDialog(this, msg,
                                      "No Modifications",
                                      JOptionPane.OK_CANCEL_OPTION,
                                      JOptionPane.WARNING_MESSAGE);
      if (answer != JOptionPane.OK_OPTION) return;
      setModified(true);
    }
    saveHelper();
  }

  /**
   * Silently save experiment from database if modified.
   */

  private void saveSilently() {
    if (modified && experiment.isInDatabase())
      saveHelper();
  }

  private void saveHelper() {
    modified = false;
    final Component c = this;
    GUIUtils.timeConsumingTaskStart(c);
    GUIUtils.timeConsumingTaskStart(csmart);
    try {
      new Thread("Save") {
        public void run() {
          doSave();
          GUIUtils.timeConsumingTaskEnd(c);
          GUIUtils.timeConsumingTaskEnd(csmart);
        }
      }.start();
    } catch (RuntimeException re) {
      System.out.println("Error saving experiment: " + re);
      GUIUtils.timeConsumingTaskEnd(c);
    }
  }

  private void doSave() {
    if (experiment.isCloned()) {
      experiment.saveToDb(saveToDbConflictHandler);
      return;
    }
    // get unique name in both database and CSMART
    if (ExperimentDB.isExperimentNameInDatabase(experiment.getShortName())) {
      String name = csmart.getUniqueExperimentName(experiment.getShortName());
      if (name == null)
        return;
      experiment.setName(name);
    }
    experiment.saveToDb(saveToDbConflictHandler);
    experiment.setCloned(true);
  }

//    private void find() {
//      String s = JOptionPane.showInputDialog(this, "Find Host, Node or Agent:",
//                                             "Find", JOptionPane.PLAIN_MESSAGE);
//      if (s == null)
//        return;
//      s = s.trim();
//      if (s.length() == 0)
//        return;
//      hostConfigurationBuilder.find(s);
//    }
}
