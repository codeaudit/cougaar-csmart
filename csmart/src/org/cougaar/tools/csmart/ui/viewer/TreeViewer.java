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
import java.io.FileInputStream;
import java.sql.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import org.cougaar.util.DBConnectionPool;
import org.cougaar.util.ConfigFinder;
import org.cougaar.util.log.Logger;
import org.cougaar.util.log.LoggerFactory;

/**
 * Get an organization from the database.
 * Get all of its components and display them as children in the tree.
 */
public class TreeViewer extends JFrame {
  // for example: jdbc:mysql://victoria.bbn.com/FCS, travers, password
  private static String database;
  private static String username;
  private static String password;
  private static String getRootQuery =
    "select org_id, command_org_id, org_code from org_hierarchy where parent_org_id = '';";
  private transient Logger log;

  private JTree tree;
  private JScrollPane treeView;
  private DefaultMutableTreeNode root;
  private String societyName = null;
  private JTextField societyNameField;
  private JTextField agentCountField;
  private boolean modified = false;

  public TreeViewer() {
    super("Society Builder");

    // create logger
    log = lf.createLogger(this.getClass().getName());

    // load mysql driver
    try {
      Class.forName("org.gjt.mm.mysql.Driver");
    } catch (Exception e) {
      if (log.isErrorEnabled()) {
        log.error("Error loading mysql driver", e);
      }
      System.exit(1);
    }

    // create menu bar
    JMenuBar menuBar = new JMenuBar();
    getRootPane().setJMenuBar(menuBar);

    JMenu fileMenu = new JMenu("File");
    fileMenu.setToolTipText("Restore from or save to database or quit.");

    JMenuItem databaseMenuItem = new JMenuItem("Database...");
    databaseMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          getDatabaseInfo();
          createTree();
        }
      });
    databaseMenuItem.setToolTipText("Specify database location.");
    fileMenu.add(databaseMenuItem);

    JMenuItem restoreMenuItem = new JMenuItem("Restore...");
    restoreMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          restoreSociety();
        }
      });
    restoreMenuItem.setToolTipText("Read society description from database.");
    fileMenu.add(restoreMenuItem);

    JMenuItem saveMenuItem = new JMenuItem("Save");
    saveMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          saveSociety();
        }
      });
    saveMenuItem.setToolTipText("Save society description in database.");
    fileMenu.add(saveMenuItem);

    JMenuItem saveAsMenuItem = new JMenuItem("Save As...");
    saveAsMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          saveSocietyAs();
        }
      });
    saveAsMenuItem.setToolTipText("Name and save the society description in the database.");
    fileMenu.add(saveAsMenuItem);

    JMenuItem deleteMenuItem = new JMenuItem("Delete...");
    deleteMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          deleteSociety();
        }
      });
    deleteMenuItem.setToolTipText("Delete the society description from the database.");
    fileMenu.add(deleteMenuItem);

    JMenuItem exitMenuItem = new JMenuItem("Exit");
    exitMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          exit();
        }
      });
    exitMenuItem.setToolTipText("Exit");
    fileMenu.add(exitMenuItem);
    menuBar.add(fileMenu);

    JMenu viewMenu = new JMenu("View");
    viewMenu.setToolTipText("Expand or collapse tree nodes.");
    JMenuItem expandTreeMenuItem = new JMenuItem("Expand Tree");
    expandTreeMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          expandTree();
        }
      });
    expandTreeMenuItem.setToolTipText("Expand all the nodes in the tree.");
    viewMenu.add(expandTreeMenuItem);

    JMenuItem collapseTreeMenuItem = new JMenuItem("Collapse Tree");
    collapseTreeMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          collapseTree();
        }
      });
    collapseTreeMenuItem.setToolTipText("Collapse all the nodes in the tree.");
    viewMenu.add(collapseTreeMenuItem);
    menuBar.add(viewMenu);
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

    // query user for database name, and user name and password
    getDatabaseInfo();
    createTree();
  }

  /**
   * Contact database to get base of tree, create and display tree.
   */
  private void createTree() {
    // execute query to obtain top-level organization info
    ArrayList queryResults = getOrganizationInfo(getRootQuery);
    if (queryResults == null || queryResults.size() == 0) {
      if (log.isErrorEnabled()) {
        log.error("Query to get organizations from database failed.");
      }
      return;
    }
    OrganizationInfo org = (OrganizationInfo)queryResults.get(0);

    // remove any previous tree view
    if (treeView != null)
      getContentPane().remove(treeView);

    // create tree with custom renderer and expansion listener
    root = new DefaultMutableTreeNode(org, true);
    tree = new JTree(root);
    tree.setShowsRootHandles(true); // show toggle on root node
    tree.setCellRenderer(new MyTreeCellRenderer());
    tree.addTreeExpansionListener(new MyTreeExpansionListener());
    addChildren(org.code, root); // populate tree with organizations

    // put tree in scroll pane in frame
    treeView = new JScrollPane(tree);
    getContentPane().add(treeView, BorderLayout.CENTER);
    validate();
  }

  // Logging Methods

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
    ConfigFinder cf = ConfigFinder.getInstance();
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
   * Make a tree node for each organization recursively.
   */
  private void addChildren(String parentOrgCode,
                           DefaultMutableTreeNode parent) {
    ArrayList queryResults = getOrganizationInfo(makeQuery(parentOrgCode));
    if (queryResults != null) {
      // for each child
      for (int i = 0; i < queryResults.size(); i++) {
        OrganizationInfo org = (OrganizationInfo)queryResults.get(i);
        DefaultMutableTreeNode child = new DefaultMutableTreeNode(org);
        parent.add(child);
        addChildren(org.code, child);
      }
    }
  }

  /**
   * Make query used to populate the tree:
   * select org_id, command_org_id, org_code from org_hierarchy
   * where org_code like parent_org_code%
   */
  private String makeQuery(String parent_org_code) {
    return "select org_id, command_org_id, org_code from org_hierarchy where org_code like '" + parent_org_code + "_';";
  }
  
  /**
   * Execute query and return array of OrganizationInfo objects.
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
        results.add(new OrganizationInfo(name, commandOrgId, code));
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
   * Execute a query which returns an array of strings.
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
   * Menu action methods
   */

  /**
   * Display society from database in the tree.
   * Note that the tree is never recreated, we simply change
   * which nodes are expanded.  If an organization is not in the new society,
   * then it is expanded in the tree.
   */
  private void restoreSociety() {
    // get the name of the society to restore
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
      JOptionPane.showConfirmDialog(this, panel, "Restore Society",
				    JOptionPane.OK_CANCEL_OPTION,
				    JOptionPane.PLAIN_MESSAGE);
    if (result != JOptionPane.OK_OPTION) return;
    setSocietyName((String)cb.getSelectedItem());

    // get node names from the new society
    String query = "select org_id from society_org where society_id = '" +
      societyName + "';";
    ArrayList nodeNamesInSociety = executeQuery(query);
    if (nodeNamesInSociety == null) {
      JOptionPane.showMessageDialog(this, "Could Not Read Society",
                                    "No Society", JOptionPane.ERROR_MESSAGE);
      return;
    }

    // redisplay the tree with society nodes visible and not expanded
    // collapse all
    collapseTree();
    // if the node is in the society and is a leaf, then ensure that it's
    // visible by expanding its parent
    Enumeration nodes = root.depthFirstEnumeration();
    while (nodes.hasMoreElements()) {
      DefaultMutableTreeNode node = 
	(DefaultMutableTreeNode)nodes.nextElement();
      OrganizationInfo orgInfo = (OrganizationInfo)node.getUserObject();
      if (nodeNamesInSociety.contains(orgInfo.name) && node.isLeaf()) {
        DefaultMutableTreeNode parent = 
          (DefaultMutableTreeNode)node.getParent();
        tree.expandPath(new TreePath(parent.getPath()));
      }
    }
    modified = false;
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
   * Ask the user for a name for a society to save.
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
      if (tree.isVisible(path) && !tree.isExpanded(path))
        saveNode(node);
    }
    modified = false;
  }

  /**
   * Save the information for the specified node.
   */
  private void saveNode(DefaultMutableTreeNode node) {
    OrganizationInfo orgInfo = (OrganizationInfo)node.getUserObject();
    String update = "Insert into society_org values('" + 
      societyName + "','" +
      orgInfo.name + "','" +
      orgInfo.commandOrgId + "','" +
      orgInfo.code + "');";
    executeUpdate(update);
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
   * Update the number of agents and display it;
   * called by MyTreeExpansionListener whenever the user expands
   * or collapses a node.
   */
  private void updateAgentCount() {
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

  public static void main(String[] args) {
    JFrame frame = new TreeViewer();
//     frame.addWindowListener(new WindowAdapter() {
//         public void windowClosing(WindowEvent e) {
//           System.exit(0);
//         }
//       });
//     frame.pack();
//     // display in center of screen
//     Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
//     frame.setSize(500, 500);
//     int w = frame.getWidth();
//     int h = frame.getHeight();
//     frame.setLocation((screenSize.width - w)/2, (screenSize.height - h)/2);
//     frame.setVisible(true);
  }

  /**
   * Custom tree cell renderer.
   * Display nodes which will be agents 
   * (i.e. leaf nodes and non-expanded nodes) in red.
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
    if (leaf || !expanded) {
      c.setForeground(Color.black);
      c.setFont(new Font(f.getName(), Font.BOLD, f.getSize()));
    } else {
      c.setForeground(Color.gray);
      c.setFont(new Font(f.getName(), Font.PLAIN, f.getSize()));
    }
    return c;
    }
  }

  /**
   * Tree expansion listener; update the number of agents
   * whenever a node is expanded or collapsed.
   */
  class MyTreeExpansionListener implements TreeExpansionListener {
    public void treeCollapsed(TreeExpansionEvent e) {
      modified = true;
      updateAgentCount();
    }
    public void treeExpanded(TreeExpansionEvent e) {
      modified = true;
      updateAgentCount();
    }
  }
  
  /**
   * Information returned from the database for each organization.
   * Used as the "user object" of the tree nodes.
   */

  class OrganizationInfo {
    public String name;
    public String commandOrgId;
    public String code;

    public OrganizationInfo(String orgName, String commandOrgId, 
                            String orgCode) {
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
}
