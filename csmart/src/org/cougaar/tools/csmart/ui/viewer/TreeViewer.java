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

package org.cougaar.tools.csmart.ui.viewer;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.sql.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.cougaar.tools.csmart.ui.monitor.generic.ExtensionFileFilter;
import org.cougaar.tools.csmart.util.XMLUtils;
import org.cougaar.util.DBConnectionPool;
import org.cougaar.util.ConfigFinder;
import org.cougaar.util.log.Logger;
import org.cougaar.util.log.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

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

public class TreeViewer extends JFrame {
  // for example, "jdbc:mysql://victoria.bbn.com/FCS";
  private static String database = "";
  private static String username = "travers";
  private static String password;
  private static String getRootQuery =
    "select org_id, command_org_id, org_code from org_hierarchy where parent_org_id = '';";
  // tree from database or CSV
  private static int DB_TREE = 1;
  private static int CSV_TREE = 2;
  private int treeType;
  private transient Logger log;
  private String societyName = null;
  private JTree tree;
  private JScrollPane treeView;
  private DefaultMutableTreeNode root;
  private JTextField societyNameField;
  private JTextField agentCountField;
  private boolean modified = false;
  private XMLUtils utils;
  private ArrayList csvOrgs;
  private ArrayList csvOrgAttributes;
  private ArrayList csvOrgRoles;
  private ArrayList csvOrgSupport;
  private JFileChooser csvFileChooser;
  private File baseCSVFile = null;

  // menu items that can be enabled/disabled
  JMenu fileMenu;
  JMenu viewMenu;
  JMenuItem databaseMenuItem;
  JMenuItem readFromDBMenuItem;
  JMenuItem readFromCSVMenuItem;
  JMenuItem readFromBaseCSVMenuItem;
  JMenuItem saveMenuItem;
  JMenuItem saveAsMenuItem;
  JMenuItem saveAsXMLMenuItem;
  JMenuItem saveAsCSVMenuItem;
  JMenuItem deleteMenuItem;
  JMenuItem exitMenuItem;
  JMenuItem collapseTreeMenuItem;
  JMenuItem expandTreeMenuItem;

