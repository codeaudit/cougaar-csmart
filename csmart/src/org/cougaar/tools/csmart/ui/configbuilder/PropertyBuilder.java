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
import java.awt.event.*;
import javax.swing.*;
import java.net.URL;
import java.sql.SQLException;

import org.cougaar.tools.csmart.core.db.PDbBase;
import org.cougaar.tools.csmart.core.property.ModifiableComponent;
import org.cougaar.tools.csmart.experiment.Experiment;
import org.cougaar.tools.csmart.recipe.RecipeComponent;
import org.cougaar.tools.csmart.ui.Browser;
import org.cougaar.tools.csmart.ui.util.NamedFrame;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
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

  public PropertyBuilder(CSMART csmart, ModifiableComponent mc) {
    log = CSMART.createLogger(this.getClass().getName());
    this.csmart = csmart;
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
    saveToDatabase(true); // silently save
  }

  public void actionPerformed(ActionEvent e) {
    Object source = e.getSource();
    String s = ((AbstractButton)source).getActionCommand();
    if (s.equals(SAVE_DB_MENU_ITEM)) {
      saveToDatabase(false);
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

  private void saveToDatabase(boolean silently) {
    if (configComponent instanceof RecipeComponent) {
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
        String experimentNames = getExperiments(rc);
        JOptionPane.showMessageDialog(this,
                                      "To update recipe in these experiments, save the experiments to the database:\n" + experimentNames,
                                      "Experiments Using This Recipe",
                                      JOptionPane.INFORMATION_MESSAGE);
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

  // get names of all experiments in workspace that contain the recipe

  private String getExperiments(RecipeComponent rc) {
    StringBuffer sbuf = new StringBuffer();
    Experiment[] experiments = csmart.getExperimentsInWorkspace();
    for (int i = 0; i < experiments.length; i++) {
      RecipeComponent[] recipes = experiments[i].getRecipes();
      for (int j = 0; j < recipes.length; j++) {
        if (recipes[j].equals(rc)) {
          if (sbuf.length() != 0)
            sbuf.append(", ");
          sbuf.append(experiments[i].getExperimentName());
          break;
        }
      }
    }
    return sbuf.toString();
  }

  public void reinit(ModifiableComponent newModifiableComponent) {
    setConfigComponent(newModifiableComponent);
    propertyEditor.reinit(configComponent);
  }

  private void setConfigComponent(ModifiableComponent newConfigComponent) {
    configComponent = newConfigComponent;
    saveMenuItem.setEnabled(configComponent instanceof RecipeComponent);
  }

}
