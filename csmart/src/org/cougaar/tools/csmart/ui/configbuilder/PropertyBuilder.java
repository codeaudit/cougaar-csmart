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
import java.awt.Dimension;
import java.awt.event.*;
import javax.swing.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Vector;

import org.cougaar.tools.csmart.core.db.DBConflictHandler;
import org.cougaar.tools.csmart.core.db.DBUtils;
import org.cougaar.tools.csmart.core.db.PDbBase;
import org.cougaar.tools.csmart.core.property.ModifiableComponent;
import org.cougaar.tools.csmart.experiment.Experiment;
import org.cougaar.tools.csmart.recipe.RecipeComponent;
import org.cougaar.tools.csmart.society.SocietyComponent;
import org.cougaar.tools.csmart.ui.Browser;
import org.cougaar.tools.csmart.ui.util.NamedFrame;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.tools.csmart.ui.viewer.GUIUtils;
import org.cougaar.util.log.Logger;

/**
 * User interface that supports building a configurable component.
 */
public class PropertyBuilder extends JFrame implements ActionListener {
  private PropertyEditorPanel propertyEditor;
  private ModifiableComponent configComponent;
  private static final String FILE_MENU = "File";
  private static final String EXIT_MENU_ITEM = "Exit";
  private static final String SAVE_DB_MENU_ITEM = "Save To Database";

  private static final String HELP_MENU = "Help";

  protected static final String HELP_DOC = "help.html";
  protected static final String ABOUT_CSMART_ITEM = "About CSMART";
  protected static final String ABOUT_DOC = "/org/cougaar/tools/csmart/ui/help/about-csmart.html";
  protected static final String HELP_MENU_ITEM = "Help";

  private String[] helpMenuItems = {
    HELP_MENU_ITEM, ABOUT_CSMART_ITEM
  };

  private JMenuItem saveMenuItem;
  private CSMART csmart;
  private transient Logger log;
  private Experiment experiment;

