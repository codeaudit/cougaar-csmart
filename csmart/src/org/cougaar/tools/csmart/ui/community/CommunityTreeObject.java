/*
 * <copyright>
 *  
 *  Copyright 2000-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
 */

package org.cougaar.tools.csmart.ui.community;

import org.cougaar.tools.csmart.core.property.BaseComponent;
import org.cougaar.tools.csmart.experiment.HostComponent;
import org.cougaar.tools.csmart.experiment.NodeComponent;
import org.cougaar.tools.csmart.society.AgentComponent;
import org.cougaar.tools.csmart.ui.tree.CSMARTDataFlavor;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.util.log.Logger;

import java.awt.datatransfer.DataFlavor;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class CommunityTreeObject implements Cloneable {
  private String label;
  private String toolTip;
  private boolean isRoot = false;
  private boolean isCommunity = false;
  private boolean isHost = false;
  private boolean isAgent = false;
  private boolean allowsChildren = true;
  private String communityName = null; // community this object is in
  private transient Logger log;

  /**
   * Constructor for root.
   * @param label for the root node
   */
  public CommunityTreeObject(String label) {
    this(null, label, "Root", null);
  }

  public CommunityTreeObject(String label, String type) {
    this(null, label, type, null);
  }
                                          
  public CommunityTreeObject(String label, String type, String communityName) {
    this(null, label, type, communityName);
  }

  public CommunityTreeObject(BaseComponent component) {
    this(component, component.toString(), "", null);
  }

  public CommunityTreeObject(BaseComponent component,
                             String label, String type, String communityName) {
    createLogger();
    this.communityName = communityName;
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
      else if (type.equalsIgnoreCase("Community"))
        isCommunity = true;
      else if (type.equals("Host"))
        isHost = true;
      else if (type.equalsIgnoreCase("Agent")) {
        isAgent = true;
        allowsChildren = false;
      } else if (type.equalsIgnoreCase("Node")) {
        isAgent = true;
        allowsChildren = false;
      } else {
	allowsChildren = false;
	isAgent = true;
        if(log.isInfoEnabled()) {
          log.info("CommunityTreeObject " + label + " created with unknown type: " +
                    type + " in community " + communityName + ". Treating as agent.");
        }
      }
    }
    this.label = label;
    this.toolTip = toolTip;
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
      // nodes are actually agents
      if ((flavorEquals(cflavor, CommunityDNDTree.communityFlavor) ||
           flavorEquals(cflavor, CommunityDNDTree.communityArrayFlavor) ||
           flavorEquals(cflavor, CommunityDNDTree.hostFlavor) ||
           flavorEquals(cflavor, CommunityDNDTree.hostArrayFlavor) ||
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

  public void setCommunityName(String communityName) {
    this.communityName = communityName;
  }

  public String getCommunityName() {
    return communityName;
  }

  public CommunityTreeObject copy() {
    CommunityTreeObject copiedObject = null;
    try {
      copiedObject = (CommunityTreeObject)clone();
    } catch (CloneNotSupportedException e) {
      if(log.isErrorEnabled()) {
        log.error("Clone not supported: " + e);
      }
    }
    return copiedObject;
  }

  private void writeObject(ObjectOutputStream os) throws IOException {
    throw new IOException("Attempt to serialize CommunityTreeObject, check DataFlavor");
  }

  private void readObject(ObjectInputStream is) throws IOException, ClassNotFoundException {
    createLogger();
  }
}


