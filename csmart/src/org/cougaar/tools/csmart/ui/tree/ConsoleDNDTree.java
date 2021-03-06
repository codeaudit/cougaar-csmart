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

package org.cougaar.tools.csmart.ui.tree;

import org.cougaar.tools.csmart.experiment.HostComponent;
import org.cougaar.tools.csmart.experiment.NodeComponent;
import org.cougaar.tools.csmart.society.AgentComponent;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.util.log.Logger;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Provides method definitions for abstract methods in DNDTree.
 * Encapsulates all information specific to the drag-and-drop
 * trees in the Console tool, leaving DNDTree as the generic
 * drag-and-drop tree class.
 */

public class ConsoleDNDTree extends DNDTree {

  private transient Logger log;


  public static CSMARTDataFlavor agentFlavor =
    new CSMARTDataFlavor(DefaultMutableTreeNode.class,
                         AgentComponent.class,
                         ConsoleDNDTree.class,
                         "CSMART Agent");
  public static CSMARTDataFlavor nodeFlavor =
    new CSMARTDataFlavor(DefaultMutableTreeNode.class,
                         NodeComponent.class,
                         ConsoleDNDTree.class,
                         "CSMART Node");
  public static CSMARTDataFlavor hostFlavor =
    new CSMARTDataFlavor(DefaultMutableTreeNode.class,
                         HostComponent.class,
                         ConsoleDNDTree.class,
                         "CSMART Host");
  public static CSMARTDataFlavor agentArrayFlavor =
    new CSMARTDataFlavor(DMTNArray.class,
                         AgentComponent.class,
                         ConsoleDNDTree.class,
                         "CSMART Agent");
  public static CSMARTDataFlavor nodeArrayFlavor =
    new CSMARTDataFlavor(DMTNArray.class,
                         NodeComponent.class,
                         ConsoleDNDTree.class,
                         "CSMART Node");

  public ConsoleDNDTree(DefaultTreeModel model) {
    super(model);
    createLogger();
  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  protected boolean supportsMultiDrag() {
    return true;
  }

  /**
   * Return true if object can be dragged and false otherwise.
   */

  public boolean isDraggable(Object selected) {
    DefaultMutableTreeNode node = (DefaultMutableTreeNode) selected;
    Object userObject = node.getUserObject();
    ConsoleTreeObject cto = (ConsoleTreeObject)userObject;
    if (cto.isHost())
      return true;
    if (node.getParent() == null)
      return false; // don't ever drag root
    return true;
  }

  /**
   * Make a draggable object from a DefaultMutableTreeNode.
   */

  public Transferable makeDraggableObject(Object o) {
    if (o instanceof DefaultMutableTreeNode) {
      return new ConsoleTreeObjectTransferable((DefaultMutableTreeNode) o);
    } else if (o instanceof DMTNArray) {
      return new CTOArrayTransferable((DMTNArray) o);
    }
    throw new IllegalArgumentException("Not a DefaultMutableTreeNode or DMTNArray");
  }

  /**
   * The node being dropped contains either a NodeComponent or AgentComponent.
   * Uses ConsoleTreeObject class to enforce these rules:
   * no component type can be dropped on itself
   * nodes can't be dropped on the "Unassigned Agents" tree root
   * nodes can't be dropped on agents
   * agents can't be dropped on hosts (they have to be dropped on nodes)
   * agents can't be dropped on the "Unassigned Nodes" tree root.
   */

  protected int isDroppable(DataFlavor[] flavors, DefaultMutableTreeNode target) {
    Object userObject = target.getUserObject();
    ConsoleTreeObject cto;
    if (userObject instanceof ConsoleTreeObject) {
      cto = (ConsoleTreeObject) userObject;
      for (int i = 0; i < flavors.length; i++) {
        if (cto.allowsDataFlavor(flavors[i])) {
//           if(log.isDebugEnabled()) {
//             log.debug(cto + " allows " + flavors[i]);
//           }
          return DnDConstants.ACTION_MOVE;
        }
//         if(log.isDebugEnabled()) {
//           log.debug(cto + " disallows " + flavors[i]);
//         }
      }
//       if(log.isDebugEnabled()) {
//         log.debug(cto + " accepts none of the flavors");
//       }
    } else {
//       if(log.isDebugEnabled()) {
//         log.debug(target + " has type " + userObject.getClass());
//       }
    }
    return DnDConstants.ACTION_NONE;
  }

 /**
   * Adds dropped object to this tree; called by drop method.
   * Object is either a ConsoleTreeObject or a DefaultMutableTreeNode
   * depending on whether the user is dragging a leaf or a node.
   * Returns true if successful.
   * This handles the tree and the CSMARTConsole class handles
   * notifying the configurable components.
   */
   
  public int addElement(Transferable transferable, DefaultMutableTreeNode target,
                        DefaultMutableTreeNode before)
  {
    Object data = null;
    try {
      data = transferable.getTransferData(transferable.getTransferDataFlavors()[0]);
    } catch (Exception e) {
      if(log.isErrorEnabled()) {
        log.error("Exception", e);
      }
      return DnDConstants.ACTION_NONE;
    }
    if (data instanceof DMTNArray) {
      DMTNArray nodes = (DMTNArray) data;
      for (int i = 0; i < nodes.nodes.length; i++) {
        addElement(nodes.nodes[i], target, before);
      }
    } else {
      addElement((DefaultMutableTreeNode) data, target, before);
    }
    return DnDConstants.ACTION_MOVE;
  }

  private void addElement(DefaultMutableTreeNode source,
                          DefaultMutableTreeNode target,
                          DefaultMutableTreeNode before)
  {
    ConsoleTreeObject cto = (ConsoleTreeObject) source.getUserObject();
    DefaultMutableTreeNode newNode =
      new DefaultMutableTreeNode(cto, !cto.isAgent());
    int ix = target.getChildCount(); // Drop at end by default
    DefaultTreeModel model = (DefaultTreeModel) getModel();
    if (before != null) {       // If before specified, put it there.
      ix = model.getIndexOfChild(target, before);
    }
    model.insertNodeInto(newNode, target, ix);
    int n = source.getChildCount();
    DefaultMutableTreeNode[] children = new DefaultMutableTreeNode[n];
    for (int i = 0; i < n; i++) {
      children[i] = (DefaultMutableTreeNode) source.getChildAt(i);
    }
    for (int i = 0; i < n; i++) {
      model.insertNodeInto(children[i], newNode, i);
    }
    scrollPathToVisible(new TreePath(newNode.getPath()));
  }

  private void readObject(ObjectInputStream ois)
    throws IOException, ClassNotFoundException
  {
    ois.defaultReadObject();
    createLogger();
  }

}

