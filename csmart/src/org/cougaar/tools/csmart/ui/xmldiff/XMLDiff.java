package org.cougaar.tools.csmart.ui.xmldiff;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;
import org.cougaar.tools.csmart.util.XMLUtils;
import org.cougaar.tools.csmart.ui.monitor.generic.ExtensionFileFilter;
import org.cougaar.tools.csmart.ui.organization.CSVTree;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Collections;
import java.util.Enumeration;

/**
 * Created by IntelliJ IDEA.
 * User: travers
 * Date: Apr 9, 2003
 * Time: 2:19:41 PM
 * To change this template use Options | File Templates.
 */
public class XMLDiff extends JFrame {
  private JTree leftTree;
  private JTree rightTree;
  private XMLUtils utils = new XMLUtils();
  private JTextField leftFileNameField;
  private JTextField rightFileNameField;
  private JSplitPane splitPane;
  private ArrayList leftAgentNames;
  private ArrayList rightAgentNames;
  // map all agent, component, attribute and argument names to tree nodes
  // name is "left-" or "right-" followed by
  // agentname-componentname-attribute or argument
  private Hashtable allNamesToNodes = new Hashtable();
  String prefix;

  public XMLDiff() {
    super("XML Diff");

    // create menu bar
    JMenuBar menuBar = new JMenuBar();
    getRootPane().setJMenuBar(menuBar);

    JMenu fileMenu = new JMenu("File");
    fileMenu.setToolTipText("Read or write society information or quit.");

    JMenuItem selectLeftXMLFile = new JMenuItem("Select Left XML File...");
    selectLeftXMLFile.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          selectLeft();
        }
      });
    selectLeftXMLFile.setToolTipText("Select Left XML File.");
    fileMenu.add(selectLeftXMLFile);

    JMenuItem selectRightXMLFile = new JMenuItem("Select Right XML File...");
    selectRightXMLFile.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        selectRight();
      }
    });
    selectRightXMLFile.setToolTipText("Select Right XML File.");
    fileMenu.add(selectRightXMLFile);

    JMenuItem compareMenuItem = new JMenuItem("Compare");
    compareMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        compare();
      }
    });
    compareMenuItem.setToolTipText("Compare displayed files.");    fileMenu.add(compareMenuItem);

    menuBar.add(fileMenu);
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    panel.add(new JLabel("Left Society File:"));
    leftFileNameField = new JTextField(20);
    leftFileNameField.setEditable(false);
    panel.add(leftFileNameField);
    panel.add(new JLabel("Right Society File:"));
    rightFileNameField = new JTextField(20);
    rightFileNameField.setEditable(false);
    panel.add(rightFileNameField);
    getContentPane().add(panel, BorderLayout.NORTH);

    splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    getContentPane().add(splitPane, BorderLayout.CENTER);
    pack();

    // display in center of screen
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    setSize(700, 500);
    int w = getWidth();
    int h = getHeight();
    setLocation((screenSize.width - w)/2, (screenSize.height - h)/2);
    setVisible(true);
  }

  private void selectLeft() {
    String filename = getFile();
    if (filename == null)
      return;
    leftFileNameField.setText(new File(filename).getName());
    leftAgentNames = new ArrayList();
    JTree tree = readFile("left", filename, leftAgentNames);
    if (tree != null) {
      leftTree = tree;
      leftTree.setShowsRootHandles(true);
      leftTree.setCellRenderer(new MyTreeCellRenderer());
      splitPane.setLeftComponent(new JScrollPane(leftTree));
    }
  }

  private void selectRight() {
    String filename = getFile();
    if (filename == null)
      return;
    rightFileNameField.setText(new File(filename).getName());
    rightAgentNames = new ArrayList();
    JTree tree = readFile("right", filename, rightAgentNames);
    if (tree != null) {
      rightTree = tree;
      rightTree.setShowsRootHandles(true);
      rightTree.setCellRenderer(new MyTreeCellRenderer());
      splitPane.setRightComponent(new JScrollPane(rightTree));
    }
  }

  private String getFile() {
    String filter = "xml";
    String title = "XML";
    JFileChooser chooser = new JFileChooser(System.getProperty("org.cougaar.install.path"));
    chooser.setDialogTitle("Select " + title + " File");
    String[] filters = { filter };
    ExtensionFileFilter extensionFilter = new ExtensionFileFilter(filters, title + " File");
    chooser.addChoosableFileFilter(extensionFilter);
    if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
      return null;
    File file = chooser.getSelectedFile();
    if (file != null && file.canRead())
      return file.getAbsolutePath();
    return null;
  }

  // read file ignoring host, node, agent mapping
  // compare only agents
  private JTree readFile(String prefix, String filename, ArrayList agentNames) {
    String societyName = "";
    if (filename.endsWith(".xml"))
      societyName = filename.substring(0, filename.length()-4);
    int lastIndex = societyName.lastIndexOf("/");
    if (lastIndex != -1)
      societyName = societyName.substring(lastIndex+1);
    Document doc = utils.loadXMLFile(filename);
    Node societyNode = doc.getFirstChild();
    societyName =
      societyNode.getAttributes().getNamedItem("name").getNodeValue();
    DefaultMutableTreeNode root =
      new DefaultMutableTreeNode(new UserObject(societyName), true);
    JTree tree = new JTree(root);
    tree.setShowsRootHandles(true);
    NodeList children = societyNode.getChildNodes();
    Hashtable nameToNode = new Hashtable(); // map agent name to agent XML node
    int n = children.getLength();
    for (int i = 0; i < n; i++)  {
      if (children.item(i).getNodeName().equals("host")) {
        Node hostNode = children.item(i);
        NodeList hostChildren = hostNode.getChildNodes();
        for (int j = 0; j < hostChildren.getLength(); j++) {
          if (hostChildren.item(j).getNodeName().equals("node")) {
            Node nodeNode = hostChildren.item(j);
            NodeList nodeChildren = nodeNode.getChildNodes();
            for (int k = 0; k < nodeChildren.getLength(); k++) {
              if (nodeChildren.item(k).getNodeName().equals("agent")) {
                Node agentNode = nodeChildren.item(k);
                String agentName =
                  agentNode.getAttributes().getNamedItem("name").getNodeValue();
                agentNames.add(agentName);    // all agents in this society
                nameToNode.put(agentName, agentNode);
              }
            } // end for each child of a node
          } // end for each node
        } // end for each child of a host
      } // end for host
    } // end for each child of the society
    // sort agent names
    // use the sorted names to sort the nodes (using the hashtable)
    // and then create and add the agent tree nodes to the tree in order
    Collections.sort(agentNames);
    for (int i = 0; i < agentNames.size(); i++) {
      String agentName = (String)agentNames.get(i);
      UserObject obj = new UserObject(agentName);
      DefaultMutableTreeNode agentTreeNode =
        new DefaultMutableTreeNode(obj, true);
      root.add(agentTreeNode);
      allNamesToNodes.put(prefix + "-" + agentName, agentTreeNode);
      Node agentNode = (Node)nameToNode.get(agentName);
      addComponents(prefix + "-" + agentName, agentNode, agentTreeNode);
    }
    return tree;
  }

