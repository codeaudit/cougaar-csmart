/* 
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 * 
 *       � Copyright 2001 by BBNT Solutions LLC.
 * </copyright>
 */
package org.cougaar.tools.csmart.recipe;

import java.io.FileFilter;

import org.cougaar.tools.csmart.recipe.RecipeComponent;

/**
 * Interface marking all Metric components.<br>
 * These are configurable components that can be added to an experiment.
 *
 */
public interface MetricComponent extends RecipeComponent {
  String getMetricName();

  /**
   * Return a file filter which can be used to fetch
   * the metrics files for this experiment.
   * @return file filter to get metrics files for this experiment
   */
  FileFilter getResultFileFilter();

  /**
   * Return a file filter which can be used to delete
   * the files generated by this experiment.
   * @return file filter for cleanup
   */
   FileFilter getCleanupFileFilter();
}






