/*
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 *
 * © Copyright 2000, 2001 BBNT Solutions LLC
 * </copyright>
 */

package org.cougaar.tools.csmart.ui.monitor.generic;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.File;
import java.lang.reflect.Constructor;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.*;

import att.grappa.*;

import org.cougaar.tools.csmart.ui.monitor.PropertyNames;
import org.cougaar.tools.csmart.ui.util.NamedFrame;
import org.cougaar.tools.csmart.ui.Browser;
import java.net.URL;

/**
 * Frame for displaying graphs.
 */

public class CSMARTFrame extends JFrame implements ActionListener
{
  public static final String FILE_MENU = "File";
  protected static final String NEW_WITH_FILTER_MENU_ITEM = "New With Filter...";
  protected static final String NEW_WITH_SELECTION_MENU_ITEM = "New With Selection";
  protected static final String CLOSE_MENU_ITEM = "Close";
  protected static final String SAVE_MENU_ITEM = "Save";
  protected static final String SAVE_AS_MENU_ITEM = "Save As...";
  protected static final String PRINT_MENU_ITEM = "Print...";

  public static final String SELECT_MENU = "Select";
  protected static final String FIND_MENU_ITEM = "Find...";
  protected static final String FILTER_MENU_ITEM = "Filter...";
  protected static final String CLEAR_MENU_ITEM = "Clear";
  protected static final String SELECT_ALL_MENU_ITEM = "Select All";

  public static final String SHOW_MENU = "Show";

  public static final String VIEW_MENU = "View";
  protected static final String OVERVIEW_MENU_ITEM = "Overview";
  protected static final String ATTRIBUTES_MENU_ITEM = "Attributes";
  protected static final String ZOOM_IN_MENU_ITEM = "Zoom In";
  protected static final String ZOOM_OUT_MENU_ITEM = "Zoom Out";
  protected static final String ZOOM_RESET_MENU_ITEM = "Zoom Reset";
  protected static final String SCROLL_TO_SELECTED_MENU_ITEM = "Scroll to Selection";

  protected static final String HELP_MENU = "Help";
  protected static final String HELP_DOC = "help.html";
  protected static final String ABOUT_CSMART_ITEM = "About CSMART";
  protected static final String ABOUT_DOC = "../../help/about-csmart.html";
  protected static final String HELP_MENU_ITEM = "Help";

  protected static final String SEPARATOR = "Separator";

  private String[] fileMenuItems = { 
    NEW_WITH_SELECTION_MENU_ITEM, SEPARATOR,
    SAVE_MENU_ITEM, SAVE_AS_MENU_ITEM,
    PRINT_MENU_ITEM, SEPARATOR, 
    CLOSE_MENU_ITEM
  };

  private String[] selectMenuItems = { 
    FIND_MENU_ITEM, FILTER_MENU_ITEM, 
    SELECT_ALL_MENU_ITEM, CLEAR_MENU_ITEM
  };

  private String[] showMenuItems = {
    CLEAR_MENU_ITEM
  };

  private String[] viewMenuItems = { 
    OVERVIEW_MENU_ITEM, ATTRIBUTES_MENU_ITEM, 
    SEPARATOR,
    ZOOM_IN_MENU_ITEM, ZOOM_OUT_MENU_ITEM, ZOOM_RESET_MENU_ITEM, SEPARATOR,
    SCROLL_TO_SELECTED_MENU_ITEM
 };

  private String[] helpMenuItems = {
    HELP_MENU_ITEM, ABOUT_CSMART_ITEM
  };

  protected CSMARTGraph graph; // the graph displayed in this frame
  private Vector associatedFrames; // overview frame and any attribute frames
  private String title;
  private JFrame overviewFrame = null;
  private JViewport viewport;
  private GrappaPanel gp;
  private JScrollPane jsp;
  private JMenuBar menuBar;

  private JMenu fileMenu;
  private JMenu newMenu;
  private JMenu selectMenu;
  protected JMenu showMenu;
  private JMenu viewMenu;

