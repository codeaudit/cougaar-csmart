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

import java.awt.Point;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import org.cougaar.tools.csmart.ui.component.*;

/**
 * Provides method definitions for abstract methods in DNDTree.
 * Encapsulates all information specific to the drag-and-drop
 * trees in the Console tool, leaving DNDTree as the generic
 * drag-and-drop tree class.
 */

public class ConsoleDNDTree extends DNDTree {
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
      return false;
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
    return null;
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
//            System.out.println(cto + " allows " + flavors[i]);
          return DnDConstants.ACTION_MOVE;
        }
//          System.out.println(cto + " disallows " + flavors[i]);
      }
//        System.out.println(cto + " accepts none of the flavors");
    } else {
//        System.out.println(target + " has type " + userObject.getClass());
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
      e.printStackTrace();
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

}