  public PropertyBuilder(CSMART csmart, ModifiableComponent mc, 
                         Experiment experiment) {
    log = CSMART.createLogger(this.getClass().getName());
    this.csmart = csmart;
    this.experiment = experiment;

    // initialize menus and gui panels
    JMenuBar menuBar = new JMenuBar();
    getRootPane().setJMenuBar(menuBar);
    JMenu fileMenu = new JMenu(FILE_MENU);

    saveMenuItem = new JMenuItem(SAVE_DB_MENU_ITEM);
    saveMenuItem.addActionListener(this);
    fileMenu.add(saveMenuItem);
    fileMenu.addSeparator();

    JMenuItem exitMenuItem = new JMenuItem(EXIT_MENU_ITEM);
    exitMenuItem.addActionListener(this);
    fileMenu.add(exitMenuItem);

    menuBar.add(fileMenu);

    JMenu helpMenu = new JMenu(HELP_MENU);
    for (int i = 0; i < helpMenuItems.length; i++) {
      JMenuItem mItem = new JMenuItem(helpMenuItems[i]);
      mItem.addActionListener(this);
      helpMenu.add(mItem);
    }
    menuBar.add(helpMenu);

    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
	exit();
      }
    });

    setConfigComponent(mc);
    propertyEditor = new PropertyEditorPanel(configComponent, true);
    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(propertyEditor, BorderLayout.CENTER);

    setSize(600,500);
    setVisible(true);
  }

  private void exit() {
    propertyEditor.stopEditing(); // accept any edit in progress
    propertyEditor.exit();
    if (experiment != null && !experiment.isModified())
      return;
    saveToDatabase(true); // silently save
  }

  private void save() {
    if (experiment != null && !experiment.isModified()) {
      String[] msg = {
        "No modifications were made.",
        "Do you want to save this experiment anyway?"
      };
      int answer =
        JOptionPane.showConfirmDialog(this, msg,
                                      "No Modifications",
                                      JOptionPane.YES_NO_OPTION,
                                      JOptionPane.WARNING_MESSAGE);
      if (answer != JOptionPane.YES_OPTION) return;
    }
    saveToDatabase(false);
  }

  public void actionPerformed(ActionEvent e) {
    Object source = e.getSource();
    String s = ((AbstractButton)source).getActionCommand();
    if (s.equals(SAVE_DB_MENU_ITEM)) {
      save();
    } else if (s.equals(EXIT_MENU_ITEM)) {
      exit();
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
  }

  /** 
   * Save society and recipes to database.
   * If editing a society or recipe from within an experiment,
   * save the experiment,
   * otherwise save the society or recipe and tell the user
   * what experiments will have to be updated.
   * If silently is true, display no dialog boxes (used on exit).
   */
  private void saveToDatabase(boolean silently) {
    if (experiment != null) {
      saveExperiment();
      return;
    }
    if (configComponent instanceof SocietyComponent) {
      if (((SocietyComponent)configComponent).isModified()) {
	final PropertyBuilder propertyBuilder = this;
	GUIUtils.timeConsumingTaskStart(this);
	GUIUtils.timeConsumingTaskStart(csmart);
	try {
	  new Thread("SaveSociety") {
	    public void run() {
	      boolean success = ((SocietyComponent)configComponent).saveToDatabase();
	      GUIUtils.timeConsumingTaskEnd(propertyBuilder);
	      GUIUtils.timeConsumingTaskEnd(csmart);
	      if (!success && propertyBuilder.log.isWarnEnabled()) {
		propertyBuilder.log.warn("Failed to save society " + configComponent.getShortName());
	      } else if (propertyBuilder.log.isDebugEnabled()) {
		propetyBuilder.log.debug("Saved society " + configComponent.getShortName());
	      }
	    }
	  }.start();
	} catch (RuntimeException re) {
	  if(log.isErrorEnabled()) {
	    log.error("Runtime exception saving society", re);
	  }
	  GUIUtils.timeConsumingTaskEnd(propertyBuilder);
	  GUIUtils.timeConsumingTaskEnd(csmart);
	}
	//((SocietyComponent)configComponent).saveToDatabase();
	CSMART.getOrganizer().displayExperiments((SocietyComponent)configComponent);
      } else {
	// If we opened a "local" version of a society within an experiment,
	// then we want to remove this SocietyComponent from the workspace
      }
    } else if (configComponent instanceof RecipeComponent) {
      try {
        RecipeComponent rc = (RecipeComponent) configComponent;
        PDbBase pdb = new PDbBase();
        switch (pdb.recipeExists(rc)) {
        case PDbBase.RECIPE_STATUS_EXISTS:
          if (silently)
            return; // don't need to save, don't say anything
          JOptionPane.showMessageDialog(this,
                                        "The recipe is already in the database with the same values.",
                                        "Write Not Needed",
                                        JOptionPane.INFORMATION_MESSAGE);
          return;
        case PDbBase.RECIPE_STATUS_DIFFERS:
          // save the recipe, so workspace is consistent with database
          break;
        case PDbBase.RECIPE_STATUS_ABSENT:
          break;                // Just write it
        }
        rc.saveToDatabase();
        if (!silently)
          JOptionPane.showMessageDialog(this,
                                        "Recipe written successfully.",
                                        "Recipe Written",
                                        JOptionPane.INFORMATION_MESSAGE);
        CSMART.getOrganizer().displayExperiments(rc);
      } catch (Exception sqle) {
        if(log.isErrorEnabled()) {
          log.error("Exception", sqle);
        }
        JOptionPane.showMessageDialog(this,
                                      "An exception occurred writing the recipe to the database",
                                      "Error Writing Database",
                                      JOptionPane.ERROR_MESSAGE);
      }
    }
  }


  private void saveExperiment() {
    final DBConflictHandler saveToDbConflictHandler =
      GUIUtils.createSaveToDbConflictHandler(this);
    final PropertyBuilder propertyBuilder = this;
    GUIUtils.timeConsumingTaskStart(this);
    GUIUtils.timeConsumingTaskStart(csmart);
    try {
      new Thread("SaveExperiment") {
          public void run() {
            experiment.saveToDb(saveToDbConflictHandler);
            GUIUtils.timeConsumingTaskEnd(propertyBuilder);
            GUIUtils.timeConsumingTaskEnd(csmart);
          }
        }.start();
    } catch (RuntimeException re) {
      if(log.isErrorEnabled()) {
        log.error("Runtime exception saving experiment", re);
      }
      GUIUtils.timeConsumingTaskEnd(propertyBuilder);
      GUIUtils.timeConsumingTaskEnd(csmart);
    }
  }

  public void reinit(ModifiableComponent newModifiableComponent) {
    setConfigComponent(newModifiableComponent);
    propertyEditor.reinit(configComponent);
  }

  private void setConfigComponent(ModifiableComponent newConfigComponent) {
    configComponent = newConfigComponent;
  }

}