  public TreeViewer() {
    super("Society Builder");

    // create logger
    log = lf.createLogger(this.getClass().getName());

    // create XML utilities
    utils = new XMLUtils();

    // load mysql driver
    try {
      Class.forName("org.gjt.mm.mysql.Driver");
    } catch (Exception e) {
      if (log.isErrorEnabled()) {
        log.error("Error loading mysql driver", e);
      }
    }

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
          if (treeType == DB_TREE)
            saveAsXML();
          else
            saveCSVAsXML();
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
          expandTree();
        }
      });
    expandTreeMenuItem.setToolTipText("Expand all the nodes in the tree.");
    viewMenu.add(expandTreeMenuItem);

    collapseTreeMenuItem = new JMenuItem("Collapse Tree");
    collapseTreeMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          collapseTree();
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
          System.exit(0);
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
   * Logging methods.
   */
  private static LoggerFactory lf;
  static {
    lf = LoggerFactory.getInstance();

    Properties defaults = new Properties();
    defaults.setProperty("log4j.rootCategory", "WARN, A1");
    defaults.setProperty("log4j.appender.A1",
                         "org.apache.log4j.ConsoleAppender");
    defaults.setProperty("log4j.appender.A1.Target", "System.out");
    defaults.setProperty("log4j.appender.A1.layout", 
                         "org.apache.log4j.PatternLayout");
    defaults.setProperty("log4j.appender.A1.layout.ConversionPattern", 
                         "%d{ABSOLUTE} %-5p [ %t] - %m%n");

    Properties props = new Properties(defaults);
    // Get the debug file.
    ConfigFinder cf = ConfigFinder.getInstance("csmart");
    try {
      props.load(new FileInputStream(cf.locateFile("debug.properties")));
    } catch(Exception e) {
      System.err.println("TreeViewer.java Could not read debug.properties file, using defaults.");
    }

    lf.configure(props);
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
        database = databaseField.getText();
        username = userNameField.getText();
        password = passwordField.getText();
        dialog.hide();
        // enable database related menu items
        readFromDBMenuItem.setEnabled(true);
        deleteMenuItem.setEnabled(true);
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
   * Contact database to get base of tree, create and display tree.
   */
  private void readBaseSocietyFromDB() {
    // execute query to obtain top-level organization info
    ArrayList queryResults = getOrganizationInfo(getRootQuery);
    if (queryResults.size() == 0) {
      if (log.isErrorEnabled()) {
        log.error("Query to get organizations from database failed.");
      }
      return;
    }
    DBOrgInfo org = (DBOrgInfo)queryResults.get(0);

    treeType = DB_TREE;
    createTree(org);
    addTreeNodesFromDB(org.code, root); // populate tree with organizations

    updateTree();

    // update menu items
    saveMenuItem.setEnabled(true);
    saveAsMenuItem.setEnabled(true);
    saveAsXMLMenuItem.setEnabled(true);
    viewMenu.setEnabled(true);
  }

  /**
   * Create tree with the specified object as the user object of the root.
   */
  private void createTree(Object org) {
    root = new DefaultMutableTreeNode(org, true);
    tree = new JTree(root);
    tree.setShowsRootHandles(true); // show toggle on root node
    tree.setCellRenderer(new MyTreeCellRenderer());
    if (treeType == DB_TREE) {
      tree.addTreeExpansionListener(new MyDBTreeExpansionListener());
    } else if (treeType == CSV_TREE) {
      tree.addTreeWillExpandListener(new MyCSVTreeWillExpandListener());
      tree.addMouseListener(new MyCSVMouseListener());
    }
  }

  /**
   * Replace any existing tree with new tree.
   */
  private void updateTree() {
    if (treeView != null)
      getContentPane().remove(treeView);
    treeView = new JScrollPane(tree);
    getContentPane().add(treeView, BorderLayout.CENTER);
    validate();
  }

  /**
   * Make a tree node for each organization described in the database
   * recursively.
   */
  private void addTreeNodesFromDB(String parentOrgCode,
                                  DefaultMutableTreeNode parent) {
    ArrayList queryResults = getOrganizationInfo(makeQuery(parentOrgCode));
    // for each child
    for (int i = 0; i < queryResults.size(); i++) {
      DBOrgInfo org = (DBOrgInfo)queryResults.get(i);
      DefaultMutableTreeNode child = new DefaultMutableTreeNode(org);
      parent.add(child);
      addTreeNodesFromDB(org.code, child);
    }
  }

  /**
   * Make query used to obtain organization info to populate the tree.
   */
  private String makeQuery(String parent_org_code) {
    return "select org_id, command_org_id, org_code from org_hierarchy where org_code like '" + parent_org_code + "_';";
  }
  
  /**
   * Execute database query and return array of DBOrgInfo objects.
   */
  private ArrayList getOrganizationInfo(String query) {
    Connection conn = null;
    ResultSet rs = null;
    ArrayList results = new ArrayList();
    try {
      conn = DBConnectionPool.getConnection(database, username, password);
      Statement stmt = conn.createStatement();
      rs = stmt.executeQuery(query);
      while(rs.next()) {
        String name = rs.getString(1);
        String commandOrgId = rs.getString(2);
        String code = rs.getString(3);
        results.add(new DBOrgInfo(name, commandOrgId, code));
      }
      rs.close();
      stmt.close();
    } catch (SQLException se) {
      if (log.isErrorEnabled()) {
        log.error("SQL Exception", se);
      }
    } finally {
      try {
        if (conn != null)
          conn.close();
      } catch (SQLException e) {}
    }
    return results;
  }

  /**
   * Execute a database query which returns an array of strings.
   */
  private ArrayList executeQuery(String query) {
    Connection conn = null;
    ResultSet rs = null;
    ArrayList results = new ArrayList();
    try {
      conn = DBConnectionPool.getConnection(database, username, password);
      Statement stmt = conn.createStatement();
      rs = stmt.executeQuery(query);
      while(rs.next()) {
        results.add(rs.getString(1));
      }
      rs.close();
      stmt.close();
    } catch (SQLException se) {
      if (log.isErrorEnabled()) {
        log.error("SQL Exception", se);
      }
    } finally {
      try {
        if (conn != null)
          conn.close();
      } catch (SQLException e) {}
    }
    return results;
  }

  /**
   * Execute an update, insert or delete from database.
   */
  private void executeUpdate(String update) {
    Connection conn = null;
    try {
      conn = DBConnectionPool.getConnection(database, username, password);
      Statement stmt = conn.createStatement();
      stmt.executeUpdate(update);
      stmt.close();
    } catch (SQLException se) {
      if (log.isErrorEnabled()) {
        log.error("SQL Exception", se);
      }
    } finally {
      try {
        if (conn != null)
          conn.close();
      } catch (SQLException e) {}
    }
  }

  /**
   * Display society from database in the tree.
   * The "base" society is defined in an org_hierarchy table which
   * includes an org_id, command_org_id and org_code.
   * The selected organizations are obtained from a society_org table which
   * includes a society_id, org_id and command_org_id.
   * The tree is expanded appropriately to display the selected organizations.
   */
  private void readFromDB() {
    ArrayList societyNames = getSocietyNamesFromDatabase();
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

    // get node names from the new society
    String query = "select org_id from society_org where society_id = '" +
      name + "';";
    ArrayList nodeNamesInSociety = executeQuery(query);
    if (nodeNamesInSociety.size() == 0) {
      JOptionPane.showMessageDialog(this, "Could Not Read Society",
                                    "No Society", JOptionPane.ERROR_MESSAGE);
      return;
    }
    setSocietyName(name);

    // first, get the base tree
    readBaseSocietyFromDB();
    // redisplay the tree with society nodes visible and not expanded
    // collapse all
    collapseTree();
    // if the node is in the society and is a leaf, then ensure that it's
    // visible by expanding its parent
    Enumeration nodes = root.depthFirstEnumeration();
    while (nodes.hasMoreElements()) {
      DefaultMutableTreeNode node = 
	(DefaultMutableTreeNode)nodes.nextElement();
      DBOrgInfo orgInfo = (DBOrgInfo)node.getUserObject();
      //      if (nodeNamesInSociety.contains(orgInfo.name) && node.isLeaf()) {
      if (nodeNamesInSociety.contains(orgInfo.name)) {
        DefaultMutableTreeNode parent = 
          (DefaultMutableTreeNode)node.getParent();
        tree.expandPath(new TreePath(parent.getPath()));
      }
    }
    modified = false;
  }

  /**
   * Read complete society information from a CSV file.
   * Information is in the form:
   * base_org_id, suffix, superior_base_org_id, supperior_suffix, rollup_code
   *
   */
  private void readFromBaseCSV() {
    // get the CSV file to read
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
      return;
    File file = csvFileChooser.getSelectedFile();
    if (file == null)
      return;
    if (!file.canRead())
      return;

    // read the CSV file
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new FileReader(file));
    } catch (FileNotFoundException e) {
      if (log.isErrorEnabled()) {
        log.error("File not found", e);
      }
      return;
    }

    baseCSVFile = file;
    csvOrgs = new ArrayList();
    String s;
    try {
      s = reader.readLine(); // skip the first line, which is a comment
      while ((s = reader.readLine()) != null) {
        MyStringTokenizer st = new MyStringTokenizer(s, ",");
        CSVOrgInfo orgInfo = getOrganizationInfoFromCSV(st);
        if (orgInfo != null)
          csvOrgs.add(orgInfo);
      }
    } catch (IOException ie) {
      if (log.isErrorEnabled()) {
        log.error("Error reading from file", ie);
      }
    }

    // create a tree
    // find the root, the organization that has no superior
    CSVOrgInfo csvRoot = null;
    for (int i = 0; i < csvOrgs.size(); i++) {
      CSVOrgInfo org = (CSVOrgInfo)csvOrgs.get(i);
      if (org.superiorBaseOrgId == "") {
        csvRoot = org;
        break;
      }
    }
    if (csvRoot == null) {
      if (log.isErrorEnabled()) {
        log.error("No root object");
      }
      return;
    }
    String name = file.getName();
    if (name.endsWith(".csv"))
      name = name.substring(0, name.length()-4);
    setSocietyName(name);
    treeType = CSV_TREE;
    createTree(csvRoot);
    addTreeNodesFromCSV(csvRoot, root);
    updateTree();
    agentCountField.setText(String.valueOf(csvOrgs.size()));
    // update menu items
    readFromCSVMenuItem.setEnabled(true);
    saveAsCSVMenuItem.setEnabled(true);
    saveAsXMLMenuItem.setEnabled(true);
    viewMenu.setEnabled(true);
    saveMenuItem.setEnabled(false);
    saveAsMenuItem.setEnabled(false);
    // temporary, for debugging, read additional csv files
    readCSVAttributes();
    readCSVRoles();
    readCSVSupport();
  }

  /**
   * Read society information from a CSV file.
   * Information is in the form:
   * base_org_id, suffix
   *
   */
  private void readFromCSV() {
    // get the CSV file to read
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
      return;
    File file = csvFileChooser.getSelectedFile();
    if (file == null)
      return;
    if (!file.canRead())
      return;

    // read the CSV file
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new FileReader(file));
    } catch (FileNotFoundException e) {
      if (log.isErrorEnabled()) {
        log.error("File not found", e);
      }
      return;
    }

    // this file simply indicates which organizations are not deleted
    String s;
    ArrayList bases = new ArrayList();
    ArrayList suffixes = new ArrayList();
    try {
      while ((s = reader.readLine()) != null) {
        MyStringTokenizer st = new MyStringTokenizer(s, ",");
        String baseOrgId = st.nextToken();
        String suffix = st.nextToken();
        bases.add(baseOrgId);
        suffixes.add(suffix);
      }
    } catch (IOException ie) {
      if (log.isErrorEnabled()) {
        log.error("Error reading from file", ie);
      }
    }

    int count = 0;
    for (int i = 0; i < csvOrgs.size(); i++) {
      CSVOrgInfo org = (CSVOrgInfo)csvOrgs.get(i);
      if (bases.contains(org.baseOrgId) &&
          suffixes.contains(org.suffix)) {
        org.deleted = false;
        count++;
      } else
        org.deleted = true;
    }
    String name = file.getName();
    if (name.endsWith(".csv"))
      name = name.substring(0, name.length()-4);
    setSocietyName(name);
    treeType = CSV_TREE;
    // create a tree
    // find the root, the organization that has no superior
    CSVOrgInfo csvRoot = null;
    for (int i = 0; i < csvOrgs.size(); i++) {
      CSVOrgInfo org = (CSVOrgInfo)csvOrgs.get(i);
      if (org.superiorBaseOrgId == "") {
        csvRoot = org;
        break;
      }
    }
    if (csvRoot == null) {
      if (log.isErrorEnabled()) {
        log.error("No root object");
      }
      return;
    }
    createTree(csvRoot);
    addTreeNodesFromCSV(csvRoot, root);
    updateTree();
    agentCountField.setText(String.valueOf(count));
    // update menu items
    readFromCSVMenuItem.setEnabled(true);
    saveAsCSVMenuItem.setEnabled(true);
    saveAsXMLMenuItem.setEnabled(true);
    viewMenu.setEnabled(true);
    saveMenuItem.setEnabled(false);
    saveAsMenuItem.setEnabled(false);
  }

  private void readCSVAttributes() {
    // get the CSV file to read
    File file = new File(baseCSVFile.getParentFile(), "org_attribute.csv");
    if (file == null)
      return;
    if (!file.canRead())
      return;

    // read the CSV file
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new FileReader(file));
    } catch (FileNotFoundException e) {
      if (log.isErrorEnabled()) {
        log.error("File not found", e);
      }
      return;
    }

    String s;
    csvOrgAttributes = new ArrayList();
    try {
      //      s = reader.readLine(); // skip the first line, which is a comment
      while ((s = reader.readLine()) != null) {
        MyStringTokenizer st = new MyStringTokenizer(s, ",");
        CSVAttributes attrs = getAttributesFromCSV(st);
        if (attrs != null)
          csvOrgAttributes.add(attrs);
      }
    } catch (IOException ie) {
      if (log.isErrorEnabled()) {
        log.error("Error reading from file", ie);
      }
    }
  }

  /**
   * Create a CSVAttributes object fro a line in a org_attribute.csv file:
   * order,orig_org_id,base_org_id,suffix,,combat_support,echelon,echelon_group,
   * is_deployable,has_physical_assets,has_equipment_assets,has_personnel_assets,
   * uic,home_location
   */
  private CSVAttributes getAttributesFromCSV(MyStringTokenizer st) {
    String ignore = st.nextToken(); // ignore order
    st.nextToken();                 // ignore orig_ord_id
    String baseOrgId = st.nextToken();
    String suffix = st.nextToken();
    st.nextToken();                 // ignore empty field
    String combatSupportEchelon = st.nextToken();
    String echelon = st.nextToken();
    String echelonGroup = st.nextToken();
    boolean isDeployable = getFlag(st.nextToken());
    boolean hasPhysicalAssets = getFlag(st.nextToken());
    boolean hasEquipmentAssets = getFlag(st.nextToken());
    boolean hasPersonnelAssets = getFlag(st.nextToken());
    String uic = st.nextToken();
    String homeLocation = st.nextToken();
    return new CSVAttributes(baseOrgId, suffix, 
                             combatSupportEchelon, echelon, echelonGroup,
                             isDeployable, hasPhysicalAssets,
                             hasEquipmentAssets, hasPersonnelAssets,
                             uic, homeLocation);
  }

  private boolean getFlag(String s) {
    if (s.equals("Y") || s.equals("y"))
      return true;
    return false;
  }

  private void readCSVRoles() {
    // get the CSV file to read
    File file = new File(baseCSVFile.getParentFile(), "org_role.csv");
    if (file == null)
      return;
    if (!file.canRead())
      return;

    // read the CSV file
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new FileReader(file));
    } catch (FileNotFoundException e) {
      if (log.isErrorEnabled()) {
        log.error("File not found", e);
      }
      return;
    }

    String s;
    csvOrgRoles = new ArrayList();
    try {
      //      s = reader.readLine(); // skip the first line, which is a comment
      while ((s = reader.readLine()) != null) {
        MyStringTokenizer st = new MyStringTokenizer(s, ",");
        CSVRole role = getRolesFromCSV(st);
        if (role != null)
          csvOrgRoles.add(role);
      }
    } catch (IOException ie) {
      if (log.isErrorEnabled()) {
        log.error("Error reading from file", ie);
      }
    }
  }

  /**
   * Create a CSVRole object from a line in a org_role.csv file:
   * orig_org_id,base_org_id,suffix,role,echelon_of_support,role_mechanism,notes
   * Note that there can be more than one entry per organization.
   */

  private CSVRole getRolesFromCSV(MyStringTokenizer st) {
    String ignore = st.nextToken();
    String baseOrgId = st.nextToken();
    String suffix = st.nextToken();
    String role = st.nextToken();
    String echelonOfSupport = st.nextToken();
    String roleMechanism = st.nextToken();
    return new CSVRole(baseOrgId, suffix, role, 
                       echelonOfSupport, roleMechanism);
  }

  private void readCSVSupport() {
    // get the CSV file to read
    File file = new File(baseCSVFile.getParentFile(), "org_support_cmd_assign.csv");
    if (file == null)
      return;
    if (!file.canRead())
      return;

    // read the CSV file
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new FileReader(file));
    } catch (FileNotFoundException e) {
      if (log.isErrorEnabled()) {
        log.error("File not found", e);
      }
      return;
    }

    String s;
    csvOrgSupport = new ArrayList();
    try {
      s = reader.readLine(); // skip the first line, which is a comment
      while ((s = reader.readLine()) != null) {
        MyStringTokenizer st = new MyStringTokenizer(s, ",");
        CSVSupport support = getSupportFromCSV(st);
        if (support != null)
          csvOrgSupport.add(support);
      }
    } catch (IOException ie) {
      if (log.isErrorEnabled()) {
        log.error("Error reading from file", ie);
      }
    }
  }

  /**
   * Create a CSVSupport object from a line in a org_support_cmd_assign.csv file:
   * order,orig_org_id,base_org_id,suffix,,supported_base_org_id,supported_suffix,echelon_of_support
   * Note that there can be more than one entry per organization.
   */

  private CSVSupport getSupportFromCSV(MyStringTokenizer st) {
    String ignore = st.nextToken(); // order
    ignore = st.nextToken(); // orig_org_id
    String baseOrgId = st.nextToken();
    String suffix = st.nextToken();
    ignore = st.nextToken(); // empty
    String supportedBaseOrgId = st.nextToken();
    String supportedSuffix = st.nextToken();
    String echelon_of_support = st.nextToken();
    return new CSVSupport(baseOrgId, suffix, 
                          supportedBaseOrgId, supportedSuffix, echelon_of_support);
  }

  /**
   * Create a CSVOrgInfo object from a line in a CSV file.
   * Current org_hierarchy.csv format is:
   * order, orig_org_id, base_org_id, suffix, 
   * superior_orig_org_id, superior_base_org_id, superior_suffix, 
   * rollup_code, org_id, , org_id,,,,,,,
   * Extract: 
   * base_org_id, suffix, superior_base_org_id, superior_suffix, rollup_code
   */
  private CSVOrgInfo getOrganizationInfoFromCSV(MyStringTokenizer st) {
    String ignore = st.nextToken(); // order
    ignore = st.nextToken();        // orig_org_id
    String baseOrgId = st.nextToken();
    String suffix = st.nextToken();
    ignore = st.nextToken();        // superior_org_org_id
    String superiorBaseOrgId = st.nextToken();
    String superiorSuffix = st.nextToken();
    String rollupCode = st.nextToken();
    return new CSVOrgInfo(baseOrgId, suffix, superiorBaseOrgId,
                          superiorSuffix, rollupCode);
  }

  /**
   * Populate a tree from information in a CSV file by recursivley adding
   * the entries whose superior is referenced in the parent node.
   */
  private void addTreeNodesFromCSV(CSVOrgInfo parentOrg,
                                   DefaultMutableTreeNode parent) {
    for (int i = 0; i < csvOrgs.size(); i++) {
      CSVOrgInfo org = (CSVOrgInfo)csvOrgs.get(i);
      if (org.superiorBaseOrgId.equals(parentOrg.baseOrgId) &&
          org.superiorSuffix.equals(parentOrg.suffix)) {
        DefaultMutableTreeNode child = new DefaultMutableTreeNode(org);
        parent.add(child);
        addTreeNodesFromCSV(org, child);
      }
    }
  }

  /**
   * Return true if the society name is in the database.
   */
  private boolean isSocietyNameInDatabase(String name) {
    ArrayList names = getSocietyNamesFromDatabase();
    if (names != null && names.contains(name))
      return true;
    else
      return false;
  }

  /**
   * Returns an array of names of the societies in the database.
   */
  private ArrayList getSocietyNamesFromDatabase() {
    return executeQuery("select distinct society_id from society_org;");
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
        if (isSocietyNameInDatabase(name)) {
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
   * Each organization this is a leaf or isn't expanded is represented.
   */
  private void saveSociety() {
    // ask for the society name if the user hasn't given one
    if (societyName == null) {
      setSocietyName(getSocietyName());
      if (societyName == null)
        return;
    }
    // save information for each of the non-expanded nodes and leaves
    Enumeration nodes = root.depthFirstEnumeration();
    while (nodes.hasMoreElements()) {
      DefaultMutableTreeNode node = 
	(DefaultMutableTreeNode)nodes.nextElement();
      TreePath path = new TreePath(node.getPath());
      if (tree.isVisible(path) && !tree.isExpanded(path)) {
        DBOrgInfo orgInfo = (DBOrgInfo)node.getUserObject();
        String update = "Insert into society_org values('" + 
          societyName + "','" +
          orgInfo.name + "','" +
          orgInfo.commandOrgId + "','" +
          orgInfo.code + "');";
        executeUpdate(update);
      }
    }
    modified = false;
  }

  /**
   * Save the society defined by the tree under a new name in the database.
   */
  private void saveSocietyAs() {
    String newSocietyName = getSocietyName();
    if (newSocietyName != null) {
      setSocietyName(newSocietyName);
      saveSociety();
    }
  }

  /**
   * Delete the society selected by the user from the database.
   */
  private void deleteSociety() {
    // get the name of the society to delete
    ArrayList societyNames = getSocietyNamesFromDatabase();
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
    executeUpdate("delete from society_org where society_id = '" + name + "';");
  }

  /**
   * Set society name and display it.
   */
  private void setSocietyName(String name) {
    societyName = name;
    societyNameField.setText(societyName);
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
    setSocietyName(name);
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    Document doc = null;
    try {
      DocumentBuilder db = dbf.newDocumentBuilder();
      doc = db.newDocument();
      Element xmlRoot = doc.createElement("society");
      // add standard society attributes
      xmlRoot.setAttribute("name", societyName);
      xmlRoot.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
      xmlRoot.setAttribute("xsi:noNamespaceSchemaLocation", "society.xsd");
      doc.appendChild(xmlRoot);
      // add localhost xml node
      Element host = doc.createElement("host");
      host.setAttribute("name", "localhost");
      xmlRoot.appendChild(host);
      // add localnode xml node
      Element node = doc.createElement("node");
      node.setAttribute("name", "localnode");
      host.appendChild(node);
      // add tree nodes as xml agent nodes
      saveXMLNodes(doc, node);
      utils.writeXMLFile(new File(file.getParent()), doc, file.getName());
    } catch (ParserConfigurationException e) {
      if (log.isErrorEnabled()) {
        log.error("Exception creating DocumentBuilder.", e);
      }
    } catch (IOException ie) {
      if (log.isErrorEnabled()) {
        log.error("Exception writing XML File.", ie);
      }
    }
  }

  /**
   * Save the non-expanded tree nodes and leaves as XML agent nodes.
   */
  private void saveXMLNodes(Document doc, Element xmlNode) {
    Enumeration treeNodes = root.depthFirstEnumeration();
    while (treeNodes.hasMoreElements()) {
      DefaultMutableTreeNode treeNode = 
	(DefaultMutableTreeNode)treeNodes.nextElement();
      TreePath path = new TreePath(treeNode.getPath());
      if (tree.isVisible(path) && !tree.isExpanded(path)) {
        Element element = doc.createElement("agent");
        if (element != null) {
          element.setAttribute("name", treeNode.toString());
          xmlNode.appendChild(element);
        }
      }
    }
  }

  /**
   * Save the selected organizations in a CSV file of the form:
   * base_org_id, suffix
   * Organizations that are not deleted are saved.
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
    setSocietyName(name);
    String path = file.getPath();
    if (!path.endsWith(".csv"))
      path = path + ".csv";
    try {
      BufferedWriter writer = new BufferedWriter(new FileWriter(path));
      for (int i = 0; i < csvOrgs.size(); i++) {
        CSVOrgInfo org = (CSVOrgInfo)csvOrgs.get(i);
        if (!org.deleted) {
          String s = org.baseOrgId + "," + org.suffix + "\n";
          writer.write(s, 0, s.length());
        }
      } 
      writer.close();
    } catch (IOException ie) {
      if (log.isErrorEnabled()) {
        log.error("Error writing to file", ie);
      }
    }
  }

  /**
   * Save a society defined in CSV files as an XML file.
   */
  private void saveCSVAsXML() {
    JFileChooser fileChooser = 
      new JFileChooser(System.getProperty("org.cougaar.install.path"));
    fileChooser.setDialogTitle("Save As XML File");
    if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
      return;
    File file = fileChooser.getSelectedFile();
    String name = file.getName();
    if (name.endsWith(".xml"))
      name = name.substring(0, name.length()-4);
    setSocietyName(name);
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    Document doc = null;
    try {
      DocumentBuilder db = dbf.newDocumentBuilder();
      doc = db.newDocument();
      Element xmlRoot = doc.createElement("society");
      // add standard society attributes
      xmlRoot.setAttribute("name", societyName);
      xmlRoot.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
      xmlRoot.setAttribute("xsi:noNamespaceSchemaLocation", "society.xsd");
      doc.appendChild(xmlRoot);
      // add localhost xml node
      Element host = doc.createElement("host");
      host.setAttribute("name", "localhost");
      xmlRoot.appendChild(host);
      // add localnode xml node
      Element node = doc.createElement("node");
      node.setAttribute("name", "localnode");
      host.appendChild(node);
      // add tree nodes as xml agent nodes
      saveCSVNodes(doc, node);
      utils.writeXMLFile(new File(file.getParent()), doc, file.getName());
    } catch (ParserConfigurationException e) {
      if (log.isErrorEnabled()) {
        log.error("Exception creating DocumentBuilder.", e);
      }
    } catch (IOException ie) {
      if (log.isErrorEnabled()) {
        log.error("Exception writing XML File.", ie);
      }
    }
  }

  /**
   * Save the non-deleted tree nodes and leaves as XML agent nodes.
   */
  private void saveCSVNodes(Document doc, Element xmlNode) {
    Enumeration treeNodes = root.depthFirstEnumeration();
    while (treeNodes.hasMoreElements()) {
      DefaultMutableTreeNode treeNode = 
	(DefaultMutableTreeNode)treeNodes.nextElement();
      CSVOrgInfo org = (CSVOrgInfo)treeNode.getUserObject();
      if (!org.deleted) {
        Element element = org.toXML(doc);
        xmlNode.appendChild(element);
        CSVAttributes attrs = getAttributes(org);
        if (attrs != null)
          attrs.appendXML(doc, element);
        appendRoles(doc, element, org);
        appendSupport(doc, element, org);
      }
    }
  }

  private CSVAttributes getAttributes(CSVOrgInfo org) {
    String name = org.toString();
    for (int i = 0; i < csvOrgAttributes.size(); i++) {
      CSVAttributes attrs = (CSVAttributes)csvOrgAttributes.get(i);
      if (attrs.getName().equals(name))
        return attrs;
    }
    return null;
  }

  private void appendRoles(Document doc, Element element,
                           CSVOrgInfo org) {
    String name = org.toString();
    for (int i = 0; i < csvOrgRoles.size(); i++) {
      CSVRole role = (CSVRole)csvOrgRoles.get(i);
      if (role.getName().equals(name))
        role.appendXML(doc, element);
    }
  }

  private void appendSupport(Document doc, Element element,
                             CSVOrgInfo org) {
    String name = org.toString();
    for (int i = 0; i < csvOrgSupport.size(); i++) {
      CSVSupport support = (CSVSupport)csvOrgSupport.get(i);
      if (support.getName().equals(name))
        support.appendXML(doc, element);
    }
  }

  /**
   * Exit, but if the user has modified the tree, then ask them to
   * confirm the exit.
   */
  private void exit() {
    if (modified) {
      int answer = 
        JOptionPane.showConfirmDialog(this, "Save Society?",
                                      "Society Is Modified",
                                      JOptionPane.YES_NO_CANCEL_OPTION,
                                      JOptionPane.QUESTION_MESSAGE);
      if (answer == JOptionPane.CANCEL_OPTION)
        return;
      if (answer == JOptionPane.YES_OPTION)
        saveSociety();
      if (answer == JOptionPane.NO_OPTION)
        System.exit(0);
    } else
      System.exit(0);
  }

  /**
   * Expand all the nodes in the tree.
   */
  private void expandTree() {
    Enumeration nodes = root.depthFirstEnumeration();
    while (nodes.hasMoreElements()) {
      DefaultMutableTreeNode node = 
	(DefaultMutableTreeNode)nodes.nextElement();
      tree.expandPath(new TreePath(node.getPath()));
    }
  }

  /**
   * Collapse all the nodes in the tree.
   */
  private void collapseTree() {
    Enumeration nodes = root.depthFirstEnumeration();
    while (nodes.hasMoreElements()) {
      DefaultMutableTreeNode node = 
	(DefaultMutableTreeNode)nodes.nextElement();
      tree.collapsePath(new TreePath(node.getPath()));
    }
  }

  /**
   * Update the number of agents (from database) and display it;
   * called by MyDBTreeExpansionListener when the user expands
   * or collapses a node.
   */
  private void updateDBAgentCount() {
    int nAgents = 0;
    // count the non-expanded nodes and leaves
    Enumeration nodes = root.depthFirstEnumeration();
    while (nodes.hasMoreElements()) {
      DefaultMutableTreeNode node = 
	(DefaultMutableTreeNode)nodes.nextElement();
      TreePath path = new TreePath(node.getPath());
      if (tree.isVisible(path) && !tree.isExpanded(path))
        nAgents++;
    }
    agentCountField.setText(String.valueOf(nAgents));
  }

  /**
   * Update the number of agents (from CSV file) and display it.
   * Called by MyCSVMouseListener when the user right clicks.
   */
  private void updateCSVAgentCount() {
    int nAgents = 0;
    for (int i = 0; i < csvOrgs.size(); i++) {
      CSVOrgInfo org = (CSVOrgInfo)csvOrgs.get(i);
      if (!org.deleted)
        nAgents++;
    }
    agentCountField.setText(String.valueOf(nAgents));
  }

  public static void main(String[] args) {
    JFrame frame = new TreeViewer();
  }

  /**
   * Custom tree cell renderer.
   * Display nodes which will be agents in black and others in gray.
   * For a database tree, nodes which will be agents are
   * leaf nodes and non-expanded nodes.
   * For a CSV tree, nodes which will be agents are the nodes
   * for which the CSVOrgInfo.deleted flag is false.
   */
  class MyTreeCellRenderer extends DefaultTreeCellRenderer {
    public Component getTreeCellRendererComponent(JTree tree,
                                                  Object value,
                                                  boolean sel,
                                                  boolean expanded,
                                                  boolean leaf,
                                                  int row,
                                                  boolean hasFocus) {
      Component c = 
        super.getTreeCellRendererComponent(tree, value, sel,
                                           expanded, leaf, row, hasFocus);
      Font f = c.getFont();
      if (treeType == DB_TREE) {
        if (leaf || !expanded) {
          c.setForeground(Color.black);
          c.setFont(new Font(f.getName(), Font.BOLD, f.getSize()));
        } else {
          c.setForeground(Color.gray);
          c.setFont(new Font(f.getName(), Font.PLAIN, f.getSize()));
        }
      } else if (treeType == CSV_TREE) {
        CSVOrgInfo org = 
          (CSVOrgInfo)((DefaultMutableTreeNode)value).getUserObject();
        if (!org.deleted) {
          c.setForeground(Color.black);
          c.setFont(new Font(f.getName(), Font.BOLD, f.getSize()));
        } else {
          c.setForeground(Color.gray);
          c.setFont(new Font(f.getName(), Font.PLAIN, f.getSize()));
        }
      }
      return c;
    }
  }

  /**
   * Tree expansion listener for tree from database; 
   * update the number of agents when a node is expanded or collapsed.
   */
  class MyDBTreeExpansionListener implements TreeExpansionListener {
    public void treeCollapsed(TreeExpansionEvent e) {
      modified = true;
      updateDBAgentCount();
    }
    public void treeExpanded(TreeExpansionEvent e) {
      modified = true;
      updateDBAgentCount();
    }
  }

  /**
   * Tree will expand listener for tree from CSV file;
   * don't allow expanding a deleted node.
   */
  class MyCSVTreeWillExpandListener implements TreeWillExpandListener {
    public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
    }

    public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
      DefaultMutableTreeNode node = 
        (DefaultMutableTreeNode)event.getPath().getLastPathComponent();
      CSVOrgInfo org = (CSVOrgInfo)node.getUserObject();
      if (org.deleted)
        throw new ExpandVetoException(event);
    }
  }

  /**
   * Mouse listener that deletes/undeletes a node and its
   * children on right click.
   * Used for tree built from CSV.
   */
  class MyCSVMouseListener extends MouseAdapter {

    public void mouseClicked(MouseEvent e) {
      if (e.getButton() == MouseEvent.BUTTON3) {
        TreePath path = tree.getPathForLocation(e.getX(), e.getY());
        if (path == null) return;
        DefaultMutableTreeNode node =
          (DefaultMutableTreeNode)path.getLastPathComponent();
        if (node == root) return; // can't delete root
        CSVOrgInfo org = (CSVOrgInfo)node.getUserObject();
        toggleDeleted(node);
        if (org.deleted)
          tree.collapsePath(path);
        tree.getModel().valueForPathChanged(path, org);
        modified = true;
      }
    }
    private void toggleDeleted(DefaultMutableTreeNode node) {
      Enumeration nodes = node.depthFirstEnumeration();
      while (nodes.hasMoreElements()) {
        DefaultMutableTreeNode descendantNode = 
          (DefaultMutableTreeNode)nodes.nextElement();
        CSVOrgInfo orgInfo = (CSVOrgInfo)descendantNode.getUserObject();
        orgInfo.deleted = !orgInfo.deleted;
      }
      updateCSVAgentCount();
    }
  }

  /**
   * String tokenizer returns empty strings between successive delimiters.
   */
  class MyStringTokenizer extends StringTokenizer {
    String s;
    String delim;

    public MyStringTokenizer(String s, String delim) {
      super(s, delim, true);
      this.s = s;
      this.delim = delim;
    }

    public String nextToken() {
      try {
        String tmp = super.nextToken();
        if (tmp.equals(delim))
          return "";
        else {
          // reads following delimiter if not at end of string
          if (hasMoreTokens())
            super.nextToken();
          return tmp;
        }
      } catch (Exception e) {
        if (log.isErrorEnabled()) {
          log.error("Error reading CSV file", e);
        }
        return "";
      }
    }
  }
  
  /**
   * Information returned from the database for each organization.
   * Used as the "user object" of the tree nodes.
   */

  class DBOrgInfo {
    public String name;
    public String commandOrgId;
    public String code;

    public DBOrgInfo(String orgName, String commandOrgId, String orgCode) {
      name = orgName;
      this.commandOrgId = commandOrgId;
      code = orgCode;
    }

    /**
     * Used by JTree to display the node.
     */
    public String toString() {
      return name;
    }
  }

  /**
   * Information returned from a CSV file for each organization.
   * Used as the "user object" of the tree nodes.
   */

  class CSVOrgInfo {
    public String baseOrgId;
    public String suffix;
    public String superiorBaseOrgId;
    public String superiorSuffix;
    public String rollupCode;
    public boolean deleted = false;

    public CSVOrgInfo(String baseOrgId, String suffix,
                      String superiorBaseOrgId, String superiorSuffix,
                      String rollupCode) {
      this.baseOrgId = baseOrgId;
      this.suffix = suffix;
      this.superiorBaseOrgId = superiorBaseOrgId;
      this.superiorSuffix = superiorSuffix;
      this.rollupCode = rollupCode;
    }

    public String toString() {
      StringBuffer sb = new StringBuffer();
      sb.append(baseOrgId);
      sb.append(".");
      sb.append(suffix);
      return sb.toString();
    }

    public Element toXML(Document doc) {
      Element element = doc.createElement("agent");
      element.setAttribute("name", toString());
      element.setAttribute("classname", "org.cougaar.core.agent.SimpleAgent");
      Element facet = doc.createElement("facet");
      facet.setAttribute("org_id", toString());
      element.appendChild(facet);
      facet = doc.createElement("facet");
      facet.setAttribute("superior_org_id", 
                         superiorBaseOrgId + "." + superiorSuffix);
      element.appendChild(facet);
      return element;
    }
  }

  /**
   * Attributes for an organization; from a org_attribute.csv file
   */

  class CSVAttributes {
    String baseOrgId;
    String suffix;
    String combatSupportEchelon;
    String echelon;
    String echelonGroup;
    boolean isDeployable;
    boolean hasPhysicalAssets;
    boolean hasEquipmentAssets;
    boolean hasPersonnelAssets;
    String uic;
    String homeLocation;

    public CSVAttributes(String baseOrgId, String suffix,
                         String combatSupportEchelon, String echelon,
                         String echelonGroup,
                         boolean isDeployable, boolean hasPhysicalAssets,
                         boolean hasEquipmentAssets, boolean hasPersonnelAssets,
                         String uic, String homeLocation) {
      this.baseOrgId = baseOrgId;
      this.suffix = suffix;
      this.combatSupportEchelon = combatSupportEchelon;
      this.echelon = echelon;
      this.echelonGroup = echelonGroup;
      this.isDeployable = isDeployable;
      this.hasPhysicalAssets = hasPhysicalAssets;
      this.hasEquipmentAssets = hasEquipmentAssets;
      this.hasPersonnelAssets = hasPersonnelAssets;
      this.uic = uic;
      this.homeLocation = homeLocation;
    }

    public String getName() {
      StringBuffer sb = new StringBuffer();
      sb.append(baseOrgId);
      sb.append(".");
      sb.append(suffix);
      return sb.toString();
    }

    public String toString() {
      StringBuffer sb = new StringBuffer();
      sb.append(baseOrgId);
      sb.append(".");
      sb.append(suffix);
      sb.append(",");
      sb.append(combatSupportEchelon);
      sb.append(",");
      sb.append(echelon);
      sb.append(",");
      sb.append(echelonGroup);
      sb.append(",");
      sb.append(isDeployable);
      sb.append(",");
      sb.append(hasPhysicalAssets);
      sb.append(",");
      sb.append(hasEquipmentAssets);
      sb.append(",");
      sb.append(hasPersonnelAssets);
      sb.append(",");
      sb.append(uic);
      sb.append(",");
      sb.append(homeLocation);
      return sb.toString();
    }

    private void addFacet(Document doc, Element element, 
                          String name, String value) {
      Element facet = doc.createElement("facet");
      facet.setAttribute(name, value);
      element.appendChild(facet);
    }

    private void addFacet(Document doc, Element element, 
                          String name, boolean flag) {
      if (flag)
        addFacet(doc, element, name, "T");
      else
        addFacet(doc, element, name, "F");
    }

    public void appendXML(Document doc, Element element) {
      addFacet(doc, element, "home_location", homeLocation);
      addFacet(doc, element, "uic", uic);
      addFacet(doc, element, "combat_support", combatSupportEchelon);
      addFacet(doc, element, "echelon", echelon);
      addFacet(doc, element, "echelon_group", echelonGroup);
      addFacet(doc, element, "is_deployable", isDeployable);
      addFacet(doc, element, "has_physical_assets", hasPhysicalAssets);
      addFacet(doc, element, "has_equipment_assets", hasEquipmentAssets);
      addFacet(doc, element, "has_personnel_assets", hasPersonnelAssets);
    }
  }

  /**
   * Roles for an organization; from a org_role.csv file
   * Note that there may be more than one entry per organization.
   */

  class CSVRole {
    String baseOrgId;
    String suffix;
    String role;
    String echelonOfSupport;
    String roleMechanism;

    public CSVRole(String baseOrgId, String suffix,
                   String role, String echelonOfSupport, String roleMechanism) {
      this.baseOrgId = baseOrgId;
      this.suffix = suffix;
      this.role = role;
      this.echelonOfSupport = echelonOfSupport;
      this.roleMechanism = roleMechanism;
    }

    public String getName() {
      StringBuffer sb = new StringBuffer();
      sb.append(baseOrgId);
      sb.append(".");
      sb.append(suffix);
      return sb.toString();
    }

    public String toString() {
      StringBuffer sb = new StringBuffer();
      sb.append(baseOrgId);
      sb.append(".");
      sb.append(suffix);
      sb.append(",");
      sb.append(role);
      sb.append(",");
      sb.append(echelonOfSupport);
      sb.append(",");
      sb.append(roleMechanism);
      return sb.toString();
    }

    public void appendXML(Document doc, Element element) {
      Element facet = doc.createElement("facet");
      facet.setAttribute("role", role);
      facet.setAttribute("echelon_of_support", echelonOfSupport);
      if (roleMechanism != "")
        facet.setAttribute("mechanism", roleMechanism);
      element.appendChild(facet);
    }
  }

  /**
   * Support for an organization; from a org_support_cmd_assign.csv file
   * Note that there may be more than one entry per organization.
   */

  class CSVSupport {
    String baseOrgId;
    String suffix;
    String supportedBaseOrgId;
    String supportedSuffix;
    String echelonOfSupport;

    public CSVSupport(String baseOrgId, String suffix,
                      String supportedBaseOrgId, String supportedSuffix,
                      String echelonOfSupport) {
      this.baseOrgId = baseOrgId;
      this.suffix = suffix;
      this.supportedBaseOrgId = supportedBaseOrgId;
      this.supportedSuffix = supportedSuffix;
      this.echelonOfSupport = echelonOfSupport;
    }

    public String getName() {
      StringBuffer sb = new StringBuffer();
      sb.append(baseOrgId);
      sb.append(".");
      sb.append(suffix);
      return sb.toString();
    }

    public String toString() {
      StringBuffer sb = new StringBuffer();
      sb.append(baseOrgId);
      sb.append(".");
      sb.append(suffix);
      sb.append(",");
      sb.append(supportedBaseOrgId);
      sb.append(".");
      sb.append(supportedSuffix);
      sb.append(",");
      sb.append(echelonOfSupport);
      return sb.toString();
    }

    public void appendXML(Document doc, Element element) {
      Element facet = doc.createElement("facet");
      facet.setAttribute("sca_supported_org", 
                         supportedBaseOrgId + "." + supportedSuffix);
      facet.setAttribute("sca_echelon_of_support", echelonOfSupport);
      element.appendChild(facet);
    }
  }

}
