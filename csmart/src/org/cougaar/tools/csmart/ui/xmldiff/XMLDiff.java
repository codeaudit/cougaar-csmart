package org.cougaar.tools.csmart.ui.xmldiff;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;
import org.apache.xerces.parsers.DOMParser;
import org.xml.sax.InputSource;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Collections;
import java.util.Enumeration;

/**
 * Compares XML files that define societies.
 * Ignores hosts and nodes; compares only agents.
 * Agent name suffixes (following the first, optional dot) are ignored.
 * Compares component attributes (excluding name) and
 * component arguments.
 * To run outside of cougaar environment, ensure that
 * your classpath includes xerces.jar and this class.
 */
public class XMLDiff extends JFrame {
  private JTree leftTree;
  private JTree rightTree;
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
    JFileChooser chooser = new JFileChooser(System.getProperty("org.cougaar.install.path"));
    chooser.setDialogTitle("Select XML File");
    ExtensionFileFilter extensionFilter =
      new ExtensionFileFilter("xml", "XML File (xml)");
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
    Document doc = loadXMLFile(filename);
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
                // ignore new agent name suffix
                int index = agentName.indexOf(".");
                if (index != -1)
                  agentName = agentName.substring(0, index);
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

  /**
   * Taken from csmart.util.XMLUtils so this class can be
   * independent of cougaar.
   * @param filename name of file to read
   * @return  XML Document or null
   */

  private Document loadXMLFile(String filename) {
    if (filename == null || filename.equals(""))
      return null;
    try {
      InputStream is = null;
      try {
        is = new FileInputStream(filename);
      } catch (IOException ioe) {
        System.out.println("Could not open " + filename + " for reading: " + ioe);
        return null;
      }
      if (is == null) {
        System.out.println("Could not open " + filename + " for reading.");
        return null;
      }
      DOMParser parser = new DOMParser();
      parser.parse(new InputSource(is));
      return parser.getDocument();
    } catch (org.xml.sax.SAXParseException spe) {
      System.out.println("Parse exception Parsing file: " + filename + " " + spe);
    } catch (org.xml.sax.SAXException se) {
      System.out.println("SAX exception Parsing file: " + filename + " " + se);
    } catch (Exception e) {
      System.out.println("Exception Parsing file: " + filename + " " + e);
    }
    return null;
  }

  /**
   * Return a string which uniquely identifies the component.
   * The string returned is the classname followed by
   * a parenthesized comma-separated list of the arguments.
   */
  private String getUniqueName(Node componentNode) {
    String className = "";
    NamedNodeMap attributes = componentNode.getAttributes();
    // get class name
    int n = attributes.getLength();
    for (int i = 0; i < n; i++) {
      Node node = attributes.item(i);
      String name = node.getNodeName();
      if (name.equals("class")) {
        className = node.getNodeValue();
        className = className.trim();
        break;
      }
    }
    // get argument list
    String argList = "";
    NodeList children = componentNode.getChildNodes();
    n = children.getLength();
    for (int i = 0; i < n; i++) {
      Node node = children.item(i);
      if (node.getNodeName().equals("argument")) {
        NodeList args = node.getChildNodes();
        for (int j = 0; j < args.getLength(); j++) {
          Node argNode = args.item(j);
          String arg = argNode.getNodeValue();
          arg = arg.trim();
          if (argList.length() == 0)
            argList = arg;
          else
            argList = argList + "," + arg;
        }
      }
    }
    return className + "(" + argList + ")";
  }


