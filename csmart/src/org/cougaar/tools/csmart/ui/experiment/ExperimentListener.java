/* 
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 * 
 *       © Copyright 2001 by BBNT Solutions LLC.
 * </copyright>
 */
package org.cougaar.tools.csmart.ui.experiment;

import java.util.*;
import org.cougaar.tools.csmart.ui.component.SocietyComponent;

public interface ExperimentListener {

  /**
   * Return true if this listener is monitoring the experiment
   * and false otherwise.  If an experiment is being monitored,
   * then the user is notified by the Experiment before the Experiment
   * is manually terminated.
   * @return true if experiment is being monitored by the listener
   */

  public boolean isMonitoring();

  /**
   * Called when the experiment is terminated.
   * The listener should terminate any monitoring of the experiment.
   */

  public void experimentTerminated();
}