  // menu items that are enabled/disabled depending on state of graph
  private JMenuItem newWithSelectionMenuItem;
  private JMenuItem saveMenuItem;
  private JMenuItem saveAsMenuItem;
  private JMenuItem closeMenuItem;
  private JMenuItem printMenuItem;
  private JMenuItem attributesMenuItem;
  private JMenuItem scrollToSelectedMenuItem;
  private JMenuItem selectClearMenuItem;
  private JMenuItem causalMenuItem;

  private Hashtable nodeToAttributeFrame; // keep track of attribute frames being shown

  private int sliderResult;    // result from slider dialog
  private final JFrame myFrame;

  /**
   * Create a new frame with the specified graph.
   * @param title   title for this frame
   * @param graph   graph to include in this frame
   */

  public CSMARTFrame(String title, CSMARTGraph graph) {
    super(title);
    //    this.title = Util.getNamedFrame().addFrame(title, this);
    // guarantee unique name, this may change title
    NamedFrame.getNamedFrame().addFrame(title, this);
    // save my frame so that it can be removed in window listener
    myFrame = this;
    // save new title for use in naming associated overview frame
    this.title = getTitle();
    this.graph = graph;
    init();
    enableGraphMenus(true);
    enableSelectedMenus(false);
    saveMenuItem.setEnabled(graph.hasOutputFile());
    customize();
    // keep track of attributes being displayed
    nodeToAttributeFrame = new Hashtable();
    show();
    //    System.out.println("CSMARTFrame: Displaying graph at: " +
    //		       System.currentTimeMillis()/1000);
  }

  /**
   * Override to customize the frame in which the graph resides.
   * Add custom menu items and handlers, etc.
   */

  protected void customize() {
  }

  /**
   * Override to provide table model for displaying node's attributes.
   * @param Node node for which to display attributes
   */

  protected TableModel getAttributeTableModel(Node node) {
    return null;
  }

  /**
   * Initialize this frame and default menus.
   * Add a window listener to
   * close associated overview & attribute windows & dispose window.
   * when user clicks on window close icon, i.e. this equates
   * the window close icon with the CLOSE menu item.
   */

