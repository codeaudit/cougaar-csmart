package org.cougaar.tools.csmart.ui.organization;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Created by IntelliJ IDEA.
 * User: travers
 * Date: Apr 7, 2003
 * Time: 11:31:35 AM
 * To change this template use Options | File Templates.
 */
public class XMLTree extends JTree {
  Model model;

  public XMLTree(Model model, DefaultMutableTreeNode root) {
    super(root);
    this.model = model;
    setShowsRootHandles(true);
  }
}
