/*
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 *
 * © Copyright 2000, 2001 BBNT Solutions LLC
 * </copyright>
 */

package org.cougaar.tools.csmart.ui.psp;

/**
 * Used by PSPs to filter objects.
 */

public class MathUtils {

  public static boolean isLessThan(long a, long b) {
    return (a < b);
  }

  public static boolean isLessThanEqual(long a, long b) {
    return (a <= b);
  }

  public static boolean isGreaterThan(long a, long b) {
    return (a > b);
  }

  public static boolean isGreaterThanEqual(long a, long b) {
    return (a >= b);
  }

  public static boolean isEqual(long a, long b) {
    return (a == b);
  }

  public static boolean isNotEqual(long a, long b) {
    return (a != b);
  }
}

