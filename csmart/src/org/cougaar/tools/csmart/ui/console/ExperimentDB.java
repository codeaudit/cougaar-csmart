/*
 * <copyright>
 *  Copyright 2000-2001 BBNT Solutions, LLC
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

package org.cougaar.tools.csmart.ui.console;

import java.util.Hashtable;

  /**
   * Interface to the database.
   * Currently returns dummy values.
   */

public class ExperimentDB {

  /*
   * Returns hashtable where the keys are experiment ids (Strings) and
   * the values are human readable names (Strings).
   */

  public static Hashtable getExperimentNames() {
    return null;
  }

  /*
   * Returns hashtable where the keys are trial ids (Strings) and
   * the values are human readable trial names (Strings).
   */

  public static Hashtable getTrialNames(String experimentId) {
    return null;
  }

  /**
   * Add the named trial to the experiment.  The name supplied is
   * the human readable name; the value returned is the new trial id or null.
   */

  public static String addTrialName(String experimentId, String name) {
    return null;
  }

  /*
   * Returns hashtable where the keys are society template ids (Strings) and
   * the values are human readable names (Strings).
   */

  public static Hashtable getSocietyTemplates(String experimentId) {
    return null;
  }

  public static String[] getOrganizationGroups(String experimentId) {
    String[] names = {
      "Third Infantry Division",
      "2nd Brigade"
    };
    return names;
  }

  public static boolean isULThreadSelected(String trialId, String threadName) {
    return false;
  }

  public static void setULThreadSelected(String trialId, String threadName, 
                                  boolean selected) {
    System.out.println("Thread: " + threadName +
                       " selected: " + selected);
  }

  public static boolean isGroupSelected(String trialId, String groupName) {
    return false;
  }

  public static void setGroupSelected(String trialId, String groupName, 
                               boolean selected) {
    System.out.println("Group: " + groupName +
                       " selected: " + selected);
  }
    
  public static int getMultiplier(String trialId, String groupName) {
    return 1;
  }

  public static void setMultiplier(String trialId, String groupName, 
                                   int value) {
    System.out.println("Group: " + groupName + " value: " + value);
  }

}
