/*
 * <copyright>
 *  Copyright 2000-2002 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
 * 
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the Cougaar Open Source License as published by
 *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 *  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *  PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */

package org.cougaar.tools.csmart.ui.util;

import org.cougaar.util.ConfigFinder;

import java.io.File;
import org.cougaar.util.log.Logger;
import org.cougaar.tools.csmart.ui.viewer.CSMART;

/**
 * Utilities for CSMART GUI.
 */

public class Util {

  /**
   * Return a path for the specified filename; the path is determined
   * using ConfigFinder.  If there is any error, null is returned.
   * @param filename the filename for which to get the path
   * @return the pathname or null if an error
   */

  public static String getPath(String filename) {
    Logger log = CSMART.createLogger("org.cougaar.tools.csmart.ui.util.Util");
    ConfigFinder configFinder = ConfigFinder.getInstance();
    File file = configFinder.locateFile(filename);
    String path = null;
    try {
      path = file.getCanonicalPath();
    } catch (Exception e) {
      if(log.isErrorEnabled()) {
        log.error("Could not find: " + filename);
      }
    }
    return path;
  }

  /**
   * Get the single NamedFrame object for the CSMARTUL application.
   * The NamedFrame object ensures that all frames in the application
   * have unique names, and it notifies the CSMARTUL application when frames
   * are added/removed so that the menu of existing frames can be updated.
   * This method is pointless.
   * @deprecated The singleton NamedFrame should be obtained directly
   * using NamedFrame.getNamedFrame().
   * @return the NamedFrame object for the application
   **/
  public static NamedFrame getNamedFrame() {
    return NamedFrame.getNamedFrame();
  }

}




