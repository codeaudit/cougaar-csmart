/*
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 *
 * © Copyright 2000, 2001 BBNT Solutions LLC
 * </copyright>
 */

package org.cougaar.tools.csmart.ui.analyzer;

import org.cougaar.tools.csmart.ui.component.ConfigurableComponent;
import org.cougaar.tools.csmart.ui.component.HostComponent;
import org.cougaar.tools.csmart.ui.component.SocietyComponent;
import org.cougaar.tools.csmart.ui.experiment.Experiment;
import org.cougaar.tools.server.CommunityServesClient;
import org.cougaar.tools.server.HostServesClient;
import org.cougaar.tools.server.rmi.ClientCommunityController;
import org.cougaar.tools.csmart.ui.util.NamedFrame;
import org.cougaar.tools.csmart.ui.viewer.CSMART;

import java.awt.GridLayout;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import javax.swing.*;
import java.util.*;

import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.tools.csmart.ui.Browser;

public class Analyzer extends JFrame implements ActionListener {
  // port for fetching files from server
  // must match port used in org.cougaar.tools.server package
  private static final int DEFAULT_PORT = 8484;
  // system property; location of Excel
  private static final String EXCEL = "excel";
  // menu items
  private static final String FILE_MENU = "File";
  private static final String OPEN_MENU_ITEM = "Open";
  private static final String RETRIEVE_MENU_ITEM = "Retrieve Metrics Files...";
  private static final String WINDOW_MENU = "Window";
  private static final String EXIT_MENU_ITEM = "Exit";
  private static final String HELP_MENU = "Help";

  protected static final String HELP_DOC = "help.html";
  protected static final String ABOUT_CSMART_ITEM = "About CSMART";
  protected static final String ABOUT_DOC = "../help/about-csmart.html";
  protected static final String HELP_MENU_ITEM = "Help";

  private String[] helpMenuItems = {
    HELP_MENU_ITEM, ABOUT_CSMART_ITEM
  };

  // tool buttons
  private static final String EXCEL_LABEL = "Excel";
  private CSMART csmart; // top level viewer, gives access to save method, etc.
  private static JFrame myFrame;
  private JMenu windowMenu;
  private Hashtable titleToFrame = new Hashtable();
  private Process process;
  private Experiment experiment;

  /**
   * Display a csv file in excel.
   */

  public Analyzer(CSMART csmart) {
    this.csmart = csmart;
    experiment = csmart.getExperiment();
    myFrame = this;
    JMenu fileMenu = new JMenu(FILE_MENU);
    JMenuItem openMenuItem = new JMenuItem(OPEN_MENU_ITEM);
    openMenuItem.addActionListener(this);
    fileMenu.add(openMenuItem);
    JMenuItem retrieveMenuItem = new JMenuItem(RETRIEVE_MENU_ITEM);
    retrieveMenuItem.addActionListener(this);
    fileMenu.add(retrieveMenuItem);
    JMenuItem exitMenuItem = new JMenuItem(EXIT_MENU_ITEM);
    exitMenuItem.addActionListener(this);
    fileMenu.add(exitMenuItem);
    JMenuBar menuBar = new JMenuBar();

    JMenu helpMenu = new JMenu(HELP_MENU);
    for (int i = 0; i < helpMenuItems.length; i++) {
      JMenuItem mItem = new JMenuItem(helpMenuItems[i]);
      mItem.addActionListener(this);
      helpMenu.add(mItem);
    }
    
    getRootPane().setJMenuBar(menuBar); 
    menuBar.add(fileMenu);
    menuBar.add(helpMenu);

    JToolBar toolBar = new JToolBar();
    toolBar.setLayout(new GridLayout(1, 1, 2, 2));

    JButton button = null;
    URL iconURL = getClass().getResource("Excel.gif");
    if (iconURL == null)
      button = new JButton(EXCEL_LABEL);
    else {
      ImageIcon icon = new ImageIcon(iconURL);
      button = new JButton(EXCEL_LABEL, icon);
    }
    button.setHorizontalTextPosition(JButton.CENTER);
    button.setVerticalTextPosition(JButton.BOTTOM);
    button.addActionListener(this);
    toolBar.add(button);
    getContentPane().add("North", toolBar);

    windowMenu = new JMenu(WINDOW_MENU);
    pack();
    setSize(400, 120);
    setVisible(true);
  }

