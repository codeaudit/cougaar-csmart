/*
 * <copyright>
 *  Copyright 2000-2001 BBNT Solutions, LLC
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

package org.cougaar.tools.csmart.ui.tree;

/**
 * This is an example of a JTree, which serves as a DragSource as 
 * well as Drop Target.
 * Adapted from Java Tutorial web site, under "Drag and Drop".
 */

import java.awt.Point;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.*;
import java.io.IOException;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.*;

/**
 * A JTree with drag and drop capability.
 * The abstract methods need to be defined to provide
 * drag and drop capability for a specific tree.
 */

public abstract class DNDTree
  extends JTree
  implements DropTargetListener,DragSourceListener,DragGestureListener
{
  DropTarget dropTarget = null;  // enables component to be a dropTarget
  DragSource dragSource = null;  // enables component to be a dragSource
  DefaultMutableTreeNode[] dragSourceNodes;     // The (TreeNodes) that are being dragged from here

  /**
   * Initialize DropTarget and DragSource and JTree.
   */

  public DNDTree(DefaultTreeModel model) {
    super(model);
    // make tree editable by default; drag checks the isEditable flag
    setEditable(true); 
    setUI(new SpecialMetalTreeUI());
    dropTarget = new DropTarget(this, this);
    dragSource = new DragSource();
    dragSource.createDefaultDragGestureRecognizer
      (this, 
       DnDConstants.ACTION_COPY_OR_MOVE, 
       this);
  }

  private class SpecialMetalTreeUI extends javax.swing.plaf.metal.MetalTreeUI {
    public SpecialMetalTreeUI() {
      super();
    }

    protected MouseListener createMouseListener() {
      return new SpecialMouseHandler();
    }

    public class SpecialMouseHandler extends MouseHandler {
      MouseEvent pressEvent;
      public void mousePressed(MouseEvent e) {
        pressEvent = e;
        dragSourceNodes = null;
      }
      public void mouseReleased(MouseEvent e) {
        if (dragSourceNodes == null) super.mousePressed(pressEvent);
      }
    }
  }

  /** Utility to select a node in the tree **/
  public void selectNode(TreeNode node) {
    DefaultTreeModel model = (DefaultTreeModel) getModel();
    TreePath path = new TreePath(model.getPathToRoot(node));
    setSelectionPath(path);
  }

  /** Utility to expand a node in the tree **/
  public void expandNode(TreeNode node) {
    DefaultTreeModel model = (DefaultTreeModel) getModel();
    TreePath path = new TreePath(model.getPathToRoot(node));
    expandPath(path);
  }

  abstract protected int isDroppable(DataFlavor[] o, DefaultMutableTreeNode target);

  abstract protected int addElement(Transferable o,
                                    DefaultMutableTreeNode target,
                                    DefaultMutableTreeNode after);

  private DefaultMutableTreeNode getDropTarget(Point location) {
    TreePath path = getPathForLocation(location.x, location.y);
    if (path != null) {
      return (DefaultMutableTreeNode) path.getLastPathComponent();
    } else if (isRootVisible()) {
      return null;
    } else {
      return (DefaultMutableTreeNode) ((DefaultTreeModel) getModel()).getRoot();
    }
  }

  /**
   * Return the action that is appropriate for this intended drop
   **/
  private int testDrop(DropTargetDragEvent event) {
    DataFlavor[] possibleFlavors = event.getCurrentDataFlavors();
    DefaultMutableTreeNode target = getDropTarget(event.getLocation());
    if (target != null) {
      if (target.getAllowsChildren()) {
        int action = isDroppable(possibleFlavors, target);
//          System.out.println("Action is " + action);
        return action;
      } else {
	// target doesn't allow children, but maybe can be sibling
        target = (DefaultMutableTreeNode) target.getParent();
	if (target.getAllowsChildren()) {
          int action = isDroppable(possibleFlavors, target);
//            System.out.println("Action is " + action);
          return action;
	} else {
	  //	  System.out.println(target + " disallows children");
	}
      }
    } else {
      //      System.out.println("no target here");
    }
    return DnDConstants.ACTION_NONE;
  }

  /**
   * A drop has occurred. Determine if the tree can accept the dropped item
   * and if so, insert the item into the tree, otherwise reject the item.
   */

  public void drop(DropTargetDropEvent event) {
    int action = DnDConstants.ACTION_NONE;
    DefaultMutableTreeNode target = getDropTarget(event.getLocation());
    DefaultMutableTreeNode after = null;
    if (target != null && !target.getAllowsChildren()) {
      after = target;
      target = (DefaultMutableTreeNode) after.getParent();
    }
    if (target != null) {
      Transferable transferable = event.getTransferable();
      action = addElement(transferable, target, after);
//        System.out.println("Action " + action);
    }
    boolean success;
    switch (action) {
    case DnDConstants.ACTION_MOVE:
    case DnDConstants.ACTION_COPY:
      event.acceptDrop(action);
      success = true;
      break;
    default:
      event.rejectDrop();
      success = false;
      break;
    }
    event.getDropTargetContext().dropComplete(success);
  }

  /**
   * Decide what, if anything, should be dragged. If multi-drag is
   * supported and the node under the mouse is in the current
   * selection we attempt to drag the entire selection. If any node in
   * the selection is not draggable we drag nothing. If multi-drag is
   * not supported or if the node under the mouse is not in the
   * current selection, we drag only the node under the mouse if it is
   * draggable. 
   * If tree is not editable, return.
   */

  public void dragGestureRecognized(DragGestureEvent event) {
    if (!isEditable()) return; // if tree isn't editable, return
    DefaultMutableTreeNode target = getDropTarget(event.getDragOrigin());
    if (target == null) return; // Nothing to drag.
    TreePath[] paths = getSelectionPaths();
    boolean doMultiDrag =
      paths != null && paths.length > 1 && supportsMultiDrag();
    if (doMultiDrag) {
      boolean targetIsSelected = false;
      for (int i = 0; i < paths.length; i++) {
        DefaultMutableTreeNode node =
          (DefaultMutableTreeNode) paths[i].getLastPathComponent();
        if (!isDraggable(node)) {
          doMultiDrag = false;
          break;
        }
        if (node == target) targetIsSelected = true;
      }
      if (!targetIsSelected) doMultiDrag = false;
    }
    if (doMultiDrag) {
      dragSourceNodes = new DefaultMutableTreeNode[paths.length];
      for (int i = 0; i < paths.length; i++) {
        dragSourceNodes[i] =
          (DefaultMutableTreeNode) paths[i].getLastPathComponent();
      }
//        System.out.println("Multi-drag " + dragSourceNodes.length + " nodes");
      Transferable draggableObject =
        makeDraggableObject(new DMTNArray(dragSourceNodes));
      dragSource.startDrag(event, DragSource.DefaultMoveDrop, 
                           draggableObject, this);
    } else {
      selectNode(target);
      if (isDraggable(target)) {
        dragSourceNodes = new DefaultMutableTreeNode[] {target};
        Transferable draggableObject = makeDraggableObject(target);
        dragSource.startDrag(event, DragSource.DefaultMoveDrop, 
                             draggableObject, this);
      }
    }
  }

  protected boolean supportsMultiDrag() {
    return false;
  }

  abstract public boolean isDraggable(Object selected);

  /**
   * DragSourceListener and DropTargetListener interface.
   */

  /**
   * Dragging has ended; remove dragged object from tree.
   */

  public void dragDropEnd (DragSourceDropEvent event) {
//      System.out.println("drop action = " + event.getDropAction());
    if (event.getDropSuccess()
        && event.getDropAction() == DnDConstants.ACTION_MOVE)
      removeElement();
  }

  /**
   * Removes a dragged element from this tree.
   */
   
  public void removeElement(){
    if (dragSourceNodes != null) {
      for (int i = 0; i < dragSourceNodes.length; i++) {
        ((DefaultTreeModel) getModel())
          .removeNodeFromParent(dragSourceNodes[i]);
      }
      dragSourceNodes = null;
    }
  }
  
  /**
   * This message goes to DragSourceListener, informing it that the dragging 
   * has entered the DropSite.
   */

  public void dragEnter (DragSourceDragEvent event) {
    //    System.out.println( " drag source listener dragEnter");
  }

  /**
   * This message goes to DragSourceListener, informing it that the dragging 
   * is currently ocurring over the DropSite.
   */

  public void dragOver (DragSourceDragEvent event) {
    //    System.out.println( "dragExit");
  }

  /**
   * This message goes to DragSourceListener, informing it that the dragging 
   * has exited the DropSite.
   */

  public void dragExit (DragSourceEvent event) {
    //    System.out.println( "dragExit");
  }

  /**
   * is invoked when the user changes the dropAction
   * 
   */
   
  public void dropActionChanged ( DragSourceDragEvent event) {
//      System.out.println( "dropActionChanged"); 
  }

  /**
   * Dragging over the DropSite.
   */

  public void dragEnter (DropTargetDragEvent event) {
    // start for debugging
    //    DefaultMutableTreeNode target = getDropTarget(event.getLocation());
    //    if (target != null)
    //      System.out.println("DRAG ENTER: " + target.getRoot());
    //    else
    //      System.out.println("DRAG ENTER: " + null);
    // end for debugging
    int action = testDrop(event);
    if (action == DnDConstants.ACTION_NONE)
      event.rejectDrag();
    else
      event.acceptDrag(DnDConstants.ACTION_MOVE);
  }

  /**
   * Drag operation is going on.
   */

  public void dragOver (DropTargetDragEvent event) {
    // start for debugging
    //    DefaultMutableTreeNode target = getDropTarget(event.getLocation());
    //    if (target != null)
    //      System.out.println("drag over: " + target.getRoot());
    //    else
    //      System.out.println("drag over: " + null);
    // end for debugging
    int action = testDrop(event);
    if (action == DnDConstants.ACTION_NONE) {
      event.rejectDrag();
    } else
      event.acceptDrag(DnDConstants.ACTION_MOVE);
  }

  /**
   * Exited DropSite without dropping.
   */

  public void dragExit (DropTargetEvent event) {
    //    System.out.println( "dragExit");
  }

  /**
   * User modifies the current drop gesture
   */
    
  public void dropActionChanged (DropTargetDragEvent event) {
    int action = testDrop(event);
    if (action == DnDConstants.ACTION_NONE)
      event.rejectDrag();
    else
      event.acceptDrag(DnDConstants.ACTION_MOVE);
  }

  /**
   * End DragSourceListener interface.
   */

  /**
   * A drag gesture has been initiated.
   */
  
  abstract public Transferable makeDraggableObject(Object selected);


}
