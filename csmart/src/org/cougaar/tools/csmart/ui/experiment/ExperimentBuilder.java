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
import javax.swing.event.*;
import java.awt.event.*;
import org.cougaar.tools.csmart.ui.util.NamedFrame;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import java.net.URL;
import org.cougaar.tools.csmart.ui.Browser;

public class ExperimentBuilder extends JFrame {
  private static final String FILE_MENU = "File";
  private static final String EXIT_MENU_ITEM = "Exit";
  private static final String HELP_MENU = "Help";
  protected static final String HELP_DOC = "help.html";
  protected static final String ABOUT_CSMART_ITEM = "About CSMART";
  protected static final String ABOUT_DOC = "../help/about-csmart.html";
  protected static final String HELP_MENU_ITEM = "Help";
  private Experiment experiment;
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
    new AbstractAction(EXIT_MENU_ITEM) {
      public void actionPerformed(ActionEvent e) {
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

    JTabbedPane tabbedPane = new JTabbedPane();
    propertyBuilder = new UnboundPropertyBuilder(experiment);
    tabbedPane.add("Properties", propertyBuilder);
    hostConfigurationBuilder = new HostConfigurationBuilder(experiment);
    tabbedPane.add("Configurations", hostConfigurationBuilder);
    trialBuilder = new TrialBuilder(experiment);
    tabbedPane.add("Trials", trialBuilder);
    
    getContentPane().add(tabbedPane);
    reinit(experiment);
    pack();
    setSize(450, 400);
    show();
  }

  /**
   * Set experiment to edit; used to re-use a running editor
   * to edit a different experiment.  Set the new experiment in all
   * the user interfaces (tabbed panes).
   */

  public void reinit(Experiment experiment) {
    this.experiment = experiment;
    propertyBuilder.setExperiment(experiment);
    hostConfigurationBuilder.setExperiment(experiment);
    trialBuilder.setExperiment(experiment);
  }

}
