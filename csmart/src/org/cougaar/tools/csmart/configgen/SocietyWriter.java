/*
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 *
 * © Copyright 2001 BBNT Solutions LLC
 * </copyright>
 */

package org.cougaar.tools.csmart.configgen;

public interface SocietyWriter  {

  /**
   * Take the command-line arguments and/or system properties and make sure 
   * they include all the necessary parameters.
   * <p>
   * Note: these arguments (<tt>args</tt>) are shared with the 
   * <code>SocietyBuilder</code> and should not be modified.  The arguments 
   * should also be carefully tagged or the order fixed to avoid
   * confusion.
   * @deprecated Use CSMART
   *
   * @see #write
   */
  public void initialize(
      String[] args) throws Exception;

  /**
   * Write the society to output, as specified in the <tt>initialize</tt>
   * arguments.
   */
  public void write(
      Society soc) throws Exception;

}