//  private JTree readFile(String prefix, String filename, ArrayList agentNames) {
//    String societyName = "";
//    if (filename.endsWith(".xml"))
//      societyName = filename.substring(0, filename.length()-4);
//    int lastIndex = societyName.lastIndexOf("/");
//    if (lastIndex != -1)
//      societyName = societyName.substring(lastIndex+1);
//    Document doc = utils.loadXMLFile(filename);
//    Node societyNode = doc.getFirstChild();
//    societyName =
//      societyNode.getAttributes().getNamedItem("name").getNodeValue();
//    DefaultMutableTreeNode root =
//      new DefaultMutableTreeNode(new UserObject(societyName), true);
//    JTree tree = new JTree(root);
//    tree.setShowsRootHandles(true);
//    NodeList children = societyNode.getChildNodes();
//    int n = children.getLength();
//    for (int i = 0; i < n; i++)  {
//      if (children.item(i).getNodeName().equals("host")) {
//        Node hostNode = children.item(i);
//        String hostName =
//          hostNode.getAttributes().getNamedItem("name").getNodeValue();
//        DefaultMutableTreeNode hostTreeNode =
//          new DefaultMutableTreeNode(new UserObject(hostName), true);
//        root.add(hostTreeNode);
//        addNodeNodes(prefix, hostNode, hostTreeNode, agentNames);
//      }
//    }
//    return tree;
//  }

