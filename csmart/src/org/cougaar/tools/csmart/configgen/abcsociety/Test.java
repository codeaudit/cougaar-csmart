/* 
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 * 
 *       © Copyright 2001 by BBNT Solutions LLC.
 * </copyright>
 */
package org.cougaar.tools.csmart.configgen.abcsociety;

import org.cougaar.tools.csmart.ui.component.*;
import java.io.*;


public class Test {
  public static void test1(String configDir) {
    ABCSociety society = new ABCSociety("MySociety");

    society.initProperties();
    society.generateIniFiles(new File(configDir));
  }

  public static void main(String[] args) {
    if( args.length == 0 ) {
      System.out.println();
      System.out.println("Usage: Test <configDir>");
      System.out.println();
      System.exit(1);
    }
    test1(args[0]);
  }
}
