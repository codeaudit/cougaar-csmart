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

import java.io.*;
import java.util.*;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.cougaar.tools.csmart.util.XMLUtils;
import org.cougaar.util.log.Logger;

public class Model extends Observable {
  public static String TREE_CHANGED = "Tree Changed";
  public static String DB_SOCIETY_CREATED = "Database Society Created";
  public static String CSV_SOCIETY_CREATED = "CSV Society Created";
  public static String SOCIETY_CHANGED = "Society Changed";
  public static String AGENT_COUNT_CHANGED = "Agent Count Changed";

  private JTree tree;
  private boolean modified = false;
  private String societyName = null;
  private int agentCount;
  private XMLUtils utils;
  private transient Logger log;
  // society from database or CSV
  private int societyType;
  private static int SOCIETY_FROM_DB = 1;
  private static int SOCIETY_FROM_CSV = 2;
  private static int SOCIETY_FROM_XML = 3;

  public Model() {
    // create logger
    log = LoggerSupport.createLogger(this.getClass().getName());

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
  }

  /**
   * Set the database name, user name, and password.
   */
  public void setDBParameters(String database, String username,
                              String password) {
    DBSupport.setDBParameters(database, username, password);
  }

  /**
   * Read the named society from the database.
   * The selected organizations are obtained from a society_org table which
   * includes a society_id, org_id and command_org_id.
   * The tree is expanded appropriately to display the selected organizations.
   */
  public JTree readSocietyFromDB(String name) {
    tree = DBSupport.readSociety(this, name);
    if (tree != null)
      finishCreatingNewDBTree(name);
    return tree;
  }

  /**
   * Read the named society from the database.
   * The "base" society is defined in an org_hierarchy table which
   * includes an org_id, command_org_id and org_code.
   */
  private void readBaseSocietyFromDB() {
    tree = DBSupport.readBaseSociety(this);
    if (tree != null)
      finishCreatingNewDBTree("Base");
  }

  private void finishCreatingNewDBTree(String name) {
    societyType = SOCIETY_FROM_DB;
    notifyObservers(new Notification(TREE_CHANGED, tree));
    notifyObservers(DB_SOCIETY_CREATED);
    setSocietyName(name);
    updateDBAgentCount(tree);
    setModified(false);
  }

  private void setModified(boolean flag) {
    modified = flag;
  }

  /**
   * Return true if the society has been modified.
   */
  public boolean getModified() {
    return modified;
  }

  /**
   * Set the name of the society.
   */
  public void setSocietyName(String name) {
    societyName = name;
    notifyObservers(new Notification(SOCIETY_CHANGED, name));
  }

  /**
   * Get the name of the society.
   */
  public String getSocietyName() {
    return societyName;
  }

  /**
   * Collapse all the nodes in the tree.
   */
  public void collapseTree() {
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
   * Expand all the nodes in the tree.
   */
  public void expandTree() {
    DefaultMutableTreeNode root = 
      (DefaultMutableTreeNode)tree.getModel().getRoot();
    Enumeration nodes = root.depthFirstEnumeration();
    while (nodes.hasMoreElements()) {
      DefaultMutableTreeNode node = 
	(DefaultMutableTreeNode)nodes.nextElement();
      tree.expandPath(new TreePath(node.getPath()));
    }
  }

  /**
   * Delete the society from the database.
   */
  public static void deleteSociety(String name) {
    DBSupport.deleteSociety(name);
  }

  /**
   * Return an array of names of the societies in the database.
   */
  public ArrayList getSocietyNamesFromDatabase() {
    return DBSupport.getSocietyNamesFromDatabase();
  }

  /**
   * Returns true if the society is in the database.
   */
  public boolean isSocietyNameInDatabase(String name) {
    return DBSupport.isSocietyNameInDatabase(name);
  }

  /**
   * Save the society in the database.
   */
  public void saveSocietyInDB() {
    DBSupport.saveSociety(tree, societyName);
    setModified(false);
  }

  public void readXML(String filename) {
    tree = XMLSupport.readFile(this, filename);
    if (tree != null) {
      String name = filename;
      if (name.endsWith(".xml"))
        name = name.substring(0, name.length()-4);
      setSocietyName(name);
      societyType = SOCIETY_FROM_XML;
      notifyObservers(new Notification(TREE_CHANGED, tree));
      updateXMLAgentCount();
      setModified(false);
    }
  }

  // TODO: make each support class have a getAgentCount method
  // make each support class have a getSocietyType method
  // a read method, a save method, a getSocietyName method,
  // and a save to XML method
  public void updateXMLAgentCount() {
    agentCount = XMLSupport.getAgentCount();
    setModified(true);
    notifyObservers(new Notification(AGENT_COUNT_CHANGED,
      new Integer(agentCount)));
  }

  /**
   * Create an XML document for the society.
   */
  public Document createXML() {
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
      if (societyType == SOCIETY_FROM_DB)
        DBSupport.saveXMLNodes(tree, doc, node);
      else
        CSVSupport.saveXMLNodes(tree, doc, node);
    } catch (ParserConfigurationException e) {
      if (log.isErrorEnabled()) {
        log.error("Exception creating DocumentBuilder.", e);
      }
      doc = null;
    }
    return doc;
  }