  private void addComponents(String prefix, Node agentNode, DefaultMutableTreeNode agentTreeNode) {
    NodeList children = agentNode.getChildNodes();
    ArrayList componentNames = new ArrayList();
    Hashtable nameToNode = new Hashtable();
    int n = children.getLength();
    for (int i = 0; i < n; i++) {
      if (children.item(i).getNodeName().equals("component")) {
        Node componentNode = children.item(i);
        // use class name  and arguments of component as the component name
        String componentName = getUniqueName(componentNode);
        componentNames.add(componentName);
        nameToNode.put(componentName, componentNode);
      }
    }
    Collections.sort(componentNames);
    for (int i = 0; i < componentNames.size(); i++) {
      String componentName = (String)componentNames.get(i);
      UserObject obj = new UserObject(componentName);
      DefaultMutableTreeNode componentTreeNode =
        new DefaultMutableTreeNode(obj, true);
      agentTreeNode.add(componentTreeNode);
      allNamesToNodes.put(prefix + "-" + componentName, componentTreeNode);
      Node componentNode = (Node)nameToNode.get(componentName);
      addAttributes(prefix + "-" + componentName, componentNode, componentTreeNode);
      addArguments(prefix + "-" + componentName, componentNode, componentTreeNode);
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
        markDifferent((DefaultMutableTreeNode)allNamesToNodes.get("left-" + leftAgentNames.get(i)));
      } else
        commonAgentNames.add(leftAgentNames.get(i));
    for (int i = 0; i < rightAgentNames.size(); i++)
      if (!leftAgentNames.contains(rightAgentNames.get(i))) {
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
   * Marks a node and all its children as different
   * (i.e. unique to its tree).
   * @param node node to mark as different
   */
  private void markDifferent(DefaultMutableTreeNode node) {
    setDifferent(node);
    Enumeration children = node.depthFirstEnumeration();
    while (children.hasMoreElements())
      setDifferent((DefaultMutableTreeNode)children.nextElement());
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
        String s = "left-" + agentName + "-" + leftComponentNames.get(i);
        markDifferent((DefaultMutableTreeNode)allNamesToNodes.get(s));
      } else
        commonComponentNames.add(leftComponentNames.get(i));
    for (int i = 0; i < rightComponentNames.size(); i++)
      if (!leftComponentNames.contains(rightComponentNames.get(i))) {
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
      if (leftAttr.startsWith("name:"))
        continue; // ignore name attribute
      if (!rightAttributes.contains(leftAttr)) {
        setDifferent((DefaultMutableTreeNode)allNamesToNodes.get("left-" + prefix + "-" + leftAttr));
      }
    }
    for (int i = 0; i < rightAttributes.size(); i++) {
      String rightAttr = (String)rightAttributes.get(i);
      if (rightAttr.startsWith("name:"))
        continue; // ignore name attribute
      if (!leftAttributes.contains(rightAttr)) {
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
        setDifferent((DefaultMutableTreeNode)allNamesToNodes.get("left-" + prefix + "-" + leftArg));
      }
    }
    for (int i = 0; i < rightArguments.size(); i++) {
      String rightArg = (String)rightArguments.get(i);
      if (!leftArguments.contains(rightArg)) {
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

  /**
   *
   * INNER CLASSES
   *
   */


  /**
   * UserObject
   * The user object that is encapsulated in a tree node.
   * It includes the name and a flag indicating whether it's different
   * (i.e. in one tree, but not the other).
   */
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
   * MyTreeCellRenderer
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

  /**
   * ExtensionFileFilter
   * A filter on the JFileChooser to display only xml files.
   * Code taken from cougaar package so this class can be independent of cougaar.
   */

  public class ExtensionFileFilter extends FileFilter {
    String filterExtension;
    String description;

    public ExtensionFileFilter(String extension, String description) {
      filterExtension = extension;
      this.description = description;
    }

    /**
     * Return true if this file should be shown in the directory pane,
     * false if it shouldn't.
     * Files that begin with "." are ignored.
     */
    public boolean accept(File f) {
      if (f != null) {
        if (f.isDirectory())
          return true;
        String extension = getExtension(f);
        if (extension != null && filterExtension.equals(extension))
          return true;
      }
      return false;
    }

    private String getExtension(File f) {
      if (f != null) {
        String filename = f.getName();
        int i = filename.lastIndexOf('.');
        if (i > 0 && i < filename.length()-1) {
          return filename.substring(i+1).toLowerCase();
        };
      }
      return null;
    }

    /**
     * Returns the human readable description of this filter.
     */
    public String getDescription() {
      return description;
    }

  }
}