  public void reinit(Experiment experiment) {
    this.experiment = experiment;
  }

  /**
   * Retrieve metrics files and copy them into directory specified by experiment.
   */

  private void getMetricsFiles() {
    CommunityServesClient communitySupport = new ClientCommunityController();
    HostComponent[] hosts = experiment.getHosts();
    for (int i = 0; i < hosts.length; i++) {
      String hostName = hosts[i].getShortName();
      HostServesClient hostInfo = null;
      try {
	hostInfo = communitySupport.getHost(hostName, DEFAULT_PORT);
      } catch (java.rmi.UnknownHostException uhe) {
	JOptionPane.showMessageDialog(this,
				      "Unknown host: " + hostName,
				      "Unknown Host",
				      JOptionPane.WARNING_MESSAGE);
	continue;
      } catch (Exception e) {
	// This happens if you listed random hosts which you don't
	// really intend to talk to
	JOptionPane.showMessageDialog(this,
				      "No response from host: " + hostName +
				      "; check that server is running.",
				      "No Response From Server",
				      JOptionPane.WARNING_MESSAGE);
	continue;
      }
      copyMetricsFiles(hostInfo);
    }
  }

  /**
   * Get the directory in which to store the metrics file.
   * If no directory is set, then display a file chooser, initted
   * to the cougaar install path, for the user to choose a directory.
   */

