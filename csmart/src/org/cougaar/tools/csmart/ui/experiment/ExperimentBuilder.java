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

import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import org.cougaar.tools.csmart.ui.util.NamedFrame;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import java.net.URL;
import org.cougaar.tools.csmart.ui.Browser;

public class ExperimentBuilder extends JFrame {
  private static final String FILE_MENU = "File";
  private static final String SAVE_MENU_ITEM = "Save";
  private static final String EXIT_MENU_ITEM = "Exit";
  private static final String HELP_MENU = "Help";
  protected static final String HELP_DOC = "help.html";
  protected static final String ABOUT_CSMART_ITEM = "About CSMART";
  protected static final String ABOUT_DOC = "../help/about-csmart.html";
  protected static final String HELP_MENU_ITEM = "Help";
  private Experiment experiment;
  private boolean isEditable;
  private boolean isRunnable;
  private JTabbedPane tabbedPane;
  private UnboundPropertyBuilder propertyBuilder;
  private HostConfigurationBuilder hostConfigurationBuilder;
  private TrialBuilder trialBuilder;

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
        hostConfigurationBuilder.save();
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

  private Action[] helpActions = {
    helpAction,
    aboutAction
  };

  public ExperimentBuilder(CSMART csmart, Experiment experiment) {
    this.experiment = experiment;
    isEditable = experiment.isEditable();
    isRunnable = experiment.isRunnable();
    JMenuBar menuBar = new JMenuBar();
    getRootPane().setJMenuBar(menuBar);
    JMenu fileMenu = new JMenu(FILE_MENU);
    for (int i = 0; i < fileActions.length; i++) {
      fileMenu.add(fileActions[i]);
    }
    JMenu helpMenu = new JMenu(HELP_MENU);
    for (int i = 0; i < helpActions.length; i++) {
      helpMenu.add(helpActions[i]);
    }
    menuBar.add(fileMenu);
    menuBar.add(helpMenu);
    setJMenuBar(menuBar);

    tabbedPane = new JTabbedPane();
    propertyBuilder = new UnboundPropertyBuilder(experiment);
    tabbedPane.add("Properties", propertyBuilder);
    hostConfigurationBuilder = 
      new HostConfigurationBuilder(experiment, csmart);
    tabbedPane.add("Configurations", hostConfigurationBuilder);
    // only display trial builder for non-database experiments
    if (!experiment.isInDatabase()) {
      trialBuilder = new TrialBuilder(experiment);
      tabbedPane.add("Trials", trialBuilder);
    }
    // after starting all the editors, set experiment editability to false
    experiment.setEditable(false);
    experiment.setRunnable(false); // not runnable when editing it
    getContentPane().add(tabbedPane);
    pack();
    setSize(450, 400);

    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
	exit();
      }
    });
    show();
  }

  private void exit() {
    // before exiting, restore experiment's and society's editability
    // FIXME Restore on society!!!!
    if (isEditable) 
      experiment.setEditable(isEditable);
    if (isRunnable)
      experiment.setRunnable(isRunnable);
    // If the experiment now has a society and is otherwise runnable, say so
    if (experiment.getSocietyComponentCount() > 0)
      experiment.setRunnable(true);
  }

  /**
   * Set experiment to edit; used to re-use a running editor
   * to edit a different experiment.  Set the new experiment in all
   * the user interfaces (tabbed panes).
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
    propertyBuilder.reinit(experiment);
    hostConfigurationBuilder.reinit(experiment);
    // only display trial builder for non-database experiments
    if (experiment.isInDatabase()) {
      if (trialBuilder != null) {
        tabbedPane.remove(trialBuilder);
        trialBuilder = null;
      } else {
        if (trialBuilder == null) {
          trialBuilder = new TrialBuilder(experiment);
          tabbedPane.add("Trials", trialBuilder);
        } else
          trialBuilder.reinit(experiment);
      }
    }
    experiment.setEditable(false);
    experiment.setRunnable(false);
  }

}
