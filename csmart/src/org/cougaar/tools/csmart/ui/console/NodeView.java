package org.cougaar.tools.csmart.ui.console;

import javax.swing.*;

/**
 * org.cougaar.tools.csmart.ui.console
 *
 */
public class NodeView {
  private NodeModel model;
  private JScrollPane scrollPane;

  public NodeView(NodeModel model) {
    this.model = model;
    scrollPane = new JScrollPane(model.getTextPane());

  }

}
