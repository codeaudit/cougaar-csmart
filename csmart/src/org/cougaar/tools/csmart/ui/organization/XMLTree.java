package org.cougaar.tools.csmart.ui.organization;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * The type of tree used for societies defined in XML files.
 */
public class XMLTree extends JTree {
  Model model;

  /**
   * Create a tree for societies read from XML files.
   * @param model model that drives this application
   * @param root root of the tree
   */
  public XMLTree(Model model, DefaultMutableTreeNode root) {
    super(root);
    this.model = model;
    setShowsRootHandles(true);
  }
}
