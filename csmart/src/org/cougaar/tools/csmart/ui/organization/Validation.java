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

import java.util.*;
import org.w3c.dom.*;

public class Validation {
  // map agent (Node) to facets (Facets)
  private static Hashtable agentsToFacets = new Hashtable(100);

  public static void validateXML(Document doc) {
    agentsToFacets.clear();
    // extract information for each agent from xml document
    Node societyNode = doc.getFirstChild();
    NodeList children = societyNode.getChildNodes();
    int n = children.getLength();
    Node hostNode = null;
    for (int i = 0; i < n; i++)  {
      if (children.item(i).getNodeName().equals("host"))
        hostNode = children.item(i);
    }
    children = hostNode.getChildNodes();
    n = children.getLength();
    Node nodeNode = null;
    for (int i = 0; i < n; i++) {
      if (children.item(i).getNodeName().equals("node"))
        nodeNode = children.item(i);
    }
    children = nodeNode.getChildNodes();
    n = children.getLength();
    ArrayList agentNodes = new ArrayList();
    int nAgents = 0;
    for (int i = 0; i < n; i++) {
      if (children.item(i).getNodeName().equals("agent")) {
        Node agentNode = children.item(i);
        agentNodes.add(agentNode);
        nAgents++;
      }
    }
    // finished extracting nodes from XML document
    for (int i = 0; i < nAgents; i++) {
      Node agentNode = (Node)agentNodes.get(i);
      NodeList agentChildren = agentNode.getChildNodes();
      int nChildren = agentChildren.getLength();
      ArrayList facetNodes = new ArrayList();
      for (int j = 0; j < nChildren; j++) {
        if (agentChildren.item(j).getNodeName().equals("facet"))
          facetNodes.add(agentChildren.item(j));
      }
      agentsToFacets.put(agentNode, new Facets(facetNodes));
    }
    // search for providers
    Enumeration agents = agentsToFacets.keys();
    int validationErrors = 0;
    while (agents.hasMoreElements()) {
      Node agentNode = (Node)agents.nextElement();
      String agentName =
        agentNode.getAttributes().getNamedItem("name").getNodeValue();
      Facets facets = (Facets)agentsToFacets.get(agentNode);
      ArrayList providersNeeded = facets.getProvidersNeeded();
      ArrayList echelonsNeeded = facets.getEchelonsNeeded();
      for (int i = 0; i < providersNeeded.size(); i++) {
        String providerNeeded = (String)providersNeeded.get(i);
        String echelonNeeded = (String)echelonsNeeded.get(i);
        if (echelonNeeded.equals("UNDEFINED")) {
          System.out.println("WARNING: " +
                             agentName + " needs " + providerNeeded +
                             " at echelon " + echelonNeeded);
          validationErrors++;
        } else if (findProvider(agentNode, providerNeeded, echelonNeeded)) {
          System.out.println(agentName + " has " + providerNeeded +
                             " at echelon " + echelonNeeded);
        } else {
          System.out.println("WARNING: " +
                             agentName + " needs " + providerNeeded +
                             " at echelon " + echelonNeeded);
          validationErrors++;
        }
      }
    }
    System.out.println("Validation Errors: " + validationErrors);
  }

  private static Node getSuperior(Node agentNode) {
    NodeList children = agentNode.getChildNodes();
    int n = children.getLength();
    ArrayList facetNodes = new ArrayList();
    for (int i = 0; i < n; i++) {
      if (children.item(i).getNodeName().equals("facet")) {
        facetNodes.add(children.item(i));
      }
    }
    int nFacets = facetNodes.size();
    for (int i = 0; i < nFacets; i++) {
      Node facetNode = (Node)facetNodes.get(i);
      NamedNodeMap nnm = facetNode.getAttributes();
      if (nnm == null)
        continue;
      Node superiorNode = nnm.getNamedItem("superior_org_id");
      if (superiorNode != null) {
        String superiorName = superiorNode.getNodeValue();
        Enumeration nodes = agentsToFacets.keys();
        while (nodes.hasMoreElements()) {
          Node node = (Node)nodes.nextElement();
          String name =
            node.getAttributes().getNamedItem("name").getNodeValue();
          if (name.equals(superiorName))
            return node;
        }        return null;
      }
    }
    return null;
  }

  private static boolean findProviderForAgent(Node agentNode,
                                              String providerNeeded, 
                                              String echelonNeeded) {
    boolean found = false;
    ArrayList info = new ArrayList(agentsToFacets.values());
    String agentName =
      agentNode.getAttributes().getNamedItem("name").getNodeValue();
    for (int i = 0; i < info.size(); i++) {
      Facets facets = (Facets)info.get(i);
      int index = facets.getRoles().indexOf(providerNeeded);
      if (index == -1)
        continue; // not a provider
      index = facets.getSupportedOrgs().indexOf(agentName);
      if (index == -1)
        continue; // doesn't support this agent
      String supportedEchelon = 
        (String)facets.getSupportedEchelons().get(index);
      if (MilitaryEchelon.echelonOrder(supportedEchelon) >= 
          MilitaryEchelon.echelonOrder(echelonNeeded)) {
        found = true;
        break;
      }
    }
    return found;
  }

  /**
   * The discovery service uses an algorithm to score the best provider:
   * +1 for each difference in echelon (between echelon supported and echelon needed)
   * +100 for each level up the superior chain; lowest score wins.
   * This just treats a >= match on echelon as a win,
   * cause going up the chain is weighted against us so heavily.
   */
  private static boolean findProvider(Node agentNode,
                                      String providerNeeded,
                                      String echelonNeeded) {
    if (findProviderForAgent(agentNode, providerNeeded, echelonNeeded))
      return true; // exact match on agent
    // go up the chain
    Node superior = getSuperior(agentNode);
    while (superior != null) {
      if (findProviderForAgent(superior, providerNeeded, echelonNeeded))
        return true;
      superior = getSuperior(superior);
    }
    return false;
  }


  // from service discovery code
  public static class MilitaryEchelon {
    public static final String UNDEFINED = "UNDEFINED";
    public static final String BRIGADE = "BRIGADE";
    public static final String DIVISION = "DIVISION";
    public static final String CORPS = "CORPS";
    public static final String THEATER = "THEATER";
    public static final String USARMY = "US-ARMY";
    public static final String JOINT = "JOINT";

    private static final String[] ECHELON_ORDER =
      {BRIGADE, DIVISION, CORPS, THEATER, USARMY, JOINT};

    public static boolean validMilitaryEchelon(String echelon) {
      return ((echelon.equals(BRIGADE)) ||
              (echelon.equals(DIVISION)) ||
              (echelon.equals(CORPS)) ||
              (echelon.equals(THEATER)) ||
              (echelon.equals(USARMY)) ||
              (echelon.equals(JOINT)) ||
              (echelon.equals(UNDEFINED)));
    }

    public static String mapToMilitaryEchelon(String echelonValue) {
      // Upcase for comparison
      String upCase = echelonValue.toUpperCase();

      if (validMilitaryEchelon(upCase)) {
        return upCase;
      } else {
        return UNDEFINED;
      }
    }

    public static String echelonName(int i) {
      if (i < 0 || i >= ECHELON_ORDER.length)
        return UNDEFINED;
      return ECHELON_ORDER[i];
    }

    public static int echelonOrder(String echelonValue) {
      // Upcase for comparison
      String upCase = echelonValue.toUpperCase();

      for (int index = 0; index < ECHELON_ORDER.length; index++) {
        if (upCase.equals(ECHELON_ORDER[index])) {
          return index;
        }
      }

      return -1;
    }
  }
}
