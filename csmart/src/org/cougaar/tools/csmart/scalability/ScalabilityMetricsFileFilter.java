/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.tools.csmart.scalability;

import java.io.File;
import java.io.FileFilter;
import java.io.Serializable;

/**
 * An implementation of FileFilter that filters out
 * all files except for ".txt" files with names ending in "results".
 */

public class ScalabilityMetricsFileFilter implements FileFilter, Serializable {

  /**
   * Returns true if this file is a metrics file and false otherwise.
   */

  public boolean accept(File f) {
    if (f == null)
      return false;
    return f.getName().endsWith("results.txt");
  }

}


