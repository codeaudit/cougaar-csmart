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

import java.sql.*;
import java.util.*;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.cougaar.util.DBConnectionPool;
import org.cougaar.util.log.Logger;

public class DBSupport {
  private static String getRootQuery =
    "select org_id, command_org_id, org_code from org_hierarchy where parent_org_id = '';";
  private static String database;
  private static String username;
  private static String password;
  private static transient Logger log = 
    LoggerSupport.createLogger("org.cougaar.tools.csmart.ui.organization.DBSupport");

  /**
   * Set database, user name and password for accessing database.
   */
  public static void setDBParameters(String db, String user, String pwd) {
    database = db;
    username = user;
    password = pwd;
  }

  /**
   * Collapse all the nodes in the tree.
   * TODO: Code repeated from Model; is there a better way?
   */
  private static void collapseTree(JTree tree) {
    DefaultMutableTreeNode root = 
      (DefaultMutableTreeNode)tree.getModel().getRoot();
    Enumeration nodes = root.depthFirstEnumeration();
    while (nodes.hasMoreElements()) {
      DefaultMutableTreeNode node = 
	(DefaultMutableTreeNode)nodes.nextElement();
      tree.collapsePath(new TreePath(node.getPath()));
    }
  }

  /**
   * Read the complete society definition from the database.
   */
  public static JTree readSociety(Model model, String name) {
    // get node names from the new society
    String query = "select org_id from society_org where society_id = '" +
      name + "';";
    ArrayList nodeNamesInSociety = executeQuery(query);
    if (nodeNamesInSociety == null)
      return null;
    JTree tree = readBaseSociety(model);
    collapseTree(tree);
    // if the node is in the society and is a leaf, then ensure that it's
    // visible by expanding its parent
    DefaultMutableTreeNode root = 
      (DefaultMutableTreeNode)tree.getModel().getRoot();
    Enumeration nodes = root.depthFirstEnumeration();
    while (nodes.hasMoreElements()) {
      DefaultMutableTreeNode node = 
	(DefaultMutableTreeNode)nodes.nextElement();
      DBOrgInfo orgInfo = (DBOrgInfo)node.getUserObject();
      if (nodeNamesInSociety.contains(orgInfo.name)) {
        DefaultMutableTreeNode parent = 
          (DefaultMutableTreeNode)node.getParent();
        tree.expandPath(new TreePath(parent.getPath()));
      }
    }
    return tree;
  }

  /**
   * Contact database to get base of tree, create and display tree.
   */
  public static JTree readBaseSociety(Model model) {
    ArrayList queryResults = getOrganizationInfo(getRootQuery);
    if (queryResults.size() == 0) {
      if (log.isErrorEnabled()) {
        log.error("Query to get organizations from database failed.");
      }
      return null;
    }
    DBOrgInfo org = (DBOrgInfo)queryResults.get(0);
    JTree tree = new DBTree(model, new DefaultMutableTreeNode(org, true));
    addTreeNodesFromDB(org.code, 
                       (DefaultMutableTreeNode)tree.getModel().getRoot());
    return tree;
  }

  /**
   * Make a tree node for each organization described in the database
   * recursively.
   */
  private static void addTreeNodesFromDB(String parentOrgCode,
                                         DefaultMutableTreeNode parent) {
    ArrayList orgs = getOrganizationInfo(makeQuery(parentOrgCode));
    // for each child
    for (int i = 0; i < orgs.size(); i++) {
      DBOrgInfo org = (DBOrgInfo)orgs.get(i);
      DefaultMutableTreeNode child = new DefaultMutableTreeNode(org);
      parent.add(child);
      addTreeNodesFromDB(org.code, child);
    }
  }

  /**
   * Execute a database query which returns an array of strings.
   */
  private static ArrayList executeQuery(String query) {
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
   * Make query used to obtain organization info to populate the tree.
   */
  private static String makeQuery(String parent_org_code) {
    return "select org_id, command_org_id, org_code from org_hierarchy where org_code like '" + parent_org_code + "_';";
  }

  /**
   * Execute database query and return array of DBOrgInfo objects.
   */
  private static ArrayList getOrganizationInfo(String query) {
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
   * Delete the named society from the database.
   */
  public static void deleteSociety(String name) {
    executeUpdate("delete from society_org where society_id = '" + 
                  name + "';");
  }

  /**
   * Execute an update, insert or delete from database.
   */
  private static void executeUpdate(String update) {
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
   * Return an array of strings of names of societies in the database.
   */
  public static ArrayList getSocietyNamesFromDatabase() {
    return executeQuery("select distinct society_id from society_org;");
  }

  /**
   * Return true if the society name is in the database.
   */
  public static boolean isSocietyNameInDatabase(String name) {
    ArrayList names = getSocietyNamesFromDatabase();
    if (names != null && names.contains(name))
      return true;
    else
      return false;
  }

  /**
   * Save the society in the database.
   * Saves information for each of the non-expanded nodes and leaves.
   */
  public static void saveSociety(JTree tree, String societyName) {
    DefaultMutableTreeNode root = 
      (DefaultMutableTreeNode)tree.getModel().getRoot();
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
  }

  /**
   * Save the non-expanded tree nodes and leaves as XML agent nodes.
   */
  public static void saveXMLNodes(JTree tree,
                                  Document doc, Element xmlNode) {
    DefaultMutableTreeNode root = 
      (DefaultMutableTreeNode)tree.getModel().getRoot();
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
   * Return the number of agents selected,
   * i.e. the number of non-expanded nodes and leaves.
   */
  public static int getAgentCount(JTree tree) {
    DefaultMutableTreeNode root =
      (DefaultMutableTreeNode)tree.getModel().getRoot();
    int nAgents = 0;
    Enumeration nodes = root.depthFirstEnumeration();
    while (nodes.hasMoreElements()) {
      DefaultMutableTreeNode node = 
	(DefaultMutableTreeNode)nodes.nextElement();
      TreePath path = new TreePath(node.getPath());
      if (tree.isVisible(path) && !tree.isExpanded(path))
        nAgents++;
    }
    return nAgents;
  }


}
