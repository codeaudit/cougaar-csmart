/* 
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 * 
 *       © Copyright 2001 by BBNT Solutions LLC.
 * </copyright>
 */

package org.cougaar.tools.csmart.ui.monitor.metrics;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import java.util.*;
import java.net.URL;

import org.cougaar.tools.csmart.ui.monitor.generic.UIProperties;
import org.cougaar.tools.csmart.ui.util.NamedFrame;
import org.cougaar.tools.csmart.ui.util.Util;
import org.cougaar.tools.csmart.ui.Browser;

public class CSMARTMetricsFrame extends JFrame implements ActionListener
{
  static final String FILE_MENU = "File";
  static final String CLOSE_MENU_ITEM = "Close";
  static final String SAVE_MENU_ITEM = "Save";
  static final String SAVE_AS_MENU_ITEM = "Save As...";
  static final String PRINT_MENU_ITEM = "Print...";

  static final String VIEW_MENU = "View";
  static final String REFRESH_MENU_ITEM = "Refresh";

  protected static final String HELP_MENU = "Help";
  protected static final String HELP_DOC = "help.html";
  protected static final String HELP_MENU_ITEM = "Help";
  protected static final String ABOUT_MENU_ITEM = "About CSMART";
  protected static final String ABOUT_DOC = "../../help/about-csmart.html";

  static final String SEPARATOR = "Separator";

  String[] fileMenuItems = { 
    SAVE_MENU_ITEM, SAVE_AS_MENU_ITEM,
    PRINT_MENU_ITEM, SEPARATOR, CLOSE_MENU_ITEM
  };

  String[] viewMenuItems = { 
    REFRESH_MENU_ITEM
  };

  private String[] helpMenuItems = {
    HELP_MENU_ITEM, ABOUT_MENU_ITEM
  };

  String title;
  CSMARTMetrics metrics;
  private final JFrame myFrame;
  // menu items that are enabled/disabled
  JMenuItem saveMenuItem;

  public CSMARTMetricsFrame(String title, ArrayList names, ArrayList data ) {
    super(title);
    this.title = NamedFrame.getNamedFrame().addFrame(title, this);
    setTitle(this.title);
    this.metrics = new CSMARTMetrics(names, data);
    myFrame = this;
    init();
    //saveMenuItem.setEnabled(false);
  }

  public CSMARTMetricsFrame(String title, File f) {
    super(title);
    this.title = NamedFrame.getNamedFrame().addFrame(title, this);
    setTitle(this.title);
    this.metrics = new CSMARTMetrics(f);
    myFrame = this;
    init();
    saveMenuItem.setEnabled(false);
  }

  private void init() {
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
	NamedFrame.getNamedFrame().removeFrame(myFrame);
	e.getWindow().dispose();
      }
    });
    setSize(600, 275);
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    setLocation((screenSize.width - 600)/2, 
		(screenSize.height - 275)/2);

    if (metrics != null)
      getContentPane().add("Center", metrics);
    
    JScrollPane jsp = new JScrollPane();
    jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    jsp.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);

    getContentPane().add(jsp, "East");

    jsp.setViewportView(metrics);

    JMenuBar menuBar = new JMenuBar();
    getRootPane().setJMenuBar(menuBar);
    JMenu fileMenu = makeMenu(FILE_MENU, fileMenuItems, menuBar);
    for (int i = 0; i < fileMenu.getMenuComponentCount(); i++) {
      Component menuComponent = fileMenu.getMenuComponent(i);
      if (!(menuComponent instanceof JMenuItem))
	continue;
      JMenuItem menuItem = (JMenuItem)menuComponent;
      String s = menuItem.getText();
      if (s.equals(SAVE_MENU_ITEM))
	saveMenuItem = menuItem;
      else if (s.equals(PRINT_MENU_ITEM)) {
	menuItem.setEnabled(false);
      }
    }
    makeMenu(VIEW_MENU, viewMenuItems, menuBar);

    makeMenu(HELP_MENU, helpMenuItems, menuBar);

    setVisible(true);
  }

  private JMenu makeMenu(String menuName, String[] menuItemNames, 
			 JMenuBar menuBar) {
    JMenu menu = new JMenu(menuName);
    if (menuItemNames != null) {
      for (int i = 0; i < menuItemNames.length; i++) {
	if (menuItemNames[i].equals(SEPARATOR))
	  menu.addSeparator();
	else {
	  JMenuItem menuItem = new JMenuItem(menuItemNames[i]);
	  menuItem.addActionListener(this);
	  menu.add(menuItem);
	}
      }
    } else
      menu.addActionListener(this);
    menuBar.add(menu);
    return menu;
  }

  public void actionPerformed(ActionEvent evt) {
    String command = ((JMenuItem)evt.getSource()).getText();

    if (command.equals(CLOSE_MENU_ITEM)) {
      NamedFrame.getNamedFrame().removeFrame(this);
      this.dispose();
      return;
    }

    // save graph into file it was read from
    if (command.equals(SAVE_MENU_ITEM)) {
      metrics.saveMetrics();
      return;
    }

    if (command.equals(SAVE_AS_MENU_ITEM)) {
      metrics.saveAsMetrics();
      //saveMenuItem.setEnabled(metrics.hasOutputFile());
      return;
    }

    if (command.equals(REFRESH_MENU_ITEM)) {
      metrics.refresh();
      return;
    }

    if (command.equals(HELP_MENU_ITEM)) {
      URL help = (URL)getClass().getResource(HELP_DOC);
      if(help != null)
	Browser.setPage(help);
      return;
    }

    if (command.equals(ABOUT_MENU_ITEM)) {
      URL help = (URL)getClass().getResource(ABOUT_DOC);
      if(help != null)
	Browser.setPage(help);
      return;
    }

  }


}
  
