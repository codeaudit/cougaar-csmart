/* 
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 * 
 *       © Copyright 2001 by BBNT Solutions LLC.
 * </copyright>
 */
package org.cougaar.tools.csmart.ldm.asset;

public interface SimpleInventoryBG {

  long consume(long time);

  long getStartTime();

  long getEndTime();

  double getInventoryLevelAt(long time);

  void setInventoryLevelAt(long time, double level);

  double[] toArray(
      long startTime, long endTime, long timeIncrement);

  double[] toArray(
      double[] toA, 
      long startTime, long endTime, long timeIncrement);

  SimpleInventoryBG copy(SimpleInventoryPG hapPG);

  public String toString();

  public String toString(boolean verbose);
  
}
