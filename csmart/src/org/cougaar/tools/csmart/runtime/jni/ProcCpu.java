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
import java.io.*;

/**
 * This class attempts to read the cpu load stats from the /proc filesystem.
 * It totals up the user and kernel cpu time for the process group that includes
 * the specified process.  This way it can accumulate stats for all of the java threads
 * in an application.
 **/
class ProcCpu {
  public static void main(String args[]) {
    ProcCpu p = new ProcCpu(args[0]);
    System.out.println("Collecting CPU stats for "+args[0]);
    int jiffies = p.get();
    System.out.println("Jiffies = "+jiffies);
  }

  /**
   * This method returs true iff this system supports the /proc filesystem.
   */
  public static boolean isOK() {
    File self = new File("/proc/self/stat");
    return self.exists();
  }


  private String pid = null;
  /**
   * Monitor the specified pid.
   */
  ProcCpu(String pid) {
    this.pid = pid;
  }

  /**
   * Monitor the current pid.
   */
  ProcCpu() {}
  
  /** 
   * Return the (cumulative) number of jiffies (1/100 sec) used
   * by this process.
   * (Actually all of the processes in the process group)
   */
  int get() {
  
    File self;
    BufferedReader f;
    String l;
    Proc p;
    int pgroup = -1;

    try {
      if (pid == null)
        self = new File("/proc/self/stat");
      else 
        self = new File("/proc/"+pid+"/stat");
      f = new BufferedReader(new InputStreamReader(new FileInputStream(self)));
      l = f.readLine();
      p = new Proc(l);
      pgroup = p.pgrp;
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    

    int jiffies = 0;
    File pd = new File("/proc");
    if (pd.isDirectory()) {
      File pfs[] = pd.listFiles();
      for (int i = 0; i<pfs.length; i++) {
        File pf = pfs[i];
        if (pf.isDirectory() &&
            "0123456789".indexOf(pf.getName().charAt(0))>=0) {
          try {
            File sf = new File(pf, "stat");
            f = new BufferedReader(new InputStreamReader(new FileInputStream(sf)));
            l = f.readLine();
            p = new Proc(l);
            // accumulate totals for the proces group
            if (p.pgrp == pgroup) {
              jiffies += p.utime+p.stime;
            }
            f.close();
          } catch (IOException ioe) {
            //ioe.printStackTrace();
          }
        }
      }
    }
    return jiffies;
  }

  /**
   * This is a struct for holding CPU performance data.
   */
  static class Proc {
    int pid;
    String cmd;
    String state;
    int ppid;
    int pgrp;
    int session;
    int utime;
    int stime;

    Proc(String l) {
      Vector v = explode(l);
      pid = Integer.parseInt((String)v.elementAt(0));
      cmd = (String) v.elementAt(1);
      state = (String) v.elementAt(2);
      ppid = Integer.parseInt((String) v.elementAt(3));
      pgrp = Integer.parseInt((String) v.elementAt(4));
      session = Integer.parseInt((String) v.elementAt(5));

      utime = Integer.parseInt((String) v.elementAt(13));
      stime = Integer.parseInt((String) v.elementAt(14));
      // ignore the rest
    }
  }
    
  public static Vector explode(String s) {
    Vector v = new Vector();
    int j = 0;                  //  non-white
    int k = 0;                  // char after last white
    int l = s.length();
    int i = 0;
    while (i < l) {
      if (Character.isWhitespace(s.charAt(i))) {
        // is white - what do we do?
        if (i == k) {           // skipping contiguous white
          k++;
        } else {                // last char wasn't white - word boundary!
          v.addElement(s.substring(k,i));
          k=i+1;
        }
      } else {                  // nonwhite
        // let it advance
      }
      i++;
    }
    if (k != i) {               // leftover non-white chars
      v.addElement(s.substring(k,i));
    }
    return v;
  }


}
