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

import javax.swing.tree.DefaultMutableTreeNode;

public class DMTNArray implements java.io.Serializable {
    public DefaultMutableTreeNode[] nodes;
    public DMTNArray(DefaultMutableTreeNode[] nodes) {
        this.nodes = nodes;
    }
}
