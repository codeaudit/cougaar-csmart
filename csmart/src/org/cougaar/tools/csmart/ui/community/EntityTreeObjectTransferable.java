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

import java.awt.datatransfer.*;
import java.io.*;
import java.util.Arrays;
import java.util.List;
import javax.swing.tree.DefaultMutableTreeNode;

import org.cougaar.tools.csmart.ui.tree.CSMARTDataFlavor;

public class EntityTreeObjectTransferable implements Transferable {
  private DefaultMutableTreeNode treeNode;

  private CSMARTDataFlavor[] theFlavors;

  public EntityTreeObjectTransferable(DefaultMutableTreeNode treeNode) {
    this.treeNode = treeNode;
    CommunityTreeObject cto = (CommunityTreeObject) treeNode.getUserObject();
    if (cto == null) throw new IllegalArgumentException("null userObject");
    if (cto.isAgent()) theFlavors = new CSMARTDataFlavor[] {EntityDNDTree.agentFlavor};
    if (cto.isNode()) theFlavors = new CSMARTDataFlavor[] {EntityDNDTree.nodeFlavor};
    if (cto.isHost()) theFlavors = new CSMARTDataFlavor[] {EntityDNDTree.hostFlavor};
    if (theFlavors == null) throw new IllegalArgumentException("Unknown EntityTreeOObject");
  }

  public synchronized DataFlavor[] getTransferDataFlavors() {
    return theFlavors;
  }

  public boolean isDataFlavorSupported( DataFlavor flavor ) {
    return flavor.equals(theFlavors[0]);
  }

  public synchronized Object getTransferData(DataFlavor flavor) {
    if (flavor instanceof CSMARTDataFlavor) {
      CSMARTDataFlavor cflavor = (CSMARTDataFlavor) flavor;
        for (int i = 0; i < theFlavors.length; i++) {
          if (CommunityTreeObject.flavorEquals(cflavor, theFlavors[i])) {
            return treeNode;
          }
        }
    }
    return null;
  }
}





