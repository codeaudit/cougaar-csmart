/*
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 *
 * © Copyright 2000, 2001 BBNT Solutions LLC
 * </copyright>
 */

package org.cougaar.tools.csmart.ui.monitor.generic;

import java.awt.Color;
import att.grappa.GrappaColor;

/**
 * For debugging: print the r,g,b value of a specified color
 * in the grappa color map.
 * Usage: java GrappaColorUtility colorname
 */

public class GrappaColorUtility {

  public static void main(String[] args) {
    if (args.length != 1) {
      System.out.println("Usage: java GrappaColorUtility colorname");
      return;
    }
    Color c = GrappaColor.getColor(args[0], null);
    System.out.println("RGB values: " + c.getRed() + " " + c.getGreen() +
		       " " + c.getBlue());
  }
}
