/*
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 *
 * © Copyright 2000, 2001 BBNT Solutions LLC
 * </copyright>
 */
package org.cougaar.tools.csmart.ui.component;

import java.io.File;
import java.io.IOException;

/**
 * A simple interface implemented by configurable components 
 * that represent agents.<br>
 * They can write their line in the Node.ini file,
 * and can write their own Agent ini File.
 */
public interface AgentComponent extends ComponentProperties {
  /**
   * Write out one line for the Node ini file
   *
   * @return a <code>String</code> line for the file
   */
  public String getConfigLine();
  
  /**
   * Write the Agents ini file
   *
   * @param configDir a <code>File</code> path to write the file into
   * @exception IOException if an error occurs
   */
  public void writeIniFile(File configDir) throws IOException;
}
