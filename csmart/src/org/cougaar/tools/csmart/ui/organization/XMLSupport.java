package org.cougaar.tools.csmart.ui.organization;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.cougaar.tools.csmart.util.XMLUtils;

/**
 * Created by IntelliJ IDEA.
 * User: travers
 * Date: Apr 7, 2003
 * Time: 11:14:58 AM
 * To change this template use Options | File Templates.
 */
public class XMLSupport {
  private static XMLUtils utils = new XMLUtils();
  private static int nAgents = 0;
  private static Document doc = null;

  public static JTree readFile(Model model, String filename) {
    doc = utils.loadXMLFile(filename);
    Node societyNode = doc.getFirstChild();
    String societyName =
      societyNode.getAttributes().getNamedItem("name").getNodeValue();
    DefaultMutableTreeNode root = new DefaultMutableTreeNode(societyName, true);
    JTree tree = new XMLTree(model, root);
    NodeList children = societyNode.getChildNodes();
    int n = children.getLength();
    Node hostNode = null;
    for (int i = 0; i < n; i++)  {
      if (children.item(i).getNodeName().equals("host"))
        hostNode = children.item(i);
    }
    String hostName =
      hostNode.getAttributes().getNamedItem("name").getNodeValue();
    DefaultMutableTreeNode hostTreeNode =
      new DefaultMutableTreeNode(hostName, true);
    root.add(hostTreeNode);
    children = hostNode.getChildNodes();
    n = children.getLength();
    Node nodeNode = null;
    for (int i = 0; i < n; i++) {
      if (children.item(i).getNodeName().equals("node"))
        nodeNode = children.item(i);
    }
    String nodeName =
      nodeNode.getAttributes().getNamedItem("name").getNodeValue();
    DefaultMutableTreeNode nodeTreeNode =
      new DefaultMutableTreeNode(nodeName, true);
    hostTreeNode.add(nodeTreeNode);
    children = nodeNode.getChildNodes();
    n = children.getLength();
    nAgents = 0;
    for (int i = 0; i < n; i++) {
      if (children.item(i).getNodeName().equals("agent")) {
        Node agentNode = children.item(i);
        String agentName =
          agentNode.getAttributes().getNamedItem("name").getNodeValue();
        nodeTreeNode.add(new DefaultMutableTreeNode(agentName, true));
        nAgents++;
      }
    }
    return tree;
  }

  public static int getAgentCount() {
    return nAgents;
  }

  public static Document getDocument() {
    return doc;
  }
}
