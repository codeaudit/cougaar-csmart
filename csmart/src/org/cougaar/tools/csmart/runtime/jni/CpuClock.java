/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
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
