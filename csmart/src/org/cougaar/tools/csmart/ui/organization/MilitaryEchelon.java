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

package org.cougaar.tools.csmart.ui.organization;

// from service discovery code
public class MilitaryEchelon {
  public static final String UNDEFINED = "UNDEFINED";
  public static final String BRIGADE = "BRIGADE";
  public static final String DIVISION = "DIVISION";
  public static final String CORPS = "CORPS";
  public static final String THEATER = "THEATER";
  public static final String USARMY = "US-ARMY";
  public static final String JOINT = "JOINT";

  private static final String[] ECHELON_ORDER =
  {BRIGADE, DIVISION, CORPS, THEATER, USARMY, JOINT};
  
  public static boolean validMilitaryEchelon(String echelon) {
    return ((echelon.equals(BRIGADE)) ||
            (echelon.equals(DIVISION)) ||
            (echelon.equals(CORPS)) ||
            (echelon.equals(THEATER)) ||
            (echelon.equals(USARMY)) ||
            (echelon.equals(JOINT)) ||
            (echelon.equals(UNDEFINED)));
  } 

  public static String mapToMilitaryEchelon(String echelonValue) {
    // Upcase for comparison
    String upCase = echelonValue.toUpperCase();
    if (validMilitaryEchelon(upCase))
      return upCase;
    else
      return UNDEFINED;
  }
  
  public static String echelonName(int i) {
    if (i < 0 || i >= ECHELON_ORDER.length)
      return UNDEFINED;
    return ECHELON_ORDER[i];
  }

  public static int echelonOrder(String echelonValue) {
    // Upcase for comparison
    String upCase = echelonValue.toUpperCase();
    for (int index = 0; index < ECHELON_ORDER.length; index++) {
      if (upCase.equals(ECHELON_ORDER[index])) 
        return index;
    }
    return -1;
  } 

}