  private File getMetricsDir() {
    File metricsDir = experiment.getMetricsDirectory();
    if (metricsDir != null)
      return metricsDir;

    String metricsDirName = ".";
    try {
      metricsDirName = System.getProperty("org.cougaar.install.path");
    } catch (RuntimeException e) {
      // just use default
    }
    if (metricsDirName == null)
      metricsDirName = ".";
    JFileChooser chooser = new JFileChooser(metricsDirName);
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
	public boolean accept (File f) {
	  return f.isDirectory();
	}
	public String getDescription() {return "All Directories";}
      });
    int result = chooser.showDialog(this, "Select Metrics Storage Directory");
    if (result != JFileChooser.APPROVE_OPTION)
      return null;
    metricsDir = chooser.getSelectedFile();
    experiment.setMetricsDirectory(metricsDir);
    // TODO: shouldn't save happen automatically whenever an experiment is changed?
    csmart.saveWorkspace(); // force csmart to save the change
    return metricsDir;
  }

  /**
   * Read remote files and copy to directory specified by experiment.
   */

  private void copyMetricsFiles(HostServesClient hostInfo) {
    File metricsDir = getMetricsDir();
    if (metricsDir == null)
      return;
    char[] cbuf = new char[1000];
    try {
      String[] filenames = hostInfo.list("./");
      for (int i = 0; i < filenames.length; i++) {
	if (!isMetricFile(filenames[i]))
	  continue;
	File newMetricsFile = 
	  new File(metricsDir.getPath() + File.separator + filenames[i]);
	InputStream is = hostInfo.open(filenames[i]);
	BufferedReader reader = 
	  new BufferedReader(new InputStreamReader(is), 1000);
	BufferedWriter writer =
	  new BufferedWriter(new FileWriter(newMetricsFile));
	int len = 0;
	while ((len = reader.read(cbuf, 0, 1000)) != -1) {
	  writer.write(cbuf, 0, len);
	}
	reader.close();
	writer.close();
      }
    } catch (Exception e) {
      System.out.println("Analyzer: copyMetricsFiles: " + e);
      e.printStackTrace();
    }
  }

  /**
   * This checks all the societies in the experiment to determine if
   * any of them generated this metrics file.
   * Creating a new File from the filename works because acceptFile
   * just looks at the filename.
   */

  private boolean isMetricFile(String filename) {
    File thisFile = new java.io.File(filename);
    int n = experiment.getSocietyComponentCount();
    for (int i = 0; i < n; i++) {
      SocietyComponent societyComponent = experiment.getSocietyComponent(i);
      java.io.FileFilter fileFilter = societyComponent.getMetricsFileFilter();
      if (fileFilter == null)
	return false;
      return fileFilter.accept(thisFile);
    }
    return false;
  }

  /**
   * Display file chooser on metrics directory specified by experiment
   * and run excel on file chosen by user.
   */

  private void showExcelFile() {
    File metricsDir = experiment.getMetricsDirectory();
    // This should do the retrieve right here...
    if (metricsDir == null) {
      getMetricsDir();
      metricsDir = experiment.getMetricsDirectory();
    }
    
    if (metricsDir == null) {
        JOptionPane.showMessageDialog(this, "No Metrics Directory for Experiment",
  				    "No Metrics Directory", 
  				    JOptionPane.WARNING_MESSAGE);
        return;
    }
    JFileChooser fileChooser = new JFileChooser(metricsDir);
    fileChooser.setFileFilter(new MyFileFilter(experiment));
    int result = fileChooser.showOpenDialog(this);
    if (result != JFileChooser.APPROVE_OPTION) 
      return;
    String filePathName = fileChooser.getSelectedFile().getPath();
    String excel = "C:/Program Files/Microsoft Office/Office/EXCEL.EXE";
    try {
      excel = System.getProperty(EXCEL);
    } catch (RuntimeException e) {
      // just use default
    }
    String[] cmds = { excel, "" };
    System.out.println("Launching excel from: " + cmds[0]);
    cmds[1] = filePathName;
    try {
      process = Runtime.getRuntime().exec(cmds);
    } catch (Exception e) {
      System.out.println("Analyzer: exception: " + e);
      e.printStackTrace();
    }
  }

  public void actionPerformed(ActionEvent e) {
    String s = ((AbstractButton)e.getSource()).getActionCommand();
    if (s.equals(OPEN_MENU_ITEM)) {
      showExcelFile();
    } else if (s.equals(RETRIEVE_MENU_ITEM)) {
      getMetricsFiles();
    } else if (s.equals(EXCEL_LABEL)) {
      showExcelFile();
    } else if (s.equals(HELP_MENU_ITEM)) {
      URL help = (URL)getClass().getResource(HELP_DOC);
      if (help != null)
	Browser.setPage(help);
    } else if (s.equals(ABOUT_CSMART_ITEM)) {
      URL about = (URL)getClass().getResource(ABOUT_DOC);
      if (about != null)
	Browser.setPage(about);
    } else if (s.equals(EXIT_MENU_ITEM)) {
      // this will destroy the process, but is it the action we want?
      // the Excel process does not query the user before exiting
      //      process.destroy();
      int n = windowMenu.getItemCount();
      for (int i = 0; i < n; i++) {
	JMenuItem menuItem = windowMenu.getItem(i);
	JFrame f = (JFrame)titleToFrame.get(menuItem.getText());
	f.dispose();
      }
      NamedFrame.getNamedFrame().removeFrame(this);
      dispose();
    }
  }

  public static void main(String[] args) {
    new Analyzer(null);
  }

  class MyFileFilter extends javax.swing.filechooser.FileFilter {
    Experiment experiment;
    static final String description = "metrics files";

    MyFileFilter(Experiment experiment) {
      this.experiment = experiment;
    }

    /**
     * Accept any file accepted by any society in this experiment.
     */

    public boolean accept(File f) {
      int n = experiment.getSocietyComponentCount();
      for (int i = 0; i < n; i++) {
	SocietyComponent societyComponent = experiment.getSocietyComponent(i);
	java.io.FileFilter fileFilter = societyComponent.getMetricsFileFilter();
	if (fileFilter == null)
	  continue;
	if (fileFilter.accept(f))
	  return true;
      }
      return false;
    }

    public String getDescription() {
      return description;
    }
  }

}
