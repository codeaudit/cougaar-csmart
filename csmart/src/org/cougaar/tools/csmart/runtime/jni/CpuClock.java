/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
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
package org.cougaar.tools.csmart.runtime.jni;

import java.util.*;

public class CpuClock {
  private static boolean needLibrary = true;
  private static boolean haveLibrary = false;
  private static ProcCpu cpu;

  /**
   * Determine whether we can use the /proc filesystem for stats.
   * If not, use the JNI code.
   */
  static {
    if (ProcCpu.isOK()) {
      System.out.println("Using Java CPU measurment");
      cpu = new ProcCpu();
    } else {
      System.out.println("Using JNI CPU measurment");
      cpu = null;
   }
  }

  /**
   * Load library if necessary. Check for loading errors and disable
   * cpu clock if library can't be loaded.
   * @return true if the libary is available.
   **/
  private static synchronized boolean checkLibrary() {
    if (needLibrary) {
      try {
        System.loadLibrary("mylib");
        haveLibrary = true;
      } catch (UnsatisfiedLinkError e) {
        System.err.println("CpuClock exception: " + e);
      }
      needLibrary = false;
    }
    return haveLibrary;
  }

  //Native method declaration
  private static native int clock();

  private static int checkedClock() {
    if (checkLibrary()) {
      return clock();
    } else {
      return 0;
    }
  }

  /**
   * clock() returns deltas, we accumulate them so we can report total cpu time.
   **/
  private static long cpuAccumulator = 0L;

  /**
   * Get total cpu time in milliseconds. This is the basis for every
   * other use of cpu time.
   **/
  public static synchronized long cpuTimeMillis() {
    if (cpu != null) {
      cpuAccumulator = cpu.get() * 10; // convert from jiffies to millis
    } else {
      cpuAccumulator += checkedClock();
    }

    return cpuAccumulator;
  }

  long lastTime = System.currentTimeMillis();
  long lastCPU = cpuTimeMillis();

  /**
   * Get ratio of cpu time to real time.
   **/
  public double cpuLoad() {
    return cpuLoad(true);
  }

  /**
   * Get the cpu utilization -- the ratio of cpu time to real time. If
   * no real time has elapsed, returns 0.0.
   * @param reset resets the bases for the next call to this method to
   * zero.
   **/
  public double cpuLoad(boolean reset) {
    long thisCPU = cpuTimeMillis();
    long thisTime = System.currentTimeMillis();
    long elapsed = thisTime - lastTime;
    long cpu = Math.min(elapsed, thisCPU - lastCPU);
    if (elapsed == 0) return 0.0;
    if (reset) {
      lastCPU += cpu;
      lastTime += elapsed;
    }
    return (double) cpu/(double) elapsed;
  }

  public static void main(String args[]) {
    //Create class instance
    CpuClock c = new CpuClock();

    //Call native method to load ReadFile.java
    int time = c.checkedClock();
    //Print contents of ReadFile.java
    for (int loops = 0; loops < 3; loops++) {
      System.out.println("LOOP: Processor load is: " + c.cpuLoad());
      for (int i=0; i<40000000; i++) {
        int j = i/3;
        Math.sqrt(j);
      }
    }
    System.out.println("Processor load  is: " + c.cpuLoad());
    System.out.println("Processor load  is: " + c.cpuLoad());
    try {Thread.sleep(2000);} catch (Exception e){}
    System.out.println("SLEEP: Processor load  is: " + c.cpuLoad());
  }
}
