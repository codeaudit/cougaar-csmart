package org.cougaar.tools.csmart.ui.console;

import org.cougaar.tools.csmart.recipe.MetricComponent;
import org.cougaar.tools.csmart.recipe.RecipeComponent;
import org.cougaar.tools.csmart.society.SocietyComponent;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.tools.csmart.util.FileUtilities;
import org.cougaar.tools.csmart.util.ResultsFileFilter;
import org.cougaar.tools.server.OutputListener;
import org.cougaar.tools.server.OutputPolicy;
import org.cougaar.tools.server.RemoteFileSystem;
import org.cougaar.tools.server.RemoteHost;
import org.cougaar.util.log.Logger;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.Observable;

/**
 * NodeModel contains all of the data structures required to
 * create and display a Node in the CSMART Console.
 *
 */
public class NodeModel extends Observable {
  private NodeStatusButton statusButton;
  private ConsoleStyledDocument doc;
  private ConsoleTextPane textPane;
  private ConsoleNodeListener listener;
  private String logFileName;
  private String nodeName;
  private Logger log;
  private NodeInfo info;
  private CSMARTConsoleModel cmodel;
  private String notifyCondition;
  private int viewSize = CSMARTConsoleModel.DEFAULT_VIEW_SIZE; // number of characters in node view
  private boolean notifyOnStandardError = false; // if stderr appears, notify user
  private ConsoleNodeOutputFilter displayFilter;
  private boolean running = false;
  private OutputPolicy outputPolicy;
  private CreateNodeThread thread;

  private NodeModel() {
    // We don't want a generic constructor, so make it private.
  }

