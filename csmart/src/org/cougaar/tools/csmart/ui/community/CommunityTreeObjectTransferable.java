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

import org.cougaar.tools.csmart.ui.tree.CSMARTDataFlavor;

import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

public class CommunityTreeObjectTransferable implements Transferable {
  private DefaultMutableTreeNode treeNode;

  private CSMARTDataFlavor[] theFlavors;

  public CommunityTreeObjectTransferable(DefaultMutableTreeNode treeNode) {
    this.treeNode = treeNode;
    CommunityTreeObject cto = (CommunityTreeObject) treeNode.getUserObject();
    if (cto == null) throw new IllegalArgumentException("null userObject");
    if (cto.isAgent()) theFlavors = new CSMARTDataFlavor[] {CommunityDNDTree.agentFlavor};
    if (cto.isHost()) theFlavors = new CSMARTDataFlavor[] {CommunityDNDTree.hostFlavor};
    if (cto.isCommunity()) theFlavors = new CSMARTDataFlavor[] {CommunityDNDTree.communityFlavor};
    if (theFlavors == null) throw new IllegalArgumentException("Unknown CommunityTreeOObject");
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





