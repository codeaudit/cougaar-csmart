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

package org.cougaar.tools.csmart.ui.analyzer;

import java.awt.GridLayout;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import javax.swing.*;
import java.util.*;

import org.cougaar.util.Parameters;

import org.cougaar.tools.csmart.experiment.Experiment;
import org.cougaar.tools.csmart.recipe.MetricComponent;
import org.cougaar.tools.csmart.recipe.RecipeComponent;
import org.cougaar.tools.csmart.society.SocietyComponent;
import org.cougaar.tools.csmart.ui.Browser;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.tools.csmart.ui.util.NamedFrame;
import org.cougaar.util.log.Logger;

public class Analyzer extends JFrame implements ActionListener {
  // system property; location of Excel
  private static final String EXCEL = "excel";
  // menu items
  private static final String FILE_MENU = "File";
  private static final String OPEN_MENU_ITEM = "Open";
  private static final String WINDOW_MENU = "Window";
  private static final String EXIT_MENU_ITEM = "Exit";
  private static final String HELP_MENU = "Help";

  protected static final String HELP_DOC = "help.html";
  protected static final String ABOUT_CSMART_ITEM = "About CSMART";
  protected static final String ABOUT_DOC = "/org/cougaar/tools/csmart/ui/help/about-csmart.html";
  protected static final String HELP_MENU_ITEM = "Help";

  private String[] helpMenuItems = {
    HELP_MENU_ITEM, ABOUT_CSMART_ITEM
  };

  // tool buttons
  private static final String EXCEL_LABEL = "Excel";
  private CSMART csmart; // top level viewer, gives access to save method, etc.
  private static JFrame myFrame;
  private JMenu windowMenu;
  private Hashtable titleToFrame = new Hashtable();
  private Process process;
  private Experiment experiment;

  private transient Logger log;

  /**
   * Display a csv file in excel.
   */