//  private void addNodeNodes(String prefix,
//                            Node hostNode, DefaultMutableTreeNode hostTreeNode,
//                            ArrayList agentNames) {
//    NodeList children = hostNode.getChildNodes();
//    int n = children.getLength();
//    for (int i = 0; i < n; i++) {
//      if (children.item(i).getNodeName().equals("node")) {
//        Node nodeNode = children.item(i);
//        String nodeName =
//          nodeNode.getAttributes().getNamedItem("name").getNodeValue();
//        DefaultMutableTreeNode nodeTreeNode =
//          new DefaultMutableTreeNode(new UserObject(nodeName), true);
//        hostTreeNode.add(nodeTreeNode);
//        addAgentNodes(prefix, nodeNode, nodeTreeNode, agentNames);
//      }
//    }
//  }

//  private void addAgentNodes(String prefix, Node nodeNode, DefaultMutableTreeNode nodeTreeNode,
//                             ArrayList agentNames) {
//    NodeList children = nodeNode.getChildNodes();
//    int n = children.getLength();
//    ArrayList agentTreeNodes = new ArrayList();
//    ArrayList tmpAgentNames = new ArrayList();
//    Hashtable nameToNode = new Hashtable(); // map agent name to agent tree node
//    for (int i = 0; i < n; i++) {
//      if (children.item(i).getNodeName().equals("agent")) {
//        Node agentNode = children.item(i);
//        String agentName =
//          agentNode.getAttributes().getNamedItem("name").getNodeValue();
//        tmpAgentNames.add(agentName); // agents in this node
//        agentNames.add(agentName);    // all agents in this society
//        DefaultMutableTreeNode agentTreeNode =
//          new DefaultMutableTreeNode(new UserObject(agentName), true);
//        allNamesToNodes.put(prefix + "-" + agentName, agentTreeNode);
//        nameToNode.put(agentName, agentTreeNode); // agents in this node and their tree nodes
//        addComponents(prefix + "-" + agentName, agentNode, agentTreeNode);
//      }
//    }
//    // sort the agent names within a node
//    // use the sorted names to sort the nodes (using the hashtable)
//    // and then add the agent tree nodes to the tree in order
//    Collections.sort(tmpAgentNames);
//    for (int i = 0; i < tmpAgentNames.size(); i++) {
//      nodeTreeNode.add((DefaultMutableTreeNode)nameToNode.get(tmpAgentNames.get(i)));
//    }
//  }

  private void addComponents(String prefix, Node agentNode, DefaultMutableTreeNode agentTreeNode) {
    NodeList children = agentNode.getChildNodes();
    int n = children.getLength();
    for (int i = 0; i < n; i++) {
      if (children.item(i).getNodeName().equals("component")) {
        Node componentNode = children.item(i);
        String componentName =
          componentNode.getAttributes().getNamedItem("name").getNodeValue();
        DefaultMutableTreeNode componentTreeNode =
          new DefaultMutableTreeNode(new UserObject(componentName), true);
        allNamesToNodes.put(prefix + "-" + componentName, componentTreeNode);
        agentTreeNode.add(componentTreeNode);
        addAttributes(prefix + "-" + componentName, componentNode, componentTreeNode);
        addArguments(prefix + "-" + componentName, componentNode, componentTreeNode);
      }
    }
  }

  private void addAttributes(String prefix, Node componentNode, DefaultMutableTreeNode componentTreeNode) {
    DefaultMutableTreeNode attributesNode =
      new DefaultMutableTreeNode(new UserObject("attributes"), true);
    componentTreeNode.add(attributesNode);
    NamedNodeMap attributes = componentNode.getAttributes();
    int n = attributes.getLength();
    for (int i = 0; i < n; i++) {
      Node node = attributes.item(i);
      String name = node.getNodeName();
      String value = node.getNodeValue();
      String attr = name.trim() + ":" + value.trim();
      DefaultMutableTreeNode attrNode =
        new DefaultMutableTreeNode(new UserObject(attr), true);
      allNamesToNodes.put(prefix + "-" + attr, attrNode);
      attributesNode.add(attrNode);
    }
  }

  private void addArguments(String prefix, Node componentNode, DefaultMutableTreeNode componentTreeNode) {
    DefaultMutableTreeNode argumentsNode =
      new DefaultMutableTreeNode(new UserObject("arguments"), true);
    componentTreeNode.add(argumentsNode);
    NodeList children = componentNode.getChildNodes();
    int n = children.getLength();
    for (int i = 0; i < n; i++) {
      Node node = children.item(i);
      if (node.getNodeName().equals("argument")) {
        NodeList args = node.getChildNodes();
        for (int j = 0; j < args.getLength(); j++) {
          Node argNode = args.item(j);
          String arg = argNode.getNodeValue();
          arg = arg.trim();
          DefaultMutableTreeNode argTreeNode =
            new DefaultMutableTreeNode(new UserObject(arg), false);
          allNamesToNodes.put(prefix + "-" + arg, argTreeNode);
          argumentsNode.add(argTreeNode);
        }

      }
    }
  }

  /**
   * Compares agents.
   * Error if any agent is in one tree, but not the other.
   * Ignores distribution of agents in nodes.
   */
  private void compare() {
    ArrayList commonAgentNames = new ArrayList();
    for (int i = 0; i < leftAgentNames.size(); i++)
      if (!rightAgentNames.contains(leftAgentNames.get(i))) {
//        System.out.println("In left tree only: " + leftAgentNames.get(i));
        markDifferent((DefaultMutableTreeNode)allNamesToNodes.get("left-" + leftAgentNames.get(i)));
      } else
        commonAgentNames.add(leftAgentNames.get(i));
    for (int i = 0; i < rightAgentNames.size(); i++)
      if (!leftAgentNames.contains(rightAgentNames.get(i))) {
//        System.out.println("In right tree only: " + rightAgentNames.get(i));
        markDifferent((DefaultMutableTreeNode)allNamesToNodes.get("right-" + rightAgentNames.get(i)));
      }
    for (int i = 0; i < commonAgentNames.size(); i++) {
      String agentName = (String)commonAgentNames.get(i);
      DefaultMutableTreeNode leftAgentNode =
        (DefaultMutableTreeNode)allNamesToNodes.get("left-" + agentName);
      DefaultMutableTreeNode rightAgentNode =
        (DefaultMutableTreeNode)allNamesToNodes.get("right-" + agentName);
      compareComponents(leftAgentNode, rightAgentNode);
    }
    expandTree(leftTree);
    expandTree(rightTree);
  }

  /**
   * If the status (different or not) of any child node is different
   * than the status of this node, then expand this node.
   */
  private void expandTree(JTree tree) {
    DefaultMutableTreeNode root =
      (DefaultMutableTreeNode)tree.getModel().getRoot();
    Enumeration nodes = root.depthFirstEnumeration();
    while (nodes.hasMoreElements()) {
      DefaultMutableTreeNode node =
        (DefaultMutableTreeNode)nodes.nextElement();
      boolean expand = false;
      boolean isDifferent = ((UserObject)node.getUserObject()).isDifferent();
      Enumeration children = node.depthFirstEnumeration();
      while (children.hasMoreElements()) {
        DefaultMutableTreeNode child =
          (DefaultMutableTreeNode)children.nextElement();
        if (((UserObject)child.getUserObject()).isDifferent() != isDifferent) {
          expand = true;
          break;
        }
      }
      if (expand)
        tree.expandPath(new TreePath(node.getPath()));
      else
        tree.collapsePath(new TreePath(node.getPath()));
    }
  }

  /**
   * Sets a flag indicating that the node is different
   * (i.e. unique to its tree).
   * @param node node that is different
   */
  private void setDifferent(DefaultMutableTreeNode node) {
    UserObject userObject = (UserObject)node.getUserObject();
    userObject.setDifferent(true);
    TreePath path = new TreePath(node.getPath());
    TreeNode myRoot = node.getRoot();
    if (myRoot.equals(leftTree.getModel().getRoot()))
      leftTree.getModel().valueForPathChanged(path, userObject);
    else
      rightTree.getModel().valueForPathChanged(path, userObject);
  }

  /**
   * Compares components within an agent.
   * Error if any component is in an agent in one tree, but not in an agent
   * with the same name in the other tree.
   * @param leftAgentNode tree node for an agent in the left tree
   * @param rightAgentNode tree node for the agent with the same name in the right tree
   */
  private void compareComponents(DefaultMutableTreeNode leftAgentNode,
                                 DefaultMutableTreeNode rightAgentNode) {
    String agentName = leftAgentNode.toString();
    Enumeration leftChildren = leftAgentNode.children();
    Enumeration rightChildren = rightAgentNode.children();
    ArrayList leftComponentNames = new ArrayList();
    Hashtable leftComponentNameToNode = new Hashtable(); // map component to tree node
    ArrayList rightComponentNames = new ArrayList();
    Hashtable rightComponentNameToNode = new Hashtable();
    getComponents(leftChildren, leftComponentNames, leftComponentNameToNode);
    getComponents(rightChildren, rightComponentNames, rightComponentNameToNode);
    ArrayList commonComponentNames = new ArrayList();
    // check for differences in components
    // if the component is different, then also mark all its children as different
    for (int i = 0; i < leftComponentNames.size(); i++)
      if (!rightComponentNames.contains(leftComponentNames.get(i))) {
//        System.out.println("In left tree only: " + leftComponentNames.get(i));
        String s = "left-" + agentName + "-" + leftComponentNames.get(i);
        markDifferent((DefaultMutableTreeNode)allNamesToNodes.get(s));
      } else
        commonComponentNames.add(leftComponentNames.get(i));
    for (int i = 0; i < rightComponentNames.size(); i++)
      if (!leftComponentNames.contains(rightComponentNames.get(i))) {
//        System.out.println("In right tree only: " + rightComponentNames.get(i));
        String s = "right-" + agentName + "-" + rightComponentNames.get(i);
        markDifferent((DefaultMutableTreeNode)allNamesToNodes.get(s));
      }
    // check that each component has the same attributes
    // check that each component has the same arguments
    for (int i = 0; i < commonComponentNames.size(); i++) {
      String componentName = (String)commonComponentNames.get(i);
      DefaultMutableTreeNode leftComponentNode =
        (DefaultMutableTreeNode)leftComponentNameToNode.get(componentName);
      DefaultMutableTreeNode rightComponentNode =
        (DefaultMutableTreeNode)rightComponentNameToNode.get(componentName);
      String prefix = agentName + "-" + componentName;
      compareAttributes(prefix, leftComponentNode, rightComponentNode);
      compareArguments(prefix, leftComponentNode, rightComponentNode);
    }
  }


  private void markDifferent(DefaultMutableTreeNode node) {
    setDifferent(node);
    Enumeration children = node.depthFirstEnumeration();
    while (children.hasMoreElements())
      setDifferent((DefaultMutableTreeNode)children.nextElement());
  }

  /**
   * Return the names of the components and a mapping from component name
   * to the component tree node.
   * @param children DefaultMutableTreeNodes which are components
   * @param names list of component names
   * @param nameToNode mapping of component name to component XML node
   */
  private void getComponents(Enumeration children, ArrayList names, Hashtable nameToNode) {
    while (children.hasMoreElements()) {
      DefaultMutableTreeNode child =
        (DefaultMutableTreeNode)children.nextElement();
      String componentName = child.toString();
      names.add(componentName);
      nameToNode.put(componentName, child);
    }
  }

  /**
   * Check that components have the same attributes.
   * @param leftComponentNode component from the left tree
   * @param rightComponentNode component with same name from the right tree
   */
  private void compareAttributes(String prefix,
                                 DefaultMutableTreeNode leftComponentNode,
                                 DefaultMutableTreeNode rightComponentNode) {
    ArrayList leftAttributes = getAttributes(leftComponentNode);
    ArrayList rightAttributes = getAttributes(rightComponentNode);
    for (int i = 0; i < leftAttributes.size(); i++) {
      String leftAttr = (String)leftAttributes.get(i);
      if (!rightAttributes.contains(leftAttr)) {
//        System.out.println("In left tree only: " + leftAttr);
        setDifferent((DefaultMutableTreeNode)allNamesToNodes.get("left-" + prefix + "-" + leftAttr));
      }
    }
    for (int i = 0; i < rightAttributes.size(); i++) {
      String rightAttr = (String)rightAttributes.get(i);
      if (!leftAttributes.contains(rightAttr)) {
//        System.out.println("In right tree only: " + rightAttr);
        setDifferent((DefaultMutableTreeNode)allNamesToNodes.get("right-" + prefix + "-" + rightAttr));
      }
    }
  }

  private ArrayList getAttributes(DefaultMutableTreeNode componentNode) {
    Enumeration attributeNodes = null;
    Enumeration children = componentNode.children();
    while (children.hasMoreElements()) {
      DefaultMutableTreeNode child =
        (DefaultMutableTreeNode)children.nextElement();
      if (child.toString().equals("attributes")) {
        attributeNodes = child.children();
        break;
      }
    }
    ArrayList attributes = new ArrayList();
    if (attributeNodes == null)
      return attributes;
    while (attributeNodes.hasMoreElements()) {
      DefaultMutableTreeNode attrNode =
        (DefaultMutableTreeNode)attributeNodes.nextElement();
      UserObject obj = (UserObject)attrNode.getUserObject();
      attributes.add(obj.getName());
    }
    return attributes;
  }

  /**
   * Check that components have the same arguments.
   * @param leftComponentNode component from the left tree
   * @param rightComponentNode component with same name from the right tree
   */
  private void compareArguments(String prefix,
                                DefaultMutableTreeNode leftComponentNode,
                                DefaultMutableTreeNode rightComponentNode) {
    ArrayList leftArguments = getArguments(leftComponentNode);
    ArrayList rightArguments = getArguments(rightComponentNode);
    for (int i = 0; i < leftArguments.size(); i++) {
      String leftArg = (String)leftArguments.get(i);
      if (!rightArguments.contains(leftArg)) {
//        System.out.println("In left tree only: " + leftArg);
        setDifferent((DefaultMutableTreeNode)allNamesToNodes.get("left-" + prefix + "-" + leftArg));
      }
    }
    for (int i = 0; i < rightArguments.size(); i++) {
      String rightArg = (String)rightArguments.get(i);
      if (!leftArguments.contains(rightArg)) {
//        System.out.println("In right tree only: " + rightArg);
        setDifferent((DefaultMutableTreeNode)allNamesToNodes.get("right-" + prefix + "-" + rightArg));
      }
    }
  }

  private ArrayList getArguments(DefaultMutableTreeNode componentNode) {
    Enumeration argumentNodes = null;
    Enumeration children = componentNode.children();
    while (children.hasMoreElements()) {
      DefaultMutableTreeNode child =
        (DefaultMutableTreeNode)children.nextElement();
      if (child.toString().equals("arguments")) {
        argumentNodes = child.children();
        break;
      }
    }
    ArrayList arguments = new ArrayList();
    if (argumentNodes == null)
      return arguments;
    while (argumentNodes.hasMoreElements()) {
      DefaultMutableTreeNode argNode =
        (DefaultMutableTreeNode)argumentNodes.nextElement();
      UserObject obj = (UserObject)argNode.getUserObject();
      arguments.add(obj.getName());
    }
    return arguments;
  }


  public static void main(String[] args) {
    XMLDiff xmlDiff = new XMLDiff();
  }

  // the user object encapsulated in a tree node
  class UserObject {
    String name;
    boolean different;

    public UserObject(String name) {
      this.name = name;
      different = false;
    }

    public String getName() {
      return name;
    }

    public boolean isDifferent() {
      return different;
    }

    public void setDifferent(boolean diff) {
      different = diff;
    }

    /**
     * Returns the string to display for this node.
     * @return
     */
    public String toString() {
      return name;
    }
  }

    /**
   * Custom tree cell renderer.
   * Display nodes which are the same in black and which are
   * different (i.e. in one tree but not the other) in gray.
   */
  class MyTreeCellRenderer extends DefaultTreeCellRenderer {
    public Component getTreeCellRendererComponent(JTree tree,
                                                  Object value,
                                                  boolean sel,
                                                  boolean expanded,
                                                  boolean leaf,
                                                  int row,
                                                  boolean hasFocus) {
      Component c =
        super.getTreeCellRendererComponent(tree, value, sel,
                                           expanded, leaf, row, hasFocus);
      Font f = c.getFont();
      UserObject userObject =
        (UserObject)((DefaultMutableTreeNode)value).getUserObject();
      if (!userObject.isDifferent()) {
        c.setForeground(Color.black);
        c.setFont(new Font(f.getName(), Font.BOLD, f.getSize()));
      } else {
        c.setForeground(Color.gray);
        c.setFont(new Font(f.getName(), Font.PLAIN, f.getSize()));
      }
      return c;
    }
  }
}