  private void init() {
    Grappa.setToolTipText("");
    associatedFrames = new Vector();
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
	for (int i = 0; i < associatedFrames.size(); i++)
	  ((JFrame)associatedFrames.elementAt(i)).dispose();
	associatedFrames.removeAllElements();
	overviewFrame = null;
	NamedFrame.getNamedFrame().removeFrame(myFrame);
	e.getWindow().dispose();
      }
    });

    setSize(600,400);
    setLocation(100,100);

    if (graph != null)
      addGraph(graph);

    menuBar = new JMenuBar();
    getRootPane().setJMenuBar(menuBar);
    fileMenu = makeMenu(FILE_MENU, fileMenuItems);
    for (int i = 0; i < fileMenu.getMenuComponentCount(); i++) {
      Component menuComponent = fileMenu.getMenuComponent(i);
      if (!(menuComponent instanceof JMenuItem))
	continue;
      JMenuItem menuItem = (JMenuItem)menuComponent;
      String s = menuItem.getText();
      if (s.equals(NEW_WITH_SELECTION_MENU_ITEM))
	newWithSelectionMenuItem = menuItem;
      if (s.equals(SAVE_MENU_ITEM))
	saveMenuItem = menuItem;
      else if (s.equals(SAVE_AS_MENU_ITEM))
	saveAsMenuItem = menuItem;
      else if (s.equals(CLOSE_MENU_ITEM))
	closeMenuItem = menuItem;
      else if (s.equals(PRINT_MENU_ITEM))
	printMenuItem = menuItem;
    }
    selectMenu = makeMenu(SELECT_MENU, selectMenuItems);
    // set selectClearMenuItem
    for (int i = 0; i < selectMenu.getMenuComponentCount(); i++) {
      Component menuComponent = selectMenu.getMenuComponent(i);
      if (!(menuComponent instanceof JMenuItem))
	continue;
      JMenuItem menuItem = (JMenuItem)menuComponent;
      String s = menuItem.getText();
      if (s.equals(CLEAR_MENU_ITEM))
	selectClearMenuItem = menuItem;
    }
    showMenu = makeMenu(SHOW_MENU, showMenuItems);
    viewMenu = makeMenu(VIEW_MENU, viewMenuItems);
    // set scrollToSelectedMenuItem, attributesMenuItem, and causalMenuItem
    for (int i = 0; i < viewMenu.getMenuComponentCount(); i++) {
      Component menuComponent = viewMenu.getMenuComponent(i);
      if (!(menuComponent instanceof JMenuItem))
	continue;
      JMenuItem menuItem = (JMenuItem)menuComponent;
      String s = menuItem.getText();
      if (s.equals(SCROLL_TO_SELECTED_MENU_ITEM))
	scrollToSelectedMenuItem = menuItem;
      else if (s.equals(ATTRIBUTES_MENU_ITEM))
	attributesMenuItem = menuItem;
    }
    makeMenu(HELP_MENU, helpMenuItems);
  }

  /**
   * Add a graph to this frame.
   * @param graph the graph to add
   */

  private void addGraph(CSMARTGraph graph) {
    this.graph = graph;
    jsp = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
			  JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
    //    jsp.getViewport().setBackingStoreEnabled(true);
    jsp.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);

    gp = new CSMARTGrappaPanel(graph, this);
    gp.addGrappaListener(new CSMARTGrappaAdapter(null, this));
    gp.setScaleToFit(false);
    // set empty tool tip text when in panel, but outside graph
    // note this displays a small dot, but that's Swing's fault
    gp.setToolTipText("");
    getContentPane().add("Center", jsp);
    setVisible(true);
    jsp.setViewportView(gp);
    viewport = jsp.getViewport();
  }

  /**
   * Make a menu from a menu name and the menu item names.
   * @param menuName       name of the menu
   * @param menuItemNames  names of items to add to menu
   * @return               the menu
   */

  private JMenu makeMenu(String menuName, String[] menuItemNames) {
    JMenu menu = new JMenu(menuName);
    if (menuItemNames != null) {
      for (int i = 0; i < menuItemNames.length; i++) {
	if (menuItemNames[i].equals(SEPARATOR))
	  menu.addSeparator();
	else {
	  JMenuItem menuItem = new JMenuItem(menuItemNames[i]);
	  menuItem.addActionListener(this);
	  menu.add(menuItem);
	}
      }
    } else
      menu.addActionListener(this);
    menuBar.add(menu);
    return menu;
  }

  /**
   * Returns a single selected node.
   * If more than one node is selected, it returns the first node.
   * @return     a single selected node
   */

  private Node getSelectedNode() {
    Vector selectedElements = graph.getSelectedElements();
    if (selectedElements == null)
      return null;
    for (int i = 0; i < selectedElements.size(); i++) {
      Element selectedElement = (Element)selectedElements.elementAt(i);
      if (selectedElement instanceof Node)
	return (Node)selectedElement;
    }
    return null;
  }

  /**
   * Query the user for a node name and select that node.
   * Gets the names of all the nodes and displays them for user to choose.
   */

  private void findNode() {
    Vector names = graph.getNames();
    Collections.sort(names);
    String message = "Select";
    JComboBox jc = new JComboBox(names);
    Object[] objects = { message, jc };
    int result = 
      JOptionPane.showOptionDialog(this, objects,
				   "Select",
				   JOptionPane.OK_CANCEL_OPTION,
				   JOptionPane.PLAIN_MESSAGE,
				   null, null, null);
    if (result == JOptionPane.CANCEL_OPTION)
      return; // user cancelled, do nothing
    Vector selectedNode = new Vector();
    String nodeName = (String)jc.getSelectedItem();
    selectedNode.addElement(graph.findNodeByName(nodeName));
    select(selectedNode);
    scrollToSelected(); // make chosen node visible in viewport
  }

  /**
   * Process actions; processes menu selections for menus
   * defined in this frame.
   * Override to provide custom processing for custom frames.
   * @param evt  the ActionEvent received
   */

  public void actionPerformed(ActionEvent evt) {
    String command = ((JMenuItem)evt.getSource()).getText();

    if (command.equals(FIND_MENU_ITEM)) {
      findNode();
      return;
    }

    if (command.equals(CLEAR_MENU_ITEM)) {
      if (evt.getSource() instanceof JComponent) {
	JComponent component = (JComponent)evt.getSource();
	if (component.getParent() instanceof JPopupMenu) {
	  JPopupMenu parentMenu = (JPopupMenu)component.getParent();
	  JMenu menu = (JMenu)parentMenu.getInvoker();
	  if (menu.getText().equals(SHOW_MENU)) {
	    graph.resetColors();
	  } else {
	    graph.clearSelection();
	    graph.resetColors();
	    enableSelectedMenus(false);
	  }
	}
      }
      return;
    }

    if (command.equals(SELECT_ALL_MENU_ITEM)) {
      graph.selectAll();
      enableSelectedMenus(true);
      return;
    }

    if (command.equals(OVERVIEW_MENU_ITEM)) {
      displayOverview();
      return;
    }

    if (command.equals(ATTRIBUTES_MENU_ITEM)) {
      Node selectedNode = getSelectedNode();
      if (selectedNode != null)
	displayAttributes(selectedNode);
      return;
    }

    if (command.equals(CLOSE_MENU_ITEM)) {
      //      Util.getNamedFrame().removeFrame(this);
      NamedFrame.getNamedFrame().removeFrame(this);
      // always get rid of overview and attribute frames
      for (int i = 0; i < associatedFrames.size(); i++)
	((JFrame)associatedFrames.elementAt(i)).dispose();
      associatedFrames.removeAllElements();
      overviewFrame = null;
      this.dispose();
      return;
    }

    // save graph into file it was read from
    if (command.equals(SAVE_MENU_ITEM)) {
      graph.saveGraph();
      return;
    }

    if (command.equals(SAVE_AS_MENU_ITEM)) {
      graph.saveAsGraph();
      return;
    }

    if (command.equals(ZOOM_IN_MENU_ITEM)) {
      gp.multiplyScaleFactor(1.25);
      gp.repaint();
      return;
    }

    if (command.equals(ZOOM_OUT_MENU_ITEM)) {
      gp.multiplyScaleFactor(0.8);
      gp.repaint();
      return;
    }

    if (command.equals(ZOOM_RESET_MENU_ITEM)) {
      gp.resetZoom();
      gp.repaint();
      return;
    }

    if (command.equals(SCROLL_TO_SELECTED_MENU_ITEM)) {
      scrollToSelected();
      return;
    }

    if (command.equals(PRINT_MENU_ITEM)) {
      // print grappa panel
      //      ComponentPrinter printer = new ComponentPrinter(gp, this.title);
      ComponentPrinter printer = new ComponentPrinter(viewport, this.title);
      printer.printPages();
      return;
    }

    if (command.equals(ABOUT_CSMART_ITEM)) {
      URL about = (URL)getClass().getResource(ABOUT_DOC);
      if (about != null)
	Browser.setPage(about);
      return;
    }

    if (command.equals(HELP_MENU_ITEM)) {
      //      System.err.println("Try to open " + HELP_DOC);
      URL help = (URL)getClass().getResource(HELP_DOC);
      if (help != null)
	Browser.setPage(help);
      return;
    }

    JOptionPane.showMessageDialog(this, "Not implemented: " + command);
  }

  /**
   * Select the specified nodes and enable menus and menu items
   * that rely on having selected nodes.
   * @param nodes vector of nodes to select; vector of att.grappa.Node
   */

  public void select(Vector nodes) {
    graph.select(nodes);
    if (nodes != null)
      enableSelectedMenus(true);
  }

  /**
   * Get the viewport containing the graph. Used to co-ordinate views across
   * frames.
   * @return         the viewport containing the graph
   */

  public JViewport getViewport() {
    return viewport;
  }

  /**
   * Display a modal dialog with slider.
   * @param lowEnd                 the low end value of the slider
   * @param highEnd                the high end value of the slider
   * @param dialogLabel            the title on the dialog 
   * @param dialogHeaderLabel      a prompt for the user
   * @param sliderLabel            a label on the slider
   * @return              the value selected by the user; -1 implies cancelled
   */

  public int displaySlider(int lowEnd, int highEnd,
			   String dialogLabel, 
			   String dialogHeaderLabel, 
			   String sliderLabel) {
    final JLabel label = new JLabel(sliderLabel, JLabel.CENTER);
    sliderResult = (highEnd - lowEnd)/2;
    final JSlider slider = new JSlider(lowEnd, highEnd, sliderResult);
    final JTextField tf = new JTextField(String.valueOf(sliderResult));
    tf.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	String s = ((JTextField)e.getSource()).getText();
	int value = 0;
	try {
	  value = Integer.parseInt(s);
	} catch (NumberFormatException nfe) {
	  return; // don't reset slider, if not valid number
	}
	slider.setValue(value);
      }
    });
    slider.setPaintLabels(true);
    Hashtable labelTable = new Hashtable();
    labelTable.put(new Integer(lowEnd),
 		   new JLabel(Integer.toString(lowEnd), JLabel.CENTER));
    labelTable.put(new Integer(highEnd),
 		   new JLabel(Integer.toString(highEnd), JLabel.CENTER));
    slider.setLabelTable(labelTable);
    slider.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
	JSlider s = (JSlider)e.getSource();
	sliderResult = s.getValue();
	tf.setText(String.valueOf(sliderResult));
      }
    });
    JPanel sliderPanel = new JPanel(new BorderLayout());
    sliderPanel.add(slider, BorderLayout.NORTH);
    JPanel textPanel = new JPanel();
    textPanel.add(label);
    textPanel.add(tf);
    sliderPanel.add(textPanel, BorderLayout.SOUTH);

    final JDialog dialog = new JDialog(this, dialogLabel, true);

    JPanel buttonPanel = new JPanel();
    JButton OKButton = new JButton("OK");
    OKButton.setFocusPainted(false);
    OKButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	dialog.dispose();
      }
    });
    buttonPanel.add(OKButton);
    JButton cancelButton = new JButton("Cancel");
    cancelButton.setFocusPainted(false);
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	sliderResult = -1;
	dialog.dispose();
      }
    });
    buttonPanel.add(cancelButton);

    Container c = dialog.getContentPane();
    c.setLayout(new BorderLayout());
    JLabel headerLabel = new JLabel(dialogHeaderLabel, JLabel.CENTER);
    headerLabel.setBorder(new EmptyBorder(10, 0, 10, 0));
    c.add(headerLabel, BorderLayout.NORTH);
    c.add(sliderPanel, BorderLayout.CENTER);
    c.add(buttonPanel, BorderLayout.SOUTH);
    dialog.setSize(300, 160);
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    dialog.setLocation((screenSize.width - 300)/2,
		       (screenSize.height - 160)/2);
    dialog.show();
    return sliderResult;
  }

  /**
   * Display attributes of the specified node in a JTable in a new frame.
   * Uses the table model returned by getAttributeTableModel.
   * @param node the node for which to display attributes
   */

  public void displayAttributes(Node node) {
    // if attributes are being displayed, just pop that window to the front
    JFrame existingFrame = (JFrame)nodeToAttributeFrame.get(node);
    if (existingFrame != null) {
      existingFrame.toFront();
      return;
    }
    TableModel attributeTableModel = getAttributeTableModel(node);
    if (attributeTableModel == null)
      return;
    String title = (String)node.getAttributeValue(PropertyNames.TABLE_TITLE);
    if (title == null)
      title = node.getName();
    final JFrame attributeFrame = new JFrame(title);
    final Node thisNode = node;
    nodeToAttributeFrame.put(node, attributeFrame);
    attributeFrame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
	nodeToAttributeFrame.remove(thisNode);
	e.getWindow().dispose();
      }
    });
    final Node attributeNode = node;
    JTable table = new JTable(getAttributeTableModel(node));
    initColumnSizes(table);
    JScrollPane scrollpane = new JScrollPane(table);
    JPanel buttonPanel = new JPanel();
    JButton findButton = new JButton("Find");
    findButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	scrollToNode(attributeNode);
      }
    });
    JButton closeButton = new JButton("Close");
    closeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	nodeToAttributeFrame.remove(thisNode);
	attributeFrame.dispose();
      }
    });
    buttonPanel.add(findButton);
    buttonPanel.add(closeButton);
    attributeFrame.getContentPane().add("Center", scrollpane);
    attributeFrame.getContentPane().add("South", buttonPanel);
    attributeFrame.setSize(400, 400);
    attributeFrame.show();
    associatedFrames.addElement(attributeFrame);
  }

  /**
   * Set column sizes for attribute table.
   */

  private void initColumnSizes(JTable table) {
    int w = 100; // desired width for first column
    TableColumn column = table.getColumnModel().getColumn(0);
    column.setPreferredWidth(w);
    int nColumns = table.getModel().getColumnCount();
    w = 200;  // desired width for remaining columns
    for (int i = 1; i < nColumns; i++) {
     column = table.getColumnModel().getColumn(i);
     column.setPreferredWidth(w);
    }
    table.revalidate();
  }


  /**
   * Display attributes for the selected node.
   * Called by CSMARTGrappaPanel to display attributes on double click.
   */

  public void displaySelectedNodeAttributes() {
    Node selectedNode = getSelectedNode();
    if (selectedNode != null)
      displayAttributes(selectedNode);
  }

  /**
   * Display the bird's eye view (or overview) frame.
   */

  private void displayOverview() {
    if (overviewFrame == null) {
      overviewFrame = new BirdFrame("Overview: " + title, 
				    graph, this); // bird's eye view
      associatedFrames.addElement(overviewFrame);
    } else {
      overviewFrame.setVisible(true);
    }
  }

  /**
   * Create a new graph containing only the selected nodes and edges.
   * @return   the new graph
   */

  public CSMARTGraph newGraphFromSelection() {
    if (graph == null) {
      System.out.println("WARNING: graph is null");
      return null;
    }
    // create a copy of the original graph, both nodes and edges
    CSMARTGraph subgraph = new CSMARTGraph(graph, "Selected Nodes");
    // clear selection in new graph
    subgraph.clearSelection();
    // get the selected nodes and edges from the original graph
    Vector elements = graph.getSelectedElements();
    if (elements == null) {
      System.out.println("No elements selected");
      return null;
    }
    // get the names of the selected nodes
    Vector nodeNames = new Vector(elements.size());
    for (int i = 0; i < elements.size(); i++) {
      Element element = (Element)elements.elementAt(i);
      if (element instanceof Node)
	nodeNames.addElement(((Node)element).getName());
    }
    // keep only the selected nodes
    subgraph.keepNamedNodes(nodeNames);
    return subgraph;
  }

  /**
   * Enable/disable menu items that are used when the event frame
   * contains a graph.
   * Can be overridden to enable/disable additional customized menu items.
   * @param enable true to enable menu items; false to disable
   */

  protected void enableGraphMenus(boolean enable) {
    selectMenu.setEnabled(enable);
    showMenu.setEnabled(enable);
    viewMenu.setEnabled(enable);
    saveMenuItem.setEnabled(enable);
    saveAsMenuItem.setEnabled(enable);
    closeMenuItem.setEnabled(enable);
    printMenuItem.setEnabled(enable);
    if (!enable)
      newWithSelectionMenuItem.setEnabled(false);
  }

  /**
   * Enable/disable menu items that are used when the event graph
   * has selected nodes.
   * Can be overridden to enable/disable additional customized menu items.
   * @param enable true to enable menu items; false to disable
   */

  protected void enableSelectedMenus(boolean enable) {
    newWithSelectionMenuItem.setEnabled(enable);
    attributesMenuItem.setEnabled(enable);
    scrollToSelectedMenuItem.setEnabled(enable);
    selectClearMenuItem.setEnabled(enable);
    showMenu.setEnabled(enable);
  }


  // Ray's original code
