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

import java.util.ArrayList;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Facets {
  ArrayList providersNeeded = new ArrayList();   // the providers needed
  ArrayList echelonsNeeded = new ArrayList();    // echelons of providers 
  ArrayList roles = new ArrayList();             // this agent's roles
  ArrayList providedEchelons = new ArrayList();  // role echelon_of_support
  ArrayList mechanisms = new ArrayList();        // role mechanism
  ArrayList supportedOrgs = new ArrayList();     // sca_supported_org
  ArrayList supportedEchelons = new ArrayList(); // sca_echelon_of_support
  String agentName;

  public Facets(String agentName, ArrayList facetNodes) {
    this.agentName = agentName;
    int nFacets = facetNodes.size();
    for (int j = 0; j < nFacets; j++) {
      Node facetNode = (Node)facetNodes.get(j);
      getInfoFromFacetNode(facetNode);
    }
  }

  public String getAgentName() {
    return agentName;
  }

  private void getInfoFromFacetNode(Node facetNode) {
    NamedNodeMap nnm = facetNode.getAttributes();
    // save information needed to locate providers
    String role = getValue(nnm, "role");
    String echelonOfSupport = getValue(nnm, "echelon_of_support");
    String mechanism = getValue(nnm, "mechanism");
    if (role != null && echelonOfSupport != null)
      addRole(role, echelonOfSupport, mechanism);
    String scaSupportedOrg = getValue(nnm, "sca_supported_org");
    String scaEchelonOfSupport = getValue(nnm, "sca_echelon_of_support");
    if (scaSupportedOrg != null && scaEchelonOfSupport != null)
      addSupported(scaSupportedOrg, scaEchelonOfSupport);
    // determine providers needed
    // if have personnel, need SubsistenceSupplyProvider
    String hasPersonnel = getValue(nnm, "has_personnel_assets");
    if (hasPersonnel != null &&
      (hasPersonnel.equals("T") || hasPersonnel.equals("t")))
      addProviderNeeded("SubsistenceSupplyProvider");
    String hasEquipment = getValue(nnm, "has_equipment_assets");
    // if have equipment, need ammo, fuel, pol, and spare parts
    if (hasEquipment != null &&
      (hasEquipment.equals("T") || hasEquipment.equals("t"))) {
      addProviderNeeded("AmmunitionProvider");
      addProviderNeeded("FuelSupplyProvider");
      addProviderNeeded("PackagedPOLSupplyProvider");
      addProviderNeeded("SparePartsProvider");
    }
    // if its a provider, but not an inventory mgr or packer, need providers
    checkProviderNeeded("AmmunitionProvider");
    checkProviderNeeded("FuelSupplyProvider");
    checkProviderNeeded("PackagedPOLSupplyProvider");
    checkProviderNeeded("SparePartsProvider");
    checkProviderNeeded("SubsistenceSupplyProvider");
  }

  /**
   * If its a provider, but not a TerminalInventoryManager or
   * TerminalAmmunitionPacker, then it needs a provider.
   */
  private void checkProviderNeeded(String s) {
    int i = roles.indexOf(s);
    if (i == -1)
      return;
    String mechanism = (String)mechanisms.get(i);
    if (mechanism == null ||
        (!mechanism.equals("TerminalInventoryManager") &&
         !mechanism.equals("TerminalAmmunitionPacker")))
      addProviderNeeded(s);
  }

  private String getValue(NamedNodeMap nnm, String name) {
    Node n = nnm.getNamedItem(name);
    if (n == null)
      return null;
    return n.getNodeValue();
  }

  /**
   * Determines echelon needed.  
   * If the agent is not a provider, then the echelon is BRIGADE.
   * If the agent is a provider, then the echelon is one greater than it provides.
   * Ensure that providers needed are unique.
   */
  private void addProviderNeeded(String providerNeeded) {
    if (providersNeeded.contains(providerNeeded))
      return;
    providersNeeded.add(providerNeeded);
    int index = roles.indexOf(providerNeeded);
    String echelonNeeded = "BRIGADE";
    if (index != -1) {
      String s = (String)providedEchelons.get(index);
      echelonNeeded = 
        MilitaryEchelon.echelonName(MilitaryEchelon.echelonOrder(s)+1);
    }
    echelonsNeeded.add(echelonNeeded);
  }

  private void addRole(String role, String echelonOfSupport, String mechanism) {
    roles.add(role);
    providedEchelons.add(echelonOfSupport);
    mechanisms.add(mechanism);
  }

  private void addSupported(String scaSupportedOrg, String echelonOfSupport) {
    supportedOrgs.add(scaSupportedOrg);
    supportedEchelons.add(echelonOfSupport);
  }

  public ArrayList getProvidersNeeded() {
    return providersNeeded;
  }

  public ArrayList getEchelonsNeeded() {
    return echelonsNeeded;
  }

  public ArrayList getRoles() {
    return roles;
  }

  public ArrayList getProvidedEchelons() {
    return providedEchelons;
  }

  public ArrayList getSupportedOrgs() {
    return supportedOrgs;
  }

  public ArrayList getSupportedEchelons() {
    return supportedEchelons;
  }


  // for debugging
  public void printAgentInfo(Node agentNode) {
    System.out.println("Agent: " + 
                       agentNode.getAttributes().getNamedItem("name").getNodeValue());
    NodeList facetNodes = agentNode.getChildNodes();
    int nFacets = facetNodes.getLength();
    for (int i = 0; i < nFacets; i++) {
      Node facetNode = facetNodes.item(i);
      NamedNodeMap nnm = facetNode.getAttributes();
      for (int j = 0; j < nnm.getLength(); j++)
        System.out.println(nnm.item(j).getNodeName() + ":" +
                           nnm.item(j).getNodeValue());
    }
  }
}
