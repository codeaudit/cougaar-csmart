/*
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 *
 * © Copyright 2000, 2001 BBNT Solutions LLC
 * </copyright>
 */

package org.cougaar.tools.csmart.ui.console;

import org.cougaar.tools.csmart.ui.component.ComponentProperties;
import java.awt.datatransfer.*;
import java.io.*;
import java.util.Arrays;
import java.util.List;
import javax.swing.tree.DefaultMutableTreeNode;

public class CTOArrayTransferable implements Transferable {
    private DMTNArray nodes;
    
    private CSMARTDataFlavor[] theFlavors;

    public CTOArrayTransferable(DMTNArray nodes) {
    this.nodes = nodes;
    ConsoleTreeObject cto = (ConsoleTreeObject) nodes.nodes[0].getUserObject();
    if (cto == null) throw new IllegalArgumentException("null userObject");
    if (cto.isAgent()) theFlavors =
                           new CSMARTDataFlavor[] {ConsoleDNDTree.agentArrayFlavor};
    if (cto.isNode()) theFlavors =
                          new CSMARTDataFlavor[] {ConsoleDNDTree.nodeArrayFlavor};
    if (theFlavors == null) throw new IllegalArgumentException("Unknown ConsoleTreeOObject");
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
                if (ConsoleTreeObject.flavorEquals(cflavor, theFlavors[i])) {
                    return nodes;
                }
            }
        }
        return null;
    }
}