//   public void scrollToSelected() {
//     Node node = getSelectedNode();
//     if (node == null) {
//       System.out.println("no selected node");
//       return;
//     }
//     Rectangle viewRect = viewport.getViewRect();
//     Rectangle r = getSelectedNode().getGrappaNexus().getBounds();
//     AffineTransform transform = gp.getTransform();
//     double[] pts = new double[] { 
//       r.getX(), r.getY(), 
// 	r.getX() + r.getWidth(), r.getY() + r.getHeight()
// 	};
//
//     transform.transform(pts, 0, pts, 0, 2);
//     r = new Rectangle((int)pts[0], (int)pts[1], 
// 		      (int)(pts[2] - pts[0]), 
// 		      (int)(pts[3] - pts[1]));
//     r.grow(20, 20);
//     Point p = gp.getLocation();
//     r.x += p.x;
//     r.y += p.y;
//     viewport.scrollRectToVisible(r);
//   }


  /**
   * Scroll to make the selected node visible.
   */

  public void scrollToSelected() {
    Node node = getSelectedNode();
    if (node == null) {
      System.out.println("no selected node");
      return;
    }
    Rectangle r = getSelectedNode().getGrappaNexus().getBounds();
    double[] pts = new double[] { 
      r.getX(), r.getY(), 
 	r.getX() + r.getWidth(), r.getY() + r.getHeight()
 	};
    scrollWorker(pts);
  }

  /** Scroll the viewport containing the graph so that the specified
   * node is visible.
   * @param node node to make visible
   */

  private void scrollToNode(Node node) {
    // get bounds of node to display
    Rectangle r = node.getGrappaNexus().getBounds();
    double[] pts = new double[] { 
      r.getX(), r.getY(), 
 	r.getX() + r.getWidth(), r.getY() + r.getHeight()
 	};
    scrollWorker(pts);
  }

  /**
   * Scroll to make the specified points visible.
   * @param array of four points defining the bounds of a node
   */

  private void scrollWorker(double[] pts) {
    // get transform from grappa panel that's displaying the node
    AffineTransform transform = gp.getTransform();
    transform.transform(pts, 0, pts, 0, 2);
    Rectangle r = new Rectangle((int)pts[0], (int)pts[1], 
				(int)(pts[2] - pts[0]), 
				(int)(pts[3] - pts[1]));
    // expand the rectangle around the node to guarantee it's displayed
    r.grow(20, 20);
    // offset by grappa panel location
    Point p = gp.getLocation();
    r.x += p.x;
    r.y += p.y;
    // don't allow scrolling off the left edge
    if (r.x < gp.getX())
      r.x = gp.getX();
    // and don't allow scrolling off the right edge
    if ((r.x + r.width) > gp.getWidth())
      r.width = gp.getWidth() - r.x;
    // scroll so that node is visible
    viewport.scrollRectToVisible(r);
  }

  /**
   * Called by EventGrappaAdapter on BirdFrame (the overview frame)
   * to scroll this to the outline drawn in the corresponding BirdFrame.
   * @param box the box in this frame to make visible
   */

  public void scrollToOutline(GrappaBox box) {
    double[] pts = new double[] { 
      box.getX(), box.getY(), 
	box.getX() + box.getWidth(), box.getY() + box.getHeight()
	};
    scrollWorker(pts);
  }

  /**
   * Insert a menu item into a menu; add the specified action listener
   * to the menu item.
   * Used by specializations of this frame to add customized menu items.
   * @param menuName     the name of a menu which must exist in this frame
   * @param menuItemName name of a menu item to create
   * @param pos          the position after which to insert this menu item
   * @param listener     the listener to register on the menu item
   * @return JMenuItem   the new menu item or null
   */

  protected JMenuItem insertMenuItem(String menuName, String menuItemName,
				     int pos, ActionListener listener) {
    int nMenus = menuBar.getMenuCount();
    for (int i = 0; i < nMenus; i++) {
      JMenu menu = menuBar.getMenu(i);
      if (menu.getText().equals(menuName)) {
	JMenuItem menuItem = new JMenuItem(menuItemName);
	menuItem.addActionListener(listener);
	menu.insert(menuItem, pos);
	return menuItem;
      } 
    }
    return null;
  }

  protected void insertMenu(String menuName, JMenu newMenu, int pos) {
    int nMenus = menuBar.getMenuCount();
    for (int i = 0; i < nMenus; i++) {
      JMenu menu = menuBar.getMenu(i);
      if (menu.getText().equals(menuName)) {
	menu.insert(newMenu, pos);
      } 
    }
  }

  /**
   * Insert a menu separator.
   * Used by specializations of this frame to add customized menu items.
   * @param menuName     the name of a menu which must exist in this frame
   * @param pos          the position after which to insert this menu item
   */

  protected void insertMenuSeparator(String menuName, int pos) {
    int nMenus = menuBar.getMenuCount();
    for (int i = 0; i < nMenus; i++) {
      JMenu menu = menuBar.getMenu(i);
      if (menu.getText().equals(menuName)) {
	menu.insertSeparator(pos);
	return;
      }
    }
  }

}



