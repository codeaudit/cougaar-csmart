package org.cougaar.tools.csmart.ui.console;

import org.cougaar.tools.server.OutputListener;
import org.cougaar.tools.server.OutputPolicy;

import javax.swing.*;

/**
 * This contains all the information needed to create a node
 * and display its output.  It's passed to a separate thread
 * that creates the node.
 */

public final class NodeCreationInfo {
  private final NodeInfo nodeInfo;
  private final OutputListener listener;
  private final OutputPolicy outputPolicy;
  private final JScrollPane scrollPane;
  private final NodeStatusButton statusButton;
  private final String logFileName;

  public NodeCreationInfo(NodeInfo nodeInfo, OutputListener listener, OutputPolicy outputPolicy,
                          JScrollPane scrollPane, NodeStatusButton statusButton, String logFileName) {

    this.nodeInfo = nodeInfo;
    this.listener = listener;
    this.outputPolicy = outputPolicy;
    this.scrollPane = scrollPane;
    this.statusButton = statusButton;
    this.logFileName = logFileName;
  }

  public NodeInfo getNodeInfo() {
    return nodeInfo;
  }

  public OutputListener getListener() {
    return listener;
  }

  public OutputPolicy getOutputPolicy() {
    return outputPolicy;
  }

  public JScrollPane getScrollPane() {
    return scrollPane;
  }

  public NodeStatusButton getStatusButton() {
    return statusButton;
  }

  public String getLogFileName() {
    return logFileName;
  }
}
