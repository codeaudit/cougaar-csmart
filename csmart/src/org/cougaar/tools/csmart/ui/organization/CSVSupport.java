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
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.cougaar.util.log.Logger;

/**
 * Provides support for reading organization information from csv files.
 */
public class CSVSupport implements SocietySupport {
  private ArrayList csvOrgs = new ArrayList();
  private ArrayList csvOrgAttributes = new ArrayList();
  private ArrayList csvOrgRoles = new ArrayList();
  private ArrayList csvOrgSupports = new ArrayList();
  private File baseCSVFile;
  private String societyName;
  private Model model;
  private transient Logger log =
    LoggerSupport.createLogger("org.cougaar.tools.csmart.ui.organization.CSVSupport");

  /**
   * Provide support for reading organization information from
   * csv files.
   * @param model model that drives this tool
   */
  public CSVSupport(Model model) {
    this.model = model;
  }

  /**
   * Get the list of undeleted organizations; doesn't actually
   * change composition of the tree; just indicates which organizations
   * are not deleted.
   * First, read the Base CSV file, which is assumed to be in the same folder,
   * and is named org_hierarchy.csv.
   * Next read the file with a list of the agents to be included in this society;
   * file is in the form:
   * society_id, orig_org_id, base_org_id, suffix
   * Only the last two fields are used.
   * @param filename file to read
   * @return the tree that represents this set of organizations
   */
  public JTree readFile(String filename) {
    // read the base CSV file that defines all agents
    File file = new File(filename);
    String baseFilename = file.getParent() + File.separatorChar + "org_hierarchy.csv";
    baseCSVFile = new File(baseFilename);
    JTree tree = readBaseSociety(model, baseCSVFile);

    // read the CSV file that defines the agents in this society
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new FileReader(file));
    } catch (FileNotFoundException e) {
      if (log.isErrorEnabled()) {
        log.error("File not found", e);
      }
      return tree;
    }
    // if we're just reading the base file, then return
    if (filename.equals(baseFilename)) {
      societyName = "org_hierarchy";
      return tree;
    }

    // this file simply indicates which organizations are not deleted
    String s;
    ArrayList bases = new ArrayList();
    ArrayList suffixes = new ArrayList();
    try {
      while ((s = reader.readLine()) != null) {
        CSVStringTokenizer st = new CSVStringTokenizer(s, ",");
        societyName = st.nextToken();
        st.nextToken();    // orig_org_id
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
    return tree;
  }

  /**
   * Return the name of the society.
   * @return society name
   */
  public String getSocietyName() {
    return societyName;
  }

  /**
   * Return the type of the society, i.e. how it was created.
   * @return Model.SOCIETY_FROM_CSV
   */
  public int getType() {
    return Model.SOCIETY_FROM_CSV;
  }

  /**
   * Return the file extension.
   * @return csv
   */
  public String getFileExtension() {
    return "csv";
  }

  /**
   * Return a descriptive type for this file.
   * @return CSV
   */
  public String getFileTitle() {
    return "CSV";
  }

  /**
   * Read complete society information from a CSV file (org_hierarchy.csv).
   */
  private JTree readBaseSociety(Model model, File file) {
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new FileReader(file));
    } catch (FileNotFoundException e) {
      if (log.isErrorEnabled()) {
        log.error("File not found", e);
      }
      return null;
    }

    baseCSVFile = file;
    csvOrgs = new ArrayList();
    String s;
    try {
      reader.readLine(); // skip the first line, which is a comment
      while ((s = reader.readLine()) != null) {
        CSVStringTokenizer st = new CSVStringTokenizer(s, ",");
        CSVOrgInfo orgInfo = getOrganizationInfoFromCSV(st);
        if (orgInfo != null)
          csvOrgs.add(orgInfo);
      }
    } catch (IOException ie) {
      if (log.isErrorEnabled()) {
        log.error("Error reading from file", ie);
        return null;
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
      return null;
    }
    JTree tree = new CSVTree(model, 
                             new DefaultMutableTreeNode(csvRoot, true));
    addTreeNodesFromCSV(csvRoot, 
                        (DefaultMutableTreeNode)tree.getModel().getRoot());
    readCSVAttributes();
    readCSVRoles();
    readCSVSupport();
    return tree;
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
  private CSVOrgInfo getOrganizationInfoFromCSV(CSVStringTokenizer st) {
    st.nextToken(); // order
    String origOrgId = st.nextToken(); // orig_org_id
    String baseOrgId = st.nextToken();
    String suffix = st.nextToken();
    st.nextToken(); // superior_org_org_id
    String superiorBaseOrgId = st.nextToken();
    String superiorSuffix = st.nextToken();
    st.nextToken(); // rollup code
    return new CSVOrgInfo(origOrgId, baseOrgId, suffix, superiorBaseOrgId,
                          superiorSuffix);
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
   * Read society information from a CSV file.
   * Information is in the form:
   * base_org_id, suffix
   *
   */
  private void readCSVAttributes() {
    // get the CSV file to read
    File file = new File(baseCSVFile.getParentFile(), "org_attribute.csv");
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
      reader.readLine(); // skip the first line, which is a comment
      while ((s = reader.readLine()) != null) {
        CSVStringTokenizer st = new CSVStringTokenizer(s, ",");
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
   * Create a CSVAttributes object from a line in a org_attribute.csv file:
   * order,orig_org_id,base_org_id,suffix,,combat_support,echelon,echelon_group,
   * is_deployable,has_physical_assets,has_equipment_assets,has_personnel_assets,
   * uic,home_location
   */
  private CSVAttributes getAttributesFromCSV(CSVStringTokenizer st) {
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

  /**
   * Read roles from a org_role.csv file.
   * Populates the csvOrgRoles array with CSVOrgRole objects.
   */
  private void readCSVRoles() {
    // get the CSV file to read
    File file = new File(baseCSVFile.getParentFile(), "org_role.csv");
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
      reader.readLine(); // skip the first line, which is a comment
      while ((s = reader.readLine()) != null) {
        CSVStringTokenizer st = new CSVStringTokenizer(s, ",");
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
  private CSVRole getRolesFromCSV(CSVStringTokenizer st) {
    String ignore = st.nextToken();
    String baseOrgId = st.nextToken();
    String suffix = st.nextToken();
    String role = st.nextToken();
    String echelonOfSupport = st.nextToken();
    String roleMechanism = st.nextToken();
    return new CSVRole(baseOrgId, suffix, role, 
                       echelonOfSupport, roleMechanism);
  }

  /**
   * Populate an array of CSVSupportOrgs from a file.
   */
  private void readCSVSupport() {
    // get the CSV file to read
    File file = new File(baseCSVFile.getParentFile(), "org_sca.csv");
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
    csvOrgSupports = new ArrayList();
    try {
      reader.readLine(); // skip the first line, which is a comment
      while ((s = reader.readLine()) != null) {
        CSVStringTokenizer st = new CSVStringTokenizer(s, ",");
        CSVSupportOrg support = getSupportFromCSV(st);
        if (support != null)
          csvOrgSupports.add(support);
      }
    } catch (IOException ie) {
      if (log.isErrorEnabled()) {
        log.error("Error reading from file", ie);
      }
    }
  }

  /**
   * Create a CSVSupportOrg object from a line in a org_support_cmd_assign.csv file:
   * order,orig_org_id,base_org_id,suffix,,supported_base_org_id,supported_suffix,echelon_of_support
   * Note that there can be more than one entry per organization.
   */

  private CSVSupportOrg getSupportFromCSV(CSVStringTokenizer st) {
    String ignore = st.nextToken(); // order
    ignore = st.nextToken(); // orig_org_id
    String baseOrgId = st.nextToken();
    String suffix = st.nextToken();
    ignore = st.nextToken(); // empty
    String supportedBaseOrgId = st.nextToken();
    String supportedSuffix = st.nextToken();
    String echelon_of_support = st.nextToken();
    return new CSVSupportOrg(baseOrgId, suffix, 
                          supportedBaseOrgId, supportedSuffix, echelon_of_support);
  }

  /**
   * Create an XML document for the society.
   * @return the XML document
   */
  public Document getDocument() {
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
      saveXMLNodes(doc, node);
    } catch (ParserConfigurationException e) {
      if (log.isErrorEnabled()) {
        log.error("Exception creating DocumentBuilder.", e);
      }
      doc = null;
    }
    return doc;
  }

  /**
   * Save the non-deleted tree nodes and leaves as XML agent nodes.
   */
  private void saveXMLNodes(Document doc, Element xmlNode) {
    DefaultMutableTreeNode root = 
      (DefaultMutableTreeNode)model.getTree().getModel().getRoot();
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
        appendSubordinates(doc, element, org);
        appendRoles(doc, element, org);
        appendSupport(doc, element, org);
      }
    }
  }

  /**
   * Get the CSVAttributes object for the specified org.
   */
  private CSVAttributes getAttributes(CSVOrgInfo org) {
    String name = org.toString();
    for (int i = 0; i < csvOrgAttributes.size(); i++) {
      CSVAttributes attrs = (CSVAttributes)csvOrgAttributes.get(i);
      if (attrs.getName().equals(name))
        return attrs;
    }
    return null;
  }

  /**
   * Append information about an organization's subordinates to an XML document.
   */
  private void appendSubordinates(Document doc, Element element,
                                         CSVOrgInfo org) {
    String name = org.toString();
    for (int i = 0; i < csvOrgs.size(); i++) {
      CSVOrgInfo nextOrg = (CSVOrgInfo)csvOrgs.get(i);
      String superior = 
        nextOrg.superiorBaseOrgId + "." + nextOrg.superiorSuffix;
      if (superior.equals(name)) {
        Element facet = doc.createElement("facet");
        facet.setAttribute("subordinate", nextOrg.toString());
        element.appendChild(facet);
      }
    }
  }

  /**
   * Append information about an organization's roles to an XML document.
   */
  private void appendRoles(Document doc, Element element,
                                  CSVOrgInfo org) {
    String name = org.toString();
    for (int i = 0; i < csvOrgRoles.size(); i++) {
      CSVRole role = (CSVRole)csvOrgRoles.get(i);
      if (role.getName().equals(name))
        role.appendXML(doc, element);
    }
  }

  /**
   * Append information about an organization's support to an XML document.
   */
  private void appendSupport(Document doc, Element element,
                                    CSVOrgInfo org) {
    String name = org.toString();
    for (int i = 0; i < csvOrgSupports.size(); i++) {
      CSVSupportOrg support = (CSVSupportOrg)csvOrgSupports.get(i);
      if (support.getName().equals(name))
        support.appendXML(doc, element);
    }
  }

  /**
   * Save the society to a CSV file; saves information on undeleted organizations.
   * Returns true if successful.
   * @return true if file written successfully
   */
  public boolean saveFile(File file) {
    try {
      BufferedWriter writer = new BufferedWriter(new FileWriter(file));
      for (int i = 0; i < csvOrgs.size(); i++) {
        CSVOrgInfo org = (CSVOrgInfo)csvOrgs.get(i);
        if (!org.deleted) {
          String s = societyName + "," + org.origOrgId + "," + org.baseOrgId + "," + org.suffix + "\n";
          writer.write(s, 0, s.length());
        }
      } 
      writer.close();
    } catch (IOException ie) {
      if (log.isErrorEnabled()) {
        log.error("Error writing to file", ie);
      }
      return false;
    }
    return true;
  }

  /**
   * Return the number of agents (undeleted organizations) in the society.
   * @return number of agents
   */
  public int updateAgentCount() {
    int nAgents = 0;
    for (int i = 0; i < csvOrgs.size(); i++) {
      CSVOrgInfo org = (CSVOrgInfo)csvOrgs.get(i);
      if (!org.deleted)
        nAgents++;
    }
    return nAgents;
  }
}
