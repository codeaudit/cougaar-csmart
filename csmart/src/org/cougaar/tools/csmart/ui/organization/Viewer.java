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

package org.cougaar.tools.csmart.ui.organization;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import org.cougaar.tools.csmart.ui.monitor.generic.ExtensionFileFilter;
import org.cougaar.util.ConfigFinder;
import org.cougaar.util.log.Logger;

/**
 * Reads organization information from a database or from a CSV file.
 * Displays the organizations in a tree and allows the user to select
 * which organizations are to be represented by agents in a society.
 * Writes information on the selected organizations into a database, 
 * a CSV file, or an XML file.  Supports:
 * reading and writing to/from a database
 * (i.e. if you read from a database, then you write to a database)
 * reading and writing to/from CSV files
 * reading from a database or CSV file and writing an XML file
 *
 * Database:
 * The "base" society (all organizations) is defined in an
 * org_hierarchy table which includes an org_id, command_org_id and org_code.
 * The selected organizations are written to a society_org table which
 * includes a society_id, org_id and command_org_id.
 * Selected organizations are those that are not expanded in the tree
 * or are leaves.
 *
 * CSV File:
 * The "base" society (all organizations) is defined in 
 * a org_hierarchy CSV file with:
 * base_org_id, suffix, superior_base_org_id, superior_suffix, rollup_code.
 * The selected organizations are written to a CSV file with:
 * base_org_id, suffix.
 * Selected organizations are those that are not deleted.
 *
 * XML File:
 * The selected organizations (agents) are written to an XML file
 * which follows the society.xsd schema, and includes one host, one node,
 * and an agent per selected organization.
 */

public class Viewer extends JFrame implements Observer {
  private JScrollPane treeView;
  private JTextField societyNameField;
  private JTextField agentCountField;
  private JFileChooser csvFileChooser;
  private JFileChooser xmlFileChooser;

  // menu items that can be enabled/disabled
  private JMenu fileMenu;
  private JMenu viewMenu;
  private JMenuItem databaseMenuItem;
  private JMenuItem readFromDBMenuItem;
  private JMenuItem readFromCSVMenuItem;
  private JMenuItem readFromBaseCSVMenuItem;
  private JMenuItem readFromXMLItem;
  private JMenuItem saveMenuItem;
  private JMenuItem saveAsMenuItem;
  private JMenuItem saveAsXMLMenuItem;
  private JMenuItem saveAsCSVMenuItem;
  private JMenuItem deleteMenuItem;
  private JMenuItem validateMenuItem;
  private JMenuItem exitMenuItem;
  private JMenuItem collapseTreeMenuItem;
  private JMenuItem expandTreeMenuItem;
  private Model model;
  private transient Logger log;

