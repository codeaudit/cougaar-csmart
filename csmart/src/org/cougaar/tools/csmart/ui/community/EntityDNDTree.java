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

import org.cougaar.tools.csmart.experiment.HostComponent;
import org.cougaar.tools.csmart.society.AgentComponent;
import org.cougaar.tools.csmart.ui.tree.CSMARTDataFlavor;
import org.cougaar.tools.csmart.ui.tree.DMTNArray;
import org.cougaar.tools.csmart.ui.tree.DNDTree;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.util.log.Logger;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Provides method definitions for abstract methods in DNDTree
 * to support Host, Node and Agent trees in the Community Panel
 * of the Experiment Builder.
 */

public class EntityDNDTree extends DNDTree {

  private transient Logger log;

  protected static CSMARTDataFlavor agentFlavor =
    new CSMARTDataFlavor(DefaultMutableTreeNode.class,
                         AgentComponent.class,
                         EntityDNDTree.class,
                         "CSMART Agent");
  protected static CSMARTDataFlavor hostFlavor =
    new CSMARTDataFlavor(DefaultMutableTreeNode.class,
                         HostComponent.class,
                         EntityDNDTree.class,
                         "CSMART Host");
  protected static CSMARTDataFlavor agentArrayFlavor =
    new CSMARTDataFlavor(DMTNArray.class,
                         AgentComponent.class,
                         EntityDNDTree.class,
                         "CSMART Agent");
  protected static CSMARTDataFlavor hostArrayFlavor =
    new CSMARTDataFlavor(DMTNArray.class,
                         HostComponent.class,
                         EntityDNDTree.class,
                         "CSMART Host");

  public EntityDNDTree(DefaultTreeModel model) {
    super(model);
    setToolTipText("");
    createLogger();
  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  public String getToolTipText(MouseEvent evt) {
    if (getRowForLocation(evt.getX(), evt.getY()) == -1) return null;    
    TreePath path = getPathForLocation(evt.getX(), evt.getY());
    DefaultMutableTreeNode node =
      (DefaultMutableTreeNode)path.getLastPathComponent();
    Object o = node.getUserObject();
    if (o != null && o instanceof CommunityTreeObject)
      return ((CommunityTreeObject)o).getToolTip();
    else
      return "";
  }

  protected boolean supportsMultiDrag() {
    return true;
  }

  /**
   * Return true if object can be dragged and false otherwise.
   * Anything but roots can be dragged.
   */

  public boolean isDraggable(Object selected) {
    DefaultMutableTreeNode node = (DefaultMutableTreeNode) selected;
    return (node.getParent() != null);
  }

  /**
   * Make a draggable object from a DefaultMutableTreeNode.
   */

  public Transferable makeDraggableObject(Object o) {
    if (o instanceof DefaultMutableTreeNode) {
      return new EntityTreeObjectTransferable((DefaultMutableTreeNode) o);
    } else if (o instanceof DMTNArray) {
      DMTNArray nodeArray = (DMTNArray)o;
      CommunityTreeObject userObject = 
        (CommunityTreeObject)nodeArray.nodes[0].getUserObject();
      if (userObject == null)
        throw new IllegalArgumentException("null user object");
      CSMARTDataFlavor[] theFlavors = null;
      if (userObject.isAgent())
        theFlavors = new CSMARTDataFlavor[] { agentArrayFlavor };
      else if (userObject.isHost())
        theFlavors = new CSMARTDataFlavor[] { hostArrayFlavor };
      return new CommunityArrayTransferable(nodeArray, theFlavors);
    }
    return null;
  }

  /**
   * You can't drop anything in the host-node-agent trees.
   */

  protected int isDroppable(DataFlavor[] flavors, 
                            DefaultMutableTreeNode target) {
    return DnDConstants.ACTION_NONE;
  }

 /**
   * Adds dropped object to this tree; called by drop method.
   * Object is either a CommunityTreeObject or a DefaultMutableTreeNode
   * depending on whether the user is dragging a leaf or a node.
   * Returns true if successful.
   */
   
  public int addElement(Transferable transferable, 
                        DefaultMutableTreeNode target,
                        DefaultMutableTreeNode before) {
    // this will never be called
    // because you can't drop anything in these trees
    return DnDConstants.ACTION_NONE;
  }

  private void readObject(ObjectInputStream ois)
    throws IOException, ClassNotFoundException
  {
    ois.defaultReadObject();
    createLogger();
  }

}

