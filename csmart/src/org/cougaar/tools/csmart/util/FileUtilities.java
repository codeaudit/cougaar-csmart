package org.cougaar.tools.csmart.util;

import org.cougaar.tools.csmart.experiment.Trial;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.util.log.Logger;

import java.io.File;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * org.cougaar.tools.csmart.util
 *
 */
public class FileUtilities {

  private static Logger log = CSMART.createLogger("FileUtilities");
  private static DateFormat fileDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

  /**
   * Create a log file name which is of the form:
   * node name + date + .log
   * Create it in the results directory if possible.
   */
  public static String getLogFileName(String nodeName, Date runStart) {
    String filename = nodeName + fileDateFormat.format(runStart) + ".log";
    String dirname = makeResultDirectory(runStart);
    if (dirname != null)
     filename = dirname + File.separatorChar + filename;
    return filename;
  }

  /**
   * Create a directory for the results of this run.
   * Results file structure is:
   * <ExperimentName>
   *    <TrialName>
   *       Results-<Timestamp>.results
   */
  private static String makeResultDirectory(Date runStart) {
    // defaults, if we don't have an experiment
    File resultDir = CSMART.getResultDir();
    String experimentName = "Experiment";
    String trialName = "Trial 1";

    // if user didn't specify results directory, save in local directory
    if (resultDir == null) {
      if (log.isInfoEnabled())
        log.info("No result directory specified. Should use a local dir. Returning null (in makeResultDirectory).");
      return null;
    }
    String dirname = resultDir.getAbsolutePath() + File.separatorChar +
        experimentName + File.separatorChar +
        trialName + File.separatorChar +
        "Results-" + fileDateFormat.format(runStart);
    try {
      File f = new File(dirname);
      // guarantee that directories exist
      if (!f.exists() && !f.mkdirs() && !f.exists()) {
        if (log.isWarnEnabled())
          log.warn("Unabled to create directory " + dirname + ". Should default to local directory - returning null (in makeResultDirectory)");
        return null;
      }
    } catch (Exception e) {
      if (log.isErrorEnabled()) {
        log.error("Couldn't create results directory " + dirname + ": ", e);
      }
      return null;
    }
    return dirname;
  }

}