  public NodeModel(NodeInfo info, CSMARTConsoleModel cmodel) {
    this.info = info;
    this.cmodel = cmodel;
    this.nodeName = info.getNodeName();
    createLogger();
    statusButton = createStatusButton(info.getNodeName(), info.getHostName());
    doc = new ConsoleStyledDocument();
    textPane = new ConsoleTextPane(doc, statusButton);
    logFileName = FileUtilities.getLogFileName(info.getNodeName(), new Date());
    this.outputPolicy = new OutputPolicy(10);
    this.thread = new CreateNodeThread(this);

    createListener();
    createFilters();
  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  // create a node event listener to get events from the node
  private void createListener() {
    try {
      listener = new ConsoleNodeListener(this);
      if (listener.statusButton == null ||
          listener.statusButton.getMyModel().getStatus() == NodeStatusButton.STATUS_NODE_DESTROYED ||
          listener.statusButton.getMyModel().getStatus() == NodeStatusButton.STATUS_NO_ANSWER) {
        listener.cleanUp();
      }
    } catch (Exception e) {
      if (log.isErrorEnabled()) {
        log.error("Unable to create output for: " + info.getNodeName(), e);
      }
    }
  }

  // Set up Node filters & notifications
  private void createFilters() {
    this.notifyCondition = cmodel.getGlobalNotifyCondition();
    if (notifyCondition != null) {
      textPane.setNotifyCondition(notifyCondition);
    }
    ((ConsoleStyledDocument) textPane.getStyledDocument()).setBufferSize(viewSize);
    if (notifyOnStandardError) {
      statusButton.getMyModel().setNotifyOnStandardError(true);
    }
    if (displayFilter != null) {
      listener.setFilter(displayFilter);
    }

  }


  /**
   * Create a button representing a node.
   */
  private NodeStatusButton createStatusButton(String nodeName, String hostName) {
    NodeStatusButton button = new NodeStatusButton(new ColoredCircle(NodeStatusButton.unknownStatus, 20, null));
    button.setSelectedIcon(new SelectedColoredCircle(NodeStatusButton.unknownStatus, 20, null));
    button.setToolTipText("Node " + nodeName + " (" + hostName + "), unknown");
    button.setActionCommand(nodeName);
    button.setFocusPainted(false);
    button.setBorderPainted(false);
    button.setContentAreaFilled(false);
    button.setMargin(new Insets(2, 2, 2, 2));
    return button;
  }

  public NodeStatusButton getStatusButton() {
    return this.statusButton;
  }

  public String getNodeName() {
    return nodeName;
  }

  public String getLogFileName() {
    return logFileName;
  }

  public ConsoleStyledDocument getDoc() {
    return doc;
  }

  public boolean isRunning() {
    return running;
  }

  public void setRunning(boolean running) {
    System.out.println("running = " + running);
    this.running = running;
  }

  public NodeInfo getInfo() {
    return info;
  }

  public ConsoleTextPane getTextPane() {
    return textPane;
  }

  public ConsoleNodeListener getListener() {
    return listener;
  }

  public OutputPolicy getOutputPolicy() {
    return outputPolicy;
  }

  public void start() {
    thread.start();
  }

  public void stop() {
    System.out.println("I am stopping now.");
    thread.interrupt();
    System.out.println("Interrupted? " + thread.isInterrupted());
  }

  /**
   * Create a file for the results of this run.
   * Results file structure is:
   * <ExperimentName>
   *       Results-<Timestamp>.results
   */
  private void saveResults() {
    String dirname = null; // FIXME makeResultDirectory();
    // Must check for null return here!?
    if (dirname == null) {
      // User didn't specify a directory or couldn't create one or something?
      if (log.isInfoEnabled())
        log.info("saveResults got no good result directory from makeResult: Using pwd.");
      // Is . really the right choice here?
      dirname = ".";
    }

    RemoteHost appServer = getInfo().getAppServer();
    RemoteFileSystem remoteFS = null;
    try {
      remoteFS = appServer.getRemoteFileSystem();
    } catch (Exception e) {
      if (log.isErrorEnabled())
        log.error("saveResults failed to get filesystem on " +
                  getInfo().getHostName() + ": ", e);
      remoteFS = null;
    }
    if (remoteFS == null) {
      final String host = getInfo().getHostName();
      SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            JOptionPane.showMessageDialog(null,
                  "Cannot save results.  Unable to access filesystem for " +
                                          host + ".",
                                          "Unable to access file system",
                                          JOptionPane.WARNING_MESSAGE);
          }
        });
    } else {
      copyResultFiles(remoteFS, dirname);
    }
  }

  /**
   * Read remote files and copy to directory specified by experiment.
   */
  private void copyResultFiles(RemoteFileSystem remoteFS, String dirname) {
    char[] cbuf = new char[1000];
    try {
      // FIXME: This reads just from the current directory,
      // but should read from wherever the BasicMetric told it to read,
      // or in general, wherever the Component says to read
      // But does the AppServer support calling list on arbitrary paths?
      // See bug 1668
      // Maybe to generalize, let this traverse sub-directories?
      String[] filenames = remoteFS.list("./");
      for (int i = 0; i < filenames.length; i++) {
        if (!isResultFile(filenames[i]))
          continue;
        File newResultFile = new File(dirname + File.separator + filenames[i]);
        InputStream is = remoteFS.read(filenames[i]);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is), 1000);
        BufferedWriter writer = new BufferedWriter(new FileWriter(newResultFile));
        int len = 0;
        while ((len = reader.read(cbuf, 0, 1000)) != -1) {
          writer.write(cbuf, 0, len);
        }
        reader.close();
        writer.close();
      }
    } catch (Exception e) {
      if (log.isErrorEnabled()) {
        log.error("CSMARTConsole: copyResultFiles failed: ", e);
      }
    }
  }

  /**
   * This checks the society and recipes in the experiment to determine if
   * any of them generated this metrics file.
   * Creating a new File from the filename works because acceptFile
   * just looks at the filename.
   */
  private boolean isResultFile(String filename) {
    File thisFile = new java.io.File(filename);
    // if no experiment, use default filter
    if (cmodel.getExperiment() == null)
      return new ResultsFileFilter().accept(thisFile);
    SocietyComponent societyComponent = cmodel.getExperiment().getSocietyComponent();
    if (societyComponent != null) {
      java.io.FileFilter fileFilter = societyComponent.getResultFileFilter();
      if (fileFilter != null && fileFilter.accept(thisFile))
        return true;
    }
    int nrecipes = cmodel.getExperiment().getRecipeComponentCount();
    for (int i = 0; i < nrecipes; i++) {
      RecipeComponent recipeComponent = cmodel.getExperiment().getRecipeComponent(i);
      if (recipeComponent instanceof MetricComponent) {
        MetricComponent metricComponent = (MetricComponent) recipeComponent;
        java.io.FileFilter fileFilter = metricComponent.getResultFileFilter();
        if (fileFilter != null && fileFilter.accept(thisFile))
          return true;
      }
    }
    return false;
  }

  /**
   * Create a directory for the results of this run.
   * Results file structure is:
   * <ExperimentName>
   *    <TrialName>
   *       Results-<Timestamp>.results
   */
// private String makeResultDirectory() {
//   // defaults, if we don't have an experiment
//   File resultDir = CSMART.getResultDir();
//   String experimentName = "Experiment";
//   String trialName = "Trial 1";
//   if (experiment != null && usingExperiment) {
//     resultDir = experiment.getResultDirectory();
//     experimentName = experiment.getExperimentName();
//     Trial trial = experiment.getTrial();
//     if (trial != null) {
//       trialName = trial.getShortName();
//     } else {
//       if (log.isWarnEnabled())
//         log.warn("Null trial in experiment " + experimentName);
//     }
//   }
//   // if user didn't specify results directory, save in local directory
//   if (resultDir == null) {
//     if (log.isInfoEnabled())
//       log.info("No result directory specified. Should use a local dir. Returning null (in makeResultDirectory).");
//     return null;
//   }
//   String dirname = resultDir.getAbsolutePath() + File.separatorChar +
//       experimentName + File.separatorChar +
//       trialName + File.separatorChar +
//       "Results-" + fileDateFormat.format(runStart);
//   try {
//     File f = new File(dirname);
//     // guarantee that directories exist
//     if (!f.exists() && !f.mkdirs() && !f.exists()) {
//       if (log.isWarnEnabled())
//         log.warn("Unabled to create directory " + dirname + ". Should default to local directory - returning null (in makeResultDirectory)");
//       return null;
//     }
//   } catch (Exception e) {
//     if (log.isErrorEnabled()) {
//       log.error("Couldn't create results directory " + dirname + ": ", e);
//     }
//     return null;
//   }
//   return dirname;
// }

}
