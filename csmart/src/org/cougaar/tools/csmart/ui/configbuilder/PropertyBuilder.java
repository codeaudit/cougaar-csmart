/*
 * <copyright>
 *  Copyright 2000-2001 BBNT Solutions, LLC
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

import org.cougaar.tools.csmart.ui.Browser;
import org.cougaar.tools.csmart.society.abc.ABCSocietyComponent;
import org.cougaar.tools.csmart.core.property.ModifiableConfigurableComponent;
import org.cougaar.tools.csmart.recipe.RecipeComponent;
import org.cougaar.tools.csmart.core.db.PDbBase;
import org.cougaar.tools.csmart.ui.util.NamedFrame;

/**
 * User interface that supports building a configurable component.
 */
public class PropertyBuilder extends JFrame implements ActionListener {
  private PropertyEditorPanel propertyEditor;
  private ModifiableConfigurableComponent configComponent;
  private boolean isEditable; // remember if society was editable on entering
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

  public PropertyBuilder(ModifiableConfigurableComponent society) {
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

    setConfigComponent(society);
    propertyEditor = new PropertyEditorPanel(configComponent);
    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(propertyEditor, BorderLayout.CENTER);

    setSize(600,500);
    setVisible(true);
  }

  private void exit() {
    // before exiting, restore society's editability
    if (isEditable)
      configComponent.setEditable(isEditable);
  }

  public void actionPerformed(ActionEvent e) {
    Object source = e.getSource();
    String s = ((AbstractButton)source).getActionCommand();
    if (s.equals(SAVE_DB_MENU_ITEM)) {
      saveToDatabase();
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

  private void saveToDatabase() {
    if (configComponent instanceof RecipeComponent) {
      try {
        RecipeComponent rc = (RecipeComponent) configComponent;
        PDbBase pdb = new PDbBase();
        switch (pdb.recipeExists(rc)) {
        case PDbBase.RECIPE_STATUS_EXISTS:
          JOptionPane.showMessageDialog(this,
                                        "The recipe is already in the database with the same values.",
                                        "Write Not Needed",
                                        JOptionPane.INFORMATION_MESSAGE);
          return;
        case PDbBase.RECIPE_STATUS_DIFFERS:
          int answer =
            JOptionPane.showConfirmDialog(this,
                                          "Recipe "
                                          + rc.getRecipeName()
                                          + " already in database. Overwrite?",
                                          "Recipe Exists",
                                          JOptionPane.OK_CANCEL_OPTION,
                                          JOptionPane.WARNING_MESSAGE);
          if (answer != JOptionPane.OK_OPTION) return;
          break;
        case PDbBase.RECIPE_STATUS_ABSENT:
          break;                // Just write it
        }
        pdb.replaceLibRecipe(rc);
        JOptionPane.showMessageDialog(this,
                                      "Recipe written successfully.",
                                      "Recipe Written",
                                      JOptionPane.INFORMATION_MESSAGE);
      } catch (Exception sqle) {
        sqle.printStackTrace();
        JOptionPane.showMessageDialog(this,
                                      "An exception occurred writing the recipe to the database",
                                      "Error Writing Database",
                                      JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  public void reinit(ModifiableConfigurableComponent newModifiableConfigurableComponent) {
    setConfigComponent(newModifiableConfigurableComponent);
    propertyEditor.reinit(configComponent);
  }

  private void setConfigComponent(ModifiableConfigurableComponent newConfigComponent) {
    configComponent = newConfigComponent;
    isEditable = configComponent.isEditable();
    saveMenuItem.setEnabled(configComponent instanceof RecipeComponent);
  }

  public static void main(String[] args) {
    final ABCSocietyComponent abc = new ABCSocietyComponent();
    PropertyBuilder builder = new PropertyBuilder(abc);
    abc.initProperties();
    JMenu testMenu = new JMenu("Test");
    JMenuItem testMenuItem = new JMenuItem("Test Change");
    testMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	abc.test();
      }
    });
    testMenu.add(testMenuItem);
    builder.getRootPane().getJMenuBar().add(testMenu);
    builder.pack();
    builder.setDefaultCloseOperation(EXIT_ON_CLOSE);
  }

}
