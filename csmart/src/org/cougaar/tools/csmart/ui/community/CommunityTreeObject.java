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

package org.cougaar.tools.csmart.ui.community;

import java.io.Serializable;
import java.io.ObjectOutputStream;
import java.io.IOException;
import org.cougaar.tools.csmart.society.AgentComponent;
import org.cougaar.tools.csmart.experiment.HostComponent;
import org.cougaar.tools.csmart.experiment.NodeComponent;
import org.cougaar.tools.csmart.core.property.BaseComponent;
import org.cougaar.tools.csmart.ui.tree.CSMARTDataFlavor;
import java.awt.datatransfer.DataFlavor;
import org.cougaar.util.log.Logger;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import java.io.ObjectInputStream;

public class CommunityTreeObject {
  private String label;
  private String toolTip;
  private boolean isRoot = false;
  private boolean isCommunity = false;
  private boolean isHost = false;
  private boolean isAgent = false;
  private boolean allowsChildren = true;
  private transient Logger log;

  /**
   * Constructor for root.
   * @param the label for the root node
   * @param allowed class of the children
   */
  public CommunityTreeObject(String label) {
    this(null, label, "Root");
  }
                                          
  public CommunityTreeObject(String label, String type) {
    this(null, label, type);
  }

  public CommunityTreeObject(BaseComponent component) {
    this(component, component.toString(), "");
  }

  public CommunityTreeObject(BaseComponent component,
                             String label, String type) {
    if (component != null) {
      if (component instanceof HostComponent)
        isHost = true;
      else if (component instanceof NodeComponent)
        isAgent = true;
      else if (component instanceof AgentComponent) {
        isAgent = true;
        allowsChildren = false;
      }
    } else {
      if (type.equals("Root"))
        isRoot = true;
      else if (type.equals("Community"))
        isCommunity = true;
      else if (type.equals("Host"))
        isHost = true;
      else if (type.equals("Agent")) {
        isAgent = true;
        allowsChildren = false;
      } else
        System.out.println("CommunityTreeObject created with unknown type: " +
                           type);
    }
    this.label = label;
    this.toolTip = toolTip;
    createLogger();
  }

  protected void setLabel(String label) {
    this.label = label;
  }

  public String toString() {
    return label;
  }

  protected void setToolTip(String toolTip) {
    this.toolTip = toolTip;
  }

  protected String getToolTip() {
    return toolTip;
  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  protected boolean isRoot() {
    return isRoot;
  }

  protected boolean isCommunity() {
    return isCommunity;
  }

  protected boolean isHost() {
    return isHost;
  }

  protected boolean isAgent() {
    return isAgent;
  }

  protected String getType() {
    if (isAgent())
      return "Agent";
    else if (isHost())
      return "Host";
    else if (isCommunity())
      return "Community";
    else if (isRoot())
      return "Root";
    else
      return "Unkown";
  }

  protected boolean allowsChildren() {
    return allowsChildren;
  }

  protected static boolean flavorEquals(CSMARTDataFlavor flavor1,
                                     CSMARTDataFlavor flavor2)
  {
    if (flavor1.equals(flavor2)) {
      String c1 = flavor1.getUserObjectClassName();
      String c2 = flavor2.getUserObjectClassName();
      if (c1 != null) return c1.equals(c2);
    }
    return false;
  }

  protected boolean allowsDataFlavor(DataFlavor flavor) {
    if (flavor instanceof CSMARTDataFlavor) {
      CSMARTDataFlavor cflavor = (CSMARTDataFlavor) flavor;
      // allow dropping hosts, nodes and agents and communities on communities
      if ((flavorEquals(cflavor, CommunityDNDTree.communityFlavor) ||
           flavorEquals(cflavor, CommunityDNDTree.communityArrayFlavor) ||
           flavorEquals(cflavor, CommunityDNDTree.hostFlavor) ||
           flavorEquals(cflavor, CommunityDNDTree.hostArrayFlavor) ||
           flavorEquals(cflavor, CommunityDNDTree.nodeFlavor) ||
           flavorEquals(cflavor, CommunityDNDTree.nodeArrayFlavor) ||
           flavorEquals(cflavor, CommunityDNDTree.agentFlavor) ||
           flavorEquals(cflavor, CommunityDNDTree.agentArrayFlavor)) &&
          isCommunity())
        return true;
      // allow dropping communities on root
      if ((flavorEquals(cflavor, CommunityDNDTree.communityFlavor) ||
           flavorEquals(cflavor, CommunityDNDTree.communityArrayFlavor)) &&
          isRoot())
        return true;
    }
    return false;
  }

  private void writeObject(ObjectOutputStream os) throws IOException {
    throw new IOException("Attempt to serialize CommunityTreeObject, check DataFlavor");
  }

  private void readObject(ObjectInputStream is) throws IOException, ClassNotFoundException {
    createLogger();
  }
}


