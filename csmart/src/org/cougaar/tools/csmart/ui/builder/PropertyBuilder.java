/*
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 *
 * © Copyright 2000, 2001 BBNT Solutions LLC
 * </copyright>
 */

package org.cougaar.tools.csmart.ui.builder;

import java.awt.BorderLayout;
import java.awt.event.*;
import javax.swing.*;
import java.net.URL;

import org.cougaar.tools.csmart.ui.Browser;
import org.cougaar.tools.csmart.ui.component.ABCSocietyComponent;
import org.cougaar.tools.csmart.ui.component.SocietyComponent;
import org.cougaar.tools.csmart.ui.util.NamedFrame;

/**
 * User interface that supports building a society.
 */

public class PropertyBuilder extends JFrame implements ActionListener {
  private PropertyEditorPanel propertyEditor;
  private static final String FILE_MENU = "File";
  private static final String EXIT_MENU_ITEM = "Exit";

  private static final String HELP_MENU = "Help";

  protected static final String HELP_DOC = "help.html";
  protected static final String ABOUT_CSMART_ITEM = "About CSMART";
  protected static final String ABOUT_DOC = "../help/about-csmart.html";
  protected static final String HELP_MENU_ITEM = "Help";

  private String[] helpMenuItems = {
    HELP_MENU_ITEM, ABOUT_CSMART_ITEM
  };

  public PropertyBuilder(SocietyComponent society) {
    // initialize menus and gui panels
    JMenuBar menuBar = new JMenuBar();
    getRootPane().setJMenuBar(menuBar);
    JMenu fileMenu = new JMenu(FILE_MENU);

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

    propertyEditor = new PropertyEditorPanel(society, true);
    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(propertyEditor, BorderLayout.CENTER);

    setSize(600,500);
    setVisible(true);
  }

  public void actionPerformed(ActionEvent e) {
    Object source = e.getSource();
    String s = ((AbstractButton)source).getActionCommand();
    if (s.equals(EXIT_MENU_ITEM)) {
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

  public void reinit(SocietyComponent societyComponent) {
    propertyEditor.reinit(societyComponent);
  }

  public static void main(String[] args) {
    PropertyBuilder builder = new PropertyBuilder(null);
    final ABCSocietyComponent abc = new ABCSocietyComponent();
    builder.reinit(abc);
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
