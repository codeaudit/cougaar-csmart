package org.cougaar.tools.csmart.ui.organization;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.cougaar.tools.csmart.util.XMLUtils;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: travers
 * Date: Apr 7, 2003
 * Time: 11:14:58 AM
 * To change this template use Options | File Templates.
 */
public class XMLSupport implements SocietySupport {
  private XMLUtils utils = new XMLUtils();
  private int nAgents = 0;
  private Document doc = null;
  private String societyName;
  private Model model;

  public XMLSupport(Model model) {
    this.model = model;
  }

  public JTree readFile(String filename) {
    if (filename.endsWith(".xml"))
      societyName = filename.substring(0, filename.length()-4);
    int lastIndex = societyName.lastIndexOf("/");
    if (lastIndex != -1)
      societyName = societyName.substring(lastIndex+1);
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

  public String getSocietyName() {
    return societyName;
  }

  public int getType() {
    return Model.SOCIETY_FROM_XML;
  }

  public String getFileExtension() {
    return "xml";
  }

  public String getFileTitle() {
    return "XML";
  }

  public int updateAgentCount() {
    return nAgents;
  }

  public Document getDocument() {
    return doc;
  }

  public boolean saveFile(File file) {
    // does nothing because this can't be modified
    return true;
  }

}