  public Analyzer(CSMART csmart, Experiment experiment) {
    createLogger();
    this.csmart = csmart;
    this.experiment = experiment;
    myFrame = this;
    JMenu fileMenu = new JMenu(FILE_MENU);
    JMenuItem openMenuItem = new JMenuItem(OPEN_MENU_ITEM);
    openMenuItem.addActionListener(this);
    fileMenu.add(openMenuItem);
    JMenuItem exitMenuItem = new JMenuItem(EXIT_MENU_ITEM);
    exitMenuItem.addActionListener(this);
    fileMenu.add(exitMenuItem);
    JMenuBar menuBar = new JMenuBar();

    JMenu helpMenu = new JMenu(HELP_MENU);
    for (int i = 0; i < helpMenuItems.length; i++) {
      JMenuItem mItem = new JMenuItem(helpMenuItems[i]);
      mItem.addActionListener(this);
      helpMenu.add(mItem);
    }
    
    getRootPane().setJMenuBar(menuBar); 
    menuBar.add(fileMenu);
    menuBar.add(helpMenu);

    JToolBar toolBar = new JToolBar();
    toolBar.setLayout(new GridLayout(1, 1, 2, 2));

    JButton button = null;
    URL iconURL = getClass().getResource("Excel.gif");
    if (iconURL == null)
      button = new JButton(EXCEL_LABEL);
    else {
      ImageIcon icon = new ImageIcon(iconURL);
      button = new JButton(EXCEL_LABEL, icon);
    }
    button.setHorizontalTextPosition(JButton.CENTER);
    button.setVerticalTextPosition(JButton.BOTTOM);
    button.addActionListener(this);
    toolBar.add(button);
    getContentPane().add("North", toolBar);

    windowMenu = new JMenu(WINDOW_MENU);
    pack();
    setSize(400, 120);
    setVisible(true);
  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  public void reinit(Experiment experiment) {
    this.experiment = experiment;
  }

  /**
   * Display file chooser on metrics directory specified by experiment
   * and run excel on file chosen by user.
   */

  private void showExcelFile() {
    File resultsDir = experiment.getResultDirectory();
    if (resultsDir == null) {
        JOptionPane.showMessageDialog(this, "No Results (Metrics) Directory for Experiment",
  				    "No Results Directory", 
  				    JOptionPane.WARNING_MESSAGE);
        return;
    }
    resultsDir = new File(resultsDir, experiment.getExperimentName());
    JFileChooser fileChooser = new JFileChooser(resultsDir);
    fileChooser.setFileFilter(new MyFileFilter(experiment));
    int result = fileChooser.showOpenDialog(this);
    if (result != JFileChooser.APPROVE_OPTION) 
      return;
    if (fileChooser.getSelectedFile().isDirectory())
      return; // user specified a directory, not a file
    String filePathName = fileChooser.getSelectedFile().getPath();
    String excel = 
      Parameters.findParameter("org.cougaar.tools.csmart.excelpath");
    
    if (excel == null || excel.equals("")) {
      if(log.isDebugEnabled()) {
        log.error("Excel location not specified");
      }
      return;
    }
    
    String[] cmds = { excel, "" };
    if(log.isDebugEnabled()) {
      log.info("Launching excel from: " + cmds[0]);
    }
    cmds[1] = filePathName;
    try {
      process = Runtime.getRuntime().exec(cmds);
    } catch (Exception e) {
      if(log.isDebugEnabled()) {
        log.error("Analyzer: exception: " + e);
        e.printStackTrace();
      }
    }
  }

  public void actionPerformed(ActionEvent e) {
    String s = ((AbstractButton)e.getSource()).getActionCommand();
    if (s.equals(OPEN_MENU_ITEM)) {
      showExcelFile();
    } else if (s.equals(EXCEL_LABEL)) {
      showExcelFile();
    } else if (s.equals(HELP_MENU_ITEM)) {
      URL help = (URL)getClass().getResource(HELP_DOC);
      if (help != null)
	Browser.setPage(help);
    } else if (s.equals(ABOUT_CSMART_ITEM)) {
      URL about = (URL)getClass().getResource(ABOUT_DOC);
      if (about != null)
	Browser.setPage(about);
    } else if (s.equals(EXIT_MENU_ITEM)) {
      // this will destroy the process, but is it the action we want?
      // the Excel process does not query the user before exiting
      //      process.destroy();
      int n = windowMenu.getItemCount();
      for (int i = 0; i < n; i++) {
	JMenuItem menuItem = windowMenu.getItem(i);
	JFrame f = (JFrame)titleToFrame.get(menuItem.getText());
	f.dispose();
      }
      NamedFrame.getNamedFrame().removeFrame(this);
      dispose();
    }
  }

  public static void main(String[] args) {
    new Analyzer(null, null);
  }

  class MyFileFilter extends javax.swing.filechooser.FileFilter {
    Experiment experiment;
    static final String description = "results files";

    MyFileFilter(Experiment experiment) {
      this.experiment = experiment;
    }

    /**
     * Accept any file accepted by a society in this experiment.
     */
    public boolean accept(File f) {
      if (f.isDirectory())
	return true; // allow user to go up/down through directory tree
      SocietyComponent societyComponent = experiment.getSocietyComponent();
      if (societyComponent != null) {
	java.io.FileFilter fileFilter = societyComponent.getResultFileFilter();
	if (fileFilter != null && fileFilter.accept(f))
	  return true;
      }
      int m = experiment.getRecipeCount();
      for (int i = 0; i < m; i++) {
	RecipeComponent recipeComponent = experiment.getRecipe(i);
        if (recipeComponent instanceof MetricComponent) {
          MetricComponent metricComponent = (MetricComponent) recipeComponent;
          java.io.FileFilter fileFilter = metricComponent.getResultFileFilter();
          if (fileFilter == null)
            continue;
          if (fileFilter.accept(f))
            return true;
        }
      }
      return false;
    }

    public String getDescription() {
      return description;
    }
  }

  private void readObject(ObjectInputStream ois)
    throws IOException, ClassNotFoundException
  {
    ois.defaultReadObject();
    createLogger();
  }

}
