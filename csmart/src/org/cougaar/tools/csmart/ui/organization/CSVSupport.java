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
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.cougaar.util.log.Logger;

public class CSVSupport {
  private static ArrayList csvOrgs = new ArrayList();
  private static ArrayList csvOrgAttributes = new ArrayList();
  private static ArrayList csvOrgRoles = new ArrayList();
  private static ArrayList csvOrgSupports = new ArrayList();
  private static File baseCSVFile;
  private static transient Logger log = 
    LoggerSupport.createLogger("org.cougaar.tools.csmart.ui.organization.CSVSupport");

  /**
   * Read complete society information from a CSV file.
   * Information is in the form:
   * base_org_id, suffix, superior_base_org_id, supperior_suffix, rollup_code
   *
   */
  public static JTree readBaseSociety(Model model, File file) {
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
      s = reader.readLine(); // skip the first line, which is a comment
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
  private static CSVOrgInfo getOrganizationInfoFromCSV(CSVStringTokenizer st) {
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
  private static void addTreeNodesFromCSV(CSVOrgInfo parentOrg,
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
   * Get the list of undeleted organizations; doesn't actually
   * change composition of the tree; just indicates which organizations
   * are not deleted.
   */
  public static void readCSV(File file) {
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
        CSVStringTokenizer st = new CSVStringTokenizer(s, ",");
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
  }

  /**
   * Read society information from a CSV file.
   * Information is in the form:
   * base_org_id, suffix
   *
   */
  private static void readCSVAttributes() {
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
      s = reader.readLine(); // skip the first line, which is a comment
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
  private static CSVAttributes getAttributesFromCSV(CSVStringTokenizer st) {
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

  private static boolean getFlag(String s) {
    if (s.equals("Y") || s.equals("y"))
      return true;
    return false;
  }

  /**
   * Read roles from a org_role.csv file.
   * Populates the csvOrgRoles array with CSVOrgRole objects.
   */
  private static void readCSVRoles() {
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
      s = reader.readLine(); // skip the first line, which is a comment
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
  private static CSVRole getRolesFromCSV(CSVStringTokenizer st) {
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
  private static void readCSVSupport() {
    // get the CSV file to read
    File file = new File(baseCSVFile.getParentFile(), "org_sca.csv");
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
    csvOrgSupports = new ArrayList();
    try {
      s = reader.readLine(); // skip the first line, which is a comment
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

  private static CSVSupportOrg getSupportFromCSV(CSVStringTokenizer st) {
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
   * Save the non-deleted tree nodes and leaves as XML agent nodes.
   */
  public static void saveXMLNodes(JTree tree,
                                  Document doc, Element xmlNode) {
    DefaultMutableTreeNode root = 
      (DefaultMutableTreeNode)tree.getModel().getRoot();
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
  private static CSVAttributes getAttributes(CSVOrgInfo org) {
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
  private static void appendSubordinates(Document doc, Element element,
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
  private static void appendRoles(Document doc, Element element,
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
  private static void appendSupport(Document doc, Element element,
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
   */
  public static boolean saveAsCSV(String path) {
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
      return false;
    }
    return true;
  }

  /**
   * Return the number of agents (undeleted organizations) in the society.
   */
  public static int getAgentCount() {
    int nAgents = 0;
    for (int i = 0; i < csvOrgs.size(); i++) {
      CSVOrgInfo org = (CSVOrgInfo)csvOrgs.get(i);
      if (!org.deleted)
        nAgents++;
    }
    return nAgents;
  }
}
