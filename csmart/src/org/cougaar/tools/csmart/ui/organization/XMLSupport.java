package org.cougaar.tools.csmart.ui.organization;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import org.w3c.dom.*;
import org.apache.xerces.parsers.DOMParser;
import org.xml.sax.InputSource;
import org.cougaar.util.log.Logger;

import java.io.*;

/**
 * Provides support for reading XML files that contain society definitions.
 */
public class XMLSupport implements SocietySupport {
  private int nAgents = 0;
  private Document doc = null;
  private String societyName;
  private Model model;
  private transient Logger log;

  /**
   * Create support object for reading XML files.
   * @param model model that drives this application
   */
  public XMLSupport(Model model) {
    this.model = model;
    log = LoggerSupport.createLogger(this.getClass().getName());
  }

  /**
   * Read an XML file and return a tree representing the society in it.
   * @param filename XML file to read
   * @return tree representing society or null
   */
  public JTree readFile(String filename) {
    if (filename.endsWith(".xml"))
      societyName = filename.substring(0, filename.length()-4);
    int lastIndex = societyName.lastIndexOf("/");
    if (lastIndex != -1)
      societyName = societyName.substring(lastIndex+1);
    doc = loadXMLFile(filename);
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
    if (hostNode == null)
      return null;
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

  /**
   * Taken from csmart.util.XMLUtils so this class can be
   * independent of csmart.
   * @param filename name of file to read
   * @return  XML Document or null
   */

  private Document loadXMLFile(String filename) {
    if (filename == null || filename.equals(""))
      return null;
    try {
      DOMParser parser = new DOMParser();
      InputStream is = null;
      try {
        is = new FileInputStream(filename);
      } catch (IOException ioe) {
        if (log.isWarnEnabled()) {
          log.warn("Could not open " + filename + " for reading: " + ioe);
        }
        return null;
      }
      parser.parse(new InputSource(is));
      return parser.getDocument();
    } catch (org.xml.sax.SAXParseException spe) {
      if (log.isErrorEnabled()) {
        log.error("Parse exception Parsing file: " + filename, spe);
      }
    } catch (org.xml.sax.SAXException se) {
      if (log.isErrorEnabled()) {
        log.error("SAX exception Parsing file: " + filename, se);
      }
    } catch (Exception e) {
      if (log.isErrorEnabled()) {
        log.error("Exception Parsing file: " + filename, e);
      }
    }
    return null;
  }

  /**
   * Get the name of the society.
   * @return society name
   */
  public String getSocietyName() {
    return societyName;
  }

  /**
   * Get the type of the society.
   * @return SOCIETY_FROM_XML
   */
  public int getType() {
    return Model.SOCIETY_FROM_XML;
  }

  /**
   * Get the file extension for the files supported by this class.
   * @return xml
   */
  public String getFileExtension() {
    return "xml";
  }

  /**
   * Get a description of the files supported by this class.
   * @return XML
   */
  public String getFileTitle() {
    return "XML";
  }

  /**
   * Get the number of agents.
   * @return number of agents
   */
  public int updateAgentCount() {
    return nAgents;
  }

  /**
   * Get the XML document.
   * @return XML document
   */
  public Document getDocument() {
    return doc;
  }

  /**
   * Save the XML file. Does nothing because you can't modify XML files.
   * @param file ignored; just needed to match interface
   * @return always returns true
   */
  public boolean saveFile(File file) {
    // does nothing because this can't be modified
    return true;
  }
}