  public Viewer() {
    super("Society Builder");

    model = new Model();
    model.addObserver(this);
    log = LoggerSupport.createLogger(this.getClass().getName());

    // create menu bar
    JMenuBar menuBar = new JMenuBar();
    getRootPane().setJMenuBar(menuBar);

    fileMenu = new JMenu("File");
    fileMenu.setToolTipText("Read or write society information or quit.");

    databaseMenuItem = new JMenuItem("Select Database...");
    databaseMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          getDatabaseInfo();
        }
      });
    databaseMenuItem.setToolTipText("Specify database location.");
    fileMenu.add(databaseMenuItem);

    readFromDBMenuItem = new JMenuItem("Read From Database...");
    readFromDBMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          readFromDB();
        }
      });
    readFromDBMenuItem.setToolTipText("Read society description from database.");
    fileMenu.add(readFromDBMenuItem);

    readFromBaseCSVMenuItem = new JMenuItem("Read From Base CSV File...");
    readFromBaseCSVMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          readFromBaseCSV();
        }
      });
    readFromBaseCSVMenuItem.setToolTipText("Read complete society description from CSV file.");
    fileMenu.add(readFromBaseCSVMenuItem);

    readFromCSVMenuItem = new JMenuItem("Read From CSV File...");
    readFromCSVMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          readFromCSV();
        }
      });
    readFromCSVMenuItem.setToolTipText("Read society description from CSV file.");
    fileMenu.add(readFromCSVMenuItem);

    readFromXMLItem = new JMenuItem("Read From XML File...");
    readFromXMLItem.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent e) {
         readFromXML();
       }
    });
    fileMenu.add(readFromXMLItem);

    saveMenuItem = new JMenuItem("Save In Database");
    saveMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          saveSociety();
        }
      });
    saveMenuItem.setToolTipText("Save society description in database.");
    fileMenu.add(saveMenuItem);

    saveAsMenuItem = new JMenuItem("Save In Database As...");
    saveAsMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          saveSocietyAs();
        }
      });
    saveAsMenuItem.setToolTipText("Name and save the society description in the database.");
    fileMenu.add(saveAsMenuItem);

    saveAsXMLMenuItem = new JMenuItem("Save As XML...");
    saveAsXMLMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          saveAsXML();
        }
      });
    saveAsXMLMenuItem.setToolTipText("Name and save the society description as an XML file.");
    fileMenu.add(saveAsXMLMenuItem);

    saveAsCSVMenuItem = new JMenuItem("Save As CSV...");
    saveAsCSVMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          saveAsCSV();
        }
      });
    saveAsCSVMenuItem.setToolTipText("Name and save the society description as an CSV file.");
    fileMenu.add(saveAsCSVMenuItem);

    deleteMenuItem = new JMenuItem("Delete...");
    deleteMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          deleteSociety();
        }
      });
    deleteMenuItem.setToolTipText("Delete the society description from the database.");
    fileMenu.add(deleteMenuItem);

    validateMenuItem = new JMenuItem("Validate");
    validateMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          validateSociety();
        }
      });
    validateMenuItem.setToolTipText("Validate a society created from CSV files.");
    fileMenu.add(validateMenuItem);

    exitMenuItem = new JMenuItem("Exit");
    exitMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          exit();
        }
      });
    exitMenuItem.setToolTipText("Exit");
    fileMenu.add(exitMenuItem);
    menuBar.add(fileMenu);

    viewMenu = new JMenu("View");
    viewMenu.setToolTipText("Expand or collapse tree nodes.");
    expandTreeMenuItem = new JMenuItem("Expand Tree");
    expandTreeMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          model.expandTree();
        }
      });
    expandTreeMenuItem.setToolTipText("Expand all the nodes in the tree.");
    viewMenu.add(expandTreeMenuItem);

    collapseTreeMenuItem = new JMenuItem("Collapse Tree");
    collapseTreeMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          model.collapseTree();
        }
      });
    collapseTreeMenuItem.setToolTipText("Collapse all the nodes in the tree.");
    viewMenu.add(collapseTreeMenuItem);
    menuBar.add(viewMenu);

    // disable unneeded menus
    viewMenu.setEnabled(false);
    readFromDBMenuItem.setEnabled(false);
    readFromCSVMenuItem.setEnabled(false);
    saveMenuItem.setEnabled(false);
    saveAsMenuItem.setEnabled(false);
    saveAsXMLMenuItem.setEnabled(false);
    saveAsCSVMenuItem.setEnabled(false);
    deleteMenuItem.setEnabled(false);
    // end create menu bar

    // add panel to display society name and number of agents
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    panel.add(new JLabel("Society Name: "));
    societyNameField = new JTextField(20);
    societyNameField.setEditable(false);
    panel.add(societyNameField);
    panel.add(new JLabel("Number of agents: "));
    agentCountField = new JTextField(3);
    agentCountField.setText("1");
    agentCountField.setEditable(false);
    panel.add(agentCountField);
    getContentPane().add(panel, BorderLayout.NORTH);

    // display the frame
    addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          exit();
        }
      });
    pack();
    
    // display in center of screen
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    setSize(500, 500);
    int w = getWidth();
    int h = getHeight();
    setLocation((screenSize.width - w)/2, (screenSize.height - h)/2);
    setVisible(true);
  }

  /**
   * Query user for name of database, and user name and password.
   */
  private void getDatabaseInfo() {
    final JDialog dialog = new JDialog(this, "Database", true);
    JPanel panel = new JPanel(new BorderLayout());
    JPanel centerPanel = new JPanel(new GridBagLayout());
    int x = 0;
    int y = 0;
    centerPanel.add(new JLabel("Database name: "),
              new GridBagConstraints(x++, y, 1, 1, 0.0, 0.0,
                                     GridBagConstraints.WEST,
                                     GridBagConstraints.NONE,
                                     new Insets(5, 5, 0, 0),
                                     0, 0));
    final JTextField databaseField = new JTextField(20);
    centerPanel.add(databaseField,
              new GridBagConstraints(x, y++, 1, 1, 0.0, 0.0,
                                     GridBagConstraints.WEST,
                                     GridBagConstraints.NONE,
                                     new Insets(5, 0, 0, 5),
                                     0, 0));
    x = 0;
    centerPanel.add(new JLabel("User name: "),
              new GridBagConstraints(x++, y, 1, 1, 0.0, 0.0,
                                     GridBagConstraints.WEST,
                                     GridBagConstraints.NONE,
                                     new Insets(5, 5, 0, 0),
                                     0, 0));
    final JTextField userNameField = new JTextField(20);
    centerPanel.add(userNameField,
              new GridBagConstraints(x, y++, 1, 1, 0.0, 0.0,
                                     GridBagConstraints.WEST,
                                     GridBagConstraints.NONE,
                                     new Insets(5, 0, 0, 5),
                                     0, 0));
    x = 0;
    centerPanel.add(new JLabel("Password: "),
              new GridBagConstraints(x++, y, 1, 1, 0.0, 0.0,
                                     GridBagConstraints.WEST,
                                     GridBagConstraints.NONE,
                                     new Insets(5, 5, 0, 0),
                                     0, 0));
    final JPasswordField passwordField = new JPasswordField(20);
    centerPanel.add(passwordField,
              new GridBagConstraints(x, y++, 1, 1, 0.0, 0.0,
                                     GridBagConstraints.WEST,
                                     GridBagConstraints.NONE,
                                     new Insets(5, 0, 0, 5),
                                     0, 0));
    panel.add(centerPanel, BorderLayout.CENTER);
    JPanel buttonPanel = new JPanel();
    JButton okButton = new JButton("OK");
    okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String database = databaseField.getText();
        String username = userNameField.getText();
        String password = new String(passwordField.getPassword());
        dialog.hide();
        // enable database related menu items
        readFromDBMenuItem.setEnabled(true);
        deleteMenuItem.setEnabled(true);
        model.setDBParameters(database, username, password);
      }
    });
    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        dialog.hide();
      }
    });

    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        dialog.hide();
      }
    });

    buttonPanel.add(okButton);
    buttonPanel.add(cancelButton);
    panel.add(buttonPanel, BorderLayout.SOUTH);
    dialog.getContentPane().add(panel);
    dialog.pack();
    // make dialog display over the middle of the frame
    Point p = this.getLocation();
    Dimension d = this.getSize();
    int centerX = p.x + d.width/2;
    int centerY = p.y + d.height/2;
    Dimension myD = dialog.getSize();
    dialog.setLocation(new Point(centerX - myD.width/2, centerY - myD.height/2));
    dialog.show();
  }

  /**
   * Query the user for the name of a society in the database
   * and display it.
   */
  private void readFromDB() {
    ArrayList societyNames = model.getSocietyNamesFromDatabase();
    if (societyNames.size() == 0) {
      JOptionPane.showMessageDialog(this, "No Societies in Database", 
                                    "No Societies", JOptionPane.ERROR_MESSAGE);
      return;
    }
    JComboBox cb = new JComboBox(societyNames.toArray());
    cb.setEditable(false);
    JPanel panel = new JPanel();
    panel.add(new JLabel("Select Society:"));
    panel.add(cb);
    int result = 
      JOptionPane.showConfirmDialog(this, panel, "Read From Database",
				    JOptionPane.OK_CANCEL_OPTION,
				    JOptionPane.PLAIN_MESSAGE);
    if (result != JOptionPane.OK_OPTION) return;
    String name = (String)cb.getSelectedItem();
    model.readSocietyFromDB(name);
  }

  /**
   * Delete the society selected by the user from the database.
   */
  private void deleteSociety() {
    ArrayList societyNames = model.getSocietyNamesFromDatabase();
    if (societyNames == null) {
      JOptionPane.showMessageDialog(this, "No Societies in Database", 
                                    "No Societies", JOptionPane.ERROR_MESSAGE);
      return;
    }
    JComboBox cb = new JComboBox(societyNames.toArray());
    cb.setEditable(false);
    JPanel panel = new JPanel();
    panel.add(new JLabel("Select Society:"));
    panel.add(cb);
    int result = 
      JOptionPane.showConfirmDialog(this, panel, "Delete Society",
				    JOptionPane.OK_CANCEL_OPTION,
				    JOptionPane.PLAIN_MESSAGE);
    if (result != JOptionPane.OK_OPTION) return;
    String name = ((String)cb.getSelectedItem());
    model.deleteSociety(name);
  }

  /**
   * Ask the user for a name for a society to save in the database.
   */
  private String getSocietyName() {
    String name = null;
    while (name == null) {
      name = JOptionPane.showInputDialog(this, "Society Name:", "Save Society",
                                         JOptionPane.QUESTION_MESSAGE);
      if (name == null) return null;
      name = name.trim();
      if (name.length() == 0)
        name = null;
      else {
        // check that society name is unique in database
        if (model.isSocietyNameInDatabase(name)) {
          name = null;
          int answer = 
            JOptionPane.showConfirmDialog(this, "Name is in database; Override?", 
                                          "Name Not Unique", 
                                          JOptionPane.YES_NO_CANCEL_OPTION,
                                          JOptionPane.ERROR_MESSAGE);
          if (answer == JOptionPane.YES_OPTION)
            return name;
          else if (answer == JOptionPane.CANCEL_OPTION)
            return null;
        }
      }
    }
    return name;
  }

  /**
   * Save the society defined by this tree in the database.
   * Each organization that is a leaf or isn't expanded is represented.
   */
  private void saveSociety() {
    // ask for the society name if the user hasn't given one
    String societyName = model.getSocietyName();
    if (societyName == null) {
      String newSocietyName = getSocietyName();
      if (newSocietyName == null)
        return;
      model.setSocietyName(newSocietyName);
    }
    model.saveSocietyInDB();
  }

  /**
   * Save the society defined by the tree under a new name in the database.
   */
  private void saveSocietyAs() {
    String newSocietyName = getSocietyName();
    if (newSocietyName != null) {
      model.setSocietyName(newSocietyName);
      saveSociety();
    }
  }

  /**
   * Read complete society information from a CSV file.
   */
  private void readFromBaseCSV() {
    File file = getCSVFile();
    if (file != null)
      model.readFromBaseCSV(file);
  }

  /**
   * Read information about a particular society (i.e. a subset
   * of the organizations in the complete society) from a CSV file.
   */
  private void readFromCSV() {
    File file = getCSVFile();
    if (file != null)
      model.readCSV(file);
  }

  /**
   * Read information about a society from an XML file.
   */
  private void readFromXML() {
    File file = getXMLFile();
    if (file != null)
      model.readXML(file.getAbsolutePath());
  }

  /**
   * Get the name of a CSV file to read.
   */
  private File getCSVFile() {
    if (csvFileChooser == null) {
      csvFileChooser =
        new JFileChooser(System.getProperty("org.cougaar.install.path"));
      csvFileChooser.setDialogTitle("Select CSV File");
      String [] filters  = { "csv" };
      ExtensionFileFilter filter = 
        new ExtensionFileFilter(filters, "CSV file");
      csvFileChooser.addChoosableFileFilter(filter);
    }
    if (csvFileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
      return null;
    File file = csvFileChooser.getSelectedFile();
    if (file == null)
      return null;
    if (!file.canRead())
      return null;
    return file;
  }

  /**
   * Get the name of an XML file to read.
   */
  private File getXMLFile() {
    if (xmlFileChooser == null) {
      xmlFileChooser =
        new JFileChooser(System.getProperty("org.cougaar.install.path"));
      xmlFileChooser.setDialogTitle("Select XML File");
      String [] filters  = { "xml" };
      ExtensionFileFilter filter =
        new ExtensionFileFilter(filters, "XML file");
      xmlFileChooser.addChoosableFileFilter(filter);
    }
    if (xmlFileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
      return null;
    File file = xmlFileChooser.getSelectedFile();
    if (file == null)
      return null;
    if (!file.canRead())
      return null;
    return file;
  }

  /**
   * Save the society as a CSV file.
   */
  private void saveAsCSV() {
    JFileChooser fileChooser = 
      new JFileChooser(System.getProperty("org.cougaar.install.path"));
    fileChooser.setDialogTitle("Save As CSV File");
    if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
      return;
    File file = fileChooser.getSelectedFile();
    String name = file.getName();
    if (name.endsWith(".csv"))
      name = name.substring(0, name.length()-4);
    model.setSocietyName(name);
    String path = file.getPath();
    if (!path.endsWith(".csv"))
      path = path + ".csv";
    model.saveAsCSV(path);
  }

  /**
   * Save the society as an XML file.
   */
  private void saveAsXML() {
    JFileChooser fileChooser = 
      new JFileChooser(System.getProperty("org.cougaar.install.path"));
    fileChooser.setDialogTitle("Save As XML File");
    if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
      return;
    File file = fileChooser.getSelectedFile();
    String name = file.getName();
    if (name.endsWith(".xml"))
      name = name.substring(0, name.length()-4);
    model.setSocietyName(name);
    model.saveAsXML(file);
  }

  private void validateSociety() {
    model.validateSociety();
  }

  /**
   * Exit, but if the user has modified the tree, then ask them to
   * confirm the exit.
   * Don't offer a save option here as the user could save in several
   * formats.
   */
  private void exit() {
    if (model.getModified()) {
      int answer = 
        JOptionPane.showConfirmDialog(this, "Society is Modified; Discard and Exit?",
                                      "Society Is Modified",
                                      JOptionPane.YES_NO_OPTION,
                                      JOptionPane.QUESTION_MESSAGE);
      if (answer == JOptionPane.YES_OPTION)
        System.exit(0);
      if (answer == JOptionPane.NO_OPTION)
        return;
    } else
      System.exit(0);
  }

  /**
   * Replace any existing tree with new tree.
   */
  private void updateTree(JTree tree) {
    if (treeView != null)
      getContentPane().remove(treeView);
    treeView = new JScrollPane(tree);
    getContentPane().add(treeView, BorderLayout.CENTER);
    validate();
  }

  /**
   * Update the number of agents.
   */
  private void updateAgentCount(int n) {
    agentCountField.setText(String.valueOf(n));
  }

  /**
   * Called by model when something has changed
   * that potentially affects the ui.
   */
  public void update(Observable o, Object arg) {
    if (arg instanceof Notification) {
      Notification n = (Notification)arg;
      String action = n.action;
      if (action == model.SOCIETY_CHANGED)
        societyNameField.setText((String)n.info);
      else if (action == model.TREE_CHANGED)
        updateTree((JTree)n.info);
      else if (action == model.AGENT_COUNT_CHANGED)
        updateAgentCount(((Integer)n.info).intValue());
    }

    if (arg == model.DB_SOCIETY_CREATED) {
      saveMenuItem.setEnabled(true);
      saveAsMenuItem.setEnabled(true);
      saveAsXMLMenuItem.setEnabled(true);
      viewMenu.setEnabled(true);
      return;
    } else if (arg == model.CSV_SOCIETY_CREATED) {
      readFromCSVMenuItem.setEnabled(true);
      saveAsCSVMenuItem.setEnabled(true);
      saveAsXMLMenuItem.setEnabled(true);
      viewMenu.setEnabled(true);
      saveMenuItem.setEnabled(false);
      saveAsMenuItem.setEnabled(false);
    }
  }

  public static void main(String[] args) {
    JFrame frame = new Viewer();
  }

}
