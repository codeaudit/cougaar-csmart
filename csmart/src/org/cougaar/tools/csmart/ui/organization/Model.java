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
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Attr;

import org.cougaar.util.log.Logger;

/**
 * The model that keeps track of the society information read from files
 * and selected by the user.
 */
public class Model extends Observable {
  public static String TREE_CHANGED = "Tree Changed";
  public static String DB_SOCIETY_CREATED = "Database Society Created";
  public static String CSV_SOCIETY_CREATED = "CSV Society Created";
  public static String XML_SOCIETY_CREATED = "XML Society Created";
  public static String SOCIETY_CHANGED = "Society Changed";
  public static String AGENT_COUNT_CHANGED = "Agent Count Changed";

  private JTree tree;
  private boolean modified = false;
  private String societyName = null;
  private int agentCount;
  private transient Logger log;
  private SocietySupport societySupport;

  // society from database or CSV
  private int societyType;
  public static int SOCIETY_FROM_DB = 1;
  public static int SOCIETY_FROM_CSV = 2;
  public static int SOCIETY_FROM_XML = 3;

  /**
   * Construct model.
   */
  public Model() {
    // create logger
    log = LoggerSupport.createLogger(this.getClass().getName());

    // load mysql driver
//    try {
//      Class.forName("org.gjt.mm.mysql.Driver");
//    } catch (Exception e) {
//      if (log.isErrorEnabled()) {
//        log.error("Error loading mysql driver", e);
//      }
//    }
  }

  /**
   * Set the type of the society, i.e. from database,
   * or from csv or xml file.
   * @param societyType one of SOCIETY_FROM_DB, FROM_CSV, FROM_XML
   */
  public void setSocietyType(int societyType) {
    this.societyType = societyType;
    if (societyType == SOCIETY_FROM_CSV)
      societySupport = new CSVSupport(this);
    else if (societyType == SOCIETY_FROM_XML)
      societySupport = new XMLSupport(this);
  }

  /**
   * Get the file extension appropriate for this society.
   * @return file extension
   */
  public String getFileExtension() {
    return societySupport.getFileExtension();
  }

  /**
   * Get a description of the file appropriate for this society,
   * i.e. CSV or XML
   * @return description
   */
  public String getFileTitle() {
    return societySupport.getFileTitle();
  }

  /**
   * Set whether or not the society is modified.
   * @param flag
   */
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
   * @param name society name
   */
  public void setSocietyName(String name) {
    societyName = name;
    notifyObservers(new Notification(SOCIETY_CHANGED, name));
  }

  /**
   * Get the name of the society.
   * @return society name
   */
  public String getSocietyName() {
    return societyName;
  }

  /**
   * Called when tree is modified.  Call the support code to recount the agents,
   * and notify listeners that the count has changed.
   */
  public void updateAgentCount() {
    agentCount = societySupport.updateAgentCount();
    setModified(true);
    notifyObservers(new Notification(AGENT_COUNT_CHANGED, new Integer(agentCount)));
  }

  /**
   * Get the tree currently being used.
   * @return the tree
   */
  public JTree getTree() {
    return tree;
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
   * Read a file describing a society.
   * @param filename
   */
  public void readFile(String filename) {
    tree = societySupport.readFile(filename);
    if (tree == null)
      return;
    if (societyType == SOCIETY_FROM_CSV)
      notifyObservers(CSV_SOCIETY_CREATED);
    else if (societyType == SOCIETY_FROM_XML)
      notifyObservers(XML_SOCIETY_CREATED);
    setSocietyName(societySupport.getSocietyName());
    notifyObservers(new Notification(TREE_CHANGED, tree));
    updateAgentCount();
    setModified(false);
  }

  /**
   * Save the society in the same type of file from which it was read.
   * @param file to save
   */
  public void saveFile(File file) {
    if (societySupport.saveFile(file))
      setModified(false);
  }

  /**
   * Save the society as an XML file.
   * @param file the XML file to save
   */
  public void saveAsXML(File file) {
    Document doc = societySupport.getDocument();
    if (doc == null)
      return;
    writeXMLFile(new File(file.getParent()), doc, file.getName());
    setModified(false);
  }

  /**
   * Validate supply chains in a society created from CSV files.
   */
  public void validateSociety() {
    Document doc = societySupport.getDocument();
    if (doc != null)
      Validation.validateXML(doc);
  }

  /**
   * Overwrite notifyObservers to always mark this object as changed,
   * so that the notification is done.
   */
  public void notifyObservers(Object arg) {
    setChanged();
    super.notifyObservers(arg);
  }

 /**
   * Writes the contents of the Node to the specified
   * file, in XML format.
   * Extracted from csmart.util.XMLUtils
   *
   * @param configDir - Directory to write new xml file.
   * @param node - Document Node to dump to xml file.
   * @param name - Name of the new xml file.
   */
  private void writeXMLFile(File configDir, Node node, String name) {
    if(!name.endsWith(".xml")) {
      name = name + ".xml";
    }
    PrintWriter writer = null;
    try {
      writer = new PrintWriter(new FileWriter(new File(configDir, name)));
      writeNode(writer, node, 0);
    } catch (Exception e) {
      if(log.isErrorEnabled()) {
        log.error("Error writing XML file: " + e);
      }
    }
    finally {
      if (writer != null)
        writer.close();
    }
  }

  private void writeNode(PrintWriter writer, Node node, int indent) {
    StringBuffer ibuff = new StringBuffer();
    for(int i=0; i < indent; i++) {
      ibuff.append(" ");
    }
    int type = node.getNodeType();
    switch(type) {
      case Node.DOCUMENT_NODE:
        writer.println("<?xml version=\"1.0\" encoding=\""+
          "UTF-8" + "\"?>");
        indent = -2;
        break;
      case Node.ELEMENT_NODE:
        writer.print(ibuff.substring(0) + '<' + node.getNodeName() );
        NamedNodeMap nnm = node.getAttributes();
        if(nnm != null )
        {
          int len = nnm.getLength() ;
          Attr attr;
          for ( int i = 0; i < len; i++ )
          {
            attr = (Attr)nnm.item(i);
            writer.print(' '
              + attr.getNodeName()
              + "=\""
              + attr.getNodeValue()
              +  '"' );
          }
        }
        writer.println('>');
        break;

      case Node.ENTITY_REFERENCE_NODE:
        writer.print('&' + node.getNodeName() + ';' );
        break;
      case Node.CDATA_SECTION_NODE:
        writer.print( "<![CDATA["
          + node.getNodeValue()
          + "]]>" );
        break;
      case Node.TEXT_NODE:
        writer.print(ibuff.substring(0) + node.getNodeValue());
        break;
      case Node.PROCESSING_INSTRUCTION_NODE:
        writer.print(ibuff.substring(0) + "<?"
          + node.getNodeName() ) ;
        String data = node.getNodeValue();
        if ( data != null && data.length() > 0 ) {
          writer.print(' ');
          writer.print(data);
        }
        writer.println("?>");
        break;

    }//end of switch


    //recurse
    for(Node child = node.getFirstChild(); child != null;
        child = child.getNextSibling()) {
      writeNode(writer, child, indent+2);
    }

    //without this the ending tags will miss
    if ( type == Node.ELEMENT_NODE )
    {
      writer.println(ibuff.substring(0) + "</" + node.getNodeName() + ">");
    }
  }

  /**
   * Database support; temporarily unused.
   */

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
}