  /**
   * Save the society as an XML file.
   */
  public void saveAsXML(File file) {
    Document doc = createXML();
    if (doc == null)
      return;
    try {
      utils.writeXMLFile(new File(file.getParent()), doc, file.getName());
    } catch (IOException ie) {
      if (log.isErrorEnabled()) {
        log.error("Exception writing XML File.", ie);
      }
    }
    setModified(false);
  }

  /**
   * Read a "base" society from a CSV file.
   */
  public void readFromBaseCSV(File file) {
    tree = CSVSupport.readBaseSociety(this, file);
    if (tree != null) {
      String name = file.getName();
      if (name.endsWith(".csv"))
        name = name.substring(0, name.length()-4);
      setSocietyName(name);
      societyType = SOCIETY_FROM_CSV;
      notifyObservers(new Notification(TREE_CHANGED, tree));
      notifyObservers(CSV_SOCIETY_CREATED);
      updateCSVAgentCount();
      setModified(false);
    }
  }

  /**
   * Read a society from a CSV file.
   * This indicates only which organizations from the base society
   * are to be included.
   */
  public void readCSV(File file) {
    // TODO: this assumes that you've read the correct CSV base file first,
    // so it doesn't actually change the tree
    // this is a bit awkward; how should this be organized?
    CSVSupport.readCSV(file);
    String name = file.getName();
    if (name.endsWith(".csv"))
      name = name.substring(0, name.length()-4);
    setSocietyName(name);
    societyType = SOCIETY_FROM_CSV;
    notifyObservers(new Notification(TREE_CHANGED, tree));
    notifyObservers(CSV_SOCIETY_CREATED);
    updateCSVAgentCount();
    setModified(false);
  }

  /**
   * Save the selected organizations in a CSV file of the form:
   * base_org_id, suffix
   * Organizations that are not deleted are saved.
   */
  public void saveAsCSV(String path) {
    if (CSVSupport.saveAsCSV(path))
      setModified(false);
  }

  /**
   * Validate supply chains in a society created from CSV files.
   */
  public void validateSociety() {
    if (societyType ==  SOCIETY_FROM_CSV) {
      Document doc = createXML();
      if (doc != null)
        Validation.validateXML(doc);
    } else if (societyType == SOCIETY_FROM_XML) {
      Document doc = XMLSupport.getDocument();
      if (doc != null)
        Validation.validateXML(doc);
    }
  }

  /**
   * Update the number of agents for a society
   * created from a database.  The tree argument is passed in,
   * as this is called from the TreeExpansionListener which is created
   * (together with the tree) in DBSupport; thus, this may be called before
   * the DBSupport methods return with the new tree.
   */
  public void updateDBAgentCount(JTree tree) {
    agentCount = DBSupport.getAgentCount(tree);
    setModified(true);
    notifyObservers(new Notification(AGENT_COUNT_CHANGED,
                                     new Integer(agentCount)));
  }    

  /**
   * Update the number of agents for a society
   * created from a CSV file.
   */
  public void updateCSVAgentCount() {
    agentCount = CSVSupport.getAgentCount();
    setModified(true);
    notifyObservers(new Notification(AGENT_COUNT_CHANGED,
                                     new Integer(agentCount)));
  }

  /**
   * Return the number of agents.
   */
  public int getAgentCount() {
    return agentCount;
  }

  /**
   * Overwrite notifyObservers to always mark this object as changed,
   * so that the notification is done.
   */
  public void notifyObservers(Object arg) {
    setChanged();
    super.notifyObservers(arg);
  }
}
